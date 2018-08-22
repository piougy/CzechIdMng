package eu.bcvsolutions.idm.core.security.evaluator.role;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleComposition;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Permission to role composition by composition's superior role
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class RoleCompositionBySuperiorRoleEvaluatorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private IdmRoleCompositionService service;
	@Autowired private IdmRoleService roleService;
	
	@Test
	public void canReadCompositionByRole() {
		IdmIdentityDto identity = getHelper().createIdentity();
		List<IdmRoleCompositionDto> compositions = null;
		IdmRoleDto role = getHelper().createRole();
		IdmRoleDto subRole = getHelper().createRole();
		IdmRoleDto superiorRole = getHelper().createRole();
		IdmRoleCompositionDto roleComposition = getHelper().createRoleComposition(role, subRole);
		getHelper().createRoleComposition(superiorRole, role); // other - without access
		getHelper().createIdentityRole(identity, role);
		getHelper().createUuidPolicy(role.getId(), role.getId(), IdmBasePermission.READ);
		//
		// check created identity doesn't have compositions
		try {			
			getHelper().login(identity.getUsername(), identity.getPassword());
			Assert.assertEquals(role.getId(), roleService.get(role.getId(), IdmBasePermission.READ).getId());
			compositions = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(compositions.isEmpty());	
		} finally {
			logout();
		}
		//
		// create authorization policy - assign to role
		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.ROLECOMPOSITION,
				IdmRoleComposition.class,
				RoleCompositionBySuperiorRoleEvaluator.class);
		//
		try {
			getHelper().login(identity.getUsername(), identity.getPassword());
			//
			// evaluate	access
			getHelper().login(identity.getUsername(), identity.getPassword());
			compositions = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, compositions.size());	
			Assert.assertEquals(roleComposition.getId(), compositions.get(0).getId());
			//
			Set<String> permissions = service.getPermissions(roleComposition);
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
			Set<String> permissions = service.getPermissions(roleComposition);
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
