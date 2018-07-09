package eu.bcvsolutions.idm.core.security.service.impl;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.activiti.engine.IdentityService;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmTokenService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Tests authentication manager for internal Idm authentication scheme.
 * 
 * @author Jan Helbich
 * @author Radek Tomiška
 *
 */
public class OAuthAuthenticationManagerTest extends AbstractUnitTest {
	
	public static final String USER_NAME = "test_user";

	@Mock private IdmIdentityService identityService;
	@Mock private IdentityService workflowIdentityService;
	@Mock private SecurityService securityService;
	@Mock private IdmTokenService tokenService;
	//
	private DefaultTokenManager tokenManager;
	private OAuthAuthenticationManager authManager;
	
	
	@Before
	public void init() {
		tokenManager = new DefaultTokenManager();
		tokenManager.setTokenService(tokenService);
		authManager = new OAuthAuthenticationManager(identityService, workflowIdentityService, securityService, tokenManager);
	}
	
	/**
	 * Successful authentication
	 */
	@Test
	public void testAuthSuccess() {
		IdmIdentityDto i = getTestIdentity();
		IdmJwtAuthentication authentication = getAuthentication(UUID.randomUUID(), i, DateTime.now().plusHours(1), DateTime.now());
		when(identityService.get(i.getId())).thenReturn(i);
		doNothing().when(workflowIdentityService).setAuthenticatedUserId(USER_NAME);
		doNothing().when(securityService).setAuthentication(authentication);
		IdmTokenDto token = new IdmTokenDto(authentication.getId());
		token.setOwnerId(i.getId());
		when(tokenService.get(authentication.getId())).thenReturn(token);
		
		Authentication auth = authManager.authenticate(authentication);
		
		Assert.assertEquals(USER_NAME, auth.getName());
		Assert.assertEquals(USER_NAME, auth.getPrincipal());
		Assert.assertTrue(auth.getAuthorities().isEmpty());
		
		verify(identityService).get(i.getId());
		verify(workflowIdentityService).setAuthenticatedUserId(USER_NAME);
		verify(securityService).setAuthentication(authentication);
		verify(tokenService).get(authentication.getId());
	}
	
	/**
	 * Non-existent identities cannot possess auth. tokens. 
	 */
	@Test
	public void testIdentityNotExists() {
		IdmIdentityDto i = getTestIdentity(); 
		IdmJwtAuthentication authentication = getAuthentication(
				UUID.randomUUID(), i,
				DateTime.now().plusHours(1), DateTime.now());
		when(identityService.getByUsername(i.getUsername())).thenReturn(null);
		IdmTokenDto token = new IdmTokenDto(authentication.getId());
		token.setOwnerId(UUID.randomUUID());
		when(tokenService.get(token.getId())).thenReturn(token);
		try {
			authManager.authenticate(authentication);
			Assert.fail("Cannot authenticate unknown identity.");
		} catch (AuthenticationException e) {
			verify(identityService).get(token.getOwnerId());
			verify(tokenService).get(token.getId());
		}
	}
	
	/**
	 * Expired tokens are not accepted.
	 */
	@Test(expected = ResultCodeException.class)
	public void testAuthExpired() {
		IdmIdentityDto i = getTestIdentity();
		IdmTokenDto token = new IdmTokenDto(UUID.randomUUID());
		token.setExpiration(DateTime.now().minusHours(1));
		when(identityService.get(i.getId())).thenReturn(i);
		when(tokenService.get(token.getId())).thenReturn(token);
		
		IdmJwtAuthentication authentication = getAuthentication(token.getId(), i,
				DateTime.now().minusHours(1), DateTime.now().plusHours(2));
		
		authManager.authenticate(authentication);
		Assert.fail("Cannot authenticate with expired token.");
	}

	private IdmIdentityDto getTestIdentity() {
		IdmIdentityDto i = new IdmIdentityDto(UUID.randomUUID());
		i.setUsername(USER_NAME);
		return i;
	}
	
	private IdmJwtAuthentication getAuthentication(UUID tokenId, IdmIdentityDto identity, DateTime exp, DateTime iat) {
		return getAuthentication(tokenId, identity, exp, iat, new ArrayList<>());
	}

	private IdmJwtAuthentication getAuthentication(
			UUID tokenId, 
			IdmIdentityDto identity, 
			DateTime exp, 
			DateTime iat, 
			Collection<GrantedAuthority> authorities) {
		return new IdmJwtAuthentication( 
				tokenId,
				identity,
				identity,
				exp,
				iat,
				authorities,
				"testmodule");
	}

}
