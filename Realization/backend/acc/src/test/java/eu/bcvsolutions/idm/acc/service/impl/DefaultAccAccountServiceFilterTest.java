package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.test.TestHelper;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for Account filter
 * 
 * @author Patrik Stloukal
 *
 */
public class DefaultAccAccountServiceFilterTest extends AbstractIntegrationTest {

	@Autowired
	private AccAccountService accAccountService;
	@Autowired
	private TestHelper helper;
	@Autowired
	private AccIdentityAccountService identityAccoutnService;
	@Autowired
	private SysSystemAttributeMappingService attributeMappingService;

	@Before
	public void login() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testSystemId() {
		IdmIdentityDto identity = helper.createIdentity("test-" + System.currentTimeMillis());
		SysSystemDto system = helper.createTestResourceSystem(true);
		AccAccountDto account = createAccount(system.getId(), identity.getId(), identity.getUsername(),
				AccountType.PERSONAL, false);
		IdmIdentityDto identity2 = helper.createIdentity("test-" + System.currentTimeMillis());
		SysSystemDto system2 = helper.createTestResourceSystem(true);
		createAccount(system2.getId(), identity2.getId(), identity2.getUsername(), AccountType.PERSONAL, false);
		AccAccountFilter testFilter = new AccAccountFilter();
		testFilter.setSystemId(system.getId());
		Page<AccAccountDto> pages = accAccountService.find(testFilter, null);
		assertEquals(1, pages.getTotalElements());
		assertEquals(account.getId(), pages.getContent().get(0).getId());
	}

	@Test
	public void testIdentityId() {
		IdmIdentityDto identity = helper.createIdentity("test-" + System.currentTimeMillis());
		SysSystemDto system = helper.createTestResourceSystem(true);
		createAccount(system.getId(), identity.getId(), identity.getUsername(), AccountType.PERSONAL, false);
		IdmIdentityDto identity2 = helper.createIdentity("test-" + System.currentTimeMillis());
		SysSystemDto system2 = helper.createTestResourceSystem(true);
		createAccount(system2.getId(), identity.getId(), identity.getUsername(), AccountType.PERSONAL, false);
		AccAccountDto account3 = createAccount(system2.getId(), identity2.getId(), identity2.getUsername(),
				AccountType.PERSONAL, false);
		AccAccountFilter testFilter = new AccAccountFilter();
		testFilter.setIdentityId(identity2.getId());
		Page<AccAccountDto> pages = accAccountService.find(testFilter, null);
		assertEquals(1, pages.getTotalElements());
		assertEquals(account3.getId(), pages.getContent().get(0).getId());
	}

	@Test
	public void testUid() {
		IdmIdentityDto identity = helper.createIdentity("test-" + System.currentTimeMillis());
		SysSystemDto system = helper.createTestResourceSystem(true);
		AccAccountDto account = createAccount(system.getId(), identity.getId(), identity.getUsername(),
				AccountType.PERSONAL, false);
		IdmIdentityDto identity2 = helper.createIdentity("test-" + System.currentTimeMillis());
		SysSystemDto system2 = helper.createTestResourceSystem(true);
		createAccount(system2.getId(), identity2.getId(), identity2.getUsername(), AccountType.PERSONAL, false);
		AccAccountFilter testFilter = new AccAccountFilter();
		testFilter.setUid(account.getUid());
		Page<AccAccountDto> pages = accAccountService.find(testFilter, null);
		assertEquals(1, pages.getTotalElements());
		assertEquals(account.getId(), pages.getContent().get(0).getId());
	}

	@Test
	public void testAccountType() {
		IdmIdentityDto identity = helper.createIdentity("test-" + System.currentTimeMillis());
		SysSystemDto system = helper.createTestResourceSystem(true);
		createAccount(system.getId(), identity.getId(), identity.getUsername(), AccountType.PERSONAL, false);
		IdmIdentityDto identity2 = helper.createIdentity("test-" + System.currentTimeMillis());
		SysSystemDto system2 = helper.createTestResourceSystem(true);
		AccAccountDto account2 = createAccount(system2.getId(), identity2.getId(), identity2.getUsername(),
				AccountType.TECHNICAL, false);
		AccAccountFilter testFilter = new AccAccountFilter();
		testFilter.setAccountType(account2.getAccountType());
		Page<AccAccountDto> pages = accAccountService.find(testFilter, null);
		assertEquals(1, pages.getTotalElements());
		assertEquals(account2.getId(), pages.getContent().get(0).getId());
	}

	@Test
	public void testOwnership() {
		IdmIdentityDto identity = helper.createIdentity("test-" + System.currentTimeMillis());
		SysSystemDto system = helper.createTestResourceSystem(true);
		AccAccountDto account = createAccount(system.getId(), identity.getId(), identity.getUsername(),
				AccountType.PERSONAL, true);
		createAccount(system.getId(), identity.getId(), identity.getUsername() + "2", AccountType.PERSONAL, false);
		AccAccountFilter testFilter = new AccAccountFilter();
		testFilter.setOwnership(true);
		testFilter.setIdentityId(identity.getId());
		Page<AccAccountDto> pages = accAccountService.find(testFilter, null);
		assertEquals(1, pages.getTotalElements());
		assertEquals(account.getId(), pages.getContent().get(0).getId());
	}

