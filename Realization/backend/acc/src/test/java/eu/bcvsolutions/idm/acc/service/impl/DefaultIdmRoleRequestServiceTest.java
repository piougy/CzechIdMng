package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.SysBlockedOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Acc tests for role-request service
 * 
 * @author Vít Švanda
 *
 */
public class DefaultIdmRoleRequestServiceTest extends AbstractIntegrationTest {

	@Autowired
	private TestHelper helper;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysProvisioningOperationService provisioningOperationService;
	@Autowired
	private ProvisioningExecutor provisioningExecutor;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;

	@Before
	public void login() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testSystemStateExecuted() {
		IdmRoleDto role = helper.createRole();
		SysSystemDto system = helper.createTestResourceSystem(true);
		helper.createRoleSystem(role, system);
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto primeContract = helper.getPrimeContract(identity);
		IdmRoleRequestDto request = helper.assignRoles(primeContract, role);
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.EXECUTED, request.getSystemState().getState());
	}
	
	@Test
	public void testSystemStateBlocked() {
		IdmRoleDto role = helper.createRole();
		SysSystemDto system = helper.createTestResourceSystem(true);
		// Block system operations
		SysBlockedOperationDto blockedOperation = new SysBlockedOperationDto();
		blockedOperation.setCreateOperation(true);
		blockedOperation.setDeleteOperation(true);
		blockedOperation.setUpdateOperation(true);
		system.setBlockedOperation(blockedOperation);
		system = systemService.save(system);
		
		helper.createRoleSystem(role, system);
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto primeContract = helper.getPrimeContract(identity);
		IdmRoleRequestDto request = helper.assignRoles(primeContract, role);
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.BLOCKED, request.getSystemState().getState());
		
		SysProvisioningOperationFilter provisioningFilter = new SysProvisioningOperationFilter();
		provisioningFilter.setSystemId(system.getId());
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(provisioningFilter, null).getContent();
		assertEquals(1, operations.size());
		
		// Unblock system operations
		blockedOperation.setCreateOperation(false);
		blockedOperation.setDeleteOperation(false);
		blockedOperation.setUpdateOperation(false);
		system.setBlockedOperation(blockedOperation);
		system = systemService.save(system);
		
		provisioningExecutor.executeSync(operations.get(0));
		// Load the request
		request = roleRequestService.get(request.getId());
		List<IdmConceptRoleRequestDto> concepts = conceptRoleRequestService.findAllByRoleRequest(request.getId());
		assertEquals(concepts.size(), 1);
		assertNull(concepts.get(0).getSystemState());
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.EXECUTED, request.getSystemState().getState());
	}
	
	@Test
	public void testSystemStateBlockedAndCanceled() {
		IdmRoleDto role = helper.createRole();
		SysSystemDto system = helper.createTestResourceSystem(true);
		// Block system operations
		SysBlockedOperationDto blockedOperation = new SysBlockedOperationDto();
		blockedOperation.setCreateOperation(true);
		blockedOperation.setDeleteOperation(true);
		blockedOperation.setUpdateOperation(true);
		system.setBlockedOperation(blockedOperation);
		system = systemService.save(system);
		
		helper.createRoleSystem(role, system);
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto primeContract = helper.getPrimeContract(identity);
		IdmRoleRequestDto request = helper.assignRoles(primeContract, role);
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.BLOCKED, request.getSystemState().getState());
		
		SysProvisioningOperationFilter provisioningFilter = new SysProvisioningOperationFilter();
		provisioningFilter.setSystemId(system.getId());
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(provisioningFilter, null).getContent();
		assertEquals(1, operations.size());
		
		// Unblock system operations
		blockedOperation.setCreateOperation(false);
		blockedOperation.setDeleteOperation(false);
		blockedOperation.setUpdateOperation(false);
		system.setBlockedOperation(blockedOperation);
		system = systemService.save(system);
		
		provisioningExecutor.cancel(operations.get(0));
		// Load the request
		request = roleRequestService.get(request.getId());
		List<IdmConceptRoleRequestDto> concepts = conceptRoleRequestService.findAllByRoleRequest(request.getId());
		assertEquals(concepts.size(), 1);
		assertEquals(OperationState.CANCELED, concepts.get(0).getSystemState().getState());
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.EXECUTED, request.getSystemState().getState());
	}
	
	@Test
	public void testSystemStateFailedAndCanceled() {
		IdmRoleDto role = helper.createRole();
		SysSystemDto system = helper.createTestResourceSystem(true);
		// Block system operations
		SysBlockedOperationDto blockedOperation = new SysBlockedOperationDto();
		blockedOperation.setCreateOperation(true);
		blockedOperation.setDeleteOperation(true);
		blockedOperation.setUpdateOperation(true);
		system.setBlockedOperation(blockedOperation);
		system = systemService.save(system);
		
		helper.createRoleSystem(role, system);
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto primeContract = helper.getPrimeContract(identity);
		IdmRoleRequestDto request = helper.assignRoles(primeContract, role);
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.BLOCKED, request.getSystemState().getState());
		
		SysProvisioningOperationFilter provisioningFilter = new SysProvisioningOperationFilter();
		provisioningFilter.setSystemId(system.getId());
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(provisioningFilter, null).getContent();
		assertEquals(1, operations.size());
		
		// Simulation of exception - Set blocked operation as failed
		SysProvisioningOperationDto operationDto = operations.get(0);
		operationDto.getResult().setState(OperationState.EXCEPTION);
		operationDto = provisioningOperationService.save(operationDto);
		
		// Refresh system state -> must be in exception now
		request = roleRequestService.refreshSystemState(request);
		request = roleRequestService.save(request);
		assertEquals(OperationState.EXCEPTION, request.getSystemState().getState());
		
		// Unblock system operations
		blockedOperation.setCreateOperation(false);
		blockedOperation.setDeleteOperation(false);
		blockedOperation.setUpdateOperation(false);
		system.setBlockedOperation(blockedOperation);
		system = systemService.save(system);
		
		provisioningExecutor.cancel(operations.get(0));
		// Load the request
		request = roleRequestService.get(request.getId());
		List<IdmConceptRoleRequestDto> concepts = conceptRoleRequestService.findAllByRoleRequest(request.getId());
		assertEquals(concepts.size(), 1);
		assertEquals(OperationState.CANCELED, concepts.get(0).getSystemState().getState());
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.EXECUTED, request.getSystemState().getState());
	}
	
	@Test
	public void testSystemStateNotexecuted() {
		IdmRoleDto role = helper.createRole();
		SysSystemDto system = helper.createTestResourceSystem(true);
		// Set system as read-only
		system.setReadonly(true);
		system = systemService.save(system);
		
		helper.createRoleSystem(role, system);
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto primeContract = helper.getPrimeContract(identity);
		IdmRoleRequestDto request = helper.assignRoles(primeContract, role);
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.NOT_EXECUTED, request.getSystemState().getState());
		
		SysProvisioningOperationFilter provisioningFilter = new SysProvisioningOperationFilter();
		provisioningFilter.setSystemId(system.getId());
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(provisioningFilter, null).getContent();
		assertEquals(1, operations.size());
		
		// Set system to write mode
		system.setReadonly(false);
		system = systemService.save(system);
		
		provisioningExecutor.executeSync(operations.get(0));
		// Load the request
		request = roleRequestService.get(request.getId());
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.EXECUTED, request.getSystemState().getState());
	}
	
	@Test
	public void testSystemStateFailed() {
		IdmRoleDto role = helper.createRole();
		SysSystemDto system = helper.createTestResourceSystem(true);
		// Block system operations
		SysBlockedOperationDto blockedOperation = new SysBlockedOperationDto();
		blockedOperation.setCreateOperation(true);
		blockedOperation.setDeleteOperation(true);
		blockedOperation.setUpdateOperation(true);
		system.setBlockedOperation(blockedOperation);
		system = systemService.save(system);
		
		helper.createRoleSystem(role, system);
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto primeContract = helper.getPrimeContract(identity);
		IdmRoleRequestDto request = helper.assignRoles(primeContract, role);
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.BLOCKED, request.getSystemState().getState());
		
		SysProvisioningOperationFilter provisioningFilter = new SysProvisioningOperationFilter();
		provisioningFilter.setSystemId(system.getId());
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(provisioningFilter, null).getContent();
		assertEquals(1, operations.size());
		
		// Simulation of exception - Set blocked operation as failed
		SysProvisioningOperationDto operationDto = operations.get(0);
		operationDto.getResult().setState(OperationState.EXCEPTION);
		operationDto = provisioningOperationService.save(operationDto);
		
		// Refresh system state -> must be in exception now
		request = roleRequestService.refreshSystemState(request);
		request = roleRequestService.save(request);
		assertEquals(OperationState.EXCEPTION, request.getSystemState().getState());
		
		// Unblock system operations
		blockedOperation.setCreateOperation(false);
		blockedOperation.setDeleteOperation(false);
		blockedOperation.setUpdateOperation(false);
		system.setBlockedOperation(blockedOperation);
		system = systemService.save(system);
		
		provisioningExecutor.executeSync(operations.get(0));
		// Load the request
		request = roleRequestService.get(request.getId());
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.EXECUTED, request.getSystemState().getState());
	}
}
