package eu.bcvsolutions.idm.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.core.GrantedAuthority;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleAuthority;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleComposition;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultGrantedAuthoritiesFactory;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultSecurityService;
import eu.bcvsolutions.idm.test.api.AbstractVerifiableUnitTest;

/**
 * Test for {@link DefaultGrantedAuthoritiesFactory}
 * 
 * @author Radek Tomiška 
 *
 */
public class DefaultGrantedAuthoritiesFactoryTest extends AbstractVerifiableUnitTest {
	
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
	@Mock
	private IdmAuthorizationPolicyService authorizationPolicyService;
	//
	private DefaultGrantedAuthoritiesFactory defaultGrantedAuthoritiesFactory;
	
	
	static {
		// prepare roles and authorities
		IdmRole subRole = new IdmRole();
		subRole.setName("sub_role");
		IdmRoleAuthority subRoleAuthority = new IdmRoleAuthority();
		subRoleAuthority.setActionPermission(IdmBasePermission.DELETE);
		subRoleAuthority.setTargetPermission(CoreGroupPermission.IDENTITY);
		SUB_ROLE_AUTHORITY = subRoleAuthority;
		subRole.getAuthorities().add(subRoleAuthority);
		IdmRole superiorRole = new IdmRole();
		superiorRole.setName("superior_role");
		IdmRoleAuthority superiorRoleAuthority = new IdmRoleAuthority();
		superiorRoleAuthority.setActionPermission(IdmBasePermission.DELETE);
		superiorRoleAuthority.setTargetPermission(CoreGroupPermission.IDENTITY);
		superiorRole.getAuthorities().add(superiorRoleAuthority);
		superiorRole.getSubRoles().add(new IdmRoleComposition(superiorRole, subRole));
		
		// prepare identity		
		IdmIdentity identity = new IdmIdentity();
		identity.setUsername("username");
		IdmIdentityContract contract = new IdmIdentityContract();
		contract.setIdentity(identity);
		IdmIdentityRole identityRole = new IdmIdentityRole();
		identityRole.setIdentityContract(contract);
		identityRole.setRole(superiorRole);
		IDENTITY_ROLES = new ArrayList<>();
		IDENTITY_ROLES.add(identityRole);
		
		TEST_IDENTITY = identity;
		
		groupPermissions.addAll(Arrays.asList(CoreGroupPermission.values()));
	}
	
	@Before
	public void init() {
		defaultGrantedAuthoritiesFactory = new DefaultGrantedAuthoritiesFactory(
				identityService, 
				identityRoleService, 
				securityService,
				authorizationPolicyService);
	}
	
	@Test
	public void testRoleComposition() {	
		when(identityService.getByUsername(TEST_IDENTITY.getUsername())).thenReturn(TEST_IDENTITY);
		when(identityRoleService.getRoles(TEST_IDENTITY)).thenReturn(IDENTITY_ROLES);
		when(authorizationPolicyService.getDefaultAuthorities()).thenReturn(new HashSet<String>());
		
		List<GrantedAuthority> grantedAuthorities =  defaultGrantedAuthoritiesFactory.getGrantedAuthorities(TEST_IDENTITY.getUsername());
		
		assertEquals(SUB_ROLE_AUTHORITY.getAuthority(), grantedAuthorities.get(0).getAuthority());
		
		verify(identityService).getByUsername(TEST_IDENTITY.getUsername());
		verify(identityRoleService).getRoles(TEST_IDENTITY);
		verify(authorizationPolicyService).getDefaultAuthorities();
	}
	
