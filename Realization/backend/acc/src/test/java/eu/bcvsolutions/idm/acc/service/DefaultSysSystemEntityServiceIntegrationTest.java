package eu.bcvsolutions.idm.acc.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * System entity operation tests
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultSysSystemEntityServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private SysSystemService systemService;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private SysSystemEntityService systemEntityService;
	
	@Before
	public void login() {
		loginAsAdmin(InitTestData.TEST_USER_1);
	}
	
	@After 
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testReferentialIntegrity() {
		// system
		SysSystem system = new SysSystem();
		String systemName = "t_s_" + System.currentTimeMillis();
		system.setName(systemName);
		system = systemService.save(system);
		// system entity
		SysSystemEntity systemEntity = new SysSystemEntity();
		systemEntity.setSystem(system);
		systemEntity.setEntityType(SystemEntityType.IDENTITY);
		String uid = "se_uid_" + System.currentTimeMillis();
		systemEntity.setUid(uid);
		systemEntityService.save(systemEntity);
		// account
		AccAccount account = new AccAccount();
		account.setSystem(system);
		account.setUid("test_uid_" + System.currentTimeMillis());
		account.setAccountType(AccountType.PERSONAL);
		account.setSystemEntity(systemEntity);
		account = accountService.save(account);
		
		assertEquals(uid, accountService.get(account.getId()).getSystemEntity().getUid());
		
		systemEntityService.delete(systemEntity);
		
		assertNull(accountService.get(account.getId()).getSystemEntity());		
	}
}
