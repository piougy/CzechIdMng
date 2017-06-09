package eu.bcvsolutions.idm.core.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyGenerateType;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;

/**
 * Excerpt interface for password policy
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

	public int getMinPasswordLength();

	public int getMaxPasswordLength();
	
	public boolean isUpperCharRequired();
	
	public int getMinUpperChar();
	
	public boolean isNumberRequired();

	public int getMinNumber();

	public boolean isSpecialCharRequired();

	public int getMinSpecialChar();

	public boolean isWeakPassRequired();
	
	public String getWeakPass();
	
	public int getMaxPasswordAge();
	
	public int getMinPasswordAge();

	public boolean isEnchancedControl();
	
	public int getMinRulesToFulfill();
	
	public IdmPasswordPolicyType getType();

	public boolean isDefaultPolicy();
	
	public String getProhibitedCharacters();
	
	public IdmPasswordPolicyGenerateType getGenerateType();
	
	public int getPassphraseWords();
	
	public int getMinLowerChar();
	
	public boolean isLowerCharRequired();
	
	public int getMaxHistorySimilar();
}
