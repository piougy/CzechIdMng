package eu.bcvsolutions.idm.core.bulk.action.impl.role;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.bulk.action.impl.delegation.ManualWorkflowTaskDelegationBulkAction;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.workflow.model.dto.IdentityLinkDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceAbstractDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.permissions.ChangeIdentityPermissionTest;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;
import org.activiti.engine.task.IdentityLinkType;
import org.assertj.core.util.Sets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Integration tests for {@link ManualWorkflowTaskDelegationBulkAction}
 *
 * @author Vít Švanda
 *
 */
public class ManualWorkflowTaskDelegationBulkActionTest extends AbstractBulkActionTest {

	@Autowired
	private WorkflowTaskInstanceService workflowTaskInstanceService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private IdmNotificationLogService notificationLogService;

	@Before
	public void login() {
		getHelper().setConfigurationValue(ChangeIdentityPermissionTest.APPROVE_BY_SECURITY_ENABLE, false);
		getHelper().setConfigurationValue(ChangeIdentityPermissionTest.APPROVE_BY_MANAGER_ENABLE, false);
		getHelper().setConfigurationValue(ChangeIdentityPermissionTest.APPROVE_BY_HELPDESK_ENABLE, false);
		getHelper().setConfigurationValue(ChangeIdentityPermissionTest.APPROVE_BY_USERMANAGER_ENABLE, false);
		//
		loginAsAdmin();
	}

	@After
	@Override
	public void logout() {
		super.logout();
	}

	@Test
	public void testBulkActionByIds() {
		// Enable approving of a role-request by manager.
		getHelper().setConfigurationValue(ChangeIdentityPermissionTest.APPROVE_BY_MANAGER_ENABLE, true);

		IdmRoleDto roleOne = getHelper().createRole();
		IdmIdentityDto delegator = getHelper().createIdentity();
		IdmIdentityDto subordinate = getHelper().createIdentity();
		IdmIdentityContractDto subordinateContract = getHelper().createIdentityContact(subordinate);
		getHelper().createContractGuarantee(subordinateContract, delegator);

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(delegator.getUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());
		// Assing role -> reuqest will be in progress state.
		IdmRoleRequestDto roleRequest = getHelper().createRoleRequest(subordinateContract, ConceptRoleRequestOperation.ADD, roleOne);
		roleRequest.setExecuteImmediately(false);
		roleRequest = roleRequestService.save(roleRequest);

		roleRequest = getHelper().executeRequest(roleRequest, false, true);
		assertEquals(RoleRequestState.IN_PROGRESS, roleRequest.getState());

		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		List<IdentityLinkDto> identityLinks = tasks.get(0).getIdentityLinks();
		assertEquals(1, identityLinks.size());
		// Task is assigned to the delegator now.
		assertEquals(delegator.getId(), UUID.fromString(identityLinks.get(0).getUserId()));

		IdmBulkActionDto bulkAction = this.findBulkActionForDto(WorkflowTaskInstanceAbstractDto.class, ManualWorkflowTaskDelegationBulkAction.NAME);

		Set<UUID> ids = Sets.newHashSet();
		ids.add(UUID.fromString(tasks.get(0).getId()));

		IdmIdentityDto delegate = getHelper().createIdentity();

		bulkAction.setIdentifiers(ids);
		bulkAction.getProperties().put(ManualWorkflowTaskDelegationBulkAction.DELEGATE_ATTRIBUTE, delegate.getId());
		bulkAction.getProperties().put(ManualWorkflowTaskDelegationBulkAction.CANDIDATE_OR_ASSIGNED_FILTER_FIELD, delegator.getId());
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);

		checkResultLrt(processAction, 1l, null, null);

		WorkflowTaskInstanceDto task = workflowTaskInstanceService.get(tasks.get(0).getId());
		assertNotNull(task);
		identityLinks = task.getIdentityLinks();
		assertEquals(2, identityLinks.size());

		// Check delegator as participant in the task.
		long participantCount = identityLinks.stream().filter(identityLink -> IdentityLinkType.PARTICIPANT.equals(identityLink.getType())
				&& UUID.fromString(identityLink.getUserId()).equals(delegator.getId())).count();
		assertEquals(1, participantCount);

