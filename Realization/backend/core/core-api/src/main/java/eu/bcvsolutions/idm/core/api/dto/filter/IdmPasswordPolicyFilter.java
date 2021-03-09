package eu.bcvsolutions.idm.core.api.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Default filter for password policy.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
public class IdmPasswordPolicyFilter 
		extends DataFilter 
		implements DisableableFilter {

	public static final String PARAMETER_PASSWORD_LENGTH_REQUIRED = "passwordLengthRequired";
	public static final String PARAMETER_MIN_PASSWORD_LENGTH = "minPasswordLength";
	public static final String PARAMETER_MAX_PASSWORD_LENGTH = "maxPasswordLength";
	public static final String PARAMETER_UPPER_CHAR_REQUIRED = "upperCharRequired";
	public static final String PARAMETER_MIN_UPPER_CHAR = "minUpperChar";
	public static final String PARAMETER_MAX_UPPER_CHAR = "maxUpperChar";
	public static final String PARAMETER_NUMBER_REQUIRED = "numberRequired";
	public static final String PARAMETER_MIN_NUMBER = "minNumber";
	public static final String PARAMETER_SPECIAL_CHAR_REQUIRED = "specialCharRequired";
	public static final String PARAMETER_MIN_SPECIAL_CHAR = "minSpecialChar";
	public static final String PARAMETER_WEAK_PASS_REQUIRED = "weakPassRequired";
	public static final String PARAMETER_WEAK_PASS = "weakPass";
	public static final String PARAMETER_MAX_PASSWORD_AGE = "maxPasswordAge";
	public static final String PARAMETER_MIN_PASSWORD_AGE = "minPasswordAge";
	public static final String PARAMETER_ENCHANCED_CONTROL = "enchancedControl";
	public static final String PARAMETER_MIN_RULES_TO_FULFILL = "minRulesToFulfill";    
	public static final String PARAMETER_TYPE = "type";
	public static final String PARAMETER_DEFAULT_POLICY = "defaultPolicy";
    
    public IdmPasswordPolicyFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmPasswordPolicyFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmPasswordPolicyFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmPasswordPolicyDto.class, data, parameterConverter);
	}

    public Boolean getPasswordLengthRequired() {
        return getParameterConverter().toBoolean(getData(), PARAMETER_PASSWORD_LENGTH_REQUIRED);
    }

    public void setPasswordLengthRequired(Boolean passwordLengthRequired) {
    	set(PARAMETER_PASSWORD_LENGTH_REQUIRED, passwordLengthRequired);
    }

    public Integer getMinPasswordLength() {
    	return getParameterConverter().toInteger(getData(), PARAMETER_MIN_PASSWORD_LENGTH);
    }

    public void setMinPasswordLength(Integer minPasswordLength) {
    	set(PARAMETER_MIN_PASSWORD_LENGTH, minPasswordLength);
    }

    public Integer getMaxPasswordLength() {
    	return getParameterConverter().toInteger(getData(), PARAMETER_MAX_PASSWORD_LENGTH);
    }

    public void setMaxPasswordLength(Integer maxPasswordLength) {
    	set(PARAMETER_MAX_PASSWORD_LENGTH, maxPasswordLength);
    }

    public Boolean getUpperCharRequired() {
    	return getParameterConverter().toBoolean(getData(), PARAMETER_UPPER_CHAR_REQUIRED);
    }

    public void setUpperCharRequired(Boolean upperCharRequired) {
    	set(PARAMETER_UPPER_CHAR_REQUIRED, upperCharRequired);
    }

    public Integer getMinUpperChar() {
    	return getParameterConverter().toInteger(getData(), PARAMETER_MIN_UPPER_CHAR);
    }

    public void setMinUpperChar(Integer minUpperChar) {
    	set(PARAMETER_MIN_UPPER_CHAR, minUpperChar);
    }

    public Integer getMaxUpperChar() {
    	return getParameterConverter().toInteger(getData(), PARAMETER_MAX_UPPER_CHAR);
    }

    public void setMaxUpperChar(Integer maxUpperChar) {
    	set(PARAMETER_MAX_UPPER_CHAR, maxUpperChar);
    }

    public Boolean getNumberRequired() {
    	return getParameterConverter().toBoolean(getData(), PARAMETER_NUMBER_REQUIRED);
    }

    public void setNumberRequired(Boolean numberRequired) {
    	set(PARAMETER_NUMBER_REQUIRED, numberRequired);
    }

    public Integer getMinNumber() {
    	return getParameterConverter().toInteger(getData(), PARAMETER_MIN_NUMBER);
    }

    public void setMinNumber(Integer minNumber) {
    	set(PARAMETER_MIN_NUMBER, minNumber);
    }

    public Boolean getSpecialCharRequired() {
    	return getParameterConverter().toBoolean(getData(), PARAMETER_SPECIAL_CHAR_REQUIRED);
    }

    public void setSpecialCharRequired(Boolean specialCharRequired) {
    	set(PARAMETER_SPECIAL_CHAR_REQUIRED, specialCharRequired);
    }

    public Integer getMinSpecialChar() {
    	return getParameterConverter().toInteger(getData(), PARAMETER_MIN_SPECIAL_CHAR);
    }

    public void setMinSpecialChar(Integer minSpecialChar) {
    	set(PARAMETER_MIN_SPECIAL_CHAR, minSpecialChar);
    }

    public Boolean getWeakPassRequired() {
    	return getParameterConverter().toBoolean(getData(), PARAMETER_WEAK_PASS_REQUIRED);
    }

    public void setWeakPassRequired(Boolean weakPassRequired) {
    	set(PARAMETER_WEAK_PASS_REQUIRED, weakPassRequired);
    }

    public String getWeakPass() {
    	return getParameterConverter().toString(getData(), PARAMETER_WEAK_PASS);
    }

    public void setWeakPass(String weakPass) {
    	set(PARAMETER_WEAK_PASS, weakPass);
    }

    public Integer getMaxPasswordAge() {
    	return getParameterConverter().toInteger(getData(), PARAMETER_MAX_PASSWORD_AGE);
    }

    public void setMaxPasswordAge(Integer maxPasswordAge) {
    	set(PARAMETER_MAX_PASSWORD_AGE, maxPasswordAge);
    }

    public Integer getMinPasswordAge() {
    	return getParameterConverter().toInteger(getData(), PARAMETER_MIN_PASSWORD_AGE);
    }

    public void setMinPasswordAge(Integer minPasswordAge) {
    	set(PARAMETER_MIN_PASSWORD_AGE, minPasswordAge);
    }

    public Boolean getEnchancedControl() {
    	return getParameterConverter().toBoolean(getData(), PARAMETER_ENCHANCED_CONTROL);
    }

    public void setEnchancedControl(Boolean enchancedControl) {
    	set(PARAMETER_ENCHANCED_CONTROL, enchancedControl);
    }

    public Integer getMinRulesToFulfill() {
    	return getParameterConverter().toInteger(getData(), PARAMETER_MIN_RULES_TO_FULFILL);
    }

    public void setMinRulesToFulfill(Integer minRulesToFulfill) {
    	set(PARAMETER_MIN_RULES_TO_FULFILL, minRulesToFulfill);
    }

    public IdmPasswordPolicyType getType() {
    	return getParameterConverter().toEnum(getData(), PARAMETER_TYPE, IdmPasswordPolicyType.class);
    }

    public void setType(IdmPasswordPolicyType type) {
    	set(PARAMETER_TYPE, type);
    }

    public Boolean getDefaultPolicy() {
    	return getParameterConverter().toBoolean(getData(), PARAMETER_DEFAULT_POLICY);
    }

    public void setDefaultPolicy(Boolean defaultPolicy) {
    	set(PARAMETER_DEFAULT_POLICY, defaultPolicy);
    }
}
