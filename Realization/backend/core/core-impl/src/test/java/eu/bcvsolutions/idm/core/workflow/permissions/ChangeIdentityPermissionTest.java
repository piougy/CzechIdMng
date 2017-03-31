package eu.bcvsolutions.idm.core.workflow.permissions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.AbstractWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.model.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.model.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;

/**
 * Test change permissions for identity
 * 
 * @author svandav
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ChangeIdentityPermissionTest extends AbstractWorkflowIntegrationTest {

	private static final String APPROVE_ROLE_BY_AUTHORIZER_KEY = "approve-role-by-authorizer";
	private static final String APPROVE_REMOVE_ROLE_BY_MANAGER_KEY = "approve-remove-role-by-manager";

	@Autowired
	private WorkflowTaskInstanceService workflowTaskInstanceService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired 
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private IdmRoleService roleService;

	@Before
	public void login() {
		super.loginAsAdmin(InitTestData.TEST_USER_1);
	}
	
	@After
	public void logout() {
		super.logout();
	}

	@Test
	@Transactional
	public void addSuperAdminRoleTest() {
		loginAsAdmin(InitTestData.TEST_USER_1);
		IdmIdentity test1 = identityService.getByName(InitTestData.TEST_USER_1);
		IdmRole adminRole = roleService.getByName(InitTestData.TEST_ADMIN_ROLE);
		IdmIdentityContract contract = identityContractService.getPrimeContract(test1);
		
		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);
		
		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);
		
		roleRequestService.startRequest(request.getId());
		request = roleRequestService.getDto(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());
		
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService.search(taskFilter).getResources();
		assertEquals(0, tasks.size());
		
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// MANAGER
		loginAsAdmin(InitTestData.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// USER MANAGER
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// SECURITY
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		
		request = roleRequestService.getDto(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.getDto(concept.getId());
		assertNotNull(concept.getWfProcessId());
	}
	
	@Test
	@Transactional
	public void addSuperAdminRoleSkipTest() {
		// We are logged as admin. By default is all approve tasks assigned to Admin. All this tasks will be skipped.
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		IdmIdentity test1 = identityService.getByName(InitTestData.TEST_USER_1);
		IdmRole adminRole = roleService.getByName(InitTestData.TEST_ADMIN_ROLE);
		IdmIdentityContract contract = identityContractService.getPrimeContract(test1);
		
		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);
		
		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);
		
		roleRequestService.startRequest(request.getId());
		request = roleRequestService.getDto(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());
		
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService.search(taskFilter).getResources();
		assertEquals(0, tasks.size());
		
		// HELPDESK - must be skipped
		// MANAGER
		loginAsAdmin(InitTestData.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// USER MANAGER
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// SECURITY - must be skipped
		
		request = roleRequestService.getDto(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.getDto(concept.getId());
		assertNotNull(concept.getWfProcessId());
	}
	
	@Test
	@Transactional
	public void addSuperAdminRoleDisapproveTest() {
		// We are logged as admin. By default is all approve tasks assigned to Admin. All this tasks will be skipped.
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		IdmIdentity test1 = identityService.getByName(InitTestData.TEST_USER_1);
		IdmRole adminRole = roleService.getByName(InitTestData.TEST_ADMIN_ROLE);
		IdmIdentityContract contract = identityContractService.getPrimeContract(test1);
		
		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);
		
		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);
		
		roleRequestService.startRequest(request.getId());
		request = roleRequestService.getDto(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());
		
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService.search(taskFilter).getResources();
		assertEquals(0, tasks.size());
		
		// HELPDESK - must be skipped
		// MANAGER
		loginAsAdmin(InitTestData.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "disapprove");
		request = roleRequestService.getDto(request.getId());
		assertEquals(RoleRequestState.DISAPPROVED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.getDto(concept.getId());
		assertEquals(null, concept.getWfProcessId());
	}
	
	
	@Test
	@Transactional
	public void addSuperAdminRoleWithSubprocessTest() {
		
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		IdmIdentity test1 = identityService.getByName(InitTestData.TEST_USER_1);
		IdmIdentity test2 = identityService.getByName(InitTestData.TEST_USER_2);
		
		IdmRole adminRole = roleService.getByName(InitTestData.TEST_ADMIN_ROLE);
		adminRole.setApproveAddWorkflow(APPROVE_ROLE_BY_AUTHORIZER_KEY);
		adminRole.setApproveRemoveWorkflow(APPROVE_REMOVE_ROLE_BY_MANAGER_KEY);
		
		// Authorizer
		IdmRoleGuarantee guarantee = new IdmRoleGuarantee();
		guarantee.setRole(adminRole);
		guarantee.setGuarantee(test2);
		adminRole.getGuarantees().add(guarantee);
		roleService.save(adminRole);
		
		IdmIdentityContract contract = identityContractService.getPrimeContract(test1);
		
		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);
		
		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);
		
		roleRequestService.startRequest(request.getId());
		request = roleRequestService.getDto(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());
		
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService.search(taskFilter).getResources();
		assertEquals(0, tasks.size());
		
		// HELPDESK - must be skipped
		// MANAGER
		loginAsAdmin(InitTestData.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// USER MANAGER
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// Subprocess - approve by AUTHORIZER
		loginAsAdmin(InitTestData.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// SECURITY 
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		
		request = roleRequestService.getDto(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.getDto(concept.getId());
		assertNotNull(concept.getWfProcessId());
	}

	private IdmConceptRoleRequestDto createRoleConcept(IdmRole adminRole, IdmIdentityContract contract,
			IdmRoleRequestDto request) {
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRoleRequest(request.getId());
		concept.setOperation(ConceptRoleRequestOperation.ADD);
		concept.setRole(adminRole.getId());
		concept.setIdentityContract(contract.getId());
		return concept;
	}

	private IdmRoleRequestDto createRoleRequest(IdmIdentity test1) {
		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(test1.getId());
		request.setExecuteImmediately(false);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		return request;
	}

	private void checkAndCompleteOneTask(WorkflowFilterDto taskFilter, String user, String decision) {
		List<WorkflowTaskInstanceDto> tasks;
		tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService.search(taskFilter).getResources();
		assertEquals(1, tasks.size());
		assertEquals(user, tasks.get(0).getApplicant());
		
		workflowTaskInstanceService.completeTask(tasks.get(0).getId(), decision);
	}
}
