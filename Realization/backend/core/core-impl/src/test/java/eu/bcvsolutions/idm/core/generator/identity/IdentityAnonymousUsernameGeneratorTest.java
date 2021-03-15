package eu.bcvsolutions.idm.core.generator.identity;


import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.ValueGeneratorDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.generator.AbstractGeneratorTest;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;

/**
 * Tests for {@link IdentityAnonymousUsernameGenerator}
 *
 * @author Ondrej Husnik
 *
 */
public class IdentityAnonymousUsernameGeneratorTest extends AbstractGeneratorTest {

	@Autowired
	private IdmIdentityService identityService;
	
	
	/**
	 * Test that generator is able to find remaining unused numbers 
	 */
	@Test
	//@Ignore
	public void find3AvailableUsernameOutOf100() {
		int genPartLen = 2;
		int genPartMax = IdentityAnonymousUsernameGenerator.calcMaxValueForLen(genPartLen);
		String prefix = "test_";

		// init existing users
		Set<Integer> excluded = new HashSet<Integer>();
		excluded.add(15);
		excluded.add(50);
		excluded.add(95);
		generateTestUsers(prefix, genPartMax, genPartLen, excluded);
		
		
		ValueGeneratorDto generator = getGenerator();
		this.createGenerator(getDtoType(), getGeneratorType(),
				this.createConfiguration(generator.getFormDefinition(), ImmutableMap.of(
						IdentityAnonymousUsernameGenerator.USERNAME_PREFIX, prefix,
						IdentityAnonymousUsernameGenerator.GENERATED_NUMBER_LENGTH, String.valueOf(genPartLen))), 1, Boolean.FALSE);
		
		
		for (int i=0; i < excluded.size(); i++) {
			IdmIdentityDto identityDto = new IdmIdentityDto();
			IdmIdentityDto generatedDto = valueGeneratorManager.generate(identityDto);
			identityService.save(generatedDto);
		}
		
		for (Integer val : excluded) {
			IdmIdentityFilter identFilt = new IdmIdentityFilter();
			identFilt.setUsername(IdentityAnonymousUsernameGenerator.createUsername(prefix, val, genPartLen));
			List<IdmIdentityDto> results = identityService.find(identFilt, null).getContent();
			Assert.assertEquals(1, results.size());
		}
	}
	
	/**
	 * This test generates defined number of identities with generated username.
	 * It can be parameterized with genPartLen specifying the length of numeric part and
	 * generatedIndentitiesNum saying number of generated users during test. Both has to conform formula
	 * generatedIndentitiesNum <= 10^genPartLen (it's power not xor).
	 */
	@Test
	//@Ignore
	public void generateNumberOfIdentities() {
		int genPartLen = 2;
		int generatedIndentitiesNum = 100;
		String prefix = getHelper().createName()+"_";

		
		ValueGeneratorDto generator = getGenerator();
		this.createGenerator(getDtoType(), getGeneratorType(),
				this.createConfiguration(generator.getFormDefinition(), ImmutableMap.of(
						IdentityAnonymousUsernameGenerator.USERNAME_PREFIX, prefix,
						IdentityAnonymousUsernameGenerator.GENERATED_NUMBER_LENGTH, String.valueOf(genPartLen))), 1, Boolean.FALSE);
		
		for (int i=0; i < generatedIndentitiesNum; i++) {
			IdmIdentityDto identityDto = new IdmIdentityDto();
			try {
				IdmIdentityDto generatedDto = valueGeneratorManager.generate(identityDto);
				identityService.save(generatedDto);
				//System.out.println(String.valueOf(i)+" - generated: " + generatedDto.getUsername());
			} catch (ResultCodeException ex) {
				// we don't care solved in following Asserts
			}
		}
		
		IdmIdentityFilter identFilt = new IdmIdentityFilter();
		identFilt.setText(prefix);
		List<IdmIdentityDto> results = identityService.find(identFilt, null).getContent();
		Assert.assertTrue(0.99*generatedIndentitiesNum < results.size()); // at least 99% has to be found 
		Set<String> usernameSet = results.stream().map(IdmIdentityDto::getUsername).collect(Collectors.toSet());
		Assert.assertEquals(results.size(), usernameSet.size());
	}
	
	/**
	 * Test that generator is able to cope with the situation when no prefix is used
	 */
	@Test
	//@Ignore
	public void generateUsernameWithoutPrefix() {
		//existing users
		List<String> existingUsers = Arrays.asList("testUserXXX01","testUserXXX02","testUserXXX03","00","01",/*"2",*/"03","04","05","06",/*"7",*/"08","09");
		existingUsers.forEach(user -> {
			identityService.save(new IdmIdentityDto(user));
		});
		
		int genPartLen = 2;
		String prefix = "";
		int generatedIndentitiesNum = 100;
		int unavailableIndexCount = 0;
		
		ValueGeneratorDto generator = getGenerator();
		this.createGenerator(getDtoType(), getGeneratorType(),
				this.createConfiguration(generator.getFormDefinition(), ImmutableMap.of(
						IdentityAnonymousUsernameGenerator.USERNAME_PREFIX, prefix,
						IdentityAnonymousUsernameGenerator.GENERATED_NUMBER_LENGTH, String.valueOf(genPartLen))), 1, Boolean.FALSE);

		for (int i=0; i < generatedIndentitiesNum; i++) {
			IdmIdentityDto identityDto = new IdmIdentityDto();
			try {
				IdmIdentityDto generatedDto = valueGeneratorManager.generate(identityDto);
				identityService.save(generatedDto);
			} catch (ResultCodeException ex) {
				unavailableIndexCount++;
			}
			
		}
		List<IdmIdentityDto> dtos = identityService.find(null).getContent();
		Set<String> relevantDtos = dtos.stream().map(IdmIdentityDto::getUsername)
				.filter(IdentityAnonymousUsernameGenerator.usernameFilterFactory(prefix, genPartLen))
				.collect(Collectors.toSet());
		
		Assert.assertEquals(generatedIndentitiesNum, relevantDtos.size());
		Assert.assertEquals(8, unavailableIndexCount);
	} 

