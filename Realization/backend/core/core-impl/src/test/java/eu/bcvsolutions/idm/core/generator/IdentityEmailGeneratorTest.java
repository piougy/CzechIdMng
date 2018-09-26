package eu.bcvsolutions.idm.core.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.dto.GeneratorDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmGeneratedValueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.generator.impl.IdentityEmailGenerator;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Test for {@link IdentityEmailGenerator}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class IdentityEmailGeneratorTest extends AbstractGeneratorTest {

	@Autowired
	private IdmIdentityService identityService;

	@Test
	public void testWithoutUsername() {
		String emailSuffix = "@example.tld";
		IdmIdentityDto identityDto = new IdmIdentityDto();
		identityDto.setFirstName(this.getHelper().createName());
		identityDto.setLastName(this.getHelper().createName());

		GeneratorDefinitionDto generator = getGenerator();

		this.createGenerator(getEntityType(), getGeneratorType(),
				this.createConfiguration(generator.getFormDefinition(), ImmutableMap.of(
						IdentityEmailGenerator.EMAIL_SUFFIX, emailSuffix)), 1, null);

		IdmIdentityDto generatedDto = valueGeneratorManager.generate(identityDto);
		assertNull(generatedDto.getEmail());
	}

	@Test
	public void testGreenLine() {
		String emailSuffix = "@example.tld";
		String username = this.getHelper().createName();

		IdmIdentityDto identityDto = new IdmIdentityDto();
		identityDto.setFirstName(this.getHelper().createName());
		identityDto.setLastName(this.getHelper().createName());
		identityDto.setUsername(username);
		
		GeneratorDefinitionDto generator = getGenerator();

		this.createGenerator(getEntityType(), getGeneratorType(),
				this.createConfiguration(generator.getFormDefinition(), ImmutableMap.of(
						IdentityEmailGenerator.EMAIL_SUFFIX, emailSuffix)), 1, null);

		IdmIdentityDto generatedDto = valueGeneratorManager.generate(identityDto);
		assertNotNull(generatedDto.getEmail());
		assertEquals(username + emailSuffix, generatedDto.getEmail());
	}
	
	@Test
	public void testGreenLineWithIdentityService() {
		String emailSuffix = "@example.tld";
		String username = this.getHelper().createName();

		IdmIdentityDto identityDto = new IdmIdentityDto();
		identityDto.setFirstName(this.getHelper().createName());
		identityDto.setLastName(this.getHelper().createName());
		identityDto.setUsername(username);
		
		GeneratorDefinitionDto generator = getGenerator();

		this.createGenerator(getEntityType(), getGeneratorType(),
				this.createConfiguration(generator.getFormDefinition(), ImmutableMap.of(
						IdentityEmailGenerator.EMAIL_SUFFIX, emailSuffix)), 1, null);

		IdmIdentityDto generatedDto = identityService.save(identityDto);
		assertNotNull(generatedDto.getEmail());
		assertEquals(username + emailSuffix, generatedDto.getEmail());
	}

	@Test
	public void testGreenLineWithoutAt() {
		String emailSuffix = "example.tld";
		String username = this.getHelper().createName();

		IdmIdentityDto identityDto = new IdmIdentityDto();
		identityDto.setFirstName(this.getHelper().createName());
		identityDto.setLastName(this.getHelper().createName());
		identityDto.setUsername(username);
		
		GeneratorDefinitionDto generator = getGenerator();

		this.createGenerator(getEntityType(), getGeneratorType(),
				this.createConfiguration(generator.getFormDefinition(), ImmutableMap.of(
						IdentityEmailGenerator.EMAIL_SUFFIX, emailSuffix)), 1, null);

		IdmIdentityDto generatedDto = valueGeneratorManager.generate(identityDto);
		assertNotNull(generatedDto.getEmail());
		assertEquals(username + "@" + emailSuffix, generatedDto.getEmail());
	}

	@Test
	public void testUsernameContainsAt() {
		String emailSuffix = "example.tld";
		String username = "test@username";

		IdmIdentityDto identityDto = new IdmIdentityDto();
		identityDto.setFirstName(this.getHelper().createName());
		identityDto.setLastName(this.getHelper().createName());
		identityDto.setUsername(username);
		
		GeneratorDefinitionDto generator = getGenerator();

		this.createGenerator(getEntityType(), getGeneratorType(),
				this.createConfiguration(generator.getFormDefinition(), ImmutableMap.of(
						IdentityEmailGenerator.EMAIL_SUFFIX, emailSuffix)), 1, null);

		IdmIdentityDto generatedDto = valueGeneratorManager.generate(identityDto);
		assertNull(generatedDto.getEmail());
	}
	
	@Test
	public void testOrder() {
		String emailSuffix = "ex.tld";
		String emailSuffix1 = "@test.tld";
		String emailSuffix2 = "bcvsolutions.tld";
		String username = this.getHelper().createName();

		IdmIdentityDto identityDto = new IdmIdentityDto();
		identityDto.setFirstName(this.getHelper().createName());
		identityDto.setLastName(this.getHelper().createName());
		identityDto.setUsername(username);
		
		GeneratorDefinitionDto generator = getGenerator();

		// first
		this.createGenerator(getEntityType(), getGeneratorType(),
				this.createConfiguration(generator.getFormDefinition(), ImmutableMap.of(
						IdentityEmailGenerator.EMAIL_SUFFIX, emailSuffix)), 10, null);

		// second
		this.createGenerator(getEntityType(), getGeneratorType(),
				this.createConfiguration(generator.getFormDefinition(), ImmutableMap.of(
						IdentityEmailGenerator.EMAIL_SUFFIX, emailSuffix1)), 20, null);

		// the last - this generator will be generate email
		this.createGenerator(getEntityType(), getGeneratorType(),
				this.createConfiguration(generator.getFormDefinition(), ImmutableMap.of(
						IdentityEmailGenerator.EMAIL_SUFFIX, emailSuffix2)), 30, null);

		IdmIdentityDto generatedDto = valueGeneratorManager.generate(identityDto);
		assertNotNull(generatedDto.getEmail());
		assertEquals(username + "@" + emailSuffix2, generatedDto.getEmail());
	}

	@Test
	public void testRegenerate() {
		String emailSuffix = "ex.tld";
		String emailSuffix1 = "@test.tld";
		String emailSuffix2 = "bcvsolutions.tld";
		String username = this.getHelper().createName();
		String email = "email@example.tdl";

		IdmIdentityDto identityDto = new IdmIdentityDto();
		identityDto.setFirstName(this.getHelper().createName());
		identityDto.setLastName(this.getHelper().createName());
		identityDto.setUsername(username);
		identityDto.setEmail(email);
		
		GeneratorDefinitionDto generator = getGenerator();

		// first
		this.createGenerator(getEntityType(), getGeneratorType(),
				this.createConfiguration(generator.getFormDefinition(), ImmutableMap.of(
						IdentityEmailGenerator.EMAIL_SUFFIX, emailSuffix)), 10, Boolean.FALSE);

		// second
		this.createGenerator(getEntityType(), getGeneratorType(),
				this.createConfiguration(generator.getFormDefinition(), ImmutableMap.of(
						IdentityEmailGenerator.EMAIL_SUFFIX, emailSuffix1)), 20, Boolean.FALSE);

		// the last
		this.createGenerator(getEntityType(), getGeneratorType(),
				this.createConfiguration(generator.getFormDefinition(), ImmutableMap.of(
						IdentityEmailGenerator.EMAIL_SUFFIX, emailSuffix2)), 30, Boolean.FALSE);

		IdmIdentityDto generatedDto = valueGeneratorManager.generate(identityDto);
		assertNotNull(generatedDto.getEmail());
		assertEquals(email, generatedDto.getEmail());
	}

	@Test
	public void testDisable() {
		String emailSuffix = "ex.tld";
		String emailSuffix1 = "@test.tld";
		String emailSuffix2 = "bcvsolutions.tld";
		String username = this.getHelper().createName();

		IdmIdentityDto identityDto = new IdmIdentityDto();
		identityDto.setFirstName(this.getHelper().createName());
		identityDto.setLastName(this.getHelper().createName());
		identityDto.setUsername(username);
		
		GeneratorDefinitionDto generator = getGenerator();

		// first - this generator will be generate email
		this.createGenerator(getEntityType(), getGeneratorType(),
				this.createConfiguration(generator.getFormDefinition(), ImmutableMap.of(
						IdentityEmailGenerator.EMAIL_SUFFIX, emailSuffix)), 10, null);

		// second
		IdmGeneratedValueDto createGenerator = this.createGenerator(getEntityType(), getGeneratorType(),
				this.createConfiguration(generator.getFormDefinition(), ImmutableMap.of(
						IdentityEmailGenerator.EMAIL_SUFFIX, emailSuffix1)), 20, null);
		createGenerator.setDisabled(true);
		this.generatedAttributeService.save(createGenerator);

		// the last
		createGenerator = this.createGenerator(getEntityType(), getGeneratorType(),
				this.createConfiguration(generator.getFormDefinition(), ImmutableMap.of(
						IdentityEmailGenerator.EMAIL_SUFFIX, emailSuffix2)), 30, null);
		createGenerator.setDisabled(true);
		this.generatedAttributeService.save(createGenerator);

		IdmIdentityDto generatedDto = valueGeneratorManager.generate(identityDto);
		assertNotNull(generatedDto.getEmail());
		assertEquals(username + "@" + emailSuffix, generatedDto.getEmail());
	}

	@Override
	protected String getEntityType() {
		return IdmIdentity.class.getCanonicalName();
	}

	@Override
	protected String getGeneratorType() {
		return IdentityEmailGenerator.class.getCanonicalName();
	}
}
