package eu.bcvsolutions.idm.core.security.service.impl;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.activiti.engine.IdentityService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmTokenService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Tests authentication manager for internal Idm authentication scheme.
 * 
 * @author Jan Helbich
 * @author Radek Tomiška
 *
 */
public class OAuthAuthenticationManagerUnitTest extends AbstractUnitTest {
	
	public static final String USER_NAME = "test_user";

	@Mock private IdmIdentityService identityService;
	@Mock private IdentityService workflowIdentityService;
	@Mock private SecurityService securityService;
	@Mock private IdmTokenService tokenService;
	@Mock private IdmCacheManager cacheManager;
	//
	@InjectMocks
	private DefaultTokenManager tokenManager;
	private OAuthAuthenticationManager authManager;
	
	@Before
	public void init() {
		authManager = new OAuthAuthenticationManager(identityService, workflowIdentityService, securityService, tokenManager);
	}
	
	/**
	 * Successful authentication
	 */
	@Test
	public void testAuthSuccess() {
		IdmIdentityDto i = getTestIdentity();
		IdmJwtAuthentication authentication = getAuthentication(UUID.randomUUID(), i, ZonedDateTime.now().plusHours(1), ZonedDateTime.now());
		doNothing().when(workflowIdentityService).setAuthenticatedUserId(USER_NAME);
		doNothing().when(securityService).setAuthentication(authentication);
		IdmTokenDto token = new IdmTokenDto(authentication.getId());
		token.setOwnerId(i.getId());
		when(tokenService.get(authentication.getId())).thenReturn(token);
		when(cacheManager.getValue(TokenManager.TOKEN_CACHE_NAME, token.getId())).thenReturn(null);
		
		Authentication auth = authManager.authenticate(authentication);
		
		Assert.assertEquals(USER_NAME, auth.getName());
		Assert.assertEquals(USER_NAME, auth.getPrincipal());
		Assert.assertTrue(auth.getAuthorities().isEmpty());
		
		verify(workflowIdentityService).setAuthenticatedUserId(USER_NAME);
		verify(securityService).setAuthentication(authentication);
		verify(tokenService).get(authentication.getId());
		verify(cacheManager).getValue(TokenManager.TOKEN_CACHE_NAME, token.getId());
	}
	
	/**
	 * Expired tokens are not accepted.
	 */
	@Test(expected = ResultCodeException.class)
	public void testAuthExpired() {
		IdmIdentityDto i = getTestIdentity();
		IdmTokenDto token = new IdmTokenDto(UUID.randomUUID());
		token.setExpiration(ZonedDateTime.now().minusHours(1));
		when(tokenService.get(token.getId())).thenReturn(token);
		when(cacheManager.getValue(TokenManager.TOKEN_CACHE_NAME, token.getId())).thenReturn(null);
		
		IdmJwtAuthentication authentication = getAuthentication(token.getId(), i,
				ZonedDateTime.now().minusHours(1), ZonedDateTime.now().plusHours(2));
		
		authManager.authenticate(authentication);
		Assert.fail("Cannot authenticate with expired token.");
	}

	private IdmIdentityDto getTestIdentity() {
		IdmIdentityDto i = new IdmIdentityDto(UUID.randomUUID());
		i.setUsername(USER_NAME);
		return i;
	}
	
	private IdmJwtAuthentication getAuthentication(UUID tokenId, IdmIdentityDto identity, ZonedDateTime exp, ZonedDateTime iat) {
		return getAuthentication(tokenId, identity, exp, iat, new ArrayList<>());
	}

	private IdmJwtAuthentication getAuthentication(
			UUID tokenId, 
			IdmIdentityDto identity, 
			ZonedDateTime exp, 
			ZonedDateTime iat, 
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
