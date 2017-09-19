package eu.bcvsolutions.idm.core.security.evaluator.role;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.core.security.evaluator.CodeableEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Test whole self identity profile read and password change
 * - based on default role inicialization, but default role could be inited here to
 * 
 * @author Radek Tomiška
 *
 */
public class RoleTransitiveEvaluatorsIntegrationTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private LoginService loginService;
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	@Autowired private ConfigurationService configurationService;
	
	private static UUID TEST_ROLE_ID;
	
	@Test
	public void testReadRoleWithoutTransitiveEvaluators() {
		IdmIdentityDto identity = createIdentityWithRole(false);
		//
		try {			
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			IdmRoleDto readRole = roleService.get(TEST_ROLE_ID, IdmBasePermission.READ);
			assertEquals(TEST_ROLE_ID, readRole.getId());
			assertEquals(1, roleService.find(null, IdmBasePermission.READ).getTotalElements());
			assertEquals(0, roleTreeNodeService.find(null, IdmBasePermission.READ).getTotalElements());
			assertEquals(0, authorizationPolicyService.find(null, IdmBasePermission.READ).getTotalElements());
		} finally {
			logout();
		}
	}
	
	@Test
	public void testReadRoleWithTransitiveEvaluators() {
		IdmIdentityDto identity = createIdentityWithRole(true);
		//
		try {			
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			IdmRoleDto readRole = roleService.get(TEST_ROLE_ID, IdmBasePermission.READ);
			assertEquals(TEST_ROLE_ID, readRole.getId());
			assertEquals(1, roleService.find(null, IdmBasePermission.READ).getTotalElements());
			assertEquals(1, roleTreeNodeService.find(null, IdmBasePermission.READ).getTotalElements());
			assertEquals(3, authorizationPolicyService.find(null, IdmBasePermission.READ).getTotalElements());
		} finally {
			logout();
		}
	}
	
	@Test(expected = ForbiddenEntityException.class)
	public void testUpdateRole() {
		IdmIdentityDto identity = createIdentityWithRole(true);
		//
		try {			
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			roleService.get(TEST_ROLE_ID, IdmBasePermission.UPDATE);
		} finally {
			logout();
		}
	}
	
	@Test(expected = ForbiddenEntityException.class)
	public void testCreateAutomaticRole() {
		IdmIdentityDto identity = createIdentityWithRole(true);
		//
		try {			
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			IdmRoleDto role = roleService.get(TEST_ROLE_ID, IdmBasePermission.READ);
			
			IdmRoleTreeNodeDto roleTreeNode = new IdmRoleTreeNodeDto();
			roleTreeNode.setRole(role.getId());
			roleTreeNode.setTreeNode(helper.createTreeNode().getId());
			roleTreeNodeService.save(roleTreeNode, IdmBasePermission.UPDATE);
		} finally {
			logout();
		}
	}
	
	@Test(expected = ForbiddenEntityException.class)
	public void testUpdateAuthorizationPolicy() {
		IdmIdentityDto identity = createIdentityWithRole(true);
		//
		try {			
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//		
			IdmAuthorizationPolicyDto policy = authorizationPolicyService.find(null, IdmBasePermission.READ).getContent().get(0);
			policy.setDisabled(true);
			authorizationPolicyService.save(policy, IdmBasePermission.UPDATE);			
		} finally {
			logout();
		}
	}
	
	@Test
	public void testDisabledPolicy() {
		IdmIdentityDto identity = createIdentityWithRole(true);
		IdmRoleDto role = null;
		//
		// before disbale
		try {			
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			role = roleService.get(TEST_ROLE_ID, IdmBasePermission.READ);
			assertEquals(TEST_ROLE_ID, role.getId());
			assertEquals(1, roleService.find(null, IdmBasePermission.READ).getTotalElements());
			assertEquals(1, roleTreeNodeService.find(null, IdmBasePermission.READ).getTotalElements());
			assertEquals(3, authorizationPolicyService.find(null, IdmBasePermission.READ).getTotalElements());
		} finally {
			logout();
		}
		//
		// disable policy
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		IdmAuthorizationPolicyFilter filter = new IdmAuthorizationPolicyFilter();
		filter.setRoleId(role.getId());
		filter.setAuthorizableType(IdmRole.class.getCanonicalName());
		IdmAuthorizationPolicyDto policy = authorizationPolicyService.find(filter, null).getContent().get(0);
		policy.setDisabled(true);
		authorizationPolicyService.save(policy);		
		logout();
		//
		// after disable
		try {			
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			assertEquals(0, roleService.find(null, IdmBasePermission.READ).getTotalElements());
			assertEquals(0, roleTreeNodeService.find(null, IdmBasePermission.READ).getTotalElements());
			assertEquals(0, authorizationPolicyService.find(null, IdmBasePermission.READ).getTotalElements());
		} finally {
			logout();
		}
	}
	
	@Test
	public void testDisabledEvaluator() {
		IdmIdentityDto identity = createIdentityWithRole(true);
		IdmRoleDto role = null;
		//
		// before disbale
		try {			
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			role = roleService.get(TEST_ROLE_ID, IdmBasePermission.READ);
			assertEquals(TEST_ROLE_ID, role.getId());
			assertEquals(1, roleService.find(null, IdmBasePermission.READ).getTotalElements());
			assertEquals(1, roleTreeNodeService.find(null, IdmBasePermission.READ).getTotalElements());
			assertEquals(3, authorizationPolicyService.find(null, IdmBasePermission.READ).getTotalElements());
		} finally {
			logout();
		}
		//
		// disable policy
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// TODO: disable configurable api
		CodeableEvaluator evaluator = new CodeableEvaluator();
		configurationService.setBooleanValue(evaluator.getConfigurationPropertyName(ConfigurationService.PROPERTY_ENABLED), false);
		logout();
		//
		// after disable
		try {			
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			assertEquals(0, roleService.find(null, IdmBasePermission.READ).getTotalElements());
			assertEquals(0, roleTreeNodeService.find(null, IdmBasePermission.READ).getTotalElements());
			assertEquals(0, authorizationPolicyService.find(null, IdmBasePermission.READ).getTotalElements());
		} finally {
			logout();
			// enable policy
			loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
			// TODO: disable configurable api
			configurationService.setBooleanValue(evaluator.getConfigurationPropertyName(ConfigurationService.PROPERTY_ENABLED), true);
			logout();
		}
	}
	
	private IdmIdentityDto createIdentityWithRole(boolean transitive) {
		TEST_ROLE_ID = UUID.randomUUID();
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		IdmRoleDto role = helper.createRole(TEST_ROLE_ID, null);
		IdmTreeNodeDto treeNode = helper.createTreeNode();
		helper.createRoleTreeNode(role, treeNode, true);
		// self policy
		IdmAuthorizationPolicyDto readRolePolicy = new IdmAuthorizationPolicyDto();
		readRolePolicy.setPermissions(IdmBasePermission.READ);
		readRolePolicy.setRole(role.getId());
		readRolePolicy.setGroupPermission(CoreGroupPermission.ROLE.getName());
		readRolePolicy.setAuthorizableType(IdmRole.class.getCanonicalName());
		readRolePolicy.setEvaluator(CodeableEvaluator.class);
		readRolePolicy.getEvaluatorProperties().put(CodeableEvaluator.PARAMETER_IDENTIFIER, role.getId());
		authorizationPolicyService.save(readRolePolicy);
		if (transitive) {
			// create transitive policies
			IdmAuthorizationPolicyDto readRoleTreeNodePolicy = new IdmAuthorizationPolicyDto();
			readRoleTreeNodePolicy.setRole(role.getId());
			readRoleTreeNodePolicy.setGroupPermission(CoreGroupPermission.ROLETREENODE.getName());
			readRoleTreeNodePolicy.setAuthorizableType(IdmRoleTreeNode.class.getCanonicalName());
			readRoleTreeNodePolicy.setEvaluator(RoleTreeNodeByRoleEvaluator.class);
			authorizationPolicyService.save(readRoleTreeNodePolicy);
			//
			IdmAuthorizationPolicyDto readAuthoritiesPolicy = new IdmAuthorizationPolicyDto();
			readAuthoritiesPolicy.setRole(role.getId());
			readAuthoritiesPolicy.setGroupPermission(CoreGroupPermission.AUTHORIZATIONPOLICY.getName());
			readAuthoritiesPolicy.setAuthorizableType(IdmAuthorizationPolicy.class.getCanonicalName());
			readAuthoritiesPolicy.setEvaluator(AuthorizationPolicyByRoleEvaluator.class);
			authorizationPolicyService.save(readAuthoritiesPolicy);
		}
		
		// prepare identity
		IdmIdentityDto identity = helper.createIdentity();
		identity.setPassword(new GuardedString("heslo"));
		identity = identityService.save(identity);
		// assign role
		helper.createIdentityRole(identity, role);
		logout();
		//
		return identity;
	}
	
	
	// filter builder - otestovat id + subordinates na identitach + disable filtreru + disable modulu
}

