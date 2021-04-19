package eu.bcvsolutions.idm.core.security.auth.filter;

import static eu.bcvsolutions.idm.core.model.event.processor.module.InitTestDataProcessor.TEST_ADMIN_USERNAME;
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
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;
import eu.bcvsolutions.idm.core.security.service.impl.JwtAuthenticationMapper;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Test authentication using SSO.
 * 
 * @author Radek Tomiška
 * @author Alena Peterová
 */
@Transactional
public class RemoteUserAuthenticationFilterIntegrationTest extends AbstractRestTest {

	private static final String TEST_SSO_USER_SSO_ENABLED = "ssoEnabledUser";
	private static final String TEST_SSO_USER_SSO_ADMIN = "ssoAdminUser";
	
	private static final String TEST_SSO_NON_EXISTING_USER = "ssoNoUser";
	private static final String TEST_SSO_UID_SUFFIX1 = "@domain.tld";
	private static final String TEST_SSO_UID_SUFFIX2 = "@idm.test.local";
	private static final String TEST_SSO_FORBIDDEN_UID_NULL = "(null)";
	
	@Autowired private IdmConfigurationService configurationService;
	@Autowired private RemoteUserAuthenticationFilter filter;
	@Autowired private IdmIdentityService identityService;
	@Autowired private TokenManager tokenManager;
	
	@Before
	public void init() {
		configurationService.setBooleanValue(filter.getConfigurationPropertyName(ConfigurationService.PROPERTY_ENABLED), true);
		createUsers();
	}

	@After
	public void after() {
		configurationService.setBooleanValue(filter.getConfigurationPropertyName(ConfigurationService.PROPERTY_ENABLED), false);
		// reset to default
		this.logout();
	}

