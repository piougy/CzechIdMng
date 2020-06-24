package eu.bcvsolutions.idm.core.workflow.service;

import java.util.Map;
import java.util.Set;

import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;

/**
 * Service for control workflow task instances.
 * 
 * @author svandav
 *
 */
public interface WorkflowTaskInstanceService extends ReadDtoService<WorkflowTaskInstanceDto, WorkflowFilterDto> {

	String WORKFLOW_DECISION = "decision";
	String FORM_PROPERTY_TOOLTIP_KEY = "tooltip";
	String FORM_PROPERTY_PLACEHOLDER_KEY = "placeholder";
	String WORKFLOW_DECISION_APPROVE = "approve";
	String WORKFLOW_DECISION_DISAPPROVE = "disapprove";
	String SORT_BY_TASK_CREATED = "taskCreated";

	/**
	 * Complete task.
	 * 
	 * @param taskId
	 * @param decision
	 */
	void completeTask(String taskId, String decision);

	/**
	 * Complete task.
	 * 
	 * @param taskId
	 * @param decision
	 * @param map
	 */
	void completeTask(String taskId, String decision, Map<String, String> map);

	/**
	 * Complete task.
	 * 
	 * @param taskId
	 * @param decision
	 * @param formData
	 * @param variables
	 */
	void completeTask(String taskId, String decision, Map<String, String> formData, Map<String, Object> variables);

	/**
	 * Get permissions for given {@link WorkflowTaskInstanceDto}
	 * 
	 * @param dto
	 * @return
	 */
	@Override
	Set<String> getPermissions(WorkflowTaskInstanceDto dto);

	/**
	 * Complete given task
	 * 
	 * @param taskId
	 * @param decision
	 * @param formData
	 * @param variables
	 * @param permission
	 */
	void completeTask(String taskId, String decision, Map<String, String> formData, Map<String, Object> variables,
			BasePermission[] permission);
	
	/**
	 * Add given variables to the DTO.
	 * 
	 * @param dto
	 * @param taskVariables 
	 */
	void convertToDtoVariables(WorkflowTaskInstanceDto dto, Map<String, Object> taskVariables);
	
	/**
	 * Get variable by key from the task or parent process.
	 * 
	 * @param taskId
	 * @param key
	 * @return 
	 */
	Object getProcessVariable(String taskId, String key);
	
	
	/**
	 * Method return true if logged user can read all tasks.
	 *
	 * @param permission
	 * @return
	 */
	boolean canReadAllTask(BasePermission... permission);
}