	/*
	 * Tests that length of numbers matter. It means 07 is not same as 7
	 */
	@Test
	public void numericPartLengthMatterTest() {
		//existing users
		List<String> existingUsers = Arrays.asList("testUserXXX0","testUserXXX1","testUserXXX2","testUserXXX3","testUserXXX4"
				,"testUserXXX5","testUserXXX6"/*"testUserXXX7"*/ ,"testUserXXX8","testUserXXX9", "testUserXXX07");
		existingUsers.forEach(user -> {
			identityService.save(new IdmIdentityDto(user));
		});
		
		int genPartLen = 1;
		String prefix = "testUserXXX";
		
		ValueGeneratorDto generator = getGenerator();
		this.createGenerator(getDtoType(), getGeneratorType(),
				this.createConfiguration(generator.getFormDefinition(), ImmutableMap.of(
						IdentityAnonymousUsernameGenerator.USERNAME_PREFIX, prefix,
						IdentityAnonymousUsernameGenerator.GENERATED_NUMBER_LENGTH, String.valueOf(genPartLen))), 1, Boolean.FALSE);

		IdmIdentityDto identityDto = new IdmIdentityDto();
		try {
			IdmIdentityDto generatedDto = valueGeneratorManager.generate(identityDto);
			identityDto = identityService.save(generatedDto);
		} catch (ResultCodeException ex) {
			fail();
		}
		Assert.assertEquals("testUserXXX7", identityDto.getUsername());
	}
	
	/**
	 * Numerical prefix works too
	 * 
	 */
	@Test
	public void numericPrefixWorksTest() {
		int genPartLen = 1;
		String prefix = "999";
		//existing users
		for (int i=0; i<10; ++i) {
			if (i != 7) {
				identityService.save(new IdmIdentityDto(prefix+i));	
			}
		}
		
		ValueGeneratorDto generator = getGenerator();
		this.createGenerator(getDtoType(), getGeneratorType(),
				this.createConfiguration(generator.getFormDefinition(), ImmutableMap.of(
						IdentityAnonymousUsernameGenerator.USERNAME_PREFIX, prefix,
						IdentityAnonymousUsernameGenerator.GENERATED_NUMBER_LENGTH, String.valueOf(genPartLen))), 1, Boolean.FALSE);

		IdmIdentityDto identityDto = new IdmIdentityDto();
		try {
			IdmIdentityDto generatedDto = valueGeneratorManager.generate(identityDto);
			identityDto = identityService.save(generatedDto);
		} catch (ResultCodeException ex) {
			fail();
		}
		Assert.assertEquals(prefix+"7", identityDto.getUsername());
	}
	
	/**
	 * Characters which could make troubles in used regex
	 */
	@Test
	public void potentiallyProblematicPrefixCharsTest() {
		int genPartLen = 1;
		String prefix = "~!@#$%^&*()_+_)*&^%$#@!~";
		//existing users
		for (int i=0; i<10; ++i) {
			if (i != 7) {
				identityService.save(new IdmIdentityDto(prefix+i));	
			}
		}
		
		ValueGeneratorDto generator = getGenerator();
		this.createGenerator(getDtoType(), getGeneratorType(),
				this.createConfiguration(generator.getFormDefinition(), ImmutableMap.of(
						IdentityAnonymousUsernameGenerator.USERNAME_PREFIX, prefix,
						IdentityAnonymousUsernameGenerator.GENERATED_NUMBER_LENGTH, String.valueOf(genPartLen))), 1, Boolean.FALSE);

		IdmIdentityDto identityDto = new IdmIdentityDto();
		try {
			IdmIdentityDto generatedDto = valueGeneratorManager.generate(identityDto);
			identityDto = identityService.save(generatedDto);
		} catch (ResultCodeException ex) {
			fail();
		}
		Assert.assertEquals(prefix+"7", identityDto.getUsername());
	}


	@Override
	protected Class<? extends AbstractDto> getDtoType() {
		return IdmIdentityDto.class;
	}

	@Override
	protected String getGeneratorType() {
		return  IdentityAnonymousUsernameGenerator.class.getCanonicalName();
	}
	
	private void generateTestUsers(String usernamePrefix, int count, int genPartLen, Collection<Integer> excludedNumbers) {
		for (int i=0; i<=count; ++i) {
			if (excludedNumbers.contains(i)) {
				continue;
			}
			String username = IdentityAnonymousUsernameGenerator.createUsername(usernamePrefix, i, genPartLen);
			IdmIdentityDto identity = new IdmIdentityDto(username);
			identity.setFirstName(getHelper().createName());
			identity.setLastName(getHelper().createName());
			identityService.save(identity);
		}
		
	}
}
