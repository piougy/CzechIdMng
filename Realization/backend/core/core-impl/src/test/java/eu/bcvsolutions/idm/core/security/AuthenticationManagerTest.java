package eu.bcvsolutions.idm.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.authentication.Authenticator;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Default test for {@link AuthenticationManager} and core {@link Authenticator}.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class AuthenticationManagerTest extends AbstractIntegrationTest {
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private IdmIdentityService identityService;
	
	@Before
	public void login() {
		loginAsAdmin("admin");
	}
	
	@After
	@Override
	public void logout() {
		super.logout();
	}
	
	@Transactional
	@Test(expected = AuthenticationException.class)
	public void loginViaManagerBadCredentials() {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername("test_login_1");
		identity.setLastName("test_login_1");
		identity.setPassword(new GuardedString("test1234"));
		identity = this.identityService.save(identity);
		
		LoginDto loginDto = new LoginDto();
		loginDto.setPassword(new GuardedString("test12345"));
		loginDto.setUsername("test_login_1");
		
		authenticationManager.authenticate(loginDto);
		fail();
	}
	
	@Test
	@Transactional
	public void loginViaManagerSuccesful() {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername("test_login_2");
		identity.setLastName("test_login_2");
		identity.setPassword(new GuardedString("test1234"));
		identity = this.identityService.save(identity);
		
		LoginDto loginDto = new LoginDto();
		loginDto.setPassword(new GuardedString("test1234"));
		loginDto.setUsername("test_login_2");
		
		loginDto = authenticationManager.authenticate(loginDto);
		assertNotNull(loginDto);
		assertNotNull(loginDto.getAuthentication());
		assertEquals("core", loginDto.getAuthenticationModule());
	}
}
