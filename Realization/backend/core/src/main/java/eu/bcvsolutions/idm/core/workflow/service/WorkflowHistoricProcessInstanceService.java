package eu.bcvsolutions.idm.core.workflow.service;

import java.io.InputStream;

import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricProcessInstanceDto;
import eu.bcvsolutions.idm.core.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
/**
 * Service for control workflow historic process instances.
 * @author svandav
 *
 */
public interface WorkflowHistoricProcessInstanceService {
	public static final String SORT_BY_START_TIME = "startTime";
	public static final String SORT_BY_END_TIME = "endTime";
	public static final String PROCESS_INSTANCE_NAME = "processInstanceName";

	/**
	 * Search process history. Process variables will be included only for get specific process history. 
	 * It means filter.processInstanceId is filled.
	 * @param filter
	 * @return
	 */
	ResourcesWrapper<WorkflowHistoricProcessInstanceDto> search(WorkflowFilterDto filter);

	/**
	 * Search historic process instance by ID. Historic process have same ID as process.
	 * @param historicProcessInstanceId
	 * @return
	 */
	WorkflowHistoricProcessInstanceDto get(String historicProcessInstanceId);

	/**
	 * Generate diagram for historic process. In diagram are highlight used paths. If isn't process ended, then only current activity is highlight.
	 * @param processInstanceId
	 * @return
	 */
	InputStream getDiagram(String processInstanceId);

}
