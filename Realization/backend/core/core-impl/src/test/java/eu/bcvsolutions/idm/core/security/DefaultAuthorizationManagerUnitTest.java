package eu.bcvsolutions.idm.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultAuthorizationManager;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Test for authorities evaluation
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultAuthorizationManagerUnitTest extends AbstractUnitTest {

	@Mock
	private ApplicationContext context;
	@Mock
	private IdmAuthorizationPolicyService service;
	@Mock
	private SecurityService securityService;
	@Mock
	private ModuleService moduleService;
	//
	private AuthorizationManager manager;
	private List<IdmAuthorizationPolicyDto> enabledPolicies;
	private final BasePermissionEvaluator evaluator = new BasePermissionEvaluator();
	
	@Before
	public void init() {		
		manager = new DefaultAuthorizationManager(context, service, securityService, moduleService);
		//
		enabledPolicies = new ArrayList<>();
		//
		IdmAuthorizationPolicyDto policyOne = new  IdmAuthorizationPolicyDto();
		policyOne.setPermissions(IdmBasePermission.READ);
		policyOne.setEvaluator(evaluator.getClass());
		enabledPolicies.add(policyOne);
		//
		IdmAuthorizationPolicyDto policyTwo = new  IdmAuthorizationPolicyDto();
		policyTwo.setPermissions(IdmBasePermission.UPDATE);
		policyTwo.setEvaluator(evaluator.getClass());
		enabledPolicies.add(policyTwo);
	}
	
	@Test
	public void testGetPermissions() {
		when(service.getEnabledPolicies(any(), any())).thenReturn(enabledPolicies);
		when(securityService.isAuthenticated()).thenReturn(true);
		when(context.getBean(BasePermissionEvaluator.class)).thenReturn(evaluator);
		//
		Set<String> basePermissions = manager.getPermissions(new IdmRole());
		assertEquals(2, basePermissions.size());
		assertTrue(basePermissions.contains(IdmBasePermission.READ.getName()));
		assertTrue(basePermissions.contains(IdmBasePermission.UPDATE.getName()));
	}
	
	@Test
	public void testEvaluate() {
		when(service.getEnabledPolicies(any(), any())).thenReturn(enabledPolicies);
		when(securityService.isAuthenticated()).thenReturn(true);
		when(context.getBean(BasePermissionEvaluator.class)).thenReturn(evaluator);
		//
		assertTrue(manager.evaluate(new IdmRole(), IdmBasePermission.READ));
		assertTrue(manager.evaluate(new IdmRole(), IdmBasePermission.UPDATE));
		assertFalse(manager.evaluate(new IdmRole(), IdmBasePermission.ADMIN));
		assertFalse(manager.evaluate(new IdmRole(), IdmBasePermission.AUTOCOMPLETE));
	}
}
