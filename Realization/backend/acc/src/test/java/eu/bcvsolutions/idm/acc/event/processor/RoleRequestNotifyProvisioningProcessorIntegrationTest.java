package eu.bcvsolutions.idm.acc.event.processor;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Execute role request
 * - one provisioning operation schould be ececuted
 * - prevent to drop and create target account, if one assigned role is deleted 
 * 
 * @author Radek Tomiška
 *
 */
public class RoleRequestNotifyProvisioningProcessorIntegrationTest extends AbstractIntegrationTest{

	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private SysProvisioningArchiveService provisioningArchiveService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired private AccAccountService accountService;
	
	@Before
	public void init() {
		loginAsAdmin();
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testAssignSubRolesByRequest() {
		// prepare role composition
		IdmRoleDto superior = getHelper().createRole();
		IdmRoleDto subOne = getHelper().createRole();
		IdmRoleDto subTwo = getHelper().createRole();
		IdmRoleDto subOneSub = getHelper().createRole();
		IdmRoleDto subOneSubSub = getHelper().createRole();
		getHelper().createRoleComposition(superior, subOne);
		getHelper().createRoleComposition(superior, subTwo);
		getHelper().createRoleComposition(subOne, subOneSub);
		getHelper().createRoleComposition(subOneSub, subOneSubSub);
		//
		IdmRoleDto other = getHelper().createRole();
		IdmRoleDto otherOne = getHelper().createRole();
		getHelper().createRoleComposition(other, otherOne);
		//
		// create test system with mapping and link her to the sub roles
		SysSystemDto system = getHelper().createTestResourceSystem(true);
		getHelper().createRoleSystem(subOneSubSub, system);
		getHelper().createRoleSystem(otherOne, system);
		//
		// assign superior role
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmRoleRequestDto roleRequest = getHelper().createRoleRequest(identity, superior);
		//
		getHelper().executeRequest(roleRequest, false);
		//
		// check after create
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(5, assignedRoles.size());
		IdmIdentityRoleDto directRole = assignedRoles.stream().filter(ir -> ir.getDirectRole() == null).findFirst().get();
		Assert.assertEquals(superior.getId(), directRole.getRole());
		//
		// check created account
		AccAccountDto account = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNotNull(account);
		Assert.assertNotNull(getHelper().findResource(account.getRealUid()));
		//
		// check provisioning archive
		SysProvisioningOperationFilter archiveFilter = new SysProvisioningOperationFilter();
		archiveFilter.setEntityIdentifier(identity.getId());
		//
		List<SysProvisioningArchiveDto> executedOperations = provisioningArchiveService.find(archiveFilter, null).getContent();
		Assert.assertEquals(1, executedOperations.size());
		Assert.assertTrue(executedOperations.stream().anyMatch(o -> o.getOperationType() == ProvisioningEventType.CREATE));
		//
		// remove one role and add other
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		roleRequest = new IdmRoleRequestDto();
		roleRequest.setApplicant(identity.getId());
		roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
		roleRequest.setExecuteImmediately(true);
		roleRequest = roleRequestService.save(roleRequest);
		// remove
		IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
		conceptRoleRequest.setRoleRequest(roleRequest.getId());
		conceptRoleRequest.setIdentityContract(contract.getId());
		conceptRoleRequest.setRole(superior.getId());
		conceptRoleRequest.setIdentityRole(directRole.getId());
		conceptRoleRequest.setOperation(ConceptRoleRequestOperation.REMOVE);
		conceptRoleRequestService.save(conceptRoleRequest);
		// add
		conceptRoleRequest = new IdmConceptRoleRequestDto();
		conceptRoleRequest.setRoleRequest(roleRequest.getId());
		conceptRoleRequest.setIdentityContract(contract.getId());
		conceptRoleRequest.setRole(other.getId());
		conceptRoleRequest.setOperation(ConceptRoleRequestOperation.ADD);
		conceptRoleRequestService.save(conceptRoleRequest);
		// execute
		getHelper().executeRequest(roleRequest, false);
		//
		// check after role request is executed
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(2, assignedRoles.size());
		//
		// check updated account
		AccAccountDto updatedAccount = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNotNull(updatedAccount);
		Assert.assertNotNull(getHelper().findResource(updatedAccount.getRealUid()));
		Assert.assertEquals(account.getCreated(), updatedAccount.getCreated());
		Assert.assertEquals(account.getRealUid(), updatedAccount.getRealUid());
		//
		// check provisioning archive
		executedOperations = provisioningArchiveService.find(archiveFilter, null).getContent();
		Assert.assertEquals(2, executedOperations.size());
		Assert.assertTrue(executedOperations.stream().anyMatch(o -> o.getOperationType() == ProvisioningEventType.CREATE));
		Assert.assertTrue(executedOperations.stream().anyMatch(o -> o.getOperationType() == ProvisioningEventType.UPDATE));
	}
	
	protected TestHelper getHelper() {
		return (TestHelper) super.getHelper();
	}
}
