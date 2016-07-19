package eu.bcvsolutions.idm.core.workflow.service;

import java.io.InputStream;
import java.util.List;

import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessDefinitionDto;

public interface WorkflowProcessDefinitionService {

	List<WorkflowProcessDefinitionDto> findAllProcessDefinitions();

	WorkflowProcessDefinitionDto get(String processDefinitionKey);

	String getProcessDefinitionId(String processDefinitionKey);

	InputStream getDiagram(String definitionId);

	InputStream getDiagramByKey(String definitionKey);

	WorkflowProcessDefinitionDto getById(String definitionId);

}