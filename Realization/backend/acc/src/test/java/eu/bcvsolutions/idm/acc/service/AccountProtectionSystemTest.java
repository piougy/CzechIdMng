package eu.bcvsolutions.idm.acc.service;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Protection account system
 * 
 * @author Svanda
 *
 */
@Service
public class AccountProtectionSystemTest extends AbstractIntegrationTest {

	private static final String ROLE_ONE = "role_one";
	private static final String IDENTITY_USERNAME = "protectionUserOne";
	private static final String IDENTITY_USERNAME_TWO = "protectionUserTwo";
	private static final String SYSTEM_NAME = "protectedSystem";
	private static final String EMAIL_ONE = "one.email@one.cz";
	private static final String EMAIL_TWO = "two.email@two.cz";

	@Autowired
	private SysSystemService sysSystemService;
	@Autowired
	private IdmIdentityService idmIdentityService;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private SysRoleSystemService roleSystemService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmIdentityContractService contractService;
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	DataSource dataSource;

	// Only for call method createTestSystem
	@Autowired
	private DefaultSysAccountManagementServiceTest defaultSysAccountManagementServiceTest;

	@Transactional
	@Before
	public void init() {
		loginAsAdmin("admin");
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
		TestResource createdAccount = this.getBean().findAccountOnTargetSystem(account.getUid());
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
		TestResource createdAccount = this.getBean().findAccountOnTargetSystem(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());

		// Remove role from identity
		identityRoleService.deleteById(identityRole.getId());

		account = accountService.getAccount(IDENTITY_USERNAME, system.getId());
		Assert.assertNotNull(account);
		Assert.assertTrue(account.isInProtection());
		Assert.assertNull(account.getEndOfProtection());
		createdAccount = this.getBean().findAccountOnTargetSystem(account.getUid());
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
		TestResource createdAccount = this.getBean().findAccountOnTargetSystem(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());

		// We again assign same role
		identityRole = assignRole(identity, roleOne);

		// Account must be unprotected
		Assert.assertNotNull(account);
		Assert.assertFalse(account.isInProtection());
		createdAccount = this.getBean().findAccountOnTargetSystem(account.getUid());
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
		TestResource createdAccount = this.getBean().findAccountOnTargetSystem(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());

		// Remove role from identity
		identityRoleService.deleteById(identityRole.getId());

		account = accountService.getAccount(IDENTITY_USERNAME, system.getId());
		Assert.assertNotNull(account);
		Assert.assertTrue(account.isInProtection());
		Assert.assertNotNull(account.getEndOfProtection());
		Assert.assertTrue(account.getEndOfProtection().toLocalDate().isEqual(LocalDate.now().plusDays(intervalInDays)));
		createdAccount = this.getBean().findAccountOnTargetSystem(account.getUid());
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
		TestResource createdAccount = this.getBean().findAccountOnTargetSystem(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(changedValue, createdAccount.getFirstname());

		// Remove role from identity
		identityRoleService.deleteById(identityRole.getId());

		account = accountService.getAccount(IDENTITY_USERNAME, system.getId());
		Assert.assertNotNull(account);
		Assert.assertTrue(account.isInProtection());
		Assert.assertNotNull(account.getEndOfProtection());
		Assert.assertTrue(account.getEndOfProtection().toLocalDate().isEqual(LocalDate.now().plusDays(intervalInDays)));
		createdAccount = this.getBean().findAccountOnTargetSystem(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());

		// Change first name and emit provisioning (provisioning must be break)
		identity.setFirstName(IDENTITY_USERNAME);
		idmIdentityService.save(identity);

		createdAccount = this.getBean().findAccountOnTargetSystem(account.getUid());
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
		TestResource createdAccount = this.getBean().findAccountOnTargetSystem(account.getUid());
		Assert.assertNotNull(createdAccount);

		// Remove role from identity
		identityRoleService.deleteById(identityRole.getId());

		account = accountService.getAccount(IDENTITY_USERNAME, system.getId());
		Assert.assertNotNull(account);
		Assert.assertTrue(account.isInProtection());
		Assert.assertNull(account.getEndOfProtection());
		createdAccount = this.getBean().findAccountOnTargetSystem(account.getUid());
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
		TestResource createdAccount = this.getBean().findAccountOnTargetSystem(account.getUid());
		Assert.assertNotNull(createdAccount);

		// Remove role from identity
		identityRoleService.deleteById(identityRole.getId());

		account = accountService.getAccount(IDENTITY_USERNAME, system.getId());
		Assert.assertNotNull(account);
		Assert.assertTrue(account.isInProtection());
		Assert.assertNull(account.getEndOfProtection());
		createdAccount = this.getBean().findAccountOnTargetSystem(account.getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());
		
		// Set account as expired
		account.setEndOfProtection(DateTime.now().minusMonths(1));
		account = accountService.save(account);
		
		// Delete AccAccount directly
		accountService.delete(account);
		account = accountService.getAccount(IDENTITY_USERNAME, system.getId());
		Assert.assertNull(account);
		createdAccount = this.getBean().findAccountOnTargetSystem(IDENTITY_USERNAME);
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
		SysSystem system = defaultSysAccountManagementServiceTest.createTestSystem("test_resource");
		system.setName(SYSTEM_NAME);
		system = sysSystemService.save(system);

		// generate schema for system
		List<SysSchemaObjectClass> objectClasses = sysSystemService.generateSchema(system);

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

		SysSystemMapping systemMapping = new SysSystemMapping();
		systemMapping.setName("default_" + System.currentTimeMillis());
		systemMapping.setEntityType(SystemEntityType.IDENTITY);
		systemMapping.setOperationType(SystemOperationType.PROVISIONING);
		systemMapping.setObjectClass(objectClasses.get(0));
		final SysSystemMapping entityHandlingResult = systemMappingService.save(systemMapping);

		SchemaAttributeFilter schemaAttributeFilter = new SchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());

		/*
		 * Create role with link on system (default)
		 */
		IdmRole roleDefault = new IdmRole();
		roleDefault.setName(ROLE_ONE);
		roleService.save(roleDefault);
		SysRoleSystem roleSystemDefault = new SysRoleSystem();
		roleSystemDefault.setRole(roleDefault);
		roleSystemDefault.setSystem(system);
		roleSystemDefault.setSystemMapping(systemMapping);
		roleSystemService.save(roleSystemDefault);

		createMapping(entityHandlingResult, schemaAttributeFilter);
	}

