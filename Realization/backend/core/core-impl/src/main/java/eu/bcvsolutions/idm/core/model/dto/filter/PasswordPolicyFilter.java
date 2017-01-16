package eu.bcvsolutions.idm.core.model.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.model.domain.IdmPasswordPolicyType;

/**
 * Default filter for password policy
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class PasswordPolicyFilter extends QuickFilter {
	
	private Boolean passwordLengthRequired;

	private Integer minPasswordLength;

	private Integer maxPasswordLength;

	private Boolean upperCharRequired;
	
	private Integer minUpperChar;

	private Integer maxUpperChar;

	private Boolean numberRequired ;

	private Integer minNumber;

	private Boolean specialCharRequired ;

	private Integer minSpecialChar;

	private Boolean weakPassRequired;

	private String weakPass;

	private Integer maxPasswordAge;

	private Integer minPasswordAge;

	private Boolean enchancedControl;

	private Integer minRulesToFulfill;

	public Boolean getPasswordLengthRequired() {
		return passwordLengthRequired;
	}

	public void setPasswordLengthRequired(Boolean passwordLengthRequired) {
		this.passwordLengthRequired = passwordLengthRequired;
	}

	public Integer getMinPasswordLength() {
		return minPasswordLength;
	}

	public void setMinPasswordLength(Integer minPasswordLength) {
		this.minPasswordLength = minPasswordLength;
	}

	public Integer getMaxPasswordLength() {
		return maxPasswordLength;
	}

	public void setMaxPasswordLength(Integer maxPasswordLength) {
		this.maxPasswordLength = maxPasswordLength;
	}

	public Boolean getUpperCharRequired() {
		return upperCharRequired;
	}

	public void setUpperCharRequired(Boolean upperCharRequired) {
		this.upperCharRequired = upperCharRequired;
	}

	public Integer getMinUpperChar() {
		return minUpperChar;
	}

	public void setMinUpperChar(Integer minUpperChar) {
		this.minUpperChar = minUpperChar;
	}

	public Integer getMaxUpperChar() {
		return maxUpperChar;
	}

	public void setMaxUpperChar(Integer maxUpperChar) {
		this.maxUpperChar = maxUpperChar;
	}

	public Boolean getNumberRequired() {
		return numberRequired;
	}

	public void setNumberRequired(Boolean numberRequired) {
		this.numberRequired = numberRequired;
	}

	public Integer getMinNumber() {
		return minNumber;
	}

	public void setMinNumber(Integer minNumber) {
		this.minNumber = minNumber;
	}

	public Boolean getSpecialCharRequired() {
		return specialCharRequired;
	}

	public void setSpecialCharRequired(Boolean specialCharRequired) {
		this.specialCharRequired = specialCharRequired;
	}

	public Integer getMinSpecialChar() {
		return minSpecialChar;
	}

	public void setMinSpecialChar(Integer minSpecialChar) {
		this.minSpecialChar = minSpecialChar;
	}

	public Boolean getWeakPassRequired() {
		return weakPassRequired;
	}

	public void setWeakPassRequired(Boolean weakPassRequired) {
		this.weakPassRequired = weakPassRequired;
	}

	public String getWeakPass() {
		return weakPass;
	}

	public void setWeakPass(String weakPass) {
		this.weakPass = weakPass;
	}

	public Integer getMaxPasswordAge() {
		return maxPasswordAge;
	}

	public void setMaxPasswordAge(Integer maxPasswordAge) {
		this.maxPasswordAge = maxPasswordAge;
	}

	public Integer getMinPasswordAge() {
		return minPasswordAge;
	}

	public void setMinPasswordAge(Integer minPasswordAge) {
		this.minPasswordAge = minPasswordAge;
	}

	public Boolean getEnchancedControl() {
		return enchancedControl;
	}

	public void setEnchancedControl(Boolean enchancedControl) {
		this.enchancedControl = enchancedControl;
	}

	public Integer getMinRulesToFulfill() {
		return minRulesToFulfill;
	}

	public void setMinRulesToFulfill(Integer minRulesToFulfill) {
		this.minRulesToFulfill = minRulesToFulfill;
	}

	public IdmPasswordPolicyType getType() {
		return type;
	}

	public void setType(IdmPasswordPolicyType type) {
		this.type = type;
	}

	public Boolean getDefaultPolicy() {
		return defaultPolicy;
	}

	public void setDefaultPolicy(Boolean defaultPolicy) {
		this.defaultPolicy = defaultPolicy;
	}

	private IdmPasswordPolicyType type;

	private Boolean defaultPolicy;
}
