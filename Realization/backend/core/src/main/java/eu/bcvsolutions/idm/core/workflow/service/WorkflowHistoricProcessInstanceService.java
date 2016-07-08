package eu.bcvsolutions.idm.core.workflow.service;

import java.io.InputStream;

import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;

public interface WorkflowHistoricProcessInstanceService {
	public static final String SORT_BY_START_TIME = "startTime";
	public static final String SORT_BY_END_TIME = "endTime";

	ResourcesWrapper<WorkflowHistoricProcessInstanceDto> search(WorkflowFilterDto filter);

	WorkflowHistoricProcessInstanceDto get(String historicProcessInstanceId);

	InputStream getDiagram(String processInstanceId);
}
