package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.IdentifiableByName;
import eu.bcvsolutions.idm.core.api.domain.PasswordGenerate;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.utils.PasswordGenerator;
import eu.bcvsolutions.idm.core.model.domain.IdmPasswordPolicyGenerateType;
import eu.bcvsolutions.idm.core.model.domain.IdmPasswordPolicyType;

/**
 * Password policies entity:
 * Used for validation and generate password,
 * in idm system can be two default policies.
 * One default for validation and one for generate.
 * For generate passwords is use random generator and
 * passphrase
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Entity
@Table(name = "idm_password_policy", indexes = {
		@Index(name = "ux_pass_policy_name", columnList = "name", unique = true)
		})
public class IdmPasswordPolicy extends AbstractEntity implements IdentifiableByName, PasswordGenerate {

	private static final long serialVersionUID = -7107125399784973455L;
	
	@Audited
	@NotEmpty
	@Size(min = 0, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false)
	private String name;
	
	@Audited
	@Column(name = "disabled", nullable = false)
	private boolean disabled = false;

	@Audited
	@Column(name = "passwordLengthRequired", nullable = false)
	private boolean passwordLengthRequired = true;
	
	@Audited
	@Column(name = "minPasswordLength")
	private int minPasswordLength;
	
	@Audited
	@Column(name = "maxPasswordLength")
	private int maxPasswordLength;

	@Audited
	@Column(name = "upperCharRequired", nullable = false)
	private boolean upperCharRequired = true;
	
	@Audited
	@Column(name = "minUpperChar")
	private int minUpperChar;
	
	@Audited
	@Column(name = "lowerCharRequired", nullable = false)
	private boolean lowerCharRequired = true;

	@Audited
	@Column(name = "minLowerChar")
	private int minLowerChar;
	
	@Audited
	@Column(name = "numberRequired", nullable = false)
	private boolean numberRequired = true;
	
	@Audited
	@Column(name = "minNumber")
	private int minNumber;
	
	@Audited
	@Column(name = "specialCharRequired", nullable = false)
	private boolean specialCharRequired = true;
	
	@Audited
	@Column(name = "minSpecialChar")
	private int minSpecialChar;
	
	@Audited
	@Column(name = "weakPassRequired", nullable = false)
	private boolean weakPassRequired = true;
	
	@Audited
	@Column(name = "weakPass")
	private String weakPass;
	
	@Audited
	@Column(name = "maxPasswordAge")
	private int maxPasswordAge;
	
	@Audited
	@Column(name = "minPasswordAge")
	private int minPasswordAge;
	
	@Audited
	@Column(name = "enchancedControl", nullable = false)
	private boolean enchancedControl = false;
	
	@Audited
	@Column(name = "minRulesToFulfill")
	private int minRulesToFulfill;
	
	@Audited
	@Column(name = "type")
	private IdmPasswordPolicyType type = IdmPasswordPolicyType.VALIDATE;
	
	@Audited
	@Column(name = "generateType")
	private IdmPasswordPolicyGenerateType generateType = IdmPasswordPolicyGenerateType.RANDOM;
	
	@Audited
	@Column(name = "passphraseWords")
	private int passphraseWords;

	@Audited
	@Column(name = "prohibitedCharacters")
	private String prohibitedCharacters;

	@Audited
	@Column(name = "defaultPolicy", nullable = false)
	private boolean defaultPolicy = false;
	
	@Audited
	@Column(name = "specialCharBase")
	private String specialCharBase;
	
	@Audited
	@Column(name = "upperCharBase")
	private String upperCharBase;
	
	@Audited
	@Column(name = "numberBase")
	private String numberBase;
	
	@Audited
	@Column(name = "lowerCharBase")
	private String lowerCharBase;
	
	@Audited
	@Column(name = "maxHistorySimilar")
	private int maxHistorySimilar;
	
	public int getMaxHistorySimilar() {
		return maxHistorySimilar;
	}

	public void setMaxHistorySimilar(int maxHistorySimilar) {
		this.maxHistorySimilar = maxHistorySimilar;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isPasswordLengthRequired() {
		return passwordLengthRequired;
	}

	public void setPasswordLengthRequired(boolean passwordLengthRequired) {
		this.passwordLengthRequired = passwordLengthRequired;
	}

	public int getMinPasswordLength() {
		return minPasswordLength;
	}

	public void setMinPasswordLength(int minPasswordLength) {
		this.minPasswordLength = minPasswordLength;
	}

	public int getMaxPasswordLength() {
		return maxPasswordLength;
	}

	public void setMaxPasswordLength(int maxPasswordLength) {
		this.maxPasswordLength = maxPasswordLength;
	}

	public boolean isUpperCharRequired() {
		return upperCharRequired;
	}

	public void setUpperCharRequired(boolean upperCharRequired) {
		this.upperCharRequired = upperCharRequired;
	}

	public int getMinUpperChar() {
		return minUpperChar;
	}

	public void setMinUpperChar(int minUpperChar) {
		this.minUpperChar = minUpperChar;
	}

	public boolean isNumberRequired() {
		return numberRequired;
	}

	public void setNumberRequired(boolean numberRequired) {
		this.numberRequired = numberRequired;
	}

	public int getMinNumber() {
		return minNumber;
	}

	public void setMinNumber(int minNumber) {
		this.minNumber = minNumber;
	}

	public boolean isSpecialCharRequired() {
		return specialCharRequired;
	}

	public void setSpecialCharRequired(boolean specialCharRequired) {
		this.specialCharRequired = specialCharRequired;
	}

	public int getMinSpecialChar() {
		return minSpecialChar;
	}

	public void setMinSpecialChar(int minSpecialChar) {
		this.minSpecialChar = minSpecialChar;
	}

	public boolean isWeakPassRequired() {
		return weakPassRequired;
	}

	public void setWeakPassRequired(boolean weakPassRequired) {
		this.weakPassRequired = weakPassRequired;
	}

	public String getWeakPass() {
		return weakPass;
	}

	public void setWeakPass(String weakPass) {
		this.weakPass = weakPass;
	}

	public int getMaxPasswordAge() {
		return maxPasswordAge;
	}

	public void setMaxPasswordAge(int maxPasswordAge) {
		this.maxPasswordAge = maxPasswordAge;
	}

	public int getMinPasswordAge() {
		return minPasswordAge;
	}

	public void setMinPasswordAge(int minPasswordAge) {
		this.minPasswordAge = minPasswordAge;
	}

	public boolean isEnchancedControl() {
		return enchancedControl;
	}

	public void setEnchancedControl(boolean enchancedControl) {
		this.enchancedControl = enchancedControl;
	}

	public int getMinRulesToFulfill() {
		return minRulesToFulfill;
	}

	public void setMinRulesToFulfill(int minRulesToFulfill) {
		this.minRulesToFulfill = minRulesToFulfill;
	}

	public IdmPasswordPolicyType getType() {
		return type;
	}

	public void setType(IdmPasswordPolicyType type) {
		this.type = type;
	}

	public boolean isDefaultPolicy() {
		return defaultPolicy;
	}

	public void setDefaultPolicy(boolean defaultPolicy) {
		this.defaultPolicy = defaultPolicy;
	}
	
	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public IdmPasswordPolicyGenerateType getGenerateType() {
		return generateType;
	}

	public void setGenerateType(IdmPasswordPolicyGenerateType generateType) {
		this.generateType = generateType;
	}

	public String getProhibitedCharacters() {
		return prohibitedCharacters;
	}

	public void setProhibitedCharacters(String prohibitedCharacters) {
		this.prohibitedCharacters = prohibitedCharacters;
	}

	public int getPassphraseWords() {
		return passphraseWords;
	}

	public void setPassphraseWords(int passphraseWords) {
		this.passphraseWords = passphraseWords;
	}
	
	public boolean isLowerCharRequired() {
		return lowerCharRequired;
	}

	public void setLowerCharRequired(boolean lowerCharRequired) {
		this.lowerCharRequired = lowerCharRequired;
	}

	public int getMinLowerChar() {
		return minLowerChar;
	}

	public void setMinLowerChar(int minLowerChar) {
		this.minLowerChar = minLowerChar;
	}
	
	public String getSpecialCharBase() {
		if (specialCharBase == null) {
			return PasswordGenerator.SPECIAL_CHARACTERS;
		}
		return specialCharBase;
	}

	public void setSpecialCharBase(String specialCharBase) {
		if (!specialCharBase.equals(PasswordGenerator.SPECIAL_CHARACTERS)) {
			this.specialCharBase = specialCharBase;
		}
	}

	public String getUpperCharBase() {
		if (upperCharBase == null) {
			return PasswordGenerator.UPPER_CHARACTERS;
		}
		return upperCharBase;
	}

	public void setUpperCharBase(String upperCharBase) {
		if (!upperCharBase.equals(PasswordGenerator.UPPER_CHARACTERS)) {
			this.upperCharBase = upperCharBase;
		}
	}

	public String getNumberBase() {
		if (numberBase == null) {
			return PasswordGenerator.NUMBERS;
		}
		return numberBase;
	}

	public void setNumberBase(String numberBase) {
		if (!numberBase.equals(PasswordGenerator.NUMBERS)) {
			this.numberBase = numberBase;
		}
	}

	public String getLowerCharBase() {
		if (lowerCharBase == null) {
			return PasswordGenerator.LOWER_CHARACTERS;
		}
		return lowerCharBase;
	}

	public void setLowerCharBase(String lowerCharBase) {
		if (!lowerCharBase.equals(PasswordGenerator.LOWER_CHARACTERS)) {
			this.lowerCharBase = lowerCharBase;
		}
	}

	@Override
	public String getName() {
		return name;
	}
}
