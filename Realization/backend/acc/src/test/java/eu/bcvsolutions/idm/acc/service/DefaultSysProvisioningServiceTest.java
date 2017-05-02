package eu.bcvsolutions.idm.acc.service;

import static org.junit.Assert.fail;

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

import com.google.common.collect.ImmutableList;

import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.MappingAttributeDto;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.domain.IdmPasswordPolicyGenerateType;
import eu.bcvsolutions.idm.core.model.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityFormValue;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Provisioning tests
 * 
 * @author Svanda
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DefaultSysProvisioningServiceTest extends AbstractIntegrationTest {

	private static final String IDENTITY_PASSWORD_ONE = "password_one";
	private static final String IDENTITY_PASSWORD_TWO = "password_two";
	private static final String IDENTITY_PASSWORD_THREE = "password_three";
	private static final String IDENTITY_USERNAME = "provisioningTestUser";
	private static final String IDENTITY_EXT_PASSWORD = "passwordExt";
	private static final String IDENTITY_CHANGED_FIRST_NAME = "changed first name";
	private static final String PASSWORD_POLICY = "passwordPolicy";
	private static final String EMAIL_ONE = "one.email@one.cz";
	private static final String EMAIL_TWO = "two.email@two.cz";

	@Autowired
	private SysSystemService sysSystemService;

	@Autowired
	private FormService formService;

	@Autowired
	private IdmIdentityService idmIdentityService;

	@Autowired
	private AccIdentityAccountService identityAccoutnService;

	@Autowired
	private AccAccountService accountService;

	@Autowired
	private ProvisioningService provisioningService;

	@Autowired
	private SysSystemMappingService systemEntityHandlingService;

	@Autowired
	private SysSystemAttributeMappingService schemaAttributeHandlingService;

	@Autowired
	private SysSchemaAttributeService schemaAttributeService;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private IdmPasswordPolicyService passwordPolicyService;

	@Autowired
	DataSource dataSource;

	// Only for call method createTestSystem
	@Autowired
	private DefaultSysAccountManagementServiceTest defaultSysAccountManagementServiceTest;

	@Before
	public void init() {
		loginAsAdmin("admin");
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void doIdentityProvisioningAddAccount() {
		initData();

		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		AccIdentityAccountDto accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);

		provisioningService.doProvisioning(idmIdentityService.get(accountIdentityOne.getIdentity()));

		TestResource createdAccount = entityManager.find(TestResource.class, accountService.get(accountIdentityOne.getAccount()).getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());
	}

	@Test
	public void doIdentityProvisioningChangeAccount() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());

		AccIdentityAccountDto accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);
		TestResource createdAccount = entityManager.find(TestResource.class, accountService.get(accountIdentityOne.getAccount()).getUid());

		identity.setFirstName(IDENTITY_CHANGED_FIRST_NAME);
		identity = idmIdentityService.save(identity);
		Assert.assertNotEquals(identity.getFirstName(), createdAccount.getFirstname());

		provisioningService.doProvisioning(identity);
		TestResource changedAccount = entityManager.find(TestResource.class, accountService.get(accountIdentityOne.getAccount()).getUid());
		Assert.assertNotNull(changedAccount);
		Assert.assertEquals(identity.getFirstName(), changedAccount.getFirstname());
	}

	@Test
	public void doIdentityProvisioningChangeAccountTransformFromResource() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());

		AccIdentityAccountDto accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);
		TestResource createdAccount = entityManager.find(TestResource.class, accountService.get(accountIdentityOne.getAccount()).getUid());

		identity.setFirstName(IDENTITY_CHANGED_FIRST_NAME.substring(1));
		identity = idmIdentityService.save(identity);
		Assert.assertNotEquals(identity.getFirstName(), createdAccount.getFirstname());

		provisioningService.doProvisioning(identity);
		TestResource changedAccount = entityManager.find(TestResource.class, accountService.get(accountIdentityOne.getAccount()).getUid());
		Assert.assertNotNull(changedAccount);
		// Must be with "c" on target system, because we have set transformation
		// from system!
		Assert.assertEquals(identity.getFirstName(), changedAccount.getFirstname().substring(1));
	}

	@Test
	public void doIdentityProvisioningChangeSingleAttribute() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		identity.setFirstName(IDENTITY_CHANGED_FIRST_NAME);
		identity = idmIdentityService.save(identity);
		Assert.assertEquals("Identity must have this first name!", IDENTITY_CHANGED_FIRST_NAME,
				identity.getFirstName());

		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		AccIdentityAccountDto accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);
		AccAccount account = accountService.get(accountIdentityOne.getAccount());
		SysSystem system = account.getSystem();
		SysSystemEntity systemEntity = account.getSystemEntity();

		SystemAttributeMappingFilter attributeFilter = new SystemAttributeMappingFilter();
		attributeFilter.setSystemId(system.getId());
		attributeFilter.setIdmPropertyName("firstName");

		TestResource resourceAccount = entityManager.find(TestResource.class, "x" + IDENTITY_USERNAME);
		Assert.assertNotNull("Idenitity have to exists on target system (after account management)", resourceAccount);
		Assert.assertEquals("Account on target system, must have same firsta name as Identity",
				IDENTITY_CHANGED_FIRST_NAME, resourceAccount.getFirstname());

		provisioningService.doProvisioningForAttribute(systemEntity,
				schemaAttributeHandlingService.find(attributeFilter, null).getContent().get(0), IDENTITY_USERNAME,
				ProvisioningOperationType.UPDATE, identity);

		resourceAccount = entityManager.find(TestResource.class, "x" + IDENTITY_USERNAME);
		Assert.assertNotNull("Idenitity have to exists on target system (after account management)", resourceAccount);
		Assert.assertEquals("Account on target system, must have changed first name!", IDENTITY_USERNAME,
				resourceAccount.getFirstname());
	}

	@Test
	public void doIdentityProvisioningChangePassword() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		AccIdentityAccountDto accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);
		SysSystem system = accountService.get(accountIdentityOne.getAccount()).getSystem();

		// Check empty password
		// provisioningService.authenticate(accountIdentityOne.getAccount().getUid(),
		// new GuardedString(), system, SystemEntityType.IDENTITY);

		// Create new password one
		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setIdentity(identity.getId().toString());
		passwordChange.setAccounts(ImmutableList.of(accountIdentityOne.getId().toString()));
		passwordChange.setNewPassword(new GuardedString(IDENTITY_PASSWORD_ONE));
		passwordChange.setIdm(true);
		// Do change of password for selected accounts
		idmIdentityService.passwordChange(identity, passwordChange);
		accountIdentityOne = identityAccoutnService.get(accountIdentityOne.getId());

		// Check correct password One
		provisioningService.authenticate(accountService.get(accountIdentityOne.getAccount()).getUid(),
				new GuardedString(IDENTITY_PASSWORD_ONE), system, SystemEntityType.IDENTITY);

		// Check incorrect password
		try {
			provisioningService.authenticate(accountService.get(accountIdentityOne.getAccount()).getUid(),
					new GuardedString(IDENTITY_PASSWORD_TWO), system, SystemEntityType.IDENTITY);
			fail("Bad credentials exception is expected here!");
		} catch (ResultCodeException ex) {
			//
		}
		// Do change of password for selected accounts
		passwordChange.setNewPassword(new GuardedString(IDENTITY_PASSWORD_TWO));
		idmIdentityService.passwordChange(idmIdentityService.get(accountIdentityOne.getIdentity()), passwordChange);

		// Check correct password Two
		accountIdentityOne = identityAccoutnService.get(accountIdentityOne.getId());
		provisioningService.authenticate(accountService.get(accountIdentityOne.getAccount()).getUid(),
				new GuardedString(IDENTITY_PASSWORD_TWO), system, SystemEntityType.IDENTITY);
	}

	@Test
	public void doIdentityProvisioningZRemoveAccount() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		AccIdentityAccountDto accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);

		// Delete account
		accountService.deleteById(accountIdentityOne.getAccount());
		Assert.assertNull(accountService.get(accountIdentityOne.getAccount()));
	}

	@Test
	public void doIdentityProvisioningExtendedAttribute() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);

		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		AccIdentityAccountDto accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);

		// We will use firstName attribute (password attribute is not returned
		// by default)
		SystemAttributeMappingFilter filterSchemaAttr = new SystemAttributeMappingFilter();
		filterSchemaAttr.setIdmPropertyName("firstName");
		filterSchemaAttr.setSystemId(accountService.get(accountIdentityOne.getAccount()).getSystem().getId());
		SysSystemAttributeMapping attributeHandling = schemaAttributeHandlingService.find(filterSchemaAttr, null)
				.getContent().get(0);
		// Set attribute to extended attribute and modify idmPropety to
		// extPassword
		attributeHandling.setIdmPropertyName(IDENTITY_EXT_PASSWORD);
		attributeHandling.setExtendedAttribute(true);
		attributeHandling.setConfidentialAttribute(true);
		attributeHandling.setEntityAttribute(false);
		attributeHandling.setTransformToResourceScript("return attributeValue");
		// Form attribute definition will be created during save attribute
		// handling
		schemaAttributeHandlingService.save(attributeHandling);

		// Create extended attribute value for password
		IdmFormDefinition formDefinition = formService.getDefinition(IdmIdentity.class.getCanonicalName());
		List<IdmIdentityFormValue> values = new ArrayList<>();
		IdmIdentityFormValue phoneValue = new IdmIdentityFormValue();
		phoneValue.setFormAttribute(formDefinition.getMappedAttributeByName(IDENTITY_EXT_PASSWORD));
		phoneValue.setStringValue(IDENTITY_PASSWORD_THREE);
		values.add(phoneValue);
		formService.saveValues(identity, formDefinition, values);

		// save account
		provisioningService.doProvisioning(identity);
		TestResource resourceAccoutn = entityManager.find(TestResource.class, accountService.get(accountIdentityOne.getAccount()).getUid());
		Assert.assertEquals(IDENTITY_PASSWORD_THREE, resourceAccoutn.getFirstname());
		;
	}

	@Test
	public void doIdentityProvisioningStrategyCreate() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);

		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		AccIdentityAccountDto accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);

		// Default email strategy is CREATE, we check value
		TestResource resourceAccoutn = entityManager.find(TestResource.class, accountService.get(accountIdentityOne.getAccount()).getUid());
		Assert.assertEquals(EMAIL_ONE, resourceAccoutn.getEmail());

		SystemAttributeMappingFilter filterSchemaAttr = new SystemAttributeMappingFilter();
		filterSchemaAttr.setIdmPropertyName("email");
		filterSchemaAttr.setSystemId(accountService.get(accountIdentityOne.getAccount()).getSystem().getId());
		SysSystemAttributeMapping attributeHandling = schemaAttributeHandlingService.find(filterSchemaAttr, null)
				.getContent().get(0);

		attributeHandling.setEntityAttribute(true);
		attributeHandling.setStrategyType(AttributeMappingStrategyType.CREATE);
		attributeHandling.setTransformToResourceScript("return \"" + EMAIL_TWO + "\";");
		schemaAttributeHandlingService.save(attributeHandling);

		// Do provisioning
		provisioningService.doProvisioning(identity);
		// Email strategy is CREATE ... email in account must not have new value 
		resourceAccoutn = entityManager.find(TestResource.class, accountService.get(accountIdentityOne.getAccount()).getUid());
		Assert.assertNotEquals(EMAIL_TWO, resourceAccoutn.getEmail());
	}
	
	@Test
	public void doIdentityProvisioningStrategyIfNull() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);

		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		AccIdentityAccountDto accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);

		// Default email strategy is CREATE, we check value
		TestResource resourceAccoutn = entityManager.find(TestResource.class, accountService.get(accountIdentityOne.getAccount()).getUid());
		Assert.assertEquals(EMAIL_ONE, resourceAccoutn.getEmail());

		SystemAttributeMappingFilter filterSchemaAttr = new SystemAttributeMappingFilter();
		filterSchemaAttr.setIdmPropertyName("email");
		filterSchemaAttr.setSystemId(accountService.get(accountIdentityOne.getAccount()).getSystem().getId());
		SysSystemAttributeMapping attributeHandling = schemaAttributeHandlingService.find(filterSchemaAttr, null)
				.getContent().get(0);

		attributeHandling.setEntityAttribute(true);
		attributeHandling.setStrategyType(AttributeMappingStrategyType.WRITE_IF_NULL);
		attributeHandling.setTransformToResourceScript("return \"" + EMAIL_TWO + "\";");
		schemaAttributeHandlingService.save(attributeHandling);

		// Do provisioning
		provisioningService.doProvisioning(identity);
		// Email strategy is WRITE_IF_NULL ... email in account must not have new value 
		resourceAccoutn = entityManager.find(TestResource.class, accountService.get(accountIdentityOne.getAccount()).getUid());
		Assert.assertNotEquals(EMAIL_TWO, resourceAccoutn.getEmail());
		
		attributeHandling.setEntityAttribute(true);
		attributeHandling.setStrategyType(AttributeMappingStrategyType.SET);
		attributeHandling.setTransformToResourceScript("return \"" + EMAIL_TWO + "\";");
		schemaAttributeHandlingService.save(attributeHandling);
		
		// Do provisioning
		provisioningService.doProvisioning(identity);
		// Email strategy is SET ... email in account must have new value 
		resourceAccoutn = entityManager.find(TestResource.class, accountService.get(accountIdentityOne.getAccount()).getUid());
		Assert.assertEquals(EMAIL_TWO, resourceAccoutn.getEmail());
	}

	
	@Test
	public void doIdentityProvisioningStrategySendOnlyIfNotNull() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);

		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		AccIdentityAccountDto accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);

		// Init value check
		TestResource resourceAccoutn = entityManager.find(TestResource.class, accountService.get(accountIdentityOne.getAccount()).getUid());
		Assert.assertEquals(EMAIL_TWO, resourceAccoutn.getEmail());

		SystemAttributeMappingFilter filterSchemaAttr = new SystemAttributeMappingFilter();
		filterSchemaAttr.setIdmPropertyName("email");
		filterSchemaAttr.setSystemId(accountService.get(accountIdentityOne.getAccount()).getSystem().getId());
		SysSystemAttributeMapping attributeHandling = schemaAttributeHandlingService.find(filterSchemaAttr, null)
				.getContent().get(0);

		attributeHandling.setEntityAttribute(true);
		attributeHandling.setStrategyType(AttributeMappingStrategyType.SET);
		attributeHandling.setSendOnlyIfNotNull(true);
		attributeHandling.setTransformToResourceScript("return null");
		schemaAttributeHandlingService.save(attributeHandling);

		// Do provisioning
		provisioningService.doProvisioning(identity);
		// Email strategy is SendOnlyIfNotNull ... email in account must have old value
		resourceAccoutn = entityManager.find(TestResource.class, accountService.get(accountIdentityOne.getAccount()).getUid());
		Assert.assertEquals(EMAIL_TWO, resourceAccoutn.getEmail());
		
		attributeHandling.setStrategyType(AttributeMappingStrategyType.SET);
		attributeHandling.setTransformToResourceScript("return \"\";");
		schemaAttributeHandlingService.save(attributeHandling);
		
		// Do provisioning
		provisioningService.doProvisioning(identity);
		// Email strategy is SendOnlyIfNotNull (value is empty string) ... email in account must have old value
		resourceAccoutn = entityManager.find(TestResource.class, accountService.get(accountIdentityOne.getAccount()).getUid());
		Assert.assertEquals(EMAIL_TWO, resourceAccoutn.getEmail());
		
		attributeHandling.setStrategyType(AttributeMappingStrategyType.SET);
		attributeHandling.setTransformToResourceScript("return \"" + EMAIL_ONE + "\";");
		schemaAttributeHandlingService.save(attributeHandling);
		
		// Do provisioning
		provisioningService.doProvisioning(identity);
		// Email strategy is SendOnlyIfNotNull  (value is not null and not empty)... email in account must have new value 
		resourceAccoutn = entityManager.find(TestResource.class, accountService.get(accountIdentityOne.getAccount()).getUid());
		Assert.assertEquals(EMAIL_ONE, resourceAccoutn.getEmail());
	}
	
	
	@Test()
	public void doIdentityProvisioningStrategyMerge() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);

		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		AccIdentityAccountDto accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);

		SystemAttributeMappingFilter filterSchemaAttr = new SystemAttributeMappingFilter();
		filterSchemaAttr.setIdmPropertyName("email");
		filterSchemaAttr.setSystemId(accountService.get(accountIdentityOne.getAccount()).getSystem().getId());
		SysSystemAttributeMapping attributeHandling = schemaAttributeHandlingService.find(filterSchemaAttr, null)
				.getContent().get(0);

		attributeHandling.setEntityAttribute(true);
		attributeHandling.setStrategyType(AttributeMappingStrategyType.MERGE);
		attributeHandling.getSchemaAttribute().setMultivalued(true);
		schemaAttributeService.save(attributeHandling.getSchemaAttribute());
		schemaAttributeHandlingService.save(attributeHandling);

		// Do provisioning
		provisioningService.doProvisioning(identity);
	}
	
	// Expected PROVISIONING_MERGE_ATTRIBUTE_IS_NOT_MULTIVALUE
	@Test(expected = ProvisioningException.class)
	public void doIdentityProvisioningStrategyMergeException() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);

		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		AccIdentityAccountDto accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);

		SystemAttributeMappingFilter filterSchemaAttr = new SystemAttributeMappingFilter();
		filterSchemaAttr.setIdmPropertyName("email");
		filterSchemaAttr.setSystemId(accountService.get(accountIdentityOne.getAccount()).getSystem().getId());
		SysSystemAttributeMapping attributeHandling = schemaAttributeHandlingService.find(filterSchemaAttr, null)
				.getContent().get(0);

		attributeHandling.setEntityAttribute(true);
		attributeHandling.setStrategyType(AttributeMappingStrategyType.MERGE);
		attributeHandling.getSchemaAttribute().setMultivalued(false);
		schemaAttributeService.save(attributeHandling.getSchemaAttribute());
		schemaAttributeHandlingService.save(attributeHandling);

		// Do provisioning
		provisioningService.doProvisioning(identity);
	}

	
	@Test
	public void doIdentityProvisioningAndPasswordCheck() {
		IdmIdentity existIdentity = idmIdentityService.getByName(IDENTITY_USERNAME);
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(existIdentity.getId());
		AccIdentityAccountDto accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);

		TestResource createdAccount = entityManager.find(TestResource.class, accountService.get(accountIdentityOne.getAccount()).getUid());

		Assert.assertNotNull(createdAccount);
		// password must be exactly two 'a' characters, see setting for password
		// policy
		Assert.assertEquals("aa", createdAccount.getPassword());
	}

	@Test
	public void compileAttributesDefaultTest() {
		List<AttributeMapping> defaultAttributes = new ArrayList<>();
		List<SysRoleSystemAttribute> overloadingAttributes = new ArrayList<>();

		AttributeMapping defOne = new MappingAttributeDto();
		defOne.setEntityAttribute(true);
		defOne.setStrategyType(AttributeMappingStrategyType.SET);
		defOne.setIdmPropertyName("one");
		defOne.setName("defOne");
		defOne.setDisabledAttribute(true);
		defaultAttributes.add(defOne);

		AttributeMapping defTwo = new MappingAttributeDto();
		defTwo.setEntityAttribute(true);
		defTwo.setStrategyType(AttributeMappingStrategyType.SET);
		defTwo.setIdmPropertyName("two");
		defTwo.setName("defTwo");
		defaultAttributes.add(defTwo);

		List<AttributeMapping> compilledAttributes = provisioningService.compileAttributes(defaultAttributes,
				overloadingAttributes, SystemEntityType.IDENTITY);
		Assert.assertEquals(1, compilledAttributes.size());
		Assert.assertEquals("defTwo", compilledAttributes.get(0).getName());
	}

	@Test
	public void compileAttributesOverrloadedDisabledTest() {
		List<AttributeMapping> defaultAttributes = new ArrayList<>();
		List<SysRoleSystemAttribute> overloadingAttributes = new ArrayList<>();

		SysSchemaAttribute attOne = new SysSchemaAttribute();
		attOne.setName("attOne");
		SysSchemaAttribute attTwo = new SysSchemaAttribute();
		attTwo.setName("attTwo");

		SysSystemAttributeMapping defOne = new SysSystemAttributeMapping();
		defOne.setEntityAttribute(true);
		defOne.setIdmPropertyName("one");
		defOne.setName("defOne");
		defOne.setDisabledAttribute(true);
		defOne.setSchemaAttribute(attOne);
		defaultAttributes.add(defOne);

		SysSystemAttributeMapping defTwo = new SysSystemAttributeMapping();
		defTwo.setEntityAttribute(true);
		defTwo.setIdmPropertyName("two");
		defTwo.setName("defTwo");
		defTwo.setSchemaAttribute(attTwo);
		defaultAttributes.add(defTwo);

		IdmRole roleOne = new IdmRole();
		roleOne.setName("roleOne");
		roleOne.setPriority(100);

		SysRoleSystem roleSystem = new SysRoleSystem();
		roleSystem.setRole(roleOne);

		SysRoleSystemAttribute overloadedOne = new SysRoleSystemAttribute();
		overloadedOne.setSystemAttributeMapping(defOne);
		overloadedOne.setEntityAttribute(true);
		overloadedOne.setIdmPropertyName("one");
		overloadedOne.setName("defOneOverloaded");
		overloadedOne.setDisabledDefaultAttribute(false);
		overloadedOne.setRoleSystem(roleSystem);
		overloadingAttributes.add(overloadedOne);

		List<AttributeMapping> compilledAttributes = provisioningService.compileAttributes(defaultAttributes,
				overloadingAttributes, SystemEntityType.IDENTITY);
		Assert.assertEquals(2, compilledAttributes.size());
		Assert.assertTrue(compilledAttributes.stream().filter(attribute -> {
			return "defOneOverloaded".equals(attribute.getName());
		}).findFirst().isPresent());
	}

	@Test
	public void compileAttributesOverrloadedSamePriorityTest() {
		List<SysRoleSystemAttribute> overloadingAttributes = new ArrayList<>();
		List<AttributeMapping> defaultAttributes = new ArrayList<>();

		initOverloadedAttributes(overloadingAttributes, defaultAttributes);

		List<AttributeMapping> compilledAttributes = provisioningService.compileAttributes(defaultAttributes,
				overloadingAttributes, SystemEntityType.IDENTITY);
		Assert.assertEquals(2, compilledAttributes.size());
		Assert.assertTrue(compilledAttributes.stream().filter(attribute -> {
			return "defOneOverloadedRoleTwo".equals(attribute.getName());
		}).findFirst().isPresent());

		// set name role One to zroleOne
		overloadingAttributes.get(0).getRoleSystem().getRole().setName("zroleOne");

		compilledAttributes = provisioningService.compileAttributes(defaultAttributes, overloadingAttributes, SystemEntityType.IDENTITY);
		Assert.assertEquals(2, compilledAttributes.size());
		Assert.assertTrue(compilledAttributes.stream().filter(attribute -> {
			return "defOneOverloaded".equals(attribute.getName());
		}).findFirst().isPresent());

		// We set role mapping attribute to disabled, then must have higher
		// "priority", then role mapping one
		// and must missing in result
		overloadingAttributes.get(1).setDisabledDefaultAttribute(true);

		compilledAttributes = provisioningService.compileAttributes(defaultAttributes, overloadingAttributes, SystemEntityType.IDENTITY);
		Assert.assertEquals(1, compilledAttributes.size());
	}

	@Test
	public void compileAttributesOverrloadedDiffPriorityTest() {
		List<SysRoleSystemAttribute> overloadingAttributes = new ArrayList<>();
		List<AttributeMapping> defaultAttributes = new ArrayList<>();

		initOverloadedAttributes(overloadingAttributes, defaultAttributes);

		// roleOne
		overloadingAttributes.get(0).getRoleSystem().getRole().setPriority(200);
		// roleTwo
		overloadingAttributes.get(1).getRoleSystem().getRole().setPriority(100);

		List<AttributeMapping> compilledAttributes = provisioningService.compileAttributes(defaultAttributes,
				overloadingAttributes, SystemEntityType.IDENTITY);
		Assert.assertEquals(2, compilledAttributes.size());
		Assert.assertTrue(compilledAttributes.stream().filter(attribute -> {
			return "defOneOverloaded".equals(attribute.getName());
		}).findFirst().isPresent());
	}

	@Test
	public void compileAttributesOverrloadedStrategyMergeTest() {
		List<SysRoleSystemAttribute> overloadingAttributes = new ArrayList<>();
		List<AttributeMapping> defaultAttributes = new ArrayList<>();

		initOverloadedAttributes(overloadingAttributes, defaultAttributes);

		// roleOne
		overloadingAttributes.get(0).getRoleSystem().getRole().setPriority(200);
		// roleTwo
		overloadingAttributes.get(1).getRoleSystem().getRole().setPriority(100);

		// overloadedRoleOne
		overloadingAttributes.get(0).setStrategyType(AttributeMappingStrategyType.MERGE);
		// overloadedRoleTwo
		overloadingAttributes.get(1).setStrategyType(AttributeMappingStrategyType.MERGE);

		List<AttributeMapping> compilledAttributes = provisioningService.compileAttributes(defaultAttributes,
				overloadingAttributes, SystemEntityType.IDENTITY);
		Assert.assertEquals(3, compilledAttributes.size());
		Assert.assertTrue(compilledAttributes.stream().filter(attribute -> {
			return "defOneOverloadedRoleTwo".equals(attribute.getName());
		}).findFirst().isPresent());
	}

	@Test
	public void compileAttributesOverrloadedStrategyMergeAuthoTest() {
		List<SysRoleSystemAttribute> overloadingAttributes = new ArrayList<>();
		List<AttributeMapping> defaultAttributes = new ArrayList<>();

		initOverloadedAttributes(overloadingAttributes, defaultAttributes);

		// roleOne
		overloadingAttributes.get(0).getRoleSystem().getRole().setPriority(200);
		// roleTwo
		overloadingAttributes.get(1).getRoleSystem().getRole().setPriority(100);

		// overloadedRoleOne
		overloadingAttributes.get(0).setStrategyType(AttributeMappingStrategyType.AUTHORITATIVE_MERGE);
		// overloadedRoleTwo
		overloadingAttributes.get(1).setStrategyType(AttributeMappingStrategyType.AUTHORITATIVE_MERGE);

		List<AttributeMapping> compilledAttributes = provisioningService.compileAttributes(defaultAttributes,
				overloadingAttributes, SystemEntityType.IDENTITY);
		Assert.assertEquals(3, compilledAttributes.size());
		Assert.assertTrue(compilledAttributes.stream().filter(attribute -> {
			return "defOneOverloadedRoleTwo".equals(attribute.getName());
		}).findFirst().isPresent());
	}

	@Test
	public void compileAttributesOverrloadedStrategyMergeAuthoDisableTest() {
		List<SysRoleSystemAttribute> overloadingAttributes = new ArrayList<>();
		List<AttributeMapping> defaultAttributes = new ArrayList<>();

		initOverloadedAttributes(overloadingAttributes, defaultAttributes);

		// roleOne
		overloadingAttributes.get(0).getRoleSystem().getRole().setPriority(200);
		// roleTwo
		overloadingAttributes.get(1).getRoleSystem().getRole().setPriority(500);

		// overloadedRoleOne
		overloadingAttributes.get(0).setStrategyType(AttributeMappingStrategyType.AUTHORITATIVE_MERGE);
		// overloadedRoleTwo
		overloadingAttributes.get(1).setStrategyType(AttributeMappingStrategyType.AUTHORITATIVE_MERGE);
		overloadingAttributes.get(1).setDisabledDefaultAttribute(true);

		List<AttributeMapping> compilledAttributes = provisioningService.compileAttributes(defaultAttributes,
				overloadingAttributes, SystemEntityType.IDENTITY);
		Assert.assertEquals(2, compilledAttributes.size());
		Assert.assertTrue(compilledAttributes.stream().filter(attribute -> {
			return "defOneOverloaded".equals(attribute.getName());
		}).findFirst().isPresent());
	}

	@Test(expected = ProvisioningException.class)
	public void compileAttributesOverrloadedConflictStrategies() {
		List<SysRoleSystemAttribute> overloadingAttributes = new ArrayList<>();
		List<AttributeMapping> defaultAttributes = new ArrayList<>();

		initOverloadedAttributes(overloadingAttributes, defaultAttributes);

		// roleOne
		overloadingAttributes.get(0).getRoleSystem().getRole().setPriority(200);
		// roleTwo
		overloadingAttributes.get(1).getRoleSystem().getRole().setPriority(500);

		// overloadedRoleOne
		overloadingAttributes.get(0).setStrategyType(AttributeMappingStrategyType.SET);
		// overloadedRoleTwo
		overloadingAttributes.get(1).setStrategyType(AttributeMappingStrategyType.AUTHORITATIVE_MERGE);

		provisioningService.compileAttributes(defaultAttributes, overloadingAttributes, SystemEntityType.IDENTITY);
	}

	@Test
	public void compileAttributesOverrloadedStrategyCreateTest() {
		List<SysRoleSystemAttribute> overloadingAttributes = new ArrayList<>();
		List<AttributeMapping> defaultAttributes = new ArrayList<>();

		initOverloadedAttributes(overloadingAttributes, defaultAttributes);

		// roleOne
		overloadingAttributes.get(0).getRoleSystem().getRole().setPriority(200);
		// roleTwo
		overloadingAttributes.get(1).getRoleSystem().getRole().setPriority(500);

		// overloadedRoleOne
		overloadingAttributes.get(0).setStrategyType(AttributeMappingStrategyType.CREATE);
		// overloadedRoleTwo
		overloadingAttributes.get(1).setStrategyType(AttributeMappingStrategyType.CREATE);

		List<AttributeMapping> compilledAttributes = provisioningService.compileAttributes(defaultAttributes,
				overloadingAttributes, SystemEntityType.IDENTITY);
		Assert.assertEquals(2, compilledAttributes.size());
		Assert.assertTrue(compilledAttributes.stream().filter(attribute -> {
			return "defOneOverloadedRoleTwo".equals(attribute.getName());
		}).findFirst().isPresent());
	}

	private void initOverloadedAttributes(List<SysRoleSystemAttribute> overloadingAttributes,
			List<AttributeMapping> defaultAttributes) {
		SysSchemaAttribute attOne = new SysSchemaAttribute();
		attOne.setName("attOne");
		SysSchemaAttribute attTwo = new SysSchemaAttribute();
		attTwo.setName("attTwo");

		SysSystemAttributeMapping defOne = new SysSystemAttributeMapping();
		defOne.setEntityAttribute(true);
		defOne.setIdmPropertyName("one");
		defOne.setName("defOne");
		defOne.setDisabledAttribute(true);
		defOne.setSchemaAttribute(attOne);
		defaultAttributes.add(defOne);

		SysSystemAttributeMapping defTwo = new SysSystemAttributeMapping();
		defTwo.setEntityAttribute(true);
		defTwo.setIdmPropertyName("two");
		defTwo.setName("defTwo");
		defTwo.setSchemaAttribute(attTwo);
		defaultAttributes.add(defTwo);

		IdmRole roleTwo = new IdmRole();
		roleTwo.setName("roleTwo");
		roleTwo.setPriority(100);

		IdmRole roleOne = new IdmRole();
		roleOne.setName("roleOne");
		roleOne.setPriority(100);

		SysRoleSystem roleSystemTwo = new SysRoleSystem();
		roleSystemTwo.setRole(roleTwo);

		SysRoleSystem roleSystemOne = new SysRoleSystem();
		roleSystemOne.setRole(roleOne);

		SysRoleSystemAttribute overloadedRoleOne = new SysRoleSystemAttribute();
		overloadedRoleOne.setSystemAttributeMapping(defOne);
		overloadedRoleOne.setEntityAttribute(true);
		overloadedRoleOne.setIdmPropertyName("one");
		overloadedRoleOne.setName("defOneOverloaded");
		overloadedRoleOne.setDisabledDefaultAttribute(false);
		overloadedRoleOne.setRoleSystem(roleSystemOne);
		overloadingAttributes.add(overloadedRoleOne);

		SysRoleSystemAttribute overloadedRoleTwo = new SysRoleSystemAttribute();
		overloadedRoleTwo.setSystemAttributeMapping(defOne);
		overloadedRoleTwo.setEntityAttribute(true);
		overloadedRoleTwo.setIdmPropertyName("one");
		overloadedRoleTwo.setName("defOneOverloadedRoleTwo");
		overloadedRoleTwo.setDisabledDefaultAttribute(false);
		overloadedRoleTwo.setRoleSystem(roleSystemTwo);
		overloadingAttributes.add(overloadedRoleTwo);

	}

	private void initData() {
		IdmIdentity identity;
		AccAccount accountOne;
		AccIdentityAccountDto accountIdentityOne;
		SysSystem system;

		// create test system
		system = defaultSysAccountManagementServiceTest.createTestSystem("test_resource");

		// set default generate password policy for system
		IdmPasswordPolicy passwordPolicy = new IdmPasswordPolicy();
		passwordPolicy.setName(PASSWORD_POLICY);
		passwordPolicy.setType(IdmPasswordPolicyType.GENERATE);
		passwordPolicy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		passwordPolicy.setLowerCharBase("a");
		passwordPolicy.setMinPasswordLength(2);
		passwordPolicy.setMaxPasswordLength(2);
		passwordPolicy.setMinLowerChar(2);
		passwordPolicyService.save(passwordPolicy);
		system.setPasswordPolicyGenerate(passwordPolicy);
		sysSystemService.save(system);

		// generate schema for system
		List<SysSchemaObjectClass> objectClasses = sysSystemService.generateSchema(system);

		// Create test identity for provisioning test
		identity = new IdmIdentity();
		identity.setUsername(IDENTITY_USERNAME);
		identity.setFirstName(IDENTITY_USERNAME);
		identity.setLastName(IDENTITY_USERNAME);
		identity = idmIdentityService.save(identity);

		accountOne = new AccAccount();
		accountOne.setSystem(system);
		accountOne.setUid("x" + IDENTITY_USERNAME);
		accountOne.setAccountType(AccountType.PERSONAL);
		accountOne = accountService.save(accountOne);

		accountIdentityOne = new AccIdentityAccountDto();
		accountIdentityOne.setIdentity(identity.getId());
		accountIdentityOne.setOwnership(true);
		accountIdentityOne.setAccount(accountOne.getId());

		accountIdentityOne = identityAccoutnService.save(accountIdentityOne);

		SysSystemMapping systemMapping = new SysSystemMapping();
		systemMapping.setName("default_" + System.currentTimeMillis());
		systemMapping.setEntityType(SystemEntityType.IDENTITY);
		systemMapping.setOperationType(SystemOperationType.PROVISIONING);
		systemMapping.setObjectClass(objectClasses.get(0));
		final SysSystemMapping entityHandlingResult = systemEntityHandlingService.save(systemMapping);

		SchemaAttributeFilter schemaAttributeFilter = new SchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());

		Page<SysSchemaAttribute> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		schemaAttributesPage.forEach(schemaAttr -> {
			if ("__NAME__".equals(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeHandlingName = new SysSystemAttributeMapping();
				attributeHandlingName.setUid(true);
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setIdmPropertyName("username");
				attributeHandlingName.setTransformToResourceScript("if(attributeValue){return \"x\"+ attributeValue;}");
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setSystemMapping(entityHandlingResult);
				schemaAttributeHandlingService.save(attributeHandlingName);

			} else if ("firstname".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeHandlingName = new SysSystemAttributeMapping();
				attributeHandlingName.setIdmPropertyName("firstName");
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName
						.setTransformFromResourceScript("if(attributeValue){return attributeValue.substring(1);}");
				attributeHandlingName.setSystemMapping(entityHandlingResult);
				schemaAttributeHandlingService.save(attributeHandlingName);

			} else if ("lastname".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeHandlingName = new SysSystemAttributeMapping();
				attributeHandlingName.setIdmPropertyName("lastName");
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setSystemMapping(entityHandlingResult);
				schemaAttributeHandlingService.save(attributeHandlingName);

			} else if (IcConnectorFacade.PASSWORD_ATTRIBUTE_NAME.equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeHandlingName = new SysSystemAttributeMapping();
				attributeHandlingName.setIdmPropertyName("password");
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSystemMapping(entityHandlingResult);
				schemaAttributeHandlingService.save(attributeHandlingName);

			} else if ("email".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeHandlingName = new SysSystemAttributeMapping();
				attributeHandlingName.setIdmPropertyName("email");
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setStrategyType(AttributeMappingStrategyType.CREATE);
				attributeHandlingName.setSystemMapping(entityHandlingResult);
				attributeHandlingName.setTransformToResourceScript("return \"" + EMAIL_ONE + "\";");
				schemaAttributeHandlingService.save(attributeHandlingName);

			}
		});
	}

}
