package eu.bcvsolutions.idm.core.security;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorizationPolicyRepository;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Test for authorization policies
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmAuthorizationPolicyServiceUnitTest extends AbstractUnitTest {

	@Mock private IdmAuthorizationPolicyRepository repository;
	@Mock private IdmRoleService roleService;
	@Mock private ModuleService moduleService;
	@Mock private EntityEventManager eventManager;
	//
	private IdmAuthorizationPolicyService service;
	private IdmRoleDto DEFAULT_ROLE;
	
	@Before
	public void init() {
		service = new DefaultIdmAuthorizationPolicyService(repository, roleService, moduleService, eventManager);
		//
		DEFAULT_ROLE = new IdmRoleDto();
	}
	
	@Test
	public void testDefaultAuthoritiesRoleIsDisabled() {
		DEFAULT_ROLE.setDisabled(true);
		when(roleService.getDefaultRole()).thenReturn(DEFAULT_ROLE);
		//
		assertTrue(service.getDefaultAuthorities(null).isEmpty());
	}
	
	@Test
	public void testDefaultAuthoritiesRoleNotFound() {
		when(roleService.getDefaultRole()).thenReturn(null);
		//
		assertTrue(service.getDefaultAuthorities(null).isEmpty());
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
