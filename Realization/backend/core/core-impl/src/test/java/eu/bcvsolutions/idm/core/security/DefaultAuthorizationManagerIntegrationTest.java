package eu.bcvsolutions.idm.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.config.cache.domain.ValueWrapper;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizationEvaluatorDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultAuthorizationManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

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
	@Autowired private LoginService loginService;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmCacheManager cacheManager;
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	//
	private AuthorizationManager manager;
	
	@Before
	public void init() {		
		manager = context.getAutowireCapableBeanFactory().createBean(DefaultAuthorizationManager.class);
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
		loginAsAdmin();
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
		loginAsAdmin();
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
		IdmRoleFilter filter = new IdmRoleFilter();
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
			loginAsAdmin();
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
			loginAsAdmin();
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
			loginAsAdmin();
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
			assignedRole.setValidFrom(LocalDate.now().plusDays(1));
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
			loginAsAdmin();
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
			contract.setState(ContractState.DISABLED);	
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
			loginAsAdmin();
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
			contract.setValidFrom(LocalDate.now().plusDays(1));
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
	
	@Test
	@Transactional
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testCache() {
		// create and login identity
		IdmIdentityDto identity = getHelper().createIdentity();
		UUID mockIdentity = UUID.randomUUID();
		// prepare role
		IdmRoleDto role = helper.createRole();
		IdmAuthorizationPolicyDto policy = helper.createBasePolicy(role.getId(), IdmBasePermission.AUTOCOMPLETE, IdmBasePermission.READ);
		getHelper().createIdentityRole(identity, role);
		//
		Assert.assertNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, identity.getId()));
		Assert.assertNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId()));
		Assert.assertNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, mockIdentity));
		Assert.assertNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, mockIdentity));
		//
		cacheManager.cacheValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, mockIdentity, new HashMap<>());
		cacheManager.cacheValue(AuthorizationManager.PERMISSION_CACHE_NAME, mockIdentity, new HashMap<>());
		Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, mockIdentity));
		Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, mockIdentity));
		//
		// without login
		Set<String> permissions = manager.getPermissions(role);
		Assert.assertTrue(permissions.isEmpty());
		//
		
		//
		try {
			getHelper().login(identity);
			//
			// new entity is not supported with cache, but permissions are evaluated
			permissions = manager.getPermissions(new IdmRoleDto());
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.AUTOCOMPLETE.getName())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
			Assert.assertNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId()));
			//
			// load from db
			permissions = manager.getPermissions(role);
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.AUTOCOMPLETE.getName())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
			Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, identity.getId()));
			Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId()));
			// load from cache
			permissions = manager.getPermissions(role);
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.AUTOCOMPLETE.getName())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
			Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, identity.getId()));
			Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId()));
			// check cache content - one
			ValueWrapper cacheValue = cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, identity.getId());
			List<IdmAuthorizationPolicyDto> cachedPolicies = (List) ((Map) cacheValue.get()).get(role.getClass());
			Assert.assertEquals(1, cachedPolicies.size());
			Assert.assertEquals(BasePermissionEvaluator.class.getCanonicalName(), cachedPolicies.get(0).getEvaluatorType());
			cacheValue = cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId());
			permissions = (Set) ((Map) cacheValue.get()).get(role.getId());
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.AUTOCOMPLETE.getName())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
			//
			// change policy => evict whole cache
			policy.setPermissions(IdmBasePermission.AUTOCOMPLETE, IdmBasePermission.READ, IdmBasePermission.UPDATE);
			authorizationPolicyService.save(policy);
			Assert.assertNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, identity.getId()));
			Assert.assertNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId()));
			Assert.assertNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, mockIdentity));
			Assert.assertNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, mockIdentity));
			//
			cacheManager.cacheValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, mockIdentity, new HashMap<>());
			cacheManager.cacheValue(AuthorizationManager.PERMISSION_CACHE_NAME, mockIdentity, new HashMap<>());
			permissions = manager.getPermissions(role);
			Assert.assertEquals(3, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.AUTOCOMPLETE.getName())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.UPDATE.getName())));
			Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, identity.getId()));
			Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId()));
		} finally {
			logout(); // evict logged identity cache only
		}
		// check cache is evicted only for logged identity
		Assert.assertNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, identity.getId()));
		Assert.assertNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId()));
		Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, mockIdentity));
		Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, mockIdentity));
	}
}
