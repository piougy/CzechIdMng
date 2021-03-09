package eu.bcvsolutions.idm.acc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterEchoItemDto;
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccUniformPasswordService;
import eu.bcvsolutions.idm.acc.service.api.AccUniformPasswordSystemService;
import eu.bcvsolutions.idm.acc.service.api.PasswordFilterManager;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordHistoryDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordHistoryFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordHistoryService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.api.utils.PasswordGenerator;
import eu.bcvsolutions.idm.core.script.evaluator.AbstractScriptEvaluator;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcPasswordAttribute;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Abstract class for integration tests that work with password filter and uniform password system.
 * 
 * FIXME: use @AbstractRestTest (~ Spring MockMvc), delete prepareUrl method etc.
 *
 * @author Ondrej Kopr
 *
 */
public abstract class AbstractPasswordFilterIntegrationTest extends AbstractIntegrationTest {

	public static final String CHANGE_REST_ENDPOINT = "change";
	public static final String VALIDATE_REST_ENDPOINT = "validate";

	@Autowired
	protected TestHelper testHelper;
	@Autowired
	protected SysSystemService systemService;
	@Autowired
	protected SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired
	protected WebApplicationContext wac;
	@Autowired
	protected AccAccountService accountService;
	@Autowired
	protected AccUniformPasswordService uniformPasswordService;
	@Autowired
	protected AccUniformPasswordSystemService uniformPasswordSystemService;
	@Autowired
	protected SysProvisioningArchiveService provisioningArchive;
	@Autowired
	protected SysProvisioningOperationService provisioningOperation;
	@Autowired
	protected PasswordFilterManager passwordFilterManager;
	@Autowired
	protected IdmPasswordHistoryService passwordHistoryService;
	@Autowired
	protected LoginService loginService;
	@Autowired
	protected IdmIdentityService identityService;
	@Autowired
	protected IdmPasswordPolicyService passwordPolicyService;
	@Autowired
	protected IdmPasswordService passwordService;

	protected LoginDto currentLogin;

	@Before
	public void before() {
		// Clear password policies
		for (IdmPasswordPolicyDto passwordPolicyDto : passwordPolicyService.find(null)) {
			passwordPolicyService.delete(passwordPolicyDto);
		}
		currentLogin = this.getHelper().loginAdmin();
	}

	@After
	public void after() {
		this.getHelper().logout();
	}

	protected TestHelper getHelper() {
		return testHelper;
	}

	protected void checkPassword(String uid, String password, boolean same) {
		if (same) {
			assertEquals(password, getPasswordOnTargetSystem(uid));
		} else {
			assertNotEquals(password, getPasswordOnTargetSystem(uid));
		}
	}
	
	protected String getPasswordOnTargetSystem(String uid) {
		TestResource resource = getHelper().findResource(uid);
		assertNotNull(resource);
		return resource.getPassword();
	}

	protected PasswordRequest prepareRequest(String username, String resourceCode, String password) {
		PasswordRequest request = new PasswordRequest();
		request.setUsername(username);
		request.setPassword(password);
		request.setResource(resourceCode);
		return request;
	}

	protected String prepareUid(IdmIdentityDto identity, SysSystemDto system) {
		return identity.getUsername() + "_" + system.getDescription();
	}

	protected IdmIdentityDto createIdentity(SysSystemDto system) {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		identity.setLastName(getHelper().createName());
		identity.setFirstName(getHelper().createName());
		identity = identityService.save(identity);
		assignSystem(identity, system);
		return identity;
	}

	protected void assignSystem(IdmIdentityDto identity, SysSystemDto system) {
		if (system == null) {
			return;
		}

		IdmRoleDto role = getHelper().createRole();
		getHelper().createRoleSystem(role, system);
		getHelper().assignRoles(getHelper().getPrimeContract(identity), role);
	}

	protected void checkEcho(IdmIdentityDto identity, SysSystemDto system, EchoCheck echoCheck) {
		AccAccountDto account = getAccount(identity, system);
		if (account == null && echoCheck == EchoCheck.DOESNT_EXIST) {
			return;
		}

		AccPasswordFilterEchoItemDto echo = (AccPasswordFilterEchoItemDto) account.getEmbedded().get(AccAccountDto.PROPERTY_ECHO);
		if (echoCheck == EchoCheck.DOESNT_EXIST) {
			assertNull(echo);
			return;
		}

		assertNotNull(echo);
		if (echoCheck == EchoCheck.VALIDATE) {
			assertTrue(echo.isValidityChecked());
			assertNotNull(echo.getValidateDate());
		} else if (echoCheck == EchoCheck.CHANGE) {
			assertTrue(echo.isChanged());
			assertNotNull(echo.getChangeDate());
		} else if (echoCheck == EchoCheck.VALIDATE_AND_CHANGE) {
			assertTrue(echo.isValidityChecked());

			assertTrue(echo.isChanged());
			assertNotNull(echo.getChangeDate());

		} else if (echoCheck == EchoCheck.VALIDATE_FAILED) {
			assertFalse(echo.isValidityChecked());
			assertNotNull(echo.getValidateDate());
		} else if (echoCheck == EchoCheck.CREATE) {
			assertTrue(echo.isValidityChecked());
			assertNull(echo.getValidateDate());

			assertTrue(echo.isChanged());
			assertNotNull(echo.getChangeDate());
		} else {
			// What you want? Hm?!
			fail();
		}

	}
	
