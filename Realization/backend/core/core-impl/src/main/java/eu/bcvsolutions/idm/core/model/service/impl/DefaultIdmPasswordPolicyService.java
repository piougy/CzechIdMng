package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.ZonedDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyGenerateType;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyIdentityAttributes;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordPolicyFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordHistoryService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.api.utils.PasswordGenerator;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.event.PasswordPolicyEvent;
import eu.bcvsolutions.idm.core.model.event.PasswordPolicyEvent.PasswordPolicyEvenType;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordPolicyRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Password policy service.
 * validation and generate method.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Ondrej Husnik
 *
 */
public class DefaultIdmPasswordPolicyService
		extends AbstractReadWriteDtoService<IdmPasswordPolicyDto, IdmPasswordPolicy, IdmPasswordPolicyFilter>
		implements IdmPasswordPolicyService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmPasswordPolicyService.class);
	
	// TODO: better place for constant?
	private static final String MIN_LENGTH = "minLength";
	private static final String MAX_LENGTH = "maxLength";
	private static final String MIN_UPPER_CHAR = "minUpperChar";
	private static final String MIN_LOWER_CHAR = "minLowerChar";
	private static final String MIN_NUMBER = "minNumber";
	private static final String MIN_SPECIAL_CHAR = "minSpecialChar";
	private static final String COINTAIN_PROHIBITED = "prohibited";
	private static final String BEGIN_PROHIBITED = "beginProhibited";
	private static final String END_PROHIBITED = "endProhibited";
	// private static final String CONTAIN_WEAK_PASS = "weakPass"; // TODO
	private static final String MIN_RULES_TO_FULFILL = "minRulesToFulfill";
	private static final String MIN_RULES_TO_FULFILL_COUNT = "minRulesToFulfillCount";
	private static final String POLICY_NAME = "policiesNames";
	private static final String PASSWORD_SIMILAR_USERNAME = "passwordSimilarUsername";
	private static final String PASSWORD_SIMILAR_EMAIL = "passwordSimilarEmail";
	private static final String PASSWORD_SIMILAR_FIRSTNAME = "passwordSimilarFirstName";
	private static final String PASSWORD_SIMILAR_LASTNAME = "passwordSimilarLastName";
	private static final String PASSWORD_SIMILAR_TITLESAFTER = "passwordSimilarTitlesAfter";
	private static final String PASSWORD_SIMILAR_TITLESBEFORE = "passwordSimilarTitlesBefore";
	private static final String PASSWORD_SIMILAR_EXTERNALCODE = "passwordSimilarExternalCode";
	private static final String PASSWORD_SIMILAR_USERNAME_PREVALIDATE = "passwordSimilarUsernamePreValidate";
	private static final String PASSWORD_SIMILAR_EMAIL_PREVALIDATE = "passwordSimilarEmailPreValidate";
	private static final String PASSWORD_SIMILAR_FIRSTNAME_PREVALIDATE = "passwordSimilarFirstNamePreValidate";
	private static final String PASSWORD_SIMILAR_LASTNAME_PREVALIDATE = "passwordSimilarLastNamePreValidate";
	private static final String PASSWORD_SIMILAR_TITLESAFTER_PREVALIDATE = "passwordSimilarTitlesAfterPreValidate";
	private static final String PASSWORD_SIMILAR_TITLESBEFORE_PREVALIDATE = "passwordSimilarTitlesBeforePreValidate";
	private static final String PASSWORD_SIMILAR_EXTERNALCODE_PREVALIDATE = "passwordSimilarExternalCodePreValidate";
	private static final String POLICY_NAME_PREVALIDATION = "policiesNamesPreValidation";
	private static final String SPECIAL_CHARACTER_BASE = "specialCharacterBase";
	private static final String FORBIDDEN_CHARACTER_BASE = "forbiddenCharacterBase";
	private static final String FORBIDDEN_BEGIN_CHARACTER_BASE = "forbiddenBeginCharacterBase";
	private static final String FORBIDDEN_END_CHARACTER_BASE = "forbiddenEndCharacterBase";
	private static final String MAX_HISTORY_SIMILAR = "maxHistorySimilar";
	private final int MIN_CONSIDERED_ATTR_LEN = 3;
	private final String DELIMITER_SET_REGEXP = ",\\.\\-—_£\\s";
	private final int TEST_POLICY_CYCLES = 20;
	
	private PasswordGenerator passwordGenerator;
	private final IdmPasswordPolicyRepository repository;
	private final SecurityService securityService;
	private final EntityEventManager entityEventProcessorService;
	private final IdmPasswordService passwordService;
	private final IdmPasswordHistoryService passwordHistoryService;
	
	@Autowired
	public DefaultIdmPasswordPolicyService(
			IdmPasswordPolicyRepository repository,
			EntityEventManager entityEventProcessorService,
			SecurityService securityService,
			IdmPasswordService passwordService,
			IdmPasswordHistoryService passwordHistoryService) {
		super(repository);
		//
		Assert.notNull(entityEventProcessorService, "Service is required.");
		Assert.notNull(repository, "Repository is required.");
		Assert.notNull(securityService, "Service is required.");
		Assert.notNull(passwordService, "Service is required.");
		Assert.notNull(passwordHistoryService, "Service is required.");
		//
		this.entityEventProcessorService = entityEventProcessorService;
		this.repository = repository;
		this.securityService = securityService;
		this.passwordService = passwordService;
		this.passwordHistoryService = passwordHistoryService;
	}
	
	@Override
	protected Page<IdmPasswordPolicy> findEntities(IdmPasswordPolicyFilter filter, Pageable pageable, BasePermission... permission) {
		if (pageable == null) {
			// pageable is required in spring data
			pageable = PageRequest.of(0, Integer.MAX_VALUE);
		}
		//
		if (filter == null) {
			return getRepository().findAll(pageable);
		}
		return repository.find(filter, pageable);
	}
	
	@Override
	@Transactional
	public IdmPasswordPolicyDto save(IdmPasswordPolicyDto dto, BasePermission... permission) {
		Assert.notNull(dto, "DtTO is required to be saved.");
		//
		// TODO: this should be moved to save internal, can be bypassed by event publishing
		if (!ObjectUtils.isEmpty(permission)) {
			IdmPasswordPolicy persistEntity = null;
			if (dto.getId() != null) {
				persistEntity = this.getEntity(dto.getId());
				if (persistEntity != null) {
					// check access on previous entity - update is needed
					checkAccess(persistEntity, IdmBasePermission.UPDATE);
				}
			}
			checkAccess(toEntity(dto, persistEntity), permission); // TODO: remove one checkAccess?
		}
		// Check, if max attempts attribute is defined, then time of blocking must have defined too
		Integer maxAttempts = dto.getMaxUnsuccessfulAttempts();
		Integer blockLogin = dto.getBlockLoginTime();
		if (maxAttempts != null && maxAttempts.intValue() > 0 && (blockLogin == null || blockLogin <= 0)) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_BLOCK_TIME_IS_REQUIRED, ImmutableMap.of("definition", dto.getName()));
		}
		if (dto.getType() == IdmPasswordPolicyType.GENERATE && dto.getGenerateType() == IdmPasswordPolicyGenerateType.RANDOM) {
			if (!this.getPasswordGenerator().testPolicySetting(dto, TEST_POLICY_CYCLES)) {
				throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_INVALID_SETTING);
			}
		}
		
		//
		LOG.debug("Saving entity [{}]", dto.getName());
		if (isNew(dto)) {
			// throw event with create
			return entityEventProcessorService.process(new PasswordPolicyEvent(PasswordPolicyEvenType.CREATE, dto)).getContent();
		}
		// else throw event with update
		return entityEventProcessorService.process(new PasswordPolicyEvent(PasswordPolicyEvenType.UPDATE, dto)).getContent();
	}
	
	@Override
	@Transactional
	public void delete(IdmPasswordPolicyDto dto, BasePermission... permission) {
		checkAccess(this.getEntity(dto.getId()), permission);
		//
		LOG.debug("Delete entity [{}]", dto.getName());
		//
		entityEventProcessorService.process(new PasswordPolicyEvent(PasswordPolicyEvenType.DELETE, dto));
	}
	
	@Override
	public void validate(IdmPasswordValidationDto passwordValidationDto) {
		this.validate(passwordValidationDto, this.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE));
	}
	
	@Override
	public void validate(IdmPasswordValidationDto passwordValidationDto, IdmPasswordPolicyDto passwordPolicy) {
		List<IdmPasswordPolicyDto> passwordPolicyList = new ArrayList<IdmPasswordPolicyDto>();
		
		if (passwordPolicy != null) {
			passwordPolicyList.add(passwordPolicy);
		}
		
		this.validate(passwordValidationDto, passwordPolicyList);
	}

	@Override
	public void validate(IdmPasswordValidationDto passwordValidationDto, List<IdmPasswordPolicyDto> passwordPolicyList) {
		validate(passwordValidationDto, passwordPolicyList, false);
	}

	@Override
	public IdmPasswordPolicyDto getDefaultPasswordPolicy(IdmPasswordPolicyType type) {
		IdmPasswordPolicy defaultPolicy = repository.findOneDefaultType(type);
		return this.toDto(defaultPolicy);
	}

	@Override
	public String generatePassword(IdmPasswordPolicyDto passwordPolicy) {
		Assert.notNull(passwordPolicy, "Password policy is requred to generate password.");
		Assert.doesNotContain(passwordPolicy.getType().name(), IdmPasswordPolicyType.VALIDATE.name(), "Bad type.");
		String generateRandom = null;
		
		// generate password with passphrase or random
		if (passwordPolicy.getGenerateType() == IdmPasswordPolicyGenerateType.PASSPHRASE) {
			generateRandom = this.getPasswordGenerator().generatePassphrase(passwordPolicy);
		} else {
			generateRandom = this.getPasswordGenerator().generateRandom(passwordPolicy);
		}

		StringBuilder result = new StringBuilder();
		// prepare prefix and suffix
		String prefix = passwordPolicy.getPrefix();
		String suffix = passwordPolicy.getSuffix();

		if (StringUtils.isNotEmpty(prefix)) {
			result.append(prefix);
		}

		// append default generated password
		result.append(generateRandom);

		if (StringUtils.isNotEmpty(suffix)) {
			result.append(suffix);
		}

		return result.toString();
	}
	
	@Override
	public PasswordGenerator getPasswordGenerator() {
		if (this.passwordGenerator == null) {
			passwordGenerator = new PasswordGenerator();
		}
		return passwordGenerator;
	}

	@Override
	public String generatePasswordByDefault() {
		IdmPasswordPolicyDto defaultPasswordPolicy = this.getDefaultPasswordPolicy(IdmPasswordPolicyType.GENERATE);
		
		// if default password policy for generating not exist
		// generate random string
		if (defaultPasswordPolicy == null) {
			return this.getPasswordGenerator().generateRandom();
		}
		
		return this.generatePassword(defaultPasswordPolicy);
	}
	
	@Override
	public Integer getMaxPasswordAge(List<IdmPasswordPolicyDto> policyList) {
		Assert.notNull(policyList, "Policy list is required.");
		//
		if (policyList.isEmpty()) {
			return null;
		}
		//
		Integer passwordAge = Integer.MIN_VALUE;
		for (IdmPasswordPolicyDto idmPasswordPolicy : policyList) {
			if (idmPasswordPolicy.getMaxPasswordAge() != 0 && 
					idmPasswordPolicy.getMaxPasswordAge() > passwordAge) {
				passwordAge = idmPasswordPolicy.getMaxPasswordAge();
			}
		}
		//
		if (passwordAge.equals(Integer.MIN_VALUE)) {
			return null;
		} 
		//
		return passwordAge;
	}
	
	public void preValidate(IdmPasswordValidationDto passwordValidationDto) {
		IdmPasswordPolicyDto defaultPolicy = this.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);
		if (defaultPolicy == null) {
			defaultPolicy = new IdmPasswordPolicyDto();
		}
		List<IdmPasswordPolicyDto> passwordPolicyList = new ArrayList<IdmPasswordPolicyDto>();
		passwordPolicyList.add(defaultPolicy);
		preValidate(passwordValidationDto, passwordPolicyList);
	}

	public void preValidate(IdmPasswordValidationDto passwordValidationDto,
			List<IdmPasswordPolicyDto> passwordPolicyList) {
		passwordValidationDto.setPassword("");
		validate(passwordValidationDto, passwordPolicyList, true);
	}

	private void validate(IdmPasswordValidationDto passwordValidationDto, List<IdmPasswordPolicyDto> passwordPolicyList,
			boolean prevalidation) {
		Assert.notNull(passwordPolicyList, "Password policy list is required.");
		Assert.notNull(passwordValidationDto, "Password validation dto is required.");
		IdmIdentityDto identity = passwordValidationDto.getIdentity();
		
		// default password policy is used when list of password policies is empty, or for get maximum equals password
		IdmPasswordPolicyDto defaultPolicy = this.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);

		// if list is empty, get default password policy
		if (passwordPolicyList.isEmpty() && !prevalidation) {
			if (defaultPolicy != null) {
				passwordPolicyList.add(defaultPolicy);
			}
		}

		// if list with password policies is empty, validate is always true
		if (passwordPolicyList.isEmpty()) {
			// this state means that system idm hasn't default password policy
			return;
		}

		IdmPasswordDto oldPassword = null;
		// For checking with old password identity must has ID (for create doesn't exists ID)
		if (identity != null && identity.getId() != null) {
			oldPassword = passwordService.findOneByIdentity(identity.getId());
		}

		String password = passwordValidationDto.getPassword().asString();

		ZonedDateTime now = ZonedDateTime.now();

		Map<String, Object> errors = new HashMap<>();
		Set<Character> prohibitedChar = new HashSet<>();
		String prohibitedBeginChar=null;
		String prohibitedEndChar=null;
		List<String> policyNames = new ArrayList<String>();
		Map<String, Object> specialCharBase = new HashMap<>();
		Map<String, Object> forbiddenCharBase = new HashMap<>();
		Map<String, Object> forbiddenBeginCharBase = new HashMap<>();
		Map<String, Object> forbiddenEndCharBase = new HashMap<>();

		for (IdmPasswordPolicyDto passwordPolicy : passwordPolicyList) {
			if (passwordPolicy.isDisabled()) {
				continue;
			}
			boolean validateNotSuccess = false;

			// check if can change password for minimal age for change
			// if loged user is admin, skip this
			if (oldPassword != null && !securityService.isAdmin() && !prevalidation) {
				if (passwordPolicy.getMinPasswordAge() != null && oldPassword.getValidFrom()
						.plusDays(passwordPolicy.getMinPasswordAge()).compareTo(now.toLocalDate()) >= 1) {
					throw new ResultCodeException(CoreResultCode.PASSWORD_CANNOT_CHANGE, ImmutableMap.of(("date"),
							oldPassword.getValidFrom().plusDays(passwordPolicy.getMinPasswordAge())));
				}
			}

			// minimum rules to fulfill
			Map<String, Object> notPassRules = new HashMap<>();

			int minRulesToFulfill = passwordPolicy.getMinRulesToFulfill() == null ? 0
					: passwordPolicy.getMinRulesToFulfill().intValue();

			// check to max password length
			if (!isNull(passwordPolicy.getMaxPasswordLength()) && (password.length() > passwordPolicy.getMaxPasswordLength() ||  prevalidation)) {
				if (!passwordPolicy.isPasswordLengthRequired() && passwordPolicy.isEnchancedControl()) {
					notPassRules.put(MAX_LENGTH,
							Math.min(convertToInt(errors.get(MAX_LENGTH)), passwordPolicy.getMaxPasswordLength()));
				} else if (!(errors.containsKey(MAX_LENGTH)
						&& compareInt(passwordPolicy.getMaxPasswordLength(), errors.get(MAX_LENGTH)))) {
					errors.put(MAX_LENGTH, passwordPolicy.getMaxPasswordLength());
				}
				validateNotSuccess = true;
			}
			// check to minimal password length
			if (!isNullOrZeroValue(passwordPolicy.getMinPasswordLength())
					&& password.length() < passwordPolicy.getMinPasswordLength()) {
				if (!passwordPolicy.isPasswordLengthRequired() && passwordPolicy.isEnchancedControl()) {
					notPassRules.put(MIN_LENGTH,
							Math.max(convertToInt(errors.get(MIN_LENGTH)), passwordPolicy.getMinPasswordLength()));
				} else if (!(errors.containsKey(MIN_LENGTH)
						&& compareInt(errors.get(MIN_LENGTH), passwordPolicy.getMinPasswordLength()))) {
					errors.put(MIN_LENGTH, passwordPolicy.getMinPasswordLength());
				}
				validateNotSuccess = true;
			}
			// check to prohibited characters
			if (!Strings.isNullOrEmpty(passwordPolicy.getProhibitedCharacters())
					&& !password.matches("[^" + Pattern.quote(passwordPolicy.getProhibitedCharacters()) + "]*")) {
				for (char character : passwordPolicy.getProhibitedCharacters().toCharArray()) {
					if (password.indexOf(character) >= 0) {
						prohibitedChar.add(character);
					}
				}
				validateNotSuccess = true;
			}
			// check character at the beginning
			prohibitedBeginChar = checkInitialFinalCharForbidden(password, passwordPolicy.getProhibitedBeginCharacters(), true);
			// check character at the end
			prohibitedEndChar = checkInitialFinalCharForbidden(password, passwordPolicy.getProhibitedEndCharacters(), false);
			
			// check to minimal numbers
			if (!isNullOrZeroValue(passwordPolicy.getMinNumber()) && !password.matches("(.*["
					+ Pattern.quote(passwordPolicy.getNumberBase()) + "].*){" + passwordPolicy.getMinNumber() + ",}")) {
				if (!passwordPolicy.isNumberRequired() && passwordPolicy.isEnchancedControl()) {
					notPassRules.put(MIN_NUMBER,
							Math.max(convertToInt(errors.get(MIN_NUMBER)), passwordPolicy.getMinNumber()));
				} else if (!(errors.containsKey(MIN_NUMBER)
						&& compareInt(errors.get(MIN_NUMBER), passwordPolicy.getMinNumber()))) {
					errors.put(MIN_NUMBER, passwordPolicy.getMinNumber());
				}
				validateNotSuccess = true;
			}
			// check to minimal lower characters
			if (!isNullOrZeroValue(passwordPolicy.getMinLowerChar())
					&& !password.matches("(.*[" + Pattern.quote(passwordPolicy.getLowerCharBase()) + "].*){"
							+ passwordPolicy.getMinLowerChar() + ",}")) {
				if (!passwordPolicy.isLowerCharRequired() && passwordPolicy.isEnchancedControl()) {
					notPassRules.put(MIN_LOWER_CHAR,
							Math.max(convertToInt(errors.get(MIN_LOWER_CHAR)), passwordPolicy.getMinLowerChar()));
				} else if (!(errors.containsKey(MIN_LOWER_CHAR)
						&& compareInt(errors.get(MIN_LOWER_CHAR), passwordPolicy.getMinLowerChar()))) {
					errors.put(MIN_LOWER_CHAR, passwordPolicy.getMinLowerChar());
				}
				validateNotSuccess = true;
			}
			// check to minimal upper character
			if (!isNullOrZeroValue(passwordPolicy.getMinUpperChar())
					&& !password.matches("(.*[" + Pattern.quote(passwordPolicy.getUpperCharBase()) + "].*){"
							+ passwordPolicy.getMinUpperChar() + ",}")) {
				if (!passwordPolicy.isUpperCharRequired() && passwordPolicy.isEnchancedControl()) {
					notPassRules.put(MIN_UPPER_CHAR,
							Math.max(convertToInt(errors.get(MIN_UPPER_CHAR)), passwordPolicy.getMinUpperChar()));
				} else if (!(errors.containsKey(MIN_UPPER_CHAR)
						&& compareInt(errors.get(MIN_UPPER_CHAR), passwordPolicy.getMinUpperChar()))) {
					errors.put(MIN_UPPER_CHAR, passwordPolicy.getMinUpperChar());
				}
				validateNotSuccess = true;
			}
			// check to minimal special character and add special character base
			if (!isNullOrZeroValue(passwordPolicy.getMinSpecialChar())
					&& !password.matches("(.*[" + Pattern.quote(passwordPolicy.getSpecialCharBase()) + "].*){"
							+ passwordPolicy.getMinSpecialChar() + ",}")) {
				if (!passwordPolicy.isSpecialCharRequired() && passwordPolicy.isEnchancedControl()) {
					notPassRules.put(MIN_SPECIAL_CHAR,
							Math.max(convertToInt(errors.get(MIN_SPECIAL_CHAR)), passwordPolicy.getMinSpecialChar()));
					specialCharBase.put(passwordPolicy.getName(), passwordPolicy.getSpecialCharBase());
				} else if (!(errors.containsKey(MIN_SPECIAL_CHAR)
						&& compareInt(errors.get(MIN_SPECIAL_CHAR), passwordPolicy.getMinSpecialChar()))) {
					errors.put(MIN_SPECIAL_CHAR, passwordPolicy.getMinSpecialChar());
					specialCharBase.put(passwordPolicy.getName(), passwordPolicy.getSpecialCharBase());
				}
				validateNotSuccess = true;
			}
			
			// building of character bases of forbidden characters in passwords for password hints 
			if (passwordPolicy.getProhibitedCharacters() != null) {
				forbiddenCharBase.put(passwordPolicy.getName(), passwordPolicy.getProhibitedCharacters());
			}
			if(!Strings.isNullOrEmpty(passwordPolicy.getProhibitedBeginCharacters())) {
				forbiddenBeginCharBase.put(passwordPolicy.getName(), passwordPolicy.getProhibitedBeginCharacters());
			}
			
			if(!Strings.isNullOrEmpty(passwordPolicy.getProhibitedEndCharacters())) {
				forbiddenEndCharBase.put(passwordPolicy.getName(), passwordPolicy.getProhibitedEndCharacters());
			}

			if (!notPassRules.isEmpty() && passwordPolicy.isEnchancedControl()) {
				int notRequiredRules = passwordPolicy.getNotRequiredRules();
				int missingRules = notRequiredRules - notPassRules.size();
				if (missingRules - minRulesToFulfill < 0) {
					errors.put(MIN_RULES_TO_FULFILL_COUNT, minRulesToFulfill - missingRules); 
					errors.put(MIN_RULES_TO_FULFILL, notPassRules);
				}
			}

			// if not success we want password policy name
			if (validateNotSuccess && !errors.isEmpty() && !prevalidation) {
				policyNames.add(passwordPolicy.getName());
			}

			// check to similar identity attributes, enhanced control
			if (prevalidation) {
				enhancedControlForSimilar(passwordPolicy, prevalidation, errors);
			} else {
				enhancedControlForSimilar(passwordPolicy, passwordValidationDto, errors);
			}

			// TODO: weak words
		}
		
		if (!specialCharBase.isEmpty() && prevalidation) {
			errors.put(SPECIAL_CHARACTER_BASE, specialCharBase); 
		}
		
		if (!forbiddenCharBase.isEmpty() && prevalidation) {
			errors.put(FORBIDDEN_CHARACTER_BASE, forbiddenCharBase); 
		}
		
		if (!forbiddenBeginCharBase.isEmpty() && prevalidation) {
			errors.put(FORBIDDEN_BEGIN_CHARACTER_BASE, forbiddenBeginCharBase);
		}
		
		if (!forbiddenEndCharBase.isEmpty() && prevalidation) {
			errors.put(FORBIDDEN_END_CHARACTER_BASE, forbiddenEndCharBase);
		}

		if (!policyNames.isEmpty() && !prevalidation) {
			String name = prevalidation ? POLICY_NAME_PREVALIDATION : POLICY_NAME;
			errors.put(name, String.join(", ", policyNames));
		}

		if (!prohibitedChar.isEmpty()) {
			errors.put(COINTAIN_PROHIBITED, prohibitedChar.toString());
		}
		
		if(!Strings.isNullOrEmpty(prohibitedBeginChar)) {
			errors.put(BEGIN_PROHIBITED, prohibitedBeginChar);
		}
		
		if(!Strings.isNullOrEmpty(prohibitedEndChar)) {
			errors.put(END_PROHIBITED, prohibitedEndChar);
		}
		
		// password history. Skip when doesn't exists settings, or identity isn't saved
		// in some case (tests) are save identity in one transaction and id doesn't exist
		if (!prevalidation && defaultPolicy != null) {
			Integer maxHistorySimilar = defaultPolicy.getMaxHistorySimilar();
			if (maxHistorySimilar != null && identity != null && identity.getId() != null) {
				boolean checkHistory = passwordHistoryService.checkHistory(passwordValidationDto.getIdentity().getId(), maxHistorySimilar, passwordValidationDto.getPassword());
				
				if (checkHistory) {
					errors.put(MAX_HISTORY_SIMILAR, maxHistorySimilar);
				}
			}
		}
		
		if (!errors.isEmpty()) {
			// TODO: password policy audit
			if(prevalidation) {
				throw new ResultCodeException(CoreResultCode.PASSWORD_PREVALIDATION, errors);
			}
			throw new ResultCodeException(CoreResultCode.PASSWORD_DOES_NOT_MEET_POLICY, errors);
		}
	}

	/**
	 * Method sets to which attribute of identity cannot be similar to password - pre-validation
	 * 
	 * @param passwordPolicy
	 * @param prevalidation
	 * @param errors
	 * @return
	 */
	private Map<String, Object> enhancedControlForSimilar(IdmPasswordPolicyDto passwordPolicy, boolean prevalidation,
			Map<String, Object> errors) {
		if (passwordPolicy.isEnchancedControl()) {
			String[] attributes = passwordPolicy.getIdentityAttributeCheck().split(", ");
			for (int index = 0; index < attributes.length; index++) {
				if (attributes[index].equals(IdmPasswordPolicyIdentityAttributes.EMAIL.name())) {
					errors.put(PASSWORD_SIMILAR_EMAIL_PREVALIDATE, "");
				} else if (attributes[index].equals(IdmPasswordPolicyIdentityAttributes.FIRSTNAME.name())) {
					errors.put(PASSWORD_SIMILAR_FIRSTNAME_PREVALIDATE, "");
				} else if (attributes[index].equals(IdmPasswordPolicyIdentityAttributes.LASTNAME.name())) {
					errors.put(PASSWORD_SIMILAR_LASTNAME_PREVALIDATE, "");
				} else if (attributes[index].equals(IdmPasswordPolicyIdentityAttributes.USERNAME.name())) {
					errors.put(PASSWORD_SIMILAR_USERNAME_PREVALIDATE, "");
				} else if (attributes[index].equals(IdmPasswordPolicyIdentityAttributes.TITLESBEFORE.name())) {
					errors.put(PASSWORD_SIMILAR_TITLESBEFORE_PREVALIDATE, "");
				} else if (attributes[index].equals(IdmPasswordPolicyIdentityAttributes.TITLESAFTER.name())) {
					errors.put(PASSWORD_SIMILAR_TITLESAFTER_PREVALIDATE, "");
				} else if (attributes[index].equals(IdmPasswordPolicyIdentityAttributes.EXTERNALCODE.name())) {
					errors.put(PASSWORD_SIMILAR_EXTERNALCODE_PREVALIDATE, "");
				}
			}
		}
		return errors;
	}

	/**
	 * Method sets to which attribute of identity is similar to password - password validation
	 * 
	 * @param passwordPolicy
	 * @param passwordValidationDto
	 * @param errors
	 * @return
	 */
	private Map<String, Object> enhancedControlForSimilar(IdmPasswordPolicyDto passwordPolicy,
			IdmPasswordValidationDto passwordValidationDto, Map<String, Object> errors) {
		String password = passwordValidationDto.getPassword().asString();

		if (passwordPolicy.isEnchancedControl()) {
			String[] attributes = passwordPolicy.getIdentityAttributeCheck().split(", ");
			String passwordWithAccents = password.toLowerCase();
			IdmIdentityDto identity = passwordValidationDto.getIdentity();

			for (int index = 0; index < attributes.length; index++) {

				String attributeToCheck = attributes[index];
				String value = null;
				String controlledValue = null;
				boolean containsSubstring = false;

				if (IdmPasswordPolicyIdentityAttributes.EMAIL.name().equals(attributeToCheck)) {
					value = identity.getEmail();
					controlledValue = PASSWORD_SIMILAR_EMAIL;
					containsSubstring = containsEmailSubstring(passwordWithAccents, value);
				} else if (IdmPasswordPolicyIdentityAttributes.FIRSTNAME.name().equals(attributeToCheck)) {
					value = identity.getFirstName();
					controlledValue = PASSWORD_SIMILAR_FIRSTNAME;
					containsSubstring = containsGeneralSubstring(passwordWithAccents, value);
				} else if (IdmPasswordPolicyIdentityAttributes.LASTNAME.name().equals(attributeToCheck)) {
					value = identity.getLastName();
					controlledValue = PASSWORD_SIMILAR_LASTNAME;
					containsSubstring = containsGeneralSubstring(passwordWithAccents, value);
				} else if (IdmPasswordPolicyIdentityAttributes.USERNAME.name().equals(attributeToCheck)) {
					value = identity.getUsername();
					controlledValue = PASSWORD_SIMILAR_USERNAME;
					containsSubstring = containsGeneralSubstring(passwordWithAccents, value);
				} else if (IdmPasswordPolicyIdentityAttributes.EXTERNALCODE.name().equals(attributeToCheck)) {
					value = identity.getExternalCode();
					controlledValue = PASSWORD_SIMILAR_EXTERNALCODE;
					containsSubstring = containsGeneralSubstring(passwordWithAccents, value);
				} else if (IdmPasswordPolicyIdentityAttributes.TITLESBEFORE.name().equals(attributeToCheck)) {
					value = identity.getTitleBefore();
					controlledValue = PASSWORD_SIMILAR_TITLESBEFORE;
					containsSubstring = containsTitleSubstring(passwordWithAccents, value);
				} else if (IdmPasswordPolicyIdentityAttributes.TITLESAFTER.name().equals(attributeToCheck)) {
					value = identity.getTitleAfter();
					controlledValue = PASSWORD_SIMILAR_TITLESAFTER;
					containsSubstring = containsTitleSubstring(passwordWithAccents, value);
				}

				if (containsSubstring) {
					errors.put(controlledValue, StringUtils.trim(value));
					containsSubstring = false;
				}
			}
		}
		return errors;
	}
	
	/**
	 *  Method splits checkedStr argument to individual substrings and then tests
	 *  whether they are contained in the password. If found, true is returned otherwise false.
	 *  Only substrings longer than MIN_CONSIDERED_ATTR_LEN are considered.
	 * 
	 * @param password
	 * @param checkedStr
	 * @param delimiterSetRegExp
	 * @return
	 */
	private boolean findSubstringsByDelimiter(String password, String checkedStr, String delimiterSetRegExp) {
		if (password == null || checkedStr == null) {
			return false;
		}

		String splitted[] = checkedStr.split("[" + delimiterSetRegExp + "]+");
		for (int i = 0; i < splitted.length; ++i) {
			if (splitted[i].length() < MIN_CONSIDERED_ATTR_LEN) {
				continue;
			}
			if (password.contains(splitted[i])) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This methods checks presence of title string in the password.
	 * Needs special treating not to split titles according 'period' sign.
	 * Some of them would fall apart. ex. 'Ph.D.' -> 'Ph' and 'D' instead of required 'PhD'
	 * 
	 * @param passwod
	 * @param checkedStr
	 * @return
	 */
	private boolean containsTitleSubstring(String passwod, String checkedStr) {
		if (checkedStr == null) {
			return false;
		}
		String checkStringNoPeriod = checkedStr.replace(".", "");
		return containsGeneralSubstring(passwod, checkStringNoPeriod);
	}
	
	/**
	 * This method is specialization for searching of the email contained in password.
	 * Email is searched as a whole.
	 * 
	 * @param password
	 * @param checkedStr
	 * @return
	 */
	public boolean containsEmailSubstring(String password, String checkedStr) {
		checkedStr = StringUtils.trimToNull(checkedStr);
		if (StringUtils.isEmpty(checkedStr)) {
			return false;
		}
		String transformedValueWithAccents = StringUtils.lowerCase(checkedStr);
		boolean contains = password.contains(transformedValueWithAccents);
		if (!contains) {
			String passwordWithoutAccents = StringUtils.stripAccents(password);
			String transformedValueWithoutAccents = StringUtils.stripAccents(transformedValueWithAccents);
			contains = passwordWithoutAccents.contains(transformedValueWithoutAccents);
		}
		return contains;
	}
	
	/**
	 * General method searching for any of substrings from attribute in password.
	 *  
	 * 
	 * @param password
	 * @param checkedStr
	 * @return
	 */
	private boolean containsGeneralSubstring(String password, String checkedStr) {
		checkedStr = StringUtils.trimToNull(checkedStr);
		if (StringUtils.isEmpty(checkedStr)) {
			return false;
		}
		String transformedValueWithAccents = StringUtils.lowerCase(checkedStr);
		boolean contains = findSubstringsByDelimiter(password, transformedValueWithAccents, DELIMITER_SET_REGEXP);
		if (!contains) {
			String passwordWithoutAccents = StringUtils.stripAccents(password);
			String transformedValueWithoutAccents = StringUtils.stripAccents(transformedValueWithAccents);
			contains = findSubstringsByDelimiter(passwordWithoutAccents, transformedValueWithoutAccents,
					DELIMITER_SET_REGEXP);
		}
		return contains;
	}
	
	/**
	 * Check whether string starts/ends with forbidden char
	 * 
	 * @param checkedStr
	 * @param forbiddenChars
	 * @param isInitial - position toggle: true - initial char otherwise final char
	 * @return
	 */
	private String checkInitialFinalCharForbidden(String checkedStr, String forbiddenChars, boolean isInitial) {
		if (Strings.isNullOrEmpty(forbiddenChars) || Strings.isNullOrEmpty(checkedStr)) {
			return null;
		}
		int index = isInitial ? 0 : checkedStr.length() - 1;
		Character begin = checkedStr.charAt(index);
		if (forbiddenChars.contains(begin.toString())) {
			return begin.toString();
		}
		return null;
	}
	
	/**
	 * Method compare integer in o1 with o2
	 * 
	 * @param o1
	 * @param o2
	 * @return
	 */
	private boolean compareInt(Object o1, Object o2) {
		Integer i1 = Integer.valueOf(o1.toString());
		Integer i2 = Integer.valueOf(o2.toString());
		
		return i1 > i2;
	}
	
	/**
	 * Convert integer value in object to int. If Object is null,
	 * return 0.
	 * 
	 * @param object
	 * @return
	 */
	private int convertToInt(Object object) {
		if (object == null) {
			return 0;
		}
		return NumberUtils.toInt(object.toString());
	}
	
	/**
	 * Method checks if given {@link Integer} is null.
	 * 
	 * @param number
	 * @return true if Integer is null.
	 */
	private boolean isNull(Integer number) {
		return number == null;
	}
	
	/**
	 * Method checks if given {@link Integer} is null or zero value.
	 * 
	 * @param number
	 * @return true if Integer is null or zero value.
	 */
	private boolean isNullOrZeroValue(Integer number) {
		return isNull(number) || number.equals(0);
	}
	
	@Override
	public IdmPasswordPolicyDto getByCode(String code) {
		return findOneByName(code);
	}

	@Override
	public IdmPasswordPolicyDto findOneByName(String name) {
		return this.toDto(this.repository.findOneByName(name));
	}
}
