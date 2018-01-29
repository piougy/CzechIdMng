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
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.AbstractCoreWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Test change permissions for identity
 * 
 * @author svandav
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ChangeIdentityPermissionTest extends AbstractCoreWorkflowIntegrationTest {

	private static final String APPROVE_ROLE_BY_GUARANTEE_KEY = "approve-role-by-guarantee";
	private static final String SECURITY_ROLE_TEST = "securityRoleTest";
	private static final String APPROVE_BY_HELPDESK_ROLE = "idm.sec.core.wf.approval.helpdesk.role";
	private static final String APPROVE_BY_SECURITY_ENABLE = "idm.sec.core.wf.approval.security.enabled";
	private static final String APPROVE_BY_MANAGER_ENABLE = "idm.sec.core.wf.approval.manager.enabled";
	private static final String APPROVE_BY_USERMANAGER_ENABLE = "idm.sec.core.wf.approval.usermanager.enabled";
	private static final String APPROVE_BY_HELPDESK_ENABLE = "idm.sec.core.wf.approval.helpdesk.enabled";
	private static final String APPROVE_BY_SECURITY_ROLE = "idm.sec.core.wf.approval.security.role";
	//
	@Autowired private TestHelper helper;
	@Autowired private WorkflowTaskInstanceService workflowTaskInstanceService;
	@Autowired private WorkflowHistoricProcessInstanceService workflowHistoricProcessInstanceService;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmConfigurationService configurationService;
	@Autowired private SecurityService securityService;

	@Before
	public void login() {
		super.loginAsAdmin(InitTestData.TEST_USER_1);
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "true");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "true");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "true");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "true");
	}
	
	@After
	public void logout() {
		super.logout();
	}

	@Test
	@Transactional
	public void addSuperAdminRoleTest() {
		loginAsAdmin(InitTestData.TEST_USER_1);
		IdmIdentityDto test1 = identityService.getByUsername(InitTestData.TEST_USER_1);
		IdmRoleDto adminRole = roleService.getByCode(InitTestData.TEST_ADMIN_ROLE);
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());
		
		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);
		
		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);
		
		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());
		
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());
		
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// MANAGER
		loginAsAdmin(InitTestData.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// USER MANAGER
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// SECURITY
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());
	}
	
	@Test
	@Transactional
	public void addSuperAdminRoleSkipTest() {
		// We are logged as admin. By default is all approve tasks assigned to Admin. All this tasks will be skipped.
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		IdmIdentityDto test1 = identityService.getByUsername(InitTestData.TEST_USER_1);
		IdmRoleDto adminRole = roleService.getByCode(InitTestData.TEST_ADMIN_ROLE);
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());
		
		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);
		
		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);
		
		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());
		
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());
		
		// HELPDESK - must be skipped
		// MANAGER
		loginAsAdmin(InitTestData.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// USER MANAGER
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// SECURITY - must be skipped
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());
	}
	
	@Test
	@Transactional
	public void addSuperAdminRoleDisapproveTest() {
		// We are logged as admin. By default is all approve tasks assigned to Admin. All this tasks will be skipped.
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		IdmIdentityDto test1 = identityService.getByUsername(InitTestData.TEST_USER_1);
		IdmRoleDto adminRole = roleService.getByCode(InitTestData.TEST_ADMIN_ROLE);
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());
		
		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);
		
		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);
		
		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());
		
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());
		
		// HELPDESK - must be skipped
		// MANAGER
		loginAsAdmin(InitTestData.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "disapprove");
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.DISAPPROVED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertEquals(null, concept.getWfProcessId());
	}
	
	
	@Test
	@Transactional
	public void addSuperAdminRoleWithSubprocessTest() {
		
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		IdmIdentityDto test1 = identityService.getByUsername(InitTestData.TEST_USER_1);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestData.TEST_USER_2);
		
		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleService.getByCode(InitTestData.TEST_ADMIN_ROLE);
		adminRole.setPriority(priority);		
		IdmRoleGuaranteeDto guarantee = new IdmRoleGuaranteeDto();
		guarantee.setRole(adminRole.getId());
		guarantee.setGuarantee(test2.getId());
		adminRole.getGuarantees().add(guarantee);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX+priority, APPROVE_ROLE_BY_GUARANTEE_KEY);
		
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());
		
		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);
		
		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);
		
		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());
		
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());
		
		// HELPDESK - must be skipped
		// MANAGER
		loginAsAdmin(InitTestData.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// USER MANAGER
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// Subprocess - approve by GUARANTEE
		loginAsAdmin(InitTestData.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");

		// SECURITY 
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());
	}
	
	
	@Test
	@Transactional
	public void addSuperAdminRoleApproveBySecurityTest() {
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "true");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		// Set security role test
		configurationService.setValue(APPROVE_BY_SECURITY_ROLE, SECURITY_ROLE_TEST);
		// Create test role for load candidates on security department (TEST_USER_1)
		IdmRoleDto role = new IdmRoleDto();
		role.setName(SECURITY_ROLE_TEST);
		role = roleService.save(role);
		helper.createIdentityRole(identityService.getByUsername(InitTestData.TEST_USER_1), role);
		
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		IdmIdentityDto test1 = identityService.getByUsername(InitTestData.TEST_USER_1);
		IdmRoleDto adminRole = roleService.getByCode(InitTestData.TEST_ADMIN_ROLE);
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());
		
		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);
		
		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);
		
		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());
		
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// HELPDESK 	turn off
		// MANAGER 		turn off
		// USER MANAGER	turn off
		// SECURITY
		loginAsAdmin(InitTestData.TEST_USER_1);
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_USER_1);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());
	}

	@Test
	@Transactional
	public void testTaskCount() {
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "true");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		//
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		IdmIdentityDto test1 = helper.createIdentity();
		IdmIdentityDto guarantee = helper.createIdentity();
		
		// Guarantee
		int priority = 500;
		IdmRoleDto role = helper.createRole();
		role.setPriority(priority);		
		IdmRoleGuaranteeDto roleGuarantee = new IdmRoleGuaranteeDto();
		roleGuarantee.setRole(role.getId());
		roleGuarantee.setGuarantee(guarantee.getId());
		role.getGuarantees().add(roleGuarantee);
		role = roleService.save(role);
		// set approve by guarantee
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority, APPROVE_ROLE_BY_GUARANTEE_KEY);

		// helpdesk role and identity
		IdmRoleDto helpdeskRole = helper.createRole();
		IdmIdentityDto helpdeskIdentity = helper.createIdentity();
		// add role dirctly
		helper.createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());
		
		IdmIdentityContractDto contract = helper.getPrimeContract(test1.getId());
		
		// check task before create request
		loginAsAdmin(test1.getUsername());
		int taskCount = getHistoricProcess().size();
		
		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);
		
		IdmConceptRoleRequestDto concept = createRoleConcept(role, contract, request);
		concept = conceptRoleRequestService.save(concept);
		
		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());
		
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());
		
		// check tasks after create request, must be +1
		loginAsAdmin(test1.getUsername());
		int taksCountAfter = getHistoricProcess().size();
		assertEquals(taskCount + 1, taksCountAfter);
		
		// HELPDESK
		loginAsAdmin(helpdeskIdentity.getUsername());
		taskFilter.setCandidateOrAssigned(helpdeskIdentity.getUsername());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");

		// check tasks by identity, must be + 2 (main process + sub process)
		loginAsAdmin(test1.getUsername());
		taksCountAfter = getHistoricProcess().size();
		assertEquals(taskCount + 2, taksCountAfter);
		
		// Subprocess - approve by GUARANTEE
		loginAsAdmin(guarantee.getUsername());
		taskFilter.setCandidateOrAssigned(guarantee.getUsername());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");

		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());
		
		// check task on the end (same as before)
		loginAsAdmin(test1.getUsername());
		taksCountAfter = getHistoricProcess().size();
		assertEquals(taskCount + 2, taksCountAfter);
	}
	
	/**
	 * Return {@link WorkflowHistoricProcessInstanceDto} for current logged user
	 * 
	 * @return
	 */
	private List<WorkflowHistoricProcessInstanceDto> getHistoricProcess() {
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		return workflowHistoricProcessInstanceService.find(taskFilter, null).getContent();
	}

	private IdmConceptRoleRequestDto createRoleConcept(IdmRoleDto adminRole, IdmIdentityContractDto contract,
			IdmRoleRequestDto request) {
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRoleRequest(request.getId());
		concept.setOperation(ConceptRoleRequestOperation.ADD);
		concept.setRole(adminRole.getId());
		concept.setIdentityContract(contract.getId());
		return concept;
	}

	private IdmRoleRequestDto createRoleRequest(IdmIdentityDto test1) {
		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(test1.getId());
		request.setExecuteImmediately(false);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		return request;
	}

	private void checkAndCompleteOneTask(WorkflowFilterDto taskFilter, String user, String decision) {
		IdmIdentityDto identity = identityService.getByUsername(user);
		List<WorkflowTaskInstanceDto> tasks;
		tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService.search(taskFilter).getResources();
		assertEquals(1, tasks.size());
		assertEquals(identity.getId().toString(), tasks.get(0).getApplicant());
		
		workflowTaskInstanceService.completeTask(tasks.get(0).getId(), decision);
	}
}
