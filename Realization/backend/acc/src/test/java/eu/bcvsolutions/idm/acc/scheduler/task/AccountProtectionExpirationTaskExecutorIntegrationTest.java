package eu.bcvsolutions.idm.acc.scheduler.task;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.scheduler.task.impl.AccountProtectionExpirationTaskExecutor;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.test.TestHelper;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.scheduler.api.config.SchedulerConfiguration;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * LRT integration test
 * 
 * TODO: provisioning helper (DRY - initialized methods, createSystem, mapping, find test accounts ...)
 * 
 * @author Radek Tomiška
 *
 */
public class AccountProtectionExpirationTaskExecutorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private TestHelper helper;
	@Autowired private AccAccountService accountService;
	@Autowired private SysSystemMappingService systemMappingService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	
	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		helper.setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, false);
	}

	@After
	public void logout() {
		helper.setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, true);
		super.logout();
	}
	
	@Test
	public void testRemoveExpiredAccount() {
		IdmIdentityDto identity = helper.createIdentity();
		IdmRoleDto role = helper.createRole();
		SysSystemDto system = helper.createTestResourceSystem(true);
		SysSystemMappingDto mapping = helper.getDefaultMapping(system);
		mapping.setProtectionInterval(1);
		mapping.setProtectionEnabled(true);
		systemMappingService.save(mapping);
		helper.createRoleSystem(role, system);
		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, role);
		//
		AccAccountDto account = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNotNull(account);
		Assert.assertFalse(account.isInProtection());
		TestResource createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());
		//
		// remove role
		identityRoleService.deleteById(identityRole.getId());
		//
		account = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNotNull(account);
		Assert.assertTrue(account.isInProtection());
		Assert.assertNotNull(account.getEndOfProtection());
		createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());
		//
		// test LRT - nothing to remove
		AccountProtectionExpirationTaskExecutor taskExecutor = new AccountProtectionExpirationTaskExecutor();
		longRunningTaskManager.execute(taskExecutor);
		//
		account = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNotNull(account);
		Assert.assertTrue(account.isInProtection());
		Assert.assertNotNull(account.getEndOfProtection());
		createdAccount = helper.findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());
		
		// change account expiration
		
		account.setEndOfProtection(new DateTime().minusDays(1));
		account = accountService.save(account);
		
		taskExecutor = new AccountProtectionExpirationTaskExecutor();
		longRunningTaskManager.execute(taskExecutor);
		
		AccAccountDto removedAccount = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNull(removedAccount);
		createdAccount = helper.findResource(account.getUid());
		Assert.assertNull(createdAccount);
	}
}
