package eu.bcvsolutions.idm.acc.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for referential integrity from core to acc module
 * 
 * @author Radek Tomiška
 *
 */
public class CoreReferentialIntegrityIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private TestHelper helper;
	@Autowired private IdmIdentityService identityService;
	@Autowired private AccIdentityAccountService identityAccountService;
	@Autowired private SysSystemService systemService;	
	@Autowired private SysSystemEntityService systemEntityService;	
	@Autowired private AccAccountService accountService;	
	@Autowired private IdmRoleService roleService;
	@Autowired private SysRoleSystemService roleSystemService;
	@Autowired private SysSystemMappingService systemEntityHandlingService;
	@Autowired private SysSchemaObjectClassService schemaObjectClassService;
	
	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_USER_1);
	}
	
	@Test
	public void testIdentityReferentialIntegrity() {
		IdmIdentityDto identity = new IdmIdentityDto();
		String username = "delete_test_" + System.currentTimeMillis();
		identity.setUsername(username);
		identity.setPassword(new GuardedString("heslo")); // confidential storage
		identity.setFirstName("Test");
		identity.setLastName("Identity");
		identity = identityService.save(identity);
		// accounts
		SysSystemDto system = new SysSystemDto();
		system.setName("system_" + System.currentTimeMillis());
		system = systemService.save(system);
		
		SysSystemEntityDto systemEntity = new SysSystemEntityDto();
		systemEntity.setUid("test_uid_" + System.currentTimeMillis());
		systemEntity.setEntityType(SystemEntityType.IDENTITY);
		systemEntity.setWish(true);
		systemEntity.setSystem(system.getId());
		systemEntity = systemEntityService.save(systemEntity);
		
		AccAccountDto account = new AccAccountDto();
		account.setSystem(system.getId());
		account.setSystemEntity(systemEntity.getId());
		account.setUid(systemEntity.getUid());
		account.setAccountType(AccountType.PERSONAL);
		account = accountService.save(account);
		
		AccIdentityAccountDto identityAccount = new AccIdentityAccountDto();  
		identityAccount.setIdentity(identity.getId());
		identityAccount.setAccount(account.getId());
		identityAccount.setOwnership(true);
		identityAccount = identityAccountService.save(identityAccount);
		
		assertNotNull(identityService.getByUsername(username));
		assertNotNull(identityAccountService.get(identityAccount.getId()));
		assertNotNull(accountService.get(account.getId()));
		
		identityService.delete(identity);
		
		assertNull(identityService.getByUsername(username));
		assertNull(identityAccountService.get(identityAccount.getId()));
		assertNull(accountService.get(account.getId()));
	}
	
	@Test
	public void testRoleReferentialIntegrity() {
		IdmRoleDto role = helper.createRole();
		// role systems
		SysSystemDto system = new SysSystemDto();
		system.setName("system_" + System.currentTimeMillis());
		system = systemService.save(system);
		// schema
		SysSchemaObjectClassDto objectClass = new SysSchemaObjectClassDto();
		objectClass.setSystem(system.getId());
		objectClass.setObjectClassName("__ACCOUNT__");	
		objectClass = schemaObjectClassService.save(objectClass);
		SysSystemMappingDto systemMapping = new SysSystemMappingDto();
		systemMapping.setName("default_" + System.currentTimeMillis());
		systemMapping.setObjectClass(objectClass.getId());
		systemMapping.setOperationType(SystemOperationType.PROVISIONING);
		systemMapping.setEntityType(SystemEntityType.IDENTITY);
		systemMapping = systemEntityHandlingService.save(systemMapping);
		SysRoleSystemDto roleSystem = new SysRoleSystemDto();
		roleSystem.setSystem(system.getId());
		roleSystem.setRole(role.getId());
		roleSystem.setSystemMapping(systemMapping.getId());
		roleSystemService.save(roleSystem);
		SysRoleSystemFilter filter = new SysRoleSystemFilter();
		filter.setRoleId(role.getId());
		
		assertNotNull(roleService.getByCode(role.getName()));
		assertEquals(1, roleSystemService.find(filter, null).getTotalElements());
		
		roleService.delete(role);
		
		assertNull(roleService.getByCode(role.getName()));
		assertEquals(0, roleSystemService.find(filter, null).getTotalElements());
	}

}
