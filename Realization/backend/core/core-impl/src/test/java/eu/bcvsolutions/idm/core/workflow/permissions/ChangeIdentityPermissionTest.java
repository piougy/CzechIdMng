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
import eu.bcvsolutions.idm.core.TestHelper;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
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

	private static final String APPROVE_ROLE_BY_GUARANTEE_KEY = "approve-role-by-guarantee";
	private static final String SECURITY_ROLE_TEST = "securityRoleTest";
	private static final String APPROVE_BY_SECURITY_ENABLE = "idm.sec.core.wf.approval.security.enabled";
	private static final String APPROVE_BY_MANAGER_ENABLE = "idm.sec.core.wf.approval.manager.enabled";
	private static final String APPROVE_BY_USERMANAGER_ENABLE = "idm.sec.core.wf.approval.usermanager.enabled";
	private static final String APPROVE_BY_HELPDESK_ENABLE = "idm.sec.core.wf.approval.helpdesk.enabled";
	private static final String APPROVE_BY_SECURITY_ROLE = "idm.sec.core.wf.approval.security.role";
	
	@Autowired
	private TestHelper helper;
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
	@Autowired
	private IdmConfigurationService configurationService;

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
		IdmIdentity test1 = identityService.getByName(InitTestData.TEST_USER_1);
		IdmRole adminRole = roleService.getByName(InitTestData.TEST_ADMIN_ROLE);
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());
		
		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);
		
		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);
		
		roleRequestService.startRequestInternal(request.getId(), true);
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
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());
		
		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);
		
		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);
		
		roleRequestService.startRequestInternal(request.getId(), true);
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
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());
		
		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);
		
		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);
		
		roleRequestService.startRequestInternal(request.getId(), true);
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
		
		// Guarantee
		int priority = 500;
		IdmRole adminRole = roleService.getByName(InitTestData.TEST_ADMIN_ROLE);
		adminRole.setPriority(priority);		
		IdmRoleGuarantee guarantee = new IdmRoleGuarantee();
		guarantee.setRole(adminRole);
		guarantee.setGuarantee(test2);
		adminRole.getGuarantees().add(guarantee);
		roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX+priority, APPROVE_ROLE_BY_GUARANTEE_KEY);
		
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());
		
		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);
		
		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);
		
		roleRequestService.startRequestInternal(request.getId(), true);
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
		// Subprocess - approve by GUARANTEE
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
		IdmRole role = new IdmRole();
		role.setName(SECURITY_ROLE_TEST);
		roleService.save(role);
		helper.createIdentityRole(identityService.getByName(InitTestData.TEST_USER_1), role);
		
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		IdmIdentity test1 = identityService.getByName(InitTestData.TEST_USER_1);
		IdmRole adminRole = roleService.getByName(InitTestData.TEST_ADMIN_ROLE);
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());
		
		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);
		
		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);
		
		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.getDto(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());
		
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		// HELPDESK 	turn off
		// MANAGER 		turn off
		// USER MANAGER	turn off
		// SECURITY
		loginAsAdmin(InitTestData.TEST_USER_1);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		
		request = roleRequestService.getDto(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.getDto(concept.getId());
		assertNotNull(concept.getWfProcessId());
	}


	private IdmConceptRoleRequestDto createRoleConcept(IdmRole adminRole, IdmIdentityContractDto contract,
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
