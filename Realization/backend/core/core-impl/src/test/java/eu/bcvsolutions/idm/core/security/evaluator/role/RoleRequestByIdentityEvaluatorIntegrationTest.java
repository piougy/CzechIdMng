package eu.bcvsolutions.idm.core.security.evaluator.role;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.InitDemoData;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.rest.impl.IdmConceptRoleRequestController;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Test permissions for role requests by identity 
 * 
 * @author Radek Tomiška
 *
 */
public class RoleRequestByIdentityEvaluatorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private IdmRoleService roleService;
	@Autowired private LoginService loginService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmConceptRoleRequestController conceptRoleRequestController;
	
	@Test
	public void testCanReadRoleRequestByIdentity() {
		IdmIdentityDto identityOne = helper.createIdentity();
		IdmIdentityDto identityTwo = helper.createIdentity();
		// create policy
		IdmRoleDto role = helper.createRole();
		helper.createUuidPolicy(role.getId(), identityOne.getId(), IdmBasePermission.READ);
		helper.createIdentityRole(identityTwo, role);
		helper.createIdentityRole(identityTwo, roleService.getByCode(InitDemoData.DEFAULT_ROLE_NAME));
		IdmRoleRequestDto roleRequest = helper.assignRoles(helper.getPrimeContract(identityOne.getId()), role);
		//
		try {			
			loginService.login(new LoginDto(identityTwo.getUsername(), identityTwo.getPassword()));
			//
			Page<IdmRoleRequestDto> roleRequests = roleRequestService.find(null, IdmBasePermission.READ);
			assertEquals(1, roleRequests.getTotalElements());
			//
			IdmRoleRequestDto read = roleRequestService.get(roleRequest.getId(), IdmBasePermission.READ);
			assertEquals(roleRequest, read);
			//			
			IdmConceptRoleRequestFilter filter = new IdmConceptRoleRequestFilter();
			filter.setRoleRequestId(roleRequest.getId());
			Page<IdmConceptRoleRequestDto> concepts = conceptRoleRequestController.find(filter, null, IdmBasePermission.READ);
			assertEquals(1, concepts.getTotalElements());	
		} finally {
			logout();
		}
	}

	@Test(expected = ForbiddenEntityException.class)
	public void testCannotReadRoleRequestByIdentity() {
		IdmIdentityDto identityOne = helper.createIdentity();
		IdmIdentityDto identityTwo = helper.createIdentity();
		//
		IdmRoleDto role = helper.createRole();
		helper.createIdentityRole(identityTwo, role);
		IdmRoleRequestDto roleRequest = helper.assignRoles(helper.getPrimeContract(identityOne.getId()), role);
		//
		try {			
			loginService.login(new LoginDto(identityTwo.getUsername(), identityTwo.getPassword()));
			//
			roleRequestService.get(roleRequest.getId(), IdmBasePermission.READ);
		} finally {
			logout();
		}
	}
}

