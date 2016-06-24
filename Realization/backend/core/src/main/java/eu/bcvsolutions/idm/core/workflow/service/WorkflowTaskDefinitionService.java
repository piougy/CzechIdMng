package eu.bcvsolutions.idm.core.workflow.service;

import java.util.List;

import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskDefinitionDto;

public interface WorkflowTaskDefinitionService {
	
	public static final String FORM_TYPE_ACTION = "action";
	public static final String FORM_ACTION_TOOLTIP = "tooltip";
	public static final String FORM_ACTION_LEVEL = "level";
	public static final String FORM_ACTION_PERMISSIONS = "permissions";
	
	
	List<WorkflowTaskDefinitionDto> searchTaskDefinitions(String processDefinitionId);
	WorkflowTaskDefinitionDto searchTaskDefinitionById(String processDefinitionId, String taskId);

}
