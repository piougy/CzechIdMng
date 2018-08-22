package eu.bcvsolutions.idm.core.workflow.permissions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.AbstractCoreWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
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
	private static final String APPROVE_ROLE_BY_SECURITY_KEY = "approve-role-by-guarantee-security";
	private static final String APPROVE_ROLE_BY_MANAGER_KEY = "approve-role-by-manager";
	private static final String APPROVE_REMOVE_ROLE_BY_MANAGER_KEY = "approve-remove-role-by-manager";
	private static final String SECURITY_ROLE_TEST = "securityRoleTest";
	private static final String APPROVE_BY_HELPDESK_ROLE = "idm.sec.core.wf.approval.helpdesk.role";
	private static final String APPROVE_BY_SECURITY_ENABLE = "idm.sec.core.wf.approval.security.enabled";
	private static final String APPROVE_BY_MANAGER_ENABLE = "idm.sec.core.wf.approval.manager.enabled";
	private static final String APPROVE_BY_USERMANAGER_ENABLE = "idm.sec.core.wf.approval.usermanager.enabled";
	private static final String APPROVE_BY_HELPDESK_ENABLE = "idm.sec.core.wf.approval.helpdesk.enabled";
	private static final String APPROVE_BY_SECURITY_ROLE = "idm.sec.core.wf.approval.security.role";
	//
	@Autowired
	private TestHelper helper;
	@Autowired
	private WorkflowTaskInstanceService workflowTaskInstanceService;
	@Autowired
	private WorkflowHistoricProcessInstanceService workflowHistoricProcessInstanceService;
	@Autowired
	private WorkflowProcessInstanceService workflowProcessInstanceService;
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
	@Autowired
	private SecurityService securityService;

	@Autowired
	private IdmIdentityRoleService identityRoleService;

	@Before
	public void login() {
		super.loginAsAdmin();
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

		loginAsAdmin();
		// HELPDESK
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// MANAGER
		loginAsAdmin(InitTestData.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// USER MANAGER
		loginAsAdmin();
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
		// We are logged as admin. By default is all approve tasks assigned to Admin.
		// All this tasks will be skipped.
		loginAsAdmin();
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
		loginAsAdmin();
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
		// We are logged as admin. By default is all approve tasks assigned to Admin.
		// All this tasks will be skipped.
		loginAsAdmin();
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

		loginAsAdmin();
		IdmIdentityDto test1 = identityService.getByUsername(InitTestData.TEST_USER_1);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestData.TEST_USER_2);

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleService.getByCode(InitTestData.TEST_ADMIN_ROLE);
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_GUARANTEE_KEY);

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
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// Subprocess - approve by GUARANTEE
		loginAsAdmin(InitTestData.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");

		// SECURITY
		loginAsAdmin();
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
		IdmRoleDto role = getHelper().createRole(SECURITY_ROLE_TEST);
		helper.createIdentityRole(identityService.getByUsername(InitTestData.TEST_USER_1), role);

		loginAsAdmin();
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

		// HELPDESK turn off
		// MANAGER turn off
		// USER MANAGER turn off
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
		loginAsAdmin();
		IdmIdentityDto test1 = helper.createIdentity();
		IdmIdentityDto guarantee = helper.createIdentity();

		// Guarantee
		int priority = 500;
		IdmRoleDto role = helper.createRole();
		role.setPriority(priority);
		role = roleService.save(role);
		getHelper().createRoleGuarantee(role, guarantee);
		//
		// set approve by guarantee
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_GUARANTEE_KEY);

		// helpdesk role and identity
		IdmRoleDto helpdeskRole = helper.createRole();
		IdmIdentityDto helpdeskIdentity = helper.createIdentity();
		// add role directly
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

	@Test
	public void testCompleteTaskByStarter() {
		// approve only by help desk
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "true");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		//
		loginAsAdmin();
		IdmIdentityDto test1 = helper.createIdentity();
		//
		IdmRoleDto role = helper.createRole();
		//
		// helpdesk role and identity
		IdmRoleDto helpdeskRole = helper.createRole();
		IdmIdentityDto helpdeskIdentity = helper.createIdentity();
		// add role directly
		helper.createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());

		IdmIdentityContractDto contract = helper.getPrimeContract(test1.getId());

		loginAsNoAdmin(test1.getUsername());
		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		try {
			completeTasksFromUsers(helpdeskIdentity.getUsername(), "approve");
			fail("This user: " + test1.getUsername() + " can't approve task.");
		} catch (ResultCodeException ex) {
			assertTrue(CoreResultCode.FORBIDDEN.name().equals(ex.getError().getError().getStatusEnum()));
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}

		loginAsNoAdmin(helpdeskIdentity.getUsername());
		try {
			completeTasksFromUsers(helpdeskIdentity.getUsername(), "approve");
		} catch (ResultCodeException ex) {
			fail("User has permission to approve task. Error message: " + ex.getLocalizedMessage());
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testCompleteTaskByAnotherUser() {
		// approve only by help desk
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "true");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");

		loginAsAdmin();
		IdmIdentityDto test1 = helper.createIdentity();
		IdmIdentityDto test2 = helper.createIdentity();
		//
		IdmRoleDto role = helper.createRole();
		//
		// helpdesk role and identity
		IdmRoleDto helpdeskRole = helper.createRole();
		IdmIdentityDto helpdeskIdentity = helper.createIdentity();
		// add role directly
		helper.createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());

		IdmIdentityContractDto contract = helper.getPrimeContract(test1.getId());

		loginAsNoAdmin(test1.getUsername());
		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		try {
			completeTasksFromUsers(helpdeskIdentity.getUsername(), "approve");
			fail("This user: " + test1.getUsername() + " can't approve task.");
		} catch (ResultCodeException ex) {
			assertTrue(CoreResultCode.FORBIDDEN.name().equals(ex.getError().getError().getStatusEnum()));
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}

		loginAsNoAdmin(test2.getUsername());
		try {
			completeTasksFromUsers(helpdeskIdentity.getUsername(), "approve");
			fail("This user: " + test1.getUsername() + " can't approve task.");
		} catch (ResultCodeException ex) {
			assertTrue(CoreResultCode.FORBIDDEN.name().equals(ex.getError().getError().getStatusEnum()));
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}
	}

	@Test
	public void testCompleteTaskByPreviosApprover() {
		// approve only by help desk
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "true");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		//
		loginAsAdmin();
		IdmIdentityDto test1 = helper.createIdentity();
		IdmIdentityDto guarantee = helper.createIdentity();

		// Guarantee
		int priority = 500;
		IdmRoleDto role = helper.createRole();
		role.setPriority(priority);
		getHelper().createRoleGuarantee(role, guarantee);
		role = roleService.save(role);
		// set approve by guarantee
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_GUARANTEE_KEY);
		//
		// helpdesk role and identity
		IdmRoleDto helpdeskRole = helper.createRole();
		IdmIdentityDto helpdeskIdentity = helper.createIdentity();
		// add role directly
		helper.createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());

		IdmIdentityContractDto contract = helper.getPrimeContract(test1.getId());

		loginAsNoAdmin(test1.getUsername());
		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		try {
			completeTasksFromUsers(helpdeskIdentity.getUsername(), "approve");
			fail("This user: " + test1.getUsername() + " can't approve task.");
		} catch (ResultCodeException ex) {
			assertTrue(CoreResultCode.FORBIDDEN.name().equals(ex.getError().getError().getStatusEnum()));
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}

		loginAsNoAdmin(helpdeskIdentity.getUsername());
		try {
			completeTasksFromUsers(helpdeskIdentity.getUsername(), "approve");
		} catch (ResultCodeException ex) {
			fail("User has permission to approve task. Error message: " + ex.getLocalizedMessage());
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}

		loginAsNoAdmin(helpdeskIdentity.getUsername());
		try {
			completeTasksFromUsers(guarantee.getUsername(), "approve");
			fail("This user: " + helpdeskIdentity.getUsername() + " can't approve task.");
		} catch (ResultCodeException ex) {
			assertTrue(CoreResultCode.FORBIDDEN.name().equals(ex.getError().getError().getStatusEnum()));
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}

		loginAsNoAdmin(test1.getUsername());
		try {
			completeTasksFromUsers(guarantee.getUsername(), "approve");
			fail("This user: " + test1.getUsername() + " can't approve task.");
		} catch (ResultCodeException ex) {
			assertTrue(CoreResultCode.FORBIDDEN.name().equals(ex.getError().getError().getStatusEnum()));
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}

		loginAsNoAdmin(guarantee.getUsername());
		try {
			completeTasksFromUsers(guarantee.getUsername(), "approve");
		} catch (ResultCodeException ex) {
			fail("User has permission to approve task. Error message: " + ex.getLocalizedMessage());
		} catch (Exception e) {
			fail("Some problem: " + e.getLocalizedMessage());
		}
	}

	@Test
	@Transactional
	public void testGetTaskByAnotherUser() {
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "true");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		//
		loginAsAdmin();
		IdmIdentityDto test1 = helper.createIdentity();
		IdmIdentityDto anotherUser = helper.createIdentity();

		IdmRoleDto role = helper.createRole();

		// helpdesk role and identity
		IdmRoleDto helpdeskRole = helper.createRole();
		IdmIdentityDto helpdeskIdentity = helper.createIdentity();
		// add role directly
		helper.createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());

		IdmIdentityContractDto contract = helper.getPrimeContract(test1.getId());

		// check task before create request
		loginAsAdmin(test1.getUsername());

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

		// HELPDESK login
		loginAsAdmin(helpdeskIdentity.getUsername());
		taskFilter.setCandidateOrAssigned(helpdeskIdentity.getUsername());
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());

		WorkflowTaskInstanceDto taskInstanceDto = tasks.get(0);
		String id = taskInstanceDto.getId();

		WorkflowTaskInstanceDto workflowTaskInstanceDto = workflowTaskInstanceService.get(id);
		assertNotNull(workflowTaskInstanceDto);

		// check task get by id
		loginWithout(test1.getUsername(), IdmGroupPermission.APP_ADMIN, CoreGroupPermission.WORKFLOW_TASK_ADMIN);
		workflowTaskInstanceDto = workflowTaskInstanceService.get(id);
		assertNull(workflowTaskInstanceDto);

		loginWithout(anotherUser.getUsername(), IdmGroupPermission.APP_ADMIN, CoreGroupPermission.WORKFLOW_TASK_ADMIN);
		workflowTaskInstanceDto = workflowTaskInstanceService.get(id);
		assertNull(workflowTaskInstanceDto);

		// candidate
		loginWithout(helpdeskIdentity.getUsername(), IdmGroupPermission.APP_ADMIN,
				CoreGroupPermission.WORKFLOW_TASK_ADMIN);
		workflowTaskInstanceDto = workflowTaskInstanceService.get(id);
		assertNotNull(workflowTaskInstanceDto);

		// WF admin
		loginWithout(InitTestData.TEST_ADMIN_USERNAME, IdmGroupPermission.APP_ADMIN);
		workflowTaskInstanceDto = workflowTaskInstanceService.get(id);
		assertNotNull(workflowTaskInstanceDto);

		// Attacker
		loginWithout(anotherUser.getUsername(), IdmGroupPermission.APP_ADMIN, CoreGroupPermission.WORKFLOW_TASK_ADMIN);
		taskFilter.setCandidateOrAssigned(helpdeskIdentity.getUsername());
		try {
			tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
			fail();
		} catch (ResultCodeException ex) {
			assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	@Transactional
	public void addSuperAdminRoleWithSubprocessSecurityTest() {

		loginAsAdmin(InitTestData.TEST_USER_2);
		IdmIdentityDto test1 = identityService.getByUsername(InitTestData.TEST_USER_1);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestData.TEST_USER_2);

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleService.getByCode(InitTestData.TEST_ADMIN_ROLE);
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_SECURITY_KEY);
		configurationService.setValue("idm.sec.core.wf.approval.security.enabled", "true");

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

		// Help Desk
		request = roleRequestService.get(request.getId());
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin(InitTestData.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// User Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// Role Guarantee - subprocess
		request = roleRequestService.get(request.getId());
		loginAsAdmin(InitTestData.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// Security - subprocess
		request = roleRequestService.get(request.getId());
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// Security
		request = roleRequestService.get(request.getId());
		loginAsAdmin();
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
	public void addSuperAdminRoleWithSubprocessManagerTest() {

		loginAsAdmin();
		IdmIdentityDto test1 = identityService.getByUsername(InitTestData.TEST_USER_1);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestData.TEST_USER_2);

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleService.getByCode(InitTestData.TEST_ADMIN_ROLE);
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_MANAGER_KEY);

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
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// Subprocess - approve by Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin(InitTestData.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");

		// SECURITY
		loginAsAdmin();
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
	public void addSuperAdminRoleWithSubprocessDisapproveTest() {

		loginAsAdmin();
		IdmIdentityDto test1 = identityService.getByUsername(InitTestData.TEST_USER_1);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestData.TEST_USER_2);

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleService.getByCode(InitTestData.TEST_ADMIN_ROLE);
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_GUARANTEE_KEY);

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
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// Subprocess - approve by GUARANTEE
		loginAsAdmin(InitTestData.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "disapprove");

		// SECURITY
		request = roleRequestService.get(request.getId());
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");

		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());

		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityId(test1.getId());
		Page<IdmIdentityRoleDto> page = identityRoleService.find(filter, null);
		assertEquals(0, page.getSize());
	}

	@Test
	@Transactional
	public void addSuperAdminRoleWithSubprocessRemoveTest() {

		IdmIdentityDto test1 = helper.createIdentity("TestUser" + System.currentTimeMillis());
		IdmIdentityDto test2 = identityService.getByUsername(InitTestData.TEST_USER_2);

		loginAsAdmin(InitTestData.TEST_USER_2);
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());
		IdmRoleDto adminRole = helper.createRole("testRole" + System.currentTimeMillis());
		adminRole.setApproveRemove(true);
		roleService.save(adminRole);

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

		loginAsAdmin();
		// HELPDESK
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// SECURITY
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");

		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());

		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityId(test1.getId());
		Page<IdmIdentityRoleDto> page = identityRoleService.find(filter, null);
		assertEquals(1, page.getContent().size());

		loginAsAdmin(InitTestData.TEST_USER_2);
		// Guarantee
		int priority = 500;
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);

		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + (priority + priority),
				APPROVE_REMOVE_ROLE_BY_MANAGER_KEY);
		IdmRoleRequestDto requestRemove = createRoleRequest(test1);
		requestRemove = roleRequestService.save(requestRemove);

		IdmConceptRoleRequestDto conceptRemove = createRoleRemoveConcept(page.getContent().get(0).getId(), adminRole,
				contract, requestRemove);
		conceptRemove = conceptRoleRequestService.save(conceptRemove);

		roleRequestService.startRequestInternal(requestRemove.getId(), true);
		requestRemove = roleRequestService.get(requestRemove.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, requestRemove.getState());

		WorkflowFilterDto taskRemoveFilter = new WorkflowFilterDto();

		// HELPDESK
		requestRemove = roleRequestService.get(requestRemove.getId());
		loginAsAdmin();
		taskRemoveFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskRemoveFilter, test1.getUsername(), "approve");
		// MANAGER
		requestRemove = roleRequestService.get(requestRemove.getId());
		loginAsAdmin();
		taskRemoveFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskRemoveFilter, test1.getUsername(), "approve");
		// USER MANAGER
		requestRemove = roleRequestService.get(requestRemove.getId());
		loginAsAdmin();
		taskRemoveFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskRemoveFilter, test1.getUsername(), "approve");
		// Subprocess - approve by GUARANTEE
		requestRemove = roleRequestService.get(requestRemove.getId());
		loginAsAdmin();
		taskRemoveFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskRemoveFilter, test1.getUsername(), "approve");
		// SECURITY
		requestRemove = roleRequestService.get(requestRemove.getId());
		loginAsAdmin();
		taskRemoveFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskRemoveFilter, test1.getUsername(), "approve");

		requestRemove = roleRequestService.get(requestRemove.getId());
		assertEquals(RoleRequestState.EXECUTED, requestRemove.getState());
		assertNotNull(requestRemove.getWfProcessId());
		conceptRemove = conceptRoleRequestService.get(conceptRemove.getId());
		assertNotNull(conceptRemove.getWfProcessId());

		IdmIdentityRoleFilter filterRemove = new IdmIdentityRoleFilter();
		filterRemove.setIdentityId(test1.getId());
		Page<IdmIdentityRoleDto> pageRemove = identityRoleService.find(filterRemove, null);
		assertEquals(0, pageRemove.getContent().size());
	}

	@Test
	@Transactional
	public void cancelWfOnRoleRequestDeleteTest() {
		// We are logged as admin. By default is all approve tasks assigned to Admin.
		// All this tasks will be skipped.
		loginAsAdmin();
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
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		concept = conceptRoleRequestService.get(concept.getId());
		String conceptWf = concept.getWfProcessId();
		assertNull(conceptWf);

		request = roleRequestService.get(request.getId());
		String requestWf = request.getWfProcessId();
		assertNotNull(requestWf);
		assertNotNull(workflowProcessInstanceService.get(requestWf));
		// Delete the request
		roleRequestService.delete(request);
		// WF have to be cancelled

		assertNull(roleRequestService.get(request.getId()));
		assertNull(workflowProcessInstanceService.get(requestWf));
	}

	@Test
	@Transactional
	public void cancelSubprocessOnContractDeleteTest() {
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		loginAsAdmin();
		IdmIdentityDto test1 = identityService.getByUsername(InitTestData.TEST_USER_1);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestData.TEST_USER_2);

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleService.getByCode(InitTestData.TEST_ADMIN_ROLE);
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_MANAGER_KEY);

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
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// Subprocess - approve by Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin(InitTestData.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_USER_2);
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		concept = conceptRoleRequestService.get(concept.getId());

		String conceptWf = concept.getWfProcessId();
		assertNotNull(conceptWf);
		assertNotNull(workflowProcessInstanceService.get(conceptWf));
		
		// Delete the contract that is using in the concept
		UUID contractId = concept.getIdentityContract();
		assertNotNull(contractId);
		// Wf process for concept cannot be cancelled (because main process would be frozen ). Process will be disapproved.
		identityContractService.deleteById(contractId);

		// Concept has to be in the Cancel state and WF must be ended
		concept = conceptRoleRequestService.get(concept.getId());
		assertEquals(RoleRequestState.CANCELED, concept.getState());
		assertNotNull(concept.getWfProcessId());
		assertNull(workflowProcessInstanceService.get(conceptWf));
		request = roleRequestService.get(request.getId());
		// Main process has to be executed
		assertEquals(RoleRequestState.EXECUTED, request.getState());
	}
	
	@Test
	@Transactional
	public void cancelSubprocessOnRoleDeleteTest() {

		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		loginAsAdmin();
		IdmIdentityDto test1 = identityService.getByUsername(InitTestData.TEST_USER_1);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestData.TEST_USER_2);

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleService.getByCode(InitTestData.TEST_ADMIN_ROLE);
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_MANAGER_KEY);

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
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestData.TEST_USER_1, "approve");
		// Subprocess - approve by Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin(InitTestData.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestData.TEST_USER_2);
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		concept = conceptRoleRequestService.get(concept.getId());

		String conceptWf = concept.getWfProcessId();
		assertNotNull(conceptWf);
		assertNotNull(workflowProcessInstanceService.get(conceptWf));
		
		// Delete the role that is using in the concept
		UUID roleId = concept.getRole();
		assertNotNull(roleId);
		
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setRoleId(roleId);
		identityRoleService.find(identityRoleFilter, null).getContent().forEach(identityRole -> identityRoleService.delete(identityRole));
		
		// Wf process for concept cannot be cancelled (because main process would be frozen ). Process will be disapproved.
		roleService.deleteById(roleId);

		// Concept has to be in the Cancel state and WF must be ended
		concept = conceptRoleRequestService.get(concept.getId());
		assertEquals(RoleRequestState.CANCELED, concept.getState());
		assertNotNull(concept.getWfProcessId());
		assertNull(workflowProcessInstanceService.get(conceptWf));
		request = roleRequestService.get(request.getId());
		// Main process has to be executed
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		
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

	private IdmConceptRoleRequestDto createRoleRemoveConcept(UUID identityRole, IdmRoleDto adminRole,
			IdmIdentityContractDto contract, IdmRoleRequestDto request) {
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRoleRequest(request.getId());
		concept.setOperation(ConceptRoleRequestOperation.REMOVE);
		concept.setRole(adminRole.getId());
		concept.setIdentityRole(identityRole);
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
		tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		assertEquals(identity.getId().toString(), tasks.get(0).getApplicant());

		workflowTaskInstanceService.completeTask(tasks.get(0).getId(), decision);
	}

	/**
	 * Complete all tasks from user given in parameters. Complete will be done by
	 * currently logged user.
	 * 
	 * @param approverUser
	 * @param decision
	 */
	private void completeTasksFromUsers(String approverUser, String decision) {
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(approverUser);
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		//
		for (WorkflowTaskInstanceDto task : tasks) {
			workflowTaskInstanceService.completeTask(task.getId(), decision);
		}
	}
}