		// Check delegate as candidat in the task;
		long candidateCount = identityLinks.stream().filter(identityLink -> IdentityLinkType.CANDIDATE.equals(identityLink.getType())
				&& UUID.fromString(identityLink.getUserId()).equals(delegate.getId())).count();
		assertEquals(1, candidateCount);
	}

	@Test
	public void testBulkActionCheckDelegateNotification() {
		// Enable approving of a role-request by manager.
		getHelper().setConfigurationValue(ChangeIdentityPermissionTest.APPROVE_BY_MANAGER_ENABLE, true);

		IdmRoleDto roleOne = getHelper().createRole();
		IdmIdentityDto delegator = getHelper().createIdentity();
		IdmIdentityDto subordinate = getHelper().createIdentity();
		IdmIdentityContractDto subordinateContract = getHelper().createIdentityContact(subordinate);
		getHelper().createContractGuarantee(subordinateContract, delegator);

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(delegator.getUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());
		// Assing role -> reuqest will be in progress state.
		IdmRoleRequestDto roleRequest = getHelper().createRoleRequest(subordinateContract, ConceptRoleRequestOperation.ADD, roleOne);
		roleRequest.setExecuteImmediately(false);
		roleRequest = roleRequestService.save(roleRequest);

		roleRequest = getHelper().executeRequest(roleRequest, false, true);
		assertEquals(RoleRequestState.IN_PROGRESS, roleRequest.getState());

		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		List<IdentityLinkDto> identityLinks = tasks.get(0).getIdentityLinks();
		assertEquals(1, identityLinks.size());
		// Task is assigned to the delegator now.
		assertEquals(delegator.getId(), UUID.fromString(identityLinks.get(0).getUserId()));

		IdmBulkActionDto bulkAction = this.findBulkActionForDto(WorkflowTaskInstanceAbstractDto.class, ManualWorkflowTaskDelegationBulkAction.NAME);

		Set<UUID> ids = Sets.newHashSet();
		ids.add(UUID.fromString(tasks.get(0).getId()));

		IdmIdentityDto delegate = getHelper().createIdentity();

		bulkAction.setIdentifiers(ids);
		bulkAction.getProperties().put(ManualWorkflowTaskDelegationBulkAction.DELEGATE_ATTRIBUTE, delegate.getId());
		bulkAction.getProperties().put(ManualWorkflowTaskDelegationBulkAction.CANDIDATE_OR_ASSIGNED_FILTER_FIELD, delegator.getId());
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);

		checkResultLrt(processAction, 1l, null, null);

		// Notification to the delegate should been sent.
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(delegate.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());
		assertEquals(CoreModuleDescriptor.TOPIC_DELEGATION_INSTANCE_CREATED_TO_DELEGATE, notifications.get(0).getTopic());
	}

	@Test
	public void testBulkActionByFilter() {

		// Enable approving of a role-request by manager.
		getHelper().setConfigurationValue(ChangeIdentityPermissionTest.APPROVE_BY_MANAGER_ENABLE, true);

		IdmRoleDto roleOne = getHelper().createRole();
		IdmIdentityDto delegator = getHelper().createIdentity();
		IdmIdentityDto subordinate = getHelper().createIdentity();
		IdmIdentityContractDto subordinateContract = getHelper().createIdentityContact(subordinate);
		getHelper().createContractGuarantee(subordinateContract, delegator);

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(delegator.getUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());
		// Assing role -> reuqest will be in progress state.
		IdmRoleRequestDto roleRequest = getHelper().createRoleRequest(subordinateContract, ConceptRoleRequestOperation.ADD, roleOne);
		roleRequest.setExecuteImmediately(false);
		roleRequest = roleRequestService.save(roleRequest);

		roleRequest = getHelper().executeRequest(roleRequest, false, true);
		assertEquals(RoleRequestState.IN_PROGRESS, roleRequest.getState());

		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		List<IdentityLinkDto> identityLinks = tasks.get(0).getIdentityLinks();
		assertEquals(1, identityLinks.size());
		// Task is assigned to the delegator now.
		assertEquals(delegator.getId(), UUID.fromString(identityLinks.get(0).getUserId()));

		IdmBulkActionDto bulkAction = this.findBulkActionForDto(WorkflowTaskInstanceAbstractDto.class, ManualWorkflowTaskDelegationBulkAction.NAME);

		IdmIdentityDto delegate = getHelper().createIdentity();

		bulkAction.setTransformedFilter(taskFilter);
		bulkAction.setFilter(toMap(taskFilter));
		bulkAction.getProperties().put(ManualWorkflowTaskDelegationBulkAction.DELEGATE_ATTRIBUTE, delegate.getId());
		bulkAction.getProperties().put(ManualWorkflowTaskDelegationBulkAction.CANDIDATE_OR_ASSIGNED_FILTER_FIELD, delegator.getId());
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);

		checkResultLrt(processAction, 1l, null, null);

		WorkflowTaskInstanceDto task = workflowTaskInstanceService.get(tasks.get(0).getId());
		assertNotNull(task);
		identityLinks = task.getIdentityLinks();
		assertEquals(2, identityLinks.size());

		// Check delegator as participant in the task.
		long participantCount = identityLinks.stream().filter(identityLink -> IdentityLinkType.PARTICIPANT.equals(identityLink.getType())
				&& UUID.fromString(identityLink.getUserId()).equals(delegator.getId())).count();
		assertEquals(1, participantCount);

		// Check delegate as candidat in the task;
		long candidateCount = identityLinks.stream().filter(identityLink -> IdentityLinkType.CANDIDATE.equals(identityLink.getType())
				&& UUID.fromString(identityLink.getUserId()).equals(delegate.getId())).count();
		assertEquals(1, candidateCount);
	}

	@Test
	public void testBulkActionWithoutPermission() {

		// Enable approving of a role-request by manager.
		getHelper().setConfigurationValue(ChangeIdentityPermissionTest.APPROVE_BY_MANAGER_ENABLE, true);

		IdmRoleDto roleOne = getHelper().createRole();
		IdmIdentityDto delegator = getHelper().createIdentity();
		IdmIdentityDto subordinate = getHelper().createIdentity();
		IdmIdentityContractDto subordinateContract = getHelper().createIdentityContact(subordinate);
		getHelper().createContractGuarantee(subordinateContract, delegator);

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(delegator.getUsername());
		List<WorkflowTaskInstanceDto> tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(0, tasks.size());
		// Assing role -> reuqest will be in progress state.
		IdmRoleRequestDto roleRequest = getHelper().createRoleRequest(subordinateContract, ConceptRoleRequestOperation.ADD, roleOne);
		roleRequest.setExecuteImmediately(false);
		roleRequest = roleRequestService.save(roleRequest);

		roleRequest = getHelper().executeRequest(roleRequest, false, true);
		assertEquals(RoleRequestState.IN_PROGRESS, roleRequest.getState());

		tasks = workflowTaskInstanceService.find(taskFilter, null).getContent();
		assertEquals(1, tasks.size());
		List<IdentityLinkDto> identityLinks = tasks.get(0).getIdentityLinks();
		assertEquals(1, identityLinks.size());
		// Task is assigned to the delegator now.
		assertEquals(delegator.getId(), UUID.fromString(identityLinks.get(0).getUserId()));

		IdmBulkActionDto bulkAction = this.findBulkActionForDto(WorkflowTaskInstanceAbstractDto.class, ManualWorkflowTaskDelegationBulkAction.NAME);

		Set<UUID> ids = Sets.newHashSet();
		ids.add(UUID.fromString(tasks.get(0).getId()));

		IdmIdentityDto delegate = getHelper().createIdentity();

		// User hasn't permission for read a workflow tasks.
		IdmIdentityDto anotherUser = this.createUserWithAuthorities(IdmBasePermission.READ);
		// Attacker
		loginWithout(anotherUser.getUsername(), IdmGroupPermission.APP_ADMIN, CoreGroupPermission.WORKFLOW_TASK_ADMIN);

		bulkAction.setIdentifiers(ids);
		bulkAction.getProperties().put(ManualWorkflowTaskDelegationBulkAction.DELEGATE_ATTRIBUTE, delegate.getId());
		bulkAction.getProperties().put(ManualWorkflowTaskDelegationBulkAction.CANDIDATE_OR_ASSIGNED_FILTER_FIELD, delegator.getId());
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);

		checkResultLrt(processAction, 0l, 0l, 0l);
	}
}
