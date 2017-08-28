package eu.bcvsolutions.idm.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.TestHelper;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
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

	@Autowired private TestHelper helper;
	@Autowired private ApplicationContext context;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmAuthorizationPolicyService service;
	@Autowired private SecurityService securityService;
	@Autowired private ModuleService moduleService;
	@Autowired private LoginService loginService;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmIdentityContractService identityContractService;
	//
	private AuthorizationManager manager;
	
	@Before
	public void init() {		
		manager = new DefaultAuthorizationManager(context, service, securityService, moduleService);
	}
	
	@Test
	public void testAuthorizableTypes() {
		Set<AuthorizableType> authorizableTypes = manager.getAuthorizableTypes();
		//
		AuthorizableType role = authorizableTypes.stream()
				.filter(a -> {
					return IdmRole.class.equals(a.getType());
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
		IdmRoleDto role = helper.createRole();
		helper.createBasePolicy(role.getId(), IdmBasePermission.READ);		
		// prepare identity
		IdmIdentityDto identity = helper.createIdentity();
		identity.setPassword(new GuardedString("heslo"));
		identityService.save(identity);
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
		IdmRoleDto role = helper.createRole();
		helper.createUuidPolicy(role.getId(), role.getId(), IdmBasePermission.READ);		
		helper.createBasePolicy(role.getId(), IdmBasePermission.AUTOCOMPLETE);	
		// prepare identity
		IdmIdentityDto identity = helper.createIdentity();
		identity.setPassword(new GuardedString("heslo"));
		identityService.save(identity);
		// assign role
		helper.createIdentityRole(identity, role);
		logout();
		//
		// empty without login
		RoleFilter filter = new RoleFilter();
		assertEquals(0, roleService.find(filter, null, IdmBasePermission.READ).getTotalElements());
		assertEquals(0, roleService.find(filter, null, IdmBasePermission.AUTOCOMPLETE).getTotalElements());
		//
		try {			
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			// evaluate	access
			assertEquals(1, roleService.find(filter, null, IdmBasePermission.READ).getTotalElements());
			assertEquals(roleService.find(null).getTotalElements(), 
					roleService.find(filter, null, IdmBasePermission.AUTOCOMPLETE).getTotalElements());			
		} finally {
			logout();
		}
	}
	
	@Test
	public void testFindValidPolicies() {
		try {
			loginAsAdmin(InitTestData.TEST_USER_1);
			// prepare role
			IdmRoleDto role = helper.createRole();
			IdmRoleDto role2 = helper.createRole();
			helper.createUuidPolicy(role.getId(), role.getId(), IdmBasePermission.READ);		
			helper.createBasePolicy(role2.getId(), IdmBasePermission.AUTOCOMPLETE);
			// prepare identity
			IdmIdentityDto identity = helper.createIdentity();
			// assign role
			helper.createIdentityRole(identity, role);
			helper.createIdentityRole(identity, role2);
			//
			assertEquals(2, service.getEnabledPolicies(identity.getId(), IdmRole.class).size());
		} finally {
			logout();
		}
	}
	
	@Test
	@Transactional
	public void testFindValidPoliciesWithInvalidRole() {
		try {
			loginAsAdmin(InitTestData.TEST_USER_1);
			// prepare role
			IdmRoleDto role = helper.createRole();
			IdmRoleDto role2 = helper.createRole();
			role2.setDisabled(true);
			roleService.save(role2);
			helper.createUuidPolicy(role.getId(), role.getId(), IdmBasePermission.READ);		
			helper.createBasePolicy(role2.getId(), IdmBasePermission.AUTOCOMPLETE);	
			// prepare identity
			IdmIdentityDto identity = helper.createIdentity();
			// assign role
			helper.createIdentityRole(identity, role);
			helper.createIdentityRole(identity, role2);
			//
			List<IdmAuthorizationPolicyDto> policies = service.getEnabledPolicies(identity.getId(), IdmRole.class);
			assertEquals(1, policies.size());
			assertEquals(role.getId(), policies.get(0).getRole());
		} finally {
			logout();
		}
	}
	
	@Test
	@Transactional
	public void testFindValidPoliciesWithInvalidIdentityRole() {
		try {
			loginAsAdmin(InitTestData.TEST_USER_1);
			// prepare role
			IdmRoleDto role = helper.createRole();
			IdmRoleDto role2 = helper.createRole();
			helper.createUuidPolicy(role.getId(), role.getId(), IdmBasePermission.READ);		
			helper.createBasePolicy(role2.getId(), IdmBasePermission.AUTOCOMPLETE);	
			// prepare identity
			IdmIdentityDto identity = helper.createIdentity();
			// assign role
			helper.createIdentityRole(identity, role);
			IdmIdentityRoleDto assignedRole = helper.createIdentityRole(identity, role2);
			assignedRole.setValidFrom(new LocalDate().plusDays(1));
			identityRoleService.save(assignedRole);
			//
			List<IdmAuthorizationPolicyDto> policies = service.getEnabledPolicies(identity.getId(), IdmRole.class);
			assertEquals(1, policies.size());
			assertEquals(role.getId(), policies.get(0).getRole());
		} finally {
			logout();
		}
	}
	
	@Test
	public void testFindValidPoliciesWithInvalidIdentityContractByDisabled() {
		try {
			loginAsAdmin(InitTestData.TEST_USER_1);
			// prepare role
			IdmRoleDto role = helper.createRole();
			IdmRoleDto role2 = helper.createRole();
			helper.createUuidPolicy(role.getId(), role.getId(), IdmBasePermission.READ);		
			helper.createBasePolicy(role2.getId(), IdmBasePermission.AUTOCOMPLETE);	
			// prepare identity
			IdmIdentityDto identity = helper.createIdentity();
			// assign role
			helper.createIdentityRole(identity, role);
			IdmIdentityContractDto contract = helper.createIdentityContact(identity);
			contract.setDisabled(true);	
			identityContractService.save(contract);
			helper.createIdentityRole(contract, role2);
			//
			List<IdmAuthorizationPolicyDto> policies = service.getEnabledPolicies(identity.getId(), IdmRole.class);
			assertEquals(1, policies.size());
			assertEquals(role.getId(), policies.get(0).getRole());
		} finally {
			logout();
		}
	}
	
	@Test
	@Transactional
	public void testFindValidPoliciesWithInvalidIdentityContractByDates() {
		try {
			loginAsAdmin(InitTestData.TEST_USER_1);
			// prepare role
			IdmRoleDto role = helper.createRole();
			IdmRoleDto role2 = helper.createRole();
			helper.createUuidPolicy(role.getId(), role.getId(), IdmBasePermission.READ);		
			helper.createBasePolicy(role2.getId(), IdmBasePermission.AUTOCOMPLETE);	
			// prepare identity
			IdmIdentityDto identity = helper.createIdentity();
			// assign role
			helper.createIdentityRole(identity, role);
			IdmIdentityContractDto contract = new IdmIdentityContractDto();
			contract.setIdentity(identity.getId());
			contract.setPosition("position-" + System.currentTimeMillis());
			contract.setValidFrom(new LocalDate().plusDays(1));
			contract = identityContractService.save(contract);
			helper.createIdentityRole(contract, role2);
			//
			List<IdmAuthorizationPolicyDto> policies = service.getEnabledPolicies(identity.getId(), IdmRole.class);
			assertEquals(1, policies.size());
			assertEquals(role.getId(), policies.get(0).getRole());
		} finally {
			logout();
		}
	}
}
