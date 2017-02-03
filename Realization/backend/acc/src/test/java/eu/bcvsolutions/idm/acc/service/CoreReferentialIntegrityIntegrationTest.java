package eu.bcvsolutions.idm.acc.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.filter.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for referential integrity from core to acc module
 * 
 * @author Radek Tomiška
 *
 */
public class CoreReferentialIntegrityIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private AccIdentityAccountService identityAccountService;
	@Autowired
	private SysSystemService systemService;	
	@Autowired
	private AccAccountService accountService;	
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private SysRoleSystemService roleSystemService;
	@Autowired
	private SysSystemMappingService systemEntityHandlingService;
	@Autowired
	private SysSchemaObjectClassService schemaObjectClassService;
	
	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_USER_1);
	}
	
	@Test
	public void testIdentityReferentialIntegrity() {
		IdmIdentity identity = new IdmIdentity();
		String username = "delete_test_" + System.currentTimeMillis();
		identity.setUsername(username);
		identity.setPassword(new GuardedString("heslo")); // confidential storage
		identity.setFirstName("Test");
		identity.setLastName("Identity");
		identity = identityService.save(identity);
		// accounts
		SysSystem system = new SysSystem();
		system.setName("system_" + System.currentTimeMillis());
		system = systemService.save(system);
		AccAccount account = new AccAccount();
		account.setSystem(system);
		account.setUid("test_uid_" + System.currentTimeMillis());
		account.setAccountType(AccountType.PERSONAL);
		account = accountService.save(account);
		AccIdentityAccount identityAccount = new AccIdentityAccount();  
		identityAccount.setIdentity(identity);
		identityAccount.setAccount(account);
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
		IdmRole role = new IdmRole();
		String roleName = "test_r_" + System.currentTimeMillis();
		role.setName(roleName);
		role = roleService.save(role);
		// role systems
		SysSystem system = new SysSystem();
		system.setName("system_" + System.currentTimeMillis());
		system = systemService.save(system);
		// schema
		SysSchemaObjectClass objectClass = new SysSchemaObjectClass();
		objectClass.setSystem(system);
		objectClass.setObjectClassName("__ACCOUNT__");	
		objectClass = schemaObjectClassService.save(objectClass);
		SysSystemMapping systemMapping = new SysSystemMapping();
		systemMapping.setName("default_" + System.currentTimeMillis());
		systemMapping.setObjectClass(objectClass);
		systemMapping.setOperationType(SystemOperationType.PROVISIONING);
		systemMapping.setEntityType(SystemEntityType.IDENTITY);
		systemMapping = systemEntityHandlingService.save(systemMapping);
		SysRoleSystem roleSystem = new SysRoleSystem();
		roleSystem.setSystem(system);
		roleSystem.setRole(role);
		roleSystem.setSystemMapping(systemMapping);
		roleSystemService.save(roleSystem);
		RoleSystemFilter filter = new RoleSystemFilter();
		filter.setRoleId(role.getId());
		
		assertNotNull(roleService.getByName(roleName));
		assertEquals(1, roleSystemService.find(filter, null).getTotalElements());
		
		roleService.delete(role);
		
		assertNull(roleService.getByName(roleName));
		assertEquals(0, roleSystemService.find(filter, null).getTotalElements());
	}

}
