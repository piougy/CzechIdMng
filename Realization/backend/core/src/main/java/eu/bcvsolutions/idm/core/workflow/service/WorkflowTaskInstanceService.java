package eu.bcvsolutions.idm.core.workflow.service;

import java.util.Map;

import eu.bcvsolutions.idm.core.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
/**
 * Service for control workflow task instances.
 * @author svandav
 *
 */
public interface WorkflowTaskInstanceService {
	
	public static final String WORKFLOW_DECISION = "decision";
	public static final String FORM_PROPERTY_TOOLTIP_KEY = "tooltip";
	public static final String FORM_PROPERTY_PLACEHOLDER_KEY = "placeholder";

	/**
	 * Complete task
	 * @param taskId
	 * @param decision
	 */
	void completeTask(String taskId, String decision);

	/**
	 * Find task instance by ID
	 * @param taskId
	 * @return
	 */
	WorkflowTaskInstanceDto get(String taskId);

	void completeTask(String taskId, String decision, Map<String, String> map);

	/**
	 * Search tasks by filter. Only candidate or assigned user can read task.
	 * @param filter
	 * @return
	 */
	ResourcesWrapper<WorkflowTaskInstanceDto> search(WorkflowFilterDto filter);

	/**
	 * Complete task
	 * @param taskId
	 * @param decision
	 * @param formData
	 * @param variables
	 */
	void completeTask(String taskId, String decision, Map<String, String> formData, Map<String, Object> variables);

}
