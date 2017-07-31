package eu.bcvsolutions.idm.core.api.domain;

/**
 * This interface should be used for class that want generate
 * password with {@link PasswordGenerator}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */

public interface PasswordGenerate {

	public Integer getMinPasswordLength();

	public Integer getMaxPasswordLength();

	public Integer getMinUpperChar();

	public Integer getMinNumber();

	public Integer getMinSpecialChar();
	
	public Integer getMinLowerChar();

	public String getProhibitedCharacters();

	public Integer getPassphraseWords();
	
	public String getSpecialCharBase();
	
	public String getUpperCharBase();
	
	public String getNumberBase();
	
	public String getLowerCharBase();
}
