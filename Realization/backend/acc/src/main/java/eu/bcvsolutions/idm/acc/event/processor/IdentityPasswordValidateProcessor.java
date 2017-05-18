package eu.bcvsolutions.idm.acc.event.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.domain.PasswordChangeType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityPasswordProcessor;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordService;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Processor with password validation.
 * Get all accounts and their distinct systems.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component("accIdentityPasswordValidateProcessor")
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Validates identity's and all selected systems password, when password is changed.")
public class IdentityPasswordValidateProcessor extends AbstractEntityEventProcessor<IdmIdentityDto> {
	
	public static final String PROCESSOR_NAME = "identity-password-validate-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityPasswordValidateProcessor.class);
	private final IdmPasswordPolicyService passwordPolicyService;
	private final AccIdentityAccountService identityAccountService;
	private final AccIdentityAccountRepository identityAccountRepository; 
	private final IdmPasswordService passwordService;
	private final IdmConfigurationService configurationService;
	private final SecurityService securityService;
	
	@Autowired
	public IdentityPasswordValidateProcessor(IdmPasswordPolicyService passwordPolicyService,
			AccIdentityAccountService identityAccountService,
			AccIdentityAccountRepository identityAccountRepository,
			IdmPasswordService passwordService,
			IdmConfigurationService configurationService,
			SecurityService securityService) {
		super(IdentityEventType.PASSWORD);
		//
		Assert.notNull(identityAccountService);
		Assert.notNull(identityAccountRepository);
		Assert.notNull(passwordPolicyService);
		Assert.notNull(passwordService);
		Assert.notNull(configurationService);
		Assert.notNull(securityService);
		//
		this.passwordPolicyService = passwordPolicyService;
		this.identityAccountService = identityAccountService;
		this.passwordService = passwordService;
		this.identityAccountRepository = identityAccountRepository;
		this.configurationService = configurationService;
		this.securityService = securityService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties().get(IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO);
		IdmIdentityDto identity = event.getContent();
		//
		Assert.notNull(passwordChangeDto);
		Assert.notNull(identity);
		//
		LOG.debug("Call validate password for systems and default password policy for identity username [{}]", event.getContent().getUsername());
		//
		List<IdmPasswordPolicy> passwordPolicyList = new ArrayList<>();
		//
		// Find user accounts
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(filter, null).getContent();
		//
		if (!securityService.isAdmin()) {
			// check accounts and property all_only
			String passwordChangeProperty = this.configurationService.getValue(IdentityConfiguration.PROPERTY_IDENTITY_CHANGE_PASSWORD);
			if (passwordChangeProperty.equals(PasswordChangeType.ALL_ONLY.toString())) {
				List<String> identityAccountsIds = identityAccounts.stream()
						.filter(identityAccount -> {
							return identityAccount.isOwnership();
						})
				.map(AccIdentityAccountDto::getId).map(UUID::toString).collect(Collectors.toList());
				//
				boolean containsAll = !Collections.disjoint(identityAccountsIds, passwordChangeDto.getAccounts());
				if (!containsAll) {
					throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_FAILED, ImmutableMap.of("note", "Password is allowed change only for all accounts."));
				}
			}
		}
		//
		// get default password policy
		IdmPasswordPolicy defaultPasswordPolicy = this.passwordPolicyService.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);
		//
		if (passwordChangeDto.isIdm() && defaultPasswordPolicy != null) {
			passwordPolicyList.add(defaultPasswordPolicy);
		}
		//
		// get systems, only ownership
		identityAccounts.stream().filter(identityAccount -> {
			return identityAccount.isOwnership() && (passwordChangeDto.isAll()
					|| passwordChangeDto.getAccounts().contains(identityAccount.getId().toString()));
		}).forEach(identityAccount -> {
			// get validate password policy from system
			IdmPasswordPolicy passwordPolicy = identityAccountRepository.findOne(identityAccount.getId()).getAccount().getSystem().getPasswordPolicyValidate();
			// if passwordPolicy is null use default password policy for validate
			if (passwordPolicy == null) {
				passwordPolicy = defaultPasswordPolicy;
			}
			if (!passwordPolicyList.contains(passwordPolicy) && passwordPolicy != null) {
				passwordPolicyList.add(passwordPolicy);
			}
		});
		//
		// validate TODO: validate for admin?
		IdmPasswordValidationDto passwordValidationDto = new IdmPasswordValidationDto();
		// get old password for validation - til, from and password history
		passwordValidationDto.setOldPassword(this.passwordService.get(identity) == null ? null : this.passwordService.get(identity).getId());
		passwordValidationDto.setIdentity(identity == null ? null : identity.getId());
		passwordValidationDto.setPassword(passwordChangeDto.getNewPassword());
		this.passwordPolicyService.validate(passwordValidationDto, passwordPolicyList);
		//
		// if change password for idm iterate by all policies and get min attribute of
		// max password age and set it into DTO, for save password processor
		if (passwordChangeDto.isIdm() && !passwordPolicyList.isEmpty()) {
			Integer maxAgeInt = this.passwordPolicyService.getMaxPasswordAge(passwordPolicyList);
			if (maxAgeInt != null) {
				DateTime maxPasswordAge = new DateTime();
				// set into DTO, in identity password save processor was add into IdmIdentityPassword
				passwordChangeDto.setMaxPasswordAge(maxPasswordAge.plusDays(maxAgeInt));
			}
		}
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PASSWORD_VALIDATION_ORDER;
	}

}
