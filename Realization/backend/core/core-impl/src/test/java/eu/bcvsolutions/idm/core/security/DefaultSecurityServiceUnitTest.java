package eu.bcvsolutions.idm.core.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.security.api.domain.AbstractAuthentication;
import eu.bcvsolutions.idm.core.security.api.domain.DefaultGrantedAuthority;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultSecurityService;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Test for {@link DefaultSecurityService}.
 * 
 * @author Radek Tomiška 
 *
 */
public class DefaultSecurityServiceUnitTest extends AbstractUnitTest {
	
	private static final String CURRENT_USERNAME = "current_username";
	private static final String ORIGINAL_USERNAME = "original_username";
	private static final String TEST_AUTHORITY = "TEST_AUTHORITY";
	private static final Collection<GrantedAuthority> AUTHORITIES = Arrays.asList(new DefaultGrantedAuthority(TEST_AUTHORITY));	
	private static final IdmJwtAuthentication AUTHENTICATION = new IdmJwtAuthentication(
			new IdmIdentityDto(CURRENT_USERNAME), 
			new IdmIdentityDto(ORIGINAL_USERNAME), 
			ZonedDateTime.now(),
			ZonedDateTime.now(),
			AUTHORITIES,
			"test");
	
	@Mock private SecurityContext securityContext;
	@Mock private RoleHierarchy authorityHierarchy; 
	//
	private DefaultSecurityService defaultSecurityService;

	@Before
	public void init() {
		SecurityContextHolder.setContext(securityContext);
		defaultSecurityService = new DefaultSecurityService(authorityHierarchy);
	}
	
	@After
	public void logout() {
		SecurityContextHolder.clearContext();
	}
	
	@Test
	public void testIsLoggedIn() {
		// setup static authentication
		when(securityContext.getAuthentication()).thenReturn(AUTHENTICATION);
		//
		AbstractAuthentication result = defaultSecurityService.getAuthentication();
		//
		assertEquals(result.getCurrentUsername(), AUTHENTICATION.getCurrentUsername());
		assertEquals(result.getOriginalUsername(), AUTHENTICATION.getOriginalUsername());
		assertEquals(result.getAuthorities(), AUTHENTICATION.getAuthorities());
		assertEquals(result.getDetails(), AUTHENTICATION.getDetails());
	}
	
	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testHasTestAuthority() {
		// setup static authentication
		Mockito.when(securityContext.getAuthentication()).thenReturn(AUTHENTICATION);
		Mockito.when(authorityHierarchy.getReachableGrantedAuthorities(any())).thenReturn((Collection) AUTHORITIES);
		//
		Assert.assertTrue(defaultSecurityService.hasAnyAuthority(TEST_AUTHORITY));
		Assert.assertFalse(defaultSecurityService.hasAnyAuthority());
	}
	
	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testHasAllAuthorities() {
		// setup static authentication
		Mockito.when(securityContext.getAuthentication()).thenReturn(new IdmJwtAuthentication(
				new IdmIdentityDto(CURRENT_USERNAME), 
				new IdmIdentityDto(ORIGINAL_USERNAME), 
				ZonedDateTime.now(),
				ZonedDateTime.now(),
				(Collection) Arrays.asList(
						new DefaultGrantedAuthority("one"),
						new DefaultGrantedAuthority("two"),
						new DefaultGrantedAuthority("three")
				),
				"test"));
		Mockito.when(authorityHierarchy.getReachableGrantedAuthorities(any())).thenReturn(
				(Collection) Arrays.asList(
					new DefaultGrantedAuthority("one"),
					new DefaultGrantedAuthority("two"),
					new DefaultGrantedAuthority("three")
		));
		//
		Assert.assertTrue(defaultSecurityService.hasAllAuthorities("one"));
		Assert.assertTrue(defaultSecurityService.hasAllAuthorities("one", "two"));
		Assert.assertTrue(defaultSecurityService.hasAllAuthorities("one", "two", "three"));
		Assert.assertFalse(defaultSecurityService.hasAllAuthorities());
		Assert.assertFalse(defaultSecurityService.hasAllAuthorities("one", "four"));
		Assert.assertFalse(defaultSecurityService.hasAllAuthorities("four"));
		Assert.assertFalse(defaultSecurityService.hasAllAuthorities("one", "two", "three", "four"));
	}
	
	@Test
	public void testIsSystemAuthority() {
		Assert.assertFalse(defaultSecurityService.isSystemAuthenticated());
		// setup normal authentication
		when(securityContext.getAuthentication()).thenReturn(AUTHENTICATION);
		Assert.assertFalse(defaultSecurityService.isSystemAuthenticated());
		//
		when(securityContext.getAuthentication())
			.thenReturn(
				new IdmJwtAuthentication(new IdmIdentityDto(SecurityService.SYSTEM_NAME), null, null, null)
			);
		//
		Assert.assertTrue(defaultSecurityService.isSystemAuthenticated());
		//
		when(securityContext.getAuthentication())
			.thenReturn(
				new IdmJwtAuthentication(new IdmIdentityDto(UUID.randomUUID(), SecurityService.SYSTEM_NAME), null, null, null)
			);
		//
		Assert.assertFalse(defaultSecurityService.isSystemAuthenticated());
	}
}
