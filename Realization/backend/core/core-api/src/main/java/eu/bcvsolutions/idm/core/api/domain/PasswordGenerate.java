package eu.bcvsolutions.idm.core.api.domain;

/**
 * This interface should be used for class that want generate
 * password with {@link PasswordGenerator}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface PasswordGenerate {

	public int getMinPasswordLength();

	public int getMaxPasswordLength();

	public int getMinUpperChar();

	public int getMinNumber();

	public int getMinSpecialChar();
	
	public int getMinLowerChar();

	public String getProhibitedCharacters();

	public int getPassphraseWords();
	
	public String getSpecialCharBase();
	
	public String getUpperCharBase();
	
	public String getNumberBase();
	
	public String getLowerCharBase();
}
