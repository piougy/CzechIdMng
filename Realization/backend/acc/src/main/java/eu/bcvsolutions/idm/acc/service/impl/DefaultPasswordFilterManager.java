package eu.bcvsolutions.idm.acc.service.impl;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.config.domain.PasswordFilterEncoderConfiguration;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterEchoItemDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterRequestDto;
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccUniformPasswordFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.entity.SysSystem_;
import eu.bcvsolutions.idm.acc.script.evaluator.DefaultTransformFromResourceEvaluator;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccUniformPasswordService;
import eu.bcvsolutions.idm.acc.service.api.PasswordFilterManager;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.core.api.config.cache.domain.ValueWrapper;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityPasswordProcessor;
import eu.bcvsolutions.idm.core.script.evaluator.AbstractScriptEvaluator;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Default implementation of {@link PasswordFilterManager}.
 * Storage implementation can be changed by override protected method:<br/ ><br/ >
 * - {@link #getEchoForAccount(UUID, GuardedString)}		- check echo in storage<br/ >
 * - {@link #createEchoForValidation(UUID, GuardedString)}	- create first echo record with flag for success password validation<br/ >
 * - {@link #setEchoForChange(UUID, GuardedString)}			- get and update existing echo for password validation<br/ >
 * - {@link #createEcho(UUID, GuardedString)}				- create standard echo record that contains flag for password validation and password change<br/ >
 * - {@link #getEcho(UUID)}									- get echo item for given account<br/ >
 * - {@link #getPasswordEncoder()}							- get default password hash function implementation<br/ >
 * - {@link #hashPassword(GuardedString)}					- hash password (default {@link SCryptPasswordEncoder})<br/ >
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Service("passwordFilterManager")
public class DefaultPasswordFilterManager implements PasswordFilterManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultPasswordFilterManager.class);

	@Autowired
	private AccUniformPasswordService uniformPasswordService;
	@Autowired
	private LookupService lookupService;
	@Autowired
	private IdmPasswordPolicyService policyService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private DefaultTransformFromResourceEvaluator scriptEvaluator;
	@Autowired
	private GroovyScriptService groovyScriptService;
	@Autowired
	private IdmCacheManager idmCacheManager; // For default implementation storage of ECHO's
	@Autowired
	private PasswordFilterEncoderConfiguration uniformPasswordConfiguration;
	@Autowired
	private SysSystemAttributeMappingService systemAttributeMappingService;
	private PasswordEncoder encoder;

	@Override
	public void validate(AccPasswordFilterRequestDto request) {
		LOG.info("Validation request from resource [{}] for identity identifier [{}] starting. {}",
				request.getResource(), request.getUsername(), request.getLogMetadata());

		SysSystemDto system = getSystem(request.getResource());
		SysSystemAttributeMappingDto passwordFilterAttribute = getAttributeMappingForPasswordFilter(system);

		IdmIdentityDto identity = evaluateUsernameToIdentity(system, request, passwordFilterAttribute);
		List<AccUniformPasswordDto> passwordDefinitions = getActiveUniformPasswordDefinitions(system);
		
		final GuardedString password = request.getPassword();
		final long timeout = passwordFilterAttribute.getEchoTimeout();
		final boolean changeInIdm = changeInIdm(passwordDefinitions);

		// Accounts with password filter support
		List<AccAccountDto> managedAccounts = null;
		// Accounts only for password changed without echo and password filter system
		List<AccAccountDto> notManagedAccounts = null;

		// System doesn't exists in password uniform feature
		if (CollectionUtils.isEmpty(passwordDefinitions)) {
			LOG.debug("System [{}] isn't exist in uniform password definition. Password will be check only trough the given system.");
			// Try find one account for given system with supported password filter
			managedAccounts = getAccountForSystemWithPasswordFilter(system, identity);
			notManagedAccounts = Lists.newArrayList();
		} else {
			UUID identityId = identity.getId();
			managedAccounts = getAccountsForPasswordChange(passwordDefinitions, identityId, Boolean.TRUE);
			notManagedAccounts = getAccountsForPasswordChange(passwordDefinitions, identityId, Boolean.FALSE);
		}

		if (managedAccounts.isEmpty()) {
			LOG.warn("For identifier [{}] (identity: [{}]) and resource [{}] wasn't found any managed account, validation will not be processed. {}",
					request.getUsername(), identity.getUsername(), request.getResource(), request.getLogMetadata());
			return;
		}

		// Accounts for current system only
		List<AccAccountDto> accounts = managedAccounts
				.stream()
				.filter(account -> {
					return account.getSystem().equals(system.getId());
				})
				.collect(Collectors.toList());

		for (AccAccountDto account : accounts) {
			AccPasswordFilterEchoItemDto echo = getEcho(account.getId());
			if (echo == null) {
				// Echo doesn't exist yet we can continue for validation
				LOG.debug("Echo for account id [{}] and system identifier [{}] doesn't exist. {}",
						account.getId(), request.getUsername(), request.getLogMetadata());
				continue;
			}

			boolean echoValid = echo.isEchoValid(timeout);
			boolean passwordEqual = isPasswordEqual(echo, password);

			if (echoValid && passwordEqual && echo.isChanged()) {
				// Classic valid echo that was already changed, for this echo will not validate again
				LOG.info("Echo record found! Account uid [{}] and system code [{}]. Validation will be skipped. {}",
						account.getUid(), system.getCode(), request.getLogMetadata());
				// For one valid echo just skip password validate for all another password from uniform password
				return;
			}

			if (echo.isValidityChecked()) {
				// Validation was successfully executed, now is second run
				// TODO: can we skip this validation?
				LOG.debug("For account [{}] and system [{}] exist only echo for validation. {}",
						account.getUid(), system.getCode(), request.getLogMetadata());
			}
		}
		
		// Unite system from managed and not managed accounts
		List<SysSystemDto> systems = getSystemForAccounts(managedAccounts);
		systems.addAll(getSystemForAccounts(notManagedAccounts));

		// Get password policies from managed systems
		List<IdmPasswordPolicyDto> policies = getPasswordPolicy(systems);

		// Default password policy must be also added when is setup change trough IdM
		if (changeInIdm) {
			IdmPasswordPolicyDto defaultPasswordPolicy = policyService.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);
			// Password policy can be added by some system check for duplicate
			if (defaultPasswordPolicy != null && !policies.contains(defaultPasswordPolicy)) {
				policies.add(defaultPasswordPolicy);
			}
		}

		// For empty policies is not required process validation
		if (policies.isEmpty()) {
			LOG.info("Any applicable password policy found! For identifier [{}] (identity: [{}]) and resource [{}]. {}",
					request.getUsername(), identity.getUsername(), request.getResource(), request.getLogMetadata());
		} else {
			// Compose validation request for IdM
			IdmPasswordValidationDto passwordValidationDto = new IdmPasswordValidationDto();
			passwordValidationDto.setPassword(password);
			passwordValidationDto.setIdentity(identity);
			passwordValidationDto.setEnforceMinPasswordAgeValidation(true); // password is changed on different logged identity, but change by password filter is originally executed as target identity
			
			try {
				policyService.validate(passwordValidationDto , policies);
			} catch (Exception e) {
				// Just log the message and send error next
				LOG.error("Validation didn't pass! For identity username [{}] and system code [{}]. Error message: [{}]. {}",
						identity.getUsername(),
						system.getCode(),
						StringUtils.defaultString(e.getMessage()),
						request.getLogMetadata());
				
				// Set echod with not information about not valid password
				managedAccounts.forEach(account -> {
					createEchoForValidation(account.getId(), password, false);
				});

				// Throw error to caller
				throw e;
			}
		}

		// Set validate echos only for managed accounts
		managedAccounts.forEach(account -> {
			createEchoForValidation(account.getId(), password, true);
		});

		// Password valid
		LOG.info("Validation request pass! For identity [{}] and system code [{}]. {}",
				identity.getUsername(), system.getCode(), request.getLogMetadata());
	}

	@Override
	public void change(AccPasswordFilterRequestDto request) {
		LOG.info("Change request from resource [{}] for identity identifier [{}] starting. {}",
				request.getResource(), request.getUsername(), request.getLogMetadata());

		SysSystemDto system = getSystem(request.getResource());
		SysSystemAttributeMappingDto passwordFilterAttribute = getAttributeMappingForPasswordFilter(system);
		
		IdmIdentityDto identity = evaluateUsernameToIdentity(system, request, passwordFilterAttribute);
		List<AccUniformPasswordDto> passwordDefinitions = getActiveUniformPasswordDefinitions(system);
		
		final GuardedString password = request.getPassword();
		final long timeout = passwordFilterAttribute.getEchoTimeout();
		final boolean changeInIdm = changeInIdm(passwordDefinitions);

		// Accounts with password filter support.
		List<AccAccountDto> managedAccounts = null;
		// Accounts only for password changed without echo and password filter system.
		List<AccAccountDto> notManagedAccounts = null;

		// System doesn't exists in password uniform feature.
		if (CollectionUtils.isEmpty(passwordDefinitions)) {
			LOG.debug("System [{}] isn't exist in uniform password definition. Password will be changed only trough the given system.");
			// Try find one account for given system with supported passwod filter
			managedAccounts = getAccountForSystemWithPasswordFilter(system, identity);
			notManagedAccounts = Lists.newArrayList();
		} else {
			UUID identityId = identity.getId();
			managedAccounts = getAccountsForPasswordChange(passwordDefinitions, identityId, Boolean.TRUE);
			notManagedAccounts = getAccountsForPasswordChange(passwordDefinitions, identityId, Boolean.FALSE);
		}

		if (managedAccounts.isEmpty()) {
			LOG.warn("Password will not be changed! For identifier [{}] (identity username: [{}]) and resource [{}] wasn't found any managed account. {}",
					request.getUsername(), identity.getUsername(), request.getResource(), request.getLogMetadata());
			return;
		}

		// Accounts for current system only.
		List<AccAccountDto> accounts = managedAccounts
				.stream()
				.filter(account -> {
					return account.getSystem().equals(system.getId());
				})
				.collect(Collectors.toList());

		for (AccAccountDto account : accounts) {
			AccPasswordFilterEchoItemDto echo = getEcho(account.getId());
			if (echo == null) {
				// Echo doesn't exist yet validation must be executed first!
				LOG.error("Echo record for validation doesn't exist! For account uid [{}] and system code [{}]. Execute validation first! {}",
						account.getUid(), system.getCode(), request.getLogMetadata());
				throw new ResultCodeException(AccResultCode.PASSWORD_FILTER_NOT_VALID_CHANGE_REQUEST, ImmutableMap.of("identifier", identity.getId())); 
			} 

			boolean echoValid = echo.isEchoValid(timeout);
			boolean passwordEqual = isPasswordEqual(echo, password);
			
			if (!passwordEqual) {
				// Password doesn't match with checked password - security problem.
				LOG.error("Password doesn't match with validated password! For account uid [{}] and system code [{}]. {}",
						account.getUid(), system.getCode(), request.getLogMetadata());
				throw new ResultCodeException(AccResultCode.PASSWORD_FILTER_NOT_VALID_CHANGE_REQUEST, ImmutableMap.of("identifier", identity.getId())); 	
			}
			
			if (BooleanUtils.isFalse(echo.isValidityChecked())) {
				// Validation wasn't successfully executed yet - validation must pass.
				LOG.error("Password wasn't successfully validated! For account id [{}] and system id [{}]. Validation must pass! {}",
						account.getId(), system.getId(), request.getLogMetadata());
				throw new ResultCodeException(AccResultCode.PASSWORD_FILTER_NOT_VALID_CHANGE_REQUEST, ImmutableMap.of("identifier", identity.getId())); 			
			}

			if (echoValid && echo.isChanged()) {
				// Classic valid echo that was already changed, for this echo will not be changed again - FOR ALL ANOTHER SYSTEMS.
				LOG.info("Echo record found! For account uid [{}] and system code [{}]. Password will not be changed. {}",
						account.getUid(), system.getCode(), request.getLogMetadata());
				// For one valid echo just skip password change
				return;
			}
		}

		// Create final account list for password change and check duplicate.
		List<AccAccountDto> finalAccounts = Lists.newArrayList(managedAccounts);
		notManagedAccounts.forEach(account -> {
			if (!finalAccounts.contains(account)) {
				finalAccounts.add(account);
			}
		});

		// Remove account for original resource - is possible that account can be empty.
		List<UUID> accountsForResource = Lists.newArrayList();
		finalAccounts.removeIf(account -> {
			if (account.getSystem().equals(system.getId())) {
				accountsForResource.add(account.getId());
				return true;
			}
			return false;
		});

		// When account is empty and uniform password doesn't required password change trough IdM skip password change process.
		if (finalAccounts.isEmpty() && changeInIdm == false) {
			LOG.info("Request for resource indetifier [{}] (identity username [{}]) and system code [{}] will not be processed! No suitable account found (including IdM). {}",
					request.getUsername(), identity.getUsername(), system.getCode(), request.getLogMetadata());
			// Set echos for accounts by the given system, because there is password changed by the system.
			accounts.forEach(account -> {
				setEchoForChange(account.getId(), password);
			});
			return;
		}

		// Prepare account ids
		List<String> accountsIds = Lists.newArrayList();
		finalAccounts.forEach(account -> {
			accountsIds.add(account.getId().toString());
		});

		if (finalAccounts.isEmpty()) {
			LOG.info("Password change will be processed only trough IdM. For identity [{}] and system [{}]. {}",
					identity.getUsername(),
					system.getCode(),
					request.getLogMetadata());
		} else {
			LOG.info("Password change will be processed. For identity [{}] and system [{}] was found these accounts for change [{}]. {}",
					identity.getUsername(),
					system.getCode(),
					Strings.join(accountsIds, ','),
					request.getLogMetadata());
		}

		// Setup echo for resource from that was executed from event password.
		accountsForResource.forEach(accountId -> {
			setEchoForChange(accountId, password);
		});

		// Prepare request for password change.
		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(false); // Not for all, but only for chosen
		passwordChangeDto.setIdm(changeInIdm);
		passwordChangeDto.setAccounts(accountsIds);
		passwordChangeDto.setNewPassword(password);
		passwordChangeDto.setSkipResetValidFrom(true);
		//
		IdentityEvent identityEvent = new IdentityEvent(
				IdentityEventType.PASSWORD,
				identity, 
				ImmutableMap.of(
						IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO, passwordChangeDto,
						// Managed accounts in event is for performance speedup in processor IdentityPasswordProvisioningProcessor and set echos
						MANAGED_ACCOUNTS, Lists.newArrayList(managedAccounts.stream().map(AccAccountDto::getId).collect(Collectors.toList())),
						// Exclude all accounts for given system
						EXCLUDED_SYSTEM, system.getId(),
						// Skip whole validation just change password - password was validate before
						IdentityProcessor.SKIP_PASSWORD_VALIDATION, Boolean.TRUE
				));

		// Classic password change event
		identityService.passwordChange(identityEvent);
		
		LOG.info("Password change was finished! For identity username [{}] and system code [{}]. {}",
				identity.getUsername(), system.getCode(), request.getLogMetadata());
	}

	@Override
	public void setEchoForChange(UUID accountId, GuardedString password) {
		AccPasswordFilterEchoItemDto echo = getEcho(accountId);
		if (echo == null) {
			// Echo record for the item missing! This isn't possible
			LOG.info("For account id [{}] cannot be find echo record. Validation wasn't called first.", accountId);
			echo = new AccPasswordFilterEchoItemDto();
			echo.setAccountId(accountId);
			echo.setValidityChecked(true);
		}

		ZonedDateTime now = ZonedDateTime.now();
		LOG.info("For account [{}] will be updated echo record with changed [true]. Echo's date of validation [{}], changed date [{}]", accountId, echo.getValidateDate(), now);
		echo.setChanged(true);
		echo.setChangeDate(now);

		// Password given
		if (password != null) {
			echo.setPassword(hashPassword(password));
		}
		
		idmCacheManager.cacheValue(ECHO_CACHE_NAME, accountId, echo);
	}

	@Override
	public void clearChangedEcho(UUID accountId) {
		AccPasswordFilterEchoItemDto echo = getEcho(accountId);
		echo.setChanged(false);
		echo.setChangeDate(null);
		idmCacheManager.cacheValue(ECHO_CACHE_NAME, accountId, echo);
	}

	@Override
	public void createEchoForValidation(UUID accountId, GuardedString password, boolean success) {
		AccPasswordFilterEchoItemDto echo = new AccPasswordFilterEchoItemDto(hashPassword(password), accountId);
		echo.setValidityChecked(success);
		echo.setValidateDate(ZonedDateTime.now());
		LOG.info("For account [{}] will be created new echo record for validation [{}].", accountId, success);
		idmCacheManager.cacheValue(ECHO_CACHE_NAME, accountId, echo);
	}

	@Override
	public void createEcho(UUID accountId, GuardedString password) {
		AccPasswordFilterEchoItemDto echo = new AccPasswordFilterEchoItemDto(hashPassword(password), accountId);
		ZonedDateTime now = ZonedDateTime.now();
		echo.setValidityChecked(true);
		echo.setChanged(true);
		echo.setChangeDate(now);
		echo.setValidateDate(now);
		LOG.info("For account [{}] will be created new standard echo with validity and changed record.", accountId);
		idmCacheManager.cacheValue(ECHO_CACHE_NAME, accountId, echo);
	}

	@Override
	public AccPasswordFilterEchoItemDto getEcho(UUID accountId) {
		ValueWrapper value = idmCacheManager.getValue(ECHO_CACHE_NAME, accountId);
		if (value == null) {
			return null;
		}

		Object echoAsObject = value.get();
		if (echoAsObject == null) {
			return null;
		}

		return(AccPasswordFilterEchoItemDto) echoAsObject;
	}
	
	/**
	 * Process all given information from {@link AccPasswordFilterRequestDto} and {@link AccUniformPasswordDto} and then
	 * evaluate {@link IdmIdentityDto} thought very defensive behavior:
	 * 
	 *  1. - check script for identity transformation,
	 *  2. - check if exist UID in given system,
	 *  3. - check identities username's.
	 *
	 * @param system
	 * @param request
	 * @param passwordFilterAttribute
	 * @return
	 */
	protected IdmIdentityDto evaluateUsernameToIdentity(SysSystemDto system, AccPasswordFilterRequestDto request, SysSystemAttributeMappingDto passwordFilterAttribute) {
		String script = passwordFilterAttribute.getTransformationUidScript();
		String usernameRequest = request.getUsername();

		if (StringUtils.isBlank(script)) {
			// First we will try find account by uid
			AccAccountDto account = accountService.getAccount(usernameRequest, system.getId());
			if (account == null) {
				// Second we will try find direct identity by username
				IdmIdentityDto identityDto = identityService.getByUsername(usernameRequest);
				if (identityDto == null) {
					LOG.error("Identity for request for username [{}] and system [{}] cannot be found. {}",
							usernameRequest, system.getId(), request.getLogMetadata());
					throw new ResultCodeException(AccResultCode.PASSWORD_FILTER_IDENTITY_NOT_FOUND, ImmutableMap.of("identifier", usernameRequest));
				}
				return identityDto;
			}
			
			IdmIdentityDto identityDto = identityService.get(account.getTargetEntityId());
			if (identityDto == null) {
				LOG.error("Identity for request for username [{}], system [{}] and account id [{}] cannot be found. {}",
						usernameRequest, system.getId(), account.getId(), request.getLogMetadata());
				throw new ResultCodeException(AccResultCode.PASSWORD_FILTER_IDENTITY_NOT_FOUND, ImmutableMap.of("identifier", usernameRequest));
			}
			return identityDto;
		}
		
		// Standard behavior with script
		Map<String, Object> variables = new HashMap<>();
		variables.put(SCRIPT_SYSTEM_PARAMETER, system);
		variables.put(SCRIPT_USERNAME_PARAMETER, request.getUsername());
		variables.put(SCRIPT_LOG_IDENTIFIER_PARAMETER, request.getLogIdentifier());
		variables.put(SCRIPT_SYSTEM_ATTRIBUTE_MAPPING_PARAMETER, passwordFilterAttribute);

		// Add system script evaluator for call another scripts
		variables.put(AbstractScriptEvaluator.SCRIPT_EVALUATOR,	scriptEvaluator);

		// Add access for script evaluator
		List<Class<?>> extraClass = new ArrayList<>();
		extraClass.add(AbstractScriptEvaluator.Builder.class);
		extraClass.add(IdmIdentityDto.class);
		extraClass.add(SysSystemDto.class);
		extraClass.add(SysSystemAttributeMappingDto.class);


		Object result = groovyScriptService.evaluate(script, variables, extraClass);
		if (result instanceof IdmIdentityDto) {
			return (IdmIdentityDto) result;
		} else {
			throw new ResultCodeException(AccResultCode.PASSWORD_FILTER_IDENTITY_NOT_FOUND, ImmutableMap.of("identifier", usernameRequest));
		}
	}

	/**
	 * Get all validate password policies for given list of systems. Method use
	 * embedded object not real DB query.
	 *
	 * @param systems
	 * @return
	 */
	private List<IdmPasswordPolicyDto> getPasswordPolicy(List<SysSystemDto> systems) {
		List<IdmPasswordPolicyDto> policies = Lists.newArrayList();

		systems.forEach(system -> {
			IdmPasswordPolicyDto policy = DtoUtils.getEmbedded(system, SysSystem_.passwordPolicyValidate, IdmPasswordPolicyDto.class, null);
			if (policy != null && !policies.contains(policy)) {
				policies.add(policy);
			}
		});

		return policies;
	}

	/**
	 * Get system from given list accounts. Method use embedded object
	 * not real DB query.
	 *
	 * @param accounts
	 * @return
	 */
	private List<SysSystemDto> getSystemForAccounts(List<AccAccountDto> accounts) {
		List<SysSystemDto> systems = Lists.newArrayList();

		accounts.forEach(account -> {
			SysSystemDto systemDto = DtoUtils.getEmbedded(account, AccAccount_.system, SysSystemDto.class);

			// For one system is possible more accounts. 
			if (!systems.contains(systemDto)) {
				systems.add(systemDto);
			}
		});

		return systems;
	}

	/**
	 * Get system by given ID or name/code (standard lookup behavior). For empty parameters or not found system throw error.
	 *
	 * @param resource
	 * @return
	 */
	private SysSystemDto getSystem(String resource) {
		if (StringUtils.isEmpty(resource)) {
			throw new ResultCodeException(AccResultCode.PASSWORD_FILTER_SYSTEM_NOT_FOUND, ImmutableMap.of("resource", StringUtils.defaultString(resource)));
		}
		SysSystemDto system = (SysSystemDto) lookupService.lookupDto(SysSystemDto.class, resource);
		if (system == null) {
			throw new ResultCodeException(AccResultCode.PASSWORD_FILTER_SYSTEM_NOT_FOUND, ImmutableMap.of("resource", resource));
		}
		return system;
	}

	/**
	 * Get active uniform password definition for given system. System can be now only in
	 * one password definition.
	 * TODO: this behavior can be changed in future.
	 *
	 * @param systemDto
	 * @return
	 * @throws ResultCodeException when definition not found or uniform password definition is disabled
	 */
	private List<AccUniformPasswordDto> getActiveUniformPasswordDefinitions(SysSystemDto systemDto) {
		AccUniformPasswordFilter filter = new AccUniformPasswordFilter();
		filter.setSystemId(systemDto.getId());
		filter.setDisabled(Boolean.FALSE);
		List<AccUniformPasswordDto> definitions = uniformPasswordService.find(filter, null).getContent();
		
		if (definitions.isEmpty()) {
			return Lists.newArrayList();
		}
		
		return definitions;
	}

	/**
	 * Get {@link SysSystemAttributeMappingDto} that define configuration for password filter.
	 *
	 * @param system
	 * @return
	 */
	private SysSystemAttributeMappingDto getAttributeMappingForPasswordFilter(SysSystemDto system) {
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(system.getId());
		filter.setPasswordAttribute(Boolean.TRUE);
		filter.setPasswordFilter(Boolean.TRUE);
		List<SysSystemAttributeMappingDto> content = systemAttributeMappingService.find(filter, null).getContent();
		if (content.isEmpty()) {
			throw new ResultCodeException(AccResultCode.PASSWORD_FILTER_DEFINITION_NOT_FOUND, ImmutableMap.of("systemId", system.getId()));
		}

		// Attribute with password filter may be only one!
		return content.get(0);
	}
	
	/**
	 * Get password encoder implementation.
	 *
	 * @return
	 */
	protected PasswordEncoder getPasswordEncoder() {
		// Cannot be initialized in constructor because the manager configuration are autowired
		if (this.encoder == null) {
			this.encoder = new SCryptPasswordEncoder(
					uniformPasswordConfiguration.getScryptCpuCost(),
					uniformPasswordConfiguration.getScryptMemoryCost(),
					uniformPasswordConfiguration.getScryptParallelization(),
					uniformPasswordConfiguration.getScryptKeyLength(),
					uniformPasswordConfiguration.getScryptSaltLength());
		}

		return this.encoder;
	}

	/**
	 * Implementation of hashing password to echos storage.
	 *
	 * @param password
	 * @return
	 */
	protected String hashPassword(GuardedString password) {
		return getPasswordEncoder().encode(password.asString());
	}

	/**
	 * Return accounts that exists in given uniform password definitions. Second parameter supportPasswordFilter
	 * return only managed system with password filter (echo support).
	 *
	 * @param passwordDefinitions
	 * @param identityId
	 * @param supportPasswordFilter
	 * @return
	 */
	private List<AccAccountDto> getAccountsForPasswordChange(List<AccUniformPasswordDto> passwordDefinitions, UUID identityId, Boolean supportPasswordFilter) {
		Assert.notNull(identityId, "Identity cannot be null!");
		List<AccAccountDto> accounts = Lists.newArrayList();

		AccAccountFilter filter = new AccAccountFilter();
		filter.setSupportPasswordFilter(supportPasswordFilter);
		filter.setIdentityId(identityId);
		for (AccUniformPasswordDto definition : passwordDefinitions) {
			filter.setUniformPasswordId(definition.getId());
			accounts.addAll(accountService.find(filter, null).getContent());
		}

		return accounts;
	}

	/**
	 * Return account for given system and identity. Only one may exists.
	 *
	 * @param system
	 * @param identity
	 * @return
	 */
	private List<AccAccountDto> getAccountForSystemWithPasswordFilter(SysSystemDto system, IdmIdentityDto identity) {
		AccAccountFilter filter = new AccAccountFilter();
		filter.setSystemId(system.getId());
		filter.setIdentityId(identity.getId());
		filter.setSupportPasswordFilter(Boolean.TRUE);
		return accountService.find(filter, null).getContent();
	}

	/**
	 * Check all given password definition and findfirst definition that allow change in IdM and then return true, otherwise return false.
	 *
	 * @param passwordDefintions
	 * @return
	 */
	private boolean changeInIdm(List<AccUniformPasswordDto> passwordDefinitions) {
		if (CollectionUtils.isEmpty(passwordDefinitions)) {
			return false;
		}

		for (AccUniformPasswordDto definition : passwordDefinitions) {
			if (definition.isChangeInIdm()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Check if stored echo record is equals with given password.
	 *
	 * @param echo
	 * @param password
	 * @return
	 */
	private boolean isPasswordEqual(AccPasswordFilterEchoItemDto echo, GuardedString password) {
		return getPasswordEncoder().matches(password.asString(), echo.getPassword());
	}
}
