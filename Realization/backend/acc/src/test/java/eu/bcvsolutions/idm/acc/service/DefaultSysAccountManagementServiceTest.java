package eu.bcvsolutions.idm.acc.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.filter.AccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysConnectorKey;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemFormValue;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityRoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Account management tests
 * 
 * @author Svanda
 *
 */
@Service
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DefaultSysAccountManagementServiceTest extends AbstractIntegrationTest {

	private static final String IDENTITY_USERNAME = "accTestUser";
	private static final String IDENTITY_EMAIL = "svanda.vit@xyz.cz";
	private static final String IDENTITY_EMAIL_CHANGED = "changedsvanda.vit@xyz.cz";
	private static final String IDENTITY_CHANGED_FIRST_NAME = "changed first name";
	private static final String ROLE_DEFAULT = "role_default";
	private static final String ROLE_OVERLOADING_LAST_NAME = "role_overloading_last_name";
	private static final String ROLE_OVERLOADING_PASSWORD = "role_overloading_password";
	private static final String ROLE_OVERLOADING_FIRST_NAME = "role_overloading_first_name";
	private static final String ROLE_OVERLOADING_Y_ACCOUNT = "role_overloading_y_account";
	private static final String IDENTITY_PASSWORD_TWO = "password_two";
	private static final String IDENTITY_PASSWORD_THREE = "password_three";

	@Autowired
	private SysSystemService sysSystemService;

	@Autowired
	private FormService formService;

	@Autowired
	private IdmIdentityService idmIdentityService;

	@Autowired
	private IdmIdentityRoleService idmIdentityRoleService;

	@Autowired
	private SysSystemEntityService systemEntityService;

	@Autowired
	private IdmRoleService idmRoleService;

	@Autowired
	private AccIdentityAccountService identityAccountService;

	@Autowired
	private AccAccountService accountService;

	@Autowired
	private SysSystemMappingService systemEntityHandlingService;

	@Autowired
	private SysSystemAttributeMappingService schemaAttributeHandlingService;

	@Autowired
	private SysSchemaAttributeService schemaAttributeService;

	@Autowired
	private SysRoleSystemService roleSystemService;

	@Autowired
	private SysRoleSystemAttributeService roleSystemAttributeService;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	DataSource dataSource;

	@Before
	public void init() {
		loginAsAdmin("admin");
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void defaultAccountAdd() {
		initData();

		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		IdmRole roleDefault = idmRoleService.getByName(ROLE_DEFAULT);

		Assert.assertNull("No account for this identity can be found, before account management start!",
				entityManager.find(TestResource.class, "x" + IDENTITY_USERNAME));

		IdmIdentityRoleDto irdto = new IdmIdentityRoleDto();
		irdto.setIdentity(identity.getId());
		irdto.setRole(roleDefault.getId());
		// This evokes IdentityRole SAVE event. On this event will be start
		// account management and provisioning
		IdmIdentityRole irCreated = idmIdentityRoleService.addByDto(irdto);

		IdentityAccountFilter iaccFilter = new IdentityAccountFilter();
		iaccFilter.setIdentityId(identity.getId());
		iaccFilter.setIdentityRoleId(irCreated.getId());
		AccIdentityAccount identityAccount = identityAccountService.find(iaccFilter, null).getContent().get(0);
		Assert.assertNotNull("Idenitity account have to exists after account management was started!", identityAccount);
		Assert.assertNotNull("Account have to exists after account management was started!",
				identityAccount.getAccount());
		Assert.assertEquals(identityAccount.getAccount().getUid(), "x" + IDENTITY_USERNAME);

		TestResource createdAccount = entityManager.find(TestResource.class, "x" + IDENTITY_USERNAME);
		Assert.assertNotNull("Idenitity have to exists on target system (after account management)", createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());
	}

	@Test
	public void defaultAccountChange() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		identity.setFirstName(IDENTITY_CHANGED_FIRST_NAME);

		// This evokes Identity SAVE event. On this event will be start account
		// management and provisioning
		idmIdentityService.save(identity);

		TestResource createdAccount = entityManager.find(TestResource.class, "x" + IDENTITY_USERNAME);
		Assert.assertNotNull("Idenitity have to exists on target system (after account management)", createdAccount);
		Assert.assertEquals("Account on target system, must have changed first name!", IDENTITY_CHANGED_FIRST_NAME,
				createdAccount.getFirstname());
	}

	
	@Test
	public void defaultAccountDisable() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		Assert.assertEquals("Identity must be enabled!", Boolean.FALSE,
				identity.isDisabled());
		TestResource resourceAccount = entityManager.find(TestResource.class, "x" + IDENTITY_USERNAME);
		Assert.assertNotNull("Idenitity have to exists on target system (after account management)", resourceAccount);
		Assert.assertEquals("Account on target system, must be enabled", "enabled",
				resourceAccount.getStatus());
		
		identity.setDisabled(true);
		
		// This evokes Identity SAVE event. On this event will be start account
		// management and provisioning
		idmIdentityService.save(identity);
		
		resourceAccount = entityManager.find(TestResource.class, "x" + IDENTITY_USERNAME);
		Assert.assertNotNull("Idenitity have to exists on target system (after account management)", resourceAccount);
		Assert.assertEquals("Account on target system, must be disabled!", "disabled",
				resourceAccount.getStatus());
	}

	@Test
	public void defaultAccountEntitySystemExist() {
		SystemEntityFilter filter = new SystemEntityFilter();
		filter.setEntityType(SystemEntityType.IDENTITY);
		filter.setUid("x" + IDENTITY_USERNAME);
		Assert.assertEquals("SystemEntity must be after account was created!", 1,
				systemEntityService.find(filter, null).getContent().size());
	}
	
	@Test
	public void defaultAccountRemove() {

		TestResource createdAccount = entityManager.find(TestResource.class, "x" + IDENTITY_USERNAME);
		Assert.assertNotNull("Idenitity have to exists on target system (before account will be delete)",
				createdAccount);

		AccountFilter accountFilter = new AccountFilter();
		accountFilter.setUidId("x" + IDENTITY_USERNAME);
		Assert.assertEquals("Account needs to exist befor will be delete", 1,
				accountService.find(accountFilter, null).getContent().size());

		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		IdentityRoleFilter irfilter = new IdentityRoleFilter();
		irfilter.setIdentityId(identity.getId());
		IdmIdentityRole identityRoleToDelete = idmIdentityRoleService.find(irfilter, null).getContent().get(0);

		// This evokes IdentityRole DELETE event. On this event will be start
		// account management and provisioning
		idmIdentityRoleService.deleteById(identityRoleToDelete.getId());

		Assert.assertEquals("Account must not be after was deleted", 0,
				accountService.find(accountFilter, null).getContent().size());

		IdentityAccountFilter iaccFilter = new IdentityAccountFilter();
		iaccFilter.setIdentityId(identity.getId());
		Assert.assertEquals("Idenitity account have to not exists after account was deleted!", 0,
				identityAccountService.find(iaccFilter, null).getContent().size());

		createdAccount = entityManager.find(TestResource.class, "x" + IDENTITY_USERNAME);
		Assert.assertNull("Idenitity have to no exists on target system (after account was deleted)", createdAccount);

		// Reset value
		identity.setFirstName(IDENTITY_USERNAME);
		idmIdentityService.save(identity);
	}

	@Test
	public void defaultAccountRemovedEntitySystemExist() {
		SystemEntityFilter filter = new SystemEntityFilter();
		filter.setEntityType(SystemEntityType.IDENTITY);
		filter.setUid("x" + IDENTITY_USERNAME);
		Assert.assertEquals("SystemEntity must not be after account was deleted!", 0,
				systemEntityService.find(filter, null).getContent().size());
	}
	
	@Test
	public void overloadedAttributeChangePassword() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		List<AccIdentityAccount> identityAccounts = identityAccountService.find(filter, null).getContent();
	
		TestResource resourceAccount = entityManager.find(TestResource.class, "x" + IDENTITY_USERNAME);

		// Create new password two
		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setIdentity(identity.getId().toString());
		passwordChange.setAccounts(ImmutableList.of(identityAccounts.get(0).getId().toString()));
		passwordChange.setNewPassword(new GuardedString(IDENTITY_PASSWORD_TWO));
		passwordChange.setIdm(true);
		// Do change of password for selected accounts
		idmIdentityService.passwordChange(identity, passwordChange);

		// Check correct password two
		resourceAccount = entityManager.find(TestResource.class, "x" + IDENTITY_USERNAME);
		Assert.assertEquals("Check same password on target system", IDENTITY_PASSWORD_TWO, resourceAccount.getPassword());

		// Add overloaded password attribute
		IdmRole rolePassword = idmRoleService.getByName(ROLE_OVERLOADING_PASSWORD);

		IdmIdentityRoleDto irdto = new IdmIdentityRoleDto();
		irdto.setIdentity(identity.getId());
		irdto.setRole(rolePassword.getId());
		// This evokes IdentityRole SAVE event. On this event will be start
		// account management and provisioning
		idmIdentityRoleService.addByDto(irdto);
		
		
		// Do change of password for selected accounts
		passwordChange.setNewPassword(new GuardedString(IDENTITY_PASSWORD_THREE));
		idmIdentityService.passwordChange(identity, passwordChange);
		// Check correct overloaded password two
		resourceAccount = entityManager.find(TestResource.class, "x" + IDENTITY_USERNAME);
		Assert.assertEquals("Check overloaded password (added x) on target system", "x"+IDENTITY_PASSWORD_THREE, resourceAccount.getPassword());
	}


	@Test
	public void overloadedAttributeAdd_A_LastNameRole() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		IdmRole roleLastName = idmRoleService.getByName(ROLE_OVERLOADING_LAST_NAME);

		Assert.assertNull("No account for this identity can be found, before account management start!",
				entityManager.find(TestResource.class, "x" + IDENTITY_USERNAME));

		IdmIdentityRoleDto irdto = new IdmIdentityRoleDto();
		irdto.setIdentity(identity.getId());
		irdto.setRole(roleLastName.getId());
		// This evokes IdentityRole SAVE event. On this event will be start
		// account management and provisioning
		IdmIdentityRole irCreated = idmIdentityRoleService.addByDto(irdto);

		IdentityAccountFilter iaccFilter = new IdentityAccountFilter();
		iaccFilter.setIdentityId(identity.getId());
		iaccFilter.setIdentityRoleId(irCreated.getId());
		AccIdentityAccount identityAccount = identityAccountService.find(iaccFilter, null).getContent().get(0);
		Assert.assertNotNull("Idenitity account have to exists after account management was started!", identityAccount);
		Assert.assertNotNull("Account have to exists after account management was started!",
				identityAccount.getAccount());
		Assert.assertEquals(identityAccount.getAccount().getUid(), "x" + IDENTITY_USERNAME);

		TestResource createdAccount = entityManager.find(TestResource.class, "x" + IDENTITY_USERNAME);
		Assert.assertNotNull("Idenitity have to exists on target system (after account management)", createdAccount);
		Assert.assertEquals(
				"Last name on target system must be equals with email on identity (we use overloded attribute)",
				identity.getEmail(), createdAccount.getLastname());
	}

	@Test
	public void overloadedAttributeAdd_B_DisableFirstNameRole() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		IdmRole roleLastName = idmRoleService.getByName(ROLE_OVERLOADING_FIRST_NAME);

		Assert.assertNotNull("Account for this identity have to be found!",
				entityManager.find(TestResource.class, "x" + IDENTITY_USERNAME));

		IdmIdentityRoleDto irdto = new IdmIdentityRoleDto();
		irdto.setIdentity(identity.getId());
		irdto.setRole(roleLastName.getId());
		// This evokes IdentityRole SAVE event. On this event will be start
		// account management and provisioning
		idmIdentityRoleService.addByDto(irdto);

		IdentityAccountFilter iaccFilter = new IdentityAccountFilter();
		iaccFilter.setIdentityId(identity.getId());
		// Now we have to identity roles (role_overloading_first_name and
		// role_overloading_last_name) and identity accounts
		Assert.assertEquals("Idenitity accounts have to exists (two items) after account management was started!", 2,
				identityAccountService.find(iaccFilter, null).getContent().size());

		TestResource createdAccount = entityManager.find(TestResource.class, "x" + IDENTITY_USERNAME);
		Assert.assertNotNull("Idenitity have to exists on target system (after account management)", createdAccount);
		Assert.assertEquals("First name on target system must be equals with first name on identity",
				identity.getFirstName(), createdAccount.getFirstname());

		identity.setFirstName(IDENTITY_CHANGED_FIRST_NAME);
		identity.setEmail(IDENTITY_EMAIL_CHANGED);
		// This evokes Identity SAVE event. On this event will be start
		// account management and provisioning
		idmIdentityService.save(identity);

		createdAccount = entityManager.find(TestResource.class, "x" + IDENTITY_USERNAME);
		// Because first name attribute was disabled, we now expect change only
		// on email attribute
		Assert.assertEquals(
				"Last name on target system must be equals with email on identity (we use overloded attribute)",
				IDENTITY_EMAIL_CHANGED, createdAccount.getLastname());
		Assert.assertNotEquals(
				"First name on target system must be not equals with first name on identity (we use overloded disabled attribute)",
				identity.getFirstName(), createdAccount.getFirstname());
	}

	@Test
	public void overloadedAttributeAdd_C_AccountYrole() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		IdmRole role = idmRoleService.getByName(ROLE_OVERLOADING_Y_ACCOUNT);

		Assert.assertNotNull("Account for this identity have to be found!",
				entityManager.find(TestResource.class, "x" + IDENTITY_USERNAME));

		IdmIdentityRoleDto irdto = new IdmIdentityRoleDto();
		irdto.setIdentity(identity.getId());
		irdto.setRole(role.getId());
		// This evokes IdentityRole SAVE event. On this event will be start
		// account management and provisioning
		idmIdentityRoleService.addByDto(irdto);

		IdentityAccountFilter iaccFilter = new IdentityAccountFilter();
		iaccFilter.setIdentityId(identity.getId());
		// Now we have to identity roles (role_overloading_first_name and
		// role_overloading_last_name and role_overloading_y_account) and
		// identity accounts
		Assert.assertEquals("Idenitity accounts have to exists (three items) after account management was started!", 3,
				identityAccountService.find(iaccFilter, null).getContent().size());

		TestResource createdAccount = entityManager.find(TestResource.class, "y" + IDENTITY_USERNAME);
		Assert.assertNotNull("Idenitity have to exists on target system (after account management)", createdAccount);
		Assert.assertEquals("First name on target system must be equals with first name on identity",
				identity.getFirstName(), createdAccount.getFirstname());
		Assert.assertEquals("Last name on target system must be equals with first name on identity",
				identity.getLastName(), createdAccount.getLastname());
	}

	@Test
	public void overloadedAttributeRemoveAllRoles() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);

		Assert.assertNotNull("Account for this identity have to be found!",
				entityManager.find(TestResource.class, "x" + IDENTITY_USERNAME));

		IdentityAccountFilter iaccFilter = new IdentityAccountFilter();
		iaccFilter.setIdentityId(identity.getId());
		// Now we have to identity roles (role_overloading_first_name and
		// role_overloading_last_name and role_overloading_y_account) and
		// identity accounts
		List<AccIdentityAccount> identityAccounts = identityAccountService.find(iaccFilter, null).getContent();
		Assert.assertEquals("Idenitity accounts have to exists (four items) after account management was started!", 4,
				identityAccounts.size());

		IdentityRoleFilter irfilter = new IdentityRoleFilter();
		irfilter.setIdentityId(identity.getId());
		idmIdentityRoleService.find(irfilter, null).getContent().forEach(identityRole -> {
			idmIdentityRoleService.delete(identityRole);
		});

		Assert.assertEquals("Idenitity accounts have to not exist after accounts deleted!", 0,
				identityAccountService.find(iaccFilter, null).getContent().size());
	}

	/**
	 * Create test system connected to same database (using configuration from
	 * dataSource)
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public SysSystem createTestSystem() {
		// create owner
		org.apache.tomcat.jdbc.pool.DataSource tomcatDataSource = ((org.apache.tomcat.jdbc.pool.DataSource) dataSource);
		SysSystem system = new SysSystem();
		system.setName("testResource_" + System.currentTimeMillis());

		system.setConnectorKey(new SysConnectorKey(sysSystemService.getTestConnectorKey()));
		sysSystemService.save(system);

		IdmFormDefinition savedFormDefinition = sysSystemService.getConnectorFormDefinition(system.getConnectorInstance());

		List<SysSystemFormValue> values = new ArrayList<>();

		SysSystemFormValue jdbcUrlTemplate = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("jdbcUrlTemplate"));
		jdbcUrlTemplate.setValue(tomcatDataSource.getUrl());
		values.add(jdbcUrlTemplate);
		SysSystemFormValue jdbcDriver = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("jdbcDriver"));
		jdbcDriver.setValue(tomcatDataSource.getDriverClassName());
		values.add(jdbcDriver);

		SysSystemFormValue user = new SysSystemFormValue(savedFormDefinition.getMappedAttributeByName("user"));
		user.setValue(tomcatDataSource.getUsername());
		values.add(user);
		SysSystemFormValue password = new SysSystemFormValue(savedFormDefinition.getMappedAttributeByName("password"));
		password.setValue(tomcatDataSource.getPoolProperties().getPassword());
		values.add(password);
		SysSystemFormValue table = new SysSystemFormValue(savedFormDefinition.getMappedAttributeByName("table"));
		table.setValue("test_resource");
		values.add(table);
		SysSystemFormValue keyColumn = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("keyColumn"));
		keyColumn.setValue("name");
		values.add(keyColumn);
		SysSystemFormValue passwordColumn = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("passwordColumn"));
		passwordColumn.setValue("password");
		values.add(passwordColumn);
		SysSystemFormValue allNative = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("allNative"));
		allNative.setValue(true);
		values.add(allNative);
		SysSystemFormValue rethrowAllSQLExceptions = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("rethrowAllSQLExceptions"));
		rethrowAllSQLExceptions.setValue(true);
		values.add(rethrowAllSQLExceptions);
		SysSystemFormValue statusColumn = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("statusColumn"));
		statusColumn.setValue("status");
		values.add(statusColumn);
		SysSystemFormValue disabledStatusValue = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("disabledStatusValue"));
		disabledStatusValue.setValue("disabled");
		values.add(disabledStatusValue);
		SysSystemFormValue enabledStatusValue = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("enabledStatusValue"));
		enabledStatusValue.setValue("enabled");
		values.add(enabledStatusValue);

		formService.saveValues(system, savedFormDefinition, values);

		return system;
	}

	private void initData() {
		IdmIdentity identity;

		// create test system
		
		SysSystem system = createTestSystem();

		// generate schema for system
		 List<SysSchemaObjectClass> objectClasses = sysSystemService.generateSchema(system);

		// Create test identity for provisioning test
		identity = new IdmIdentity();
		identity.setUsername(IDENTITY_USERNAME);
		identity.setFirstName(IDENTITY_USERNAME);
		identity.setLastName(IDENTITY_USERNAME);
		identity.setEmail(IDENTITY_EMAIL);
		identity = idmIdentityService.save(identity);

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
		SysSystemAttributeMapping attributeHandlingFirstName = new SysSystemAttributeMapping();
		SysSystemAttributeMapping attributeHandlingUserName = new SysSystemAttributeMapping();
		SysSystemAttributeMapping attributeHandlingEnable = new SysSystemAttributeMapping();

		Page<SysSchemaAttribute> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		schemaAttributesPage.forEach(schemaAttr -> {
			if ("__NAME__".equals(schemaAttr.getName())) {
				attributeHandlingUserName.setUid(true);
				attributeHandlingUserName.setEntityAttribute(false);
				attributeHandlingUserName.setTransformToResourceScript("return \"" + "x" + IDENTITY_USERNAME + "\";");
				attributeHandlingUserName.setName(schemaAttr.getName());
				attributeHandlingUserName.setSchemaAttribute(schemaAttr);
				attributeHandlingUserName.setSystemMapping(entityHandlingResult);
				schemaAttributeHandlingService.save(attributeHandlingUserName);
			} else if ("__ENABLE__".equals(schemaAttr.getName())) {
				attributeHandlingEnable.setUid(false);
				attributeHandlingEnable.setEntityAttribute(true);
				attributeHandlingEnable.setIdmPropertyName("disabled");
				attributeHandlingEnable.setTransformToResourceScript("return String.valueOf(!attributeValue);");
				attributeHandlingEnable.setTransformFromResourceScript("return !attributeValue;");
				attributeHandlingEnable.setName(schemaAttr.getName());
				attributeHandlingEnable.setSchemaAttribute(schemaAttr);
				attributeHandlingEnable.setSystemMapping(entityHandlingResult);
				schemaAttributeHandlingService.save(attributeHandlingEnable);

			} else if ("firstname".equalsIgnoreCase(schemaAttr.getName())) {
				attributeHandlingFirstName.setIdmPropertyName("firstName");
				attributeHandlingFirstName.setSchemaAttribute(schemaAttr);
				attributeHandlingFirstName.setName(schemaAttr.getName());
				attributeHandlingFirstName.setSystemMapping(entityHandlingResult);
				schemaAttributeHandlingService.save(attributeHandlingFirstName);

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

		/*
		 * Create role with link on system (default)
		 */
		IdmRole roleDefault = new IdmRole();
		roleDefault.setName(ROLE_DEFAULT);
		idmRoleService.save(roleDefault);
		SysRoleSystem roleSystemDefault = new SysRoleSystem();
		roleSystemDefault.setRole(roleDefault);
		roleSystemDefault.setSystem(system);
		roleSystemDefault.setSystemMapping(systemMapping);
		roleSystemService.save(roleSystemDefault);

		/*
		 * Create role with link on system (overloading last name attribute)
		 */
		IdmRole roleOverloadingLastName = new IdmRole();
		roleOverloadingLastName.setName(ROLE_OVERLOADING_LAST_NAME);
		idmRoleService.save(roleOverloadingLastName);
		SysRoleSystem roleSystemLastName = new SysRoleSystem();
		roleSystemLastName.setRole(roleOverloadingLastName);
		roleSystemLastName.setSystem(system);
		roleSystemLastName.setSystemMapping(systemMapping);
		roleSystemService.save(roleSystemLastName);

		// Attribute for overloading last name attribute
		SysRoleSystemAttribute attributeLastName = new SysRoleSystemAttribute();
		attributeLastName.setEntityAttribute(true);
		attributeLastName.setIdmPropertyName("email");
		attributeLastName.setName("Overloaded lastName with email");
		attributeLastName.setRoleSystem(roleSystemLastName);
		attributeLastName.setSystemAttributeMapping(attributeHandlingLastName);
		roleSystemAttributeService.save(attributeLastName);
		
		/*
		 * Create role with link on system (overloading password attribute)
		 */
		IdmRole roleOverloadingPassword = new IdmRole();
		roleOverloadingPassword.setName(ROLE_OVERLOADING_PASSWORD);
		idmRoleService.save(roleOverloadingPassword);
		SysRoleSystem roleSystemPassword = new SysRoleSystem();
		roleSystemPassword.setRole(roleOverloadingPassword);
		roleSystemPassword.setSystem(system);
		roleSystemPassword.setSystemMapping(systemMapping);
		roleSystemService.save(roleSystemPassword);

		// Attribute for overloading last name attribute
		SysRoleSystemAttribute attributePassword = new SysRoleSystemAttribute();
		attributePassword.setEntityAttribute(true);
		attributePassword.setIdmPropertyName("password");
		attributePassword.setConfidentialAttribute(true);
		attributePassword.setName("Overloaded password - add x");
		attributePassword.setRoleSystem(roleSystemPassword);
		attributePassword.setSystemAttributeMapping(attributeHandlingPassword);
		attributePassword.setTransformScript("return new "+GuardedString.class.getName()+"(\"x\"+attributeValue.asString());");
		roleSystemAttributeService.save(attributePassword);

		/*
		 * Create role with link on system (overloading (disable) first name
		 * attribute)
		 */
		IdmRole roleOverloadingFirstName = new IdmRole();
		roleOverloadingFirstName.setName(ROLE_OVERLOADING_FIRST_NAME);
		idmRoleService.save(roleOverloadingFirstName);
		SysRoleSystem roleSystemFirstName = new SysRoleSystem();
		roleSystemFirstName.setRole(roleOverloadingFirstName);
		roleSystemFirstName.setSystem(system);
		roleSystemFirstName.setSystemMapping(systemMapping);
		roleSystemService.save(roleSystemFirstName);

		// Attribute for overloading first name attribute (disable him)
		SysRoleSystemAttribute attributeFirstName = new SysRoleSystemAttribute();
		attributeFirstName.setDisabledDefaultAttribute(true);
		attributeFirstName.setName("Disable first name");
		attributeFirstName.setRoleSystem(roleSystemFirstName);
		attributeFirstName.setSystemAttributeMapping(attributeHandlingFirstName);
		roleSystemAttributeService.save(attributeFirstName);

		/*
		 * Create role with link on system (overloading name attribute ...
		 * create Y account)
		 */
		IdmRole roleOverloadingName = new IdmRole();
		roleOverloadingName.setName(ROLE_OVERLOADING_Y_ACCOUNT);
		idmRoleService.save(roleOverloadingName);
		SysRoleSystem roleSystemName = new SysRoleSystem();
		roleSystemName.setRole(roleOverloadingName);
		roleSystemName.setSystem(system);
		roleSystemName.setSystemMapping(systemMapping);
		roleSystemService.save(roleSystemName);

		// Attribute for overloading first name attribute (disable him)
		SysRoleSystemAttribute attributeName = new SysRoleSystemAttribute();
		attributeName.setUid(true);
		attributeName.setEntityAttribute(true);
		attributeName.setIdmPropertyName("username");
		attributeName.setName("Account with Y-prefix name");
		attributeName.setTransformScript("return \"y\" + attributeValue ;");
		attributeName.setRoleSystem(roleSystemName);
		attributeName.setSystemAttributeMapping(attributeHandlingUserName);
		roleSystemAttributeService.save(attributeName);

	}

}
