package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
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
		SysSystemDto system = new SysSystemDto();
		String systemName = "t_s_" + System.currentTimeMillis();
		system.setName(systemName);
		system = systemService.save(system);
		// system entity
		SysSystemEntityDto systemEntity = new SysSystemEntityDto();
		systemEntity.setSystem(system.getId());
		systemEntity.setEntityType(SystemEntityType.IDENTITY);
		String uid = "se_uid_" + System.currentTimeMillis();
		systemEntity.setUid(uid);
		systemEntity = systemEntityService.save(systemEntity);
		// account
		AccAccountDto account = new AccAccountDto();
		account.setSystem(system.getId());
		account.setUid("test_uid_" + System.currentTimeMillis());
		account.setAccountType(AccountType.PERSONAL);
		account.setSystemEntity(systemEntity.getId());
		account = accountService.save(account);
		
		SysSystemEntityDto systemEntityDto = DtoUtils.getEmbedded(account, AccAccount_.systemEntity, SysSystemEntityDto.class);
		assertEquals(uid, systemEntityDto.getUid());
		
		systemEntityService.delete(systemEntity);
		
		assertNull(accountService.get(account.getId()).getSystemEntity());		
	}
}
