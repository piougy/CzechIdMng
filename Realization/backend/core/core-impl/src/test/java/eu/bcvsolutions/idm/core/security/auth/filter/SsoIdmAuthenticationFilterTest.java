package eu.bcvsolutions.idm.core.security.auth.filter;

import static eu.bcvsolutions.idm.InitTestData.HAL_CONTENT_TYPE;
import static eu.bcvsolutions.idm.InitTestData.TEST_ADMIN_USERNAME;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;

/**
 * Test authentication using SSO.
 * 
 * @author Alena Peterová
 * @author Radek Tomiška
 */
@Transactional
public class SsoIdmAuthenticationFilterTest extends AbstractRestTest {

	private static final String TEST_SSO_USER_SSO_ENABLED = "ssoEnabledUser";
	private static final String TEST_SSO_USER_SSO_DISABLED = "ssoDisabledUser";
	
	private static final String TEST_SSO_HEADER = "TEST_SSO_HEADER";
	private static final String TEST_SSO_NON_EXISTING_USER = "ssoNoUser";
	private static final String TEST_SSO_UID_SUFFIX1 = "@domain.tld";
	private static final String TEST_SSO_UID_SUFFIX2 = "@idm.test.local";
	private static final String TEST_SSO_FORBIDDEN_UID_NULL = "(null)";
	
	@Autowired private IdmConfigurationService configurationService;
	@Autowired private SsoIdmAuthenticationFilter filter;
	
	@Before
	public void init() {
		configurationService.setBooleanValue(filter.getConfigurationPropertyName(ConfigurationService.PROPERTY_ENABLED), true);
		configurationService.setValue(filter.getConfigurationPropertyName(SsoIdmAuthenticationFilter.PARAMETER_HEADER_NAME), TEST_SSO_HEADER);
		createUsers();
	}

	@After
	public void after() {
		// reset to default
		this.logout();
	}

