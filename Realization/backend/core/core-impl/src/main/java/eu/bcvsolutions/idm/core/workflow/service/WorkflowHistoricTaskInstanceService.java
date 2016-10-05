package eu.bcvsolutions.idm.core.workflow.service;

import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricTaskInstanceDto;

public interface WorkflowHistoricTaskInstanceService {

	public static final String SORT_BY_CREATE_TIME = "createTime";
	public static final String SORT_BY_END_TIME = "endTime";
	public static final String TASK_COMPLETE_DECISION = "taskCompleteDecision";

	/**
	 * Search historic tasks
	 * @param filter
	 * @return
	 */
	ResourcesWrapper<WorkflowHistoricTaskInstanceDto> search(WorkflowFilterDto filter);

	/**
	 * Search historic task by ID.
	 * @param historicTaskInstanceId
	 * @return
	 */
	WorkflowHistoricTaskInstanceDto get(String historicTaskInstanceId);

}
