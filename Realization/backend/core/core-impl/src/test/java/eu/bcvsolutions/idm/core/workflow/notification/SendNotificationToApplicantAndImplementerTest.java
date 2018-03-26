package eu.bcvsolutions.idm.core.workflow.notification;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import eu.bcvsolutions.idm.InitApplicationData;
import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.AbstractCoreWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.workflow.config.WorkflowConfig;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Test for request role notification. Testing if notification is send right with dependency on two boolean flags and if applicant == implementer
 * 
 * @author Patrik Stloukal
 *
 */
@SuppressWarnings("deprecation")
public class SendNotificationToApplicantAndImplementerTest extends AbstractCoreWorkflowIntegrationTest {

	private static final String APPROVE_BY_SECURITY_ENABLE = "idm.sec.core.wf.approval.security.enabled";
	private static final String APPROVE_BY_MANAGER_ENABLE = "idm.sec.core.wf.approval.manager.enabled";
	private static final String APPROVE_BY_USERMANAGER_ENABLE = "idm.sec.core.wf.approval.usermanager.enabled";
	private static final String APPROVE_BY_HELPDESK_ENABLE = "idm.sec.core.wf.approval.helpdesk.enabled";
	private static String SENT_TO_APPLICANT = "idm.sec.core.wf.notification.applicant.enabled";
	private static String SENT_TO_IMPLEMENTER = "idm.sec.core.wf.notification.implementer.enabled";
	private IdmIdentityDto testUser2;
	private IdmTreeNodeDto organization;

	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private IdmNotificationLogService notificationLogService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private TestHelper helper;
	@Autowired
	private WorkflowTaskInstanceService workflowTaskInstanceService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmTreeNodeService treeNodeService;
	@Autowired
	private IdmTreeTypeService treeTypeService;
	@Autowired
	private IdmContractGuaranteeService contractGuaranteeService;

