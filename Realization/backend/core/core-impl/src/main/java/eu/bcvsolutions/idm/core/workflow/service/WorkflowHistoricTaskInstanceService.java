package eu.bcvsolutions.idm.core.workflow.service;

import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricTaskInstanceDto;

/**
 * Service for control workflow task instances.
 * 
 * @author svandav
 *
 */
public interface WorkflowHistoricTaskInstanceService extends ReadDtoService<WorkflowHistoricTaskInstanceDto, WorkflowFilterDto> {

	String SORT_BY_CREATE_TIME = "createTime";
	String SORT_BY_END_TIME = "endTime";
	String TASK_COMPLETE_DECISION = "taskCompleteDecision";
	String TASK_COMPLETE_MESSAGE = "taskCompleteMessage";

	/**
	 * Search historic task by ID.
	 * @param historicTaskInstanceId
	 * @return
	 */
	WorkflowHistoricTaskInstanceDto get(String historicTaskInstanceId);

	/**
	 * Search historic task by process ID
	 * @param processId
	 * @return
	 */
	WorkflowHistoricTaskInstanceDto getTaskByProcessId(String processId);

}
