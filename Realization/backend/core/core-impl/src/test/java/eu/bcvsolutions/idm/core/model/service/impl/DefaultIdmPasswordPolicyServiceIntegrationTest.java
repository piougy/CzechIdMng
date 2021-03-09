package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.testng.collections.Lists;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyGenerateType;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyIdentityAttributes;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.exception.ErrorModel;
import eu.bcvsolutions.idm.core.api.exception.PasswordChangeException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.api.utils.PasswordGenerator;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Basic test for validation and generate password by IdmPasswordPolicyService.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@Transactional
public class DefaultIdmPasswordPolicyServiceIntegrationTest extends AbstractIntegrationTest {
	
	private static final int ATTEMPTS = 20;
	
	@Autowired private ApplicationContext context;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmPasswordService passwordService;
	//
	private DefaultIdmPasswordPolicyService passwordPolicyService;
	
	@Before
	public void before() {
		passwordPolicyService = context.getAutowireCapableBeanFactory().createBean(DefaultIdmPasswordPolicyService.class);
	}
	
	@Test
	public void testGenerateRandomPasswordLength() {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
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
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
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
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
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
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
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
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
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
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
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
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
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
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
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
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
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
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_10");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(2);
		policy.setMinPasswordLength(2);
		policy.setSpecialCharBase("");
		policy.setLowerCharBase("a");
		policy.setUpperCharBase("");
		policy.setNumberBase("123");
		policy.setProhibitedCharacters("a+-^sd2!@#%3$");
		
		for (int index = 0; index < ATTEMPTS; index++) {
			String password = passwordPolicyService.generatePassword(policy);
			
			assertTrue(password.length() == 2);
			
			assertTrue(StringUtils.countMatches(password, "1") == 2);
			
			assertTrue(StringUtils.containsNone(password, "a+-^sd2!@#%3$"));
		}
	}
	
	@Test
	public void testGenerateWithBeginForbidden() {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_10_1");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(5);
		policy.setMinPasswordLength(5);
		policy.setSpecialCharBase("+");
		policy.setLowerCharBase("a");
		policy.setUpperCharBase("A");
		policy.setNumberBase("1");
		policy.setProhibitedBeginCharacters("1Aa");

		for (int index = 0; index < ATTEMPTS; index++) {
			String password = passwordPolicyService.generatePassword(policy);
			assertTrue(password.length() == 5);
			assertTrue(StringUtils.startsWith(password, "+"));
		}
	}
	
	@Test
	public void testGenerateWithBeginForbiddenNotPossible() {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_10_2");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(5);
		policy.setMinPasswordLength(5);
		policy.setSpecialCharBase("+");
		policy.setLowerCharBase("a");
		policy.setUpperCharBase("A");
		policy.setNumberBase("1");
		policy.setProhibitedBeginCharacters("1Aa+");

		for (int index = 0; index < ATTEMPTS; index++) {
			try {
				passwordPolicyService.generatePassword(policy);
				fail("Expected to throw");
			} catch (ResultCodeException e) {
				assertEquals(e.getError().getError().getStatusEnum(), CoreResultCode.PASSWORD_POLICY_INVALID_SETTING.getCode());
			} catch (Exception e) {
				fail("Unexcpected exception");
			}
		}
	}

	@Test
	public void testGenerateWithEndForbidden() {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_10_3");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(5);
		policy.setMinPasswordLength(5);
		policy.setSpecialCharBase("+");
		policy.setLowerCharBase("a");
		policy.setUpperCharBase("A");
		policy.setNumberBase("1");
		policy.setProhibitedEndCharacters("1Aa");

		for (int index = 0; index < ATTEMPTS; index++) {
			String password = passwordPolicyService.generatePassword(policy);
			assertTrue(password.length() == 5);
			assertTrue(StringUtils.endsWith(password, "+"));
		}
	}
	
	@Test
	public void testBeginEndIgnoredWhenPrefixSuffix () {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_10_4");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(5);
		policy.setMinPasswordLength(5);
		policy.setSpecialCharBase("+");
		policy.setLowerCharBase("a");
		policy.setUpperCharBase("A");
		policy.setNumberBase("1");
		policy.setProhibitedBeginCharacters("X");
		policy.setProhibitedEndCharacters("Y");
		policy.setPrefix("Xzz");
		policy.setSuffix("zzY");
		
		for (int index = 0; index < ATTEMPTS; index++) {
			String password = passwordPolicyService.generatePassword(policy);
			assertTrue(StringUtils.startsWith(password, "Xzz"));
			assertTrue(StringUtils.endsWith(password, "zzY"));
		}
	}
	
