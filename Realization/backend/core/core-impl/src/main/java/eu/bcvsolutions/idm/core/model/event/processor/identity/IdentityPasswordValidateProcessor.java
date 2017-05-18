package eu.bcvsolutions.idm.core.model.event.processor.identity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.PasswordChangeType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
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

	private static final String PROPERTY_REQUIRE_OLD_PASSWORD = "idm.pub.core.identity.passwordChange.requireOldPassword";
	// private static final String PROPERTY_PASSWORD_CHANGE_TYPE = "idm.pub.core.identity.passwordChange"; // TODO: secure BE
	public static final String PROCESSOR_NAME = "identity-password-validate-processor";
	private final SecurityService securityService;
	private final IdmPasswordService passwordService;
	private final IdmPasswordPolicyService passwordPolicyService;
	private final IdmConfigurationService configuration;
	private final AuthenticationManager authenticationManager;
	private final ConfigurationService configurationService;

	@Autowired
	public IdentityPasswordValidateProcessor(SecurityService securityService,
			IdmPasswordService passwordService, IdmPasswordPolicyService passwordPolicyService,
			IdmConfigurationService configuration,
			AuthenticationManager authenticationManager,
			ConfigurationService configurationService) {
		super(IdentityEventType.PASSWORD);
		//
		Assert.notNull(securityService);
		Assert.notNull(passwordPolicyService);
		Assert.notNull(passwordService);
		Assert.notNull(configuration);
		Assert.notNull(authenticationManager);
		Assert.notNull(configurationService);
		//
		this.securityService = securityService;
		this.passwordService = passwordService;
		this.passwordPolicyService = passwordPolicyService;
		this.configuration = configuration;
		this.authenticationManager = authenticationManager;
		this.configurationService = configurationService;
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
			// check if isn't disable password change
			String passwordChangeProperty = this.configurationService.getValue(IdentityConfiguration.PROPERTY_IDENTITY_CHANGE_PASSWORD);
			if (passwordChangeProperty.equals(PasswordChangeType.DISABLED.toString())) {
				throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_FAILED, ImmutableMap.of("note", "Password change is disabled"));
			} else if (passwordChangeProperty.equals(PasswordChangeType.ALL_ONLY.toString())) {
				// for all only must change also password for czechidm
				if (!passwordChangeDto.isIdm()) {
					throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_FAILED, ImmutableMap.of("note", "Password is allowed change only for all accounts."));
				}
			}
			//
			// check old password
			if (passwordChangeDto.getOldPassword() == null) {
				throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_CURRENT_FAILED_IDM);
			}
			// get configuration
			boolean oldPasswordRequired = configuration.getBooleanValue(PROPERTY_REQUIRE_OLD_PASSWORD, true);			
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
		return super.getOrder() - 1100;
	}
}
