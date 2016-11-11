package eu.bcvsolutions.idm.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.security.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.security.dto.LoginDto;
import eu.bcvsolutions.idm.security.rest.LoginController;
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
		// TODO: prepare test data - through flyway? Test db initializer with rollback only?
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(InitTestData.TEST_ADMIN_USERNAME);
		loginDto.setPassword(InitTestData.TEST_ADMIN_PASSWORD);
		ResourceWrapper<LoginDto> response = loginController.login(loginDto);
		
		IdmJwtAuthenticationDto authentication = response.getResource().getAuthentication();
		
		assertNotNull(authentication);
		assertEquals(InitTestData.TEST_ADMIN_USERNAME, authentication.getCurrentUsername());
		assertEquals(InitTestData.TEST_ADMIN_USERNAME, authentication.getOriginalUsername());
	}
	
	@Test(expected = AuthenticationException.class)
	public void testBadCredentialsLogIn() {
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(InitTestData.TEST_ADMIN_USERNAME);
		loginDto.setPassword("wrong_pass");
		loginController.login(loginDto);
	}
}
