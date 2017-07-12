package eu.bcvsolutions.idm.core.workflow.service.impl;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import eu.bcvsolutions.idm.core.rest.AbstractBaseDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessDefinitionDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessDefinitionService;

@Service
/**
 * Service for workflow process definition
 * 
 * @author svandav
 *
 */
public class DefaultWorkflowProcessDefinitionService extends AbstractBaseDtoService<WorkflowProcessDefinitionDto, WorkflowFilterDto> implements WorkflowProcessDefinitionService {	
	
	@Autowired
	private RepositoryService repositoryService;

		
	/**
	 * Find all last version and active process definitions
	 */
	@Override
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
			processDefinitionDtos.add(toDto(p));
		}
		return processDefinitionDtos;

	}

	/**
	 * Find last version process definition by key
	 */
	@Override
	public WorkflowProcessDefinitionDto getByName(String definitionKey) {
		ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
		query.active();
		query.latestVersion();
		query.processDefinitionKey((String) definitionKey);
		ProcessDefinition result = query.singleResult();
		if (result != null) {
			return toDto(result);
		}
		return null;
	}
	
	@Override
	public Page<WorkflowProcessDefinitionDto> find(WorkflowFilterDto filter, Pageable pageable,
			BasePermission... permission) {
		ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
		
		query.active();
		
		query.latestVersion();
		
		if (filter != null && filter.getCategory() != null && !StringUtils.isEmpty(filter.getCategory())){
			query.processDefinitionCategoryLike(filter.getCategory() + '%');
		}
		
		// Default sort
		//query.orderByProcessDefinitionId();
		//query.asc();

		if (pageable != null && pageable.getSort() != null) {
			pageable.getSort().forEach(order -> {
				if (SORT_BY_KEY.equals(order.getProperty())) {
					// Sort by key
					query.orderByProcessDefinitionKey();
					if (order.isAscending()) {
						query.asc();
					} else {
						query.desc();
					}
				}
				if (SORT_BY_NAME.equals(order.getProperty())) {
					// Sort by name
					query.orderByProcessDefinitionName();
					if (order.isAscending()) {
						query.asc();
					} else {
						query.desc();
					}
				}
			});

		}
		
		// paginator
		long count = query.count();
		
		List<ProcessDefinition> processInstances = query.listPage((pageable.getPageNumber() * pageable.getPageSize()),
				pageable.getPageSize());
		
		List<WorkflowProcessDefinitionDto> dtos = new ArrayList<>();

		if (processInstances != null) {
			for (ProcessDefinition instance : processInstances) {
				dtos.add(toDto(instance));
			}
		}
		return toPage(dtos, count, pageable.getPageNumber(), pageable.getPageSize());
	}
	
	
	/**
	 * Find last version process definition by key
	 */
	@Override
	public WorkflowProcessDefinitionDto get(Serializable definitionId, BasePermission... permission) {
		Assert.notNull(definitionId, "Id definition cannot be null");
		Assert.isInstanceOf(String.class, definitionId, "Id definition must be String!");
		ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
		query.processDefinitionId((String) definitionId);
		ProcessDefinition result = query.singleResult();
		if (result != null) {
			return toDto(result);
		}
		return null;
	}
	

	/**
	 * Find last version of process definition by key and return his ID
	 * 
	 * @param processDefinitionKey
	 * @return
	 */
	@Override
	public String getProcessDefinitionId(String processDefinitionKey) {
		return repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey)
				.latestVersion().singleResult().getId();
	}

	/**
	 * Generate diagram for process definition.
	 */
	@Override
	public InputStream getDiagram(String definitionId) {
		if (definitionId == null) {
			throw new ActivitiIllegalArgumentException("No process definition id provided");
		}
		return repositoryService.getProcessDiagram(definitionId);
	}

	/**
	 * Generate diagram for process definition.
	 */
	@Override
	public InputStream getDiagramByKey(String definitionKey) {
		if (definitionKey == null) {
			throw new ActivitiIllegalArgumentException("No process definition key provided");
		}
		return getDiagram(this.getProcessDefinitionId(definitionKey));
	}

	private WorkflowProcessDefinitionDto toDto(ProcessDefinition processDefinition) {

		WorkflowProcessDefinitionDto dto = new WorkflowProcessDefinitionDto();
		dto.setId(processDefinition.getKey());
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


	private Page<WorkflowProcessDefinitionDto> toPage(List<WorkflowProcessDefinitionDto> dtos, long totalElements, int pageNumber, int pageSize) {
		PageRequest pageRequest = null;
		if (pageSize > 0) {
			pageRequest = new PageRequest(pageNumber, pageSize);
		}
		Page<WorkflowProcessDefinitionDto> dtoPage = new PageImpl<>(dtos, pageRequest, totalElements);
		return dtoPage;
	}

}
