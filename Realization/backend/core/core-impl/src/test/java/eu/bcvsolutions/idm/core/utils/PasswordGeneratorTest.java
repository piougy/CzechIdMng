package eu.bcvsolutions.idm.core.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.utils.PasswordGenerator;

/**
 * JUnit test for password generator
 *
 * @author Ondrej Kopr
 *
 */
public class PasswordGeneratorTest {

	private static final PasswordGenerator generator = new PasswordGenerator();

	@Test
	public void testmaxLengthWithMinLength() {
		String generatedString = generator.generateRandom(10, null, null, null, null, null);
		assertEquals(10, generatedString.length());
	}

	@Test
	public void testmaxLengthWithAllNull() {
		String generatedString = generator.generateRandom(null, null, null, null, null, null);
		assertEquals(12, generatedString.length());
	}

	@Test
	public void testmaxLengthWithNextMinDefinition() {
		String generatedString = generator.generateRandom(10, null, 1, 1, 1, 2);
		assertEquals(10, generatedString.length());
	}

	@Test
	public void testmaxLengthWithoutMinLengthAndNextDefinition() {
		String generatedString = generator.generateRandom(null, null, 1, 1, 1, 2);
		assertEquals(5, generatedString.length());
	}

	@Test
	public void testGenerateWithoutProhibited() {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setMinPasswordLength(16);
		policy.setMaxPasswordLength(16);
		policy.setMinLowerChar(4);
		policy.setMinUpperChar(4);
		policy.setMinSpecialChar(4);
		policy.setMinNumber(4);
		policy.setSpecialCharBase("-");
		policy.setProhibitedCharacters(PasswordGenerator.LOWER_CHARACTERS + PasswordGenerator.NUMBERS + PasswordGenerator.UPPER_CHARACTERS + "!@#$%&");

		for (int index = 0; index < 15; index++) {
			String generateRandom = generator.generateRandom(policy);
			assertEquals(16, generateRandom.length());
			assertEquals("----------------", generateRandom);
		}
	}
}
