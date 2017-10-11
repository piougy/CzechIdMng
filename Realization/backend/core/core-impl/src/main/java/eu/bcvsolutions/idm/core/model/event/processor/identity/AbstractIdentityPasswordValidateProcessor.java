package eu.bcvsolutions.idm.core.model.event.processor.identity;

import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.PasswordChangeType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractIdentityProcessor;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Abstract class that incorporates all logic necessary for validating user's password
 *
 * @author Peter Sourek <peter.sourek@bcvsolutions.eu>
 */
public abstract class AbstractIdentityPasswordValidateProcessor extends AbstractIdentityProcessor {


	private final IdmPasswordService passwordService;
	private final IdmPasswordPolicyService passwordPolicyService;
	private final AuthenticationManager authenticationManager;
	private final IdentityConfiguration identityConfiguration;
	private final SecurityService securityService;

	public AbstractIdentityPasswordValidateProcessor(IdentityConfiguration identityConfiguration,
	                                                 IdmPasswordService passwordService,
	                                                 AuthenticationManager authenticationManager,
	                                                 IdmPasswordPolicyService passwordPolicyService,
	                                                 SecurityService securityService, EventType... types
	) {
		super(types);
		//
		Assert.notNull(identityConfiguration);
		Assert.notNull(passwordService);
		Assert.notNull(authenticationManager);
		Assert.notNull(passwordPolicyService);
		Assert.notNull(securityService);
		//
		this.securityService = securityService;
		this.identityConfiguration = identityConfiguration;
		this.passwordService = passwordService;
		this.authenticationManager = authenticationManager;
		this.passwordPolicyService = passwordPolicyService;
	}


	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto identity = event.getContent();
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties()
			.get(IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO);
		Assert.notNull(passwordChangeDto);
		//
		if (requiresOriginalPassword()) {
			PasswordChangeType passwordChangeType = identityConfiguration.getPasswordChangeType();
			if (passwordChangeType == PasswordChangeType.DISABLED) {
				// check if isn't disable password change
				throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_DISABLED);
			} else if (passwordChangeType == PasswordChangeType.ALL_ONLY && !passwordChangeDto.isAll()) {
				// for all only must change also password for czechidm
				throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_ALL_ONLY);
			}
			// check old password - the same identity only
			// checkAccess(identity, IdentityBasePermission.PASSWORDCHANGE) is called before event publishing
			if (identity.getId().equals(securityService.getCurrentId())) {
				if (passwordChangeDto.getOldPassword() == null) {
					throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_CURRENT_FAILED_IDM);
				}
				//
				if (identityConfiguration.isRequireOldPassword()) {
					// authentication trough chain
					boolean successChainAuthentication = authenticationManager.validate(identity.getUsername(), passwordChangeDto.getOldPassword());
					if (!successChainAuthentication) {
						throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_CURRENT_FAILED_IDM);
					}
				}
			}
		}


		if (passwordChangeDto.isAll() || passwordChangeDto.isIdm()) { // change identity's password
			// validate password
			IdmPasswordValidationDto passwordValidationDto = new IdmPasswordValidationDto();
			// set old password for validation - valid till, from and history check
			IdmPasswordDto oldPassword = this.passwordService.findOneByIdentity(identity.getId());
			passwordValidationDto.setOldPassword(oldPassword == null ? null : oldPassword.getId());
			passwordValidationDto.setPassword(passwordChangeDto.getNewPassword());
			passwordValidationDto.setIdentity(identity);
			this.passwordPolicyService.validate(passwordValidationDto);
		}
		return new DefaultEventResult<>(event, this);
	}


	protected abstract boolean requiresOriginalPassword();
}
