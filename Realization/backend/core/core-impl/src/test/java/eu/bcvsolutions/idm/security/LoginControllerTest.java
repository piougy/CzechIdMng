package eu.bcvsolutions.idm.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.security.dto.LoginDto;
import eu.bcvsolutions.idm.security.rest.impl.LoginController;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Login to application with test user
 * 
 * @author Radek Tomiška 
 *
 */
public class LoginControllerTest extends AbstractIntegrationTest {

	@Autowired
	private LoginController loginController;
	
	@Test
	@Transactional
	public void testSuccesfulLogIn() throws Exception {
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(InitTestData.TEST_ADMIN_USERNAME);
		loginDto.setPassword(new GuardedString(InitTestData.TEST_ADMIN_PASSWORD));
		ResourceWrapper<LoginDto> response = loginController.login(loginDto);
		
		IdmJwtAuthenticationDto authentication = response.getResource().getAuthentication();
		
		assertNotNull(authentication);
		assertEquals(InitTestData.TEST_ADMIN_USERNAME, authentication.getCurrentUsername());
		assertEquals(InitTestData.TEST_ADMIN_USERNAME, authentication.getOriginalUsername());
	}
	
	@Ignore
	@Test(expected = AuthenticationException.class)
	public void testBadCredentialsLogIn() {
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(InitTestData.TEST_ADMIN_USERNAME);
		loginDto.setPassword(new GuardedString("wrong_pass"));
		loginController.login(loginDto);
	}
}
