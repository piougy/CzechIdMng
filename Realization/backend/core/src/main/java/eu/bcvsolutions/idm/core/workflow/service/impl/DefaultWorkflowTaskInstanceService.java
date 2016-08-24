package eu.bcvsolutions.idm.core.workflow.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.AbstractFormType;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.domain.formtype.DecisionFormType;
import eu.bcvsolutions.idm.core.workflow.model.dto.DecisionFormTypeDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.FormDataDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.IdentityLinkDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricTaskInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskDefinitionService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;
import eu.bcvsolutions.idm.security.service.SecurityService;

/**
 * Default workflow task instance service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultWorkflowTaskInstanceService implements WorkflowTaskInstanceService {

	@Autowired
	private SecurityService securityService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private FormService formService;

	@Autowired
	private WorkflowTaskDefinitionService workflowTaskDefinitionService;

	@Override
	public ResourcesWrapper<WorkflowTaskInstanceDto> search(WorkflowFilterDto filter) {

		String processDefinitionId = filter.getProcessDefinitionId();
		Map<String, Object> equalsVariables = filter.getEqualsVariables();

		TaskQuery query = taskService.createTaskQuery();

		query.active();
		query.includeProcessVariables();

		if (processDefinitionId != null) {
			query.processDefinitionId(processDefinitionId);
		}
		if(filter.getProcessDefinitionKey() != null){
			query.processDefinitionKey(filter.getProcessDefinitionKey());
		}
		if(filter.getProcessInstanceId() != null){
			query.processInstanceId(filter.getProcessInstanceId());
		}
		if(filter.getId() != null){
			query.taskId(filter.getId());
		}
		if (equalsVariables != null) {
			for (String key : equalsVariables.keySet()) {
				query.processVariableValueEquals(key, equalsVariables.get(key));
			}
		}

		// check security ... only candidate or assigned user can read task
		String loggedUser = securityService.getUsername();
		query.taskCandidateOrAssigned(loggedUser);
		query.orderByTaskCreateTime();
		query.desc();
		long count = query.count();
		List<Task> tasks = query.listPage((filter.getPageNumber()) * filter.getPageSize(), filter.getPageSize());
		List<WorkflowTaskInstanceDto> dtos = new ArrayList<>();

		if (tasks != null) {
			for (Task task : tasks) {
				dtos.add(toResource(task));
			}
		}

		double totalPageDouble = ((double) count / filter.getPageSize());
		double totlaPageFlorred = Math.floor(totalPageDouble);
		long totalPage = 0;
		if (totalPageDouble > totlaPageFlorred) {
			totalPage = (long) (totlaPageFlorred + 1);
		}

		ResourcesWrapper<WorkflowTaskInstanceDto> result = new ResourcesWrapper<>(dtos, count, totalPage,
				filter.getPageNumber(), filter.getPageSize());
		return result;
	}

	@Override
	public WorkflowTaskInstanceDto get(String taskId) {
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setId(taskId);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) search(filter).getResources();
		
		return tasks.isEmpty() ? null : tasks.get(0);
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
	public void completeTask(String taskId, String decision, Map<String, String> formData, Map<String, Object> variables) {
		String loggedUser = securityService.getUsername();
		taskService.setAssignee(taskId, loggedUser);
		taskService.setVariables(taskId, variables);
		taskService.setVariableLocal(taskId, WorkflowHistoricTaskInstanceService.TASK_COMPLETE_DECISION, decision);
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(WorkflowTaskInstanceService.WORKFLOW_DECISION, decision);
		if(formData != null){
			properties.putAll(formData);
		}
		formService.submitTaskFormData(taskId, properties);
	}

	@SuppressWarnings("unchecked")
	private WorkflowTaskInstanceDto toResource(Task task) {
		if (task == null) {
			return null;
		}
		
		WorkflowTaskInstanceDto dto = new WorkflowTaskInstanceDto();
		dto.setId(task.getId());
		dto.setCreated(task.getCreateTime());
		dto.setFormKey(task.getFormKey());
		dto.setAssignee(task.getAssignee());
		dto.setName(task.getName());
		dto.setDescription(task.getDescription());
		dto.setProcessInstanceId(task.getProcessInstanceId());

		Map<String, Object> taksVariables = task.getTaskLocalVariables();
		Map<String, Object> processVariables = task.getProcessVariables();
		
		// Add applicant username to task dto (for easier work)
		if (processVariables != null
				&& processVariables.containsKey(WorkflowProcessInstanceService.APPLICANT_USERNAME)) {
			dto.setApplicant((String) processVariables.get(WorkflowProcessInstanceService.APPLICANT_USERNAME));
		}

		dto.setVariables(processVariables);
		convertToDtoVariables(dto, taksVariables);
		//convertToDtoVariables(dto, processVariables);

		dto.setDefinition(workflowTaskDefinitionService.searchTaskDefinitionById(task.getProcessDefinitionId(),
				task.getTaskDefinitionKey()));

		TaskFormData taskFormData = formService.getTaskFormData(task.getId());
		
		//Add form data (it means form properties and value from WF)
		List<FormProperty> formProperties = taskFormData.getFormProperties();
		if (formProperties != null && !formProperties.isEmpty()) {
			for (FormProperty property : formProperties) {
				if (property.getType() instanceof DecisionFormType) {
					DecisionFormTypeDto decisionDto = (DecisionFormTypeDto) ((DecisionFormType) property.getType())
							.convertFormValueToModelValue(property.getValue());
					if (decisionDto != null) {
						decisionDto.setId(property.getId());
						dto.getDecisions().add(decisionDto);
					}
				} else {
					if (property.getType() instanceof AbstractFormType) {

						Object values = ((AbstractFormType) property.getType()).getInformation("values");
						if (values instanceof Map<?, ?>) {
							dto.getFormData().add(toResource(property, (Map<String, String>) values));
						} else {
							dto.getFormData().add(toResource(property, null));
						}
					}
				}
			}
		}
		
		//Serach and add identity links to dto (It means all user (assigned/candidates/group) for this task)
		List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
		if (identityLinks != null) {
			List<IdentityLinkDto> identityLinksDtos = new ArrayList<>();
			for (IdentityLink il : identityLinks) {
				identityLinksDtos.add(toResource(il));
			}
			dto.getIdentityLinks().addAll(identityLinksDtos);
		}

		return dto;
	}

	private void convertToDtoVariables(WorkflowTaskInstanceDto dto, Map<String, Object> taksVariables) {
		if (taksVariables != null) {
			for (String key : taksVariables.keySet()) {
				dto.getVariables().put(key, taksVariables.get(key) == null ? null : taksVariables.get(key).toString());
			}
		}
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

}