	@Test
	public void testUniqueAuthorities() {
		when(identityService.getByUsername(TEST_IDENTITY.getUsername())).thenReturn(TEST_IDENTITY);
		when(identityRoleService.getRoles(TEST_IDENTITY)).thenReturn(IDENTITY_ROLES);
		when(authorizationPolicyService.getDefaultAuthorities()).thenReturn(new HashSet<String>());
		
		List<GrantedAuthority> grantedAuthorities =  defaultGrantedAuthoritiesFactory.getGrantedAuthorities(TEST_IDENTITY.getUsername());
		
		assertEquals(1, grantedAuthorities.size());
		
		verify(identityService).getByUsername(TEST_IDENTITY.getUsername());
		verify(identityRoleService).getRoles(TEST_IDENTITY);
		verify(authorizationPolicyService).getDefaultAuthorities();
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
		IdmIdentityContract contract = new IdmIdentityContract();
		contract.setIdentity(identity);
		IdmIdentityRole identityRole = new IdmIdentityRole();
		identityRole.setIdentityContract(contract);
		identityRole.setRole(role);
		List<IdmIdentityRole> roles = Lists.newArrayList(identityRole);
		
		when(securityService.getAllAvailableAuthorities()).thenReturn(DefaultSecurityService.toAuthorities(groupPermissions));
		when(identityService.getByUsername(identity.getUsername())).thenReturn(identity);
		when(identityRoleService.getRoles(identity)).thenReturn(roles);
		when(authorizationPolicyService.getDefaultAuthorities()).thenReturn(new HashSet<String>());
		
		List<GrantedAuthority> grantedAuthorities = defaultGrantedAuthoritiesFactory.getGrantedAuthorities(identity.getUsername());
		
		assertTrue(grantedAuthorities.containsAll(DefaultSecurityService.toAuthorities(groupPermissions)));
		
		verify(securityService).getAllAvailableAuthorities();
		verify(identityService).getByUsername(identity.getUsername());
		verify(identityRoleService).getRoles(identity);
		verify(authorizationPolicyService).getDefaultAuthorities();
	}
	
	/**
	 * Group admin has all group authorities
	 */
	@Test
	public void testGroupAdmin() {
		IdmRole role = new IdmRole();
		role.setName("role");
		IdmRoleAuthority roleAuthority = new IdmRoleAuthority();
		roleAuthority.setTargetPermission(CoreGroupPermission.IDENTITY);
		roleAuthority.setActionPermission(IdmBasePermission.ADMIN);
		role.getAuthorities().add(roleAuthority);
		
		IdmIdentity identity = new IdmIdentity();
		identity.setUsername("identityAdmin");
		IdmIdentityContract contract = new IdmIdentityContract();
		contract.setIdentity(identity);
		IdmIdentityRole identityRole = new IdmIdentityRole();
		identityRole.setIdentityContract(contract);
		identityRole.setRole(role);
		List<IdmIdentityRole> roles = Lists.newArrayList(identityRole);
		
		when(securityService.getAvailableGroupPermissions()).thenReturn(groupPermissions);
		when(identityService.getByUsername(identity.getUsername())).thenReturn(identity);
		when(identityRoleService.getRoles(identity)).thenReturn(roles);
		when(authorizationPolicyService.getDefaultAuthorities()).thenReturn(new HashSet<String>());
		
		List<GrantedAuthority> grantedAuthorities = defaultGrantedAuthoritiesFactory.getGrantedAuthorities(identity.getUsername());
			
		assertTrue(grantedAuthorities.containsAll(DefaultSecurityService.toAuthorities(CoreGroupPermission.IDENTITY)));
		assertEquals(CoreGroupPermission.IDENTITY.getPermissions().size(), grantedAuthorities.size());
		
		verify(securityService).getAvailableGroupPermissions();
		verify(identityService).getByUsername(identity.getUsername());
		verify(identityRoleService).getRoles(identity);
		verify(authorizationPolicyService).getDefaultAuthorities();
	}
	
	@Test
	public void testDefaultRoleAutorities() {
		Set<String> authorities = Sets.newHashSet(CoreGroupPermission.ROLE_CREATE, CoreGroupPermission.ROLE_DELETE);
		when(identityService.getByUsername(TEST_IDENTITY.getUsername())).thenReturn(TEST_IDENTITY);
		when(identityRoleService.getRoles(TEST_IDENTITY)).thenReturn(new ArrayList<>());
		when(authorizationPolicyService.getDefaultAuthorities()).thenReturn(authorities);
		
		List<GrantedAuthority> grantedAuthorities =  defaultGrantedAuthoritiesFactory.getGrantedAuthorities(TEST_IDENTITY.getUsername());
		
		assertEquals(2, grantedAuthorities.size());
		assertTrue(grantedAuthorities.containsAll(DefaultSecurityService.toAuthorities(authorities)));
		
		verify(identityService).getByUsername(TEST_IDENTITY.getUsername());
		verify(identityRoleService).getRoles(TEST_IDENTITY);
		verify(authorizationPolicyService).getDefaultAuthorities();
	}
}
