package eu.bcvsolutions.idm.core.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyGenerateType;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;

/**
 * Excerpt Integererface for password policy
 * 
 * TODO: weak password dictonary is String
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Projection(name = "excerpt", types = IdmPasswordPolicy.class)
public interface IdmPasswordPolicyExcerpt extends AbstractDtoProjection {
	
	public String getName();
	
	public boolean isDisabled();
	
	public boolean isPasswordLengthRequired();

	public Integer getMinPasswordLength();

	public Integer getMaxPasswordLength();
	
	public boolean isUpperCharRequired();
	
	public Integer getMinUpperChar();
	
	public boolean isNumberRequired();

	public Integer getMinNumber();

	public boolean isSpecialCharRequired();

	public Integer getMinSpecialChar();

	public boolean isWeakPassRequired();
	
	public String getWeakPass();
	
	public Integer getMaxPasswordAge();
	
	public Integer getMinPasswordAge();

	public boolean isEnchancedControl();
	
	public Integer getMinRulesToFulfill();
	
	public IdmPasswordPolicyType getType();

	public boolean isDefaultPolicy();
	
	public String getProhibitedCharacters();
	
	public IdmPasswordPolicyGenerateType getGenerateType();
	
	public Integer getPassphraseWords();
	
	public Integer getMinLowerChar();
	
	public boolean isLowerCharRequired();
	
	public Integer getMaxHistorySimilar();
}
