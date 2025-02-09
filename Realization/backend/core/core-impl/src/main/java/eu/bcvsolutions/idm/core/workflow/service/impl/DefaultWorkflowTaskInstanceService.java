package eu.bcvsolutions.idm.core.workflow.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.activiti.engine.FormService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.AbstractFormType;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.FormType;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfo;
import org.activiti.engine.task.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.rest.AbstractBaseDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.workflow.domain.formtype.AbstractComponentFormType;
import eu.bcvsolutions.idm.core.workflow.domain.formtype.DecisionFormType;
import eu.bcvsolutions.idm.core.workflow.domain.formtype.TaskHistoryFormType;
import eu.bcvsolutions.idm.core.workflow.model.dto.DecisionFormTypeDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.FormDataDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.IdentityLinkDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessDefinitionDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricTaskInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessDefinitionService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskDefinitionService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;

/**
 * Default workflow task instance service
 *
 * @author svandav
 * @author Ondrej Husnik
 *
 */
@Service
public class DefaultWorkflowTaskInstanceService extends
		AbstractBaseDtoService<WorkflowTaskInstanceDto, WorkflowFilterDto> implements WorkflowTaskInstanceService {

	@Autowired
	private SecurityService securityService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private FormService formService;
	@Autowired
	private LookupService lookupService;
	@Autowired
	private WorkflowTaskDefinitionService workflowTaskDefinitionService;
	@Autowired
	private WorkflowProcessDefinitionService workflowProcessDefinitionService;
	@Autowired
	private WorkflowHistoricTaskInstanceService historicTaskInstanceService;
	@Autowired
	private ConfigurationService configurationService;

	@Override
	public Page<WorkflowTaskInstanceDto> find(WorkflowFilterDto filter, Pageable pageable,
			BasePermission... permission) {
		return internalSearch(filter, pageable, permission);
	}

	@Override
	public void completeTask(String taskId, String decision) {
		completeTask(taskId, decision, null);
	}

	@Override
	public void completeTask(String taskId, String decision, Map<String, String> formData) {
		completeTask(taskId, decision, null, null);
	}

	@Override
	public void completeTask(String taskId, String decision, Map<String, String> formData,
			Map<String, Object> variables) {
		BasePermission[] permission = {IdmBasePermission.UPDATE};
		this.completeTask(taskId, decision, formData, variables, permission);
	}

	@Override
	public void completeTask(String taskId, String decision, Map<String, String> formData,
			Map<String, Object> variables, BasePermission[] permission) {
		UUID implementerId = securityService.getCurrentId();
		String loggedUser = implementerId.toString();
		UUID originalImplementerId = securityService.getOriginalId();
		// Check if user can complete this task
		if (!canExecute(this.get(taskId, permission), permission)) {
			throw new ResultCodeException(CoreResultCode.FORBIDDEN,
					"You do not have permission for execute task with ID: %s !", ImmutableMap.of("taskId", taskId));
		}
		// add original user into task variables
		if (originalImplementerId != null && !originalImplementerId.equals(implementerId)) {
			if (variables == null) {
				variables = new HashMap<>();
			}
			variables.put(WorkflowProcessInstanceService.ORIGINAL_IMPLEMENTER_IDENTIFIER, originalImplementerId);
		}
		taskService.setAssignee(taskId, loggedUser);
		taskService.setVariables(taskId, variables);
		taskService.setVariableLocal(taskId, WorkflowHistoricTaskInstanceService.TASK_COMPLETE_DECISION, decision);
		if (variables != null) {
			if (variables.containsKey(WorkflowHistoricTaskInstanceService.TASK_COMPLETE_MESSAGE)) {
				taskService.setVariableLocal(taskId, WorkflowHistoricTaskInstanceService.TASK_COMPLETE_MESSAGE,
						variables.get(WorkflowHistoricTaskInstanceService.TASK_COMPLETE_MESSAGE));
			}
		}
		Map<String, String> properties = new HashMap<>();
		properties.put(WorkflowTaskInstanceService.WORKFLOW_DECISION, decision);
		if (formData != null) {
			properties.putAll(formData);
		}
		formService.submitTaskFormData(taskId, properties);
	}

	/**
	 * Check if user can complete this task
	 */
	private boolean canExecute(WorkflowTaskInstanceDto task, BasePermission[] permission) {
		return permission == null || permission.length == 0 || this.getPermissions(task).contains(IdmBasePermission.EXECUTE.getName());
	}

	@Override
	public Set<String> getPermissions(Serializable id) {
		Assert.notNull(id, "Identifier is required.");
		if (id instanceof BaseDto) {
			BaseDto baseDto = (BaseDto) id;
			return this.getPermissions(this.get(baseDto.getId()));
		}
		return this.getPermissions(this.get(id));
	}

	@Override
	public WorkflowTaskInstanceDto get(Serializable id, WorkflowFilterDto context, BasePermission... permission) {
		if (context == null) {
			context = new WorkflowFilterDto();
		}
		context.setId(UUID.fromString(String.valueOf(id)));
		List<WorkflowTaskInstanceDto> tasks = internalSearch(context, null, permission).getContent();

		if (!tasks.isEmpty()) {
			return tasks.get(0);

		}
		// Current task doesn't exists, we try to find historic task.
		WorkflowHistoricTaskInstanceDto historicTask = historicTaskInstanceService.get(String.valueOf(id), context);
		if (historicTask != null) {
			return historicTask;
		}

		return null;
	}

	@Override
	public WorkflowTaskInstanceDto get(Serializable id, BasePermission... permission) {
		Assert.notNull(id, "Identifier is required.");
		return this.get(id, null, permission);
	}

	@Override
	public Set<String> getPermissions(WorkflowTaskInstanceDto dto) {
		Assert.notNull(dto, "DTO is required.");
		//
		final Set<String> permissions = new HashSet<>();
		String loggedUserId = securityService.getCurrentId().toString();

		// TODO: user with admin permission can execute any tasks.
		// Set<GrantedAuthority> defaultAuthorities =
		// autorizationPolicyService.getDefaultAuthorities(securityService.getCurrentId());
		// defaultAuthorities.contains(CoreGroupPermission.WORKFLOW_TASK_ADMIN);
		
		// If is logged user candidate or assigned, then can execute task.
		boolean isCandidate = dto.getIdentityLinks().stream()
				.anyMatch(
						(identity) -> ((identity.getType().equals(IdentityLinkType.ASSIGNEE)
						|| identity.getType().equals(IdentityLinkType.CANDIDATE))
						&& identity.getUserId().equals(loggedUserId)));
		if (isCandidate) {
			permissions.add(IdmBasePermission.EXECUTE.getName());
		}
		return permissions;
	}

	@Override
	public Page<UUID> findIds(WorkflowFilterDto filter, Pageable pageable, BasePermission... permission) {
		
		Page<WorkflowTaskInstanceDto> page = this.find(filter, pageable, permission);
		
		List<UUID> uuids = page.getContent()
				.stream()
				.map(task -> UUID.fromString(task.getId()))
				.collect(Collectors.toList());
		
		return new PageImpl<>(uuids, page.getPageable(), page.getTotalElements());
	}
	
	@Override
	public Page<UUID> findIds(Pageable pageable, BasePermission... permission) {
		
		return findIds(new WorkflowFilterDto(), pageable, permission);
	}
	
	private WorkflowTaskInstanceDto toResource(TaskInfo task, BasePermission[] permission) {
		if (task == null) {
			return null;
		}

		WorkflowTaskInstanceDto dto = new WorkflowTaskInstanceDto();
		dto.setId(task.getId());
		dto.setName(task.getName());
		dto.setProcessDefinitionId(task.getProcessDefinitionId());
		dto.setPriority(task.getPriority());
		dto.setAssignee(task.getAssignee());
		dto.setCreated(task.getCreateTime());
		dto.setFormKey(task.getFormKey());
		dto.setDescription(task.getDescription());
		dto.setProcessInstanceId(task.getProcessInstanceId());

		Map<String, Object> taskVariables = task.getTaskLocalVariables();
		Map<String, Object> processVariables = task.getProcessVariables();

		// Add applicant username to task dto (for easier work)
		if (processVariables != null
				&& processVariables.containsKey(WorkflowProcessInstanceService.APPLICANT_IDENTIFIER)) {
			dto.setApplicant(processVariables.get(WorkflowProcessInstanceService.APPLICANT_IDENTIFIER).toString());
		}

		dto.setVariables(processVariables);
		convertToDtoVariables(dto, taskVariables);

		dto.setDefinition(workflowTaskDefinitionService.searchTaskDefinitionById(task.getProcessDefinitionId(),
				task.getTaskDefinitionKey()));

		if (!Strings.isNullOrEmpty(task.getProcessDefinitionId())) {
			WorkflowProcessDefinitionDto processDefinition = workflowProcessDefinitionService
					.get(task.getProcessDefinitionId());
			if (processDefinition != null) {
				dto.setProcessDefinitionKey(processDefinition.getKey());
			}
		}

		TaskFormData taskFormData = formService.getTaskFormData(task.getId());

		// Add form data (it means form properties and value from WF)
		List<FormProperty> formProperties = taskFormData.getFormProperties();

		// Search and add identity links to dto (It means all user
		// (assigned/candidates/group) for this task)
		List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
		if (identityLinks != null) {
			List<IdentityLinkDto> identityLinksDtos = new ArrayList<>(identityLinks.size());
			identityLinks.forEach((il) -> {
				identityLinksDtos.add(toResource(il));
			});
			dto.getIdentityLinks().addAll(identityLinksDtos);
		}

		// Check if the logged user can complete this task
		boolean canExecute = this.canExecute(dto, permission);
		if (formProperties != null && !formProperties.isEmpty()) {
			formProperties.forEach((property) -> {
				resovleFormProperty(property, dto, canExecute);
			});
		}

		return dto;
	}

	/**
	 * Convert form property and add to result dto
	 *
	 * @param property
	 * @param dto
	 * @param canExecute
	 */
	@SuppressWarnings("unchecked")
	private void resovleFormProperty(FormProperty property, WorkflowTaskInstanceDto dto, boolean canExecute) {
		FormType formType = property.getType();

		if (formType instanceof DecisionFormType) {
			// Decision buttons will be add only if logged user can execute/complete this
			// task
			if (!canExecute) {
				return;
			}
			DecisionFormTypeDto decisionDto = (DecisionFormTypeDto) ((DecisionFormType) formType)
					.convertFormValueToModelValue(property.getValue());
			if (decisionDto != null) {
				decisionDto.setId(property.getId());
				setDecisionReasonRequired(decisionDto);
				dto.getDecisions().add(decisionDto);
			}
		} else if (formType instanceof TaskHistoryFormType) {
			WorkflowFilterDto filterDto = new WorkflowFilterDto();
			filterDto.setProcessInstanceId(dto.getProcessInstanceId());
			List<WorkflowHistoricTaskInstanceDto> tasks = historicTaskInstanceService.find(filterDto, PageRequest.of(0, 50)).getContent();

			List<WorkflowHistoricTaskInstanceDto> history = tasks.stream()
					.filter(workflowHistoricTaskInstanceDto -> workflowHistoricTaskInstanceDto.getEndTime() != null)
					.sorted((o1, o2) -> {
						if (o1.getEndTime().before(o2.getEndTime())) {
							return -1;
						} else if (o1.getEndTime().after(o2.getEndTime())) {
							return 1;
						}
						return 0;
					})
					.collect(Collectors.toList());
			dto.getFormData().add(historyToResource(property, history));
		} else if (formType instanceof AbstractFormType) {
			// To rest will be add only component form type marked as "exportable to rest".
			if (formType instanceof AbstractComponentFormType
					&& !((AbstractComponentFormType) formType).isExportableToRest()) {
				return;
			}
			Object values = formType.getInformation("values");
			if (values instanceof Map<?, ?>) {
				dto.getFormData().add(toResource(property, (Map<String, String>) values));
			} else {
				dto.getFormData().add(toResource(property, null));
			}
		}
	}

	@Override
	public void convertToDtoVariables(WorkflowTaskInstanceDto dto, Map<String, Object> taskVariables) {
		if (taskVariables != null) {
			taskVariables.entrySet().forEach((entry) -> {
				Object value = entry.getValue();
				dto.getVariables().put(entry.getKey(), value == null ? null : value.toString());
			});
		}
	}
	
	@Override
	public Object getProcessVariable(String taskId, String key) {
		return taskService.getVariableInstance(taskId, key);
	}
	

	@Override
	public boolean canReadAllTask(BasePermission... permission) {
		// TODO: Implement check on permission (READ, UPDATE ...). Permissions are uses only for skip rights check now!
		return permission == null || securityService.isAdmin() || securityService.hasAnyAuthority(CoreGroupPermission.WORKFLOW_TASK_ADMIN);
	}

	private FormDataDto historyToResource(FormProperty property, List<WorkflowHistoricTaskInstanceDto> history) {
		FormDataDto dto = new FormDataDto();
		dto.setId(property.getId());
		dto.setName(property.getName());
		String value;
		try {
			value = new ObjectMapper().writeValueAsString(history);
		} catch (JsonProcessingException e) {
			throw new CoreException(e);
		}
		dto.setValue(value);
		dto.setType(property.getType().getName());
		dto.setReadable(property.isReadable());
		dto.setRequired(property.isRequired());
		dto.setWritable(property.isWritable());
		return dto;
	}

	private FormDataDto toResource(FormProperty property, Map<String, String> additionalInformations) {
		FormDataDto dto = new FormDataDto();
		dto.setId(property.getId());
		dto.setName(property.getName());
		dto.setValue(property.getValue());
		dto.setType(property.getType().getName());
		dto.setReadable(property.isReadable());
		dto.setRequired(property.isRequired());
		dto.setWritable(property.isWritable());
		// Add all additional informations (come from form properties - form
		// values)
		if (additionalInformations != null) {
			// extra add tooltip to dto
			if (additionalInformations.containsKey(WorkflowTaskInstanceService.FORM_PROPERTY_TOOLTIP_KEY)) {
				dto.setTooltip(additionalInformations.get(WorkflowTaskInstanceService.FORM_PROPERTY_TOOLTIP_KEY));
			}
			// extra add placeholder to dto
			if (additionalInformations.containsKey(WorkflowTaskInstanceService.FORM_PROPERTY_PLACEHOLDER_KEY)) {
				dto.setPlaceholder(
						additionalInformations.get(WorkflowTaskInstanceService.FORM_PROPERTY_PLACEHOLDER_KEY));
			}
			dto.getAdditionalInformations().putAll(additionalInformations);
		}
		return dto;
	}

	private IdentityLinkDto toResource(IdentityLink link) {
		if (link == null) {
			return null;
		}

		IdentityLinkDto dto = new IdentityLinkDto();
		dto.setGroupId(link.getGroupId());
		dto.setType(link.getType());
		dto.setUserId(link.getUserId());
		return dto;
	}

	private PageImpl<WorkflowTaskInstanceDto> internalSearch(WorkflowFilterDto filter, Pageable pageable, BasePermission... permission) {
		if (pageable == null) {
			pageable = PageRequest.of(0, Integer.MAX_VALUE);
		}
		// if currently logged user can read all task continue
		if (!canReadAllTask(permission)) {
			// if user can't read all task check filter
			if (filter.getCandidateOrAssigned() == null) {
				if (Boolean.TRUE == filter.getOnlyInvolved()) {
					filter.setCandidateOrAssigned(securityService.getCurrentId().toString());
				}
			} else {
				IdmIdentityDto identity = lookupService.lookupDto(IdmIdentityDto.class, filter.getCandidateOrAssigned());
				if (!identity.getId().equals(securityService.getCurrentId())) {
					throw new ResultCodeException(CoreResultCode.FORBIDDEN,
							"You do not have permission for access to all tasks!");
				}
			}
			// else is filled candidate and it is equals currently logged user
		}

		String processDefinitionId = filter.getProcessDefinitionId();
		Map<String, Object> equalsVariables = filter.getEqualsVariables();

		TaskQuery query = taskService.createTaskQuery();

		query.active();
		query.includeProcessVariables();

		if (processDefinitionId != null) {
			query.processDefinitionId(processDefinitionId);
		}
		if (filter.getProcessDefinitionKey() != null) {
			query.processDefinitionKey(filter.getProcessDefinitionKey());
		}
		if (filter.getProcessInstanceId() != null) {
			query.processInstanceId(filter.getProcessInstanceId());
		}
		if (filter.getId() != null) {
			query.taskId(filter.getId().toString());
		}
		if (filter.getCreatedAfter() != null) {
			query.taskCreatedAfter(Date.from(filter.getCreatedAfter().toInstant()));
		}
		if (filter.getCreatedBefore() != null) {
			query.taskCreatedBefore(Date.from(filter.getCreatedBefore().toInstant()));
		}
		if (equalsVariables != null) {
			equalsVariables.entrySet().forEach((entry) -> {
				query.processVariableValueEquals(entry.getKey(), entry.getValue());
			});
		}

		if (filter.getCandidateOrAssigned() != null) {
			BaseDto dto = lookupService.lookupDto(IdmIdentityDto.class, filter.getCandidateOrAssigned());
			Assert.notNull(dto, "DTO is required.");
			query.taskCandidateOrAssigned(String.valueOf(dto.getId()));
		}

		if (pageable.getSort() != null) {
			pageable.getSort().forEach(order -> {
				if (SORT_BY_TASK_CREATED.equals(order.getProperty())) {
					// Sort by key
					query.orderByTaskCreateTime();
					if (order.isAscending()) {
						query.asc();
					} else {
						query.desc();
					}
				}
			});
		}
		query.orderByTaskCreateTime();
		query.desc();

		long count = query.count();
		List<Task> tasks = query.listPage((pageable.getPageNumber()) * pageable.getPageSize(), pageable.getPageSize());

		List<WorkflowTaskInstanceDto> dtos = new ArrayList<>();
		if (tasks != null) {
			tasks.forEach((task) -> {
				dtos.add(toResource(task, permission));
			});
		}
		return new PageImpl<>(dtos, pageable, count);
	}
	
	private void setDecisionReasonRequired(DecisionFormTypeDto decisionDto) {
		Boolean reasonRequired = decisionDto.isReasonRequired();
		if (reasonRequired == null) {
			String key = null;
			if (WORKFLOW_DECISION_APPROVE.equalsIgnoreCase(decisionDto.getId())) {
				key = WorkflowTaskInstanceService.PROPERTY_APPROVE_DECISION_REASON_REQUIRED;
			} else if (WORKFLOW_DECISION_DISAPPROVE.equalsIgnoreCase(decisionDto.getId())) {
				key = WorkflowTaskInstanceService.PROPERTY_DISAPPROVE_DECISION_REASON_REQUIRED;
			}
			reasonRequired = key == null ? false : configurationService.getBooleanValue(key, false);
			decisionDto.setReasonRequired(reasonRequired);
		}
	}
}
