package eu.bcvsolutions.idm.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.core.GrantedAuthority;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.model.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleAuthority;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleComposition;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.security.api.service.SecurityService;
import eu.bcvsolutions.idm.security.service.impl.DefaultGrantedAuthoritiesFactory;
import eu.bcvsolutions.idm.security.service.impl.DefaultSecurityService;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Test for {@link DefaultGrantedAuthoritiesFactory}
 * 
 * @author Radek Tomiška 
 *
 */
public class DefaultGrantedAuthoritiesFactoryTest extends AbstractUnitTest {
	
	private static final IdmRoleAuthority SUB_ROLE_AUTHORITY;
	private static final IdmIdentity TEST_IDENTITY;
	private static final List<IdmIdentityRole> IDENTITY_ROLES;
	private static final List<GroupPermission> groupPermissions = new ArrayList<>();
	
	@Mock
	private IdmIdentityService identityService;	
	@Mock
	private IdmIdentityRoleService identityRoleService;
	@Mock
	private SecurityService securityService;
	
	private DefaultGrantedAuthoritiesFactory defaultGrantedAuthoritiesFactory;
	
	static {
		// prepare roles and authorities
		IdmRole subRole = new IdmRole();
		subRole.setName("sub_role");
		IdmRoleAuthority subRoleAuthority = new IdmRoleAuthority();
		subRoleAuthority.setActionPermission(IdmBasePermission.DELETE);
		subRoleAuthority.setTargetPermission(IdmGroupPermission.IDENTITY);
		SUB_ROLE_AUTHORITY = subRoleAuthority;
		subRole.getAuthorities().add(subRoleAuthority);
		IdmRole superiorRole = new IdmRole();
		superiorRole.setName("superior_role");
		IdmRoleAuthority superiorRoleAuthority = new IdmRoleAuthority();
		superiorRoleAuthority.setActionPermission(IdmBasePermission.DELETE);
		superiorRoleAuthority.setTargetPermission(IdmGroupPermission.IDENTITY);
		superiorRole.getAuthorities().add(superiorRoleAuthority);
		superiorRole.getSubRoles().add(new IdmRoleComposition(superiorRole, subRole));
		
		// prepare identity		
		IdmIdentity identity = new IdmIdentity();
		identity.setUsername("username");
		IdmIdentityRole identityRole = new IdmIdentityRole();
		identityRole.setIdentity(identity);
		identityRole.setRole(superiorRole);
		IDENTITY_ROLES = new ArrayList<>();
		IDENTITY_ROLES.add(identityRole);
		
		TEST_IDENTITY = identity;
		
		groupPermissions.addAll(Arrays.asList(IdmGroupPermission.values()));
	}
	
	@Before
	public void init() {
		defaultGrantedAuthoritiesFactory = new DefaultGrantedAuthoritiesFactory(
				identityService, 
				identityRoleService, 
				securityService);
	}
	
	@Test
	public void testRoleComposition() {	
		when(identityService.getByUsername(TEST_IDENTITY.getUsername())).thenReturn(TEST_IDENTITY);
		when(identityRoleService.getRoles(TEST_IDENTITY)).thenReturn(IDENTITY_ROLES);
		
		List<GrantedAuthority> grantedAuthorities =  defaultGrantedAuthoritiesFactory.getGrantedAuthorities(TEST_IDENTITY.getUsername());
		
		assertEquals(SUB_ROLE_AUTHORITY.getAuthority(), grantedAuthorities.get(0).getAuthority());
		
		verify(identityService).getByUsername(TEST_IDENTITY.getUsername());
		verify(identityRoleService).getRoles(TEST_IDENTITY);
	}
	
	@Test
	public void testUniqueAuthorities() {
		when(identityService.getByUsername(TEST_IDENTITY.getUsername())).thenReturn(TEST_IDENTITY);
		when(identityRoleService.getRoles(TEST_IDENTITY)).thenReturn(IDENTITY_ROLES);
		
		List<GrantedAuthority> grantedAuthorities =  defaultGrantedAuthoritiesFactory.getGrantedAuthorities(TEST_IDENTITY.getUsername());
		
		assertEquals(1, grantedAuthorities.size());
		
		verify(identityService).getByUsername(TEST_IDENTITY.getUsername());
		verify(identityRoleService).getRoles(TEST_IDENTITY);
	}
	
	/**
	 * System admin have all authorities
	 */
	@Test
	public void testSystemAdmin() {
		IdmRole role = new IdmRole();
		role.setName("role");
		IdmRoleAuthority roleAuthority = new IdmRoleAuthority();
		roleAuthority.setTargetPermission(IdmGroupPermission.APP);
		roleAuthority.setActionPermission(IdmBasePermission.ADMIN);
		role.getAuthorities().add(roleAuthority);
		
		IdmIdentity identity = new IdmIdentity();
		identity.setUsername("admin");
		IdmIdentityRole identityRole = new IdmIdentityRole();
		identityRole.setIdentity(identity);
		identityRole.setRole(role);
		List<IdmIdentityRole> roles = Lists.newArrayList(identityRole);
		
		when(securityService.getAllAvailableAuthorities()).thenReturn(DefaultSecurityService.toAuthorities(groupPermissions));
		when(identityService.getByUsername(identity.getUsername())).thenReturn(identity);
		when(identityRoleService.getRoles(identity)).thenReturn(roles);
		
		List<GrantedAuthority> grantedAuthorities = defaultGrantedAuthoritiesFactory.getGrantedAuthorities(identity.getUsername());
		
		assertTrue(grantedAuthorities.containsAll(DefaultSecurityService.toAuthorities(groupPermissions)));
		
		verify(securityService).getAllAvailableAuthorities();
		verify(identityService).getByUsername(identity.getUsername());
		verify(identityRoleService).getRoles(identity);
	}
	
	/**
	 * Group admin has all group authorities
	 */
	@Test
	public void testGroupAdmin() {
		IdmRole role = new IdmRole();
		role.setName("role");
		IdmRoleAuthority roleAuthority = new IdmRoleAuthority();
		roleAuthority.setTargetPermission(IdmGroupPermission.IDENTITY);
		roleAuthority.setActionPermission(IdmBasePermission.ADMIN);
		role.getAuthorities().add(roleAuthority);
		
		IdmIdentity identity = new IdmIdentity();
		identity.setUsername("identityAdmin");
		IdmIdentityRole identityRole = new IdmIdentityRole();
		identityRole.setIdentity(identity);
		identityRole.setRole(role);
		List<IdmIdentityRole> roles = Lists.newArrayList(identityRole);
		
		when(securityService.getAvailableGroupPermissions()).thenReturn(groupPermissions);
		when(identityService.getByUsername(identity.getUsername())).thenReturn(identity);
		when(identityRoleService.getRoles(identity)).thenReturn(roles);
		
		List<GrantedAuthority> grantedAuthorities = defaultGrantedAuthoritiesFactory.getGrantedAuthorities(identity.getUsername());
			
		assertTrue(grantedAuthorities.containsAll(DefaultSecurityService.toAuthorities(IdmGroupPermission.IDENTITY)));
		assertEquals(IdmGroupPermission.IDENTITY.getPermissions().size(), grantedAuthorities.size());
		
		verify(securityService).getAvailableGroupPermissions();
		verify(identityService).getByUsername(identity.getUsername());
		verify(identityRoleService).getRoles(identity);
	}
}
