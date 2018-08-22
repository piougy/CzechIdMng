package eu.bcvsolutions.idm.core.security.evaluator.role;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Role by guarantee evaluator
 * - guarantee defined by identity
 * - guarantee defined by role
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class RoleByGuaranteeEvaluatorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private IdmRoleService service;
	
	@Test
	public void canReadByIdentity() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		getHelper().createRoleGuarantee(role, identity);
		getHelper().createIdentityRole(identity, role);
		List<IdmRoleDto> roles = null;
		//
		// check created identity doesn't have compositions
		try {			
			getHelper().login(identity.getUsername(), identity.getPassword());
			roles = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(roles.isEmpty());	
		} finally {
			logout();
		}
		//
		// create authorization policy - assign to role
		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.ROLE,
				IdmRole.class,
				RoleGuaranteeEvaluator.class,
				IdmBasePermission.READ);
		//
		try {
			//
			// evaluate	access
			getHelper().login(identity.getUsername(), identity.getPassword());
			roles = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, roles.size());	
			Assert.assertEquals(role.getId(), roles.get(0).getId());
			//
			Set<String> permissions = service.getPermissions(role);
			Assert.assertEquals(1, permissions.size());
			Assert.assertEquals(IdmBasePermission.READ.name(), permissions.iterator().next());
		} finally {
			logout();
		}
	}
	
	@Test
	public void canReadByRole() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		getHelper().createRoleGuaranteeRole(role, role);
		getHelper().createIdentityRole(identity, role);
		List<IdmRoleDto> roles = null;
		//
		// check created identity doesn't have compositions
		try {			
			getHelper().login(identity.getUsername(), identity.getPassword());
			roles = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(roles.isEmpty());	
		} finally {
			logout();
		}
		//
		// create authorization policy - assign to role
		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.ROLE,
				IdmRole.class,
				RoleGuaranteeEvaluator.class,
				IdmBasePermission.READ);
		//
		try {
			//
			// evaluate	access
			getHelper().login(identity.getUsername(), identity.getPassword());
			roles = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, roles.size());	
			Assert.assertEquals(role.getId(), roles.get(0).getId());
			//
			Set<String> permissions = service.getPermissions(role);
			Assert.assertEquals(1, permissions.size());
			Assert.assertEquals(IdmBasePermission.READ.name(), permissions.iterator().next());
		} finally {
			logout();
		}
	}
}
	
