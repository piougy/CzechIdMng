package eu.bcvsolutions.idm.core.security.service.impl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Tests for {@link DefaultJwtAuthenticationService}.
 * 
 * @author Alena Peterová
 *
 */
public class DefaultJwtAuthenticationServiceTest extends AbstractIntegrationTest {

	@Autowired
	private IdmIdentityService idmIdentityService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private DefaultJwtAuthenticationService jwtAuthenticationService;

	private static final String MODULE = "djastModule";
	private static final String USERNAME = "djastUserName";
	
	@After 
	public void logout() {
		super.logout();
	}

	@Test
	public void createJwtAuthenticationAndAuthenticateTest() {

		createTestUser();
		IdmIdentityDto identityDto = getTestIdentity();
		LoginDto loginDto = getTestLoginDto();

		LoginDto resultLoginDto = jwtAuthenticationService.createJwtAuthenticationAndAuthenticate(loginDto, identityDto,
				MODULE);

		Assert.assertTrue(securityService.isAuthenticated());
		Assert.assertEquals(USERNAME, securityService.getCurrentUsername());

		Assert.assertEquals(USERNAME, resultLoginDto.getUsername());
		Assert.assertEquals(MODULE, resultLoginDto.getAuthenticationModule());
		Assert.assertNotNull(resultLoginDto.getToken());

		IdmJwtAuthenticationDto jwtAuthenticationDto = resultLoginDto.getAuthentication();
		Assert.assertNotNull(jwtAuthenticationDto);
		Assert.assertEquals(USERNAME, jwtAuthenticationDto.getCurrentUsername());
		Assert.assertEquals(MODULE, jwtAuthenticationDto.getFromModule());
		Assert.assertTrue(jwtAuthenticationDto.getAuthorities().isEmpty());

	}

	private void createTestUser() {
		loginAsAdmin("admin");

		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername(USERNAME);
		identity.setLastName(USERNAME);
		identity = idmIdentityService.save(identity);

		logout();
	}

	private LoginDto getTestLoginDto() {
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(USERNAME);
		return loginDto;
	}

	private IdmIdentityDto getTestIdentity() {
		return idmIdentityService.getByUsername(USERNAME);
	}
}
