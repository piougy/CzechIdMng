package eu.bcvsolutions.idm.core.workflow.permissions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.AbstractCoreWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.ResolvedIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmIncompatibleRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRequestIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitTestDataProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricTaskInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;

/**
 * Test change permissions for identity
 *
 * @author svandav
 *
 */
@Transactional
public class ChangeIdentityPermissionTest extends AbstractCoreWorkflowIntegrationTest {

	private static final String APPROVE_ROLE_BY_GUARANTEE_KEY = "approve-role-by-guarantee";
	private static final String APPROVE_ROLE_BY_SECURITY_KEY = "approve-role-by-guarantee-security";
	public static final String APPROVE_ROLE_BY_MANAGER_KEY = "approve-role-by-manager";
	private static final String APPROVE_REMOVE_ROLE_BY_MANAGER_KEY = "approve-remove-role-by-manager";
	private static final String SECURITY_ROLE_TEST = "securityRoleTest";
	// FIXME: move to api (workflow config constant)
	private static final String APPROVE_BY_HELPDESK_ROLE = "idm.sec.core.wf.approval.helpdesk.role";
	public static final String APPROVE_BY_SECURITY_ENABLE = "idm.sec.core.wf.approval.security.enabled";
	public static final String APPROVE_BY_MANAGER_ENABLE = "idm.sec.core.wf.approval.manager.enabled";
	public static final String APPROVE_BY_USERMANAGER_ENABLE = "idm.sec.core.wf.approval.usermanager.enabled";
	public static final String APPROVE_BY_HELPDESK_ENABLE = "idm.sec.core.wf.approval.helpdesk.enabled";
	private static final String APPROVE_BY_SECURITY_ROLE = "idm.sec.core.wf.approval.security.role";
	public static final String APPROVE_INCOMPATIBLE_ENABLE = "idm.sec.core.wf.approval.incompatibility.enabled";
	private static final String APPROVE_INCOMPATIBLE_ROLE = "idm.sec.core.wf.approval.incompatibility.role";
	private static final String INCOMPATIBILITY_ROLE_TEST = "IncompatibilityRoleTest";
	//
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
	private RoleConfiguration roleConfiguration;
	@Autowired
	private IdmConfigurationService configurationService;
	@Autowired
	private SecurityService securityService;
	@Autowired
	private IdmIncompatibleRoleService incompatibleRoleService;
	@Autowired
	private IdmRequestIdentityRoleService requestIdentityRoleService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private LoginService loginService;
	@Autowired
	private WorkflowHistoricTaskInstanceService workflowHistoricTaskInstanceService;

	@Before
	public void init() {
		getHelper().setConfigurationValue(APPROVE_BY_SECURITY_ENABLE, true);
		getHelper().setConfigurationValue(APPROVE_BY_MANAGER_ENABLE, true);
		getHelper().setConfigurationValue(APPROVE_BY_HELPDESK_ENABLE, true);
		getHelper().setConfigurationValue(APPROVE_BY_USERMANAGER_ENABLE, true);
	}

	@After
	public void logout() {
		getHelper().setConfigurationValue(APPROVE_BY_SECURITY_ENABLE, false);
		getHelper().setConfigurationValue(APPROVE_BY_MANAGER_ENABLE, false);
		getHelper().setConfigurationValue(APPROVE_BY_HELPDESK_ENABLE, false);
		getHelper().setConfigurationValue(APPROVE_BY_USERMANAGER_ENABLE, false);
		//
		super.logout();
	}

