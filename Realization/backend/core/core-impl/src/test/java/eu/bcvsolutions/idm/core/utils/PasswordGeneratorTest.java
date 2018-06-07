package eu.bcvsolutions.idm.core.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.bcvsolutions.idm.core.api.utils.PasswordGenerator;

/**
 * JUnit test for password generator
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class PasswordGeneratorTest {

	@Test
	public void testmaxLengthWithMinLength() {
		PasswordGenerator generator = new PasswordGenerator();
		String generatedString = generator.generateRandom(10, null, null, null, null, null);
		assertEquals(10, generatedString.length());
	}

	@Test
	public void testmaxLengthWithAllNull() {
		PasswordGenerator generator = new PasswordGenerator();
		String generatedString = generator.generateRandom(null, null, null, null, null, null);
		assertEquals(12, generatedString.length());
	}

	@Test
	public void testmaxLengthWithNextMinDefinition() {
		PasswordGenerator generator = new PasswordGenerator();
		String generatedString = generator.generateRandom(10, null, 1, 1, 1, 2);
		assertEquals(10, generatedString.length());
	}

	@Test
	public void testmaxLengthWithoutMinLengthAndNextDefinition() {
		PasswordGenerator generator = new PasswordGenerator();
		String generatedString = generator.generateRandom(null, null, 1, 1, 1, 2);
		assertEquals(5, generatedString.length());
	}
}