	@Before
	public void login() {
		super.loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		//
		helper.setConfigurationValue(WorkflowConfig.SEND_NOTIFICATION_CONFIGURATION_PROPERTY, true);
		helper.setConfigurationValue(APPROVE_BY_SECURITY_ENABLE, true);
		helper.setConfigurationValue(APPROVE_BY_MANAGER_ENABLE, true);
		helper.setConfigurationValue(APPROVE_BY_HELPDESK_ENABLE, true);
		helper.setConfigurationValue(APPROVE_BY_USERMANAGER_ENABLE, true);
		createStructure();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void requestRejectedByHelpdeskApplicantImplementerSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "disapprove");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_DISAPPROVE_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestRejectedByHelpdeskApplicantImplementerNotSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "disapprove");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_DISAPPROVE_IDENTITY_ROLES, notifications.get(0).getTopic());

		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_DISAPPROVE_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestRejectedByHelpdeskApplicantSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "disapprove");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_DISAPPROVE_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestRejectedByHelpdeskApplicantNotSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "disapprove");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_DISAPPROVE_IDENTITY_ROLES, notifications.get(0).getTopic());

		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
	}

	@Test
	public void requestRejectedByHelpdeskImplementerSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "disapprove");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_DISAPPROVE_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestRejectedByHelpdeskImplementerNotSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "disapprove");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());

		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_DISAPPROVE_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestRejectedByHelpdeskSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "disapprove");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
	}

	@Test
	public void requestRejectedByHelpdeskNotSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "disapprove");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
	}

	@Test
	public void requestApprovedApplicantImplementerSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin(testUser2.getUsername());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// SECURITY
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_CHANGE_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestApprovedApplicantImplementerNotSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin(testUser2.getUsername());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// SECURITY
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_CHANGE_IDENTITY_ROLES, notifications.get(0).getTopic());

		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_CHANGE_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestApprovedApplicantSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin(testUser2.getUsername());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// SECURITY
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_CHANGE_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestApprovedApplicantNotSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin(testUser2.getUsername());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// SECURITY
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_CHANGE_IDENTITY_ROLES, notifications.get(0).getTopic());

		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
	}

	@Test
	public void requestApprovedImplementerSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin(testUser2.getUsername());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// SECURITY
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_CHANGE_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestApprovedImplementerNotSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin(testUser2.getUsername());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// SECURITY
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());

		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_CHANGE_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestApprovedSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin(testUser2.getUsername());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// SECURITY
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
	}

	@Test
	public void requestApprovedNotSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin(testUser2.getUsername());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// SECURITY
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
	}

	@Test
	public void requestReturnedByHelpdeskApplicantImplementerSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "backToApplicant");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_RETURN_REQUEST_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestReturnedByHelpdeskApplicantImplementerNotSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "backToApplicant");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_RETURN_REQUEST_IDENTITY_ROLES, notifications.get(0).getTopic());

		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_RETURN_REQUEST_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestReturnedByHelpdeskApplicantSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "backToApplicant");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_RETURN_REQUEST_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestReturnedByHelpdeskApplicantNotSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "backToApplicant");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_RETURN_REQUEST_IDENTITY_ROLES, notifications.get(0).getTopic());

		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
	}

	@Test
	public void requestReturnedByHelpdeskImplementerSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "backToApplicant");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_RETURN_REQUEST_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestReturnedByHelpdeskImplementerNotSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "backToApplicant");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());

		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_RETURN_REQUEST_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestReturnedByHelpdeskSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "backToApplicant");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
	}

	@Test
	public void requestReturnedByHelpdeskNotSameTest() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole("test_role" + System.currentTimeMillis());
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.search(taskFilter).getResources();
		assertEquals(0, tasks.size());

		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "backToApplicant");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
		
		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
	}

	/**
	 * Creates concept role request for assign role to identity
	 * 
	 * @param IdmRoleDto, IdmIdentityContractDto, IdmRoleRequestDto
	 * @return IdmConceptRoleRequestDto
	 */
	private IdmConceptRoleRequestDto createRoleConcept(IdmRoleDto adminRole, IdmIdentityContractDto contract,
			IdmRoleRequestDto request) {
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRoleRequest(request.getId());
		concept.setOperation(ConceptRoleRequestOperation.ADD);
		concept.setRole(adminRole.getId());
		concept.setIdentityContract(contract.getId());
		return concept;
	}

	/**
	 * Creates request for identity
	 * 
	 * @param IdmIdentityDto
	 * @return IdmRoleRequestDto
	 */
	private IdmRoleRequestDto createRoleRequest(IdmIdentityDto test1) {
		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(test1.getId());
		request.setExecuteImmediately(false);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		return request;
	}

	/**
	 * Completes one task (as helpdesk/manager/user manager/security)
	 * 
	 * @param taskFilter, userName, decision
	 */
	private void checkAndCompleteOneTask(WorkflowFilterDto taskFilter, String userName, String decision) {
		IdmIdentityDto identity = identityService.getByUsername(userName);
		List<WorkflowTaskInstanceDto> tasks;
		tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService.search(taskFilter).getResources();
		assertEquals(1, tasks.size());
		assertEquals(identity.getId().toString(), tasks.get(0).getApplicant());

		workflowTaskInstanceService.completeTask(tasks.get(0).getId(), decision);
	}

	/**
	 * Creates role
	 * 
	 * @param name
	 * @return IdmRoleDto
	 */
	private IdmRoleDto createRole(String name) {
		IdmRoleDto role = helper.createRole();
		role.setName(name);
		role.setId(UUID.randomUUID());
		role.setCanBeRequested(true);
		roleService.save(role);

		return role;
	}

	/**
	 * Creates testUser with working position and contract
	 * 
	 * @return IdmIdentityDto
	 */
	private IdmIdentityDto createTestUser() {
		IdmIdentityDto testUser = new IdmIdentityDto();
		testUser.setUsername("" + System.currentTimeMillis());
		testUser.setPassword(new GuardedString("heslo"));
		testUser.setFirstName("Test");
		testUser.setLastName("User");
		testUser.setEmail(testUser.getUsername() + "@bscsolutions.eu");
		testUser = this.identityService.save(testUser);

		IdmIdentityContractDto identityWorkPosition2 = new IdmIdentityContractDto();
		identityWorkPosition2.setIdentity(testUser.getId());
		identityWorkPosition2.setWorkPosition(organization.getId());
		identityWorkPosition2 = identityContractService.save(identityWorkPosition2);

		IdmContractGuaranteeDto contractGuarantee = new IdmContractGuaranteeDto();
		contractGuarantee.setIdentityContract(identityWorkPosition2.getId());
		contractGuarantee.setGuarantee(testUser2.getId());
		contractGuaranteeService.save(contractGuarantee);
		return testUser;
	}
	/**
	 * Creates organization's structure and identity testUser2 as manager
	 * 
	 */
	private void createStructure() {
		IdmRoleDto superAdminRole = this.roleService.getByCode(InitApplicationData.ADMIN_ROLE);
		IdmTreeNodeDto rootOrganization = treeNodeService.findRoots((UUID) null, new PageRequest(0, 1)).getContent()
				.get(0);

		IdmRoleDto role2 = new IdmRoleDto();
		role2.setName("TestCustomRole002" + System.currentTimeMillis());
		List<IdmRoleCompositionDto> subRoles = new ArrayList<>();
		subRoles.add(new IdmRoleCompositionDto(role2.getId(), superAdminRole.getId()));
		role2.setSubRoles(subRoles);
		role2 = this.roleService.save(role2);

		testUser2 = new IdmIdentityDto();
		testUser2.setUsername("Test_user_Manager" + System.currentTimeMillis());
		testUser2.setPassword(new GuardedString("heslo"));
		testUser2.setFirstName("Test");
		testUser2.setLastName("Second User");
		testUser2.setEmail("test2@bscsolutions.eu");
		testUser2 = this.identityService.save(testUser2);

		IdmTreeTypeDto type = treeTypeService.get(rootOrganization.getTreeType());

		organization = new IdmTreeNodeDto();
		organization.setCode("test" + System.currentTimeMillis());
		organization.setName("Organization Test Notification");
		organization.setParent(rootOrganization.getId());
		organization.setTreeType(type.getId());
		organization = this.treeNodeService.save(organization);
	}

}