	@Test
	public void addSuperAdminRoleTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		loginAsAdmin(InitTestDataProcessor.TEST_USER_1);
		IdmIdentityDto test1 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1);

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		// HELPDESK
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// MANAGER
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// SECURITY
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");

		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());
	}

	@Test
	public void addSuperAdminRoleSkipTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		// We are logged as admin. By default is all approve tasks assigned to Admin.
		// All this tasks will be skipped.
		loginAsAdmin();
		IdmIdentityDto test1 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// HELPDESK - must be skipped
		// MANAGER
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// SECURITY - must be skipped
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());
	}

	@Test
	public void addSuperAdminRoleDisapproveTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		// We are logged as admin. By default is all approve tasks assigned to Admin.
		// All this tasks will be skipped.
		loginAsAdmin();
		IdmIdentityDto test1 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// HELPDESK - must be skipped
		// MANAGER
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "disapprove");
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.DISAPPROVED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertEquals(null, concept.getWfProcessId());
	}

	@Test
	public void addSuperAdminRoleWithSubprocessTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		loginAsAdmin();
		IdmIdentityDto test1 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_GUARANTEE_KEY);

		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// HELPDESK - must be skipped
		// MANAGER
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// Subprocess - approve by GUARANTEE
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");

		// SECURITY
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");

		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());
	}

	@Test
	public void addSuperAdminRoleApproveBySecurityTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);
		
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "true");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		// Set security role test
		configurationService.setValue(APPROVE_BY_SECURITY_ROLE, SECURITY_ROLE_TEST);
		// Create test role for load candidates on security department (TEST_USER_1)
		IdmRoleDto role = getHelper().createRole(SECURITY_ROLE_TEST);
		getHelper().createIdentityRole(identityService.getByUsername(InitTestDataProcessor.TEST_USER_1), role);

		loginAsAdmin();
		IdmIdentityDto test1 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// HELPDESK turn off
		// MANAGER turn off
		// USER MANAGER turn off
		// SECURITY
		loginAsAdmin(InitTestDataProcessor.TEST_USER_1);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_1);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");

		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());
	}

	@Test
	public void approveIncompatibleRolesTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_INCOMPATIBLE_ENABLE, "true");
		// Set incompatibility role test
		configurationService.setValue(APPROVE_INCOMPATIBLE_ROLE, INCOMPATIBILITY_ROLE_TEST);
		// Create test role for load candidates on approval incompatibility (TEST_USER_1)
		IdmRoleDto role = getHelper().createRole(INCOMPATIBILITY_ROLE_TEST);
		getHelper().createIdentityRole(identityService.getByUsername(InitTestDataProcessor.TEST_USER_1), role);

		IdmIdentityDto applicant = getHelper().createIdentity();

		// Create definition of incompatible roles
		IdmRoleDto incompatibleRoleOne = getHelper().createRole();
		IdmRoleDto incompatibleRoleTwo = getHelper().createRole();
		IdmIncompatibleRoleDto incompatibleRole = new IdmIncompatibleRoleDto();
		incompatibleRole.setSub(incompatibleRoleOne.getId());
		incompatibleRole.setSuperior(incompatibleRoleTwo.getId());
		incompatibleRole = incompatibleRoleService.save(incompatibleRole);

		// Assign first incompatible role
		getHelper().createIdentityRole(applicant, incompatibleRoleOne);
		loginAsAdmin();

		// Create request
		IdmRoleRequestDto request = createRoleRequest(applicant);
		request = roleRequestService.save(request);
		IdmIdentityContractDto applicantContract = getHelper().getPrimeContract(applicant.getId());
		IdmConceptRoleRequestDto concept = createRoleConcept(incompatibleRoleTwo, applicantContract, request);
		concept = conceptRoleRequestService.save(concept);
		// Check on incompatible role in the request
		Set<ResolvedIncompatibleRoleDto> incompatibleRoles = roleRequestService.getIncompatibleRoles(request);
		assertEquals(2, incompatibleRoles.size());

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// HELPDESK turn off
		// MANAGER turn off
		// USER MANAGER turn off
		// SECURITY turn off
		// ROLE INCOMPATIBILITY turn on
		loginAsAdmin(InitTestDataProcessor.TEST_USER_1);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_1);
		checkAndCompleteOneTask(taskFilter, applicant.getUsername(), "approve", "approveIncompatibilities");

		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());
		// Find all identity roles for applicant
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(applicant.getId());
		boolean exists = identityRoles.stream()
			.filter(identityRole -> incompatibleRoleTwo.getId().equals(identityRole.getRole()))
			.findFirst()
			.isPresent();
		// Incompatible role two must be assigned for applicant
		assertTrue(exists);

		// Create next request
		IdmRoleRequestDto requestNext = createRoleRequest(applicant);
		requestNext = roleRequestService.save(requestNext);
		// Check on incompatible role in the request
		// Incompatibilities exist for this user, but not in this request (none concept
		// added new role is presents)
		incompatibleRoles = roleRequestService.getIncompatibleRoles(requestNext);
		assertEquals(0, incompatibleRoles.size());

	}

	@Test
	public void defaultWithoutApproveTest() {
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_INCOMPATIBLE_ENABLE, "false");
		// Set incompatibility role test
		configurationService.setValue(APPROVE_INCOMPATIBLE_ROLE, INCOMPATIBILITY_ROLE_TEST);
		// Create test role for load candidates on approval incompatibility (TEST_USER_1)
		IdmRoleDto role = getHelper().createRole(INCOMPATIBILITY_ROLE_TEST);
		getHelper().createIdentityRole(identityService.getByUsername(InitTestDataProcessor.TEST_USER_1), role);

		IdmIdentityDto applicant = getHelper().createIdentity();

		// Create definition of incompatible roles
		IdmRoleDto incompatibleRoleOne = getHelper().createRole();
		IdmRoleDto incompatibleRoleTwo = getHelper().createRole();
		IdmIncompatibleRoleDto incompatibleRole = new IdmIncompatibleRoleDto();
		incompatibleRole.setSub(incompatibleRoleOne.getId());
		incompatibleRole.setSuperior(incompatibleRoleTwo.getId());
		incompatibleRole = incompatibleRoleService.save(incompatibleRole);

		// Assign first incompatible role
		getHelper().createIdentityRole(applicant, incompatibleRoleOne);
		loginAsAdmin();

		// Create request
		IdmRoleRequestDto request = createRoleRequest(applicant);
		request = roleRequestService.save(request);
		IdmIdentityContractDto applicantContract = getHelper().getPrimeContract(applicant.getId());
		IdmConceptRoleRequestDto concept = createRoleConcept(incompatibleRoleTwo, applicantContract, request);
		concept = conceptRoleRequestService.save(concept);
		// Check on incompatible role in the request
		Set<ResolvedIncompatibleRoleDto> incompatibleRoles = roleRequestService.getIncompatibleRoles(request);
		assertEquals(2, incompatibleRoles.size());

		// HELPDESK turn off
		// MANAGER turn off
		// USER MANAGER turn off
		// SECURITY turn off
		// ROLE INCOMPATIBILITY turn off
		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
	}

	@Test
	public void testTaskCount() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "true");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		//
		loginAsAdmin();
		IdmIdentityDto test1 = getHelper().createIdentity();
		IdmIdentityDto guarantee = getHelper().createIdentity();

		// Guarantee
		int priority = 500;
		IdmRoleDto role = getHelper().createRole();
		role.setPriority(priority);
		role = roleService.save(role);
		getHelper().createRoleGuarantee(role, guarantee);
		//
		// set approve by guarantee
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_GUARANTEE_KEY);

		// helpdesk role and identity
		IdmRoleDto helpdeskRole = getHelper().createRole();
		IdmIdentityDto helpdeskIdentity = getHelper().createIdentity();
		// add role directly
		getHelper().createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());

		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());

		// check task before create request
		loginAsAdmin(test1.getUsername());
		int taskCount = getHistoricProcess(now).size();

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// check tasks after create request, must be +1
		loginAsAdmin(test1.getUsername());
		int taksCountAfter = getHistoricProcess(now).size();
		assertEquals(taskCount + 1, taksCountAfter);
		// Check count of historic processes
		assertEquals(taksCountAfter, getHistoricProcessCount(now));

		// HELPDESK
		loginAsAdmin(helpdeskIdentity.getUsername());
		taskFilter.setCandidateOrAssigned(helpdeskIdentity.getUsername());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");

		// check tasks by identity, must be + 2 (main process + sub process)
		loginAsAdmin(test1.getUsername());
		taksCountAfter = getHistoricProcess(now).size();
		assertEquals(taskCount + 2, taksCountAfter);
		// Check count of historic processes
		assertEquals(taksCountAfter, getHistoricProcessCount(now));

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
		taksCountAfter = getHistoricProcess(now).size();
		assertEquals(taskCount + 2, taksCountAfter);
		// Check count of historic processes
		assertEquals(taksCountAfter, getHistoricProcessCount(now));
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
		IdmIdentityDto test1 = getHelper().createIdentity();
		//
		IdmRoleDto role = getHelper().createRole();
		//
		// helpdesk role and identity
		IdmRoleDto helpdeskRole = getHelper().createRole();
		IdmIdentityDto helpdeskIdentity = getHelper().createIdentity();
		// add role directly
		getHelper().createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());

		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());

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
		IdmIdentityDto test1 = getHelper().createIdentity();
		IdmIdentityDto test2 = getHelper().createIdentity();
		//
		IdmRoleDto role = getHelper().createRole();
		//
		// helpdesk role and identity
		IdmRoleDto helpdeskRole = getHelper().createRole();
		IdmIdentityDto helpdeskIdentity = getHelper().createIdentity();
		// add role directly
		getHelper().createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());

		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());

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
		IdmIdentityDto test1 = getHelper().createIdentity();
		IdmIdentityDto guarantee = getHelper().createIdentity();

		// Guarantee
		int priority = 500;
		IdmRoleDto role = getHelper().createRole();
		role.setPriority(priority);
		getHelper().createRoleGuarantee(role, guarantee);
		role = roleService.save(role);
		// set approve by guarantee
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_GUARANTEE_KEY);
		//
		// helpdesk role and identity
		IdmRoleDto helpdeskRole = getHelper().createRole();
		IdmIdentityDto helpdeskIdentity = getHelper().createIdentity();
		// add role directly
		getHelper().createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());

		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());

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
	public void testGetTaskByAnotherUser() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "true");
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		//
		loginAsAdmin();
		IdmIdentityDto test1 = getHelper().createIdentity();
		IdmIdentityDto anotherUser = getHelper().createIdentity();

		IdmRoleDto role = getHelper().createRole();

		// helpdesk role and identity
		IdmRoleDto helpdeskRole = getHelper().createRole();
		IdmIdentityDto helpdeskIdentity = getHelper().createIdentity();
		// add role directly
		getHelper().createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());

		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());

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
		taskFilter.setCreatedAfter(now);
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
		loginWithout(InitTestDataProcessor.TEST_ADMIN_USERNAME, IdmGroupPermission.APP_ADMIN);
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
	public void addSuperAdminRoleWithSubprocessSecurityTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		IdmIdentityDto test1 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_SECURITY_KEY);
		getHelper().setConfigurationValue(APPROVE_BY_SECURITY_ENABLE, true);

		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// Help Desk
		request = roleRequestService.get(request.getId());
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// User Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// Role Guarantee - subprocess
		request = roleRequestService.get(request.getId());
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// Security - subprocess
		request = roleRequestService.get(request.getId());
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// Security
		request = roleRequestService.get(request.getId());
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");

		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());
	}

	@Test
	public void addSuperAdminRoleWithSubprocessManagerTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		loginAsAdmin();
		IdmIdentityDto test1 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_MANAGER_KEY);

		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// HELPDESK - must be skipped
		// MANAGER
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// Subprocess - approve by Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");

		// SECURITY
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");

		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());
	}

	@Test
	public void addSuperAdminRoleWithSubprocessDisapproveTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		loginAsAdmin();
		IdmIdentityDto test1 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_GUARANTEE_KEY);

		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// HELPDESK - must be skipped
		// MANAGER
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// Subprocess - approve by GUARANTEE
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "disapprove");

		// SECURITY
		request = roleRequestService.get(request.getId());
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");

		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertNotNull(request.getWfProcessId());
		concept = conceptRoleRequestService.get(concept.getId());
		assertNotNull(concept.getWfProcessId());

		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityId(test1.getId());
		Page<IdmIdentityRoleDto> page = identityRoleService.find(filter, null);
		assertEquals(0, page.getTotalElements());
	}

	@Test
	public void addSuperAdminRoleWithSubprocessRemoveTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		IdmIdentityDto test1 = getHelper().createIdentity("TestUser" + System.currentTimeMillis());
		IdmIdentityDto test2 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);

		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());
		IdmRoleDto adminRole = getHelper().createRole();
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
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		// HELPDESK
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// SECURITY
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
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

		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		// Guarantee
		int priority = 500;
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);

		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + (priority + priority),
				APPROVE_REMOVE_ROLE_BY_MANAGER_KEY);
		IdmRoleRequestDto requestRemove = createRoleRequest(test1);
		requestRemove = roleRequestService.save(requestRemove);

		// Mock set of validity - only for test if created remove concept doesn't have filled validity
		contract.setValidFrom(LocalDate.now());
		contract.setValidTill(LocalDate.now());
		IdmConceptRoleRequestDto conceptRemove = createRoleRemoveConcept(page.getContent().get(0).getId(), adminRole,
				contract, requestRemove);
		conceptRemove = conceptRoleRequestService.save(conceptRemove);

		// Remove concept should have not set a validity.
		assertNull(conceptRemove.getValidFrom());
		assertNull(conceptRemove.getValidTill());

		roleRequestService.startRequestInternal(requestRemove.getId(), true);
		requestRemove = roleRequestService.get(requestRemove.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, requestRemove.getState());

		WorkflowFilterDto taskRemoveFilter = new WorkflowFilterDto();
		taskRemoveFilter.setCreatedAfter(now);

		// HELPDESK
		requestRemove = roleRequestService.get(requestRemove.getId());
		loginAsAdmin();
		taskRemoveFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskRemoveFilter, test1.getUsername(), "approve");
		// MANAGER
		requestRemove = roleRequestService.get(requestRemove.getId());
		loginAsAdmin();
		taskRemoveFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskRemoveFilter, test1.getUsername(), "approve");
		// USER MANAGER
		requestRemove = roleRequestService.get(requestRemove.getId());
		loginAsAdmin();
		taskRemoveFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskRemoveFilter, test1.getUsername(), "approve");
		// Subprocess - approve by GUARANTEE
		requestRemove = roleRequestService.get(requestRemove.getId());
		loginAsAdmin();
		taskRemoveFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskRemoveFilter, test1.getUsername(), "approve");
		// SECURITY
		requestRemove = roleRequestService.get(requestRemove.getId());
		loginAsAdmin();
		taskRemoveFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
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
	public void cancelWfOnRoleRequestDeleteTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		// We are logged as admin. By default is all approve tasks assigned to Admin.
		// All this tasks will be skipped.
		loginAsAdmin();
		IdmIdentityDto test1 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// HELPDESK - must be skipped
		// MANAGER
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
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
	public void cancelSubprocessOnContractDeleteTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		loginAsAdmin();
		IdmIdentityDto test1 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_MANAGER_KEY);

		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// HELPDESK - must be skipped
		// MANAGER
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// Subprocess - approve by Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
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
	public void cancelSubprocessOnRoleDeleteTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		loginAsAdmin();
		IdmIdentityDto test1 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		IdmIdentityDto test2 = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, test2);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_MANAGER_KEY);

		IdmIdentityContractDto contract = getHelper().getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		// HELPDESK - must be skipped
		// MANAGER
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");
		// Subprocess - approve by Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
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

	@Test
	public void testFindCandidatesWithoutSubprocess() {
		// approve only by help desk
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "true");
		//
		loginAsAdmin();
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmRoleDto role = getHelper().createRole();
		//
		// helpdesk role and identity
		IdmRoleDto helpdeskRole = getHelper().createRole();
		IdmIdentityDto helpdeskIdentity = getHelper().createIdentity();
		// add role directly
		getHelper().createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());

		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());

		loginAsNoAdmin(identity.getUsername());
		IdmRoleRequestDto request = createRoleRequest(identity);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		IdmRequestIdentityRoleFilter requestIdentityRoleFilter = new IdmRequestIdentityRoleFilter();
		requestIdentityRoleFilter.setIncludeCandidates(true);
		requestIdentityRoleFilter.setRoleRequestId(request.getId());
		requestIdentityRoleFilter.setIdentityId(identity.getId());
		List<IdmRequestIdentityRoleDto> requestIdentityRoles = requestIdentityRoleService.find(requestIdentityRoleFilter, null).getContent();
		assertEquals(1, requestIdentityRoles.size());
		IdmRequestIdentityRoleDto requestIdentityRoleDto = requestIdentityRoles.get(0);
		assertNull(requestIdentityRoleDto.getCandidates());

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		Set<IdmIdentityDto> candidates = workflowProcessInstanceService.getApproversForProcess(request.getWfProcessId());
		assertEquals(1, candidates.size());

		candidates = workflowProcessInstanceService.getApproversForSubprocess(request.getWfProcessId());
		assertEquals(0, candidates.size());

		requestIdentityRoleFilter = new IdmRequestIdentityRoleFilter();
		requestIdentityRoleFilter.setIncludeCandidates(true);
		requestIdentityRoleFilter.setRoleRequestId(request.getId());
		requestIdentityRoleFilter.setIdentityId(identity.getId());
		requestIdentityRoles = requestIdentityRoleService.find(requestIdentityRoleFilter, null).getContent();
		assertEquals(1, requestIdentityRoles.size());
		requestIdentityRoleDto = requestIdentityRoles.get(0);
		assertNull(requestIdentityRoleDto.getCandidates());
	}

	@Test
	public void testFindCandidatesWithSubprocess() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		getHelper().waitForResult(null, 1, 1);

		// approve only by help desk
		configurationService.setValue(APPROVE_BY_USERMANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_SECURITY_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_MANAGER_ENABLE, "false");
		configurationService.setValue(APPROVE_BY_HELPDESK_ENABLE, "true");

		loginAsAdmin();

		// helpdesk role and identity
		IdmRoleDto helpdeskRole = getHelper().createRole();
		IdmIdentityDto helpdeskIdentity = getHelper().createIdentity();
		// add role directly
		getHelper().createIdentityRole(helpdeskIdentity, helpdeskRole);
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());

		IdmIdentityDto identity = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		IdmIdentityDto guarantee = identityService.getByUsername(InitTestDataProcessor.TEST_USER_2);

		// Guarantee
		int priority = 500;
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		adminRole.setPriority(priority);
		getHelper().createRoleGuarantee(adminRole, guarantee);
		adminRole = roleService.save(adminRole);
		configurationService.setValue(IdmRoleService.WF_BY_ROLE_PRIORITY_PREFIX + priority,
				APPROVE_ROLE_BY_MANAGER_KEY);

		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());

		IdmRoleRequestDto request = createRoleRequest(identity);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
		concept = conceptRoleRequestService.save(concept);

		IdmRequestIdentityRoleFilter requestIdentityRoleFilter = new IdmRequestIdentityRoleFilter();
		requestIdentityRoleFilter.setIncludeCandidates(true);
		requestIdentityRoleFilter.setRoleRequestId(request.getId());
		requestIdentityRoleFilter.setIdentityId(identity.getId());
		List<IdmRequestIdentityRoleDto> requestIdentityRoles = requestIdentityRoleService.find(requestIdentityRoleFilter, null).getContent();
		assertEquals(1, requestIdentityRoles.size());
		IdmRequestIdentityRoleDto requestIdentityRoleDto = requestIdentityRoles.get(0);
		assertNull(requestIdentityRoleDto.getCandidates());

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCreatedAfter(now);
		taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());

		Set<IdmIdentityDto> candidates = workflowProcessInstanceService.getApproversForProcess(request.getWfProcessId());
		assertEquals(1, candidates.size());

		candidates = workflowProcessInstanceService.getApproversForSubprocess(request.getWfProcessId());
		assertEquals(0, candidates.size());

		requestIdentityRoleFilter = new IdmRequestIdentityRoleFilter();
		requestIdentityRoleFilter.setIncludeCandidates(true);
		requestIdentityRoleFilter.setRoleRequestId(request.getId());
		requestIdentityRoleFilter.setIdentityId(identity.getId());
		requestIdentityRoles = requestIdentityRoleService.find(requestIdentityRoleFilter, null).getContent();
		assertEquals(1, requestIdentityRoles.size());
		requestIdentityRoleDto = requestIdentityRoles.get(0);
		assertNull(requestIdentityRoleDto.getCandidates());

		IdmRoleRequestFilter filter = new IdmRoleRequestFilter();
		filter.setIncludeApprovers(true);
		IdmRoleRequestDto requestDto = roleRequestService.get(request.getId(), filter);
		assertEquals(1, requestDto.getApprovers().size());

		// HELPDESK
		loginAsAdmin(helpdeskIdentity.getUsername());
		taskFilter.setCandidateOrAssigned(helpdeskIdentity.getUsername());
		checkAndCompleteOneTask(taskFilter, InitTestDataProcessor.TEST_USER_1, "approve");

		filter.setIncludeApprovers(false);
		requestDto = roleRequestService.get(request.getId(), filter);
		assertNull(requestDto.getApprovers());

		// Subprocess - approve by Manager
		request = roleRequestService.get(request.getId());
		loginAsAdmin(guarantee.getUsername());
		taskFilter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		concept = conceptRoleRequestService.get(concept.getId());

		String conceptWf = concept.getWfProcessId();
		assertNotNull(conceptWf);
		assertNotNull(workflowProcessInstanceService.get(conceptWf));

		candidates = workflowProcessInstanceService.getApproversForProcess(request.getWfProcessId());
		assertEquals(1, candidates.size());
		IdmIdentityDto approversFromProcess = candidates.stream().findFirst().get();

		candidates = workflowProcessInstanceService.getApproversForSubprocess(request.getWfProcessId());
		assertEquals(1, candidates.size());
		IdmIdentityDto approversFromSubProcess = candidates.stream().findFirst().get();
		assertEquals(approversFromProcess.getId(), approversFromSubProcess.getId());

		requestIdentityRoleFilter = new IdmRequestIdentityRoleFilter();
		requestIdentityRoleFilter.setIncludeCandidates(true);
		requestIdentityRoleFilter.setRoleRequestId(request.getId());
		requestIdentityRoleFilter.setIdentityId(identity.getId());
		requestIdentityRoles = requestIdentityRoleService.find(requestIdentityRoleFilter, null).getContent();
		assertEquals(1, requestIdentityRoles.size());
		requestIdentityRoleDto = requestIdentityRoles.get(0);
		assertEquals(1, requestIdentityRoleDto.getCandidates().size());

		requestIdentityRoleFilter.setIncludeCandidates(false);
		requestIdentityRoles = requestIdentityRoleService.find(requestIdentityRoleFilter, null).getContent();
		assertEquals(1, requestIdentityRoles.size());
		requestIdentityRoleDto = requestIdentityRoles.get(0);
		assertNull(requestIdentityRoleDto.getCandidates());

		filter = new IdmRoleRequestFilter();
		filter.setIncludeApprovers(true);
		requestDto = roleRequestService.get(request.getId(), filter);
		assertEquals(1, requestDto.getApprovers().size());

		filter.setIncludeApprovers(false);
		requestDto = roleRequestService.get(request.getId(), filter);
		assertNull(requestDto.getApprovers());
	}

	@Test
	public void testAccessIsAddedForOwnerAndImplementerToSubprocesses() {
		// reset approvers
		getHelper().setConfigurationValue(APPROVE_BY_USERMANAGER_ENABLE, false);
		getHelper().setConfigurationValue(APPROVE_BY_SECURITY_ENABLE, false);
		getHelper().setConfigurationValue(APPROVE_BY_MANAGER_ENABLE, false);
		getHelper().setConfigurationValue(APPROVE_BY_HELPDESK_ENABLE, false);
		// role with guarantees and critical 2 => approve by guarantee
		IdmRoleDto role = new IdmRoleDto();
		role.setCode(getHelper().createName());
		role.setPriority(2); // default by configuration
		IdmRoleDto roleOne = roleService.save(role);
		role = new IdmRoleDto();
		role.setCode(getHelper().createName());
		role.setPriority(2); // default by configuration
		IdmRoleDto roleTwo = roleService.save(role);
		//
		IdmIdentityDto implementer = getHelper().createIdentity();
		IdmIdentityDto applicant = getHelper().createIdentity();
		IdmIdentityContractDto applicantContract = getHelper().getPrimeContract(applicant);
		IdmIdentityDto guaranteeOne = getHelper().createIdentity();
		IdmIdentityDto guaranteeTwo = getHelper().createIdentity();
		//
		getHelper().createRoleGuarantee(roleOne, guaranteeOne);
		getHelper().createRoleGuarantee(roleTwo, guaranteeTwo);
		//
		loginAsAdmin(implementer.getUsername()); // login as implementer
		//
		IdmRoleRequestDto request = createRoleRequest(applicant);
		request = roleRequestService.save(request);
		IdmConceptRoleRequestDto concept = createRoleConcept(roleOne, applicantContract, request);
		conceptRoleRequestService.save(concept);
		concept = createRoleConcept(roleTwo, applicantContract, request);
		conceptRoleRequestService.save(concept);
		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		Assert.assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		IdmRequestIdentityRoleFilter requestIdentityRoleFilter = new IdmRequestIdentityRoleFilter();
		requestIdentityRoleFilter.setIncludeCandidates(true);
		requestIdentityRoleFilter.setRoleRequestId(request.getId());
		requestIdentityRoleFilter.setIdentityId(applicant.getId());
		List<IdmRequestIdentityRoleDto> requestIdentityRoles = requestIdentityRoleService.find(requestIdentityRoleFilter, null).getContent();
		Assert.assertEquals(2, requestIdentityRoles.size());
		Assert.assertTrue(requestIdentityRoles.stream().anyMatch(rir -> rir.getRole().equals(roleOne.getId())
				&& rir.getCandidates().size() == 1
				&& rir.getCandidates().iterator().next().getId().equals(guaranteeOne.getId())));
		Assert.assertTrue(requestIdentityRoles.stream().anyMatch(rir -> rir.getRole().equals(roleTwo.getId())
				&& rir.getCandidates().size() == 1
				&& rir.getCandidates().iterator().next().getId().equals(guaranteeTwo.getId())));
		//
		// check applicant and implemented can read process instance
		getHelper().login(implementer);
		List<WorkflowProcessInstanceDto> processes = workflowProcessInstanceService.find(new WorkflowFilterDto(), null, IdmBasePermission.READ).getContent();
		Assert.assertEquals(3, processes.size());
		getHelper().login(applicant);
		Assert.assertEquals(3, workflowProcessInstanceService.find(new WorkflowFilterDto(), null, IdmBasePermission.READ).getTotalElements());
		getHelper().login(guaranteeOne);
		Assert.assertEquals(1, workflowProcessInstanceService.find(new WorkflowFilterDto(), null, IdmBasePermission.READ).getTotalElements());
		getHelper().login(guaranteeTwo);
		Assert.assertEquals(1, workflowProcessInstanceService.find(new WorkflowFilterDto(), null, IdmBasePermission.READ).getTotalElements());
		//
		// test identity links are created (=> access added)
		processes.forEach(process -> {
			List<IdentityLink> links = runtimeService.getIdentityLinksForProcessInstance(process.getProcessInstanceId());
			Assert.assertTrue(links.stream().anyMatch(l -> l.getUserId().equals(implementer.getId().toString())
					&& l.getType().equals(IdentityLinkType.STARTER)));
			Assert.assertTrue(links.stream().anyMatch(l -> l.getUserId().equals(applicant.getId().toString())
					&& l.getType().equals(IdentityLinkType.OWNER)));
		});
	}
	
	@Test
	public void testSwitchUserAuditVariables() {
		IdmIdentityDto adminOriginal = getHelper().createIdentity();
		IdmIdentityDto adminSwitched = getHelper().createIdentity();
		IdmIdentityDto adminHelpdesk = getHelper().createIdentity();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		getHelper().createIdentityRole(adminOriginal, adminRole);
		getHelper().createIdentityRole(adminSwitched, adminRole);
		IdmRoleDto helpdeskRole = getHelper().createRole();
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());
		getHelper().createIdentityRole(adminHelpdesk, helpdeskRole);
		IdmRoleRequestDto request = createRoleRequest(identity);
		//
		try {
			getHelper().login(adminOriginal);
			loginService.switchUser(adminSwitched);
			//
			IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
			request = roleRequestService.save(request);
			IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
			concept = conceptRoleRequestService.save(concept);
			roleRequestService.startRequestInternal(request.getId(), true);
			request = roleRequestService.get(request.getId());
			assertEquals(RoleRequestState.IN_PROGRESS, request.getState());
			//
			// check original identity is filled in process variables
			WorkflowProcessInstanceDto workflowProcessInstanceDto = workflowProcessInstanceService.get(request.getWfProcessId());
			Assert.assertEquals(adminSwitched.getId().toString(), workflowProcessInstanceDto.getProcessVariables().get(WorkflowProcessInstanceService.IMPLEMENTER_IDENTIFIER));
			Assert.assertEquals(adminOriginal.getId().toString(), workflowProcessInstanceDto.getProcessVariables().get(WorkflowProcessInstanceService.ORIGINAL_IMPLEMENTER_IDENTIFIER));
		} finally {
			logout();
		}		
		//
		try {
			getHelper().login(adminOriginal);
			loginService.switchUser(adminHelpdesk);
			//
			// complete the first task (~helpdesk)
			WorkflowFilterDto taskFilter = new WorkflowFilterDto();
			taskFilter.setCandidateOrAssigned(securityService.getCurrentUsername());
			List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
			Assert.assertEquals(1, tasks.size());
			Assert.assertEquals(identity.getId().toString(), tasks.get(0).getApplicant());
			workflowTaskInstanceService.completeTask(tasks.get(0).getId(), "approve");
			//
			// check original identity is filled in task variables
			WorkflowHistoricTaskInstanceDto workflowHistoricTaskInstanceDto = workflowHistoricTaskInstanceService.get(tasks.get(0).getId());
			Assert.assertEquals(adminHelpdesk.getId().toString(), workflowHistoricTaskInstanceDto.getAssignee());
			Assert.assertEquals(adminOriginal.getId(), workflowHistoricTaskInstanceDto.getVariables().get(WorkflowProcessInstanceService.ORIGINAL_IMPLEMENTER_IDENTIFIER));
		} finally {
			logout();
		}
	}
	
	@Test
	public void testSwitchUserDeleteProcess() {
		IdmIdentityDto adminOriginal = getHelper().createIdentity();
		IdmIdentityDto adminSwitched = getHelper().createIdentity();
		IdmIdentityDto adminHelpdesk = getHelper().createIdentity();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto adminRole = roleConfiguration.getAdminRole();
		getHelper().createIdentityRole(adminOriginal, adminRole);
		getHelper().createIdentityRole(adminSwitched, adminRole);
		IdmRoleDto helpdeskRole = getHelper().createRole();
		configurationService.setValue(APPROVE_BY_HELPDESK_ROLE, helpdeskRole.getCode());
		getHelper().createIdentityRole(adminHelpdesk, helpdeskRole);
		IdmRoleRequestDto request = createRoleRequest(identity);
		//
		try {
			getHelper().login(adminOriginal);
			loginService.switchUser(adminSwitched);
			//
			IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
			request = roleRequestService.save(request);
			IdmConceptRoleRequestDto concept = createRoleConcept(adminRole, contract, request);
			concept = conceptRoleRequestService.save(concept);
			roleRequestService.startRequestInternal(request.getId(), true);
			request = roleRequestService.get(request.getId());
			assertEquals(RoleRequestState.IN_PROGRESS, request.getState());
			//
			// check original identity is filled in process variables
			WorkflowProcessInstanceDto workflowProcessInstanceDto = workflowProcessInstanceService.get(request.getWfProcessId());
			Assert.assertEquals(adminSwitched.getId().toString(), workflowProcessInstanceDto.getProcessVariables().get(WorkflowProcessInstanceService.IMPLEMENTER_IDENTIFIER));
			Assert.assertEquals(adminOriginal.getId().toString(), workflowProcessInstanceDto.getProcessVariables().get(WorkflowProcessInstanceService.ORIGINAL_IMPLEMENTER_IDENTIFIER));
		} finally {
			logout();
		}		
		//
		try {
			getHelper().login(adminOriginal);
			loginService.switchUser(adminHelpdesk);
			//
			// delete process
			request = roleRequestService.get(request.getId());
			Assert.assertEquals(RoleRequestState.IN_PROGRESS, request.getState());
			workflowProcessInstanceService.delete(request.getWfProcessId(), null);
			WorkflowHistoricProcessInstanceDto workflowHistoricProcessInstanceDto = workflowHistoricProcessInstanceService.get(request.getWfProcessId());
			Assert.assertTrue(workflowHistoricProcessInstanceDto.getDeleteReason().contains(adminOriginal.getUsername()));
			Assert.assertTrue(workflowHistoricProcessInstanceDto.getDeleteReason().contains(adminHelpdesk.getUsername()));
		} finally {
			logout();
		}
	}

	/**
	 * Return {@link WorkflowHistoricProcessInstanceDto} for current logged user
	 *
	 * @return
	 */
	private List<WorkflowHistoricProcessInstanceDto> getHistoricProcess(ZonedDateTime from) {
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(from);
		return workflowHistoricProcessInstanceService.find(taskFilter, null).getContent();
	}
	
	/**
	 * Return count of historic processes for current logged user
	 *
	 * @return
	 */
	private long getHistoricProcessCount(ZonedDateTime from) {
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(from);
		return workflowHistoricProcessInstanceService.count(taskFilter);
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
		this.checkAndCompleteOneTask(taskFilter, user, decision, null);
	}

	private void checkAndCompleteOneTask(WorkflowFilterDto taskFilter, String user, String decision, String userTaskId) {
		IdmIdentityDto identity = identityService.getByUsername(user);
		List<WorkflowTaskInstanceDto> tasks;
		tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		if (userTaskId != null) {
			assertEquals(userTaskId,  tasks.get(0).getDefinition().getId());
		}
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
