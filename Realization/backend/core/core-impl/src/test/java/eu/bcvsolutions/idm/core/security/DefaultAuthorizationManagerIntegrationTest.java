package eu.bcvsolutions.idm.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.TestHelper;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizationEvaluatorDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.service.LoginService;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultAuthorizationManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for authorities evaluation
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultAuthorizationManagerIntegrationTest extends AbstractIntegrationTest {

	@Autowired 
	protected TestHelper helper;
	@Autowired
	private ApplicationContext context;
	@Autowired
	private IdmAuthorizationPolicyService service;
	@Autowired
	private SecurityService securityService;
	@Autowired
	private LoginService loginService;
	@Autowired
	private IdmRoleService roleService;
	//
	private AuthorizationManager manager;
	
	@Before
	public void init() {		
		manager = new DefaultAuthorizationManager(context, service, securityService);
	}
	
	@Test
	public void testAuthorizableTypes() {
		List<AuthorizableType> authorizableTypes = manager.getAuthorizableTypes();
		//
		AuthorizableType role = authorizableTypes.stream()
				.filter(a -> {
					return a.getType().equals(IdmRole.class);
				})
				.findFirst()
	            .get();
		assertNotNull(role);
	}
	
	@Test
	public void testSupportedEvaluators() {
		List<AuthorizationEvaluatorDto> dtos = manager.getSupportedEvaluators();
		//
		assertTrue(dtos.size() > 1); // TODO: improve (check uuid and base evaluator at least)
	}
	
	@Test
	public void testEvaluate() {
		loginAsAdmin(InitTestData.TEST_USER_1);
		// prepare role
		IdmRole role = helper.createRole();
		helper.createBasePolicy(role.getId(), IdmBasePermission.READ);		
		// prepare identity
		IdmIdentity identity = helper.createIdentity();
		// assign role
		helper.createIdentityRole(identity, role);
		logout();
		//
		// without login
		assertFalse(manager.evaluate(role, IdmBasePermission.READ));
		assertFalse(manager.evaluate(role, IdmBasePermission.UPDATE));
		assertFalse(manager.evaluate(role, IdmBasePermission.ADMIN));
		assertFalse(manager.evaluate(role, IdmBasePermission.AUTOCOMPLETE));
		//
		try {
			
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			// evaluate	access
			assertTrue(manager.evaluate(role, IdmBasePermission.READ));
			assertFalse(manager.evaluate(role, IdmBasePermission.UPDATE));
			assertFalse(manager.evaluate(role, IdmBasePermission.ADMIN));
			assertFalse(manager.evaluate(role, IdmBasePermission.AUTOCOMPLETE));			
		} finally {
			logout();
		}
	}
	
	@Test
	public void testPredicate() {
		loginAsAdmin(InitTestData.TEST_USER_1);
		// prepare role
		IdmRole role = helper.createRole();
		helper.createUuidPolicy(role.getId(), role.getId(), IdmBasePermission.READ);		
		helper.createBasePolicy(role.getId(), IdmBasePermission.AUTOCOMPLETE);	
		// prepare identity
		IdmIdentity identity = helper.createIdentity();
		// assign role
		helper.createIdentityRole(identity, role);
		logout();
		//
		// empty without login
		RoleFilter filter = new RoleFilter();
		assertEquals(0, roleService.findSecured(filter, IdmBasePermission.READ, null).getTotalElements());
		assertEquals(0, roleService.findSecured(filter, IdmBasePermission.AUTOCOMPLETE, null).getTotalElements());
		//
		try {			
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			// evaluate	access
			assertEquals(1, roleService.findSecured(filter, IdmBasePermission.READ, null).getTotalElements());
			assertEquals(roleService.find(null).getTotalElements(), 
					roleService.findSecured(filter, IdmBasePermission.AUTOCOMPLETE, null).getTotalElements());			
		} finally {
			logout();
		}
	}
	
	
}
