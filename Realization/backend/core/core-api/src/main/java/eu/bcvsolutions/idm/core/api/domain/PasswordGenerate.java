package eu.bcvsolutions.idm.core.api.domain;

/**
 * This interface should be used for class that want generate
 * password with {@link PasswordGenerator}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */
public interface PasswordGenerate {

	Integer getMinPasswordLength();

	Integer getMaxPasswordLength();

	Integer getMinUpperChar();

	Integer getMinNumber();

	Integer getMinSpecialChar();
	
	Integer getMinLowerChar();

	String getProhibitedCharacters();

	Integer getPassphraseWords();
	
	String getSpecialCharBase();
	
	String getUpperCharBase();
	
	String getNumberBase();
	
	String getLowerCharBase();
}
