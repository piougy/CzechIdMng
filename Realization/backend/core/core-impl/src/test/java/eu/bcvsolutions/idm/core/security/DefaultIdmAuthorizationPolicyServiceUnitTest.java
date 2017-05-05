package eu.bcvsolutions.idm.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorizationPolicyRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Test for authorization policies
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmAuthorizationPolicyServiceUnitTest extends AbstractUnitTest {

	@Mock
	private IdmAuthorizationPolicyRepository repository;
	@Mock
	private IdmRoleService roleService;
	@Mock
	private ModuleService moduleService;
	@Mock
	private EntityEventManager eventManager;
	//
	private IdmAuthorizationPolicyService service;
	private IdmRole DEFAULT_ROLE;
	
	@Before
	public void init() {		
		service = new DefaultIdmAuthorizationPolicyService(repository, roleService, moduleService, eventManager);
		service.setModelMapper(new ModelMapper());
		//
		DEFAULT_ROLE = new IdmRole();
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
		when(service.find(any(), any())).thenReturn(policies);
		//
		List<UUID> defaultPolicies = service.getDefaultPolicies(Identifiable.class).stream().map(IdmAuthorizationPolicyDto::getId).collect(Collectors.toList());
		assertEquals(2, defaultPolicies.size());
		assertTrue(defaultPolicies.containsAll(policies.getContent().stream().map(IdmAuthorizationPolicy::getId).collect(Collectors.toList())));
	}
	
	@Test
	public void testDefaultPoliciesRoleIsDisabled() {
		DEFAULT_ROLE.setDisabled(true);
		when(roleService.getDefaultRole()).thenReturn(DEFAULT_ROLE);
		//
		assertTrue(service.getDefaultPolicies(Identifiable.class).isEmpty());
	}
	
	@Test
	public void testDefaultPoliciesRoleNotFound() {
		when(roleService.getDefaultRole()).thenReturn(null);
		//
		assertTrue(service.getDefaultPolicies(Identifiable.class).isEmpty());
	}
}