	@Test
	public void testSsoAuthSuccess() throws Exception {
		getMockMvc().perform(get(getRemotePath())
				.header(TEST_SSO_HEADER, TEST_SSO_USER_SSO_ENABLED)
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TEST_SSO_USER_SSO_ENABLED)));
	}
	
	@Test
	public void testSsoAuthDisabled() throws Exception {
		configurationService.setBooleanValue(filter.getConfigurationPropertyName(ConfigurationService.PROPERTY_ENABLED), false);
		//
		getMockMvc().perform(get(getRemotePath())
				.header(TEST_SSO_HEADER, TEST_SSO_USER_SSO_ENABLED)
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
	}

	@Test
	public void testSsoAuthNoToken() throws Exception {
		getMockMvc().perform(get(getRemotePath())
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
	}

	@Test
	public void testSsoAuthEmptyToken() throws Exception {
		getMockMvc().perform(get(getRemotePath())
				.header(TEST_SSO_HEADER, "")
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
	}

	@Test
	public void testSsoAuthNonExistingUser() throws Exception {
		getMockMvc().perform(get(getRemotePath())
				.header(TEST_SSO_HEADER, TEST_SSO_NON_EXISTING_USER)
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
	}
	
	@Test
	public void testSsoAuthSuccessUidWithSuffix() throws Exception {
		configurationService.setValue(filter.getConfigurationPropertyName(SsoIdmAuthenticationFilter.PARAMETER_UID_SUFFIXES), 
				TEST_SSO_UID_SUFFIX1 + "," + TEST_SSO_UID_SUFFIX2);
		// Check first suffix
		getMockMvc().perform(get(getRemotePath())
				.header(TEST_SSO_HEADER, TEST_SSO_USER_SSO_ENABLED + TEST_SSO_UID_SUFFIX1)
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TEST_SSO_USER_SSO_ENABLED)));
		// Check second suffix
		getMockMvc().perform(get(getRemotePath())
				.header(TEST_SSO_HEADER, TEST_SSO_USER_SSO_ENABLED + TEST_SSO_UID_SUFFIX2)
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TEST_SSO_USER_SSO_ENABLED)));
		// Check unsupported suffix
		getMockMvc().perform(get(getRemotePath())
				.header(TEST_SSO_HEADER, TEST_SSO_USER_SSO_ENABLED + "@unsupporteddomain.tld")
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
	}

	@Test
	public void testSsoAuthForbiddenUid() throws Exception {
		configurationService.setValue(filter.getConfigurationPropertyName(SsoIdmAuthenticationFilter.PARAMETER_FORBIDDEN_UIDS), 
				TEST_SSO_USER_SSO_ENABLED + "," + TEST_SSO_FORBIDDEN_UID_NULL);
		// Check first forbidden uid
		getMockMvc().perform(get(getRemotePath())
				.header(TEST_SSO_HEADER, TEST_SSO_USER_SSO_ENABLED)
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
		// Check second forbidden uid
		getMockMvc().perform(get(getRemotePath())
				.header(TEST_SSO_HEADER, TEST_SSO_FORBIDDEN_UID_NULL)
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
		//
		configurationService.setValue(filter.getConfigurationPropertyName(SsoIdmAuthenticationFilter.PARAMETER_FORBIDDEN_UIDS), 
				TEST_SSO_FORBIDDEN_UID_NULL);
		//
		// Check that the uid can be authenticated when removed from the configuration
		getMockMvc().perform(get(getRemotePath())
				.header(TEST_SSO_HEADER, TEST_SSO_USER_SSO_ENABLED)
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TEST_SSO_USER_SSO_ENABLED)));
	}
	
	@Test
	public void testSsoAuthForbiddenIdentity() throws Exception {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		configurationService.setValue(filter.getConfigurationPropertyName(SsoIdmAuthenticationFilter.PARAMETER_FORBIDDEN_UIDS), 
				identity.getId().toString());
		//
		// Check forbidden identity
		getMockMvc().perform(get(getRemotePath())
				.header(TEST_SSO_HEADER, identity.getUsername())
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
	}

	@Test
	public void testSsoAuthForbiddenUidWithSuffix() throws Exception {
		configurationService.setValue(filter.getConfigurationPropertyName(SsoIdmAuthenticationFilter.PARAMETER_UID_SUFFIXES), 
				TEST_SSO_UID_SUFFIX1);
		configurationService.setValue(filter.getConfigurationPropertyName(SsoIdmAuthenticationFilter.PARAMETER_FORBIDDEN_UIDS), 
				TEST_SSO_USER_SSO_ENABLED);
		//
		getMockMvc().perform(get(getRemotePath())
				.header(TEST_SSO_HEADER, TEST_SSO_USER_SSO_ENABLED + TEST_SSO_UID_SUFFIX1)
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
	}

	@Test
	public void testSsoAuthAdminDisabledSso() throws Exception {
		getMockMvc().perform(get(getRemotePath())
				.header(TEST_SSO_HEADER, TEST_ADMIN_USERNAME)
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
	}

	@Test
	public void testSsoAuthUserDisabledSso() throws Exception {
		getMockMvc().perform(get(getRemotePath())
				.header(TEST_SSO_HEADER, TEST_SSO_USER_SSO_DISABLED)
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
	}
	
	private String getRemotePath() {
		return BaseDtoController.BASE_PATH + "/authentication/remote-auth";
	}
	
	private void createUsers() {
		// password is not needed
		getHelper().createIdentity(TEST_SSO_USER_SSO_ENABLED, null);
		IdmIdentityDto identityDisabled = getHelper().createIdentity(TEST_SSO_USER_SSO_DISABLED, null);
		IdmRoleDto permissionRole = getHelper().createRole();
		// TODO after creating the permission APP_SSODISABLED, set this permission here
		getHelper().createAuthorizationPolicy(permissionRole.getId(), IdmGroupPermission.APP, null, BasePermissionEvaluator.class, IdmBasePermission.ADMIN);
		getHelper().createIdentityRole(identityDisabled, permissionRole);
	}
}
