package eu.bcvsolutions.idm.acc.event.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.domain.PasswordChangeType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityPasswordProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Processor with password validation. Get all accounts and their distinct
 * systems.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component("accIdentityPasswordValidateProcessor")
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Validates identity's and all selected systems password, when password is changed.")
public class IdentityPasswordValidateProcessor
		extends CoreEventProcessor<IdmIdentityDto> 
		implements IdentityProcessor {

	public static final String PROCESSOR_NAME = "identity-password-validate-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(IdentityPasswordValidateProcessor.class);
	private final IdmPasswordPolicyService passwordPolicyService;
	private final AccIdentityAccountService identityAccountService;
	private final AccIdentityAccountRepository identityAccountRepository;
	private final IdmPasswordService passwordService;
	private final SecurityService securityService;
	private final IdentityConfiguration identityConfiguration;

	@Autowired
	public IdentityPasswordValidateProcessor(
			IdmPasswordPolicyService passwordPolicyService,
			AccIdentityAccountService identityAccountService, 
			AccIdentityAccountRepository identityAccountRepository,
			IdmPasswordService passwordService, 
			SecurityService securityService,
			IdentityConfiguration identityConfiguration) {
		super(IdentityEventType.PASSWORD);
		//
		Assert.notNull(identityAccountService);
		Assert.notNull(identityAccountRepository);
		Assert.notNull(passwordPolicyService);
		Assert.notNull(passwordService);
		Assert.notNull(securityService);
		Assert.notNull(identityConfiguration);
		//
		this.passwordPolicyService = passwordPolicyService;
		this.identityAccountService = identityAccountService;
		this.passwordService = passwordService;
		this.identityAccountRepository = identityAccountRepository;
		this.securityService = securityService;
		this.identityConfiguration = identityConfiguration;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties()
				.get(IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO);
		IdmIdentityDto identity = event.getContent();
		//
		Assert.notNull(passwordChangeDto);
		Assert.notNull(identity);
		//
		LOG.debug("Call validate password for systems and default password policy for identity username [{}]",
				event.getContent().getUsername());
		//
		List<IdmPasswordPolicyDto> passwordPolicyList = validateDefinition(identity, passwordChangeDto);
		//
		// Find user accounts
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(filter, null).getContent();
		//
		if (!securityService.isAdmin()) {
			// check accounts and property all_only
			PasswordChangeType passwordChangeType = identityConfiguration.getPasswordChangeType();
			if (passwordChangeType == PasswordChangeType.ALL_ONLY) {
				// get distinct account ids from identity accounts
				List<String> accountIds = identityAccounts.stream()
						.filter(identityAccount -> {
							// filter by ownership
							return (identityAccount.isOwnership());
						}).map(AccIdentityAccountDto::getAccount).map(UUID::toString).collect(Collectors.toList());
				//
				if (!accountIds.isEmpty() && !passwordChangeDto.getAccounts().isEmpty()) {
					// size of the found accounts must match the account size in the password change - ALL_ONLY
					boolean containsAll = accountIds.size() == passwordChangeDto.getAccounts().size();
					if (!containsAll) {
						throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_ALL_ONLY);
					}
				}
			}
		}
		//
		// validate TODO: validate for admin?
		IdmPasswordValidationDto passwordValidationDto = new IdmPasswordValidationDto();
		// get old password for validation - til, from and password history
		IdmPasswordDto oldPassword = this.passwordService.findOneByIdentity(identity.getId());
		passwordValidationDto.setOldPassword(oldPassword == null ? null : oldPassword.getId());
		passwordValidationDto.setIdentity(identity);
		passwordValidationDto.setPassword(passwordChangeDto.getNewPassword());
		this.passwordPolicyService.validate(passwordValidationDto, passwordPolicyList);
		// maximum password age is solved in {@link IdentityPasswordProcessor}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Method returns password policy list for accounts
	 * 
	 * @param identity
	 * @param passwordChangeDto
	 * @return
	 * 
	 */
	public List<IdmPasswordPolicyDto> validateDefinition(IdmIdentityDto identity, PasswordChangeDto passwordChangeDto) {
		
		List<IdmPasswordPolicyDto> passwordPolicyList = new ArrayList<>();
		// Find user accounts
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(filter, null).getContent();
		//
		// get default password policy
		IdmPasswordPolicyDto defaultPasswordPolicy = this.passwordPolicyService
				.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);
		//
		if (passwordChangeDto.isIdm() && defaultPasswordPolicy != null) {
			passwordPolicyList.add(defaultPasswordPolicy);
		}
		//
		// get systems, only ownership
		identityAccounts.stream().filter(identityAccount -> {
			return identityAccount.isOwnership() && (passwordChangeDto.isAll()
					|| passwordChangeDto.getAccounts().contains(identityAccount.getAccount().toString()));
		}).forEach(identityAccount -> {
			// get validate password policy from system
			// TODO: change to DTO after refactoring
			IdmPasswordPolicy passwordPolicyEntity = identityAccountRepository.findOne(identityAccount.getId()).getAccount()
					.getSystem().getPasswordPolicyValidate();
			IdmPasswordPolicyDto passwordPolicy = null;
			if (passwordPolicyEntity != null) {
				passwordPolicy = passwordPolicyService.get(passwordPolicyEntity.getId());
			}
			// if passwordPolicy is null use default password policy for
			// validate
			if (passwordPolicy == null) {
				passwordPolicy = defaultPasswordPolicy;
			}
			if (!passwordPolicyList.contains(passwordPolicy) && passwordPolicy != null) {
				passwordPolicyList.add(passwordPolicy);
			}
		});
	
		return  passwordPolicyList;
	}

	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PASSWORD_VALIDATION_ORDER;
	}

}
