package eu.bcvsolutions.idm.acc.security;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.config.domain.AuthenticatorConfiguration;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.security.authentication.impl.DefaultAccMultipleSystemAuthenticator;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.core.security.api.exception.MustChangePasswordException;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Tests for {@link DefaultAccMultipleSystemAuthenticator}
 *
 * @author Ondrej Kopr
 *
 */
public class DefaultAccMultipleSystemAuthenticatorIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private TestHelper helper;
	@Autowired
	private ProvisioningService provisioningService;
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private AuthenticatorConfiguration authenticatorConfiguration;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired 
	private IdmPasswordService passwordService;
	@Autowired 
	private IdmIdentityService identityService;
	@Autowired 
	private DefaultAccMultipleSystemAuthenticator defaultAccMultipleSystemAuthenticator;

	@After
	public void after() {
		removeAllConfiguration();
	}

	@Test
	public void testTwoSystems() {
		String passwordSystemOne = getHelper().createName();
		String passwordSystemTwo = getHelper().createName();
		String passwordIdm = getHelper().createName();

		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString(passwordIdm));

		SysSystemDto systemOne = createSystem(null);
		SysSystemDto systemTwo = createSystem("2");

		addSystemToIdentity(identity, systemOne, systemTwo);

		changePassword(identity, passwordSystemOne, getAccountIdForSystem(identity, systemOne));
		changePassword(identity, passwordSystemTwo, getAccountIdForSystem(identity, systemTwo));

		setupAuthentication(systemOne, systemTwo);

		// Bad password
		login(identity, "test", true, null);
		// IdM
		login(identity, passwordIdm, false, CoreModuleDescriptor.MODULE_ID);
		// System One
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(new GuardedString(passwordSystemOne));
		// valid credentials
		Assert.assertTrue(authenticationManager.validate(loginDto));
		login(identity, passwordSystemTwo, false, AccModuleDescriptor.MODULE_ID);
		login(identity, passwordSystemOne, false, AccModuleDescriptor.MODULE_ID);
		// System two
		// Bas password
		login(identity, passwordSystemTwo + 1, true, null);
	}
	
	@Test
	public void testLoginNotConfiguredWithWrongPassword() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString(getHelper().createName()));
		//
		Assert.assertNull(defaultAccMultipleSystemAuthenticator.authenticate(new LoginDto(identity.getUsername(), new GuardedString(getHelper().createName()))));
	}
	
	@Test(expected = MustChangePasswordException.class)
	public void testMustChangePasswordException() {
		String passwordSystem = getHelper().createName();
		String passwordIdm = getHelper().createName();

		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString(passwordIdm));
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		password.setMustChange(true);
		passwordService.save(password);

		SysSystemDto systemOne = createSystem(null);

		addSystemToIdentity(identity, systemOne);

		changePassword(identity, passwordSystem, getAccountIdForSystem(identity, systemOne));

		setupAuthentication(systemOne);

		// System One
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(new GuardedString(passwordSystem));
		
		// valid credentials
		Assert.assertTrue(authenticationManager.validate(loginDto));
		// but must change password before authentication
		authenticationManager.authenticate(loginDto);
	}
	
	@Test(expected = IdmAuthenticationException.class)
	public void testPreventLoginInvalidIdentity() {
		String passwordSystem = getHelper().createName();
		String passwordIdm = getHelper().createName();

		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString(passwordIdm));

		SysSystemDto systemOne = createSystem(null);

		addSystemToIdentity(identity, systemOne);

		changePassword(identity, passwordSystem, getAccountIdForSystem(identity, systemOne));

		setupAuthentication(systemOne);
		
		identityService.disable(identity.getId());

		// System One
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(new GuardedString(passwordSystem));
		// invalid
		Assert.assertFalse(authenticationManager.validate(loginDto));
		// and cannot login
		authenticationManager.authenticate(loginDto);
	}

	@Test
	public void testTwoSystemsWithoutSetup() {
		String passwordSystemOne = getHelper().createName();
		String passwordSystemTwo = getHelper().createName();
		String passwordIdm = getHelper().createName();

		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString(passwordIdm));

		SysSystemDto systemOne = createSystem(null);
		SysSystemDto systemTwo = createSystem("2");

		addSystemToIdentity(identity, systemOne, systemTwo);

		changePassword(identity, passwordSystemOne, getAccountIdForSystem(identity, systemOne));
		changePassword(identity, passwordSystemTwo, getAccountIdForSystem(identity, systemTwo));

		// Bad password
		login(identity, "test", true, null);
		// IdM
		login(identity, passwordIdm, false, CoreModuleDescriptor.MODULE_ID);
		// System One, but without configuration
		login(identity, passwordSystemOne, true, null);
		// System two, but without configuration
		login(identity, passwordSystemTwo, true, null);
	}

	@Test
	public void testTwoSystemsWithCombinationDefaultAuth() {
		String passwordSystemDefault = getHelper().createName();
		String passwordSystemOne = getHelper().createName();
		String passwordSystemTwo = getHelper().createName();
		String passwordIdm = getHelper().createName();

		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString(passwordIdm));

		SysSystemDto systemOne = createSystem("1");
		SysSystemDto systemTwo = createSystem("2");
		SysSystemDto systemDefault = createSystem(null);

		addSystemToIdentity(identity, systemOne, systemTwo, systemDefault);

		changePassword(identity, passwordSystemOne, getAccountIdForSystem(identity, systemOne));
		changePassword(identity, passwordSystemTwo, getAccountIdForSystem(identity, systemTwo));
		changePassword(identity, passwordSystemDefault, getAccountIdForSystem(identity, systemDefault));

		setupDefaultAuthentication(systemDefault);
		setupAuthentication(systemOne, systemTwo);

		// Bad password
		login(identity, "test", true, null);
		// IdM
		login(identity, passwordIdm, false, CoreModuleDescriptor.MODULE_ID);
		// System One
		login(identity, passwordSystemOne, false, AccModuleDescriptor.MODULE_ID);
		// System two
		login(identity, passwordSystemTwo, false, AccModuleDescriptor.MODULE_ID);
		// Default system password
		login(identity, passwordSystemDefault, false, AccModuleDescriptor.MODULE_ID);
		// Bas password
		login(identity, passwordSystemTwo + 1, true, null);
	}

	@Test
	@Ignore // Test takes about 150 seconds
	public void testPerformanceTest() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		List<SysSystemDto> systems = Lists.newArrayList();
		for (int index = 1; index < 101; index++) {
			SysSystemDto system = createSystem(String.valueOf(index));
			addSystemToIdentity(identity, system);
			
			// Stored password for identity :)
			system.setDescription(getHelper().createName());
			system = systemService.save(system);
			changePassword(identity, system.getDescription(), getAccountIdForSystem(identity, system));
			systems.add(system);
		}

		setupAuthentication(systems.toArray(new SysSystemDto[systems.size()]));

		SysSystemDto lastSystem = systems.get(systems.size() - 1);

		// Try last system
		login(identity, lastSystem.getDescription(), false, AccModuleDescriptor.MODULE_ID);

		// Try all
		for (SysSystemDto system : systems) {
			login(identity, system.getDescription(), false, AccModuleDescriptor.MODULE_ID);
		}
		
	}

	@Test
	public void testMaxCount() {
		String passwordSystemOne = getHelper().createName();
		String passwordSystemTwo = getHelper().createName();
		String passwordSystemThree = getHelper().createName();

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		SysSystemDto systemOne = createSystem(null);
		SysSystemDto systemTwo = createSystem("2");
		SysSystemDto systemThree = createSystem("3");

		addSystemToIdentity(identity, systemOne, systemTwo, systemThree);

		changePassword(identity, passwordSystemOne, getAccountIdForSystem(identity, systemOne));
		changePassword(identity, passwordSystemTwo, getAccountIdForSystem(identity, systemTwo));
		changePassword(identity, passwordSystemThree, getAccountIdForSystem(identity, systemThree));

		setupAuthentication(systemOne, systemTwo, systemThree);
		getHelper().setConfigurationValue(AuthenticatorConfiguration.PROPERTY_AUTH_MAX_SYSTEM_COUNT, "2");// Maximum is now two

		// System One
		login(identity, passwordSystemOne, false, AccModuleDescriptor.MODULE_ID);
		// System two
		login(identity, passwordSystemTwo, false, AccModuleDescriptor.MODULE_ID);
		// System three is defined but maximum is different
		login(identity, passwordSystemThree, true, null);
	}

	@Test
	public void testMissingAccount() {
		String passwordSystemOne = getHelper().createName();
		String passwordSystemTwo = getHelper().createName();
		String passwordSystemThree = getHelper().createName();

		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		SysSystemDto systemOne = createSystem(null);
		SysSystemDto systemTwo = createSystem("2");
		SysSystemDto systemThree = createSystem("3");

		// For system two missing account
		addSystemToIdentity(identity, systemOne, systemThree);

		changePassword(identity, passwordSystemOne, getAccountIdForSystem(identity, systemOne));
		changePassword(identity, passwordSystemThree, getAccountIdForSystem(identity, systemThree));

		// Definition exists
		setupAuthentication(systemOne, systemTwo, systemThree);

		// System One
		login(identity, passwordSystemOne, false, AccModuleDescriptor.MODULE_ID);
		// System two - missing account
		login(identity, passwordSystemTwo, true, null); // Missing account
		// System three
		login(identity, passwordSystemThree, false, AccModuleDescriptor.MODULE_ID);
	}

	public TestHelper getHelper() {
		return helper;
	}

	/**
	 * Setup authentication properties for authentiyation for given system
	 *
	 * @param systems
	 */
	private void setupAuthentication(SysSystemDto ...systems) {
		int index = 1;

		for (SysSystemDto system : systems) {
			getHelper().setConfigurationValue(composeKey(index), system.getId().toString());
			index++;
		}
	}

	/**
	 * Setup default acc authenticator for given system
	 *
	 * @param system
	 */
	@SuppressWarnings("deprecation")
	private void setupDefaultAuthentication(SysSystemDto system) {
		getHelper().setConfigurationValue(AuthenticatorConfiguration.PROPERTY_AUTH_SYSTEM_ID, system.getId().toString());
	}
	
	/**
	 * Remove all configuration that may be initialized by this tests
	 */
	@SuppressWarnings("deprecation")
	private void removeAllConfiguration() {
		for (int index = 0; index < authenticatorConfiguration.getMaximumSystemCount(); index++) {
			getHelper().setConfigurationValue(composeKey(index), null);
		}

		getHelper().setConfigurationValue(AuthenticatorConfiguration.PROPERTY_AUTH_SYSTEM_ID, null);
		getHelper().setConfigurationValue(AuthenticatorConfiguration.PROPERTY_AUTH_MAX_SYSTEM_COUNT, String.valueOf(AuthenticatorConfiguration.DEFAULT_AUTH_MAX_SYSTEM_COUNT));
	}

	private String composeKey(int index) {
		StringBuilder property = new StringBuilder();
		property.append(AuthenticatorConfiguration.PROPERTY_AUTH_PREFIX);
		property.append(index);
		property.append(ConfigurationService.PROPERTY_SEPARATOR);
		property.append(AuthenticatorConfiguration.AUTH_SYSTEM_SEPARATOR);
		return property.toString();
	}
	/**
	 * Create identity account for given identity on all given systems
	 *
	 * @param identity
	 */
	private void addSystemToIdentity(IdmIdentityDto identity, SysSystemDto ...systems) {
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		for(SysSystemDto system : systems) {
			IdmRoleDto role = getHelper().createRole();
			getHelper().createRoleSystem(role, system);
			getHelper().assignRoles(contract, role);
		}
	}

	/**
	 * Process authentication against {@link AuthenticationManager} - IdM and all available system.
	 *
	 * @param identity
	 * @param password
	 * @param failed
	 * @return
	 */
	private LoginDto login(IdmIdentityDto identity, String password, boolean failed, String module) {
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(new GuardedString(password));

		LoginDto authenticate = null;
		try {
			authenticate = authenticationManager.authenticate(loginDto);
		} catch (IdmAuthenticationException e) {
			authenticate = null;
		}

		if (failed) {
			assertNull(authenticate);
		} else {
			assertNotNull(authenticate);
			assertEquals(module, authenticate.getAuthenticationModule());
		}

		return authenticate;
	}

	/**
	 * Change password for given identity with given password
	 *
	 * @param identity
	 * @param password
	 * @param all
	 * @param idm
	 * @param accounts
	 */
	private void changePassword(IdmIdentityDto identity, String password, String... accounts) {
		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(false);
		passwordChangeDto.setIdm(false);
		if (accounts != null && accounts.length > 0) {
			passwordChangeDto.setAccounts(Lists.newArrayList(accounts));
		}
		passwordChangeDto.setNewPassword(new GuardedString(password));
		// change password for system
		List<OperationResult> changePassword = provisioningService.changePassword(identity, passwordChangeDto);
		assertEquals(1, changePassword.size());
	}

	/**
	 * Get account ID for system and identity
	 *
	 * @param identity
	 * @param system
	 * @return
	 */
	private String getAccountIdForSystem(IdmIdentityDto identity, SysSystemDto system) {
		AccAccountFilter filter = new AccAccountFilter();
		filter.setIdentityId(identity.getId());
		filter.setSystemId(system.getId());

		List<AccAccountDto> content = accountService.find(filter, null).getContent();
		assertEquals(1, content.size()); // Tests expect only one account
		return content.get(0).getId().toString();
	}

	/**
	 * Create system with mapping and if exists parametr uidSuffix add this suffix into __NAME__ transforamtion to.
	 * This behavior can help for create different account for same system and one identity.
	 *
	 * @param uidSuffix
	 * @return 
	 */
	private SysSystemDto createSystem(String uidSuffix) {
		SysSystemDto system = getHelper().createSystem(TestResource.TABLE_NAME, getHelper().createName());

		SysSystemMappingDto mapping = getHelper().createMapping(system);

		if (uidSuffix != null) {
			List<SysSystemAttributeMappingDto> attributes = systemAttributeMappingService.findBySystemMapping(mapping);
			
			for (SysSystemAttributeMappingDto attribute : attributes) {
				if (TestHelper.ATTRIBUTE_MAPPING_NAME.equals(attribute.getName())) {
					attribute.setTransformToResourceScript("return attributeValue + " + uidSuffix + ";");
					attribute.setAuthenticationAttribute(true);
					systemAttributeMappingService.save(attribute);
				}
			}
		}

		return system;
	}
}
