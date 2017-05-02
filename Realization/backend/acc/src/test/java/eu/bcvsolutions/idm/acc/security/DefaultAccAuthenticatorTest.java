package eu.bcvsolutions.idm.acc.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.security.authentication.impl.DefaultAccAuthenticator;
import eu.bcvsolutions.idm.acc.service.DefaultSysAccountManagementServiceTest;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Default test for {@link DefaultAccAuthenticator} against new system.
 * 
 * @author Svanda
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DefaultAccAuthenticatorTest extends AbstractIntegrationTest {
	
	private static final String USERNAME = "test_authnetication_user";
	private static final String PASSWORD = "test1234";
	private static final String ROLE_NAME = "role";
	
	@Autowired
	private SysSystemService sysSystemService;
	
	@Autowired
	DataSource dataSource;
	
	@Autowired
	private SysSystemMappingService systemEntityHandlingService;

	@Autowired
	private SysSystemAttributeMappingService schemaAttributeHandlingService;
	
	@Autowired
	private IdmIdentityService identityService;
	
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	
	@Autowired
	private IdmRoleService roleService;
	
	@Autowired
	private IdmIdentityRoleService identityRoleService;

	@Autowired
	private AuthenticationManager authenticationManager;

	
	@Autowired
	private IdmIdentityContractService identityContractService;

	@Autowired
	private SysRoleSystemService roleSystemService;

	@Autowired
	private SysRoleSystemAttributeService roleSystemAttributeService;
	
	@Autowired
	private ProvisioningService provisioningService;
	
	@Autowired
	private ConfigurationService configurationService;
	
	@Autowired
	private AccIdentityAccountService identityAccoutnService;
	
	@Autowired
	private DefaultSysAccountManagementServiceTest defaultSysAccountManagementServiceTest;

	
	@Before
	public void login() {
		loginAsAdmin("admin");
	}
	
	@After
	@Override
	public void logout() {
		super.logout();
	}
	
	@Test
	public void A_loginAgainstSystem() {
		initData();
		IdmIdentity identity = identityService.getByName(USERNAME);
		IdmRole role = roleService.getByName(ROLE_NAME);
		
		IdmIdentityRoleDto irdto = new IdmIdentityRoleDto();
		irdto.setIdentityContract(identityContractService.findAllByIdentity(identity.getId()).get(0).getId());
		irdto.setRole(role.getId());
		// This evokes IdentityRole SAVE event. On this event will be start
		// account management and provisioning
		identityRoleService.save(irdto);
		
		// do provisioning
		provisioningService.doProvisioning(identity);
		
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		List<AccIdentityAccount> accounts = identityAccoutnService.find(filter, null).getContent();
		
		assertEquals(1, accounts.size());
		
		List<String> accs = new ArrayList<>();
		accs.add(accounts.get(0).getId().toString());
		
		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAccounts(accs);
		passwordChangeDto.setAll(true);
		passwordChangeDto.setNewPassword(new GuardedString("test"));
		// change password for system
		provisioningService.changePassword(identity, passwordChangeDto);
		
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(USERNAME);
		loginDto.setPassword(new GuardedString("test"));
		loginDto = authenticationManager.authenticate(loginDto);
		//
		assertNotNull(loginDto);
		assertNotNull(loginDto.getAuthentication());
		assertEquals("acc", loginDto.getAuthenticationModule());
	}
	
	@Test
	public void loginAgainstTwoAccount() {
		IdmIdentity identity = identityService.getByName(USERNAME);
		
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		List<AccIdentityAccount> accounts = identityAccoutnService.find(filter, null).getContent();
		
		assertEquals(1, accounts.size());
		
		IdmRole role2 = roleService.getByName(ROLE_NAME + "2");
		
		IdmIdentityRoleDto irdto = new IdmIdentityRoleDto();
		irdto.setIdentityContract(identityContractService.findAllByIdentity(identity.getId()).get(0).getId());
		irdto.setRole(role2.getId());

		identityRoleService.save(irdto);
		
		accounts = identityAccoutnService.find(filter, null).getContent();
		assertEquals(2, accounts.size());
		
		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		List<String> accs = new ArrayList<>();
		accs.add(accounts.get(0).getId().toString());
		passwordChangeDto.setAccounts(accs);
		passwordChangeDto.setAll(false);
		passwordChangeDto.setNewPassword(new GuardedString("1234"));
		// change password for system
		provisioningService.changePassword(identity, passwordChangeDto);
		
		passwordChangeDto = new PasswordChangeDto();
		accs = new ArrayList<>();
		accs.add(accounts.get(1).getId().toString());
		passwordChangeDto.setAccounts(accs);
		passwordChangeDto.setAll(false);
		passwordChangeDto.setNewPassword(new GuardedString("4321"));
		// change password for system
		provisioningService.changePassword(identity, passwordChangeDto);
		
		// bough password are right
		LoginDto loginDto1 = new LoginDto();
		loginDto1.setUsername(USERNAME);
		loginDto1.setPassword(new GuardedString("1234"));
		loginDto1 = authenticationManager.authenticate(loginDto1);
		
		LoginDto loginDto2 = new LoginDto();
		loginDto2.setUsername(USERNAME);
		loginDto2.setPassword(new GuardedString("4321"));
		loginDto2 = authenticationManager.authenticate(loginDto2);
		
		assertNotNull(loginDto2);
		assertNotNull(loginDto2.getAuthentication());
		assertEquals("acc", loginDto2.getAuthenticationModule());
		
		assertNotNull(loginDto1);
		assertNotNull(loginDto1.getAuthentication());
		assertEquals("acc", loginDto1.getAuthenticationModule());
	}
	
	@Test
	public void loginAgainstIdm() {
		IdmIdentity identity = new IdmIdentity();
		identity.setUsername("test_login_1");
		identity.setLastName("test_login_1");
		identity.setPassword(new GuardedString("test1234"));
		identity = identityService.save(identity);
		
		LoginDto loginDto = new LoginDto();
		loginDto.setPassword(new GuardedString("test1234"));
		loginDto.setUsername("test_login_1");
		
		loginDto = authenticationManager.authenticate(loginDto);
		assertNotNull(loginDto);
		assertNotNull(loginDto.getAuthentication());
		assertEquals("core", loginDto.getAuthenticationModule());
	}
	
	@Test(expected = IdmAuthenticationException.class)
	public void loginViaManagerBadCredentials() {
		IdmIdentity identity = identityService.getByUsername(USERNAME);

		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setIdm(false);
		passwordChangeDto.setNewPassword(new GuardedString(PASSWORD));
		// change password for system
		provisioningService.changePassword(identity, passwordChangeDto);
		
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(USERNAME);
		loginDto.setPassword(new GuardedString("test"));
		authenticationManager.authenticate(loginDto);
	}
	
	private void initData() {
		SysSystem system = createTestSystem();
		List<SysSchemaObjectClass> objectClasses = sysSystemService.generateSchema(system);
		
		IdmIdentity identity = new IdmIdentity();
		identity.setUsername(USERNAME);
		identity.setLastName(USERNAME);
		identity.setPassword(new GuardedString(PASSWORD));
		identity = identityService.save(identity);
		
		// Create mapped attributes to schema
		SysSystemMapping systemMapping = new SysSystemMapping();
		systemMapping.setName("default_" + System.currentTimeMillis());
		systemMapping.setEntityType(SystemEntityType.IDENTITY);
		systemMapping.setOperationType(SystemOperationType.PROVISIONING);
		systemMapping.setObjectClass(objectClasses.get(0));
		final SysSystemMapping entityHandlingResult = systemEntityHandlingService.save(systemMapping);
		
		SchemaAttributeFilter schemaAttributeFilter = new SchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		SysSystemAttributeMapping attributeHandlingLastName = new SysSystemAttributeMapping();
		SysSystemAttributeMapping attributeHandlingPassword = new SysSystemAttributeMapping();
		SysSystemAttributeMapping attributeHandlingUsername = new SysSystemAttributeMapping();
		
		Page<SysSchemaAttribute> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		schemaAttributesPage.forEach(schemaAttr -> {
			if ("__NAME__".equals(schemaAttr.getName())) {
				attributeHandlingUsername.setUid(true);
				attributeHandlingUsername.setEntityAttribute(true);
				attributeHandlingUsername.setAuthenticationAttribute(true);
				attributeHandlingUsername.setIdmPropertyName("username");
				attributeHandlingUsername.setTransformToResourceScript("if(attributeValue){return \"x\"+ attributeValue;}");
				attributeHandlingUsername.setName(schemaAttr.getName());
				attributeHandlingUsername.setSchemaAttribute(schemaAttr);
				attributeHandlingUsername.setSystemMapping(entityHandlingResult);
				schemaAttributeHandlingService.save(attributeHandlingUsername);
				
			} else if ("lastname".equalsIgnoreCase(schemaAttr.getName())) {
				attributeHandlingLastName.setIdmPropertyName("lastName");
				attributeHandlingLastName.setName(schemaAttr.getName());
				attributeHandlingLastName.setSchemaAttribute(schemaAttr);
				attributeHandlingLastName.setSystemMapping(entityHandlingResult);
				schemaAttributeHandlingService.save(attributeHandlingLastName);

			} else if (IcConnectorFacade.PASSWORD_ATTRIBUTE_NAME.equalsIgnoreCase(schemaAttr.getName())) {
				attributeHandlingPassword.setIdmPropertyName("password");
				attributeHandlingPassword.setSchemaAttribute(schemaAttr);
				attributeHandlingPassword.setName(schemaAttr.getName());
				attributeHandlingPassword.setSystemMapping(entityHandlingResult);
				schemaAttributeHandlingService.save(attributeHandlingPassword);

			}
		});
		
		// create two roles with same system and different override username
		IdmRole role1 = new IdmRole();
		role1.setName(ROLE_NAME);
		roleService.save(role1);
		SysRoleSystem role1System = new SysRoleSystem();
		role1System.setRole(role1);
		role1System.setSystem(system);
		role1System.setSystemMapping(systemMapping);
		roleSystemService.save(role1System);

		
		IdmRole role2 = new IdmRole();
		role2.setName(ROLE_NAME + "2");
		roleService.save(role2);
		SysRoleSystem roleSystem2 = new SysRoleSystem();
		roleSystem2.setSystem(system);
		roleSystem2.setSystemMapping(systemMapping);
		roleSystem2.setRole(role2);
		roleSystemService.save(roleSystem2);
		
		SysRoleSystemAttribute overloadedRole2 = new SysRoleSystemAttribute();
		overloadedRole2.setSystemAttributeMapping(attributeHandlingUsername);
		overloadedRole2.setUid(true);
		overloadedRole2.setEntityAttribute(true);
		overloadedRole2.setTransformScript("return \"z" + USERNAME + "\";");
		overloadedRole2.setIdmPropertyName("username");
		overloadedRole2.setName("username");
		overloadedRole2.setRoleSystem(roleSystem2);
		
		roleSystemAttributeService.save(overloadedRole2);
	}
	
	private SysSystem createTestSystem() {
		SysSystem system = defaultSysAccountManagementServiceTest.createTestSystem("test_resource");
		system = sysSystemService.save(system);
		
		// set system id to application property
		configurationService.setValue(DefaultAccAuthenticator.PROPERTY_AUTH_SYSTEM_ID, system.getId().toString());
		return system;
	}
}
