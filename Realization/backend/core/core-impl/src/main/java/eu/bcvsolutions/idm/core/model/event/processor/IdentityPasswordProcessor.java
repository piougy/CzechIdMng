package eu.bcvsolutions.idm.core.model.event.processor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.IdentityOperationType;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.security.api.service.SecurityService;

/**
 * Save identity's password
 * 
 * @author Radek Tomiška
 *
 */
@Order(0)
@Component
public class IdentityPasswordProcessor extends AbstractEntityEventProcessor<IdmIdentity> {

	public static final String PROPERTY_PASSWORD_CHANGE_DTO = "idm:password-change-dto"; 
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityPasswordProcessor.class);
	private final SecurityService securityService;
	private final ConfidentialStorage confidentialStorage;
	
	@Autowired
	public IdentityPasswordProcessor(
			SecurityService securityService,
			ConfidentialStorage confidentialStorage) {
		super(IdentityOperationType.PASSWORD);
		//
		Assert.notNull(securityService);
		Assert.notNull(confidentialStorage);
		//
		this.securityService = securityService;
		this.confidentialStorage = confidentialStorage;
	}

	@Override
	public EntityEvent<IdmIdentity> process(EntityEvent<IdmIdentity> context) {
		IdmIdentity identity = context.getContent();
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) context.getProperties().get(PROPERTY_PASSWORD_CHANGE_DTO);
		Assert.notNull(identity);
		Assert.notNull(passwordChangeDto);
		//		
		if (!securityService.isAdmin()) {
			if(passwordChangeDto.getOldPassword() == null) {
				throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_CURRENT_FAILED_IDM);
			}
			// previous password check
			GuardedString idmPassword = confidentialStorage.getGuardedString(identity, IdmIdentityService.PASSWORD_CONFIDENTIAL_PROPERTY);
			if(!StringUtils.equals(String.valueOf(idmPassword.asString()), passwordChangeDto.getOldPassword().asString())) {
				throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_CURRENT_FAILED_IDM);
			}
		}
		if (passwordChangeDto.isIdm()) { // change identity's password
			savePassword(identity, passwordChangeDto.getNewPassword());
		}
		return context;
	}
	
	/**
	 * Saves identity's password to confidential storage
	 * 
	 * @param identity
	 * @param newPassword
	 */
	protected void savePassword(IdmIdentity identity, GuardedString newPassword) {
		LOG.debug("Saving password for identity [{}] to configental storage under key [{}]", identity.getUsername(), IdmIdentityService.PASSWORD_CONFIDENTIAL_PROPERTY);
		confidentialStorage.save(identity, IdmIdentityService.PASSWORD_CONFIDENTIAL_PROPERTY, newPassword.asString());
	}
	
	/**
	 * Delete identity's password from confidential storage
	 * 
	 * @param identity
	 */
	protected void deletePassword(IdmIdentity identity) {
		LOG.debug("Deleting password for identity [{}] to configental storage under key [{}]", identity.getUsername(), IdmIdentityService.PASSWORD_CONFIDENTIAL_PROPERTY);
		confidentialStorage.delete(identity, IdmIdentityService.PASSWORD_CONFIDENTIAL_PROPERTY);
	}
}