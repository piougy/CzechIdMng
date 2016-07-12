package eu.bcvsolutions.idm.core.workflow.service;

import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;

import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;

public interface WorkflowProcessInstanceService {
	public final static String OBJECT_TYPE = "objectType";
	public final static String OBJECT_IDENTIFIER = "objectIdentifier";
	public final static String APPLICANT_USERNAME = "applicantUsername";
	public final static String APPLICANT_IDENTIFIER = "applicantIdentifier";
	public final static String IMPLEMENTER_USERNAME = "implementerUsername";
	
	ProcessInstance startProcess(String definitionKey, String objectType, String applicant, Long objectIdentifier,
			Map<String, Object> variables);

	ResourcesWrapper<WorkflowProcessInstanceDto> search(WorkflowFilterDto filter);

	WorkflowProcessInstanceDto delete(String processInstanceId, String deleteReason);


}
