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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.utils.PasswordGenerator;
import eu.bcvsolutions.idm.core.model.domain.IdmPasswordPolicyGenerateType;
import eu.bcvsolutions.idm.core.model.domain.IdmPasswordPolicyIdentityAttributes;
import eu.bcvsolutions.idm.core.model.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.model.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.model.dto.filter.PasswordPolicyFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmPassword;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordPolicyRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordPolicyService;

/**
 * Password policy service.
 * validation and generate method.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service
public class DefaultIdmPasswordPolicyService extends AbstractReadWriteEntityService<IdmPasswordPolicy, PasswordPolicyFilter> implements IdmPasswordPolicyService {
	
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
	private final IdmPasswordPolicyRepository passwordPolicyRepository;
	
	@Autowired
	public DefaultIdmPasswordPolicyService(
			AbstractEntityRepository<IdmPasswordPolicy, PasswordPolicyFilter> repository, IdmPasswordPolicyRepository passwordPolicyRepository) {
		super(repository);
		this.passwordPolicyRepository = passwordPolicyRepository;
	}
	
	@Override
	public IdmPasswordPolicy save(IdmPasswordPolicy entity) {
		if (validatePasswordPolicyAttributes(entity) && canSaveEntity(entity)) {
			return super.save(entity);
		} else {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_DEFAULT_TYPE, ImmutableMap.of("name", entity.getName()));
		}
	}
	
	/**
	 * method check if dont exist another default type of same type
	 * and min > max 
	 * this method also resave existing default password policy
	 * if exist.
	 * 
	 * @param entity
	 * @return false if there is any another default type 
	 * and entity can be save. True if entity can't be save
	 */
	private boolean canSaveEntity(IdmPasswordPolicy entity) {
		if (!entity.isDefaultPolicy()) {
			return true;
		}
		// create filter and found default password policy
		PasswordPolicyFilter filter = new PasswordPolicyFilter();
		filter.setDefaultPolicy(true);
		filter.setType(entity.getType());
		List<IdmPasswordPolicy> result = this.find(filter, null).getContent();
		
		if (result.isEmpty()) {
			return true;
		}
		
		IdmPasswordPolicy policy = result.get(0);
		
		if (policy.getId().equals(entity.getId())) {
			return true;
		} else {
			// if some entity was found remove set their default policy to false
			policy.setDefaultPolicy(false);
			// resave founded policy with change default password policy attribute
			this.save(policy);
			return true;
		}
	}
	
	/**
	 * Method check attributes of password policy
	 * TODO: send all error message at once
	 * 
	 * @param entity
	 * @return true, if password policy attribute are valid, otherwise throw error
	 */
	private boolean validatePasswordPolicyAttributes(IdmPasswordPolicy entity) {
		if (entity.getMaxPasswordLength() < entity.getMinPasswordLength()) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_MAX_LENGTH_LOWER);
		}
		if (entity.getMinLowerChar() + entity.getMinNumber() + entity.getMinSpecialChar() + entity.getMinUpperChar() > 
				entity.getMaxPasswordLength()) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_CANNOT_CHANGE);
		}
		if (entity.getMaxPasswordAge() < entity.getMinPasswordAge()) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_CANNOT_CHANGE);
		}
		return true;
	}
	
	@Override
	public boolean validate(IdmPasswordValidationDto passwordValidationDto) {
		return this.validate(passwordValidationDto, this.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE));
	}
	
	@Override
	public boolean validate(IdmPasswordValidationDto passwordValidationDto, IdmPasswordPolicy passwordPolicy) {
		List<IdmPasswordPolicy> passwordPolicyList = new ArrayList<IdmPasswordPolicy>();
		passwordPolicyList.add(passwordPolicy);
		
		return this.validate(passwordValidationDto, passwordPolicyList);
	}

	@Override
	public boolean validate(IdmPasswordValidationDto passwordValidationDto, List<IdmPasswordPolicy> passwordPolicyList) {
		Assert.notNull(passwordPolicyList);
		Assert.notNull(passwordValidationDto);
		
		// if list is empty, get default password policy
		if (passwordPolicyList.isEmpty()) {
			IdmPasswordPolicy defaultPolicy = this.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);
			passwordPolicyList.add(defaultPolicy);
		}
		
		// if list with password policies has 1 password policy and it is null, validate is always true
		if (passwordPolicyList.size() == 1 && passwordPolicyList.get(0) == null) {
			// this state means that system idm hasn't default password policy
			return true;
		}
		
		IdmPassword oldPassword = passwordValidationDto.getOldPassword();
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
			if (oldPassword != null) {
				if (oldPassword.getValidFrom().plusDays(passwordPolicy.getMinPasswordAge()).compareTo(now.toLocalDate()) > 1) {
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
			
			// if not success we want password policy name
			if (validateNotSuccess) {
				policyNames.add(passwordPolicy.getName());
			}
			
			// check how many required rules is not filled
			if (notPassRules.size() >= minRulesToFulfill) {
				errors.put(MIN_RULES_TO_FULFILL_COUNT, minRulesToFulfill);
				errors.put(MIN_RULES_TO_FULFILL, notPassRules);
			}
			
			// check to similar identity attributes, enchanced control
			if (passwordPolicy.isEnchancedControl()) {
				String[] attributes = passwordPolicy.getIdentityAttributeCheck().split(", ");
				IdmIdentity identity = passwordValidationDto.getIdentity();
				for (int index = 0; index < attributes.length; index++) {
					if (attributes[index].equals(IdmPasswordPolicyIdentityAttributes.EMAIL.name())) {
						if (identity.getEmail().toLowerCase().matches("(?i).*" + password.toLowerCase() + ".*")) {
							errors.put(PASSWORD_SIMILAR_EMAIL, identity.getEmail());
						}
					} else if (attributes[index].equals(IdmPasswordPolicyIdentityAttributes.FIRSTNAME.name())) {
						if (identity.getFirstName().toLowerCase().matches("(?i).*" + password.toLowerCase() + ".*")) {
							errors.put(PASSWORD_SIMILAR_FIRSTNAME, identity.getFirstName());
						}
					} else if (attributes[index].equals(IdmPasswordPolicyIdentityAttributes.LASTNAME.name())) {
						if (identity.getLastName().toLowerCase().matches("(?i).*" + password.toLowerCase() + ".*")) {
							errors.put(PASSWORD_SIMILAR_LASTNAME, identity.getLastName());
						}
					} else if (attributes[index].equals(IdmPasswordPolicyIdentityAttributes.USERNAME.name())) {
						if (identity.getUsername().toLowerCase().matches("(?i).*" + password.toLowerCase() + ".*")) {
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
		
		return true;
	}

	@Override
	public IdmPasswordPolicy getDefaultPasswordPolicy(IdmPasswordPolicyType type) {
		List<IdmPasswordPolicy> policiesList = passwordPolicyRepository.findDefaultType(type, new PageRequest(0, 1));
		if (policiesList.isEmpty()) {
			return null;
		}
		return policiesList.get(0);
	}

	@Override
	public String generatePassword(IdmPasswordPolicy passwordPolicy) {
		Assert.notNull(passwordPolicy);
		Assert.doesNotContain(passwordPolicy.getType().name(), IdmPasswordPolicyType.VALIDATE.name(), "Bad type.");
		if (passwordPolicy.getGenerateType().equals(IdmPasswordPolicyGenerateType.PASSPHRASE)) {
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
		PasswordPolicyFilter filter = new PasswordPolicyFilter();
		filter.setDefaultPolicy(true);
		filter.setType(IdmPasswordPolicyType.GENERATE);
		List<IdmPasswordPolicy> list = this.find(filter, null).getContent();
		
		if (list.isEmpty() || list.size() > 1) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_DEFAULT_TYPE_NOT_EXIST);
		}
		
		return this.generatePassword(list.get(0));
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
		Integer i1 = Integer.parseInt(o1.toString());
		Integer i2 = Integer.parseInt(o2.toString());
		
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
}
