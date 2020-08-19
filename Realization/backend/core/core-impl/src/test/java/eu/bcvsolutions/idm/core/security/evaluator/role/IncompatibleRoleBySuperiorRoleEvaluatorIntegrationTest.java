package eu.bcvsolutions.idm.core.security.evaluator.role;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIncompatibleRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIncompatibleRole;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractEvaluatorIntegrationTest;

/**
 * Permission to role composition by composition's superior role
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class IncompatibleRoleBySuperiorRoleEvaluatorIntegrationTest extends AbstractEvaluatorIntegrationTest {

	@Autowired private IdmIncompatibleRoleService service;
	@Autowired private IdmRoleService roleService;
	
	@Test
	public void canReadCompositionByRole() {
		IdmIdentityDto identity = getHelper().createIdentity();
		List<IdmIncompatibleRoleDto> results = null;
		IdmRoleDto role = getHelper().createRole();
		IdmRoleDto subRole = getHelper().createRole();
		IdmRoleDto superiorRole = getHelper().createRole();
		IdmIncompatibleRoleDto incompatibleRole = getHelper().createIncompatibleRole(role, subRole);
		getHelper().createRoleComposition(superiorRole, role); // other - without access
		getHelper().createIdentityRole(identity, role);
		getHelper().createUuidPolicy(role.getId(), role.getId(), IdmBasePermission.READ);
		//
		// check created identity doesn't have compositions
		try {			
			getHelper().login(identity.getUsername(), identity.getPassword());
			Assert.assertEquals(role.getId(), roleService.get(role.getId(), IdmBasePermission.READ).getId());
			results = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(results.isEmpty());	
		} finally {
			logout();
		}
		//
		// create authorization policy - assign to role
		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.INCOMPATIBLEROLE,
				IdmIncompatibleRole.class,
				IncompatibleRoleBySuperiorRoleEvaluator.class);
		//
		try {
			getHelper().login(identity);
			//
			// evaluate	access
			getHelper().login(identity.getUsername(), identity.getPassword());
			results = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, results.size());	
			Assert.assertEquals(incompatibleRole.getId(), results.get(0).getId());
			//
			Set<String> permissions = service.getPermissions(incompatibleRole);
			Assert.assertEquals(1, permissions.size());
			Assert.assertEquals(IdmBasePermission.READ.name(), permissions.iterator().next());
		} finally {
			logout();
		}
		//
		getHelper().createUuidPolicy(role.getId(), role.getId(), IdmBasePermission.UPDATE);
		//
		try {
			getHelper().login(identity.getUsername(), identity.getPassword());
			//
			Set<String> permissions = service.getPermissions(incompatibleRole);
			Assert.assertEquals(4, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.name())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.UPDATE.name())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.CREATE.name())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.DELETE.name())));
		} finally {
			logout();
		}
	}	
}
