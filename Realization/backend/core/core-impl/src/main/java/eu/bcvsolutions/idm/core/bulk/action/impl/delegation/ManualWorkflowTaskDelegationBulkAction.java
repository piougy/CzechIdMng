package eu.bcvsolutions.idm.core.bulk.action.impl.delegation;

import java.util.List;
import java.util.UUID;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.IdentityLinkType;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmDelegationDefinitionFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.DelegationManager;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationDefinitionService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.delegation.type.ManualTaskDelegationType;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceAbstractDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;

/**
 * Bulk operation for manual delegation of workflow tasks.
 * 
 * Delegation of the task can be done by everyone who has right to read the task.
 *
 * @author Vít Švanda
 *
 */
@Component("manualWorkflowTaskDelegationBulkAction")
@Description("Delegate workflow task in bulk action.")
public class ManualWorkflowTaskDelegationBulkAction extends AbstractBulkAction<WorkflowTaskInstanceAbstractDto, WorkflowFilterDto> {

	public static final String NAME = "core-manual-wf-task-delegation-bulk-action";

	public static final String DELEGATE_ATTRIBUTE = "delegate";
	public static final String CANDIDATE_OR_ASSIGNED_FILTER_FIELD = "candidateOrAssigned";

	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private WorkflowTaskInstanceService workflowTaskInstanceService;
	@Autowired
	private DelegationManager delegationManager;
	@Autowired
	private IdmDelegationDefinitionService delegationDefinitionService;
	@Autowired
	@Lazy
	private TaskService taskService;

	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		formAttributes.add(getDelegatorAttribute());
		formAttributes.add(getDelegateAttribute());
		return formAttributes;
	}

	@Override
	public String getName() {
		return ManualWorkflowTaskDelegationBulkAction.NAME;
	}
	
	
	@Override
	public ResultModels prevalidate() {
		ResultModels result = new ResultModels();
		IdmIdentityDto delegator = this.findDelegator();
		if (delegator == null) {
			result.addInfo(new DefaultResultModel(CoreResultCode.MANUAL_TASK_DELEGATION_DELEGATOR_MISSING));
		}

		return result;
	}

	@Override
	protected OperationResult processDto(WorkflowTaskInstanceAbstractDto task) {
		IdmBulkActionDto action = this.getAction();
		Assert.notNull(action, "Bulk action is required.");

		IdmIdentityDto delegator = findDelegator();

		if (delegator == null) {
			throw new ResultCodeException(CoreResultCode.MANUAL_TASK_DELEGATION_DELEGATOR_MISSING);
		}

		UUID delegateId = this.getDelegateId();
		UUID delegatorId = delegator.getId();
		Assert.notNull(delegateId, "Delegate ID cannot be null!");
		IdmIdentityDto delegate = identityService.get(delegateId);
		Assert.notNull(delegate, "Delegate cannot be null!");

		boolean delegatorIsCandidate = task.getIdentityLinks().stream()
				.filter(identityLink -> IdentityLinkType.CANDIDATE.equals(identityLink.getType())
				|| IdentityLinkType.ASSIGNEE.equals(identityLink.getType()))
				.filter(identityLink -> UUID.fromString(identityLink.getUserId()).equals(delegatorId))
				.findFirst()
				.isPresent();
		
		// Delegator have to be candidate or assigned task user!
		if (!delegatorIsCandidate) {
			throw new ResultCodeException(CoreResultCode.MANUAL_TASK_DELEGATION_DELEGATOR_IS_NOT_CANDIDATE,
					ImmutableMap.of("delegator", delegator.getUsername(), "task", task.getId()));
		}
		
		// Find delegation definitions for delegator and delegate.
		IdmDelegationDefinitionDto delegationDefinition = findDelegationDefinition(delegateId, delegatorId);
		
		
		WorkflowTaskInstanceDto mockTask = new WorkflowTaskInstanceDto();
		mockTask.setId(task.getId().toString());
				
		// Create delegation for this task.
		delegationManager.delegate(mockTask, delegationDefinition);
		// Add delegate as task candidat.
		taskService.addCandidateUser(mockTask.getId(), delegationDefinition.getDelegate().toString());
		// Delete delegator form the task's candidats.
		taskService.deleteCandidateUser(mockTask.getId(), delegationDefinition.getDelegator().toString());
		// Add delegator as participant to this task.
		taskService.addUserIdentityLink(mockTask.getId(), delegationDefinition.getDelegator().toString(), IdentityLinkType.PARTICIPANT);

		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}

	/**
	 * Find delegation definitions for delegator and delegate.
	 * 
	 * @param delegateId
	 * @param delegatorId
	 * @return 
	 */
	private IdmDelegationDefinitionDto findDelegationDefinition(UUID delegateId, UUID delegatorId) {
		IdmDelegationDefinitionFilter definitionFilter = new IdmDelegationDefinitionFilter();
		definitionFilter.setValid(Boolean.TRUE);
		definitionFilter.setType(ManualTaskDelegationType.NAME);
		definitionFilter.setDelegateId(delegateId);
		definitionFilter.setDelegatorId(delegatorId);
		List<IdmDelegationDefinitionDto> delegations = delegationDefinitionService
				.find(definitionFilter, null).getContent();
		IdmDelegationDefinitionDto delegationDefinition;
		if (CollectionUtils.isEmpty(delegations)) {
			delegationDefinition = new IdmDelegationDefinitionDto();
			delegationDefinition.setDelegate(delegateId);
			delegationDefinition.setDelegator(delegatorId);
			delegationDefinition.setType(ManualTaskDelegationType.NAME);
			delegationDefinition = delegationDefinitionService.save(delegationDefinition);
		} else {
			// Should be only one.
			delegationDefinition = delegations.get(0);
		}
		return delegationDefinition;
	}

	@Override
	protected WorkflowTaskInstanceAbstractDto getDtoById(UUID id) {
		WorkflowTaskInstanceDto task = workflowTaskInstanceService.get(id.toString());
		if (task != null) {
			WorkflowTaskInstanceAbstractDto mockTask = new WorkflowTaskInstanceAbstractDto();
			mockTask.setId(task.getId());
			mockTask.setIdentityLinks(task.getIdentityLinks());
			mockTask.setApplicant(task.getApplicant());

			return mockTask;
		}
		return null;
	}

	private UUID getDelegateId() {
		Object delegate = this.getProperties().get(DELEGATE_ATTRIBUTE);
		return DtoUtils.toUuid(delegate);
	}
	
	private IdmIdentityDto findDelegator() {
		Object delegatorObj = this.getProperties().get(CANDIDATE_OR_ASSIGNED_FILTER_FIELD);

		UUID delegatorId = DtoUtils.toUuid(delegatorObj);
		if (delegatorId != null) {
			return identityService.get(delegatorId);
		}
		return null;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.WORKFLOW_TASK_READ);
	}

	@Override
	protected boolean checkPermissionForEntity(BaseDto entity) {
		// If has user right for read this task, then has right for this action.
		WorkflowTaskInstanceDto task = workflowTaskInstanceService.get(entity.getId(), IdmBasePermission.READ);
		
		return task != null; 
	}
	
	
	
	/**
	 * Get {@link IdmFormAttributeDto} for select delegate
	 *
	 * @return
	 */
	private IdmFormAttributeDto getDelegateAttribute() {
		IdmFormAttributeDto roles = new IdmFormAttributeDto(
				DELEGATE_ATTRIBUTE,
				DELEGATE_ATTRIBUTE,
				PersistentType.UUID);
		roles.setFaceType(BaseFaceType.IDENTITY_SELECT);
		roles.setMultiple(false);
		roles.setRequired(true);
		return roles;
	}
	
	/**
	 * Get {@link IdmFormAttributeDto} for select delegator
	 *
	 * @return
	 */
	private IdmFormAttributeDto getDelegatorAttribute() {
		IdmFormAttributeDto roles = new IdmFormAttributeDto(
				CANDIDATE_OR_ASSIGNED_FILTER_FIELD,
				CANDIDATE_OR_ASSIGNED_FILTER_FIELD,
				PersistentType.UUID);
		roles.setFaceType(BaseFaceType.IDENTITY_ALLOW_DISABLED_SELECT);
		roles.setMultiple(false);
		roles.setRequired(true);
		return roles;
	}

	@Override
	public int getOrder() {
		return super.getOrder();
	}

	@Override
	public boolean supports(Class<? extends BaseEntity> clazz) {
		return false;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ReadWriteDtoService<WorkflowTaskInstanceAbstractDto, WorkflowFilterDto> getService() {
		// Workaround: Service doesn't have correct DTO type, but I need made this.
		return (ReadWriteDtoService) workflowTaskInstanceService;
	}

}
