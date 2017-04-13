package eu.bcvsolutions.idm.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.core.GrantedAuthority;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleComposition;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.DefaultGrantedAuthority;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultGrantedAuthoritiesFactory;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Test for {@link DefaultGrantedAuthoritiesFactory}
 * 
 * @author Radek Tomiška 
 *
 */
public class DefaultGrantedAuthoritiesFactoryTest extends AbstractUnitTest {
	
	private static final IdmIdentity TEST_IDENTITY;
	private static final List<IdmIdentityRole> IDENTITY_ROLES;
	private static final List<GroupPermission> groupPermissions = new ArrayList<>();
	private static final Set<GrantedAuthority> DEFAULT_AUTHORITIES = Sets.newHashSet(
			new DefaultGrantedAuthority(CoreGroupPermission.ROLE_CREATE), 
			new DefaultGrantedAuthority(CoreGroupPermission.ROLE_CREATE), 
			new DefaultGrantedAuthority(CoreGroupPermission.ROLE_CREATE), 
			new DefaultGrantedAuthority(CoreGroupPermission.ROLE_DELETE),
			new DefaultGrantedAuthority(CoreGroupPermission.ROLE_DELETE));
	
	@Mock
	private IdmIdentityService identityService;	
	@Mock
	private IdmIdentityRoleService identityRoleService;
	@Mock
	private SecurityService securityService;
	@Mock
	private ModuleService moduleService;
	@Mock
	private IdmAuthorizationPolicyService authorizationPolicyService;
	//
	private DefaultGrantedAuthoritiesFactory defaultGrantedAuthoritiesFactory;
	
	
	static {
		// prepare roles and authorities
		IdmRole subRole = new IdmRole();
		subRole.setName("sub_role");
		IdmRole superiorRole = new IdmRole();
		superiorRole.setName("superior_role");
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
				authorizationPolicyService);
	}
	
	// TODO: enable after subroles rewrite
//	@Test
//	public void testRoleComposition() {	
//		when(identityService.getByUsername(TEST_IDENTITY.getUsername())).thenReturn(TEST_IDENTITY);
//		when(identityRoleService.getRoles(TEST_IDENTITY)).thenReturn(IDENTITY_ROLES);
//		when(authorizationPolicyService.getDefaultAuthorities()).thenReturn(new HashSet<GrantedAuthority>());
//		
//		List<GrantedAuthority> grantedAuthorities =  defaultGrantedAuthoritiesFactory.getGrantedAuthorities(TEST_IDENTITY.getUsername());
//		
//		assertEquals(SUB_ROLE_AUTHORITY.getAuthority(), grantedAuthorities.get(0).getAuthority());
//		
//	}
	
	@Test
	public void testUniqueAuthorities() {
		when(identityService.getByUsername(TEST_IDENTITY.getUsername())).thenReturn(TEST_IDENTITY);
		when(identityRoleService.getRoles(TEST_IDENTITY)).thenReturn(IDENTITY_ROLES);
		when(authorizationPolicyService.getDefaultAuthorities()).thenReturn(DEFAULT_AUTHORITIES);
		
		List<GrantedAuthority> grantedAuthorities =  defaultGrantedAuthoritiesFactory.getGrantedAuthorities(TEST_IDENTITY.getUsername());
		
		assertEquals(2, grantedAuthorities.size());
	}
	
	/**
	 * System admin have all authorities
	 */
	@Test
	public void testSystemAdmin() {
		IdmRole role = new IdmRole();
		role.setName("role");		
		IdmIdentity identity = new IdmIdentity();
		identity.setUsername("admin");
		IdmIdentityContract contract = new IdmIdentityContract();
		contract.setIdentity(identity);
		IdmIdentityRole identityRole = new IdmIdentityRole();
		identityRole.setIdentityContract(contract);
		identityRole.setRole(role);
		List<IdmIdentityRole> roles = Lists.newArrayList(identityRole);
		
		when(moduleService.getAvailablePermissions()).thenReturn(groupPermissions);
		when(identityService.getByUsername(identity.getUsername())).thenReturn(identity);
		when(identityRoleService.getRoles(identity)).thenReturn(roles);
		when(authorizationPolicyService.getDefaultAuthorities()).thenReturn(Sets.newHashSet(
				new DefaultGrantedAuthority(IdmGroupPermission.APP, IdmBasePermission.ADMIN),
				new DefaultGrantedAuthority(CoreGroupPermission.IDENTITY, IdmBasePermission.READ),
				new DefaultGrantedAuthority(CoreGroupPermission.IDENTITY, IdmBasePermission.ADMIN)
				));
		
		List<GrantedAuthority> grantedAuthorities = defaultGrantedAuthoritiesFactory.getGrantedAuthorities(identity.getUsername());
		
		assertEquals(1, grantedAuthorities.size());
		assertEquals(new DefaultGrantedAuthority(IdmGroupPermission.APP, IdmBasePermission.ADMIN), grantedAuthorities.iterator().next());
	}
	
	/**
	 * Group admin has all group authorities
	 */
	@Test
	public void testGroupAdmin() {
		IdmRole role = new IdmRole();
		role.setName("role");
		
		IdmIdentity identity = new IdmIdentity();
		identity.setUsername("identityAdmin");
		IdmIdentityContract contract = new IdmIdentityContract();
		contract.setIdentity(identity);
		IdmIdentityRole identityRole = new IdmIdentityRole();
		identityRole.setIdentityContract(contract);
		identityRole.setRole(role);
		List<IdmIdentityRole> roles = Lists.newArrayList(identityRole);
		
		when(moduleService.getAvailablePermissions()).thenReturn(groupPermissions);
		when(identityService.getByUsername(identity.getUsername())).thenReturn(identity);
		when(identityRoleService.getRoles(identity)).thenReturn(roles);
		when(authorizationPolicyService.getDefaultAuthorities()).thenReturn(Sets.newHashSet(
				new DefaultGrantedAuthority(CoreGroupPermission.IDENTITY, IdmBasePermission.ADMIN),
				new DefaultGrantedAuthority(CoreGroupPermission.IDENTITY, IdmBasePermission.READ),
				new DefaultGrantedAuthority(CoreGroupPermission.IDENTITY, IdmBasePermission.DELETE)
				));
		// returns trimmed authorities
		List<GrantedAuthority> grantedAuthorities = defaultGrantedAuthoritiesFactory.getGrantedAuthorities(identity.getUsername());
		//
		assertEquals(1, grantedAuthorities.size());
		assertEquals(new DefaultGrantedAuthority(CoreGroupPermission.IDENTITY, IdmBasePermission.ADMIN), grantedAuthorities.iterator().next());
	}
	
	@Test
	public void testDefaultRoleAutorities() {
		when(identityService.getByUsername(TEST_IDENTITY.getUsername())).thenReturn(TEST_IDENTITY);
		when(identityRoleService.getRoles(TEST_IDENTITY)).thenReturn(new ArrayList<>());
		when(authorizationPolicyService.getDefaultAuthorities()).thenReturn(DEFAULT_AUTHORITIES);
		
		List<GrantedAuthority> grantedAuthorities =  defaultGrantedAuthoritiesFactory.getGrantedAuthorities(TEST_IDENTITY.getUsername());
		
		assertEquals(2, grantedAuthorities.size());
		assertTrue(grantedAuthorities.containsAll(DEFAULT_AUTHORITIES));
	}
}
