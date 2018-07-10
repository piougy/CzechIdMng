package eu.bcvsolutions.idm.core.security.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Tests for {@link DefaultJwtAuthenticationService}.
 * 
 * @author Alena Peterová
 * @author Radek Tomiška
 *
 */
@Transactional
public class DefaultJwtAuthenticationServiceTest extends AbstractIntegrationTest {

	@Autowired private SecurityService securityService;
	@Autowired private DefaultJwtAuthenticationService jwtAuthenticationService;

	private static final String MODULE = "djastModule";

	@Test
	public void createJwtAuthenticationAndAuthenticateTest() {
		String username = getHelper().createName();
		IdmIdentityDto identityDto = getHelper().createIdentity(username, (GuardedString) null);
		LoginDto loginDto =  new LoginDto(identityDto.getUsername(), identityDto.getPassword());

		LoginDto resultLoginDto = jwtAuthenticationService.createJwtAuthenticationAndAuthenticate(loginDto, identityDto, MODULE);

		Assert.assertTrue(securityService.isAuthenticated());
		Assert.assertEquals(username, securityService.getCurrentUsername());

		Assert.assertEquals(username, resultLoginDto.getUsername());
		Assert.assertEquals(MODULE, resultLoginDto.getAuthenticationModule());
		Assert.assertNotNull(resultLoginDto.getToken());

		IdmJwtAuthenticationDto jwtAuthenticationDto = resultLoginDto.getAuthentication();
		Assert.assertNotNull(jwtAuthenticationDto);
		Assert.assertEquals(username, jwtAuthenticationDto.getCurrentUsername());
		Assert.assertEquals(MODULE, jwtAuthenticationDto.getFromModule());
		Assert.assertTrue(resultLoginDto.getAuthorities().isEmpty());
	}
}
