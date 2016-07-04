package eu.bcvsolutions.idm.core.workflow.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessDefinitionDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessDefinitionService;

@Service
/**
 * Service for workflow process definition
 * 
 * @author svandav
 *
 */
public class DefaultWorkflowProcessDefinitionService implements WorkflowProcessDefinitionService {

	@Autowired
	private RepositoryService repositoryService;

	@Override
	/**
	 * Find all last version and active process definitions
	 */
	public List<WorkflowProcessDefinitionDto> findAllProcessDefinitions() {
		ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
		query.active();
		query.latestVersion();
		List<ProcessDefinition> processDefinitions = query.list();
		List<WorkflowProcessDefinitionDto> processDefinitionDtos = new ArrayList<>();
		if (processDefinitions == null) {
			return processDefinitionDtos;
		}
		for (ProcessDefinition p : processDefinitions) {
			processDefinitionDtos.add(convertToDto(p));
		}
		return processDefinitionDtos;

	}

	/**
	 * Find last version process definition by key
	 */
	@Override
	public WorkflowProcessDefinitionDto get(String definitionKey) {
		ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
		query.active();
		query.latestVersion();
		query.processDefinitionKey(definitionKey);
		ProcessDefinition result = query.singleResult();
		if (result != null) {
			return convertToDto(result);
		}
		return null;
	}
	
	/**
	 * Find last version of process definition by key and return his ID
	 * @param processDefinitionKey
	 * @return 
	 */
	@Override
	public String getProcessDefinitionId(String processDefinitionKey){
		return repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).latestVersion().singleResult().getId();
	}

	private WorkflowProcessDefinitionDto convertToDto(ProcessDefinition processDefinition) {

		WorkflowProcessDefinitionDto dto = new WorkflowProcessDefinitionDto();
		dto.setId(processDefinition.getId());
		dto.setCategory(processDefinition.getCategory());
		dto.setDeploymentId(processDefinition.getDeploymentId());
		dto.setDescription(processDefinition.getDescription());
		dto.setDiagramResourceName(processDefinition.getDiagramResourceName());
		dto.setGraphicalNotation(processDefinition.hasGraphicalNotation());
		dto.setKey(processDefinition.getKey());
		dto.setName(processDefinition.getName());
		dto.setResourceName(processDefinition.getResourceName());
		dto.setStartFormKey(processDefinition.hasStartFormKey());
		dto.setSuspended(processDefinition.isSuspended());
		dto.setTenantId(processDefinition.getTenantId());
		dto.setVersion(processDefinition.getVersion());

		return dto;

	}

}
