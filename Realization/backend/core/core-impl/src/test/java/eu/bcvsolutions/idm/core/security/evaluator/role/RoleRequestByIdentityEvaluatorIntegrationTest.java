package eu.bcvsolutions.idm.core.security.evaluator.role;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.InitDemoData;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.rest.impl.IdmConceptRoleRequestController;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
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
	
	@Test(expected = ForbiddenEntityException.class)
	public void testCannotCreateRoleRequestByIdentity() {
		IdmIdentityDto identityOne = helper.createIdentity();
		IdmIdentityDto identityTwo = helper.createIdentity();
		//
		try {			
			loginService.login(new LoginDto(identityOne.getUsername(), identityOne.getPassword()));
			//
			IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
			roleRequest.setApplicant(identityTwo.getId());
			roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
			roleRequestService.save(roleRequest, IdmBasePermission.CREATE);
		} finally {
			logout();
		}
	}
	
	@Test
	public void testCreateRoleRequestForOtherIdentity() {
		IdmIdentityDto identityOne = helper.createIdentity();
		IdmIdentityDto identityTwo = helper.createIdentity();
		// create policy
		IdmRoleDto role = helper.createRole();
		helper.createUuidPolicy(role.getId(), identityTwo.getId(), IdentityBasePermission.CHANGEPERMISSION);
		helper.createIdentityRole(identityOne, role);
		// assign default role
		helper.createIdentityRole(identityOne, roleService.getByCode(InitDemoData.DEFAULT_ROLE_NAME));
		//
		try {			
			loginService.login(new LoginDto(identityOne.getUsername(), identityOne.getPassword()));
			//
			IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
			roleRequest.setApplicant(identityTwo.getId());
			roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
			roleRequest = roleRequestService.save(roleRequest, IdmBasePermission.CREATE);
			//
			Assert.notNull(roleRequest.getId());
		} finally {
			logout();
		}
	}
}

