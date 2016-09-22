package eu.bcvsolutions.idm.core.workflow.service;

import java.io.InputStream;
import java.util.List;

import eu.bcvsolutions.idm.core.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessDefinitionDto;
/**
 * Service for control workflow definitions.
 * @author svandav
 *
 */
public interface WorkflowProcessDefinitionService {
	
	public static final String SORT_BY_KEY = "key";
	public static final String SORT_BY_NAME = "name";

	/**
	 * Find all last version and active process definitions
	 */
	List<WorkflowProcessDefinitionDto> findAllProcessDefinitions();

	/**
	 * Search process definition by key. Return only last active version of definition.
	 * @param processDefinitionKey
	 * @return
	 */
	WorkflowProcessDefinitionDto get(String processDefinitionKey);

	/**
	 * Find last version of process definition by key and return his ID
	 * 
	 * @param processDefinitionKey
	 * @return
	 */
	String getProcessDefinitionId(String processDefinitionKey);

	/**
	 * Generate diagram for process definition ID
	 */
	InputStream getDiagram(String definitionId);
	
	/**
	 * Generate diagram for process definition key
	 */
	InputStream getDiagramByKey(String definitionKey);

	/**
	 * Find process definition by ID
	 * 
	 * @param definitionId
	 * @return
	 */
	WorkflowProcessDefinitionDto getById(String definitionId);
	
	/**
	 * Search process definitions
	 * @param filter
	 * @return
	 */
	ResourcesWrapper<WorkflowProcessDefinitionDto> search(WorkflowFilterDto filter);

}
