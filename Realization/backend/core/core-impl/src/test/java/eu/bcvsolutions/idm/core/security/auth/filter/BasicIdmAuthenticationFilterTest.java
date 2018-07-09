package eu.bcvsolutions.idm.core.security.auth.filter;

import static eu.bcvsolutions.idm.InitTestData.HAL_CONTENT_TYPE;
import static eu.bcvsolutions.idm.InitTestData.TEST_ADMIN_PASSWORD;
import static eu.bcvsolutions.idm.InitTestData.TEST_ADMIN_USERNAME;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.rest.impl.PasswordChangeController;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.core.security.evaluator.identity.SelfIdentityEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;

/**
 * Test authentication using the basic scheme. 
 * 
 * @author Jan Helbich
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */
public class BasicIdmAuthenticationFilterTest extends AbstractRestTest {

	@Autowired private IdmConfigurationService configurationService;
	@Autowired private LoginService loginService;
	@Autowired private IdmPasswordService passwordService;
	@Autowired private IdmIdentityService identityService;
	@Autowired private PasswordChangeController passwordChangeController;
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	
	@After
	public void after() {
		// reset to default
		configurationService.setBooleanValue(IdentityConfiguration.PROPERTY_PUBLIC_CHANGE_PASSWORD_FOR_IDM_ENABLED, true);
		this.logout();
	}
	
