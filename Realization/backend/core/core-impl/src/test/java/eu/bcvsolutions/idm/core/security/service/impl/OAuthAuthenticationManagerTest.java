package eu.bcvsolutions.idm.core.security.service.impl;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.activiti.engine.IdentityService;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorityChange;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorityChangeRepository;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Tests authentication manager for internal Idm authentication scheme.
 * @author Jan Helbich
 *
 */
public class OAuthAuthenticationManagerTest extends AbstractUnitTest {
	
	public static final String USER_NAME = "test_user";

	@Mock
	private IdmIdentityService identityService;
	
	@Mock
	private IdentityService workflowIdentityService;
	
	@Mock
	private SecurityService securityService;
	
	@Mock
	private IdmAuthorityChangeRepository acRepository;
	
	private OAuthAuthenticationManager authManager;
	
	
	@Before
	public void init() {
		authManager = new OAuthAuthenticationManager(identityService,
				workflowIdentityService, securityService, acRepository);
	}
	
	/**
	 * Successful authentication
	 */
	@Test
	public void testAuthSuccess() {
		IdmIdentityDto i = getTestIdentity();
		IdmJwtAuthentication authentication = getAuthentication(USER_NAME,
				DateTime.now().plusHours(1), DateTime.now());
		
		when(identityService.getByUsername(USER_NAME)).thenReturn(i);
		doNothing().when(workflowIdentityService).setAuthenticatedUserId(USER_NAME);
		doNothing().when(securityService).setAuthentication(authentication);
		
		Authentication auth = authManager.authenticate(authentication);
		
		Assert.assertEquals(USER_NAME, auth.getName());
		Assert.assertEquals(USER_NAME, auth.getPrincipal());
		Assert.assertTrue(auth.getAuthorities().isEmpty());
		
		verify(identityService).getByUsername(USER_NAME);
		verify(workflowIdentityService).setAuthenticatedUserId(USER_NAME);
		verify(securityService).setAuthentication(authentication);
	}
	
	/**
	 * Non-existent identities cannot possess auth. tokens. 
	 */
	@Test
	public void testIdentityNotExists() {
		IdmJwtAuthentication authentication = getAuthentication(USER_NAME,
				DateTime.now().plusHours(1), DateTime.now());
		when(identityService.getByUsername(USER_NAME)).thenReturn(null);
		try {
			authManager.authenticate(authentication);
			Assert.fail("Cannot authenticate unknown identity.");
		} catch (AuthenticationException e) {
			verify(identityService).getByUsername(USER_NAME);
		}
	}
	
	/**
	 * Expired tokens are not accepted.
	 */
	@Test(expected = ResultCodeException.class)
	public void testAuthExpired() {
		IdmIdentityDto i = getTestIdentity();
		when(identityService.getByUsername(USER_NAME)).thenReturn(i);
		IdmJwtAuthentication authentication = getAuthentication(USER_NAME,
				DateTime.now().minusHours(1), DateTime.now().plusHours(2));
		
		authManager.authenticate(authentication);
		Assert.fail("Cannot authenticate with expired token.");
	}
	
	/**
	 * Removing a role which grants authorities results in authentication
	 * expiration.
	 */
	@Test
	public void testAuthorityModification() {
		IdmIdentityDto i = getTestIdentity();
		IdmAuthorityChange ac = getAuthChange(i, DateTime.now());
		
		IdmJwtAuthentication authentication = getAuthentication(USER_NAME,
				DateTime.now().plusHours(1), DateTime.now().minusHours(1));
		when(identityService.getByUsername(USER_NAME)).thenReturn(i);
		when(acRepository.findOneByIdentity_Id(i.getId())).thenReturn(ac);
		
		try {
			authManager.authenticate(authentication);
			Assert.fail("Cannot authenticate identity with modified authorities.");
		} catch (ResultCodeException e) {
			Assert.assertEquals(CoreResultCode.AUTHORITIES_CHANGED.getStatus(), e.getStatus());
			Assert.assertEquals(CoreResultCode.AUTHORITIES_CHANGED.getMessage(), e.getMessage());
			verify(identityService).getByUsername(USER_NAME);
			verify(acRepository).findOneByIdentity_Id(i.getId());
		}
	}

	
	private IdmIdentityDto getTestIdentity() {
		IdmIdentityDto i = new IdmIdentityDto();
		i.setUsername(USER_NAME);
		return i;
	}
	
	private IdmAuthorityChange getAuthChange(IdmIdentityDto i, DateTime dt) {
		IdmAuthorityChange c = new IdmAuthorityChange();
		c.setIdentity(new IdmIdentity(i.getId()));
		c.setAuthChangeTimestamp(dt);
		return c;
	}
	
	private IdmJwtAuthentication getAuthentication(String username, DateTime exp, DateTime iat) {
		return getAuthentication(username, exp, iat, new ArrayList<>());
	}

	private IdmJwtAuthentication getAuthentication(String username, DateTime exp,
			DateTime iat, Collection<GrantedAuthority> authorities) {
		return new IdmJwtAuthentication(
				new IdmIdentityDto(username),
				new IdmIdentityDto(username),
				exp, iat, authorities, "testmodule");
	}

}
