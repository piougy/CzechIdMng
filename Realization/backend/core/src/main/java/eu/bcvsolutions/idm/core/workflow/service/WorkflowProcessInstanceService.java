package eu.bcvsolutions.idm.core.workflow.service;

import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;

import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;

public interface WorkflowProcessInstanceService {
	public final static String OBJECT_TYPE = "OBJECT_TYPE";
	public final static String OBJECT_IDENTIFIER = "OBJECT_IDENTIFIER";
	public final static String APPLICANT_USERNAME = "APPLICANT_USERNAME";
	public final static String APPLICANT_FULL_NAME = "APPLICANT_FULL_NAME";
	
	ProcessInstance startProcess(String definitionKey, String objectType, String objectIdentifier,
			Map<String, Object> variables);

	ResourcesWrapper<WorkflowProcessInstanceDto> search(WorkflowFilterDto filter);

	WorkflowProcessInstanceDto delete(String processInstanceId, String deleteReason);

}