	@Test
	public void testSsoAuthSuccess() throws Exception {
		getMockMvc().perform(get(getRemotePath())
				.with(request -> {
                    request.setRemoteUser(TEST_SSO_USER_SSO_ENABLED);
                    return request;
                })
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TEST_SSO_USER_SSO_ENABLED)));
	}
	
	
	@Test
	public void testRemoteLoginWithMissingToken() throws Exception {
		LoginDto login = getHelper().loginAdmin();
		getHelper().logout(); // ~ disable token
		tokenManager.deleteToken(login.getAuthentication().getId()); // ~ delete token
		//
		getMockMvc().perform(get(getRemotePath())
				.with(request -> {
                    request.setRemoteUser(TEST_SSO_USER_SSO_ENABLED);
                    return request;
                })
				.header(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME, login.getToken())
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TEST_SSO_USER_SSO_ENABLED)));
	}
	
	@Test
	public void testRemoteLoginWithMissingTokenWithouAuthentication() throws Exception {
		LoginDto login = getHelper().loginAdmin();
		getHelper().logout(); // ~ disable token
		tokenManager.deleteToken(login.getAuthentication().getId()); // ~ delete token
		//
		getMockMvc().perform(get(getRemotePath())
				.header(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME, login.getToken())
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	public void testSsoAuthDisabled() throws Exception {
		configurationService.setBooleanValue(filter.getConfigurationPropertyName(ConfigurationService.PROPERTY_ENABLED), false);
		//
		getMockMvc().perform(get(getRemotePath())
				.with(request -> {
                    request.setRemoteUser(TEST_SSO_USER_SSO_ENABLED);
                    return request;
                })
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
	}

	@Test
	public void testSsoNoAuth() throws Exception {
		getMockMvc().perform(get(getRemotePath())
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
	}

	@Test
	public void testSsoAuthEmptyUser() throws Exception {
		getMockMvc().perform(get(getRemotePath())
				.with(request -> {
                    request.setRemoteUser("");
                    return request;
                })
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
	}

	@Test
	public void testSsoAuthNonExistingUser() throws Exception {
		getMockMvc().perform(get(getRemotePath())
				.with(request -> {
                    request.setRemoteUser(TEST_SSO_NON_EXISTING_USER);
                    return request;
                })
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
	}
	
	@Test
	public void testSsoAuthSuccessUidWithSuffix() throws Exception {
		configurationService.setValue(filter.getConfigurationPropertyName(RemoteUserAuthenticationFilter.PARAMETER_UID_SUFFIXES), 
				TEST_SSO_UID_SUFFIX1 + "," + TEST_SSO_UID_SUFFIX2);
		// Check first suffix
		getMockMvc().perform(get(getRemotePath())
				.with(request -> {
                    request.setRemoteUser(TEST_SSO_USER_SSO_ENABLED + TEST_SSO_UID_SUFFIX1);
                    return request;
                })
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TEST_SSO_USER_SSO_ENABLED)));
		// Check second suffix
		getMockMvc().perform(get(getRemotePath())
				.with(request -> {
                    request.setRemoteUser(TEST_SSO_USER_SSO_ENABLED + TEST_SSO_UID_SUFFIX2);
                    return request;
                })
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TEST_SSO_USER_SSO_ENABLED)));
		// Check unsupported suffix
		getMockMvc().perform(get(getRemotePath())
				.with(request -> {
                    request.setRemoteUser(TEST_SSO_USER_SSO_ENABLED + "@unsupporteddomain.tld");
                    return request;
                })
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
	}

	@Test
	public void testSsoAuthForbiddenUid() throws Exception {
		configurationService.setValue(filter.getConfigurationPropertyName(RemoteUserAuthenticationFilter.PARAMETER_FORBIDDEN_UIDS), 
				TEST_SSO_USER_SSO_ENABLED + "," + TEST_SSO_FORBIDDEN_UID_NULL);
		// Check first forbidden uid
		getMockMvc().perform(get(getRemotePath())
				.with(request -> {
                    request.setRemoteUser(TEST_SSO_USER_SSO_ENABLED);
                    return request;
                })
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
		// Check second forbidden uid
		getMockMvc().perform(get(getRemotePath())
				.with(request -> {
                    request.setRemoteUser(TEST_SSO_FORBIDDEN_UID_NULL);
                    return request;
                })
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
		//
		configurationService.setValue(filter.getConfigurationPropertyName(RemoteUserAuthenticationFilter.PARAMETER_FORBIDDEN_UIDS), 
				TEST_SSO_FORBIDDEN_UID_NULL);
		//
		// Check that the uid can be authenticated when removed from the configuration
		getMockMvc().perform(get(getRemotePath())
				.with(request -> {
                    request.setRemoteUser(TEST_SSO_USER_SSO_ENABLED);
                    return request;
                })
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TEST_SSO_USER_SSO_ENABLED)));
	}
	
	@Test
	public void testSsoAuthForbiddenIdentity() throws Exception {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		configurationService.setValue(filter.getConfigurationPropertyName(RemoteUserAuthenticationFilter.PARAMETER_FORBIDDEN_UIDS), 
				identity.getId().toString());
		//
		// Check forbidden identity
		getMockMvc().perform(get(getRemotePath())
				.with(request -> {
                    request.setRemoteUser(identity.getUsername());
                    return request;
                })
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
	}

	@Test
	public void testSsoAuthForbiddenUidWithSuffix() throws Exception {
		configurationService.setValue(filter.getConfigurationPropertyName(RemoteUserAuthenticationFilter.PARAMETER_UID_SUFFIXES), 
				TEST_SSO_UID_SUFFIX1);
		configurationService.setValue(filter.getConfigurationPropertyName(RemoteUserAuthenticationFilter.PARAMETER_FORBIDDEN_UIDS), 
				TEST_SSO_USER_SSO_ENABLED);
		//
		getMockMvc().perform(get(getRemotePath())
				.with(request -> {
                    request.setRemoteUser(TEST_SSO_USER_SSO_ENABLED + TEST_SSO_UID_SUFFIX1);
                    return request;
                })
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
	}

	@Test
	public void testSsoAuthAdmin() throws Exception {
		getMockMvc().perform(get(getRemotePath())
				.with(request -> {
                    request.setRemoteUser(TEST_ADMIN_USERNAME);
                    return request;
                })
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TEST_ADMIN_USERNAME)));
	}

	@Test
	public void testSsoAuthUserDisabledSso() throws Exception {
		getMockMvc().perform(get(getRemotePath())
				.with(request -> {
                    request.setRemoteUser(TEST_SSO_USER_SSO_ADMIN);
                    return request;
                })
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TEST_SSO_USER_SSO_ADMIN)));
	}
	
	@Test
	public void testPreventLoginInvalidIdentity() throws Exception {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		identityService.disable(identity.getId());
		//
		getMockMvc().perform(get(getRemotePath())
				.with(request -> {
                    request.setRemoteUser(identity.getUsername());
                    return request;
                })
				.contentType(TestHelper.HAL_CONTENT_TYPE))
			.andExpect(status().isForbidden());
	}
	
	private String getRemotePath() {
		return BaseDtoController.BASE_PATH + "/authentication/remote-auth";
	}
	
	private void createUsers() {
		// password is not needed
		getHelper().createIdentity(TEST_SSO_USER_SSO_ENABLED, null);
		IdmIdentityDto identityAdmin = getHelper().createIdentity(TEST_SSO_USER_SSO_ADMIN, null);
		IdmRoleDto permissionRole = getHelper().createRole();
		// TODO after creating the permission APP_SSODISABLED, set this permission here
		getHelper().createAuthorizationPolicy(permissionRole.getId(), IdmGroupPermission.APP, null, BasePermissionEvaluator.class, IdmBasePermission.ADMIN);
		getHelper().createIdentityRole(identityAdmin, permissionRole);
	}
}