	private void createMapping(final SysSystemMapping entityHandlingResult,
			SchemaAttributeFilter schemaAttributeFilter) {
		Page<SysSchemaAttribute> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		schemaAttributesPage.forEach(schemaAttr -> {
			if ("__NAME__".equals(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeMapping = new SysSystemAttributeMapping();
				attributeMapping.setUid(true);
				attributeMapping.setEntityAttribute(true);
				attributeMapping.setIdmPropertyName(IdmIdentity_.username.getName());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr);
				attributeMapping.setSystemMapping(entityHandlingResult);
				systemAttributeMappingService.save(attributeMapping);

			} else if ("firstname".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeMapping = new SysSystemAttributeMapping();
				attributeMapping.setIdmPropertyName(IdmIdentity_.firstName.getName());
				attributeMapping.setSchemaAttribute(schemaAttr);
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSystemMapping(entityHandlingResult);
				systemAttributeMappingService.save(attributeMapping);

			} else if ("lastname".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeMapping = new SysSystemAttributeMapping();
				attributeMapping.setIdmPropertyName(IdmIdentity_.lastName.getName());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr);
				attributeMapping.setSystemMapping(entityHandlingResult);
				systemAttributeMappingService.save(attributeMapping);

			} else if (IcConnectorFacade.PASSWORD_ATTRIBUTE_NAME.equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeMapping = new SysSystemAttributeMapping();
				attributeMapping.setIdmPropertyName("password");
				attributeMapping.setSchemaAttribute(schemaAttr);
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSystemMapping(entityHandlingResult);
				systemAttributeMappingService.save(attributeMapping);

			} else if ("email".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeMapping = new SysSystemAttributeMapping();
				attributeMapping.setIdmPropertyName(IdmIdentity_.email.getName());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr);
				attributeMapping.setSystemMapping(entityHandlingResult);
				systemAttributeMappingService.save(attributeMapping);

			}
		});
	}

	private AccountProtectionSystemTest getBean() {
		return applicationContext.getBean(this.getClass());
	}
}
