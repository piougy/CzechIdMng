package eu.bcvsolutions.idm.core.workflow.listener;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmDelegationFilter;
import eu.bcvsolutions.idm.core.api.service.DelegationManager;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.delegation.type.DefaultDelegationType;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import java.util.List;
import java.util.UUID;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.event.ActivitiActivityCancelledEvent;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.Assert;

/**
 * Listener call after task created. Ensures delegation.
 *
 * @author Vít Švanda
 *
 */
public class DelegationEventListener implements ActivitiEventListener {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DelegationEventListener.class);

	@Autowired
	private DelegationManager delegationManager;
	@Autowired
	private IdmDelegationService delegationService;
	@Autowired
	@Lazy
	private TaskService taskService;

	@Override
	public void onEvent(ActivitiEvent event) {
		LOG.debug("DelegationEventListener - recieve event [{}]", event.getType());
		switch (event.getType()) {

		case TASK_ASSIGNED:
		case TASK_CREATED:
			if (event instanceof ActivitiEntityEventImpl
					&& ((ActivitiEntityEventImpl) event).getEntity() instanceof TaskEntity) {
				TaskEntity taskEntity = (TaskEntity) ((ActivitiEntityEventImpl) event).getEntity();
				if (taskEntity != null && taskEntity.getCandidates() != null) {
					String processDefinitionId = taskEntity.getProcessDefinitionId();
					String processDelegationType = delegationManager.getProcessDelegationType(processDefinitionId);
					if (processDelegationType == null) {
						// If process doesn't have a delegation type defined, then will be used default.
						processDelegationType = DefaultDelegationType.NAME;
					}
					String delegationType = processDelegationType;

					taskEntity.getCandidates().forEach(identityLink -> {
						String user = identityLink.getUserId();
						Assert.notNull(user, "User id cannot be null!");
						Assert.isTrue(EntityUtils.isUuid(user), "User id must be UUID in this phase!");
						UUID userId = UUID.fromString(user);

						WorkflowTaskInstanceDto mockTask = new WorkflowTaskInstanceDto();
						mockTask.setId(taskEntity.getId());

						List<IdmDelegationDefinitionDto> delegationDefinitions
								= delegationManager.findDelegation(delegationType, userId, null, mockTask);
						if (CollectionUtils.isEmpty(delegationDefinitions)) {
							// No delegation found.
							return;
						}

						// Create delegation.
						delegationDefinitions.forEach(delegationDefinition -> {

							delegationManager.delegate(mockTask, delegationDefinition);
							UUID delegate = delegationDefinition.getDelegate();
							Assert.notNull(delegate, "Delegate cannot be null!");
							taskEntity.addCandidateUser(delegate.toString());
						});
						// Remove delegator form task and add delegate as candidate.
						taskEntity.deleteUserIdentityLink(user, identityLink.getType());
						// Add delegator to the task as participant (delegator must have permission for read the task)
						taskEntity.addUserIdentityLink(user, IdentityLinkType.PARTICIPANT);
					});
				}
			}
			break;
		case TASK_COMPLETED:
			// Check if exists task delegation for this task and set state on executed.
			if (event instanceof ActivitiEntityEventImpl
					&& ((ActivitiEntityEventImpl) event).getEntity() instanceof TaskEntity) {
				TaskEntity taskEntity = (TaskEntity) ((ActivitiEntityEventImpl) event).getEntity();
				if (taskEntity.getId() != null) {
					IdmDelegationFilter delegationFilter = new IdmDelegationFilter();
					delegationFilter.setOwnerId(DtoUtils.toUuid(taskEntity.getId()));
					delegationFilter.setOwnerType(WorkflowTaskInstanceDto.class.getCanonicalName());

					IdmDelegationDto delegation = delegationService.find(delegationFilter, null).getContent()
							.stream()
							.findFirst()
							.orElse(null);

					if (delegation != null) {
						delegation.setOwnerState(new OperationResultDto(OperationState.EXECUTED));
						delegationService.save(delegation);
					}
				}

			}
			break;
		case ACTIVITY_CANCELLED:
			// Check if exists task delegation for this cancelled activity and set state to the cancelled.
			if (event instanceof ActivitiActivityCancelledEvent) {
				// Find activiti task by name (event doesn't contains ID of user task)
				ActivitiActivityCancelledEvent cancelledEvent = (ActivitiActivityCancelledEvent) event;
				TaskQuery taskQuery = taskService.createTaskQuery();

				taskQuery.taskDefinitionKey(cancelledEvent.getActivityId());
				taskQuery.processInstanceId(cancelledEvent.getProcessInstanceId());
				taskQuery.processDefinitionId(cancelledEvent.getProcessDefinitionId());
				// Search a task.
				List<Task> tasks = taskQuery.list();

				if (tasks != null && tasks.size() == 1) {
					Task task = tasks.get(0);
					if (task.getId() != null) {
						WorkflowTaskInstanceDto mockTask = new WorkflowTaskInstanceDto();
						mockTask.setId(task.getId());

						List<IdmDelegationDto> delegations = delegationManager.findDelegationForOwner(mockTask);
						if (!CollectionUtils.isEmpty(delegations)) {
							delegations.forEach(delegation -> {
								delegation.setOwnerState(new OperationResultDto(OperationState.CANCELED));
								delegationService.save(delegation);
							});
						}
					}
				}
			}
			break;
		// Delete IdmDelegations using this workflow task - ensures integrity.
		case ENTITY_DELETED:
			if (event instanceof ActivitiEntityEventImpl
					&& ((ActivitiEntityEventImpl) event).getEntity() instanceof HistoricTaskInstanceEntity) {
				HistoricTaskInstanceEntity taskEntity = (HistoricTaskInstanceEntity) ((ActivitiEntityEventImpl) event).getEntity();
				String taskId = taskEntity.getId();
				Assert.notNull(taskId, "Task ID cannot be null here!");

				IdmDelegationFilter delegationFilter = new IdmDelegationFilter();
				delegationFilter.setOwnerId(DtoUtils.toUuid(taskId));
				delegationFilter.setOwnerType(WorkflowTaskInstanceDto.class.getCanonicalName());

				// Delete delegation connected to this task.
				delegationService.find(delegationFilter, null).getContent()
						.forEach(delegation -> {
							delegationService.delete(delegation);
						});
			}
			break;
		default:
			LOG.debug("DelegationEventListener - receive not required event [{}]", event.getType());
		}
	}

	@Override
	public boolean isFailOnException() {
		// We can throw exception
		return true;
	}
}
