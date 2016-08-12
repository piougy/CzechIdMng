package eu.bcvsolutions.idm.security;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;

import eu.bcvsolutions.idm.core.AbstractIntegrationTest;
import eu.bcvsolutions.idm.core.TestUtils;
import eu.bcvsolutions.idm.core.model.domain.ResourceWrapper;
import eu.bcvsolutions.idm.security.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.security.dto.LoginDto;
import eu.bcvsolutions.idm.security.rest.LoginController;

/**
 * Login to application with test user
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public class LoginControllerTest extends AbstractIntegrationTest {

	@Autowired
	private LoginController loginController;
	
	@Test
	public void testSuccesfulLogIn() throws Exception {
		// TODO: prepare test data - through flyway? Test db initializer with rollback only?
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(TestUtils.TEST_USERNAME);
		loginDto.setPassword(TestUtils.TEST_PASSWORD);
		ResourceWrapper<LoginDto> response = loginController.login(loginDto);
		
		IdmJwtAuthenticationDto authentication = response.getResource().getAuthentication();
		
		assertNotNull(authentication);
		assertEquals(TestUtils.TEST_USERNAME, authentication.getCurrentUsername());
		assertEquals(TestUtils.TEST_USERNAME, authentication.getOriginalUsername());
	}
	
	@Test(expected = AuthenticationException.class)
	public void testBadCredentialsLogIn() {
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(TestUtils.TEST_USERNAME);
		loginDto.setPassword("wrong_pass");
		loginController.login(loginDto);
	}
}
