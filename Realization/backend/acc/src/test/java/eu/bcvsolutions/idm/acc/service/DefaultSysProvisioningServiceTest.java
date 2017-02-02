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

import eu.bcvsolutions.idm.acc.domain.AccountOperationType;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityFormValue;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.service.api.FormService;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;
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
		AccIdentityAccount accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);

		provisioningService.doProvisioning(accountIdentityOne.getIdentity());

		TestResource createdAccount = entityManager.find(TestResource.class, accountIdentityOne.getAccount().getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());
	}

	@Test
	public void doIdentityProvisioningChangeAccount() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());

		AccIdentityAccount accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);
		TestResource createdAccount = entityManager.find(TestResource.class, accountIdentityOne.getAccount().getUid());

		identity.setFirstName(IDENTITY_CHANGED_FIRST_NAME);
		identity = idmIdentityService.save(identity);
		Assert.assertNotEquals(identity.getFirstName(), createdAccount.getFirstname());

		provisioningService.doProvisioning(identity);
		TestResource changedAccount = entityManager.find(TestResource.class, accountIdentityOne.getAccount().getUid());
		Assert.assertNotNull(changedAccount);
		Assert.assertEquals(identity.getFirstName(), changedAccount.getFirstname());
	}
	
	
	@Test
	public void doIdentityProvisioningChangeSingleAttribute() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		Assert.assertEquals("Identity must have this first name!", IDENTITY_CHANGED_FIRST_NAME,
				identity.getFirstName());
		
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		AccIdentityAccount accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);
		SysSystem system = accountIdentityOne.getAccount().getSystem();
		
		SystemAttributeMappingFilter attributeFilter = new SystemAttributeMappingFilter();
		attributeFilter.setSystemId(system.getId());
		attributeFilter.setIdmPropertyName("firstName");
		
		TestResource resourceAccount = entityManager.find(TestResource.class, "x" + IDENTITY_USERNAME);
		Assert.assertNotNull("Idenitity have to exists on target system (after account management)", resourceAccount);
		Assert.assertEquals("Account on target system, must have same firsta name as Identity", IDENTITY_CHANGED_FIRST_NAME,
				resourceAccount.getFirstname());
		
		provisioningService.doProvisioningForAttribute("x" + IDENTITY_USERNAME, schemaAttributeHandlingService.find(attributeFilter, null).getContent().get(0), IDENTITY_USERNAME, system, AccountOperationType.UPDATE, SystemEntityType.IDENTITY, identity);

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
		AccIdentityAccount accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);
		SysSystem system = accountIdentityOne.getAccount().getSystem();

		// Check empty password
		provisioningService.authenticate(accountIdentityOne.getAccount().getUid(), new GuardedString(), system, SystemEntityType.IDENTITY);


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
		provisioningService.authenticate(accountIdentityOne.getAccount().getUid(), new GuardedString(IDENTITY_PASSWORD_ONE), system, SystemEntityType.IDENTITY);

		// Check incorrect password
		try {
			provisioningService.authenticate(accountIdentityOne.getAccount().getUid(), new GuardedString(IDENTITY_PASSWORD_TWO), system, SystemEntityType.IDENTITY);
			fail("Bad credentials exception is expected here!");
		} catch (ResultCodeException ex) {
			//
		}
		// Do change of password for selected accounts
		passwordChange.setNewPassword(new GuardedString(IDENTITY_PASSWORD_TWO));
		idmIdentityService.passwordChange(accountIdentityOne.getIdentity(), passwordChange);

		// Check correct password Two
		accountIdentityOne = identityAccoutnService.get(accountIdentityOne.getId());
		provisioningService.authenticate(accountIdentityOne.getAccount().getUid(), new GuardedString(IDENTITY_PASSWORD_TWO), system, SystemEntityType.IDENTITY);
	}
	
	@Test
	public void doIdentityProvisioningRemoveAccount() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		AccIdentityAccount accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);

		// Delete account
		accountService.delete(accountIdentityOne.getAccount());
		TestResource removedAccount = entityManager.find(TestResource.class, accountIdentityOne.getAccount().getUid());
		Assert.assertNull(removedAccount);
	}
	
	@Test
	public void doIdentityProvisioningExtendedAttribute() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
				
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		AccIdentityAccount accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);
		
		// We will use firstName attribute (password attribute is not returned by default)
		SystemAttributeMappingFilter filterSchemaAttr = new SystemAttributeMappingFilter();
		filterSchemaAttr.setIdmPropertyName("firstName");
		filterSchemaAttr.setSystemId(accountIdentityOne.getAccount().getSystem().getId());
		SysSystemAttributeMapping attributeHandling = schemaAttributeHandlingService.find(filterSchemaAttr, null).getContent().get(0);
		// Set attribute to extended attribute and modify idmPropety to extPassword
		attributeHandling.setIdmPropertyName(IDENTITY_EXT_PASSWORD);
		attributeHandling.setExtendedAttribute(true);
		attributeHandling.setConfidentialAttribute(true);
		attributeHandling.setEntityAttribute(false);
		attributeHandling.setTransformToResourceScript("return attributeValue");
		// Form attribute definition will be created during save attribute handling
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
		TestResource resourceAccoutn = entityManager.find(TestResource.class, accountIdentityOne.getAccount().getUid());
		Assert.assertEquals(IDENTITY_PASSWORD_THREE, resourceAccoutn.getFirstname());;
	}

	private void initData() {
		IdmIdentity identity;
		AccAccount accountOne;
		AccIdentityAccount accountIdentityOne;
		SysSystem system;

		// create test system
		system = defaultSysAccountManagementServiceTest.createTestSystem();

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

		accountIdentityOne = new AccIdentityAccount();
		accountIdentityOne.setIdentity(identity);
		accountIdentityOne.setOwnership(true);
		accountIdentityOne.setAccount(accountOne);

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
				attributeHandlingName.setEntityAttribute(false);
				attributeHandlingName.setTransformToResourceScript("return \""+"x" + IDENTITY_USERNAME+"\";");
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setSystemMapping(entityHandlingResult);
				schemaAttributeHandlingService.save(attributeHandlingName);

			} else if ("firstname".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeHandlingName = new SysSystemAttributeMapping();
				attributeHandlingName.setIdmPropertyName("firstName");
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setName(schemaAttr.getName());
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

			}
		});
	}

}
