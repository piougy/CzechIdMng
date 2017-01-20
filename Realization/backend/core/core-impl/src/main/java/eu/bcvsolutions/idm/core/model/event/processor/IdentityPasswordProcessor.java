package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordService;

/**
 * Save identity's password
 * 
 * @author Radek Tomiška
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
public class IdentityPasswordProcessor extends CoreEventProcessor<IdmIdentity> {

	public static final String PROPERTY_PASSWORD_CHANGE_DTO = "idm:password-change-dto"; 
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityPasswordProcessor.class);
	private final IdmPasswordService passwordService;
	
	@Autowired
	public IdentityPasswordProcessor(IdmPasswordService passwordService) {
		super(IdentityEventType.PASSWORD);
		//
		Assert.notNull(passwordService);
		//
		this.passwordService = passwordService;
	}

	@Override
	public EventResult<IdmIdentity> process(EntityEvent<IdmIdentity> event) {
		IdmIdentity identity = event.getContent();
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties().get(PROPERTY_PASSWORD_CHANGE_DTO);
		Assert.notNull(passwordChangeDto);
		//		
		if (passwordChangeDto.isAll() || passwordChangeDto.isIdm()) { // change identity's password			
			savePassword(identity, passwordChangeDto);
		}
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Saves identity's password
	 * 
	 * @param identity
	 * @param newPassword
	 */
	protected void savePassword(IdmIdentity identity, PasswordChangeDto passwordDto) {
		LOG.debug("Saving password for identity [{}].", identity.getUsername());
		this.passwordService.save(identity, passwordDto);
	}
	
	/**
	 * Delete identity's password from confidential storage
	 * 
	 * @param identity
	 */
	protected void deletePassword(IdmIdentity identity) {
		LOG.debug("Deleting password for identity [{}]. ", identity.getUsername());
		this.passwordService.delete(identity);
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 100;
	}
}