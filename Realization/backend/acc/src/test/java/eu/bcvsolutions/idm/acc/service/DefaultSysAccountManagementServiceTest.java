package eu.bcvsolutions.idm.acc.service;

import java.util.List;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.repository.SysSystemAttributeMappingRepository;
import eu.bcvsolutions.idm.acc.repository.SysSystemMappingRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdentityRoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
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
	private TestHelper helper;

	@Autowired
	private IdmIdentityService identityService;
	
	@Autowired
	private IdmIdentityContractService identityContractService;

	@Autowired
	private IdmIdentityRoleService identityRoleService;

	@Autowired
	private SysSystemEntityService systemEntityService;

	@Autowired
	private IdmRoleService roleService;

	@Autowired
	private AccIdentityAccountService identityAccountService;

	@Autowired
	private AccAccountService accountService;

	@Autowired
	private SysSystemAttributeMappingService schemaAttributeHandlingService;

	@Autowired
	private SysRoleSystemService roleSystemService;

	@Autowired
	private SysRoleSystemAttributeService roleSystemAttributeService;

	@Autowired
	private SysSystemAttributeMappingRepository systemAttributeMappingRepository;
	
	@Autowired
	private SysSystemMappingRepository systemMappingRepository;
	
	@Before
	public void init() {
		loginAsAdmin("admin");
	}

	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	/**
	 * Add invalid identity role. Account cannot be created.
	 */
	public void defaultAccountAddInvalid() {
		initData();

		IdmIdentityDto identity = identityService.getByUsername(IDENTITY_USERNAME);
		IdmRole roleDefault = roleService.getByName(ROLE_DEFAULT);

		Assert.assertNull("No account for this identity can be found, before account management start!",
				helper.findResource("x" + IDENTITY_USERNAME));

		IdmIdentityRoleDto irdto = new IdmIdentityRoleDto();
		irdto.setIdentityContract(identityContractService.findAllByIdentity(identity.getId()).get(0).getId());
		irdto.setRole(roleDefault.getId());
		// Set valid from to future
		irdto.setValidFrom(LocalDate.now().plusDays(1));
		// This evokes IdentityRole SAVE event. On this event will be start
		// account management and provisioning
		irdto = identityRoleService.save(irdto);

		IdentityAccountFilter iaccFilter = new IdentityAccountFilter();
		iaccFilter.setIdentityId(identity.getId());
		iaccFilter.setIdentityRoleId(irdto.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(iaccFilter, null).getContent();
		// Identity-account have to not exists after account management was started (INVALID identityRole was added)!
		Assert.assertEquals(0, identityAccounts.size());

		// Set valid from to null - Account must be created
		irdto.setValidFrom(null);
		// This evokes IdentityRole SAVE event. On this event will be start
		// account management and provisioning
		irdto = identityRoleService.save(irdto);

		identityAccounts = identityAccountService.find(iaccFilter, null).getContent();
		Assert.assertEquals(1, identityAccounts.size());
		AccIdentityAccountDto identityAccount = identityAccounts.get(0);
		Assert.assertNotNull("Idenitity account have to exists after account management was started!", identityAccount);
		Assert.assertNotNull("Account have to exists after account management was started!",
				identityAccount.getAccount());
		Assert.assertEquals(accountService.get(identityAccount.getAccount()).getUid(), "x" + IDENTITY_USERNAME);

		TestResource createdAccount = helper.findResource("x" + IDENTITY_USERNAME);
		Assert.assertNotNull("Idenitity have to exists on target system (after account management)", createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());
		
		// Set valid from to null - Account must be created
		irdto.setValidTill(LocalDate.now().minusDays(1));
		// This evokes IdentityRole SAVE event. On this event will be start
		// account management and provisioning
		irdto = identityRoleService.save(irdto);
		identityAccounts = identityAccountService.find(iaccFilter, null).getContent();
		// Identity-account have to not exists after account management was started (INVALID identityRole was added)!
		Assert.assertEquals(0, identityAccounts.size());
		
		// Clean identity role
		identityRoleService.delete(irdto);
		
	}

	@Test
	public void defaultAccountAddValid() {

		IdmIdentityDto identity = identityService.getByUsername(IDENTITY_USERNAME);
		IdmRole roleDefault = roleService.getByName(ROLE_DEFAULT);

		Assert.assertNull("No account for this identity can be found, before account management start!",
				helper.findResource("x" + IDENTITY_USERNAME));

		IdmIdentityRoleDto irdto = new IdmIdentityRoleDto();
		irdto.setIdentityContract(identityContractService.findAllByIdentity(identity.getId()).get(0).getId());
		irdto.setRole(roleDefault.getId());
		// This evokes IdentityRole SAVE event. On this event will be start
		// account management and provisioning
		IdmIdentityRoleDto irCreated = identityRoleService.save(irdto);

		IdentityAccountFilter iaccFilter = new IdentityAccountFilter();
		iaccFilter.setIdentityId(identity.getId());
		iaccFilter.setIdentityRoleId(irCreated.getId());
		AccIdentityAccountDto identityAccount = identityAccountService.find(iaccFilter, null).getContent().get(0);
		Assert.assertNotNull("Idenitity account have to exists after account management was started!", identityAccount);
		Assert.assertNotNull("Account have to exists after account management was started!",
				identityAccount.getAccount());
		Assert.assertEquals(accountService.get(identityAccount.getAccount()).getUid(), "x" + IDENTITY_USERNAME);

		TestResource createdAccount = helper.findResource("x" + IDENTITY_USERNAME);
		Assert.assertNotNull("Idenitity have to exists on target system (after account management)", createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());
	}

	@Test
	public void defaultAccountChange() {
		IdmIdentityDto identity = identityService.getByUsername(IDENTITY_USERNAME);
		identity.setFirstName(IDENTITY_CHANGED_FIRST_NAME);

		// This evokes Identity SAVE event. On this event will be start account
		// management and provisioning
		identityService.save(identity);

		TestResource createdAccount = helper.findResource("x" + IDENTITY_USERNAME);
		Assert.assertNotNull("Idenitity have to exists on target system (after account management)", createdAccount);
		Assert.assertEquals("Account on target system, must have changed first name!", IDENTITY_CHANGED_FIRST_NAME,
				createdAccount.getFirstname());
	}

	
	@Test
	public void defaultAccountDisable() {
		IdmIdentityDto identity = identityService.getByUsername(IDENTITY_USERNAME);
		Assert.assertEquals("Identity must be enabled!", Boolean.FALSE,
				identity.isDisabled());
		TestResource resourceAccount = helper.findResource("x" + IDENTITY_USERNAME);
		Assert.assertNotNull("Idenitity have to exists on target system (after account management)", resourceAccount);
		Assert.assertEquals("Account on target system, must be enabled", "enabled",
				resourceAccount.getStatus());
		
		identity.setDisabled(true);
		
		// This evokes Identity SAVE event. On this event will be start account
		// management and provisioning
		identityService.save(identity);
		
		resourceAccount = helper.findResource("x" + IDENTITY_USERNAME);
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

		TestResource createdAccount = helper.findResource("x" + IDENTITY_USERNAME);
		Assert.assertNotNull("Idenitity have to exists on target system (before account will be delete)",
				createdAccount);

		AccountFilter accountFilter = new AccountFilter();
		accountFilter.setUid("x" + IDENTITY_USERNAME);
		Assert.assertEquals("Account needs to exist befor will be delete", 1,
				accountService.find(accountFilter, null).getContent().size());

		IdmIdentityDto identity = identityService.getByUsername(IDENTITY_USERNAME);
		IdentityRoleFilter irfilter = new IdentityRoleFilter();
		irfilter.setIdentityId(identity.getId());
		IdmIdentityRoleDto identityRoleToDelete = identityRoleService.find(irfilter, null).getContent().get(0);

		// This evokes IdentityRole DELETE event. On this event will be start
		// account management and provisioning
		identityRoleService.deleteById(identityRoleToDelete.getId());

		Assert.assertEquals("Account must not be after was deleted", 0,
				accountService.find(accountFilter, null).getContent().size());

		IdentityAccountFilter iaccFilter = new IdentityAccountFilter();
		iaccFilter.setIdentityId(identity.getId());
		Assert.assertEquals("Idenitity account have to not exists after account was deleted!", 0,
				identityAccountService.find(iaccFilter, null).getContent().size());

		createdAccount = helper.findResource("x" + IDENTITY_USERNAME);
		Assert.assertNull("Idenitity have to no exists on target system (after account was deleted)", createdAccount);

		// Reset value
		identity.setFirstName(IDENTITY_USERNAME);
		identityService.save(identity);
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
		IdmIdentityDto identity = identityService.getByUsername(IDENTITY_USERNAME);
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(filter, null).getContent();
	
		TestResource resourceAccount = helper.findResource("x" + IDENTITY_USERNAME);

		// Create new password two
		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setAccounts(ImmutableList.of(identityAccounts.get(0).getAccount().toString()));
		passwordChange.setNewPassword(new GuardedString(IDENTITY_PASSWORD_TWO));
		passwordChange.setIdm(true);
		// Do change of password for selected accounts
		identityService.passwordChange(identity, passwordChange);

		// Check correct password two
		resourceAccount = helper.findResource("x" + IDENTITY_USERNAME);
		Assert.assertEquals("Check same password on target system", IDENTITY_PASSWORD_TWO, resourceAccount.getPassword());

		// Add overloaded password attribute
		IdmRole rolePassword = roleService.getByName(ROLE_OVERLOADING_PASSWORD);

		IdmIdentityRoleDto irdto = new IdmIdentityRoleDto();
		irdto.setIdentityContract(identityContractService.findAllByIdentity(identity.getId()).get(0).getId());
		irdto.setRole(rolePassword.getId());
		// This evokes IdentityRole SAVE event. On this event will be start
		// account management and provisioning
		identityRoleService.save(irdto);
		
		
		// Do change of password for selected accounts
		passwordChange.setNewPassword(new GuardedString(IDENTITY_PASSWORD_THREE));
		identityService.passwordChange(identity, passwordChange);
		// Check correct overloaded password two
		resourceAccount = helper.findResource("x" + IDENTITY_USERNAME);
		Assert.assertEquals("Check overloaded password (added x) on target system", "x"+IDENTITY_PASSWORD_THREE, resourceAccount.getPassword());
	}


	@Test
	public void overloadedAttributeAdd_A_LastNameRole() {
		IdmIdentityDto identity = identityService.getByUsername(IDENTITY_USERNAME);
		IdmRole roleLastName = roleService.getByName(ROLE_OVERLOADING_LAST_NAME);

		Assert.assertNull("No account for this identity can be found, before account management start!",
				helper.findResource("x" + IDENTITY_USERNAME));

		IdmIdentityRoleDto irdto = new IdmIdentityRoleDto();
		irdto.setIdentityContract(identityContractService.findAllByIdentity(identity.getId()).get(0).getId());
		irdto.setRole(roleLastName.getId());
		// This evokes IdentityRole SAVE event. On this event will be start
		// account management and provisioning
		IdmIdentityRoleDto irCreated = identityRoleService.save(irdto);

		IdentityAccountFilter iaccFilter = new IdentityAccountFilter();
		iaccFilter.setIdentityId(identity.getId());
		iaccFilter.setIdentityRoleId(irCreated.getId());
		AccIdentityAccountDto identityAccount = identityAccountService.find(iaccFilter, null).getContent().get(0);
		Assert.assertNotNull("Idenitity account have to exists after account management was started!", identityAccount);
		Assert.assertNotNull("Account have to exists after account management was started!",
				identityAccount.getAccount());
		Assert.assertEquals(accountService.get(identityAccount.getAccount()).getUid(), "x" + IDENTITY_USERNAME);

		TestResource createdAccount = helper.findResource("x" + IDENTITY_USERNAME);
		Assert.assertNotNull("Idenitity have to exists on target system (after account management)", createdAccount);
		Assert.assertEquals(
				"Last name on target system must be equals with email on identity (we use overloded attribute)",
				identity.getEmail(), createdAccount.getLastname());
	}

	@Test
	public void overloadedAttributeAdd_B_DisableFirstNameRole() {
		IdmIdentityDto identity = identityService.getByUsername(IDENTITY_USERNAME);
		IdmRole roleLastName = roleService.getByName(ROLE_OVERLOADING_FIRST_NAME);

		Assert.assertNotNull("Account for this identity have to be found!",
				helper.findResource("x" + IDENTITY_USERNAME));

		IdmIdentityRoleDto irdto = new IdmIdentityRoleDto();
		irdto.setIdentityContract(identityContractService.findAllByIdentity(identity.getId()).get(0).getId());
		irdto.setRole(roleLastName.getId());
		// This evokes IdentityRole SAVE event. On this event will be start
		// account management and provisioning
		identityRoleService.save(irdto);

		IdentityAccountFilter iaccFilter = new IdentityAccountFilter();
		iaccFilter.setIdentityId(identity.getId());
		// Now we have to identity roles (role_overloading_first_name and
		// role_overloading_last_name) and identity accounts
		Assert.assertEquals("Idenitity accounts have to exists (two items) after account management was started!", 2,
				identityAccountService.find(iaccFilter, null).getContent().size());

		TestResource createdAccount = helper.findResource("x" + IDENTITY_USERNAME);
		Assert.assertNotNull("Idenitity have to exists on target system (after account management)", createdAccount);
		Assert.assertEquals("First name on target system must be equals with first name on identity",
				identity.getFirstName(), createdAccount.getFirstname());

		identity.setFirstName(IDENTITY_CHANGED_FIRST_NAME);
		identity.setEmail(IDENTITY_EMAIL_CHANGED);
		// This evokes Identity SAVE event. On this event will be start
		// account management and provisioning
		identityService.save(identity);

		createdAccount = helper.findResource("x" + IDENTITY_USERNAME);
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
		IdmIdentityDto identity = identityService.getByUsername(IDENTITY_USERNAME);
		IdmRole role = roleService.getByName(ROLE_OVERLOADING_Y_ACCOUNT);

		Assert.assertNotNull("Account for this identity have to be found!",
				helper.findResource("x" + IDENTITY_USERNAME));

		IdmIdentityRoleDto irdto = new IdmIdentityRoleDto();
		irdto.setIdentityContract(identityContractService.findAllByIdentity(identity.getId()).get(0).getId());
		irdto.setRole(role.getId());
		// This evokes IdentityRole SAVE event. On this event will be start
		// account management and provisioning
		identityRoleService.save(irdto);

		IdentityAccountFilter iaccFilter = new IdentityAccountFilter();
		iaccFilter.setIdentityId(identity.getId());
		// Now we have to identity roles (role_overloading_first_name and
		// role_overloading_last_name and role_overloading_y_account) and
		// identity accounts
		Assert.assertEquals("Idenitity accounts have to exists (three items) after account management was started!", 3,
				identityAccountService.find(iaccFilter, null).getContent().size());

		TestResource createdAccount = helper.findResource("y" + IDENTITY_USERNAME);
		Assert.assertNotNull("Idenitity have to exists on target system (after account management)", createdAccount);
		Assert.assertEquals("First name on target system must be equals with first name on identity",
				identity.getFirstName(), createdAccount.getFirstname());
		Assert.assertEquals("Last name on target system must be equals with first name on identity",
				identity.getLastName(), createdAccount.getLastname());
	}

	@Transactional
	@Test
	public void overloadedAttributeRemoveAllRoles() {
		IdmIdentityDto identity = identityService.getByUsername(IDENTITY_USERNAME);

		Assert.assertNotNull("Account for this identity have to be found!",
				helper.findResource("x" + IDENTITY_USERNAME));

		IdentityAccountFilter iaccFilter = new IdentityAccountFilter();
		iaccFilter.setIdentityId(identity.getId());
		// Now we have to identity roles (role_overloading_first_name and
		// role_overloading_last_name and role_overloading_y_account) and
		// identity accounts
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(iaccFilter, null).getContent();
		Assert.assertEquals("Idenitity accounts have to exists (four items) after account management was started!", 4,
				identityAccounts.size());

		IdentityRoleFilter irfilter = new IdentityRoleFilter();
		irfilter.setIdentityId(identity.getId());
		identityRoleService.find(irfilter, null).getContent().forEach(identityRole -> {
			identityRoleService.delete(identityRole);
		});

		Assert.assertEquals("Idenitity accounts have to not exist after accounts deleted!", 0,
				identityAccountService.find(iaccFilter, null).getContent().size());
	}

	private void initData() {
		// create test system
		SysSystem system = helper.createTestResourceSystem(true);
		//
		// Create test identity for provisioning test
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername(IDENTITY_USERNAME);
		identity.setFirstName(IDENTITY_USERNAME);
		identity.setLastName(IDENTITY_USERNAME);
		identity.setEmail(IDENTITY_EMAIL);
		identity = identityService.save(identity);

		// Create mapped attributes to schema
		SysSystemMappingDto systemMapping = helper.getDefaultMapping(system);
		SysSystemAttributeMappingDto attributeHandlingLastName = schemaAttributeHandlingService
				.findBySystemMappingAndName(systemMapping.getId(), TestHelper.ATTRIBUTE_MAPPING_LASTNAME);
		SysSystemAttributeMappingDto attributeHandlingPassword = schemaAttributeHandlingService
				.findBySystemMappingAndName(systemMapping.getId(), TestHelper.ATTRIBUTE_MAPPING_PASSWORD);
		SysSystemAttributeMappingDto attributeHandlingFirstName = schemaAttributeHandlingService
				.findBySystemMappingAndName(systemMapping.getId(), TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME);
		SysSystemAttributeMappingDto attributeHandlingUserName = schemaAttributeHandlingService
				.findBySystemMappingAndName(systemMapping.getId(), TestHelper.ATTRIBUTE_MAPPING_NAME);
		// username is transformed
		attributeHandlingUserName.setTransformToResourceScript("return \"" + "x" + IDENTITY_USERNAME + "\";");
		attributeHandlingUserName = schemaAttributeHandlingService.save(attributeHandlingUserName);
		
		/*
		 * Create role with link on system (default)
		 */
		IdmRole roleDefault = new IdmRole();
		roleDefault.setName(ROLE_DEFAULT);
		roleService.save(roleDefault);
		SysRoleSystem roleSystemDefault = new SysRoleSystem();
		roleSystemDefault.setRole(roleDefault);
		roleSystemDefault.setSystem(system);
		roleSystemDefault.setSystemMapping(systemMappingRepository.findOne(systemMapping.getId()));
		roleSystemService.save(roleSystemDefault);

		/*
		 * Create role with link on system (overloading last name attribute)
		 */
		IdmRole roleOverloadingLastName = new IdmRole();
		roleOverloadingLastName.setName(ROLE_OVERLOADING_LAST_NAME);
		roleService.save(roleOverloadingLastName);
		SysRoleSystem roleSystemLastName = new SysRoleSystem();
		roleSystemLastName.setRole(roleOverloadingLastName);
		roleSystemLastName.setSystem(system);
		roleSystemLastName.setSystemMapping(systemMappingRepository.findOne(systemMapping.getId()));
		roleSystemService.save(roleSystemLastName);

		// Attribute for overloading last name attribute
		SysRoleSystemAttribute attributeLastName = new SysRoleSystemAttribute();
		attributeLastName.setEntityAttribute(true);
		attributeLastName.setIdmPropertyName("email");
		attributeLastName.setName("Overloaded lastName with email");
		attributeLastName.setRoleSystem(roleSystemLastName);
		attributeLastName.setSystemAttributeMapping(systemAttributeMappingRepository.findOne(attributeHandlingLastName.getId()));
		roleSystemAttributeService.save(attributeLastName);
		
		/*
		 * Create role with link on system (overloading password attribute)
		 */
		IdmRole roleOverloadingPassword = new IdmRole();
		roleOverloadingPassword.setName(ROLE_OVERLOADING_PASSWORD);
		roleService.save(roleOverloadingPassword);
		SysRoleSystem roleSystemPassword = new SysRoleSystem();
		roleSystemPassword.setRole(roleOverloadingPassword);
		roleSystemPassword.setSystem(system);
		roleSystemPassword.setSystemMapping(systemMappingRepository.findOne(systemMapping.getId()));
		roleSystemService.save(roleSystemPassword);

		// Attribute for overloading last name attribute
		SysRoleSystemAttribute attributePassword = new SysRoleSystemAttribute();
		attributePassword.setEntityAttribute(true);
		attributePassword.setIdmPropertyName("password");
		attributePassword.setConfidentialAttribute(true);
		attributePassword.setName("Overloaded password - add x");
		attributePassword.setRoleSystem(roleSystemPassword);
		attributePassword.setSystemAttributeMapping(systemAttributeMappingRepository.findOne(attributeHandlingPassword.getId()));
		attributePassword.setTransformScript("return new "+GuardedString.class.getName()+"(\"x\"+attributeValue.asString());");
		roleSystemAttributeService.save(attributePassword);

		/*
		 * Create role with link on system (overloading (disable) first name
		 * attribute)
		 */
		IdmRole roleOverloadingFirstName = new IdmRole();
		roleOverloadingFirstName.setName(ROLE_OVERLOADING_FIRST_NAME);
		roleService.save(roleOverloadingFirstName);
		SysRoleSystem roleSystemFirstName = new SysRoleSystem();
		roleSystemFirstName.setRole(roleOverloadingFirstName);
		roleSystemFirstName.setSystem(system);
		roleSystemFirstName.setSystemMapping(systemMappingRepository.findOne(systemMapping.getId()));
		roleSystemService.save(roleSystemFirstName);

		// Attribute for overloading first name attribute (disable him)
		SysRoleSystemAttribute attributeFirstName = new SysRoleSystemAttribute();
		attributeFirstName.setDisabledDefaultAttribute(true);
		attributeFirstName.setName("Disable first name");
		attributeFirstName.setRoleSystem(roleSystemFirstName);
		attributeFirstName.setSystemAttributeMapping(systemAttributeMappingRepository.findOne(attributeHandlingFirstName.getId()));
		roleSystemAttributeService.save(attributeFirstName);

		/*
		 * Create role with link on system (overloading name attribute ...
		 * create Y account)
		 */
		IdmRole roleOverloadingName = new IdmRole();
		roleOverloadingName.setName(ROLE_OVERLOADING_Y_ACCOUNT);
		roleService.save(roleOverloadingName);
		SysRoleSystem roleSystemName = new SysRoleSystem();
		roleSystemName.setRole(roleOverloadingName);
		roleSystemName.setSystem(system);
		roleSystemName.setSystemMapping(systemMappingRepository.findOne(systemMapping.getId()));
		roleSystemService.save(roleSystemName);

		// Attribute for overloading first name attribute (disable him)
		SysRoleSystemAttribute attributeName = new SysRoleSystemAttribute();
		attributeName.setUid(true);
		attributeName.setEntityAttribute(true);
		attributeName.setIdmPropertyName("username");
		attributeName.setName("Account with Y-prefix name");
		attributeName.setTransformScript("return \"y\" + attributeValue ;");
		attributeName.setRoleSystem(roleSystemName);
		attributeName.setSystemAttributeMapping(systemAttributeMappingRepository.findOne(attributeHandlingUserName.getId()));
		roleSystemAttributeService.save(attributeName);
	}

}
