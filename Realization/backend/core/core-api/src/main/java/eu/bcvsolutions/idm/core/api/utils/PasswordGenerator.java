package eu.bcvsolutions.idm.core.api.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.identityconnectors.common.StringUtil;
import org.springframework.util.Assert;

import com.google.common.base.Strings;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.PasswordGenerate;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

/**
 * Password generator
 * 
 * @author Ondrej Kopr
 * @author Ondrej Husnik
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
	
	private static final int DEFAULT_PASSWORD_LENGTH = 12;

	private Map<Integer, Long> filePosition;
	
	/**
	 * Local container of character bases  
	 */
	private class CharBaseCont {
		public String base;
		public Set<Character> baseSet;
		// min necessary chars
		public Integer minCount = new Integer(0);
		// currently allocated chars
		public Integer allocCount = new Integer(0);
	}
	
	public String generatePassphrase(PasswordGenerate policy) {
		Assert.notNull(policy, "Password policy can't be null.");
		
		int passphraseLength = policy.getPassphraseWords() == null ? 0 : policy.getPassphraseWords().intValue();
		List<String> password = new ArrayList<>(passphraseLength);
		
		for (int index = 0; index < passphraseLength; index++) {
			
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
		Assert.notNull(policy, "Password policy can't be null.");		
		// policy may contains null values
		int minimumLength = intNullToZero(policy.getMinPasswordLength());

		int minimumLengthAll = intNullToZero(policy.getMinLowerChar()); 
		minimumLengthAll += intNullToZero(policy.getMinUpperChar());
		minimumLengthAll += intNullToZero(policy.getMinSpecialChar());
		minimumLengthAll += intNullToZero(policy.getMinNumber());

		// defensive set of max length
		int maximumLength = 0;
		if (policy.getMaxPasswordLength() != null) {
			// if is defined maximum password length in policy use it
			maximumLength = policy.getMaxPasswordLength().intValue();
		} else {
			// set higher value of minimal lengths
			maximumLength = minimumLength > minimumLengthAll ? minimumLength : minimumLengthAll;
		}
		
		// if maximum and minimum length is both zero use default password length (defensive behavior)
		if (maximumLength == 0 && minimumLength == 0) {
			maximumLength = DEFAULT_PASSWORD_LENGTH;
			minimumLength = DEFAULT_PASSWORD_LENGTH;
		}
		
		if (maximumLength < minimumLength) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_INVALID_SETTING,
					"Parameter: maxLength can't be lower than parameter: minLength");
		}
		
		
		if (maximumLength < minimumLengthAll) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_INVALID_SETTING,
					"Parameter: maxLength must be higer or same as sum minimal length of all base.");
		}

		// Start of password generating
		int generatedPassLen = 0;
		StringBuilder password = new StringBuilder();
		List<CharBaseCont> baseList	= buildBaseContainerList(policy);
		
		// ********** generate begin/end character if necessary *****************
		String beginChar = null;
		String endChar = null;
		String beginProhibited = Strings.emptyToNull(policy.getProhibitedBeginCharacters());
		String endProhibited = Strings.emptyToNull(policy.getProhibitedEndCharacters());
		boolean limitBeginChar = StringUtils.isEmpty(policy.getPrefix()) && !StringUtils.isEmpty(beginProhibited);
		boolean limitEndChar = StringUtils.isEmpty(policy.getSuffix()) && !StringUtils.isEmpty(endProhibited);
		Set<Character> beginCandidates = null;
		Set<Character> endCandidates = null;
		int limitedCharsVsTotalLen = maximumLength 
				- baseList.stream().map((cont) -> cont.minCount).reduce(0, (sum, count) -> sum + count)
				- (limitBeginChar ? 1 : 0) - (limitEndChar ? 1 : 0);
		
		// very rare case when password has 1 character but possible
		// then begin/and char is the same -> beginCandidates will be used for both
		if (maximumLength < 2 && limitBeginChar && limitEndChar) {
			beginCandidates = aggregatedBaseSet(baseList, true);
			beginCandidates.removeAll(fromStringToCharSet(beginProhibited));
			beginCandidates.removeAll(fromStringToCharSet(endProhibited));
			beginChar = getMarginCharWithAllocUpdate(beginCandidates, baseList).toString();
			generatedPassLen++;
		} else {
			// we have to count marginal characters to overall required
			// count of characters from individual bases if following condition is true   
			boolean basesWithRequiredCharsOnly = limitedCharsVsTotalLen < 0;		
			if(limitBeginChar) {
				beginCandidates = aggregatedBaseSet(baseList, basesWithRequiredCharsOnly);
				beginCandidates.removeAll(fromStringToCharSet(beginProhibited));
				beginChar = getMarginCharWithAllocUpdate(beginCandidates, baseList).toString();
				generatedPassLen++;
			}
			if(limitEndChar) {
				endCandidates = aggregatedBaseSet(baseList, basesWithRequiredCharsOnly);
				endCandidates.removeAll(fromStringToCharSet(endProhibited));
				endChar = getMarginCharWithAllocUpdate(endCandidates, baseList).toString();
				generatedPassLen++;
			}
		}
		
		// ********* generate up to minimum number of mandatory characters from individual bases **********
		for (CharBaseCont cont : baseList) {
			int neededNum = cont.minCount - cont.allocCount;
			if (neededNum > 0) {
				password.append(getRandomChars(cont.base, neededNum, null));
			}
		}
		generatedPassLen += password.length();
		
		// ********** generate rest of remaining characters up to password length	
		// generated missing number of characters 
		int missingLength = getRandomNumber(minimumLength - generatedPassLen, maximumLength - generatedPassLen);
		if (missingLength > 0) {
			Set<Character> generalCandidates = aggregatedBaseSet(baseList, false);
			StringBuilder base = new StringBuilder(fromCharSetToString(generalCandidates));
			base = shuffle(base);
			password.append(getRandomChars(base.toString(), missingLength, null));
		}
		// final password completion
		password = shuffle(password);
		password.insert(0, Strings.nullToEmpty(beginChar));
		password.append(Strings.nullToEmpty(endChar));
		return password.toString();
	}
	
	/**
	 * Test generated password whether it meets requirements
	 * 
	 * @param password
	 * @param policy
	 * @return
	 */
	public boolean testPasswordAgainstPolicy(String password, PasswordGenerate policy) {
		if (Strings.isNullOrEmpty(password)) {
			return false;
		}

		int passLen = password.length();
		if (passLen < intNullToZero(policy.getMinPasswordLength())
				|| passLen > intNullToMax(policy.getMaxPasswordLength())) {
			return false;
		}
		if (leaveAllowed(password, policy.getLowerCharBase()).length() < intNullToZero(policy.getMinLowerChar())) {
			return false;
		}
		if (leaveAllowed(password, policy.getUpperCharBase()).length() < intNullToZero(policy.getMinUpperChar())) {
			return false;
		}
		if (leaveAllowed(password, policy.getNumberBase()).length() < intNullToZero(policy.getMinNumber())) {
			return false;
		}
		if (leaveAllowed(password, policy.getSpecialCharBase()).length() < intNullToZero(policy.getMinSpecialChar())) {
			return false;
		}
		// contains forbidden characters
		if (!Strings.isNullOrEmpty(leaveAllowed(password, Strings.nullToEmpty(policy.getProhibitedCharacters())))) {
			return false;
		}
		// must not start with
		if (Strings.isNullOrEmpty(policy.getPrefix()) && Strings.nullToEmpty(policy.getProhibitedBeginCharacters())
				.contains(String.valueOf(password.charAt(0)))) {
			return false;
		}
		// must not end with
		if (Strings.isNullOrEmpty(policy.getSuffix()) && Strings.nullToEmpty(policy.getProhibitedEndCharacters())
				.contains(String.valueOf(password.charAt(password.length() - 1)))) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param policy
	 * @param testCycleNum
	 * @return
	 */
	public boolean testPolicySetting(PasswordGenerate policy, int testCycleNum) {
		for (int i = 0; i < testCycleNum; ++i) {
			String pass = generateRandom(policy);
			boolean result = testPasswordAgainstPolicy(pass, policy);
			if (!result) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Generate default password:
	 * 5 characters from lower,
	 * 1 characters from upper,
	 * 1 characters from special,
	 * 1 characters from number
	 * For generating will be use character base defined by this class.
     *
	 * @return
	 */
	public String generateRandom() {
		StringBuilder password = new StringBuilder();
		password.append(getRandomNumbers(1));
		password.append(getRandomSpecialCharacters(1));
		password.append(getRandomLowerCharacters(5));
		password.append(getRandomUpperCharacters(1));
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
			public Integer getPassphraseWords() {
				return 0;
			}
			
			@Override
			public String getNumberBase() {
				return numberBase;
			}
			
			@Override
			public Integer getMinUpperChar() {
				return upperMin;
			}
			
			@Override
			public Integer getMinSpecialChar() {
				return specialMin;
			}
			
			@Override
			public Integer getMinPasswordLength() {
				return minLength;
			}
			
			@Override
			public Integer getMinNumber() {
				return numberMin;
			}
			
			@Override
			public Integer getMinLowerChar() {
				return lowerMin;
			}
			
			@Override
			public Integer getMaxPasswordLength() {
				return maxLength;
			}
			
			@Override
			public String getLowerCharBase() {
				return lowerBase;
			}
			
			@Override
			public String getPrefix() {
				return "";
			}
			
			@Override
			public String getSuffix() {
				return "";
			}
			
			@Override
			public String getProhibitedBeginCharacters() {
				return "";
			}
			
			@Override
			public String getProhibitedEndCharacters() {
				return "";
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
	
	private StringBuilder shuffle(StringBuilder string) {
		char[] charArray = string.toString().toCharArray();
		List<Character> list = new ArrayList<>(charArray.length);
        for (char character : charArray){
        	list.add(character);
        }
        
        StringBuilder result = new StringBuilder(string.length());
        
        while(!list.isEmpty()){
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
	
	private StringBuilder getRandomChars(String base, int length, String prohibited) {
		StringBuilder result = new StringBuilder();
		// if base is empty return empty result 
		if (StringUtil.isEmpty(base)) {
			return result;
		}
		if (StringUtil.isNotEmpty(prohibited)) {
			base = removeProhibited(base, prohibited);
		}
		if (StringUtil.isEmpty(base)) {
			return result;
		}
		for (int index = 0; index < length; index++) {
			result.append(getRandomChar(base));
		}
		return result;
	}
	
	private String removeProhibited(String string, String prohibited) {
		if (prohibited != null) {
			 for (Character character : prohibited.toCharArray()) {
				 string = string.replaceAll(Pattern.quote(character.toString()), "");
			 }
			 string = string.trim();
		}
		return string;
	}
	
	private String leaveAllowed(String string, String allowed) {
		if (Strings.isNullOrEmpty(string)) {
			return Strings.nullToEmpty(string);
		}
		StringBuilder result = new StringBuilder();
		for (Character character : string.toCharArray()) {
			if (allowed.contains(character.toString())) {
				result.append(character);
			}
		}
		return result.toString();
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
				filePosition.put(Integer.valueOf(splitLine[0]), offset);
				offset = randFile.getFilePointer();
			}
			
			randFile.close();
		} catch (IOException e) {
			throw new IllegalArgumentException("Diceware IO exception, see log.", e);
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
			throw new IllegalArgumentException("Diceware IO exception, see log.", e);
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
			
            String[] line = randFile.readLine().split(" ", 2);
            ;
			word = line[1];
			
			randFile.close();
		} catch (IOException e) {
			throw new IllegalArgumentException("Diceware IO exception, see log.", e);
		}

		return word;
	}
	
	/**
	 * Generates marginal character from defined set of characters and increments
	 * allocCount in corresponding baseCont which is generated character from. If
	 * {@code srcBaseSet} is empty - means no allowed chars available to be used at
	 * the begin/end - throws invalid policy exception.
	 * 
	 * @param srcBaseSet
	 * @param baseListToUpdate
	 * @return
	 */
	private Character getMarginCharWithAllocUpdate (Set<Character> srcBaseSet, List<CharBaseCont> baseListToUpdate) throws ResultCodeException {
		if(srcBaseSet.isEmpty()) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_INVALID_SETTING);
		}
		StringBuilder sb = new StringBuilder(fromCharSetToString(srcBaseSet));
		sb = shuffle(sb);
		Character ch = getRandomChar(sb.toString());
		CharBaseCont cont = findBaseContWithChar(ch, baseListToUpdate);
		cont.allocCount++;
		return ch;
	}
	
	
	/**
	 * Returns baseCont containing {@code character}
	 * 
	 * @param charac
	 * @param baseList
	 * @return
	 */
	private CharBaseCont findBaseContWithChar(Character character, List<CharBaseCont> baseList) {
		for (CharBaseCont baseCont : baseList) {
			if (baseCont.baseSet.contains(character)) {
				return baseCont;
			}
		}
		return null;
	}
	

	/**
	 * Generates list of containers with character bases Container carries
	 * additional information like minimal or already generated char counts from
	 * individual bases
	 * 
	 * @param policy
	 * @return
	 */
	private List<CharBaseCont> buildBaseContainerList(PasswordGenerate policy) {
		String prohibited = policy.getProhibitedCharacters();
		List<CharBaseCont> baseList = new ArrayList<>(4);
		CharBaseCont lowerCont = new CharBaseCont();
		lowerCont.base = removeProhibited(policy.getLowerCharBase(), prohibited);
		lowerCont.baseSet = fromStringToCharSet(lowerCont.base);
		lowerCont.minCount = intNullToZero(policy.getMinLowerChar());
		baseList.add(lowerCont);
		CharBaseCont upperCont = new CharBaseCont();
		upperCont.base = removeProhibited(policy.getUpperCharBase(), prohibited);
		upperCont.baseSet = fromStringToCharSet(upperCont.base);
		upperCont.minCount = intNullToZero(policy.getMinUpperChar());
		baseList.add(upperCont);
		CharBaseCont specialCont = new CharBaseCont();
		specialCont.base = removeProhibited(policy.getSpecialCharBase(), prohibited);
		specialCont.baseSet = fromStringToCharSet(specialCont.base);
		specialCont.minCount = intNullToZero(policy.getMinSpecialChar());
		baseList.add(specialCont);
		CharBaseCont numberCont = new CharBaseCont();
		numberCont.base = removeProhibited(policy.getNumberBase(), prohibited);
		numberCont.baseSet = fromStringToCharSet(numberCont.base);
		numberCont.minCount = intNullToZero(policy.getMinNumber());
		baseList.add(numberCont);
		return baseList;
	}
	
	/**
	 * Creates aggregated set of characters from bases. If
	 * {@code mandatoryCharsOnly} is true, limit to bases, characters of which are
	 * mandatory in the password. Otherwise all bases. If there are no mandatory
	 * characters, return all bases anyway
	 * 
	 * @param baseList
	 * @param mandatoryCharsOnly
	 * @return
	 */
	private Set<Character> aggregatedBaseSet(List<CharBaseCont> baseList, boolean mandatoryCharsOnly) {
		Set<Character> result = new HashSet<Character>();
		for (CharBaseCont baseCont : baseList) {
			if (mandatoryCharsOnly && ((baseCont.minCount - baseCont.allocCount) <= 0)) {
				continue;
			}
			result.addAll(baseCont.baseSet);
		}
		// true if no mandatory characters found
		if (result.isEmpty()) {
			for (CharBaseCont baseCont : baseList) {
				result.addAll(baseCont.baseSet);
			}
		}
		return result;
	}
	
	/**
	 * Transform String of chars to Set
	 * 
	 * @param chars
	 * @return
	 */
	private Set<Character> fromStringToCharSet(String chars) {
		Set<Character> result = new HashSet<Character>();
		for (Character ch : chars.toCharArray()) {
			result.add(ch);
		}
		return result;
	}
	
	/**
	 * Transform a Set of chars to String
	 * 
	 * @param charSet
	 * @return
	 */
	private String fromCharSetToString(Set<Character> charSet) {
		return charSet.stream().map(String::valueOf).collect(Collectors.joining());
	}
	
	
	/**
	 * Null to zero value
	 * 
	 * @param val
	 * @return
	 */
	private Integer intNullToZero(Integer val) {
		return val == null ? 0 : val;
	}
	
	/**
	 * Null to max value
	 * 
	 * @param val
	 * @return
	 */
	private Integer intNullToMax(Integer val) {
		return val == null ? Integer.MAX_VALUE : val;
	}
}
