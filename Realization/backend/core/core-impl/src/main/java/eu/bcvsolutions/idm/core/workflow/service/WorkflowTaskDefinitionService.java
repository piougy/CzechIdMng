package eu.bcvsolutions.idm.core.workflow.service;

import java.util.List;

import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskDefinitionDto;
/**
 * Service for control workflow definitions.
 * @author svandav
 *
 */
public interface WorkflowTaskDefinitionService extends ReadDtoService<WorkflowTaskDefinitionDto, EmptyFilter> {
	
	public static final String FORM_TYPE_ACTION = "action";
	public static final String FORM_ACTION_TOOLTIP = "tooltip";
	public static final String FORM_ACTION_LEVEL = "level";
	public static final String FORM_ACTION_PERMISSIONS = "permissions";
	
	/**
	 * Search all tasks definitions for process definition
	 * @param processDefinitionId
	 * @return
	 */
	List<WorkflowTaskDefinitionDto> searchTaskDefinitions(String processDefinitionId);
	
	/**
	 * Search one task definition for specific definition and task id
	 * @param processDefinitionId
	 * @param taskId
	 * @return
	 */
	WorkflowTaskDefinitionDto searchTaskDefinitionById(String processDefinitionId, String taskId);

}
