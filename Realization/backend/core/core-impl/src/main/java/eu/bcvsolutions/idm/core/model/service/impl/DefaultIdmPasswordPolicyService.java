package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyGenerateType;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyIdentityAttributes;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.api.dto.filter.PasswordPolicyFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.PasswordGenerator;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.event.PasswordPolicyEvent;
import eu.bcvsolutions.idm.core.model.event.PasswordPolicyEvent.PasswordPolicyEvenType;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordPolicyRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordService;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Password policy service.
 * validation and generate method.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class DefaultIdmPasswordPolicyService extends AbstractReadWriteEntityService<IdmPasswordPolicy, PasswordPolicyFilter> implements IdmPasswordPolicyService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmPasswordPolicyService.class);
	
	// TODO: better place for constant?
	private static final String MIN_LENGTH = "minLength";
	private static final String MAX_LENGTH = "maxLength";
	private static final String MIN_UPPER_CHAR = "minUpperChar";
	private static final String MIN_LOWER_CHAR = "minLowerChar";
	private static final String MIN_NUMBER = "minNumber";
	private static final String MIN_SPECIAL_CHAR = "minSpecialChar";
	private static final String COINTAIN_PROHIBITED = "prohibited";
	// private static final String CONTAIN_WEAK_PASS = "weakPass"; // TODO
	private static final String MIN_RULES_TO_FULFILL = "minRulesToFulfill";
	private static final String MIN_RULES_TO_FULFILL_COUNT = "minRulesToFulfillCount";
	private static final String POLICY_NAME = "policiesNames";
	private static final String PASSWORD_SIMILAR_USERNAME = "passwordSimilarUsername";
	private static final String PASSWORD_SIMILAR_EMAIL = "passwordSimilarEmail";
	private static final String PASSWORD_SIMILAR_FIRSTNAME = "passwordSimilarFirstName";
	private static final String PASSWORD_SIMILAR_LASTNAME = "passwordSimilarLastName";
	
	private PasswordGenerator passwordGenerator;
	private final IdmPasswordPolicyRepository repository;
	private final SecurityService securityService;
	private final EntityEventManager entityEventProcessorService;
	private final IdmPasswordService passwordService;
	
	@Autowired
	public DefaultIdmPasswordPolicyService(
			IdmPasswordPolicyRepository repository,
			EntityEventManager entityEventProcessorService,
			SecurityService securityService,
			IdmPasswordService passwordService) {
		super(repository);
		//
		Assert.notNull(entityEventProcessorService);
		Assert.notNull(repository);
		Assert.notNull(securityService);
		Assert.notNull(passwordService);
		//
		this.entityEventProcessorService = entityEventProcessorService;
		this.repository = repository;
		this.securityService = securityService;
		this.passwordService = passwordService;
	}
	
	@Override
	@Transactional
	public IdmPasswordPolicy save(IdmPasswordPolicy entity) {
		Assert.notNull(entity);
		//
		LOG.debug("Saving entity [{}]", entity.getName());
		if (isNew(entity)) {
			// throw event with create
			return entityEventProcessorService.process(new PasswordPolicyEvent(PasswordPolicyEvenType.CREATE, entity)).getContent();
		}
		// else throw event with update
		return entityEventProcessorService.process(new PasswordPolicyEvent(PasswordPolicyEvenType.UPDATE, entity)).getContent();
	}
	
	@Override
	@Transactional
	public void delete(IdmPasswordPolicy entity) {
		Assert.notNull(entity);
		//
		LOG.debug("Delete entity [{}]", entity.getName());
		//
		entityEventProcessorService.process(new PasswordPolicyEvent(PasswordPolicyEvenType.DELETE, entity)).getContent();
	}
	
	@Override
	public void validate(IdmPasswordValidationDto passwordValidationDto) {
		this.validate(passwordValidationDto, this.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE));
	}
	
	@Override
	public void validate(IdmPasswordValidationDto passwordValidationDto, IdmPasswordPolicy passwordPolicy) {
		List<IdmPasswordPolicy> passwordPolicyList = new ArrayList<IdmPasswordPolicy>();
		
		if (passwordPolicy != null) {
			passwordPolicyList.add(passwordPolicy);
		}
		
		this.validate(passwordValidationDto, passwordPolicyList);
	}

	@Override
	public void validate(IdmPasswordValidationDto passwordValidationDto, List<IdmPasswordPolicy> passwordPolicyList) {
		Assert.notNull(passwordPolicyList);
		Assert.notNull(passwordValidationDto);
		
		// if list is empty, get default password policy
		if (passwordPolicyList.isEmpty()) {
			IdmPasswordPolicy defaultPolicy = this.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);
			if (defaultPolicy != null) {
				passwordPolicyList.add(defaultPolicy);
			}
		}
		
		// if list with password policies is empty, validate is always true
		if (passwordPolicyList.isEmpty()) {
			// this state means that system idm hasn't default password policy
			return;
		}
		
		IdmPasswordDto oldPassword = passwordValidationDto.getOldPassword() != null ? passwordService.get(passwordValidationDto.getOldPassword()) : null;
		String password = passwordValidationDto.getPassword().asString();
		
		DateTime now = new DateTime();
		
		Map<String, Object> errors = new HashMap<>();
		Set<Character> prohibitedChar = new HashSet<>();
		List<String> policyNames = new ArrayList<String>();
		
		for (IdmPasswordPolicy passwordPolicy : passwordPolicyList) {
			if (passwordPolicy.isDisabled()) {
				continue;
			}
			boolean validateNotSuccess = false;
			
			// check if can change password for minimal age for change
			// if loged user is admin, skip this
			if (oldPassword != null && !securityService.isAdmin()) {
				if (oldPassword.getValidFrom().plusDays(passwordPolicy.getMinPasswordAge()).compareTo(now.toLocalDate()) >= 1) {
					throw new ResultCodeException(CoreResultCode.PASSWORD_CANNOT_CHANGE,
							ImmutableMap.of(("date"), oldPassword.getValidFrom().plusDays(passwordPolicy.getMinPasswordAge())));
				}
			}
			
			// minimum rules to fulfill
			Map<String, Object> notPassRules = new HashMap<>();
			int minRulesToFulfill = passwordPolicy.getMinRulesToFulfill();
			
			// check to max password length
			if (passwordPolicy.getMaxPasswordLength() != 0 && password.length() > passwordPolicy.getMaxPasswordLength()) {
				if (!passwordPolicy.isPasswordLengthRequired() && passwordPolicy.isEnchancedControl()) {
					notPassRules.put(MAX_LENGTH, Math.max(convertToInt(errors.get(MAX_LENGTH)), passwordPolicy.getMaxPasswordLength()));
				} else if (!(errors.containsKey(MAX_LENGTH) && compareInt(errors.get(MAX_LENGTH), passwordPolicy.getMaxPasswordLength()))) {
					errors.put(MAX_LENGTH, passwordPolicy.getMaxPasswordLength());
				}
				validateNotSuccess = true;
			}
			// check to minimal password length
			if (passwordPolicy.getMinPasswordLength() != 0 && password.length() < passwordPolicy.getMinPasswordLength()) {
				if (!passwordPolicy.isPasswordLengthRequired() && passwordPolicy.isEnchancedControl()) {
					notPassRules.put(MIN_LENGTH, Math.max(convertToInt(errors.get(MIN_LENGTH)), passwordPolicy.getMinPasswordLength()));
				} else if (!(errors.containsKey(MIN_LENGTH) && compareInt(errors.get(MIN_LENGTH), passwordPolicy.getMinPasswordLength()))) {
					errors.put(MIN_LENGTH, passwordPolicy.getMinPasswordLength());
				}
				validateNotSuccess = true;
			}
			// check to prohibited characters
			if (!Strings.isNullOrEmpty(passwordPolicy.getProhibitedCharacters()) && !password.matches("[^" + passwordPolicy.getProhibitedCharacters() + "]*")) {
				for (char character : passwordPolicy.getProhibitedCharacters().toCharArray()) {
					if (password.indexOf(character) >= 0) {
						prohibitedChar.add(character);
					}
				}
				validateNotSuccess = true;
			}
			// check to minimal numbers
			if (passwordPolicy.getMinNumber() != 0 && !password.matches("(.*[" + Pattern.quote(passwordPolicy.getNumberBase()) + "].*){" + passwordPolicy.getMinNumber() + ",}")) {
				if (!passwordPolicy.isNumberRequired() && passwordPolicy.isEnchancedControl()) {
					notPassRules.put(MIN_NUMBER, Math.max(convertToInt(errors.get(MIN_NUMBER)), passwordPolicy.getMinNumber()));
				} else if (!(errors.containsKey(MIN_NUMBER) && compareInt(errors.get(MIN_NUMBER), passwordPolicy.getMinNumber()))) {
					errors.put(MIN_NUMBER, passwordPolicy.getMinNumber());
				}
				validateNotSuccess = true;
			}
			// check to minimal lower characters
			if (passwordPolicy.getMinLowerChar() != 0 && !password.matches("(.*[" + Pattern.quote(passwordPolicy.getLowerCharBase()) + "].*){" + passwordPolicy.getMinLowerChar() + ",}")) {
				if (!passwordPolicy.isLowerCharRequired() && passwordPolicy.isEnchancedControl()) {
					notPassRules.put(MIN_LOWER_CHAR, Math.max(convertToInt(errors.get(MIN_LOWER_CHAR)), passwordPolicy.getMinLowerChar()));
				} else if (!(errors.containsKey(MIN_LOWER_CHAR) && compareInt(errors.get(MIN_LOWER_CHAR), passwordPolicy.getMinLowerChar()))) {
					errors.put(MIN_LOWER_CHAR, passwordPolicy.getMinLowerChar());
				}
				validateNotSuccess = true;
			}
			// check to minimal upper character
			if (passwordPolicy.getMinUpperChar() != 0 && !password.matches("(.*[" + Pattern.quote(passwordPolicy.getUpperCharBase()) + "].*){" + passwordPolicy.getMinUpperChar() + ",}")) {
				if (!passwordPolicy.isUpperCharRequired() && passwordPolicy.isEnchancedControl()) {
					notPassRules.put(MIN_UPPER_CHAR, Math.max(convertToInt(errors.get(MIN_UPPER_CHAR)), passwordPolicy.getMinUpperChar()));
				} else if (!(errors.containsKey(MIN_UPPER_CHAR) && compareInt(errors.get(MIN_UPPER_CHAR), passwordPolicy.getMinUpperChar()))) {
					errors.put(MIN_UPPER_CHAR, passwordPolicy.getMinUpperChar());
				}
			}
			// check to minimal special character
			if (passwordPolicy.getMinSpecialChar() != 0 && !password.matches("(.*[" + Pattern.quote(passwordPolicy.getSpecialCharBase()) + "].*){" + passwordPolicy.getMinSpecialChar() + ",}")) {
				if (!passwordPolicy.isSpecialCharRequired() && passwordPolicy.isEnchancedControl()) {
					notPassRules.put(MIN_SPECIAL_CHAR, Math.max(convertToInt(errors.get(MIN_SPECIAL_CHAR)), passwordPolicy.getMinSpecialChar()));
				} else if (!(errors.containsKey(MIN_SPECIAL_CHAR) && compareInt(errors.get(MIN_SPECIAL_CHAR), passwordPolicy.getMinSpecialChar()))) {
					errors.put(MIN_SPECIAL_CHAR, passwordPolicy.getMinSpecialChar());
				}
				validateNotSuccess = true;
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
			if (validateNotSuccess && !errors.isEmpty()) {
				policyNames.add(passwordPolicy.getName());
			}
			
			// check to similar identity attributes, enhanced control
			if (passwordPolicy.isEnchancedControl()) {
				String[] attributes = passwordPolicy.getIdentityAttributeCheck().split(", ");
				IdmIdentityDto identity = passwordValidationDto.getIdentity();
				for (int index = 0; index < attributes.length; index++) {
					if (attributes[index].equals(IdmPasswordPolicyIdentityAttributes.EMAIL.name())) {
						if (identity.getEmail()!= null && identity.getEmail().toLowerCase().matches("(?i).*" + password.toLowerCase() + ".*")) {
							errors.put(PASSWORD_SIMILAR_EMAIL, identity.getEmail());
						}
					} else if (attributes[index].equals(IdmPasswordPolicyIdentityAttributes.FIRSTNAME.name())) {
						if (identity.getFirstName() != null && identity.getFirstName().toLowerCase().matches("(?i).*" + password.toLowerCase() + ".*")) {
							errors.put(PASSWORD_SIMILAR_FIRSTNAME, identity.getFirstName());
						}
					} else if (attributes[index].equals(IdmPasswordPolicyIdentityAttributes.LASTNAME.name())) {
						if (identity.getLastName() != null && identity.getLastName().toLowerCase().matches("(?i).*" + password.toLowerCase() + ".*")) {
							errors.put(PASSWORD_SIMILAR_LASTNAME, identity.getLastName());
						}
					} else if (attributes[index].equals(IdmPasswordPolicyIdentityAttributes.USERNAME.name())) {
						if (identity.getUsername() != null && identity.getUsername().toLowerCase().matches("(?i).*" + password.toLowerCase() + ".*")) {
							errors.put(PASSWORD_SIMILAR_USERNAME, identity.getUsername());
						}
					}
				}
			}
			
			// TODO: weak words
			
			// TODO: history similiar
		}
		
		if (!policyNames.isEmpty()) {
			errors.put(POLICY_NAME, String.join(", ", policyNames));
		}
		
		if (!prohibitedChar.isEmpty()) {
			errors.put(COINTAIN_PROHIBITED, prohibitedChar.toString());
		}
		
		if (!errors.isEmpty()) {
			// TODO: password policy audit
			throw new ResultCodeException(CoreResultCode.PASSWORD_DOES_NOT_MEET_POLICY, errors);
		}
	}

	@Override
	public IdmPasswordPolicy getDefaultPasswordPolicy(IdmPasswordPolicyType type) {
		IdmPasswordPolicy defaultPolicy = repository.findOneDefaultType(type);
		return defaultPolicy;
	}

	@Override
	public String generatePassword(IdmPasswordPolicy passwordPolicy) {
		Assert.notNull(passwordPolicy);
		Assert.doesNotContain(passwordPolicy.getType().name(), IdmPasswordPolicyType.VALIDATE.name(), "Bad type.");
		if (passwordPolicy.getGenerateType() == IdmPasswordPolicyGenerateType.PASSPHRASE) {
			return this.getPasswordGenerator().generatePassphrase(passwordPolicy);
		}
		// TODO: use random generate?
		return this.getPasswordGenerator().generateRandom(passwordPolicy);
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
		IdmPasswordPolicy defaultPasswordPolicy = this.getDefaultPasswordPolicy(IdmPasswordPolicyType.GENERATE);
		
		// if default password policy for generating not exist
		// generate random string
		if (defaultPasswordPolicy == null) {
			return this.getPasswordGenerator().generateRandom();
		}
		
		return this.generatePassword(defaultPasswordPolicy);
	}
	
	@Override
	public Integer getMaxPasswordAge(List<IdmPasswordPolicy> policyList) {
		Assert.notNull(policyList);
		//
		if (policyList.isEmpty()) {
			return null;
		}
		//
		Integer passwordAge = new Integer(Integer.MIN_VALUE);
		for (IdmPasswordPolicy idmPasswordPolicy : policyList) {
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


	@Override
	public IdmPasswordPolicy findOneByName(String name) {
		return this.repository.findOneByName(name);
	}
}
