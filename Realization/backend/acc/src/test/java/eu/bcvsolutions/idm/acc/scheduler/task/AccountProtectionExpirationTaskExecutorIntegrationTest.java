package eu.bcvsolutions.idm.acc.scheduler.task;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.scheduler.task.impl.AccountProtectionExpirationTaskExecutor;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * LRT integration test
 * 
 * @author Radek Tomiška
 *
 */
public class AccountProtectionExpirationTaskExecutorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private AccAccountService accountService;
	@Autowired private SysSystemMappingService systemMappingService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	
	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testRemoveExpiredAccount() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto role = getHelper().createRole();
		SysSystemDto system = getHelper().createTestResourceSystem(true);
		SysSystemMappingDto mapping = getHelper().getDefaultMapping(system);
		mapping.setProtectionInterval(1);
		mapping.setProtectionEnabled(true);
		systemMappingService.save(mapping);
		getHelper().createRoleSystem(role, system);
		IdmIdentityRoleDto identityRole = getHelper().createIdentityRole(identity, role);
		//
		AccAccountDto account = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNotNull(account);
		Assert.assertFalse(account.isInProtection());
		TestResource createdAccount = getHelper().findResource(account.getUid());
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
		createdAccount = getHelper().findResource(account.getUid());
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
		createdAccount = getHelper().findResource(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());
		
		// change account expiration
		
		account.setEndOfProtection(new DateTime().minusDays(1));
		account = accountService.save(account);
		
		taskExecutor = new AccountProtectionExpirationTaskExecutor();
		longRunningTaskManager.execute(taskExecutor);
		
		AccAccountDto removedAccount = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNull(removedAccount);
		createdAccount = getHelper().findResource(account.getUid());
		Assert.assertNull(createdAccount);
	}
	
	@Override
	protected TestHelper getHelper() {
		return (TestHelper) super.getHelper();
	}
}
