package eu.bcvsolutions.idm.acc.provisioning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.ProvisioningAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.entity.TestResource_;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.service.impl.IdentityProvisioningExecutor;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.security.api.domain.ConfidentialString;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for password transformation, passwords provisoning and etc.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class IdentityPasswordProvisioningTest extends AbstractIntegrationTest {

	public static final String DEFAULT_PASSWORD = "aa";

	@Autowired
	private TestHelper helper;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private AccIdentityAccountService identityAccountService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired
	private SysRoleSystemService roleSystemService;
	@Autowired
	private IdmPasswordPolicyService passwordPolicyService;
	@Autowired
	private SysProvisioningOperationService provisioningOperationService;
	@Autowired
	private IdentityProvisioningExecutor identityProvisioningExecutor;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private ConfidentialStorage confidentialStorage;
	@Autowired
	private ProvisioningExecutor provisioningExecutor;
	@Autowired
	private SysProvisioningArchiveService provisioningArchiveService;

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testIdentityCreate() {
		SysSystemDto system = initSystem();
		IdmRoleDto role = initRole(system);

		String identityUsername = "test-" + System.currentTimeMillis();
		IdmIdentityDto identity = helper.createIdentity(identityUsername);

		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, role);
		checkIdentityAccount(identity, identityRole, 1);

		TestResource findResource = helper.findResource(identityUsername);
		assertNotNull(findResource);
		assertEquals(DEFAULT_PASSWORD, findResource.getPassword());
	}

	@Test
	public void testIdentityPasswordChange() {
		SysSystemDto system = initSystem();
		IdmRoleDto role = initRole(system);

		String identityUsername = "test-" + System.currentTimeMillis();
		IdmIdentityDto identity = helper.createIdentity(identityUsername);

		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, role);
		checkIdentityAccount(identity, identityRole, 1);

		String newPassword = "newPass" + System.currentTimeMillis();
		changePassword(identity, null, newPassword);

		TestResource findResource = helper.findResource(identityUsername);
		assertNotNull(findResource);
		assertEquals(newPassword, findResource.getPassword());
	}

	@Test
	public void testIdentityPasswordChangeWithAnotherAtt() {
		SysSystemDto system = initSystem();
		IdmRoleDto role = initRole(system);

		SysSystemAttributeMappingDto descriptionAttribute = initDescriptionAttribute(system);
		descriptionAttribute = changeAttributeToPasswordMapping(descriptionAttribute, null);

		String identityUsername = "test-" + System.currentTimeMillis();
		IdmIdentityDto identity = helper.createIdentity(identityUsername);

		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, role);
		checkIdentityAccount(identity, identityRole, 1);

		String newPassword = "newPass" + System.currentTimeMillis();
		changePassword(identity, null, newPassword);

		TestResource findResource = helper.findResource(identityUsername);
		assertNotNull(findResource);
		assertEquals(newPassword, findResource.getPassword());
		assertEquals(newPassword, findResource.getDescrip());
	}

	@Test
	public void testIdentityCreateWithAnotherAtt() {
		SysSystemDto system = initSystem();
		IdmRoleDto role = initRole(system);

		SysSystemAttributeMappingDto descriptionAttribute = initDescriptionAttribute(system);
		descriptionAttribute = changeAttributeToPasswordMapping(descriptionAttribute, null);

		String identityUsername = "test-" + System.currentTimeMillis();
		IdmIdentityDto identity = helper.createIdentity(identityUsername);

		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, role);
		checkIdentityAccount(identity, identityRole, 1);

		TestResource findResource = helper.findResource(identityUsername);
		assertNotNull(findResource);
		assertEquals(DEFAULT_PASSWORD, findResource.getPassword());
		assertEquals(DEFAULT_PASSWORD, findResource.getDescrip());
	}

	@Test
	public void testCheckMapping() {
		String postfixForPassword = "-" + System.currentTimeMillis();
		SysSystemDto system = initSystem();
		IdmRoleDto role = initRole(system);

		SysSystemAttributeMappingDto descriptionAttribute = initDescriptionAttribute(system);
		descriptionAttribute = changeAttributeToPasswordMapping(descriptionAttribute,
				"import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;" + System.lineSeparator() + ""
						+ System.lineSeparator() + "String newPassword = attributeValue.asString();"
						+ System.lineSeparator() + "newPassword = newPassword + " + postfixForPassword + ";"
						+ System.lineSeparator() + "" + System.lineSeparator()
						+ "return new GuardedString(newPassword);" + System.lineSeparator());

		String identityUsername = "test-" + System.currentTimeMillis();
		IdmIdentityDto identity = helper.createIdentity(identityUsername);

		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, role);
		checkIdentityAccount(identity, identityRole, 1);

		TestResource findResource = helper.findResource(identityUsername);
		assertNotNull(findResource);
		assertEquals(DEFAULT_PASSWORD, findResource.getPassword());
		assertEquals(DEFAULT_PASSWORD + postfixForPassword, findResource.getDescrip());
	}

	@Test
	public void testCheckMappingReturnNull() {
		SysSystemDto system = initSystem();
		IdmRoleDto role = initRole(system);

		SysSystemAttributeMappingDto descriptionAttribute = initDescriptionAttribute(system);
		descriptionAttribute = changeAttributeToPasswordMapping(descriptionAttribute,
				"return null;" + System.lineSeparator());

		String identityUsername = "test-" + System.currentTimeMillis();
		IdmIdentityDto identity = helper.createIdentity(identityUsername);

		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, role);
		checkIdentityAccount(identity, identityRole, 1);

		TestResource findResource = helper.findResource(identityUsername);
		assertNotNull(findResource);
		assertEquals(DEFAULT_PASSWORD, findResource.getPassword());
		assertNull(findResource.getDescrip());
	}

	@Test(expected = ResultCodeException.class)
	public void testCheckMappingReturnStringPasswordChange() {
		String postfixForPassword = "-" + System.currentTimeMillis();
		SysSystemDto system = initSystem();
		IdmRoleDto role = initRole(system);

		SysSystemAttributeMappingDto descriptionAttribute = initDescriptionAttribute(system);
		descriptionAttribute = changeAttributeToPasswordMapping(descriptionAttribute, null);

		String identityUsername = "test-" + System.currentTimeMillis();
		IdmIdentityDto identity = helper.createIdentity(identityUsername);

		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, role);
		checkIdentityAccount(identity, identityRole, 1);

		TestResource findResource = helper.findResource(identityUsername);
		assertNotNull(findResource);
		assertEquals(DEFAULT_PASSWORD, findResource.getPassword());
		assertEquals(DEFAULT_PASSWORD, findResource.getDescrip());
		
		descriptionAttribute = changeAttributeToPasswordMapping(descriptionAttribute,
				"import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;" + System.lineSeparator() + ""
						+ System.lineSeparator() + "String newPassword = attributeValue.asString();"
						+ System.lineSeparator() + "newPassword = newPassword + " + postfixForPassword + ";"
						+ System.lineSeparator() + "" + System.lineSeparator() + "return newPassword;"
						+ System.lineSeparator());
		
		findResource = helper.findResource(identityUsername);
		assertNotNull(findResource);

		String newPassword = "newPass" + System.currentTimeMillis();
		// this throw exception
		changePassword(identity, null, newPassword);
		fail();
	}

	@Test
	public void testCheckMappingReturnStringCreate() {
		String postfixForPassword = "-" + System.currentTimeMillis();
		SysSystemDto system = initSystem();
		IdmRoleDto role = initRole(system);

		SysSystemAttributeMappingDto descriptionAttribute = initDescriptionAttribute(system);
		descriptionAttribute = changeAttributeToPasswordMapping(descriptionAttribute,
				"" + "import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;" + System.lineSeparator() + ""
						+ System.lineSeparator() + "String newPassword = attributeValue.asString();"
						+ System.lineSeparator() + "newPassword = newPassword + " + postfixForPassword + ";"
						+ System.lineSeparator() + "" + System.lineSeparator() + "return newPassword;"
						+ System.lineSeparator());

		String identityUsername = "test-" + System.currentTimeMillis();
		IdmIdentityDto identity = helper.createIdentity(identityUsername);

		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, role);
		checkIdentityAccount(identity, identityRole, 1);

		TestResource findResource = helper.findResource(identityUsername);
		assertNull(findResource);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityType(SystemEntityType.IDENTITY);
		filter.setEntityIdentifier(identity.getId());
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(filter, null).getContent();
		assertEquals(1, operations.size());
		SysProvisioningOperationDto operationDto = operations.get(0);
		assertEquals(OperationState.EXCEPTION, operationDto.getResultState());
		OperationResult result = operationDto.getResult();
		assertEquals(AccResultCode.PROVISIONING_PASSWORD_TRANSFORMATION_FAILED.getCode(), result.getCode());
	}

	@Test
	public void testCreateProvisioningOperation() {
		String testSchemaAttributeName = "test-Att";
		ProvisioningAttributeDto attribute = new ProvisioningAttributeDto(testSchemaAttributeName, AttributeMappingStrategyType.SET);
		assertFalse(attribute.isPasswordAttribute());

		attribute = new ProvisioningAttributeDto(ProvisioningService.PASSWORD_SCHEMA_PROPERTY_NAME, AttributeMappingStrategyType.SET);
		assertTrue(attribute.isPasswordAttribute());

		SysSystemAttributeMappingDto dto = new SysSystemAttributeMappingDto();
		dto.setPasswordAttribute(false);
		attribute = ProvisioningAttributeDto.createProvisioningAttributeKey(dto, testSchemaAttributeName, String.class.getName());
		assertFalse(attribute.isPasswordAttribute());

		attribute = ProvisioningAttributeDto.createProvisioningAttributeKey(dto, ProvisioningService.PASSWORD_SCHEMA_PROPERTY_NAME, String.class.getName());
		assertTrue(attribute.isPasswordAttribute());
	}

	@Test
	public void testPasswordChangeGreenLine() {
		String newPassword = "newPassword" + System.currentTimeMillis();
		String newPassword2 = "newPassword2" + System.currentTimeMillis();

		SysSystemDto system = initSystem();
		IdmRoleDto role = initRole(system);

		IdmIdentityDto identity = helper.createIdentity();

		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, role);
		checkIdentityAccount(identity, identityRole, 1);

		AccAccountDto account = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNotNull(account);
		Assert.assertFalse(account.isInProtection());

		TestResource entityOnSystem = helper.findResource(account.getUid());
		assertNotNull(entityOnSystem);
		assertEquals(DEFAULT_PASSWORD, entityOnSystem.getPassword());

		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setAll(true);
		passwordChange.setOldPassword(new GuardedString(DEFAULT_PASSWORD));
		passwordChange.setNewPassword(new GuardedString(newPassword));
		List<OperationResult> results = identityProvisioningExecutor.changePassword(identity, passwordChange);
		assertEquals(1, results.size());

		entityOnSystem = helper.findResource(account.getUid());
		assertNotNull(entityOnSystem);
		assertEquals(newPassword, entityOnSystem.getPassword());

		passwordChange = new PasswordChangeDto();
		passwordChange.setAccounts(Lists.newArrayList(account.getId().toString()));
		passwordChange.setOldPassword(new GuardedString(DEFAULT_PASSWORD));
		passwordChange.setNewPassword(new GuardedString(newPassword2));
		results = identityProvisioningExecutor.changePassword(identity, passwordChange);
		assertEquals(1, results.size());

		entityOnSystem = helper.findResource(account.getUid());
		assertNotNull(entityOnSystem);
		assertEquals(newPassword2, entityOnSystem.getPassword());
	}

	@Test
	public void testPasswordChangeProtected() {
		String newPassword = "newPassword" + System.currentTimeMillis();
		String newPassword2 = "newPassword2" + System.currentTimeMillis();

		SysSystemDto system = initSystem();
		IdmRoleDto role = initRole(system);

		IdmIdentityDto identity = helper.createIdentity();

		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, role);
		checkIdentityAccount(identity, identityRole, 1);

		AccAccountDto account = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNotNull(account);
		Assert.assertFalse(account.isInProtection());
		account.setInProtection(true);
		accountService.save(account);

		TestResource entityOnSystem = helper.findResource(account.getUid());
		assertNotNull(entityOnSystem);
		assertEquals(DEFAULT_PASSWORD, entityOnSystem.getPassword());

		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setAll(true);
		passwordChange.setOldPassword(new GuardedString(DEFAULT_PASSWORD));
		passwordChange.setNewPassword(new GuardedString(newPassword));
		List<OperationResult> results = identityProvisioningExecutor.changePassword(identity, passwordChange);
		assertEquals(0, results.size());

		entityOnSystem = helper.findResource(account.getUid());
		assertNotNull(entityOnSystem);
		assertEquals(DEFAULT_PASSWORD, entityOnSystem.getPassword());

		passwordChange = new PasswordChangeDto();
		passwordChange.setAccounts(Lists.newArrayList(account.getId().toString()));
		passwordChange.setOldPassword(new GuardedString(DEFAULT_PASSWORD));
		passwordChange.setNewPassword(new GuardedString(newPassword2));
		results = identityProvisioningExecutor.changePassword(identity, passwordChange);
		assertEquals(0, results.size());

		entityOnSystem = helper.findResource(account.getUid());
		assertNotNull(entityOnSystem);
		assertEquals(DEFAULT_PASSWORD, entityOnSystem.getPassword());
	}

	@Test
	public void testReadOnlySystem() {
		String suffixForPassword = "-" + System.currentTimeMillis();
		SysSystemDto system = initSystem();
		system.setReadonly(true);
		system = systemService.save(system);

		SysSystemAttributeMappingDto descriptionAttribute = initDescriptionAttribute(system);
		descriptionAttribute = changeAttributeToPasswordMapping(descriptionAttribute,
				"" + "import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;" + System.lineSeparator() + ""
						+ System.lineSeparator() + "String newPassword = attributeValue.asString();"
						+ System.lineSeparator() + "return new GuardedString(newPassword + '" + suffixForPassword + "');"
						+ System.lineSeparator());

		IdmRoleDto role = initRole(system);

		IdmIdentityDto identity = helper.createIdentity();

		IdmIdentityRoleDto identityRole = helper.createIdentityRole(identity, role);
		checkIdentityAccount(identity, identityRole, 1);

		long deleteOperations = provisioningOperationService.deleteOperations(system.getId());
		assertEquals(1l, deleteOperations);

		// Execute provisioning
		identityService.save(identity);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(identity.getId());
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(filter, null).getContent();

		assertEquals(1, operations.size());
		SysProvisioningOperationDto operationDto = operations.get(0);

		assertEquals(ProvisioningEventType.CREATE, operationDto.getOperationType());
		Map<ProvisioningAttributeDto, Object> accountObject = operationDto.getProvisioningContext().getAccountObject();

		String confidentialAccountKeyPassword = null;
		String confidentialAccountKeyDescrip = null;
		String confidentialConnectorKeyPassword = null;
		String confidentialConnectorKeyDescrip = null;

		boolean descripAttributeExists = false;
		boolean passwordAttributeExists = false;
		for (Entry<ProvisioningAttributeDto, Object> entry : accountObject.entrySet()) {
			ProvisioningAttributeDto key = entry.getKey();
			if (key.getSchemaAttributeName().equalsIgnoreCase(TestResource_.descrip.getName())) {
				descripAttributeExists = true;
				Object value = entry.getValue();
				assertTrue(value instanceof ConfidentialString);
				ConfidentialString confidentialStorageValue = (ConfidentialString) value;
				confidentialAccountKeyDescrip = confidentialStorageValue.getKey();
				GuardedString guardedString = confidentialStorage.getGuardedString(operationDto, confidentialAccountKeyDescrip);
				assertNotNull(guardedString);
				String asString = guardedString.asString();
				assertEquals(DEFAULT_PASSWORD + suffixForPassword, asString);		}
			if (key.getSchemaAttributeName().equalsIgnoreCase(ProvisioningService.PASSWORD_SCHEMA_PROPERTY_NAME)) {
				passwordAttributeExists = true;
				Object value = entry.getValue();
				assertTrue(value instanceof ConfidentialString);
				ConfidentialString confidentialStorageValue = (ConfidentialString) value;
				confidentialAccountKeyPassword = confidentialStorageValue.getKey();
				GuardedString guardedString = confidentialStorage.getGuardedString(operationDto, confidentialAccountKeyPassword);
				assertNotNull(guardedString);
				String asString = guardedString.asString();
				assertEquals(DEFAULT_PASSWORD, asString);
			}
		}
		assertTrue(passwordAttributeExists);
		assertTrue(descripAttributeExists);

		descripAttributeExists = false;
		passwordAttributeExists = false;
		IcConnectorObject connectorObject = operationDto.getProvisioningContext().getConnectorObject();
		for (IcAttribute attribute : connectorObject.getAttributes()) {
			if (attribute.getName().equalsIgnoreCase(TestResource_.descrip.getName())) {
				descripAttributeExists = true;
				Object value = attribute.getValue();
				assertTrue(value instanceof ConfidentialString);
				ConfidentialString confidentialStorageValue = (ConfidentialString) value;
				confidentialConnectorKeyDescrip = confidentialStorageValue.getKey();
				GuardedString guardedString = confidentialStorage.getGuardedString(operationDto, confidentialConnectorKeyDescrip);
				assertNotNull(guardedString);
				String asString = guardedString.asString();
				assertEquals(DEFAULT_PASSWORD + suffixForPassword, asString);
			}
			if (attribute.getName().equalsIgnoreCase(ProvisioningService.PASSWORD_SCHEMA_PROPERTY_NAME)) {
				passwordAttributeExists = true;
				Object value = attribute.getValue();
				assertTrue(value instanceof ConfidentialString);
				ConfidentialString confidentialStorageValue = (ConfidentialString) value;
				confidentialConnectorKeyPassword = confidentialStorageValue.getKey();
				GuardedString guardedString = confidentialStorage.getGuardedString(operationDto, confidentialConnectorKeyPassword);
				assertNotNull(guardedString);
				String asString = guardedString.asString();
				assertEquals(DEFAULT_PASSWORD, asString);
			}
		}
		assertTrue(passwordAttributeExists);
		assertTrue(descripAttributeExists);

		assertNotNull(confidentialAccountKeyPassword);
		assertNotNull(confidentialAccountKeyDescrip);
		assertNotNull(confidentialConnectorKeyPassword);
		assertNotNull(confidentialConnectorKeyDescrip);

		system.setReadonly(false);
		system = systemService.save(system);

		operationDto = provisioningExecutor.executeSync(operationDto);
		assertEquals(OperationState.EXECUTED, operationDto.getResultState());

		Serializable serializable = confidentialStorage.get(operationDto, confidentialAccountKeyPassword);
		assertNull(serializable);
		serializable = confidentialStorage.get(operationDto, confidentialAccountKeyDescrip);
		assertNull(serializable);
		serializable = confidentialStorage.get(operationDto, confidentialConnectorKeyPassword);
		assertNull(serializable);
		serializable = confidentialStorage.get(operationDto, confidentialConnectorKeyDescrip);
		assertNull(serializable);

		SysProvisioningOperationFilter archiveFilter = new SysProvisioningOperationFilter();
		archiveFilter.setSystemId(system.getId());
		archiveFilter.setEntityIdentifier(identity.getId());
		List<SysProvisioningArchiveDto> archive = provisioningArchiveService.find(archiveFilter, null).getContent();
		assertEquals(1, archive.size());
		SysProvisioningArchiveDto provisioningArchiveDto = archive.get(0);
		
		descripAttributeExists = false;
		passwordAttributeExists = false;
		for (Entry<ProvisioningAttributeDto, Object> entry : provisioningArchiveDto.getProvisioningContext().getAccountObject().entrySet()) {
			ProvisioningAttributeDto key = entry.getKey();
			if (key.getSchemaAttributeName().equalsIgnoreCase(TestResource_.descrip.getName())) {
				descripAttributeExists = true;
				Object value = entry.getValue();
				assertTrue(value instanceof ConfidentialString);
				ConfidentialString confidentialStorageValue = (ConfidentialString) value;
				confidentialAccountKeyDescrip = confidentialStorageValue.getKey();
				GuardedString guardedString = confidentialStorage.getGuardedString(operationDto, confidentialAccountKeyDescrip);
				assertNotNull(guardedString);
				assertTrue(StringUtils.isEmpty(guardedString.asString()));
				guardedString = confidentialStorage.getGuardedString(provisioningArchiveDto, confidentialAccountKeyDescrip);
				assertNotNull(guardedString);
				assertTrue(StringUtils.isEmpty(guardedString.asString()));
			}
			if (key.getSchemaAttributeName().equalsIgnoreCase(ProvisioningService.PASSWORD_SCHEMA_PROPERTY_NAME)) {
				passwordAttributeExists = true;
				Object value = entry.getValue();
				assertTrue(value instanceof ConfidentialString);
				ConfidentialString confidentialStorageValue = (ConfidentialString) value;
				confidentialAccountKeyPassword = confidentialStorageValue.getKey();
				GuardedString guardedString = confidentialStorage.getGuardedString(operationDto, confidentialAccountKeyPassword);
				assertNotNull(guardedString);
				assertTrue(StringUtils.isEmpty(guardedString.asString()));
				guardedString = confidentialStorage.getGuardedString(provisioningArchiveDto, confidentialAccountKeyPassword);
				assertNotNull(guardedString);
				assertTrue(StringUtils.isEmpty(guardedString.asString()));
			}
		}
		assertTrue(passwordAttributeExists);
		assertTrue(descripAttributeExists);

		descripAttributeExists = false;
		passwordAttributeExists = false;
		connectorObject = provisioningArchiveDto.getProvisioningContext().getConnectorObject();
		for (IcAttribute attribute : connectorObject.getAttributes()) {
			if (attribute.getName().equalsIgnoreCase(TestResource_.descrip.getName())) {
				descripAttributeExists = true;
				Object value = attribute.getValue();
				assertTrue(value instanceof ConfidentialString);
				ConfidentialString confidentialStorageValue = (ConfidentialString) value;
				confidentialConnectorKeyDescrip = confidentialStorageValue.getKey();
				GuardedString guardedString = confidentialStorage.getGuardedString(operationDto, confidentialConnectorKeyDescrip);
				assertNotNull(guardedString);
				assertTrue(StringUtils.isEmpty(guardedString.asString()));
				guardedString = confidentialStorage.getGuardedString(provisioningArchiveDto, confidentialConnectorKeyDescrip);
				assertNotNull(guardedString);
				assertTrue(StringUtils.isEmpty(guardedString.asString()));
			}
			if (attribute.getName().equalsIgnoreCase(ProvisioningService.PASSWORD_SCHEMA_PROPERTY_NAME)) {
				passwordAttributeExists = true;
				Object value = attribute.getValue();
				assertTrue(value instanceof ConfidentialString);
				ConfidentialString confidentialStorageValue = (ConfidentialString) value;
				confidentialConnectorKeyPassword = confidentialStorageValue.getKey();
				GuardedString guardedString = confidentialStorage.getGuardedString(operationDto, confidentialConnectorKeyPassword);
				assertNotNull(guardedString);
				assertTrue(StringUtils.isEmpty(guardedString.asString()));
				guardedString = confidentialStorage.getGuardedString(provisioningArchiveDto, confidentialConnectorKeyPassword);
				assertNotNull(guardedString);
				assertTrue(StringUtils.isEmpty(guardedString.asString()));
			}
		}

		List<AccAccountDto> accounts = accountService.getAccounts(system.getId(), identity.getId());
		assertEquals(1, accounts.size());
		AccAccountDto accountDto = accounts.get(0);
		TestResource resource = this.helper.findResource(accountDto.getUid());
		assertNotNull(resource);
		assertEquals(DEFAULT_PASSWORD, resource.getPassword());
		assertEquals(DEFAULT_PASSWORD + suffixForPassword, resource.getDescrip());
	}

	/**
	 * Change attribute to password mapping
	 *
	 * @param attribute
	 * @param transformToResourceScript
	 * @return
	 */
	private SysSystemAttributeMappingDto changeAttributeToPasswordMapping(SysSystemAttributeMappingDto attribute,
			String transformToResourceScript) {
		attribute.setPasswordAttribute(true);
		attribute.setEntityAttribute(false);
		attribute.setExtendedAttribute(false);
		attribute.setIdmPropertyName(null);
		attribute.setTransformToResourceScript(transformToResourceScript);
		return systemAttributeMappingService.save(attribute);
	}

	/**
	 * Init description attribute. The attribute will be mapped to entity attribute
	 * description.
	 *
	 * @param system
	 * @return
	 */
	private SysSystemAttributeMappingDto initDescriptionAttribute(SysSystemDto system) {
		// update attribute description for password
		SysSystemMappingDto mapping = helper.getDefaultMapping(system);

		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());

		List<SysSchemaAttributeDto> schemaAttributes = schemaAttributeService.find(schemaAttributeFilter, null)
				.getContent();
		assertFalse(schemaAttributes.isEmpty());

		SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
		for (SysSchemaAttributeDto schemaAttr : schemaAttributes) {
			if (schemaAttr.getName().equals(TestHelper.ATTRIBUTE_MAPPING_DESCRIPTION)) {
				attributeMapping.setEntityAttribute(true);
				attributeMapping.setIdmPropertyName(IdmIdentity_.description.getName());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(mapping.getId());
				attributeMapping = systemAttributeMappingService.save(attributeMapping);
			}
		}
		return attributeMapping;
	}

	/**
	 * Change password for identity on all resources expect idm.
	 *
	 * @param identity
	 * @param oldPassword
	 * @param newPassword
	 */
	private void changePassword(IdmIdentityDto identity, String oldPassword, String newPassword) {
		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setAll(true);
		passwordChange.setIdm(true);
		if (oldPassword == null) {
			passwordChange.setOldPassword(new GuardedString(DEFAULT_PASSWORD));
		} else {
			passwordChange.setOldPassword(new GuardedString(oldPassword));
		}
		passwordChange.setNewPassword(new GuardedString(newPassword));
		identityService.passwordChange(identity, passwordChange);
	}

	/**
	 * Check identity accounts for identity and identity role
	 *
	 * @param identity
	 * @param identityRole
	 * @param count
	 */
	private void checkIdentityAccount(IdmIdentityDto identity, IdmIdentityRoleDto identityRole, int count) {
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		filter.setIdentityRoleId(identityRole.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(filter, null).getContent();
		Assert.assertEquals(count, identityAccounts.size());
	}

	/**
	 * Init test system with mapping and password policy.
	 *
	 * @return
	 */
	private SysSystemDto initSystem() {
		// create test system
		SysSystemDto system = helper.createTestResourceSystem(true);
		IdmPasswordPolicyDto passwordPolicy = initPasswordPolicy();
		system.setPasswordPolicyGenerate(passwordPolicy.getId());
		return systemService.save(system);
	}

	/**
	 * Init role for system
	 *
	 * @param system
	 * @return
	 */
	private IdmRoleDto initRole(SysSystemDto system) {
		// create role mapping to system
		SysSystemMappingDto systemMapping = helper.getDefaultMapping(system);
		IdmRoleDto role = helper.createRole();
		SysRoleSystemDto roleSystemDefault = new SysRoleSystemDto();
		roleSystemDefault.setRole(role.getId());
		roleSystemDefault.setSystem(system.getId());
		roleSystemDefault.setSystemMapping(systemMapping.getId());
		roleSystemService.save(roleSystemDefault);

		return role;
	}

	/**
	 * Init password policy with predefined password policy for generate with
	 * exactly two characters and both characters will be lower 'a'.
	 * 
	 * @return
	 */
	private IdmPasswordPolicyDto initPasswordPolicy() {
		IdmPasswordPolicyDto passwordPolicy = new IdmPasswordPolicyDto();
		passwordPolicy.setName(helper.createName());
		passwordPolicy.setType(IdmPasswordPolicyType.GENERATE);
		passwordPolicy.setMinPasswordLength(2);
		passwordPolicy.setMaxPasswordLength(2);
		passwordPolicy.setLowerCharBase(DEFAULT_PASSWORD);
		passwordPolicy.setMinLowerChar(2);
		return passwordPolicyService.save(passwordPolicy);
	}
}
