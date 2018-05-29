package eu.bcvsolutions.idm.acc.security.evaluator;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.entity.AccRoleAccount;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.core.security.evaluator.CodeableEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.role.AuthorizationPolicyByRoleEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for RoleAccountEvaluator
 * 
 * @author Kučera
 *
 */
public class RoleAccountByRoleEvaluatorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private TestHelper helper;
	@Autowired private IdmRoleService roleService;
	@Autowired private LoginService loginService;
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	@Autowired private IdmIdentityService identityService;
	
	private static UUID TEST_ROLE_ID;
	
	@Test
	public void testRoleWithoutEvaluator() {
		IdmIdentityDto identity = createIdentityWithRole(false);
		try {
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			IdmRoleDto role = roleService.get(TEST_ROLE_ID, IdmBasePermission.READ);
			
			assertEquals(TEST_ROLE_ID, role.getId());
			assertEquals(1, roleService.find(null, IdmBasePermission.READ).getTotalElements());
			assertEquals(0, authorizationPolicyService.find(null, IdmBasePermission.READ).getTotalElements());
		} finally {
			logout();
		}
	}
	
	@Test
	public void testReadRoleWithEvaluator() {
		IdmIdentityDto identity = createIdentityWithRole(true);
		try {			
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			IdmRoleDto role = roleService.get(TEST_ROLE_ID, IdmBasePermission.READ);
			
			assertEquals(TEST_ROLE_ID, role.getId());
			assertEquals(1, roleService.find(null, IdmBasePermission.READ).getTotalElements());
			assertEquals(3, authorizationPolicyService.find(null, IdmBasePermission.READ).getTotalElements());
		} finally {
			logout();
		}
	}

	private IdmIdentityDto createIdentityWithRole(boolean transitive) {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		IdmRoleDto role = helper.createRole();
		TEST_ROLE_ID = role.getId();
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
			IdmAuthorizationPolicyDto readAuthoritiesPolicy = new IdmAuthorizationPolicyDto();
			readAuthoritiesPolicy.setRole(role.getId());
			readAuthoritiesPolicy.setGroupPermission(CoreGroupPermission.AUTHORIZATIONPOLICY.getName());
			readAuthoritiesPolicy.setAuthorizableType(IdmAuthorizationPolicy.class.getCanonicalName());
			readAuthoritiesPolicy.setEvaluator(AuthorizationPolicyByRoleEvaluator.class);
			authorizationPolicyService.save(readAuthoritiesPolicy);
			
			IdmAuthorizationPolicyDto readRoleAccountPolicy = new IdmAuthorizationPolicyDto();
			readRoleAccountPolicy.setRole(role.getId());
			readRoleAccountPolicy.setGroupPermission(AccGroupPermission.ROLEACCOUNT.getName());
			readRoleAccountPolicy.setAuthorizableType(AccRoleAccount.class.getCanonicalName());
			readRoleAccountPolicy.setEvaluator(RoleAccountByRoleEvaluator.class);
			authorizationPolicyService.save(readRoleAccountPolicy);
		}
		
		GuardedString password = new GuardedString("heslo");
		// prepare identity
		IdmIdentityDto identity = helper.createIdentity();
		identity.setPassword(password);
		identity = identityService.save(identity);
		// assign role
		helper.createIdentityRole(identity, role);
		logout();
		//
		// password is transient, some test except password back in identity
		identity.setPassword(password);
		//
		return identity;
	}
}
