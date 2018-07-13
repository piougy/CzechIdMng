package eu.bcvsolutions.idm.core.security.evaluator;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Codeable evaluator tests
 * 
 * @author Radek Tomiška
 *
 */
public class CodeableEvaluatorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	
	@Test
	public void testPermissionByWrongUuid() {
		loginAsAdmin();
		// prepare role
		IdmRoleDto role = getHelper().createRole();
		//
		IdmAuthorizationPolicyDto dto = new IdmAuthorizationPolicyDto();
		dto.setRole(role.getId());
		dto.setEvaluator(CodeableEvaluator.class);
		dto.setGroupPermission(CoreGroupPermission.ROLE.getName());
		dto.setAuthorizableType(IdmRole.class.getCanonicalName());
		dto.getEvaluatorProperties().put(CodeableEvaluator.PARAMETER_IDENTIFIER, "wrong");
		dto.setPermissions(IdmBasePermission.READ);
		authorizationPolicyService.save(dto);
		// prepare identity
		IdmIdentityDto identity = getHelper().createIdentity();
		// assign role
		getHelper().createIdentityRole(identity, role);
		logout();
		//
		try {			
			getHelper().login(identity.getUsername(), identity.getPassword());
			//
			// evaluate	access
			assertEquals(0, roleService.find(null, IdmBasePermission.READ).getTotalElements());		
		} finally {
			logout();
		}
	}
	
	@Test
	public void testPermissionByUuid() {
		loginAsAdmin();
		// prepare role
		IdmRoleDto role = getHelper().createRole();
		//
		IdmAuthorizationPolicyDto dto = new IdmAuthorizationPolicyDto();
		dto.setRole(role.getId());
		dto.setEvaluator(CodeableEvaluator.class);
		dto.setGroupPermission(CoreGroupPermission.ROLE.getName());
		dto.setAuthorizableType(IdmRole.class.getCanonicalName());
		dto.getEvaluatorProperties().put(CodeableEvaluator.PARAMETER_IDENTIFIER, role.getId().toString());
		dto.setPermissions(IdmBasePermission.READ);
		authorizationPolicyService.save(dto);
		// prepare identity
		IdmIdentityDto identity = getHelper().createIdentity();
		// assign role
		getHelper().createIdentityRole(identity, role);
		logout();
		//
		try {
			getHelper().login(identity.getUsername(), identity.getPassword());
			//
			// evaluate	access
			List<IdmRoleDto> roles = roleService.find(null, IdmBasePermission.READ).getContent();
			assertEquals(1, roles.size());
			assertEquals(role.getId(), roles.get(0).getId());
		} finally {
			logout();
		}
	}
	
	@Test
	public void testPermissionByCode() {
		loginAsAdmin();
		// prepare role
		IdmRoleDto role = getHelper().createRole();
		//
		IdmAuthorizationPolicyDto dto = new IdmAuthorizationPolicyDto();
		dto.setRole(role.getId());
		dto.setEvaluator(CodeableEvaluator.class);
		dto.setGroupPermission(CoreGroupPermission.ROLE.getName());
		dto.setAuthorizableType(IdmRole.class.getCanonicalName());
		dto.getEvaluatorProperties().put(CodeableEvaluator.PARAMETER_IDENTIFIER, role.getCode());
		dto.setPermissions(IdmBasePermission.READ);
		authorizationPolicyService.save(dto);
		// prepare identity
		IdmIdentityDto identity = getHelper().createIdentity();
		// assign role
		getHelper().createIdentityRole(identity, role);
		logout();
		//
		try {
			getHelper().login(identity.getUsername(), identity.getPassword());
			//
			// evaluate	access
			List<IdmRoleDto> roles = roleService.find(null, IdmBasePermission.READ).getContent();
			assertEquals(1, roles.size());
			assertEquals(role.getId(), roles.get(0).getId());
		} finally {
			logout();
		}
	}

}
