package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.AbstractPasswordFilterIntegrationTest;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordChangeOptionDto;
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccUniformPasswordFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccUniformPasswordSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.AccUniformPasswordService;
import eu.bcvsolutions.idm.acc.service.api.AccUniformPasswordSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;

/**
 * Test for {@link DefaultAccUniformPasswordService} more tests was created in {@link DefaultPasswordFilterManagerIntegrationTest}
 *
 * @author Ondrej Kopr
 *
 */
public class DefaultAccUniformPasswordServiceTest extends AbstractPasswordFilterIntegrationTest {

	@Autowired
	private AccUniformPasswordService uniformPasswordService;
	@Autowired
	private AccUniformPasswordSystemService uniformPasswordSystemService;

	@Test
	public void testFilterSystemId() {
		SysSystemDto system = createSystem(true);
		SysSystemDto systemTwo = createSystem(true);
		AccUniformPasswordDto uniformDefinition = createUniformDefinition(true);
		assignSystem(uniformDefinition, system, systemTwo);

		AccUniformPasswordDto uniformDefinitionTwo = createUniformDefinition(true);
		assignSystem(uniformDefinitionTwo, systemTwo);

		AccUniformPasswordFilter filter = new AccUniformPasswordFilter();
		filter.setSystemId(system.getId());
		List<AccUniformPasswordDto> uniform = uniformPasswordService.find(filter, null).getContent();
		assertEquals(1, uniform.size());
		AccUniformPasswordDto uniformPasswordDto = uniform.get(0);
		assertEquals(uniformDefinition.getId(), uniformPasswordDto.getId());
	}

	@Test
	public void testFilterByCode() {
		createUniformDefinition(true);
		AccUniformPasswordDto uniformDefinitionTwo = createUniformDefinition(true);
		createUniformDefinition(true);

		AccUniformPasswordFilter filter = new AccUniformPasswordFilter();
		filter.setCodeableIdentifier(uniformDefinitionTwo.getCode());
		List<AccUniformPasswordDto> uniform = uniformPasswordService.find(filter, null).getContent();
		assertEquals(1, uniform.size());
		AccUniformPasswordDto uniformPasswordDto = uniform.get(0);
		assertEquals(uniformDefinitionTwo.getId(), uniformPasswordDto.getId());
	}

	@Test
	public void testFilterUniformSystemSystemId() {
		SysSystemDto system = createSystem(true);
		SysSystemDto systemTwo = createSystem(true);
		AccUniformPasswordDto uniformDefinition = createUniformDefinition(true);
		assignSystem(uniformDefinition, system, systemTwo);

		AccUniformPasswordDto uniformDefinitionTwo = createUniformDefinition(true);
		assignSystem(uniformDefinitionTwo, systemTwo);

		AccUniformPasswordSystemFilter filter = new AccUniformPasswordSystemFilter();
		filter.setSystemId(system.getId());
		List<AccUniformPasswordSystemDto> content = uniformPasswordSystemService.find(filter, null).getContent();
		assertEquals(1, content.size());

		filter.setSystemId(systemTwo.getId());
		content = uniformPasswordSystemService.find(filter, null).getContent();
		assertEquals(2, content.size());
	}

	@Test
	public void testFindOptionsForIdentityOneWithoutUniform() {
		SysSystemDto system = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);

