package eu.bcvsolutions.idm.acc.service;

import javax.persistence.EntityManager;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

	private static String ROLE_ONE;

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

	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void accountWithoutProtectionTest() {

		IdmIdentityDto identity = helper.createIdentity();
		SysSystem system = initSystem();
		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, roleService.getByCode(ROLE_ONE));

		AccAccount account = accountService.getAccount(identity.getUsername(), system.getId());

		Assert.assertNotNull(account);
		Assert.assertFalse(account.isInProtection());
		TestResource createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());

		// Remove role from identity
		identityRoleService.deleteById(identityRole.getId());

		account = accountService.getAccount(identity.getUsername(), system.getId());

		Assert.assertNull(account);
		createdAccount = entityManager.find(TestResource.class, identity.getUsername());
		Assert.assertNull(createdAccount);
	}

	@Test
	public void accountWithProtectionTest() {
		IdmIdentityDto identity = helper.createIdentity();
		SysSystem system = initSystem();
		IdmRole roleOne = roleService.getByCode(ROLE_ONE);

		// Set system to protected mode
		SysSystemMapping mapping = systemMappingService
				.findBySystem(system, SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY).get(0);
		mapping.setProtectionEnabled(Boolean.TRUE);
		mapping.setProtectionInterval(null);
		systemMappingService.save(mapping);

		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, roleOne);

		AccAccount account = accountService.getAccount(identity.getUsername(), system.getId());

		Assert.assertNotNull(account);
		Assert.assertFalse(account.isInProtection());
		TestResource createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());

		// Remove role from identity
		identityRoleService.deleteById(identityRole.getId());

		account = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNotNull(account);
		Assert.assertTrue(account.isInProtection());
		Assert.assertNull(account.getEndOfProtection());
		createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());
	}

	@Test
	public void accountWithProtectionRetryTest() {

		IdmIdentityDto identity = helper.createIdentity();
		SysSystem system = initSystem();
		IdmRole roleOne = roleService.getByCode(ROLE_ONE);

		// Set system to protected mode
		SysSystemMapping mapping = helper.getDefaultMapping(system);
		mapping.setProtectionInterval(null);
		mapping.setProtectionEnabled(true);
		systemMappingService.save(mapping);

		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, roleOne);

		AccAccount account = accountService.getAccount(identity.getUsername(), system.getId());

		// Remove role from identity
		identityRoleService.deleteById(identityRole.getId());

		account = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNotNull(account);
		Assert.assertTrue(account.isInProtection());
		Assert.assertNull(account.getEndOfProtection());
		TestResource createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());

		// We again assign same role
		identityRole = helper.createIdentityRole(identity, roleOne);

		// Account must be unprotected
		account = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNotNull(account);
		Assert.assertFalse(account.isInProtection());
		createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());
	}

	@Test
	public void accountWithProtectionAndIntervalTest() {

		IdmIdentityDto identity = helper.createIdentity();
		SysSystem system = initSystem();
		IdmRole roleOne = roleService.getByCode(ROLE_ONE);

		int intervalInDays = 10;

		// Set system to protected mode
		SysSystemMapping mapping = systemMappingService
				.findBySystem(system, SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY).get(0);
		mapping.setProtectionEnabled(Boolean.TRUE);
		mapping.setProtectionInterval(intervalInDays);
		systemMappingService.save(mapping);

		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, roleOne);

		AccAccount account = accountService.getAccount(identity.getUsername(), system.getId());

		Assert.assertNotNull(account);
		Assert.assertFalse(account.isInProtection());
		TestResource createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());

		// Remove role from identity
		identityRoleService.deleteById(identityRole.getId());

		account = accountService.getAccount(identity.getUsername(), system.getId());
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
	@Test
	public void protectedAccountNoProvisioningTest() {

		IdmIdentityDto identity = helper.createIdentity();
		SysSystem system = initSystem();
		IdmRole roleOne = roleService.getByCode(ROLE_ONE);

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

		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, roleOne);

		AccAccount account = accountService.getAccount(identity.getUsername(), system.getId());

		Assert.assertNotNull(account);
		Assert.assertFalse(account.isInProtection());
		TestResource createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(changedValue, createdAccount.getFirstname());

		// Remove role from identity
		identityRoleService.deleteById(identityRole.getId());

		account = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNotNull(account);
		Assert.assertTrue(account.isInProtection());
		Assert.assertNotNull(account.getEndOfProtection());
		Assert.assertTrue(account.getEndOfProtection().toLocalDate().isEqual(LocalDate.now().plusDays(intervalInDays)));
		createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());

		// Change first name and emit provisioning (provisioning must be break)
		identity.setFirstName(identity.getUsername());
		idmIdentityService.save(identity);

		createdAccount = helper.findResource(account.getUid());
		Assert.assertNotEquals(identity.getFirstName(), createdAccount.getFirstname());
	}
	
	/**
	 * When is account in protection mode, then cannot be deleted.
	 */
	@Test(expected=ResultCodeException.class)
	public void protectedAccountDeleteTest() {

		IdmIdentityDto identity = helper.createIdentity();
		SysSystem system = initSystem();
		IdmRole roleOne = roleService.getByCode(ROLE_ONE);

		// Set system to protected mode
		SysSystemMapping mapping = systemMappingService
				.findBySystem(system, SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY).get(0);
		mapping.setProtectionEnabled(Boolean.TRUE);
		mapping.setProtectionInterval(null);
		systemMappingService.save(mapping);
		

		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, roleOne);

		AccAccount account = accountService.getAccount(identity.getUsername(), system.getId());

		Assert.assertNotNull(account);
		Assert.assertFalse(account.isInProtection());
		TestResource createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);

		// Remove role from identity
		identityRoleService.deleteById(identityRole.getId());

		account = accountService.getAccount(identity.getUsername(), system.getId());
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
	@Test()
	public void protectedAccountExpiredDeleteTest() {

		IdmIdentityDto identity = helper.createIdentity();
		SysSystem system = initSystem();
		IdmRole roleOne = roleService.getByCode(ROLE_ONE);

		// Set system to protected mode
		SysSystemMapping mapping = systemMappingService
				.findBySystem(system, SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY).get(0);
		mapping.setProtectionEnabled(Boolean.TRUE);
		mapping.setProtectionInterval(null);
		systemMappingService.save(mapping);
		

		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, roleOne);

		AccAccount account = accountService.getAccount(identity.getUsername(), system.getId());

		Assert.assertNotNull(account);
		Assert.assertFalse(account.isInProtection());
		TestResource createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);

		// Remove role from identity
		identityRoleService.deleteById(identityRole.getId());

		account = accountService.getAccount(identity.getUsername(), system.getId());
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
		account = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNull(account);
		createdAccount = helper.findResource(identity.getUsername());
		Assert.assertNull(createdAccount);
	}

	private SysSystem initSystem() {
		// create test system
		SysSystem system = helper.createTestResourceSystem(true);
		// Create role with link on system (default)
		IdmRole role = helper.createRole();
		ROLE_ONE = role.getCode();
		// assign role to system
		helper.createRoleSystem(role, system);
		//
		return system;
	}
}
