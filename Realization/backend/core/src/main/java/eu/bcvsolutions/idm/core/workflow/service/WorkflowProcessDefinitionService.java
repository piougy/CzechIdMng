package eu.bcvsolutions.idm.core.workflow.service;

import java.util.List;

import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessDefinitionDto;

public interface WorkflowProcessDefinitionService {

	List<WorkflowProcessDefinitionDto> findAllProcessDefinitions();

	WorkflowProcessDefinitionDto get(String definitionId);

	String getProcessDefinitionId(String processDefinitionKey);

}