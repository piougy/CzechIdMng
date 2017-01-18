package eu.bcvsolutions.idm.core.api.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import eu.bcvsolutions.idm.core.api.domain.PasswordGenerate;

/**
 * Password generator
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class PasswordGenerator {
	
	public static final String LOWER_CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
	
	public static final String UPPER_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	public static final String SPECIAL_CHARACTERS = "!@#$%&*";
	
	public static final String NUMBERS = "0123456789";
	
	private static final int ROLLS = 6;
	
	private static final String PATH = "eu/bcvsolutions/idm/core/diceware";
	
	private static final String DEFAULT_DICEWARE = "cs";
	
	private static final String DEFAULT_FILE_TYPE = "csv";
	
	private static final String DEFAULT_DELIMITER = " ";
	
	private Map<Integer, Long> filePosition;
	
	public String generatePassphrase(PasswordGenerate policy) {
		notNull(policy, "Password policy can't be null.");
		List<String> password = new ArrayList<>();
		
		for (int index = 0; index < policy.getPassphraseWords(); index++) {
			
			StringBuilder number = new StringBuilder();
			for (int roll = 1; roll < ROLLS; roll++) {
				number.append(getRandomNumber(1, 6));
			}
			password.add(getWordFromDictonary(Integer.parseInt(number.toString()), DEFAULT_DICEWARE));
		}
		
		return String.join(DEFAULT_DELIMITER, password);
	}
	
	/**
	 * Method create password by given password policy that implements interface PasswordGenerate
	 * 
	 * @param policy
	 * @return
	 */
	public String generateRandom(PasswordGenerate policy) {
		notNull(policy, "Password policy can't be null.");
		notNull(policy.getMinPasswordLength(), "Parameter: minLength can't be null!");
		notNull(policy.getMaxPasswordLength(), "Parameter: maxLength can't be null!");
		if (policy.getMaxPasswordLength() < policy.getMinPasswordLength()) {
			throw new IllegalArgumentException("Parameter: maxLength can't be lower than parameter: minLength");
		}
		
		if (policy.getMaxPasswordLength() < (policy.getMinLowerChar() + policy.getMinUpperChar() + policy.getMinSpecialChar() + policy.getMinNumber())) {
			throw new IllegalArgumentException("Parameter: maxLength must be higer or same as sum minimal length of all base.");
		} else if (policy.getMinPasswordLength() < (policy.getMinLowerChar() + policy.getMinUpperChar() + policy.getMinSpecialChar() + policy.getMinNumber())) {
			throw new IllegalArgumentException("Parameter: minLength must be lower or same as sum minimal length of all base.");
		}

		StringBuilder password = new StringBuilder();
		StringBuilder base = new StringBuilder();
		String prohibited = policy.getProhibitedCharacters();
		
		// set bases for generating
		String lowerBase = policy.getLowerCharBase() == null || policy.getLowerCharBase().isEmpty() ? LOWER_CHARACTERS : policy.getLowerCharBase();
		String upperBase = policy.getUpperCharBase() == null || policy.getUpperCharBase().isEmpty() ? UPPER_CHARACTERS : policy.getUpperCharBase();
		String specialBase = policy.getSpecialCharBase() == null || policy.getSpecialCharBase().isEmpty() ? SPECIAL_CHARACTERS : policy.getSpecialCharBase();
		String numberBase = policy.getNumberBase() == null || policy.getNumberBase().isEmpty() ? NUMBERS : policy.getNumberBase();
		
		// generate minimal requirements
		if (policy.getMinLowerChar() != 0) {
			String lower = removeProhibited(lowerBase, prohibited);
			password.append(getRandomChars(lower, policy.getMinLowerChar(), null));
			base.append(lower);
		}
		if (policy.getMinUpperChar() != 0) {
			String upper = removeProhibited(upperBase, prohibited);
			password.append(getRandomChars(upper, policy.getMinUpperChar(), null));
			base.append(upper);
		}
		if (policy.getMinSpecialChar() != 0) {
			String special = removeProhibited(specialBase, prohibited);
			password.append(getRandomChars(special, policy.getMinSpecialChar(), prohibited));
			base.append(special);
		}
		if (policy.getMinNumber() != 0) {
			String number = removeProhibited(numberBase, prohibited);
			password.append(getRandomChars(number, policy.getMinNumber(), prohibited));
			base.append(number);
		}
		
		// add final string to password 
		int missingLength = getRandomNumber(policy.getMinPasswordLength() - password.length(), policy.getMaxPasswordLength() - password.length());
		password.append(getRandomChars(shuffle(base).toString(), missingLength, null));
		
		return shuffle(password).toString();
	}
	
	/**
	 * Generate default password:
	 * 4 characters from lower,
	 * 4 characters from upper,
	 * 4 characters from special,
	 * 4 characters from number
	 * For generating will be use character base defined by this class.
	 * @return
	 */
	public String generateRandom() {
		StringBuilder password = new StringBuilder();
		password.append(getRandomNumbers(4));
		password.append(getRandomSpecialCharacters(4));
		password.append(getRandomLowerCharacters(4));
		password.append(getRandomUpperCharacters(4));
		return shuffle(password).toString();
	}
	
	/**
	 * Generate password by given parameters.
	 * If you want to skip some category set parameters for category to null.
	 * For generating will be use user defined character base.
	 * 
	 * @param minLength minimal length for password
	 * @param maxLength maximal length for password
	 * @param lowerMin minimal characters from lower category
	 * @param lowerBase lower character that will be use for generating
	 * @param upperMin minimal characters from upper category
	 * @param upperBase upper character that will be use for generating
	 * @param specialMin minimal characters from special category
	 * @param specialBase special character that will be use for generating
	 * @param numberMin minimal characters from number category
	 * @param numberBase numbers that will be use for generating
	 * @param prohibited
	 * @return new password
	 */
	public String generateRandom(
			Integer minLength,Integer maxLength,
			Integer lowerMin, String lowerBase,
			Integer upperMin, String upperBase,
			Integer specialMin, String specialBase,
			Integer numberMin, String numberBase, String prohibited) {
		
		return this.generateRandom(new PasswordGenerate() {
			
			@Override
			public String getUpperCharBase() {
				return upperBase;
			}
			
			@Override
			public String getSpecialCharBase() {
				return specialBase;
			}
			
			@Override
			public String getProhibitedCharacters() {
				return prohibited;
			}
			
			@Override
			public int getPassphraseWords() {
				return 0;
			}
			
			@Override
			public String getNumberBase() {
				return numberBase;
			}
			
			@Override
			public int getMinUpperChar() {
				return getValue(upperMin);
			}
			
			@Override
			public int getMinSpecialChar() {
				return getValue(specialMin);
			}
			
			@Override
			public int getMinPasswordLength() {
				return getValue(minLength);
			}
			
			@Override
			public int getMinNumber() {
				return getValue(numberMin);
			}
			
			@Override
			public int getMinLowerChar() {
				return getValue(lowerMin);
			}
			
			@Override
			public int getMaxPasswordLength() {
				return getValue(maxLength);
			}
			
			@Override
			public String getLowerCharBase() {
				return lowerBase;
			}
		});
	}
	
	/**
	 * Generate password by given parameters.
	 * If you want to skip some category set parameters for category to null.
	 * For generating will be use default characters defined by this class.
	 * 
	 * @param minLength minimal length for password
	 * @param maxLength maximal length for password
	 * @param lowerMin minimal characters from lower category
	 * @param upperMin minimal characters from upper category
	 * @param specialMin minimal characters from special category
	 * @param numberMin minimal characters from number category
	 * @param prohibited string with character/s which will be skipped
	 * @return new password
	 */
	public String generateRandom(
			Integer minLength,Integer maxLength,
			Integer lowerMin, Integer upperMin,
			Integer specialMin,	Integer numberMin,
			String prohibited) {
		return generateRandom(
				minLength, maxLength, lowerMin,
				LOWER_CHARACTERS, upperMin,
				UPPER_CHARACTERS, specialMin,
				SPECIAL_CHARACTERS, numberMin,
				NUMBERS, prohibited);
	}
	
	/**
	 * Generate password by given parameters.
	 * If you want to skip some category set parameters for category to null.
	 * For generating will be use default characters defined by this class.
	 * 
	 * @param minLength minimal length for password
	 * @param maxLength maximal length for password
	 * @param lowerMin minimal characters from lower category
	 * @param upperMin minimal characters from upper category
	 * @param specialMin minimal characters from special category
	 * @param numberMin minimal characters from number category
	 * @return new password
	 */
	public String generateRandom(
			Integer minLength, Integer maxLength,
			Integer lowerMin, Integer upperMin,
			Integer specialMin, Integer numberMin) {
		return generateRandom(
				minLength, maxLength, lowerMin,
				upperMin, specialMin, numberMin, null);
	}
	
	private Integer getValue(Integer integer) {
		return integer != null ? integer : 0;
	}
	
	private StringBuilder shuffle(StringBuilder string) {
		List<Character> list = new ArrayList<Character>();
        for(char character : string.toString().toCharArray()){
        	list.add(character);
        }
        
        StringBuilder result = new StringBuilder(string.length());
        
        while(list.size() != 0){
            result.append(list.remove(getRandomNumber(0, list.size() - 1)));
        }
        
        return result;
	}
	
	private StringBuilder getRandomNumbers(int max) {
		StringBuilder password = new StringBuilder();
		for (int index = 0; index < max; index++) {
			password.append(getRandomNumber(0, 9));
		}
		return password;
	}
	
	private StringBuilder getRandomSpecialCharacters(int max) {
		StringBuilder password = new StringBuilder();
		for (int index = 0; index < max; index++) {
			password.append(getRandomSpecialCharacter());
		}
		return password;
	}
	
	private StringBuilder getRandomUpperCharacters(int max) {
		StringBuilder password = new StringBuilder();
		for (int index = 0; index < max; index++) {
			password.append(getRandomUpperCharacter());
		}
		return password;
	}
	
	
	private StringBuilder getRandomLowerCharacters(int max) {
		StringBuilder password = new StringBuilder();
		for (int index = 0; index < max; index++) {
			password.append(getRandomLowerCharacter());
		}
		return password;
	}
	
	private char getRandomSpecialCharacter() {
		return getRandomChar(SPECIAL_CHARACTERS);
	}
	
	private char getRandomUpperCharacter() {
		return getRandomChar(UPPER_CHARACTERS);
	}
	
	private char getRandomLowerCharacter() {
		return getRandomChar(LOWER_CHARACTERS);
	}
	
	private char getRandomChar(String string) {
		return string.charAt(getRandomNumber(0, string.length() - 1));
	}
	
	private StringBuilder getRandomChars(String string, int length, String prohibited) {
		StringBuilder result = new StringBuilder();
		string = removeProhibited(string, prohibited);
		
		for (int index = 0; index < length; index++) {
			result.append(getRandomChar(string));
		}
		return result;
	}
	
	private String removeProhibited(String string, String prohibited) {
		if (prohibited != null) {
			 for (Character character : prohibited.toCharArray()) {
				 string = string.replaceAll(character.toString(), "");
			 }
			 string.trim();
		}
		return string;
	}
	
	private void notNull(Object object, String message) {
		if (object == null) {
			throw new IllegalArgumentException(message);
		}
	}
	
	private int getRandomNumber(int min, int max) {
		// Better random generator?
		Random rand = new Random();
		return rand.nextInt((max - min) + 1) + min;
	}
	
	private void loadDicewareDictonary(String localization) {
		if (this.filePosition == null) {
			this.filePosition = new HashMap<>();
		}
		
		String fileName = exportResource("diceware_" + localization + "." + DEFAULT_FILE_TYPE, PATH);
		
		String line = null;
		try {
			
			
	        File file = new File(fileName);
			RandomAccessFile randFile = new RandomAccessFile(file, "r");
			
			long offset = randFile.getFilePointer();
			while((line = randFile.readLine()) != null) {
				String[] splitLine = line.split(" ", 2);
				filePosition.put(Integer.parseInt(splitLine[0]), offset);
				offset = randFile.getFilePointer();
			}
			
			randFile.close();
		} catch (IOException e) {
			throw new IllegalArgumentException("[diceware] " + e.getMessage());
		}
	}
	
	private String exportResource(String resourceFileName, String path) {
		InputStream is = getClass().getClassLoader().getResourceAsStream(path + "/" + resourceFileName);
        String newFile = System.getProperty("java.io.tmpdir") + "/tmp_idm_" + resourceFileName;
        try {
        	byte[] buffer = new byte[4096];
        	int readBytes;
        	
			OutputStream outputStream = new FileOutputStream(newFile);
			while ((readBytes = is.read(buffer)) > 0) {
				outputStream.write(buffer, 0, readBytes);
            }
			outputStream.close();
		} catch (IOException e) {
			throw new IllegalArgumentException("[diceware] " + e.getMessage());
		}
        return newFile;
	}
	
	private String getWordFromDictonary(int rolls, String localization) {
		if (this.filePosition == null || this.filePosition.isEmpty()) {
			this.loadDicewareDictonary(DEFAULT_DICEWARE);
		}
		
		String word;
		
		try {
			String fileName = System.getProperty("java.io.tmpdir") + "/tmp_idm_" + "diceware_" + localization + "." + DEFAULT_FILE_TYPE;
			RandomAccessFile randFile = new RandomAccessFile(new File(fileName), "r");

			randFile.seek(this.filePosition.get(rolls));
			
			String[] line = randFile.readLine().split(" ", 2);;
			word = line[1];
			
			randFile.close();
		} catch (IOException e) {
			throw new IllegalArgumentException("[diceware] " + e.getMessage());
		}

		return word;
	}
}