		List<AccPasswordChangeOptionDto> options = uniformPasswordService.findOptionsForPasswordChange(identity);
		assertEquals(1, options.size());
		AccPasswordChangeOptionDto option = options.get(0);
		assertEquals(1, option.getAccounts().size());
		String string = option.getAccounts().get(0);
		AccAccountDto account = getAccount(identity, system);
		assertEquals(account.getId().toString(), string);
	}

	@Test
	public void testFindOptionsForIdentityMoreWithoutUniform() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		SysSystemDto systemThree = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);

		assignSystem(identity, systemTwo);
		assignSystem(identity, systemThree);

		List<AccPasswordChangeOptionDto> options = uniformPasswordService.findOptionsForPasswordChange(identity);
		assertEquals(3, options.size());

		AccAccountDto account = getAccount(identity, system);
		AccAccountDto accountTwo = getAccount(identity, systemTwo);
		AccAccountDto accountThree = getAccount(identity, systemThree);

		options.forEach(option -> {
			assertEquals(1, option.getAccounts().size());
			UUID uuid = UUID.fromString(option.getAccounts().get(0));

			assertFalse(option.isChangeInIdm());

			if (!(uuid.equals(account.getId()) ||
					uuid.equals(accountTwo.getId()) ||
					uuid.equals(accountThree.getId()))) {
				fail();
			}
		});
	}

	@Test
	public void testFindOptionsForIdentityMoreWithUniform() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		SysSystemDto systemThree = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);

		assignSystem(identity, systemTwo);
		assignSystem(identity, systemThree);
		
		assignSystem(createUniformDefinition(false), system, systemTwo, systemThree);

		List<AccPasswordChangeOptionDto> options = uniformPasswordService.findOptionsForPasswordChange(identity);
		assertEquals(1, options.size());

		AccAccountDto account = getAccount(identity, system);
		AccAccountDto accountTwo = getAccount(identity, systemTwo);
		AccAccountDto accountThree = getAccount(identity, systemThree);
		
		AccPasswordChangeOptionDto option = options.get(0);
		assertFalse(option.isChangeInIdm());
		assertEquals(3, option.getAccounts().size());

		option.getAccounts().forEach(acc -> {
			UUID uuid = UUID.fromString(acc);

			if (!(uuid.equals(account.getId()) ||
					uuid.equals(accountTwo.getId()) ||
					uuid.equals(accountThree.getId()))) {
				fail();
			}
		});
	}

	@Test
	public void testFindOptionsSameSystemInTwoDefinition() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		SysSystemDto systemThree = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);

		assignSystem(identity, systemTwo);
		assignSystem(identity, systemThree);
		
		assignSystem(createUniformDefinition(false), system, systemTwo, systemThree);
		assignSystem(createUniformDefinition(true), systemTwo);

		List<AccPasswordChangeOptionDto> options = uniformPasswordService.findOptionsForPasswordChange(identity);
		assertEquals(2, options.size());
	}

	@Test
	public void testFindOptionsSystemSameUniformDefinition() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		SysSystemDto systemThree = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);

		assignSystem(identity, systemTwo);
		assignSystem(identity, systemThree);
		
		assignSystem(createUniformDefinition(true), system, systemTwo, systemThree);
		assignSystem(createUniformDefinition(true), systemTwo);
		assignSystem(createUniformDefinition(true), system);

		List<AccPasswordChangeOptionDto> options = uniformPasswordService.findOptionsForPasswordChange(identity);
		assertEquals(3, options.size());
	}

	@Test
	public void testFilterByDisabledWithTrue() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		SysSystemDto systemThree = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);

		assignSystem(identity, systemTwo);
		assignSystem(identity, systemThree);

		AccUniformPasswordDto createUniformDefinition = createUniformDefinition(true);
		createUniformDefinition.setDisabled(true);
		createUniformDefinition = uniformPasswordService.save(createUniformDefinition);

		assignSystem(createUniformDefinition(true), system, systemTwo, systemThree);
		assignSystem(createUniformDefinition, systemTwo);
		assignSystem(createUniformDefinition(true), system);

		AccUniformPasswordSystemFilter filter = new AccUniformPasswordSystemFilter();
		filter.setSystemId(systemTwo.getId());
		filter.setUniformPasswordDisabled(true);
		List<AccUniformPasswordSystemDto> content = uniformPasswordSystemService.find(filter, null).getContent();
		assertEquals(1, content.size());

		AccUniformPasswordSystemDto accUniformPasswordSystemDto = content.get(0);
		assertEquals(createUniformDefinition.getId(), accUniformPasswordSystemDto.getUniformPassword());
	}

	@Test
	public void testFilterByDisabledWithFalse() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		SysSystemDto systemThree = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);

		assignSystem(identity, systemTwo);
		assignSystem(identity, systemThree);

		AccUniformPasswordDto createUniformDefinition = createUniformDefinition(true);
		createUniformDefinition.setDisabled(true);
		createUniformDefinition = uniformPasswordService.save(createUniformDefinition);

		AccUniformPasswordDto createUniformDefinitionSecond = createUniformDefinition(true);

		assignSystem(createUniformDefinitionSecond, system, systemTwo, systemThree);
		assignSystem(createUniformDefinition, systemTwo);
		assignSystem(createUniformDefinition(true), system);

		AccUniformPasswordSystemFilter filter = new AccUniformPasswordSystemFilter();
		filter.setSystemId(systemTwo.getId());
		filter.setUniformPasswordDisabled(false);
		List<AccUniformPasswordSystemDto> content = uniformPasswordSystemService.find(filter, null).getContent();
		assertEquals(1, content.size());

		AccUniformPasswordSystemDto accUniformPasswordSystemDto = content.get(0);
		assertEquals(createUniformDefinitionSecond.getId(), accUniformPasswordSystemDto.getUniformPassword());
	}

	@Test
	public void testFilterByDisabledWithNull() {
		SysSystemDto system = createSystem(false);
		SysSystemDto systemTwo = createSystem(false);
		SysSystemDto systemThree = createSystem(false);
		IdmIdentityDto identity = createIdentity(system);

		assignSystem(identity, systemTwo);
		assignSystem(identity, systemThree);

		AccUniformPasswordDto createUniformDefinition = createUniformDefinition(true);
		createUniformDefinition.setDisabled(true);
		createUniformDefinition = uniformPasswordService.save(createUniformDefinition);

		AccUniformPasswordDto createUniformDefinitionSecond = createUniformDefinition(true);

		assignSystem(createUniformDefinitionSecond, system, systemTwo, systemThree);
		assignSystem(createUniformDefinition, systemTwo);
		assignSystem(createUniformDefinition(true), system);

		AccUniformPasswordSystemFilter filter = new AccUniformPasswordSystemFilter();
		filter.setSystemId(systemTwo.getId());
		filter.setUniformPasswordDisabled(null);
		List<AccUniformPasswordSystemDto> content = uniformPasswordSystemService.find(filter, null).getContent();
		assertEquals(2, content.size());
	}
}
