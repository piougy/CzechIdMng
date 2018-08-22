package eu.bcvsolutions.idm.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.GrantedAuthority;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
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
public class DefaultGrantedAuthoritiesFactoryUnitTest extends AbstractUnitTest {
	
	private static final IdmIdentityDto TEST_IDENTITY;
	private static final IdmRoleDto TEST_ROLE;
	private static final List<IdmIdentityRoleDto> IDENTITY_ROLES;
	private static final List<GroupPermission> groupPermissions = new ArrayList<>();
	private static final Set<GrantedAuthority> DEFAULT_AUTHORITIES = Sets.newHashSet(
			new DefaultGrantedAuthority(CoreGroupPermission.ROLE_CREATE), 
			new DefaultGrantedAuthority(CoreGroupPermission.ROLE_CREATE), 
			new DefaultGrantedAuthority(CoreGroupPermission.ROLE_CREATE), 
			new DefaultGrantedAuthority(CoreGroupPermission.ROLE_DELETE),
			new DefaultGrantedAuthority(CoreGroupPermission.ROLE_DELETE));
	
	@Mock private IdmIdentityService identityService;	
	@Mock private IdmIdentityRoleService identityRoleService;
	@Mock private SecurityService securityService;
	@Mock private ModuleService moduleService;
	@Mock private IdmAuthorizationPolicyService authorizationPolicyService;
	@Mock private IdmRoleService roleService;
	//
	@InjectMocks
	private DefaultGrantedAuthoritiesFactory defaultGrantedAuthoritiesFactory;
	
	
	static {
		// prepare roles and authorities
		TEST_ROLE = new IdmRoleDto();
		TEST_ROLE.setId(UUID.randomUUID());
		TEST_ROLE.setCode("superior_role");
		
		// prepare identity		
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setId(UUID.randomUUID());
		identity.setUsername("username");
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setId(UUID.randomUUID());
		contract.setIdentity(identity.getId());
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		identityRole.setIdentityContractDto(contract);
		identityRole.setRole(TEST_ROLE.getId());
		IDENTITY_ROLES = new ArrayList<>();
		IDENTITY_ROLES.add(identityRole);
		
		TEST_IDENTITY = identity;
		
		groupPermissions.addAll(Arrays.asList(CoreGroupPermission.values()));
	}
	
	@Test
	public void testUniqueAuthorities() {
		IdmRoleDto role = new IdmRoleDto();
		role.setCode("role");
		role.setId(UUID.randomUUID());
		//
		when(identityService.getByUsername(TEST_IDENTITY.getUsername())).thenReturn(TEST_IDENTITY);
		when(identityRoleService.findValidRoles(TEST_IDENTITY.getId(), null)).thenReturn(new PageImpl<>(new ArrayList<>(IDENTITY_ROLES)));
		when(roleService.get(TEST_ROLE.getId())).thenReturn(TEST_ROLE);
		when(authorizationPolicyService.getDefaultAuthorities(any())).thenReturn(DEFAULT_AUTHORITIES);
		
		List<GrantedAuthority> grantedAuthorities =  defaultGrantedAuthoritiesFactory.getGrantedAuthorities(TEST_IDENTITY.getUsername());
		
		assertEquals(2, grantedAuthorities.size());
	}
	
	/**
	 * System admin have all authorities
	 */
	@Test
	public void testSystemAdmin() {
		IdmRole role = new IdmRole();
		role.setCode("role");
		role.setId(UUID.randomUUID());
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setId(UUID.randomUUID());
		identity.setUsername("admin");
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		role.setId(UUID.randomUUID());
		contract.setIdentity(identity.getId());
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		identityRole.setIdentityContract(contract.getId());
		identityRole.setRole(role.getId());
		List<IdmIdentityRoleDto> roles = Lists.newArrayList();
		
		when(moduleService.getAvailablePermissions()).thenReturn(groupPermissions);
		when(identityService.getByUsername(identity.getUsername())).thenReturn(identity);
		when(identityRoleService.findValidRoles(identity.getId(), null)).thenReturn(new PageImpl<>(new ArrayList<>(roles)));
		when(authorizationPolicyService.getDefaultAuthorities(any())).thenReturn(Sets.newHashSet(
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
		IdmRoleDto role = new IdmRoleDto();
		role.setCode("role");
		role.setId(UUID.randomUUID());
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setId(UUID.randomUUID());
		identity.setUsername("identityAdmin");
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setId(UUID.randomUUID());
		contract.setIdentity(identity.getId());
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		identityRole.setIdentityContractDto(contract);
		identityRole.setRole(role.getId());
		List<IdmIdentityRoleDto> roles = Lists.newArrayList(identityRole);
		
		when(moduleService.getAvailablePermissions()).thenReturn(groupPermissions);
		when(identityService.getByUsername(identity.getUsername())).thenReturn(identity);
		when(roleService.get(role.getId())).thenReturn(role);
		when(identityRoleService.findValidRoles(identity.getId(), null)).thenReturn(new PageImpl<>(new ArrayList<>(roles)));
		when(authorizationPolicyService.getDefaultAuthorities(any())).thenReturn(Sets.newHashSet(
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
		when(identityRoleService.findAllByIdentity(TEST_IDENTITY.getId())).thenReturn(new ArrayList<>());
		when(identityRoleService.findValidRoles(TEST_IDENTITY.getId(), null)).thenReturn(new PageImpl<>(new ArrayList<>()));
		when(authorizationPolicyService.getDefaultAuthorities(any())).thenReturn(DEFAULT_AUTHORITIES);
		List<GrantedAuthority> grantedAuthorities =  defaultGrantedAuthoritiesFactory.getGrantedAuthorities(TEST_IDENTITY.getUsername());
		
		assertEquals(2, grantedAuthorities.size());
		assertTrue(grantedAuthorities.containsAll(DEFAULT_AUTHORITIES));
	}
}
