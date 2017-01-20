package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmPassword;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordService;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.security.api.service.SecurityService;

/**
 * Validate identity password
 * 
 * @author Radek Tomiška
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
public class IdentityPasswordValidateProcessor extends CoreEventProcessor<IdmIdentity> {

	public static final String PROPERTY_PASSWORD_CHANGE_DTO = "idm:password-change-dto";
	private final SecurityService securityService;
	private final IdmPasswordService passwordService;
	private final IdmPasswordPolicyService passwordPolicyService;

	@Autowired
	public IdentityPasswordValidateProcessor(SecurityService securityService,
			IdmPasswordService passwordService, IdmPasswordPolicyService passwordPolicyService) {
		super(IdentityEventType.PASSWORD);
		//
		Assert.notNull(securityService);
		Assert.notNull(passwordPolicyService);
		Assert.notNull(passwordService);
		//
		this.securityService = securityService;
		this.passwordService = passwordService;
		this.passwordPolicyService = passwordPolicyService;
	}

	@Override
	public EventResult<IdmIdentity> process(EntityEvent<IdmIdentity> event) {
		IdmIdentity identity = event.getContent();
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties()
				.get(PROPERTY_PASSWORD_CHANGE_DTO);
		Assert.notNull(passwordChangeDto);
		//
		if (!securityService.isAdmin()) {
			if (passwordChangeDto.getOldPassword() == null) {
				throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_CURRENT_FAILED_IDM);
			}
			// previous password check
			IdmPassword idmPassword = passwordService.get(identity);
			if (!passwordService.checkPassword(passwordChangeDto.getOldPassword(), idmPassword)) {
				throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_CURRENT_FAILED_IDM);
			}
			// can change password, minimal age for change
		}

		if (passwordChangeDto.isAll() || passwordChangeDto.isIdm()) { // change identity's password
			// validate password
			this.passwordPolicyService.validate(passwordChangeDto.getNewPassword().asString());
		}
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return super.getOrder() - 100;
	}

}
