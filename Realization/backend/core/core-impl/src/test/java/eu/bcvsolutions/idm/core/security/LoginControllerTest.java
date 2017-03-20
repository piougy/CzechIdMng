package eu.bcvsolutions.idm.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.security.core.AuthenticationException;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.rest.impl.LoginController;
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
	public void testSuccesfulLogIn() throws Exception {
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(InitTestData.TEST_ADMIN_USERNAME);
		loginDto.setPassword(new GuardedString(InitTestData.TEST_ADMIN_PASSWORD));
		Resource<LoginDto> response = loginController.login(loginDto);
		
		IdmJwtAuthenticationDto authentication = response.getContent().getAuthentication();
		
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
