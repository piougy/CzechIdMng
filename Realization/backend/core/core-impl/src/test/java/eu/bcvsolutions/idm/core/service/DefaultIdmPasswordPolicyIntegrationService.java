package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.*;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.model.domain.IdmPasswordPolicyGenerateType;
import eu.bcvsolutions.idm.core.model.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Basic test for validation and generate password by IdmPasswordPolicyService
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class DefaultIdmPasswordPolicyIntegrationService extends AbstractIntegrationTest {
	
	private static final int ATTEMPTS = 20;
	
	@Autowired
	private IdmPasswordPolicyService passwordPolicyService;
	
	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}
	
	@After 
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testGenerateRandomPasswordLength() {
		IdmPasswordPolicy policy = new IdmPasswordPolicy();
		policy.setName("test_01");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMinPasswordLength(5);
		policy.setMaxPasswordLength(12);
		
		for (int index = 0; index < ATTEMPTS; index++) {
			String password = passwordPolicyService.generatePassword(policy);
			assertTrue(password.length() >= 5);
			assertTrue(password.length() <= 12);
		}
	}
	
	@Test
	public void testGeneratePasshrase() {
		IdmPasswordPolicy policy = new IdmPasswordPolicy();
		policy.setName("test_02");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.PASSPHRASE);
		policy.setPassphraseWords(5);
		
		for (int index = 0; index < ATTEMPTS; index++) {
			String password = passwordPolicyService.generatePassword(policy);
			assertEquals(5, password.split(" ").length);
		}
	}
	
	@Test
	public void testFailGenerateRandom() {
		IdmPasswordPolicy policy = new IdmPasswordPolicy();
		policy.setName("test_03");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(5);
		
		String password = passwordPolicyService.generatePassword(policy);
		assertTrue(password.length() <= 5);
		
		policy.setMinNumber(2);
		policy.setMinSpecialChar(2);
		policy.setMinLowerChar(2);
		try {
			password = passwordPolicyService.generatePassword(policy);
			fail("Password cant be generate");
		} catch (Exception e) {
			// nothing
		}
		
		policy.setMinLowerChar(1);
		password = passwordPolicyService.generatePassword(policy);
		assertEquals(5, password.length());
	}
	
	@Test
	public void testOnlyMinimalLength() {
		// maximal password length must be always set!!
		IdmPasswordPolicy policy = new IdmPasswordPolicy();
		policy.setName("test_04");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMinPasswordLength(20);
		policy.setMaxPasswordLength(25);
		
		for (int index = 0; index < ATTEMPTS; index++) {
			String password = passwordPolicyService.generatePassword(policy);
			assertTrue(password.length() >= 20);
		}
	}
	
	@Test
	public void testOnlyMaximalLength() {
		IdmPasswordPolicy policy = new IdmPasswordPolicy();
		policy.setName("test_05");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(20);
		
		for (int index = 0; index < ATTEMPTS; index++) {
			String password = passwordPolicyService.generatePassword(policy);
			assertTrue(password.length() <= 20);
		}
	}
	
	@Test
	public void testGenerateOnlyNumbers() {
		IdmPasswordPolicy policy = new IdmPasswordPolicy();
		policy.setName("test_06");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(10);
		policy.setMinPasswordLength(1);
		policy.setLowerCharBase("");
		policy.setSpecialCharBase("");
		policy.setNumberBase("0123456789");
		policy.setUpperCharBase("");
		
		for (int index = 0; index < ATTEMPTS; index++) {
			try {
				String password = passwordPolicyService.generatePassword(policy);
				Long.parseLong(password);
			} catch (Exception e) {
				fail("Password must cointains only numbers: " + e.getMessage());
			}
		}
	}
	
	@Test
	public void testGenerateOnlyAlpha() {
		IdmPasswordPolicy policy = new IdmPasswordPolicy();
		policy.setName("test_07");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(10);
		policy.setMinPasswordLength(1);
		policy.setSpecialCharBase("");
		policy.setNumberBase("");
		
		for (int index = 0; index < ATTEMPTS; index++) {
			String password = passwordPolicyService.generatePassword(policy);
			if (!password.matches("[a-zA-Z]+")) {
				fail("Password must cointain only aplha characters, password: " + password);
			}
			
		}
	}
	
	@Test
	public void testGenerateOnlyOneSpecial() {
		IdmPasswordPolicy policy = new IdmPasswordPolicy();
		policy.setName("test_08");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(1);
		policy.setMinPasswordLength(1);
		policy.setSpecialCharBase("@");
		policy.setNumberBase("");
		policy.setLowerCharBase("");
		policy.setUpperCharBase("");
		
		for (int index = 0; index < ATTEMPTS; index++) {
			String password = passwordPolicyService.generatePassword(policy);
			if (!password.equals("@")) {
				fail("Password must cointain only @ character, password: " + password);
			}
			
		}
	}
	
	@Test
	public void testGenerateComplexPassword() {
		IdmPasswordPolicy policy = new IdmPasswordPolicy();
		policy.setName("test_09");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(10);
		policy.setMinPasswordLength(5);
		policy.setSpecialCharBase("!");
		policy.setNumberBase("123");
		
		policy.setMinNumber(3);
		policy.setMinLowerChar(2);
		policy.setMinSpecialChar(2);
		policy.setMinUpperChar(1);
		
		for (int index = 0; index < ATTEMPTS * 5; index++) {
			String password = passwordPolicyService.generatePassword(policy);
			
			assertTrue(password.length() >= 8);
			
			assertTrue(StringUtils.countMatches(password, "!") >= 2);
			
			assertTrue(StringUtils.containsNone(password, "@#$%^&*()"));
			
			assertTrue(StringUtils.containsNone(password, "0456789"));
			
			assertTrue(password.matches(".*[A-Z].*{1,}"));
			
			assertTrue(password.matches(".*[a-z].*{2,}"));
		}
	}
	
	@Test
	public void testGenerateProhibited() {
		IdmPasswordPolicy policy = new IdmPasswordPolicy();
		policy.setName("test_10");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(2);
		policy.setMinPasswordLength(2);
		policy.setSpecialCharBase("");
		policy.setLowerCharBase("a");
		policy.setUpperCharBase("");
		policy.setNumberBase("123");
		policy.setProhibitedCharacters("asd2!@#%3$");
		
		for (int index = 0; index < ATTEMPTS; index++) {
			String password = passwordPolicyService.generatePassword(policy);
			
			assertTrue(password.length() == 2);
			
			assertTrue(StringUtils.countMatches(password, "1") == 2);
			
			assertTrue(StringUtils.containsNone(password, "asd2!@#%3$"));
		}
	}
	
	@Test
	public void testValidateLength() {
		IdmPasswordPolicy policy = new IdmPasswordPolicy();
		policy.setName("test_11");
		policy.setType(IdmPasswordPolicyType.VALIDATE);
		policy.setMaxPasswordLength(10);
		policy.setMinPasswordLength(5);
		
		try {
			this.passwordPolicyService.validate("12345", policy);
			this.passwordPolicyService.validate("123456", policy);
			this.passwordPolicyService.validate("1234567", policy);
			this.passwordPolicyService.validate("12345678", policy);
			this.passwordPolicyService.validate("123456789", policy);
			this.passwordPolicyService.validate("1234567890", policy);
		} catch (Exception e) {
			fail("Password validation length.");
		}
		
		
		try {
			this.passwordPolicyService.validate("1234", policy);
			fail("Password validation length.");
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			this.passwordPolicyService.validate("", policy);
			fail("Password validation length.");
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			this.passwordPolicyService.validate("123456789123", policy);
			fail("Password validation length.");
		} catch (Exception e) {
			// nothing, success
		}
	}
	
	@Test
	public void testValidateMinNumbers() {
		IdmPasswordPolicy policy = new IdmPasswordPolicy();
		policy.setName("test_12");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(3);
		policy.setMinPasswordLength(1);
		policy.setMinNumber(2);
		
		try {
			this.passwordPolicyService.validate("123", policy);
			this.passwordPolicyService.validate("12", policy);
			this.passwordPolicyService.validate("12a", policy);
		} catch (Exception e) {
			fail("Password validation numbers. " + e.getMessage());
		}
		
		try {
			this.passwordPolicyService.validate("1", policy);
			fail("Password validation numbers. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			this.passwordPolicyService.validate("1234", policy);
			fail("Password validation numbers. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			this.passwordPolicyService.validate("test", policy);
			fail("Password validation numbers. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
	}
	
	@Test
	public void testValidateSpecialChar() {
		IdmPasswordPolicy policy = new IdmPasswordPolicy();
		policy.setName("test_13");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(3);
		policy.setMinPasswordLength(1);
		policy.setMinSpecialChar(2);
		
		try {
			this.passwordPolicyService.validate("!@", policy);
			this.passwordPolicyService.validate("!@#", policy);
			this.passwordPolicyService.validate("!@a", policy);
		} catch (Exception e) {
			fail("Password validation special chars. " + e.getMessage());
		}
		
		try {
			this.passwordPolicyService.validate("!", policy);
			fail("Password validation special chars. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			this.passwordPolicyService.validate("!@#$", policy);
			fail("Password validation special chars. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			this.passwordPolicyService.validate("test", policy);
			fail("Password validation special chars. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
	}
	
	@Test
	public void testValidationProhibitedChars() {
		IdmPasswordPolicy policy = new IdmPasswordPolicy();
		policy.setName("test_14");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(5);
		policy.setMinPasswordLength(1);
		policy.setProhibitedCharacters("12abcDEF!@");
		
		try {
			this.passwordPolicyService.validate("test", policy);
			this.passwordPolicyService.validate("ABde", policy);
		} catch (Exception e) {
			fail("Password validate prohibited characters. " + policy);
		}
		
		try {
			this.passwordPolicyService.validate("tEst", policy);
			fail("Password validate prohibited characters. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			this.passwordPolicyService.validate("eddD", policy);
			fail("Password validate prohibited characters. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			this.passwordPolicyService.validate("5416", policy);
			fail("Password validate prohibited characters. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			this.passwordPolicyService.validate("test!", policy);
			fail("Password validate prohibited characters. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
	}
	
	@Test
	public void testValidateBase() {
		IdmPasswordPolicy policy = new IdmPasswordPolicy();
		policy.setName("test_15");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(5);
		policy.setMinPasswordLength(1);
		policy.setNumberBase("123");
		policy.setMinNumber(3);
		
		try {
			this.passwordPolicyService.validate("123", policy);
			this.passwordPolicyService.validate("1234", policy);
			this.passwordPolicyService.validate("111", policy);
		} catch (Exception e) {
			fail("Password base validation. " + policy);
		}
		
		try {
			this.passwordPolicyService.validate("124", policy);
			fail("Password base validation. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			this.passwordPolicyService.validate("456", policy);
			fail("Password base validation. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
	}
	
	@Test
	public void testValidateComplex() {
		IdmPasswordPolicy policy = new IdmPasswordPolicy();
		policy.setName("test_16");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(20);
		policy.setMinPasswordLength(6);
		
		policy.setMinNumber(3);
		policy.setMinLowerChar(3);
		policy.setMinSpecialChar(3);
		policy.setMinUpperChar(3);
		
		policy.setSpecialCharBase("@#");
		policy.setNumberBase("0");
		
		policy.setProhibitedCharacters("*/^mn");
		
		try {
			this.passwordPolicyService.validate("000abc@@@DEF", policy);
			this.passwordPolicyService.validate("F0a@0Ec0b@@D", policy);
			this.passwordPolicyService.validate("#3aBb@C3A1#0c00", policy);
		} catch (Exception e) {
			fail("Password complex validation. " + policy);
		}
		
		try {
			this.passwordPolicyService.validate("001abc@@@DEF", policy);
			fail("Password complex validation. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			this.passwordPolicyService.validate("000abc##$DEF", policy);
			fail("Password complex validation. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			this.passwordPolicyService.validate("000abc)()DEF", policy);
			fail("Password complex validation. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			this.passwordPolicyService.validate("#3aBb@C3A1#0c00idheff", policy);
			fail("Password complex validation. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			this.passwordPolicyService.validate("#3aBmb@C3A1#0c00", policy);
			fail("Password complex validation. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			this.passwordPolicyService.validate("#3aBb@C3A1n#0c00", policy);
			fail("Password complex validation. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			this.passwordPolicyService.validate("#3mBb*@C3A1n#0c00", policy);
			fail("Password complex validation. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
	}
}
