package eu.bcvsolutions.idm.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleAuthority;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorizationPolicyRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Test for authorization policies
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmAuthorizationServiceUnitTest extends AbstractUnitTest {

	@Mock
	private IdmAuthorizationPolicyRepository repository;
	@Mock
	private IdmRoleService roleService;
	//
	private IdmAuthorizationPolicyService service;
	private IdmRole DEFAULT_ROLE;
	
	@Before
	public void init() {		
		service = new DefaultIdmAuthorizationPolicyService(repository, roleService);
		service.setModelMapper(new ModelMapper());
		//
		DEFAULT_ROLE = new IdmRole();
		IdmRoleAuthority a1 = new IdmRoleAuthority();
		a1.setTargetPermission(CoreGroupPermission.ROLE);
		a1.setActionPermission(IdmBasePermission.READ);
		DEFAULT_ROLE.getAuthorities().add(a1);
		IdmRoleAuthority a2 = new IdmRoleAuthority();
		a2.setTargetPermission(CoreGroupPermission.ROLE);
		a2.setActionPermission(IdmBasePermission.CREATE);
		DEFAULT_ROLE.getAuthorities().add(a2);
	}
	
	
	@Test
	public void testDefaultAuthorities() {
		when(roleService.getDefaultRole()).thenReturn(DEFAULT_ROLE);
		//
		Set<String> defaultAuthorities = service.getDefaultAuthorities();
		assertEquals(2, defaultAuthorities.size());
		assertTrue(defaultAuthorities.containsAll(DEFAULT_ROLE.getAuthorities().stream().map(a -> { return a.getAuthority(); }).collect(Collectors.toList())));
	}
	
	@Test
	public void testDefaultAuthoritiesRoleIsDisabled() {
		DEFAULT_ROLE.setDisabled(true);
		when(roleService.getDefaultRole()).thenReturn(DEFAULT_ROLE);
		//
		assertTrue(service.getDefaultAuthorities().isEmpty());
	}
	
	@Test
	public void testDefaultAuthoritiesRoleNotFound() {
		when(roleService.getDefaultRole()).thenReturn(null);
		//
		assertTrue(service.getDefaultAuthorities().isEmpty());
	}
	
	@Test
	public void testDefaultPolicies() {
		when(roleService.getDefaultRole()).thenReturn(DEFAULT_ROLE);
		Page<IdmAuthorizationPolicy> policies = new PageImpl<>(Lists.newArrayList(new IdmAuthorizationPolicy(UUID.randomUUID()), new IdmAuthorizationPolicy(UUID.randomUUID())));
		when(repository.find(any(), any())).thenReturn(policies);
		//
		List<UUID> defaultPolicies = service.getDefaultPolicies().stream().map(IdmAuthorizationPolicyDto::getId).collect(Collectors.toList());
		assertEquals(2, defaultPolicies.size());
		assertTrue(defaultPolicies.containsAll(policies.getContent().stream().map(IdmAuthorizationPolicy::getId).collect(Collectors.toList())));
	}
	
	@Test
	public void testDefaultPoliciesRoleIsDisabled() {
		DEFAULT_ROLE.setDisabled(true);
		when(roleService.getDefaultRole()).thenReturn(DEFAULT_ROLE);
		//
		assertTrue(service.getDefaultPolicies().isEmpty());
	}
	
	@Test
	public void testDefaultPoliciesRoleNotFound() {
		when(roleService.getDefaultRole()).thenReturn(null);
		//
		assertTrue(service.getDefaultPolicies().isEmpty());
	}
}
