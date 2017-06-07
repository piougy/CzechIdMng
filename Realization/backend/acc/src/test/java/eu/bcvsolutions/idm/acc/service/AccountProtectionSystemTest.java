package eu.bcvsolutions.idm.acc.service;

import java.util.UUID;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Protection account system
 * 
 * @author Svanda
 *
 */
public class AccountProtectionSystemTest extends AbstractIntegrationTest {

	private static final String ROLE_ONE = "role_one";
	private static final String IDENTITY_USERNAME = "protectionUserOne";
	private static final String IDENTITY_USERNAME_TWO = "protectionUserTwo";
	private static final String SYSTEM_NAME = "protectedSystem";
	private static final String EMAIL_ONE = "one.email@one.cz";
	private static final String EMAIL_TWO = "two.email@two.cz";

	@Autowired 
	private TestHelper helper;
	@Autowired
	private SysSystemService sysSystemService;
	@Autowired
	private IdmIdentityService idmIdentityService;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmIdentityContractService contractService;
	@Autowired
	private DataSource dataSource;

	@Transactional
	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		initData();
	}

	@After
	public void logout() {
		//resetData();
		super.logout();
	}

	@Transactional
	@Test
	public void accountWithoutProtectionTest() {

		IdmIdentityDto identity = idmIdentityService.getByUsername(IDENTITY_USERNAME);
		IdmRole roleOne = roleService.getByName(ROLE_ONE);
		SysSystem system = sysSystemService.getByCode(SYSTEM_NAME);
		IdmIdentityRoleDto identityRole = assignRole(identity, roleOne);

		AccAccount account = accountService.getAccount(IDENTITY_USERNAME, system.getId());

		Assert.assertNotNull(account);
		Assert.assertFalse(account.isInProtection());
		TestResource createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());

		// Remove role from identity
		identityRoleService.deleteById(identityRole.getId());

		account = accountService.getAccount(IDENTITY_USERNAME, system.getId());

		Assert.assertNull(account);
		createdAccount = entityManager.find(TestResource.class, IDENTITY_USERNAME);
		Assert.assertNull(createdAccount);

	}

	@Transactional
	@Test
	public void accountWithProtectionTest() {

		IdmIdentityDto identity = idmIdentityService.getByUsername(IDENTITY_USERNAME);
		IdmRole roleOne = roleService.getByName(ROLE_ONE);
		SysSystem system = sysSystemService.getByCode(SYSTEM_NAME);

		// Set system to protected mode
		SysSystemMapping mapping = systemMappingService
				.findBySystem(system, SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY).get(0);
		mapping.setProtectionEnabled(Boolean.TRUE);
		mapping.setProtectionInterval(null);
		systemMappingService.save(mapping);

		IdmIdentityRoleDto identityRole = assignRole(identity, roleOne);

		AccAccount account = accountService.getAccount(IDENTITY_USERNAME, system.getId());

		Assert.assertNotNull(account);
		Assert.assertFalse(account.isInProtection());
		TestResource createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());

		// Remove role from identity
		identityRoleService.deleteById(identityRole.getId());

		account = accountService.getAccount(IDENTITY_USERNAME, system.getId());
		Assert.assertNotNull(account);
		Assert.assertTrue(account.isInProtection());
		Assert.assertNull(account.getEndOfProtection());
		createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());
	}

	@Transactional
	@Test
	public void accountWithProtectionRetryTest() {

		IdmIdentityDto identity = idmIdentityService.getByUsername(IDENTITY_USERNAME);
		IdmRole roleOne = roleService.getByName(ROLE_ONE);
		SysSystem system = sysSystemService.getByCode(SYSTEM_NAME);

		// Set system to protected mode
		SysSystemMapping mapping = systemMappingService
				.findBySystem(system, SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY).get(0);
		mapping.setProtectionEnabled(Boolean.TRUE);
		mapping.setProtectionInterval(null);
		systemMappingService.save(mapping);

		IdmIdentityRoleDto identityRole = assignRole(identity, roleOne);

		AccAccount account = accountService.getAccount(IDENTITY_USERNAME, system.getId());

		// Remove role from identity
		identityRoleService.deleteById(identityRole.getId());

		account = accountService.getAccount(IDENTITY_USERNAME, system.getId());
		Assert.assertNotNull(account);
		Assert.assertTrue(account.isInProtection());
		Assert.assertNull(account.getEndOfProtection());
		TestResource createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());

		// We again assign same role
		identityRole = assignRole(identity, roleOne);

		// Account must be unprotected
		Assert.assertNotNull(account);
		Assert.assertFalse(account.isInProtection());
		createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());
	}

	@Transactional
	@Test
	public void accountWithProtectionAndIntervalTest() {

		IdmIdentityDto identity = idmIdentityService.getByUsername(IDENTITY_USERNAME);
		IdmRole roleOne = roleService.getByName(ROLE_ONE);
		SysSystem system = sysSystemService.getByCode(SYSTEM_NAME);

		int intervalInDays = 10;

		// Set system to protected mode
		SysSystemMapping mapping = systemMappingService
				.findBySystem(system, SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY).get(0);
		mapping.setProtectionEnabled(Boolean.TRUE);
		mapping.setProtectionInterval(intervalInDays);
		systemMappingService.save(mapping);

		IdmIdentityRoleDto identityRole = assignRole(identity, roleOne);

		AccAccount account = accountService.getAccount(IDENTITY_USERNAME, system.getId());

		Assert.assertNotNull(account);
		Assert.assertFalse(account.isInProtection());
		TestResource createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());

		// Remove role from identity
		identityRoleService.deleteById(identityRole.getId());

		account = accountService.getAccount(IDENTITY_USERNAME, system.getId());
		Assert.assertNotNull(account);
		Assert.assertTrue(account.isInProtection());
		Assert.assertNotNull(account.getEndOfProtection());
		Assert.assertTrue(account.getEndOfProtection().toLocalDate().isEqual(LocalDate.now().plusDays(intervalInDays)));
		createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());
	}

	/**
	 * When is account in protection mode, then cannot be provisioned.
	 */
	@Transactional
	@Test
	public void protectedAccountNoProvisioningTest() {

		IdmIdentityDto identity = idmIdentityService.getByUsername(IDENTITY_USERNAME);
		IdmRole roleOne = roleService.getByName(ROLE_ONE);
		SysSystem system = sysSystemService.getByCode(SYSTEM_NAME);

		int intervalInDays = 10;

		// Set system to protected mode
		SysSystemMapping mapping = systemMappingService
				.findBySystem(system, SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY).get(0);
		mapping.setProtectionEnabled(Boolean.TRUE);
		mapping.setProtectionInterval(intervalInDays);
		systemMappingService.save(mapping);
		
		String changedValue = "changed";
		identity.setFirstName(changedValue);
		idmIdentityService.save(identity);

		IdmIdentityRoleDto identityRole = assignRole(identity, roleOne);

		AccAccount account = accountService.getAccount(IDENTITY_USERNAME, system.getId());

		Assert.assertNotNull(account);
		Assert.assertFalse(account.isInProtection());
		TestResource createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(changedValue, createdAccount.getFirstname());

		// Remove role from identity
		identityRoleService.deleteById(identityRole.getId());

		account = accountService.getAccount(IDENTITY_USERNAME, system.getId());
		Assert.assertNotNull(account);
		Assert.assertTrue(account.isInProtection());
		Assert.assertNotNull(account.getEndOfProtection());
		Assert.assertTrue(account.getEndOfProtection().toLocalDate().isEqual(LocalDate.now().plusDays(intervalInDays)));
		createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());

		// Change first name and emit provisioning (provisioning must be break)
		identity.setFirstName(IDENTITY_USERNAME);
		idmIdentityService.save(identity);

		createdAccount = helper.findResource(account.getUid());
		Assert.assertNotEquals(identity.getFirstName(), createdAccount.getFirstname());
	}
	
	/**
	 * When is account in protection mode, then cannot be deleted.
	 */
	@Transactional
	@Test(expected=ResultCodeException.class)
	public void protectedAccountDeleteTest() {

		IdmIdentityDto identity = idmIdentityService.getByUsername(IDENTITY_USERNAME);
		IdmRole roleOne = roleService.getByName(ROLE_ONE);
		SysSystem system = sysSystemService.getByCode(SYSTEM_NAME);

		// Set system to protected mode
		SysSystemMapping mapping = systemMappingService
				.findBySystem(system, SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY).get(0);
		mapping.setProtectionEnabled(Boolean.TRUE);
		mapping.setProtectionInterval(null);
		systemMappingService.save(mapping);
		

		IdmIdentityRoleDto identityRole = assignRole(identity, roleOne);

		AccAccount account = accountService.getAccount(IDENTITY_USERNAME, system.getId());

		Assert.assertNotNull(account);
		Assert.assertFalse(account.isInProtection());
		TestResource createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);

		// Remove role from identity
		identityRoleService.deleteById(identityRole.getId());

		account = accountService.getAccount(IDENTITY_USERNAME, system.getId());
		Assert.assertNotNull(account);
		Assert.assertTrue(account.isInProtection());
		Assert.assertNull(account.getEndOfProtection());
		createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());
		
		// Delete AccAccount directly
		accountService.delete(account);
	}
	
	/**
	 * When is account in protection mode (but expired), then can be deleted.
	 */
	@Transactional
	@Test()
	public void protectedAccountExpiredDeleteTest() {

		IdmIdentityDto identity = idmIdentityService.getByUsername(IDENTITY_USERNAME);
		IdmRole roleOne = roleService.getByName(ROLE_ONE);
		SysSystem system = sysSystemService.getByCode(SYSTEM_NAME);

		// Set system to protected mode
		SysSystemMapping mapping = systemMappingService
				.findBySystem(system, SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY).get(0);
		mapping.setProtectionEnabled(Boolean.TRUE);
		mapping.setProtectionInterval(null);
		systemMappingService.save(mapping);
		

		IdmIdentityRoleDto identityRole = assignRole(identity, roleOne);

		AccAccount account = accountService.getAccount(IDENTITY_USERNAME, system.getId());

		Assert.assertNotNull(account);
		Assert.assertFalse(account.isInProtection());
		TestResource createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);

		// Remove role from identity
		identityRoleService.deleteById(identityRole.getId());

		account = accountService.getAccount(IDENTITY_USERNAME, system.getId());
		Assert.assertNotNull(account);
		Assert.assertTrue(account.isInProtection());
		Assert.assertNull(account.getEndOfProtection());
		createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());
		
		// Set account as expired
		account.setEndOfProtection(DateTime.now().minusMonths(1));
		account = accountService.save(account);
		
		// Delete AccAccount directly
		accountService.delete(account);
		account = accountService.getAccount(IDENTITY_USERNAME, system.getId());
		Assert.assertNull(account);
		createdAccount = helper.findResource(IDENTITY_USERNAME);
		Assert.assertNull(createdAccount);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public TestResource findAccountOnTargetSystem(String uid) {
		return entityManager.find(TestResource.class, uid);
	}

	private IdmIdentityRoleDto assignRole(IdmIdentityDto identity, IdmRole roleOne) {
		UUID contractId = contractService.getPrimeContract(identity.getId()).getId();
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		identityRole.setIdentityContract(contractId);
		identityRole.setRole(roleOne.getId());
		identityRole = identityRoleService.save(identityRole);
		return identityRole;
	}

	private void initData() {
	
		IdmIdentityDto identity;

		// create test system
		SysSystem system = helper.createSystem(TestResource.TABLE_NAME, true);
		system.setName(SYSTEM_NAME);
		system = sysSystemService.save(system);


		// Create test identity for provisioning test
		identity = new IdmIdentityDto();
		identity.setUsername(IDENTITY_USERNAME);
		identity.setFirstName(IDENTITY_USERNAME);
		identity.setLastName(IDENTITY_USERNAME);
		identity.setEmail(EMAIL_ONE);
		identity = idmIdentityService.save(identity);

		IdmIdentityDto identityTwo = new IdmIdentityDto();
		identityTwo.setUsername(IDENTITY_USERNAME_TWO);
		identityTwo.setFirstName(IDENTITY_USERNAME_TWO);
		identityTwo.setLastName(IDENTITY_USERNAME_TWO);
		identityTwo.setEmail(EMAIL_TWO);
		identityTwo = idmIdentityService.save(identityTwo);
		/*
		 * Create role with link on system (default)
		 */
		IdmRole roleDefault = new IdmRole();
		roleDefault.setName(ROLE_ONE);
		roleService.save(roleDefault);
		
		helper.createRoleSystem(roleDefault, system);
	}
}
