package eu.bcvsolutions.idm.core.api.domain;

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
