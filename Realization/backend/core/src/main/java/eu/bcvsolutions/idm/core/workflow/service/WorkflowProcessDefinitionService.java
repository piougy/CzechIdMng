package eu.bcvsolutions.idm.core.workflow.service;

import java.io.InputStream;
import java.util.List;

import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessDefinitionDto;

public interface WorkflowProcessDefinitionService {
	
	public static final String SORT_BY_KEY = "key";
	public static final String SORT_BY_NAME = "name";

	List<WorkflowProcessDefinitionDto> findAllProcessDefinitions();

	WorkflowProcessDefinitionDto get(String processDefinitionKey);

	String getProcessDefinitionId(String processDefinitionKey);

	InputStream getDiagram(String definitionId);

	InputStream getDiagramByKey(String definitionKey);

	WorkflowProcessDefinitionDto getById(String definitionId);
	
	ResourcesWrapper<WorkflowProcessDefinitionDto> search(WorkflowFilterDto filter);

}
