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
 * Test self role request permission 
 * 
 * @author Radek Tomiška
 *
 */
public class SelfRoleRequestEvaluatorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private IdmRoleService roleService;
	@Autowired private LoginService loginService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmConceptRoleRequestController conceptRoleRequestController;
	
	@Test
	public void testCanReadSelfRoleRequest() {
		IdmRoleDto role = roleService.getByCode(InitDemoData.DEFAULT_ROLE_NAME);
		// prepare identity
		IdmIdentityDto identity = helper.createIdentity();
		// assign role request
		IdmRoleRequestDto roleRequest = helper.assignRoles(helper.getPrimeContract(identity.getId()), role);
		//
		try {			
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
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
	public void testCannotReadForeignRoleRequest() {
		IdmRoleDto role = roleService.getByCode(InitDemoData.DEFAULT_ROLE_NAME);
		// prepare identities
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityDto identityTwo = helper.createIdentity();
		// assign role request
		IdmRoleRequestDto roleRequest = helper.assignRoles(helper.getPrimeContract(identity.getId()), role);
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
	public void testCannotReadForeignConceptRoleRequest() {
		// prepare identity
		IdmIdentityDto identity = helper.createIdentity();
		//
		try {			
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			conceptRoleRequestController.find(null, null, IdmBasePermission.READ);
		} finally {
			logout();
		}
	}
}

