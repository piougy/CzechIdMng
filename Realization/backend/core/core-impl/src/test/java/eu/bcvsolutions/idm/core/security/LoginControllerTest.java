package eu.bcvsolutions.idm.core.security;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;
import eu.bcvsolutions.idm.core.security.rest.impl.LoginController;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Login to application with test user
 * 
 * @author Radek Tomiška 
 *
 */
@Transactional
public class LoginControllerTest extends AbstractIntegrationTest {

	@Autowired private LoginController loginController;
	@Autowired private TokenManager tokenManager;
	
	@Test
	public void testSuccesfulLogIn() throws Exception {
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(TestHelper.ADMIN_USERNAME);
		loginDto.setPassword(new GuardedString(TestHelper.ADMIN_PASSWORD));
		Resource<LoginDto> response = loginController.login(loginDto);
		
		IdmJwtAuthenticationDto authentication = response.getContent().getAuthentication();
		
		Assert.assertNotNull(authentication);
		Assert.assertEquals(TestHelper.ADMIN_USERNAME, authentication.getCurrentUsername());
		Assert.assertEquals(TestHelper.ADMIN_USERNAME, authentication.getOriginalUsername());
	}
	
	@Test
	public void testSuccesfulLogout() throws Exception {
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(TestHelper.ADMIN_USERNAME);
		loginDto.setPassword(new GuardedString(TestHelper.ADMIN_PASSWORD));
		Resource<LoginDto> response = loginController.login(loginDto);
		
		IdmJwtAuthenticationDto authentication = response.getContent().getAuthentication();
		
		Assert.assertNotNull(authentication.getId());
		Assert.assertFalse(tokenManager.getToken(authentication.getId()).isDisabled());
		//
		loginController.logout();
		//
		Assert.assertTrue(tokenManager.getToken(authentication.getId()).isDisabled());
	}
	
	@Test(expected = AuthenticationException.class)
	public void testBadCredentialsLogIn() {
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(InitTestData.TEST_ADMIN_USERNAME);
		loginDto.setPassword(new GuardedString("wrong_pass"));
		loginController.login(loginDto);
	}
}