	@Test
	public void testSystemEntityType() {
		IdmIdentityDto identity = helper.createIdentity("test-" + System.currentTimeMillis());
		SysSystemDto system = helper.createTestResourceSystem(true);
		AccAccountDto account = createAccount(system.getId(), identity.getId(), identity.getUsername(),
				AccountType.PERSONAL, false);
		IdmIdentityDto identity2 = helper.createIdentity("test-" + System.currentTimeMillis());
		SysSystemDto system2 = helper.createTestResourceSystem(true);
		createAccount(system2.getId(), identity2.getId(), identity2.getUsername(), AccountType.PERSONAL, false);
		AccAccountFilter testFilter = new AccAccountFilter();
		testFilter.setEntityType(account.getEntityType());
		testFilter.setSystemId(system.getId());
		Page<AccAccountDto> pages = accAccountService.find(testFilter, null);
		assertEquals(1, pages.getTotalElements());
		assertEquals(account.getId(), pages.getContent().get(0).getId());
	}
	
	@Test
	public void testSupportChangePassword() {
		IdmIdentityDto identity = helper.createIdentity("test-" + System.currentTimeMillis());
		SysSystemDto system = helper.createTestResourceSystem(true);
		AccAccountDto account = createAccount(system.getId(), identity.getId(), identity.getUsername(),
				AccountType.PERSONAL, false);
		IdmIdentityDto identity2 = helper.createIdentity("test-" + System.currentTimeMillis());
		SysSystemDto system2 = helper.createTestResourceSystem(true);
		createAccount(system2.getId(), identity2.getId(), identity2.getUsername(), AccountType.PERSONAL, false);
		AccAccountFilter testFilter = new AccAccountFilter();
		testFilter.setUid(identity.getUsername());
		testFilter.setSupportChangePassword(true);
		Page<AccAccountDto> pages = accAccountService.find(testFilter, null);
		assertEquals(1, pages.getTotalElements());
		assertEquals(account.getId(), pages.getContent().get(0).getId());
		//
		SysSystemMappingDto defaultMapping = helper.getDefaultMapping(system);
		List<SysSystemAttributeMappingDto> attributes = attributeMappingService.findBySystemMapping(defaultMapping);
		//
		for (SysSystemAttributeMappingDto attr : attributes) {
			if (attr.getName().equals(IcConnectorFacade.PASSWORD_ATTRIBUTE_NAME)) {
				attributeMappingService.delete(attr);
			}
		}
		//
		testFilter = new AccAccountFilter();
		testFilter.setUid(identity.getUsername());
		testFilter.setSupportChangePassword(true);
		pages = accAccountService.find(testFilter, null);
		assertEquals(0, pages.getTotalElements());
	}
	
	@Test
	public void testEventType() {
		SysSystemDto system = helper.createTestResourceSystem(true);		
		AccAccountDto accountOne = new AccAccountDto();
		accountOne.setSystem(system.getId());
		accountOne.setUid(UUID.randomUUID().toString());
		accountOne.setAccountType(AccountType.PERSONAL);
		accountOne.setEntityType(SystemEntityType.IDENTITY);
		accountOne = accAccountService.save(accountOne);
		
		AccAccountDto accountTwo = new AccAccountDto();
		accountTwo.setSystem(system.getId());
		accountTwo.setUid(UUID.randomUUID().toString());
		accountTwo.setAccountType(AccountType.PERSONAL);
		accountTwo.setEntityType(SystemEntityType.ROLE);
		accountTwo = accAccountService.save(accountTwo);
		//
		AccAccountFilter testFilter = new AccAccountFilter();
		testFilter.setId(accountOne.getId());
		testFilter.setEntityType(SystemEntityType.IDENTITY);
		List<AccAccountDto> accounts = accAccountService.find(testFilter, null).getContent();
		//
		Assert.assertEquals(1, accounts.size());
		Assert.assertEquals(accountOne.getUid(), accounts.get(0).getUid());
		//
		testFilter.setId(accountTwo.getId());
		accounts = accAccountService.find(testFilter, null).getContent();
		Assert.assertTrue(accounts.isEmpty());
	}

	private AccAccountDto createAccount(UUID systemId, UUID identityId, String uid, AccountType accountType,
			Boolean ownership) {
		AccAccountDto account = new AccAccountDto();
		account.setSystem(systemId);
		account.setUid(uid);
		account.setAccountType(accountType);
		account = accAccountService.save(account);

		AccIdentityAccountDto accountIdentity = new AccIdentityAccountDto();
		accountIdentity.setIdentity(identityId);
		accountIdentity.setOwnership(ownership);
		accountIdentity.setAccount(account.getId());

		identityAccoutnService.save(accountIdentity);
		return account;
	}

}