	@Test
	public void testBasicAuthSuccess() throws Exception {
		String basedAuth = getBasicAuth(TEST_ADMIN_USERNAME, TEST_ADMIN_PASSWORD);
		
		getMockMvc().perform(get(getSelfPath(TEST_ADMIN_USERNAME))
				.header("Authorization", "Basic " + basedAuth)
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TEST_ADMIN_USERNAME)));
	}
	
	@Test
	public void testBasicAuthFail() throws Exception {
		String basedAuth = getBasicAuth(TEST_ADMIN_USERNAME, "");
		
		getMockMvc().perform(get(getSelfPath(TEST_ADMIN_USERNAME))
				.header("Authorization", "Basic " + basedAuth)
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
	}
	
	@Test
	public void testDisableIdmPasswordChange() {
		String testPassword = "testPassword";
		String newTestPassword = "newTestPassword";
		//
		this.loginAsAdmin();
		configurationService.setBooleanValue(IdentityConfiguration.PROPERTY_PUBLIC_CHANGE_PASSWORD_FOR_IDM_ENABLED, false);
		//
		// create identity
		IdmIdentityDto identity = getHelper().createIdentity();
		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setNewPassword(new GuardedString(testPassword));
		passwordService.save(identity, passwordChangeDto);
		this.logout();
		//
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(new GuardedString(testPassword));
		LoginDto login = loginService.login(loginDto);
		//
		assertNotNull(login.getAuthentication());
		//
		passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setNewPassword(new GuardedString(newTestPassword));
		passwordChangeDto.setOldPassword(new GuardedString(testPassword));
		passwordChangeDto.setAll(true);
		passwordChangeDto.setIdm(true);
		//
		List<OperationResult> passwordChangeResults = identityService.passwordChange(identity, passwordChangeDto);
		//
		assertEquals(1, passwordChangeResults.size());
		OperationResult operationResult = passwordChangeResults.get(0);
		assertEquals(OperationState.EXECUTED, operationResult.getState());
	}
	
	@Test
	public void testEnableIdmPasswordChange() {
		String testPassword = "testPassword";
		String newTestPassword = "newTestPassword";
		//
		loginAsAdmin();
		configurationService.setBooleanValue(IdentityConfiguration.PROPERTY_PUBLIC_CHANGE_PASSWORD_FOR_IDM_ENABLED, true);
		//
		// create identity
		IdmIdentityDto identity = getHelper().createIdentity();
		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setNewPassword(new GuardedString(testPassword));
		passwordService.save(identity, passwordChangeDto);
		this.logout();
		//
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(new GuardedString(testPassword));
		LoginDto login = loginService.login(loginDto);
		//
		assertNotNull(login.getAuthentication());
		//
		passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setNewPassword(new GuardedString(newTestPassword));
		passwordChangeDto.setOldPassword(new GuardedString(testPassword));
		passwordChangeDto.setAll(true);
		passwordChangeDto.setIdm(true);
		//
		List<OperationResult> passwordChangeResults = identityService.passwordChange(identity, passwordChangeDto);
		//
		assertEquals(1, passwordChangeResults.size());
		OperationResult operationResult = passwordChangeResults.get(0);
		assertEquals(OperationState.EXECUTED, operationResult.getState());
		assertEquals(CoreResultCode.PASSWORD_CHANGE_ACCOUNT_SUCCESS.name(), operationResult.getModel().getStatusEnum());
		assertEquals(HttpStatus.OK, operationResult.getModel().getStatus());
		//
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(new GuardedString(newTestPassword));
		login = loginService.login(loginDto);
		//
		assertNotNull(login.getAuthentication());
	}
	
	@Test
	public void testDisableIdmPasswordChangeViaRest() throws JsonProcessingException {
		String testPassword = "testPassword";
		String newTestPassword = "newTestPassword";
		//
		this.loginAsAdmin();
		configurationService.setBooleanValue(IdentityConfiguration.PROPERTY_PUBLIC_CHANGE_PASSWORD_FOR_IDM_ENABLED, false);
		//
		// create identity
		IdmIdentityDto identity = createIdentityInTransaction(testPassword);

		// allow password change
		IdmRoleDto roleWithPermission = getHelper().createRole();
		getHelper().createAuthorizationPolicy(roleWithPermission.getId(), CoreGroupPermission.IDENTITY, IdmIdentity.class, SelfIdentityEvaluator.class, IdentityBasePermission.PASSWORDCHANGE);
		getHelper().assignRoles(getHelper().getPrimeContract(identity.getId()), roleWithPermission);
		this.logout();
		
		
		authorizationPolicyService.getDefaultAuthorities(identity.getId());
		
		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setIdm(true);
		passwordChangeDto.setNewPassword(new GuardedString(newTestPassword));
		passwordChangeDto.setOldPassword(new GuardedString(testPassword));
		List<OperationResult> passwordChangeResults = passwordChangeController.passwordChange(identity.getUsername(), passwordChangeDto);
		
		assertEquals(0, passwordChangeResults.size());
	}
	
	@Test
	public void testEnableIdmPasswordChangeViaRest() throws JsonProcessingException {
		String testPassword = "testPassword";
		String newTestPassword = "newTestPassword";
		//
		this.loginAsAdmin();
		configurationService.setBooleanValue(IdentityConfiguration.PROPERTY_PUBLIC_CHANGE_PASSWORD_FOR_IDM_ENABLED, true);
		//
		// create identity
		IdmIdentityDto identity = createIdentityInTransaction(testPassword);

		// allow password change
		IdmRoleDto roleWithPermission = getHelper().createRole();
		getHelper().createAuthorizationPolicy(roleWithPermission.getId(), CoreGroupPermission.IDENTITY, IdmIdentity.class, SelfIdentityEvaluator.class, IdentityBasePermission.PASSWORDCHANGE);
		getHelper().assignRoles(getHelper().getPrimeContract(identity.getId()), roleWithPermission);
		this.logout();
		
		
		authorizationPolicyService.getDefaultAuthorities(identity.getId());
		
		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setIdm(true);
		passwordChangeDto.setNewPassword(new GuardedString(newTestPassword));
		passwordChangeDto.setOldPassword(new GuardedString(testPassword));
		List<OperationResult> passwordChangeResults = passwordChangeController.passwordChange(identity.getUsername(), passwordChangeDto);
		
		assertEquals(1, passwordChangeResults.size());
		OperationResult operationResult = passwordChangeResults.get(0);
		assertEquals(OperationState.EXECUTED, operationResult.getState());
		assertEquals(CoreResultCode.PASSWORD_CHANGE_ACCOUNT_SUCCESS.name(), operationResult.getModel().getStatusEnum());
		assertEquals(HttpStatus.OK, operationResult.getModel().getStatus());
		
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(identity.getUsername());
		loginDto.setPassword(new GuardedString(newTestPassword));
		LoginDto login = loginService.login(loginDto);
		
		assertNotNull(login.getAuthentication());
	}
	
	/**
	 * Method create identity in transaction and set password
	 *
	 * @return
	 */
	private IdmIdentityDto createIdentityInTransaction(String password) {
		return getTransactionTemplate().execute(new TransactionCallback<IdmIdentityDto>() {
			public IdmIdentityDto doInTransaction(TransactionStatus transactionStatus) {
				IdmIdentityDto identity = getHelper().createIdentity();
				if (password != null) {
					PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
					passwordChangeDto.setNewPassword(new GuardedString(password));
					passwordService.save(identity, passwordChangeDto);
				}
				return identity;
			}
		});
	}
	
	private String getSelfPath(String user) {
		return BaseDtoController.BASE_PATH + "/identities/" + user;
	}
}