	protected IdmResponse processChange(PasswordRequest request, boolean success) {
		String url = prepareUrl(CHANGE_REST_ENDPOINT);
		return process(request, url, success);
	}

	protected IdmResponse processValidate(PasswordRequest request, boolean success) {
		String url = prepareUrl(VALIDATE_REST_ENDPOINT);
		return process(request, url, success);
	}

	// FIXME: use Spring MVC and remove this method at all
	// FIXME: admin authentication is used internally => security cannot be tested
	protected IdmResponse process(PasswordRequest request, String url, boolean success) {
		RestTemplate template = new RestTemplate();
		HttpEntity<PasswordRequest> requestUpdate = new HttpEntity<>(request, prepareHeaderBasicAuth());
		
		ResponseEntity<Object> result = null;
		IdmResponse response = new IdmResponse();
		try {
			result = template.exchange(url, HttpMethod.PUT, requestUpdate, Object.class);
			if (success) {
				assertEquals(HttpStatus.OK, result.getStatusCode());
				assertNull(result.getBody());
			} else {
				assertNotEquals(HttpStatus.OK, result.getStatusCode());
				assertNotNull(result.getBody());
			}
			
			response.message = String.valueOf(result.getBody());
			response.status = result.getStatusCode();
		} catch (HttpClientErrorException e) {
			response.message = e.getResponseBodyAsString();
			response.status = e.getStatusCode();
		}
		return response;
	}

	protected String prepareUrl(String endpoint) {
		String port = wac.getEnvironment().getProperty("local.server.port");
		return "http://localhost:" + port + "/api/v1/systems/password-filter/" + endpoint;
	}

