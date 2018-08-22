package eu.bcvsolutions.idm.core.security.evaluator.role;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuaranteeRole;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Permission to role guarantee relations by relation's role
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class RoleGuaranteeRoleByRoleEvaluatorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private IdmRoleGuaranteeRoleService service;
	@Autowired private IdmRoleService roleService;
	
	@Test
	public void canReadByRole() {
		IdmIdentityDto identity = getHelper().createIdentity();
		List<IdmRoleGuaranteeRoleDto> roleGuarantees = null;
		IdmRoleDto role = getHelper().createRole();
		IdmRoleGuaranteeRoleDto roleGuaranteeRole = null;
		try {
			getHelper().loginAdmin();
			roleGuaranteeRole = getHelper().createRoleGuaranteeRole(role, role);
			getHelper().createIdentityRole(identity, role);
			getHelper().createUuidPolicy(role.getId(), role.getId(), IdmBasePermission.READ);
		} finally {
			logout();
		}
		//
		// check created identity doesn't have compositions
		try {			
			getHelper().login(identity.getUsername(), identity.getPassword());
			Assert.assertEquals(role.getId(), roleService.get(role.getId(), IdmBasePermission.READ).getId());
			roleGuarantees = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(roleGuarantees.isEmpty());	
		} finally {
			logout();
		}
		//
		// create authorization policy - assign to role
		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.ROLEGUARANTEEROLE,
				IdmRoleGuaranteeRole.class,
				RoleGuaranteeRoleByRoleEvaluator.class);
		//
		try {
			getHelper().login(identity.getUsername(), identity.getPassword());
			//
			// evaluate	access
			roleGuarantees = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, roleGuarantees.size());	
			Assert.assertEquals(roleGuaranteeRole.getId(), roleGuarantees.get(0).getId());
			//
			Set<String> permissions = service.getPermissions(roleGuaranteeRole);
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
			Set<String> permissions = service.getPermissions(roleGuaranteeRole);
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
