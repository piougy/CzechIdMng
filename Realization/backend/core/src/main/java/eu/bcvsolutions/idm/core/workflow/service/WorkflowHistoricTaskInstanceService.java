package eu.bcvsolutions.idm.core.workflow.service;

import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricTaskInstanceDto;

public interface WorkflowHistoricTaskInstanceService {

	public static final String SORT_BY_CREATE_TIME = "createTime";

	ResourcesWrapper<WorkflowHistoricTaskInstanceDto> search(WorkflowFilterDto filter);

	WorkflowHistoricTaskInstanceDto get(String historicProcessInstanceId);

}
