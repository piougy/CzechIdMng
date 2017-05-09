package eu.bcvsolutions.idm.core.model.event.processor.identity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordService;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Validate identity password
 * 
 * @author Radek Tomiška
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component
@Description("Validates identity's password, when password is changed.")
public class IdentityPasswordValidateProcessor extends CoreEventProcessor<IdmIdentityDto> {

	public static final String PROCESSOR_NAME = "identity-password-validate-processor";
	private final SecurityService securityService;
	private final IdmPasswordService passwordService;
	private final IdmPasswordPolicyService passwordPolicyService;
	private final IdmConfigurationService configuration;
	private final AuthenticationManager authenticationManager;

	@Autowired
	public IdentityPasswordValidateProcessor(SecurityService securityService,
			IdmPasswordService passwordService, IdmPasswordPolicyService passwordPolicyService,
			IdmConfigurationService configuration,
			AuthenticationManager authenticationManager) {
		super(IdentityEventType.PASSWORD);
		//
		Assert.notNull(securityService);
		Assert.notNull(passwordPolicyService);
		Assert.notNull(passwordService);
		Assert.notNull(configuration);
		Assert.notNull(authenticationManager);
		//
		this.securityService = securityService;
		this.passwordService = passwordService;
		this.passwordPolicyService = passwordPolicyService;
		this.configuration = configuration;
		this.authenticationManager = authenticationManager;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto identity = event.getContent();
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties()
				.get(IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO);
		Assert.notNull(passwordChangeDto);
		//
		if (!securityService.isAdmin()) {
			if (passwordChangeDto.getOldPassword() == null) {
				throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_CURRENT_FAILED_IDM);
			}
			// get configuration
			boolean oldPasswordRequired = configuration.getBooleanValue("idm.pub.core.identity.passwordChange.requireOldPassword", true);
			//
			if (oldPasswordRequired) {
				// authentication trough chain 
				boolean successChainAuthentication = authenticationManager.validate(identity.getUsername(), passwordChangeDto.getOldPassword());
				if (!successChainAuthentication) {
					throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_CURRENT_FAILED_IDM);
				}
			}
		}

		if (passwordChangeDto.isAll() || passwordChangeDto.isIdm()) { // change identity's password
			// validate password
			IdmPasswordValidationDto passwordValidationDto = new IdmPasswordValidationDto();
			// set old password for validation - valid till, from and history check
			passwordValidationDto.setOldPassword(this.passwordService.get(identity) == null ? null : this.passwordService.get(identity).getId());
			passwordValidationDto.setPassword(passwordChangeDto.getNewPassword());
			passwordValidationDto.setIdentity(identity == null ? null : identity.getId());
			this.passwordPolicyService.validate(passwordValidationDto);
		}
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return super.getOrder() - 100;
	}
}