	@Test
	public void testBeginEndPartOfManadatoryChars () {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_10_5");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(5);
		policy.setMinPasswordLength(5);
		policy.setSpecialCharBase("+");
		policy.setLowerCharBase("abcdefghijklmnopqrstuvwxyz");
		policy.setUpperCharBase("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		policy.setNumberBase("0123456789");
		policy.setProhibitedBeginCharacters("abcde");
		policy.setProhibitedEndCharacters("vwxyz");
		
		for (int index = 0; index < ATTEMPTS; index++) {
			// must not throw
			// password policy test is part of save process
			passwordPolicyService.generatePassword(policy);
		}
	}
	
	@Test
	public void testBeginEndForbiddenWithPassLen1Char () {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_10_6");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(1);
		policy.setMinPasswordLength(1);
		policy.setSpecialCharBase("+");
		policy.setLowerCharBase("abcdefghijklmnopqrstuvwxyz");
		policy.setUpperCharBase("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		policy.setNumberBase("0123456789");
		policy.setProhibitedBeginCharacters("abcde");
		policy.setProhibitedEndCharacters("vwxyz");
		
		for (int index = 0; index < ATTEMPTS; index++) {
			// must not throw
			// password policy test is part of save process
			passwordPolicyService.generatePassword(policy);
		}
	}
	
	@Test
	public void testPolicyGeneratorValidator () {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_10_7");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(10);
		policy.setMinPasswordLength(8);
		policy.setMinLowerChar(2);
		policy.setMinNumber(2);
		policy.setMinUpperChar(2);
		policy.setMinSpecialChar(2);
		policy.setSpecialCharBase("!@#+$%&*");
		policy.setLowerCharBase("abcdefghijklmnopqrstuvwxyz");
		policy.setUpperCharBase("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		policy.setNumberBase("0123456789");
		policy.setProhibitedCharacters("X");
		policy.setProhibitedBeginCharacters("a");
		policy.setProhibitedEndCharacters("z");
		
		PasswordGenerator generator = passwordPolicyService.getPasswordGenerator();
		
		// contains forbidden chars
		assertFalse(generator.testPasswordAgainstPolicy("a1@9X+loZs", policy));
		// contains forbidden chars at the beginning
		assertFalse(generator.testPasswordAgainstPolicy("a1@9Y+loZs", policy));
		// contains forbidden chars at the end
		assertFalse(generator.testPasswordAgainstPolicy("b1@9Y+loJz", policy));
		// does not contain min count from lower
		assertFalse(generator.testPasswordAgainstPolicy("A1@9Y+9*Zs", policy));
		// does not contain min count from upper
		assertFalse(generator.testPasswordAgainstPolicy("f1@9y+loZs", policy));
		// does not contain min count from special
		assertFalse(generator.testPasswordAgainstPolicy("f1#9yUloZs", policy));
		// does not contain min count from numbers
		assertFalse(generator.testPasswordAgainstPolicy("fG@9y+loZs", policy));
		// pass too long
		assertFalse(generator.testPasswordAgainstPolicy("h6%#ghGABDc*+369", policy));
		// pass too short
		assertFalse(generator.testPasswordAgainstPolicy("h6%G", policy));
	}
	
	@Test
	public void testValidateLength() {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_11");
		policy.setType(IdmPasswordPolicyType.VALIDATE);
		policy.setMaxPasswordLength(10);
		policy.setMinPasswordLength(5);
		
		IdmPasswordValidationDto password = new IdmPasswordValidationDto();
		
		try {
			password.setPassword("12345");
			this.passwordPolicyService.validate(password, policy);
			
			password.setPassword("123456");
			this.passwordPolicyService.validate(password, policy);
			
			password.setPassword("1234567");
			this.passwordPolicyService.validate(password, policy);
			
			password.setPassword("12345678");
			this.passwordPolicyService.validate(password, policy);
			
			password.setPassword("123456789");
			this.passwordPolicyService.validate(password, policy);
			
			password.setPassword("1234567890");
			this.passwordPolicyService.validate(password, policy);
		} catch (Exception e) {
			fail("Password validation length. Error message: " + e.getStackTrace().toString());
		}
		
		
		try {
			password.setPassword("1234");
			this.passwordPolicyService.validate(password, policy);
			fail("Password validation length.");
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			password.setPassword("");
			this.passwordPolicyService.validate(password, policy);
			fail("Password validation length.");
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			password.setPassword("123456789123");
			this.passwordPolicyService.validate(password, policy);
			fail("Password validation length.");
		} catch (Exception e) {
			// nothing, success
		}
	}
	
	@Test
	public void testValidateMinNumbers() {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_12");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(3);
		policy.setMinPasswordLength(1);
		policy.setMinNumber(2);
		
		IdmPasswordValidationDto password = new IdmPasswordValidationDto();
		
		try {
			password.setPassword("123");
			this.passwordPolicyService.validate(password, policy);
			
			password.setPassword("12");
			this.passwordPolicyService.validate(password, policy);
			
			password.setPassword("12a");
			this.passwordPolicyService.validate(password, policy);
		} catch (Exception e) {
			fail("Password validation numbers. " + e.getMessage());
		}
		
		try {
			password.setPassword("1");
			this.passwordPolicyService.validate(password, policy);
			fail("Password validation numbers. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			password.setPassword("1234");
			this.passwordPolicyService.validate(password, policy);
			fail("Password validation numbers. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			password.setPassword("test");
			this.passwordPolicyService.validate(password, policy);
			fail("Password validation numbers. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
	}
	
	@Test
	public void testMinCharacterCountExplicitlySetZero() {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_20");
		policy.setType(IdmPasswordPolicyType.VALIDATE);
		policy.setMinPasswordLength(0);
		policy.setMinNumber(0);
		policy.setMinLowerChar(0);
		policy.setMinSpecialChar(0);
		policy.setMinUpperChar(0);

		IdmPasswordValidationDto password = new IdmPasswordValidationDto();

		try {
			password.setPassword("asdfg12345###");
			this.passwordPolicyService.validate(password, policy);

			password.setPassword("");
			this.passwordPolicyService.validate(password, policy);

			password.setPassword("123456@#$%^&*ASDFGHJK");
			this.passwordPolicyService.validate(password, policy);
		} catch (Exception e) {
			// nothing, success
		}
	}
	
	@Test
	public void testValidateSpecialChar() {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_13");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(3);
		policy.setMinPasswordLength(1);
		policy.setMinSpecialChar(2);
		
		IdmPasswordValidationDto password = new IdmPasswordValidationDto();
		
		try {
			password.setPassword("!@");
			this.passwordPolicyService.validate(password, policy);
			
			password.setPassword("!@#");
			this.passwordPolicyService.validate(password, policy);
			
			password.setPassword("!@a");
			this.passwordPolicyService.validate(password, policy);
		} catch (Exception e) {
			fail("Password validation special chars. " + e.getMessage());
		}
		
		try {
			password.setPassword("!");
			this.passwordPolicyService.validate(password, policy);
			fail("Password validation special chars. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			password.setPassword("!@#$");
			this.passwordPolicyService.validate(password, policy);
			fail("Password validation special chars. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			password.setPassword("test");
			this.passwordPolicyService.validate(password, policy);
			fail("Password validation special chars. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
	}
	
	@Test
	public void testValidationProhibitedChars() {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_14");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(5);
		policy.setMinPasswordLength(1);
		policy.setProhibitedCharacters("12abcDEF^-!@");
		
		IdmPasswordValidationDto password = new IdmPasswordValidationDto();
		
		try {
			password.setPassword("test");
			this.passwordPolicyService.validate(password, policy);
			
			password.setPassword("ABde");
			this.passwordPolicyService.validate(password, policy);
		} catch (Exception e) {
			fail("Password validate prohibited characters. " + policy);
		}
		
		try {
			password.setPassword("tEst");
			this.passwordPolicyService.validate(password, policy);
			fail("Password validate prohibited characters. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			password.setPassword("eddD");
			this.passwordPolicyService.validate(password, policy);
			fail("Password validate prohibited characters. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			password.setPassword("5416");
			this.passwordPolicyService.validate(password, policy);
			fail("Password validate prohibited characters. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			password.setPassword("test!");
			this.passwordPolicyService.validate(password, policy);
			fail("Password validate prohibited characters. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
	}
	
	@Test
	public void testValidateWithForbiddenBeginEnd() {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_14_1");
		policy.setType(IdmPasswordPolicyType.VALIDATE);
		policy.setMinPasswordLength(0);
		policy.setMinNumber(0);
		policy.setMinLowerChar(0);
		policy.setMinSpecialChar(0);
		policy.setMinUpperChar(0);
		policy.setProhibitedBeginCharacters("A");
		policy.setProhibitedEndCharacters("B");
		IdmPasswordValidationDto password = new IdmPasswordValidationDto();
		password.setPassword("aAsdfg12B3");

		this.passwordPolicyService.validate(password, policy);

		try {
			password.setPassword("Asdfg12s");
			this.passwordPolicyService.validate(password, policy);
			fail("A forbidden character at the beginning of the password was not detected");
		} catch (Exception e) {
			// nothing, success
		}

		try {
			password.setPassword("asdfg12B");
			this.passwordPolicyService.validate(password, policy);
			fail("A forbidden character at the end of the password was not detected");
		} catch (Exception e) {
			// nothing, success
		}
	}
	
	@Test
	public void testValidateBase() {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_15");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMaxPasswordLength(5);
		policy.setMinPasswordLength(1);
		policy.setNumberBase("123");
		policy.setMinNumber(3);
		
		IdmPasswordValidationDto password = new IdmPasswordValidationDto();
		
		try {
			password.setPassword("123");
			this.passwordPolicyService.validate(password, policy);
			
			password.setPassword("1234");
			this.passwordPolicyService.validate(password, policy);
			
			password.setPassword("111");
			this.passwordPolicyService.validate(password, policy);
		} catch (Exception e) {
			fail("Password base validation. " + policy);
		}
		
		try {
			password.setPassword("124");
			this.passwordPolicyService.validate(password, policy);
			fail("Password base validation. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			password.setPassword("456");
			this.passwordPolicyService.validate(password, policy);
			fail("Password base validation. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
	}
	
	@Test
	public void testValidateComplex() {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
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
		
		IdmPasswordValidationDto password = new IdmPasswordValidationDto();
		
		try {
			password.setPassword("000abc@@@DEF");
			this.passwordPolicyService.validate(password, policy);
			
			password.setPassword("F0a@0Ec0b@@D");
			this.passwordPolicyService.validate(password, policy);
			
			password.setPassword("#3aBb@C3A1#0c00");
			this.passwordPolicyService.validate(password, policy);
		} catch (Exception e) {
			fail("Password complex validation. " + policy);
		}
		
		try {
			password.setPassword("001abc@@@DEF");
			this.passwordPolicyService.validate(password, policy);
			fail("Password complex validation. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			password.setPassword("000abc##$DEF");
			this.passwordPolicyService.validate(password, policy);
			fail("Password complex validation. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			password.setPassword("000abc)()DEF");
			this.passwordPolicyService.validate(password, policy);
			fail("Password complex validation. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			password.setPassword("#3aBb@C3A1#0c00idheff");
			this.passwordPolicyService.validate(password, policy);
			fail("Password complex validation. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			password.setPassword("#3aBmb@C3A1#0c00");
			this.passwordPolicyService.validate(password, policy);
			fail("Password complex validation. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			password.setPassword("#3aBb@C3A1n#0c00");
			this.passwordPolicyService.validate(password, policy);
			fail("Password complex validation. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
		
		try {
			password.setPassword("#3mBb*@C3A1n#0c00");
			this.passwordPolicyService.validate(password, policy);
			fail("Password complex validation. " + policy);
		} catch (Exception e) {
			// nothing, success
		}
	}
	
	@Test
	public void testCreateAndFoundPasswordPolicy() {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_17_saved");
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setDefaultPolicy(false);
		policy.setMaxPasswordLength(20);
		policy.setMinPasswordLength(6);
		
		policy = this.passwordPolicyService.save(policy);
		
		IdmPasswordPolicyDto foundPolicyByName = this.passwordPolicyService.findOneByName("test_17_saved");
		assertEquals(policy.getName(), foundPolicyByName.getName());
		assertEquals(policy.getType(), foundPolicyByName.getType());
		assertEquals(policy.getMaxPasswordLength(), foundPolicyByName.getMaxPasswordLength());
		assertEquals(policy.getMinPasswordLength(), foundPolicyByName.getMinPasswordLength());
		
		IdmPasswordPolicyDto foundPolicyById = this.passwordPolicyService.get(policy.getId());
		assertEquals(policy.getName(), foundPolicyById.getName());
		assertEquals(policy.getType(), foundPolicyById.getType());
		assertEquals(policy.getMaxPasswordLength(), foundPolicyById.getMaxPasswordLength());
		assertEquals(policy.getMinPasswordLength(), foundPolicyById.getMinPasswordLength());
	}
	
	@Test
	public void testCreateTwoDefaultPolicy() {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_18_default");
		policy.setType(IdmPasswordPolicyType.VALIDATE);
		policy.setDefaultPolicy(true);
		
		policy = saveInTransaction(policy, passwordPolicyService);
		
		IdmPasswordPolicyDto defaultValidatePolicy = this.passwordPolicyService.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);
		assertEquals(policy.getId(), defaultValidatePolicy.getId());
		assertEquals(policy.getName(), defaultValidatePolicy.getName());
		assertEquals(policy.getType(), defaultValidatePolicy.getType());
		
		IdmPasswordPolicyDto policyNew = new IdmPasswordPolicyDto();
		policyNew.setName("test_19_default");
		policyNew.setType(IdmPasswordPolicyType.VALIDATE);
		policyNew.setDefaultPolicy(true);
		
		policyNew = saveInTransaction(policyNew, passwordPolicyService);
		
		defaultValidatePolicy = this.passwordPolicyService.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);
		assertEquals(policyNew.getId(), defaultValidatePolicy.getId());
		assertEquals(policyNew.getName(), defaultValidatePolicy.getName());
		assertEquals(policyNew.getType(), defaultValidatePolicy.getType());
	}
	
	@Test
	public void testHistoryPassword() {
		String firstPassword = "test-password-first-" + System.currentTimeMillis();
		String secondPassword = "test-password-second-" + System.currentTimeMillis();
		String thridPassword = "test-password-third" + System.currentTimeMillis();
		IdmIdentityDto identity = getHelper().createIdentity();
		
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_default_policy_" + System.currentTimeMillis());
		policy.setType(IdmPasswordPolicyType.VALIDATE);
		policy.setDefaultPolicy(true);
		policy.setMaxHistorySimilar(2);
		policy = passwordPolicyService.save(policy);
		
		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setIdm(true);
		passwordChange.setAll(true);
		passwordChange.setNewPassword(new GuardedString(firstPassword));
		

		identityService.passwordChange(identity, passwordChange);

		// we must login as no admin
		loginAsNoAdmin(identity.getUsername());
		try {
			// same password as before
			passwordChange.setNewPassword(new GuardedString(firstPassword));
			passwordChange.setOldPassword(new GuardedString(firstPassword));
			identityService.passwordChange(identity, passwordChange);
		} catch (ResultCodeException e) {
			checkMaxHistorySimilarError(e, 2);
		} catch (Exception ex) {
			fail("Bad exception: " + ex.toString());
		}
		
		// new password
		passwordChange.setNewPassword(new GuardedString(secondPassword));
		passwordChange.setOldPassword(new GuardedString(firstPassword));
		identityService.passwordChange(identity, passwordChange);
		
		try {
			// same password as first
			passwordChange.setNewPassword(new GuardedString(firstPassword));
			passwordChange.setOldPassword(new GuardedString(secondPassword));
			identityService.passwordChange(identity, passwordChange);
		} catch (ResultCodeException e) {
			checkMaxHistorySimilarError(e, 2);
		} catch (Exception ex) {
			fail("Bad exception: " + ex.toString());
		}
		
		
		try {
			// same password as the second
			passwordChange.setNewPassword(new GuardedString(secondPassword));
			passwordChange.setOldPassword(new GuardedString(secondPassword));
			identityService.passwordChange(identity, passwordChange);
		} catch (ResultCodeException e) {
			checkMaxHistorySimilarError(e, 2);
		} catch (Exception ex) {
			fail("Bad exception: " + ex.toString());
		}
		
		// new password
		passwordChange.setNewPassword(new GuardedString(thridPassword));
		passwordChange.setOldPassword(new GuardedString(secondPassword));
		identityService.passwordChange(identity, passwordChange);
		
		try {
			// same password as the second
			passwordChange.setNewPassword(new GuardedString(secondPassword));
			passwordChange.setOldPassword(new GuardedString(thridPassword));
			identityService.passwordChange(identity, passwordChange);
		} catch (ResultCodeException e) {
			checkMaxHistorySimilarError(e, 2);
		} catch (Exception ex) {
			fail("Bad exception: " + ex.toString());
		}
		
		try {
			// same password as the second
			passwordChange.setNewPassword(new GuardedString(thridPassword));
			passwordChange.setOldPassword(new GuardedString(thridPassword));
			identityService.passwordChange(identity, passwordChange);
		} catch (ResultCodeException e) {
			checkMaxHistorySimilarError(e, 2);
		} catch (Exception ex) {
			fail("Bad exception: " + ex.toString());
		}
		
		// new password (the first one)
		passwordChange.setNewPassword(new GuardedString(firstPassword));
		passwordChange.setOldPassword(new GuardedString(thridPassword));
		identityService.passwordChange(identity, passwordChange);
		
		passwordPolicyService.delete(policy);
	}

	@Test
	public void testExistingSuffix() {
		String suffix = "test-suffix-" + System.currentTimeMillis();
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_policy" + System.currentTimeMillis());
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setMinNumber(4);
		policy.setMinPasswordLength(4);
		policy.setMaxPasswordLength(4);
		policy.setSuffix(suffix);
		policy = passwordPolicyService.save(policy);
		
		String generatePassword = passwordPolicyService.generatePassword(policy);
		
		assertTrue(generatePassword.endsWith(suffix));
		String password = StringUtils.remove(generatePassword, suffix);
		// password must be only integer
		Integer.valueOf(password);
		assertEquals(4, password.length());
		assertEquals(suffix.length() + 4, generatePassword.length());
	}

	@Test
	public void testExistingPrefix() {
		String prefix = "test-prefix-" + System.currentTimeMillis();
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_policy" + System.currentTimeMillis());
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setPrefix(prefix);
		policy.setMinLowerChar(5);
		policy.setMinPasswordLength(5);
		policy.setMaxPasswordLength(5);
		policy = passwordPolicyService.save(policy);
		
		String generatePassword = passwordPolicyService.generatePassword(policy);
		
		assertTrue(generatePassword.startsWith(prefix));
		String password = StringUtils.remove(generatePassword, prefix);
		assertEquals(5, password.length());
		assertEquals(prefix.length() + 5, generatePassword.length());
	}

	@Test
	public void testExistingPrefixAndSuffix() {
		String prefix = "test-prefix-" + System.currentTimeMillis();
		String suffix = "test-suffix-" + System.currentTimeMillis();
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_policy" + System.currentTimeMillis());
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		policy.setPrefix(prefix);
		policy.setSuffix(suffix);
		policy.setMaxPasswordLength(10);
		policy.setMinPasswordLength(10);
		policy = passwordPolicyService.save(policy);
		
		String generatePassword = passwordPolicyService.generatePassword(policy);
		
		assertTrue(generatePassword.startsWith(prefix));
		assertTrue(generatePassword.endsWith(suffix));
		assertEquals(suffix.length() + prefix.length() + 10, generatePassword.length());
		
		String password = StringUtils.remove(generatePassword, prefix);
		password = StringUtils.remove(password, suffix);
		assertEquals(10, password.length());
	}

	@Test
	public void testPrefixAndSuffixWithPassphrase() {
		String prefix = "test-prefix-" + System.currentTimeMillis();
		String suffix = "test-suffix-" + System.currentTimeMillis();
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName("test_policy" + System.currentTimeMillis());
		policy.setType(IdmPasswordPolicyType.GENERATE);
		policy.setGenerateType(IdmPasswordPolicyGenerateType.PASSPHRASE);
		policy.setPrefix(prefix);
		policy.setSuffix(suffix);
		policy.setPassphraseWords(2);
		policy = passwordPolicyService.save(policy);
		
		String generatePassword = passwordPolicyService.generatePassword(policy);
		
		assertTrue(generatePassword.startsWith(prefix));
		assertTrue(generatePassword.endsWith(suffix));
	}

	@Test
	public void testContainsUsername() {
		IdmIdentityDto identity = this.getHelper().createIdentity("John217", (GuardedString) null);
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setType(IdmPasswordPolicyType.VALIDATE);
		policy.setEnchancedControl(true);
		policy.setIdentityAttributeCheck(IdmIdentity_.username.getName().toUpperCase());

		// Equals
		IdmPasswordValidationDto validation = new IdmPasswordValidationDto();
		validation.setIdentity(identity);
		validation.setPassword(identity.getUsername());
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Prefix with lower
		validation.setPassword("123" + identity.getUsername().toLowerCase());
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Suffix with upper
		validation.setPassword(identity.getUsername().toUpperCase() + System.currentTimeMillis());
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Suffix and prefix with accent
		validation.setPassword("demojÓhn217" + System.currentTimeMillis());
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Prefix with accent
		validation.setPassword("demojÓhň217");
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Success prefix
		validation.setPassword("demojoh");
		try {
			passwordPolicyService.validate(validation, policy);
			// Success
		} catch (ResultCodeException e) {
			fail("Password not pass.");
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Success suffix with accent
		validation.setPassword("jóhdemo");
		try {
			passwordPolicyService.validate(validation, policy);
			// Success
		} catch (ResultCodeException e) {
			fail("Password not pass.");
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
		// Compound username
		identity.setUsername("Dobromila-,Josefa_\tM.");
		identityService.save(identity);
		validation.setPassword("joseFadobrómílá");
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Success compound username
		validation.setPassword("josefmJeLidumil");
		try {
			passwordPolicyService.validate(validation, policy);
		} catch (ResultCodeException e) {
			fail("Password not pass.");
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testContainsEmail() {
		String email = "repa@example.tld";
		IdmIdentityDto identity = this.getHelper().createIdentity((GuardedString) null);
		identity.setEmail(email);
		identity = identityService.save(identity);

		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setType(IdmPasswordPolicyType.VALIDATE);
		policy.setEnchancedControl(true);
		policy.setIdentityAttributeCheck(IdmIdentity_.email.getName().toUpperCase());

		// Equals
		IdmPasswordValidationDto validation = new IdmPasswordValidationDto();
		validation.setIdentity(identity);
		validation.setPassword(email);
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Prefix with lower
		validation.setPassword("123" + email.toLowerCase());
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Suffix with upper
		validation.setPassword(email.toUpperCase() + System.currentTimeMillis());
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Suffix and prefix with accent
		validation.setPassword("demořěpá@example.tld" + System.currentTimeMillis());
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Prefix with accent
		validation.setPassword("demoŘĚPÁ@example");
		try {
			passwordPolicyService.validate(validation, policy);
			// Success
		} catch (ResultCodeException e) {
			fail("Password pass.");
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Success prefix
		validation.setPassword("demorepa@example.");
		try {
			passwordPolicyService.validate(validation, policy);
			// Success
		} catch (ResultCodeException e) {
			fail("Password not pass.");
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Success suffix with accent
		validation.setPassword("@example.tdldemo");
		try {
			passwordPolicyService.validate(validation, policy);
			// Success
		} catch (ResultCodeException e) {
			fail("Password not pass.");
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Success suffix with accent
		validation.setPassword("@");
		try {
			passwordPolicyService.validate(validation, policy);
			// Success
		} catch (ResultCodeException e) {
			fail("Password not pass.");
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testContainsLastName() {
		String lastName = "čeněk";
		IdmIdentityDto identity = this.getHelper().createIdentity((GuardedString) null);
		identity.setLastName(lastName);
		identity = identityService.save(identity);

		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setType(IdmPasswordPolicyType.VALIDATE);
		policy.setEnchancedControl(true);
		policy.setIdentityAttributeCheck(IdmIdentity_.lastName.getName().toUpperCase());

		// Equals
		IdmPasswordValidationDto validation = new IdmPasswordValidationDto();
		validation.setIdentity(identity);
		validation.setPassword(lastName);
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		validation.setPassword("cENEkdemo");
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Prefix with lower
		validation.setPassword("123" + lastName.toLowerCase());
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Suffix with upper
		validation.setPassword(lastName.toUpperCase() + System.currentTimeMillis());
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Suffix and prefix with accent
		validation.setPassword("demo" + lastName + System.currentTimeMillis());
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Prefix
		validation.setPassword("demoCENEK");
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Success prefix
		validation.setPassword("demočene");
		try {
			passwordPolicyService.validate(validation, policy);
			// Success
		} catch (ResultCodeException e) {
			fail("Password not pass.");
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Success suffix with accent
		validation.setPassword("cenedemo");
		try {
			passwordPolicyService.validate(validation, policy);
			// Success
		} catch (ResultCodeException e) {
			fail("Password not pass.");
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
		// Compound lastname
		identity.setLastName("Nováková-Bláhová III.");
		identityService.save(identity);
		validation.setPassword("novakova");
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		validation.setPassword("blahovÁ");
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		validation.setPassword("novak-blahaIII");
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Success compound username
		validation.setPassword("novak-blahaII");
		try {
			passwordPolicyService.validate(validation, policy);
		} catch (ResultCodeException e) {
			fail("Password not pass.");
		} catch (Exception e) {
			fail(e.getMessage());
		}

		validation.setPassword("novak-blahaI II");
		try {
			passwordPolicyService.validate(validation, policy);
		} catch (ResultCodeException e) {
			fail("Password not pass.");
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testContainsFirstName() {
		String firstName = "%ř8()á";
		IdmIdentityDto identity = this.getHelper().createIdentity((GuardedString) null);
		identity.setFirstName(firstName);
		identity = identityService.save(identity);

		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setType(IdmPasswordPolicyType.VALIDATE);
		policy.setEnchancedControl(true);
		policy.setIdentityAttributeCheck(IdmIdentity_.firstName.getName().toUpperCase());

		// Equals
		IdmPasswordValidationDto validation = new IdmPasswordValidationDto();
		validation.setIdentity(identity);
		validation.setPassword(firstName);
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		validation.setPassword("%r8()ademo");
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Prefix with lower
		validation.setPassword("123" + firstName.toLowerCase());
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Suffix with upper
		validation.setPassword(firstName.toUpperCase() + System.currentTimeMillis());
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Suffix and prefix with accent
		validation.setPassword("demo" + firstName + System.currentTimeMillis());
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Prefix
		validation.setPassword("demo%Ř8()A");
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Success suffix with accent
		validation.setPassword("%r()daemoa");
		try {
			passwordPolicyService.validate(validation, policy);
			// Success
		} catch (ResultCodeException e) {
			fail("Password not pass.");
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Compound firstname
		identity.setFirstName("Arnold,.\t-—_£J.Rimm3r");
		identityService.save(identity);
		validation.setPassword("ArnoldRimm3r");
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		validation.setPassword("SchwarzeArnoldNegger");
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Success compound username
		validation.setPassword("JJJ Rimmer");
		try {
			passwordPolicyService.validate(validation, policy);
		} catch (ResultCodeException e) {
			fail("Password not pass.");
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	
	@Test
	public void testContainsTitleBeforeAfter() {
		IdmIdentityDto identity = this.getHelper().createIdentity((GuardedString) null);
		identity.setTitleBefore("Judr. MUDr, MvDr. , Prof.,Doc. Bc. Ing. Arch. Dr.");
		identity.setTitleAfter("CSc.,DrSc. Ph.D.");
		identity = identityService.save(identity);

		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setType(IdmPasswordPolicyType.VALIDATE);
		policy.setEnchancedControl(true);
		policy.setIdentityAttributeCheck(IdmPasswordPolicyIdentityAttributes.TITLESAFTER.name() + ", "
				+ IdmPasswordPolicyIdentityAttributes.TITLESBEFORE.name());

		IdmPasswordValidationDto validation = new IdmPasswordValidationDto();
		validation.setIdentity(identity);
		validation.setPassword("IngKarelVomacka");

		// Titles before only
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		validation.setPassword("noemovaarcha");
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		validation.setPassword("youAreDoingWell");
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Success titles before only
		validation.setPassword("DrVostepJeOkBoJeKratky");
		try {
			passwordPolicyService.validate(validation, policy);
		} catch (ResultCodeException e) {
			fail("Password not pass.");
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Titles after
		validation.setPassword("PhdMitfaktNikdyNebudu");
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testContainsPersonalNumber() {
		IdmIdentityDto identity = this.getHelper().createIdentity((GuardedString) null);
		identity.setExternalCode("123-456-789-0_EEE");
		identity = identityService.save(identity);

		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setType(IdmPasswordPolicyType.VALIDATE);
		policy.setEnchancedControl(true);
		policy.setIdentityAttributeCheck(IdmPasswordPolicyIdentityAttributes.EXTERNALCODE.name());

		IdmPasswordValidationDto validation = new IdmPasswordValidationDto();
		validation.setIdentity(identity);
		validation.setPassword("123456789");

		// Numbers only
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		validation.setPassword("jenda123");
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Works with letters
		validation.setPassword("999999999ĚÉE");
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Success
		validation.setPassword("XXXXXX0");
		try {
			passwordPolicyService.validate(validation, policy);
		} catch (ResultCodeException e) {
			fail("Password not pass.");
		} catch (Exception e) {
			fail(e.getMessage());
		}

		validation.setPassword("987-654-321");
		try {
			passwordPolicyService.validate(validation, policy);
		} catch (ResultCodeException e) {
			fail("Password not pass.");
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	

	@Test
	public void testContainsCombination() {
		String firstName = "DěmÓ";
		String lastName = "Těšť";
		String username = "ExámplÉ";
		IdmIdentityDto identity = this.getHelper().createIdentity((GuardedString) null);
		identity.setFirstName(firstName);
		identity.setUsername(username);
		identity.setLastName(lastName);
		identity = identityService.save(identity);

		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setType(IdmPasswordPolicyType.VALIDATE);
		policy.setEnchancedControl(true);
		policy.setIdentityAttributeCheck(IdmIdentity_.firstName.getName().toUpperCase() + ", " + IdmIdentity_.username.getName().toUpperCase());

		// Equals
		IdmPasswordValidationDto validation = new IdmPasswordValidationDto();
		validation.setIdentity(identity);
		validation.setPassword(firstName);
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}

		// Equals
		validation.setPassword(username);
		try {
			passwordPolicyService.validate(validation, policy);
			fail("Password pass.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
		// Equals with not controlled
		validation.setPassword(lastName);
		try {
			passwordPolicyService.validate(validation, policy);
			// Success
		} catch (ResultCodeException e) {
			fail("Password not pass.");
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testValidateMinPasswordAgeSuccess() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setType(IdmPasswordPolicyType.VALIDATE);
		policy.setMinPasswordAge(1);
		//
		IdmPasswordValidationDto validation = new IdmPasswordValidationDto();
		validation.setIdentity(identity);
		validation.setPassword(getHelper().createName());
		//
		try {
			// without password - ok
			passwordPolicyService.validate(validation, policy);
			//
			// create password
			PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
			passwordChangeDto.setIdm(true);
			GuardedString newPassword = new GuardedString(getHelper().createName());
			passwordChangeDto.setNewPassword(newPassword);
			IdmPasswordDto password = passwordService.save(identity, passwordChangeDto);
			Assert.assertNull(password.getValidFrom());
			//
			identity.setPassword(newPassword);
			getHelper().login(identity);
			// null valid from - ok
			passwordPolicyService.validate(validation, policy);
			//
			// in past - ok
			password.setValidFrom(LocalDate.now().minusDays(1));
			password = passwordService.save(password);
			passwordPolicyService.validate(validation, policy);
			//
			// must change - ok
			password.setValidFrom(LocalDate.now());
			password.setMustChange(true);
			password = passwordService.save(password);
			passwordPolicyService.validate(validation, policy);
			//
			//
			// prevalidate - ok
			password.setMustChange(false);
			password = passwordService.save(password);
			passwordPolicyService.preValidate(validation, Lists.newArrayList(policy));
		} finally {
			getHelper().logout();
		}
		//
		// under admin - ok
		try {
			getHelper().loginAdmin();
			passwordPolicyService.validate(validation, policy);
		} finally {
			getHelper().logout();
		}
		//
		// under different user - ok
		IdmIdentityDto manager = getHelper().createIdentity();
		try {
			getHelper().login(manager);
			passwordPolicyService.validate(validation, policy);
		} finally {
			getHelper().logout();
		}
	}
	
	@Test(expected = PasswordChangeException.class)
	public void testValidateMinPasswordAgeFailedSameUser() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmPasswordDto password = getHelper().getPassword(identity);
		password.setValidFrom(LocalDate.now());
		password = passwordService.save(password);
		//
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setType(IdmPasswordPolicyType.VALIDATE);
		policy.setMinPasswordAge(1);
		//
		IdmPasswordValidationDto validation = new IdmPasswordValidationDto();
		validation.setIdentity(identity);
		validation.setPassword(getHelper().createName());
		//
		try {
			getHelper().login(identity);
			//
			passwordPolicyService.validate(validation, Lists.newArrayList(policy));
		} finally {
			getHelper().logout();
		}
	}
	
	@Test(expected = PasswordChangeException.class)
	public void testValidateMinPasswordAgeFailedEnforce() {
		IdmIdentityDto manager = getHelper().createIdentity();
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmPasswordDto password = getHelper().getPassword(identity);
		password.setValidFrom(LocalDate.now());
		password = passwordService.save(password);
		//
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setType(IdmPasswordPolicyType.VALIDATE);
		policy.setMinPasswordAge(1);
		//
		IdmPasswordValidationDto validation = new IdmPasswordValidationDto();
		validation.setIdentity(identity);
		validation.setPassword(getHelper().createName());
		validation.setEnforceMinPasswordAgeValidation(true);
		//
		try {
			getHelper().login(manager);
			//
			passwordPolicyService.validate(validation, Lists.newArrayList(policy));
		} finally {
			getHelper().logout();
		}
	}

	private void checkMaxHistorySimilarError(ResultCodeException exception, int maxHistorySettingOriginal) {
		ErrorModel error = exception.getError().getError();
		assertTrue(error.getMessage().contains("Password does not match password policy"));
		assertEquals(CoreResultCode.PASSWORD_DOES_NOT_MEET_POLICY.getCode(), error.getStatusEnum());
		Map<String, Object> parameters = error.getParameters();
		assertEquals(1, parameters.size());
		assertTrue(parameters.containsKey("maxHistorySimilar"));
		Object parameterAsObject = parameters.get("maxHistorySimilar");
		int maxHistorySetting = Integer.valueOf(parameterAsObject.toString());
		assertEquals(maxHistorySettingOriginal, maxHistorySetting);
	}
}
