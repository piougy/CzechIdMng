package eu.bcvsolutions.idm.core.workflow.service;

import java.util.Map;

import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;

public interface WorkflowTaskInstanceService {
	
	public static final String WORKFLOW_DECISION = "decision";
	public static final String FORM_PROPERTY_TOOLTIP_KEY = "tooltip";
	public static final String FORM_PROPERTY_PLACEHOLDER_KEY = "placeholder";

	void completeTask(String taskId, String decision);

	WorkflowTaskInstanceDto get(String taskId);

	void completeTask(String taskId, String decision, Map<String, String> map);

	ResourcesWrapper<WorkflowTaskInstanceDto> search(WorkflowFilterDto filter);

	void completeTask(String taskId, String decision, Map<String, String> formData, Map<String, Object> variables);

}