	protected HttpHeaders prepareHeaderBasicAuth() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBasicAuth(currentLogin.getUsername(), currentLogin.getPassword().asString());
		return headers;
	}

	protected SysSystemDto createSystem(boolean createPasswordFilter) {
		return createSystem(createPasswordFilter, true);
	}

	protected SysSystemDto createSystem(boolean createPasswordFilter, boolean uidTransformation) {
		SysSystemDto system = this.getHelper().createTestResourceSystem(true);

		if (uidTransformation) {
			PasswordGenerator g = new PasswordGenerator();
			String uidSuffix = g.generateRandom(5, 5, 3, 2, 0, 0);
			
			SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
			filter.setSystemId(system.getId());
			filter.setName(TestHelper.ATTRIBUTE_MAPPING_NAME);
			List<SysSystemAttributeMappingDto> attributes = systemAttributeMappingService.find(filter, null).getContent();
			assertEquals(1, attributes.size());
			SysSystemAttributeMappingDto uid = attributes.get(0);
			uid.setTransformToResourceScript("return entity.getUsername() + '_" + uidSuffix + "';");
			uid = systemAttributeMappingService.save(uid);
			
			system.setDescription(uidSuffix);
			system = systemService.save(system);
		}
		
		setPasswordFilter(system, createPasswordFilter);
		return system;
	}
	
	protected SysSystemAttributeMappingDto setPasswordFilter(SysSystemDto system, boolean passwordFilter) {
		SysSystemAttributeMappingDto passwordAttribute = getPasswordAttribute(system);
		passwordAttribute.setPasswordFilter(passwordFilter);
		return systemAttributeMappingService.save(passwordAttribute);
	}

	protected SysSystemAttributeMappingDto getPasswordAttribute(SysSystemDto system) {
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(system.getId());
		filter.setName(TestHelper.ATTRIBUTE_MAPPING_PASSWORD);
		List<SysSystemAttributeMappingDto> attributes = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributes.size());
		return attributes.get(0);
	}

	protected AccAccountDto getAccount(IdmIdentityDto identity, SysSystemDto system) {
		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setIdentityId(identity.getId());
		accountFilter.setSystemId(system.getId());
		accountFilter.setIncludeEcho(Boolean.TRUE);
		List<AccAccountDto> accounts = accountService.find(accountFilter , null).getContent();
		if (CollectionUtils.isEmpty(accounts)) {
			return null;
		}
		assertEquals(1, accounts.size());
		return accounts.get(0);
	}

	protected AccUniformPasswordDto createUniformDefinition(boolean changeInIdm) {
		AccUniformPasswordDto dto = new AccUniformPasswordDto();
		dto.setCode(getHelper().createName());
		dto.setChangeInIdm(changeInIdm);
		return uniformPasswordService.save(dto);
	}

	protected void assignSystem(AccUniformPasswordDto uniform, SysSystemDto... systems) {
		for (SysSystemDto system : systems) {
			AccUniformPasswordSystemDto uniformSystem = new AccUniformPasswordSystemDto();
			uniformSystem.setSystem(system.getId());
			uniformSystem.setUniformPassword(uniform.getId());
			uniformPasswordSystemService.save(uniformSystem);
		}
	}

	protected void checkProcessedPasswordOperation(IdmIdentityDto identity, SysSystemDto system, int expectedOperation, String password) {
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(identity.getId());
		filter.setSystemId(system.getId());
		List<SysProvisioningArchiveDto> archives = provisioningArchive.find(filter, null).getContent();

		assertEquals(expectedOperation, archives.size());
		for (SysProvisioningArchiveDto archive : archives) {
			ProvisioningContext provisioningContext = archive.getProvisioningContext();
			assertNotNull(provisioningContext);
			IcConnectorObject connectorObject = provisioningContext.getConnectorObject();
			List<IcAttribute> attributes = connectorObject.getAttributes();

			boolean passwordExists = false;
			for (IcAttribute attribute : attributes) {
				if (attribute instanceof IcPasswordAttribute) {
					IcPasswordAttribute passAttr = (IcPasswordAttribute) attribute;
					GuardedString passwordValue = passAttr.getPasswordValue();
					assertNotNull(passwordValue);
					String asString = passwordValue.asString();
					assertEquals(GuardedString.SECRED_PROXY_STRING, asString);
					String passwordOnTargetSystem = getPasswordOnTargetSystem(prepareUid(identity, system));
					if (StringUtils.isNotBlank(password)) {
						assertEquals(password, passwordOnTargetSystem);
					}
					// Only one password for whole operation
					assertFalse(passwordExists);
					passwordExists = true;
				}
			}
			assertTrue(passwordExists);
		}
	}
	
	protected void checkActivePasswordOperation(IdmIdentityDto identity, SysSystemDto system, int expectedOperation, String password) {
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(identity.getId());
		filter.setSystemId(system.getId());
		List<SysProvisioningOperationDto> operations = provisioningOperation.find(filter, null).getContent();

		assertEquals(expectedOperation, operations.size());
		for (SysProvisioningOperationDto operation : operations) {
			IcConnectorObject fullConnectorObject = provisioningOperation.getFullConnectorObject(operation);
			List<IcAttribute> attributes = fullConnectorObject.getAttributes();
			boolean passwordExists = false;
			for (IcAttribute attribute : attributes) {
				if (attribute instanceof IcPasswordAttribute) {
					IcPasswordAttribute passAttr = (IcPasswordAttribute) attribute;
					GuardedString passwordValue = passAttr.getPasswordValue();
					assertNotNull(passwordValue);
					if (StringUtils.isNotBlank(password)) {
						assertEquals(password, passwordValue.asString());
					}
					// Only one password for whole operation
					assertFalse(passwordExists);
					passwordExists = true;
				}
			}
			assertTrue(passwordExists);
		}
	}

	protected void checkEmptyProvisioning(IdmIdentityDto identity, SysSystemDto ...systems) {
		for (SysSystemDto system : systems) {
			checkProcessedPasswordOperation(identity, system, 0, null);
			checkActivePasswordOperation(identity, system, 0, null);
		}
	}

	protected void cleanProvivisioning(IdmIdentityDto identity, SysSystemDto ...systems) {
		for (SysSystemDto system : systems) {
			SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
			filter.setEntityIdentifier(identity.getId());
			filter.setSystemId(system.getId());
			List<SysProvisioningOperationDto> operations = provisioningOperation.find(filter, null).getContent();
			
			operations.forEach(operation -> {
				provisioningOperation.delete(operation);
			});
			
			SysProvisioningOperationFilter filterArchive = new SysProvisioningOperationFilter();
			filterArchive.setEntityIdentifier(identity.getId());
			filterArchive.setSystemId(system.getId());
			List<SysProvisioningArchiveDto> archives = provisioningArchive.find(filterArchive, null).getContent();
			
			archives.forEach(archive -> {
				provisioningArchive.delete(archive);
			});
		}
	}

	protected void checkChangeInIdm(IdmIdentityDto identity, int count) {
		IdmPasswordHistoryFilter filter = new IdmPasswordHistoryFilter();
		filter.setIdentityId(identity.getId());
		List<IdmPasswordHistoryDto> histories = passwordHistoryService.find(filter, null).getContent();
		assertEquals(count, histories.size());
	}

	// FIXME: logout is not called!
	protected LoginDto loginToIdm(IdmIdentityDto identity, String password, boolean success) {
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(new GuardedString(password));

		LoginDto login = null;
		try {
			login = loginService.login(loginDto);
		} catch (IdmAuthenticationException e) {
			if (success) {
				throw e;
			}
		}
		if (success) {
			assertNotNull(login);
		} else {
			assertNull(login);
		}
		return login;
	}

	// FIXME: enum just for "switch" in test method ... remove + concrete method without mega if
	protected enum EchoCheck {
		DOESNT_EXIST,
		VALIDATE,
		VALIDATE_FAILED,
		CHANGE,
		VALIDATE_AND_CHANGE,
		CREATE
	}

	protected IdmPasswordPolicyDto createPasswordPolicy(SysSystemDto system, Integer minLength, Integer minSpecialChar, Integer minLowerChar, Integer minUpperChar, boolean defaultPolicy) {
		IdmPasswordPolicyDto passwordPolicy = new IdmPasswordPolicyDto();
		passwordPolicy.setName(getHelper().createName());
		passwordPolicy.setType(IdmPasswordPolicyType.VALIDATE);
		passwordPolicy.setMinPasswordLength(minLength);
		passwordPolicy.setMinSpecialChar(minSpecialChar);
		passwordPolicy.setMinLowerChar(minLowerChar);
		passwordPolicy.setMinUpperChar(minUpperChar);
		passwordPolicy.setDefaultPolicy(defaultPolicy);
		passwordPolicy = passwordPolicyService.save(passwordPolicy);
		
		if (system != null) {
			system.setPasswordPolicyValidate(passwordPolicy.getId());
			systemService.save(system);
		}
		
		return passwordPolicy;
	}
	
	protected String prepareScriptExecutor(String code) {
		StringBuilder example = new StringBuilder();
		example.append(AbstractScriptEvaluator.SCRIPT_EVALUATOR);
		example.append(".evaluate(\n");
		example.append("    ");
		example.append(AbstractScriptEvaluator.SCRIPT_EVALUATOR);
		example.append(".newBuilder()\n");

		example.append("        .setScriptCode('");
		example.append(code);
		example.append("')\n");

		example.append("        .addParameter('");
		example.append(PasswordFilterManager.SCRIPT_LOG_IDENTIFIER_PARAMETER);
		example.append("', ");
		example.append(PasswordFilterManager.SCRIPT_LOG_IDENTIFIER_PARAMETER);
		example.append(")\n");

		example.append("        .addParameter('");
		example.append(PasswordFilterManager.SCRIPT_SYSTEM_ATTRIBUTE_MAPPING_PARAMETER);
		example.append("', ");
		example.append(PasswordFilterManager.SCRIPT_SYSTEM_ATTRIBUTE_MAPPING_PARAMETER);
		example.append(")\n");

		example.append("        .addParameter('");
		example.append(PasswordFilterManager.SCRIPT_SYSTEM_PARAMETER);
		example.append("', ");
		example.append(PasswordFilterManager.SCRIPT_SYSTEM_PARAMETER);
		example.append(")\n");

		example.append("        .addParameter('");
		example.append(PasswordFilterManager.SCRIPT_USERNAME_PARAMETER);
		example.append("', ");
		example.append(PasswordFilterManager.SCRIPT_USERNAME_PARAMETER);
		example.append(")\n");

		example.append("	.build());\n");
		return example.toString();
	}

	// FIXME: remove this - use Spring MVC
	protected class IdmResponse {
		public HttpStatus status;
		public String message;
	}
	
	// FIXME: remove this, use AccPasswordFilterRequestDto + standard mapper
	protected class PasswordRequest implements Serializable {

		private static final long serialVersionUID = 1L;

		protected String password;
		protected String username;
		protected String resource;
		protected String logIdentifier;
		protected String version;
		
		public PasswordRequest() {
		}

		public PasswordRequest(String password, String username, String resource) {
			super();
			this.password = password;
			this.username = username;
			this.resource = resource;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getResource() {
			return resource;
		}

		public void setResource(String resource) {
			this.resource = resource;
		}

		public String getLogIdentifier() {
			return logIdentifier;
		}

		public void setLogIdentifier(String logIdentifier) {
			this.logIdentifier = logIdentifier;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

	}
}
