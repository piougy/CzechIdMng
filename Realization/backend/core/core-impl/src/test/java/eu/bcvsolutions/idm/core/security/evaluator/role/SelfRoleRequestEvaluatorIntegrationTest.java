package eu.bcvsolutions.idm.core.security.evaluator.role;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
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
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractEvaluatorIntegrationTest;

/**
 * Test self role request permission.
 * 
 * @author Radek Tomiška
 *
 */
public class SelfRoleRequestEvaluatorIntegrationTest extends AbstractEvaluatorIntegrationTest {

	@Autowired private IdmRoleService roleService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmConceptRoleRequestController conceptRoleRequestController;
	
	@Test
	public void testCanReadSelfRoleRequest() {
		IdmRoleDto role = roleService.getByCode(RoleConfiguration.DEFAULT_DEFAULT_ROLE);
		// prepare identity
		IdmIdentityDto identity = getHelper().createIdentity();
		// assign role request
		IdmRoleRequestDto roleRequest = getHelper().assignRoles(getHelper().getPrimeContract(identity.getId()), role);
		//
		try {			
			getHelper().login(identity);
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
		IdmRoleDto role = roleService.getByCode(RoleConfiguration.DEFAULT_DEFAULT_ROLE);
		// prepare identities
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityDto identityTwo = getHelper().createIdentity();
		// assign role request
		IdmRoleRequestDto roleRequest = getHelper().assignRoles(getHelper().getPrimeContract(identity.getId()), role);
		//
		try {			
			getHelper().login(identityTwo);
			//
			roleRequestService.get(roleRequest.getId(), IdmBasePermission.READ);
		} finally {
			logout();
		}
	}
	
	@Test(expected = ForbiddenEntityException.class)
	public void testCannotReadForeignConceptRoleRequest() {
		// prepare identity
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		try {			
			getHelper().login(identity);
			//
			conceptRoleRequestController.find(null, null, IdmBasePermission.READ);
		} finally {
			logout();
		}
	}
	
	@Test
	public void testCreateRoleRequestForSelf() {
		IdmIdentityDto identityOne = getHelper().createIdentity();
		// assign default role
		getHelper().createIdentityRole(identityOne, roleService.getByCode(RoleConfiguration.DEFAULT_DEFAULT_ROLE));
		//
		try {			
			getHelper().login(identityOne);
			//
			IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
			roleRequest.setApplicant(identityOne.getId());
			roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
			roleRequest = roleRequestService.save(roleRequest, IdmBasePermission.CREATE);
			//
			Assert.notNull(roleRequest.getId(), "Request identifier is required.");
		} finally {
			logout();
		}
	}
}

