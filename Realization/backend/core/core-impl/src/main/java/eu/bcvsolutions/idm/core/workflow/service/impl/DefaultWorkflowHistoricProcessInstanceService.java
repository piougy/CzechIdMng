package eu.bcvsolutions.idm.core.workflow.service.impl;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.rest.AbstractBaseDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessDefinitionService;

/**
 * Default implementation of workflow process historic service.
 * 
 * @author svandav
 *
 */
@Service
public class DefaultWorkflowHistoricProcessInstanceService 
		extends AbstractBaseDtoService<WorkflowHistoricProcessInstanceDto, WorkflowFilterDto> 
		implements WorkflowHistoricProcessInstanceService {

	private static final String DEFINITION_ID_DELIMITER = ":";

	@Autowired
	private HistoryService historyService;
	@Autowired
	private SecurityService securityService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private WorkflowProcessDefinitionService definitionService;

	@Override
	public Page<WorkflowHistoricProcessInstanceDto> find(WorkflowFilterDto filter, Pageable pageable,
			BasePermission... permission) {
	
		HistoricProcessInstanceQuery query = this.getQuery(filter, pageable, permission);
		if (pageable == null) {
			pageable = PageRequest.of(0, Integer.MAX_VALUE);
		}
		long count = query.count();
		List<HistoricProcessInstance> processInstances = query.listPage((pageable.getPageNumber()) * pageable.getPageSize(), pageable.getPageSize());
		
		String processInstanceId = filter.getProcessInstanceId();
		boolean trimmed = true;
		if (processInstanceId != null) {
			// Process variables will be included only for get by instance ID
			trimmed = false;
			query.includeProcessVariables();
			query.processInstanceId(processInstanceId);
		}
		List<WorkflowHistoricProcessInstanceDto> dtos = new ArrayList<>();
		if (processInstances != null) {
			for (HistoricProcessInstance instance : processInstances) {
				dtos.add(toDto(instance, trimmed));
			}
		}
		
		return new PageImpl<>(dtos, pageable, count);
	}

	@Override
	public long count(WorkflowFilterDto filter, BasePermission... permission) {
		HistoricProcessInstanceQuery query = this.getQuery(filter, null, permission);

		return query.count();
	}
	
	/**
	 * Beware, rights on involeved user are evolved here, but given permissions are not used!
	 */
	@Override
	public WorkflowHistoricProcessInstanceDto get(Serializable id, BasePermission... permission) {
		Assert.notNull(id, "Identifier is required.");
		return this.get(String.valueOf(id));
	}

	/**
	 * Rights on involved user are evolved here!
	 */
	@Override
	public WorkflowHistoricProcessInstanceDto get(String historicProcessInstanceId) {
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setProcessInstanceId(historicProcessInstanceId);
		
		List<WorkflowHistoricProcessInstanceDto> resources = this
				.find(filter, PageRequest.of(0, 1))
				.getContent();
		return !resources.isEmpty() ? resources.get(0) : null;
	}

	/**
	 * Generate diagram for process instance. Highlight historic path (activity
	 * and flows)
	 */
	@Override
	public InputStream getDiagram(String processInstanceId) {
		if (processInstanceId == null) {
			throw new ActivitiIllegalArgumentException("No process instance id provided");
		}

		HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery()
				.processInstanceId(processInstanceId).singleResult();

		if (pi == null) {
			throw new ActivitiObjectNotFoundException(
					"Process instance with id " + processInstanceId + " could not be found", ProcessInstance.class);
		}

		ProcessDefinitionEntity pde = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
				.getDeployedProcessDefinition(pi.getProcessDefinitionId());

		if (pde != null && pde.isGraphicalNotationDefined()) {
			BpmnModel bpmnModel = repositoryService.getBpmnModel(pde.getId());
			List<String> historicActivityInstanceList = new ArrayList<String>();
			List<String> highLightedFlows = new ArrayList<String>();
			historicActivityInstanceList = getHighLightedFlows(bpmnModel, processInstanceId, historicActivityInstanceList,
					highLightedFlows);

			ProcessDiagramGenerator diagramGenerator = new DefaultProcessDiagramGenerator();

			return diagramGenerator.generateDiagram(bpmnModel, "png", historicActivityInstanceList, highLightedFlows);

		} else {
			throw new ActivitiException(
					"Process instance with id " + processInstanceId + " has no graphic description");
		}
	}
	
	/**
	 * Get activiti query for historic processes.
	 * 
	 * @param filter
	 * @param pageable
	 * @param permission
	 * @return 
	 */
	protected HistoricProcessInstanceQuery getQuery(WorkflowFilterDto filter, Pageable pageable,
			BasePermission... permission) {

		HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
		if (filter != null) {
			String processDefinitionId = filter.getProcessDefinitionId();
			String processInstanceId = filter.getProcessInstanceId();

			Map<String, Object> equalsVariables = filter.getEqualsVariables();

			if (processInstanceId != null) {
				// Process variables will be included only for get by instance ID
				query.includeProcessVariables();
				query.processInstanceId(processInstanceId);
			}
			if (processDefinitionId != null) {
				query.processDefinitionId(processDefinitionId);
			}
			if (filter.getSuperProcessInstanceId() != null) {
				query.superProcessInstanceId(filter.getSuperProcessInstanceId());
			}
			if (filter.getProcessDefinitionKey() != null) {
				// For case when we have only process id, we will convert him to key
				query.processDefinitionKey(convertProcessIdToKey(filter.getProcessDefinitionKey()));
			}
			if (filter.getName() != null) {
				// with case sensitive
				query.variableValueLike(WorkflowHistoricProcessInstanceService.PROCESS_INSTANCE_NAME, "%" + filter.getName() + "%");
			}
			if (equalsVariables != null) {
				equalsVariables.entrySet().forEach((entry) -> {
					query.variableValueEquals(entry.getKey(), entry.getValue());
				});
			}
		}

		// check security ... only involved user or applicant can work with
		// historic process instance ... admin can see all historic processes every time
		// TODO: refactor and use username/id from filter
		if (!securityService.isAdmin()) {
			// Applicant and Implementer is added to involved user after process
			// (subprocess) started. This modification allow not use OR clause.
			query.involvedUser(securityService.getCurrentId() == null ? UUID.randomUUID().toString() : securityService.getCurrentId().toString());
		}

		String fieldForSort = null;
		boolean ascSort = false;
		boolean descSort = false;
		//
		if (pageable == null) {
			pageable = PageRequest.of(0, Integer.MAX_VALUE);
		}
		Sort sort = pageable.getSort();
		if (sort != null) {
			for (Order order : sort) {
				if (!StringUtils.isEmpty(order.getProperty())) {
					// TODO: now is implemented only one property sort 
					fieldForSort = order.getProperty();
					if (order.getDirection() == Direction.ASC) {
						ascSort = true;
					} else if (order.getDirection() == Direction.DESC) {
						descSort = true;
					}
					break;
				}
			}
		}

		if (WorkflowHistoricProcessInstanceService.SORT_BY_START_TIME.equals(fieldForSort)) {
			query.orderByProcessInstanceStartTime();
		} else if (WorkflowHistoricProcessInstanceService.SORT_BY_END_TIME.equals(fieldForSort)) {
			query.orderByProcessInstanceEndTime();
		} else {
			query.orderByProcessDefinitionId();
			// there must be default order
			query.asc();
		}
		if (ascSort) {
			query.asc();
		}
		if (descSort) {
			query.desc();
		}
		return query;
	}

	/**
	 * Convert process definition ID to process definition KEY.
	 * 
	 * @param processId
	 * @return
	 */
	private String convertProcessIdToKey(String processId) {
		if (processId == null || !processId.contains(DEFINITION_ID_DELIMITER)) {
			return processId;
		}
		return processId.split(DEFINITION_ID_DELIMITER)[0];
	}

	private List<String> getHighLightedFlows(
			BpmnModel bpmnModel, 
			String processInstanceId,
			List<String> historicActivityInstanceList, 
			List<String> highLightedFlows) {

		List<HistoricActivityInstance> historicActivityInstances = historyService
				.createHistoricActivityInstanceQuery()
				.processInstanceId(processInstanceId)
				.orderByHistoricActivityInstanceEndTime()
				.asc()
				.list();

		Map<String, HistoricActivityInstance> activitiCache = new HashMap<>(historicActivityInstances.size());
		for (HistoricActivityInstance hai : historicActivityInstances) {
			historicActivityInstanceList.add(hai.getActivityId());
			activitiCache.put(hai.getActivityId(), hai);
		}

		// Check if is process still active.
		boolean isProcessActive = runtimeService
				.createProcessInstanceQuery()
				.processInstanceId(processInstanceId)
				.active()
				.count() > 0;
		List<String> currentHighLightedActivities = null;
		if (isProcessActive) {
			// add current activities to list
			currentHighLightedActivities = runtimeService.getActiveActivityIds(processInstanceId);
			historicActivityInstanceList.addAll(currentHighLightedActivities);
		}
		
		// Activities and their sequence-flows.
		List<SequenceFlow> flows = bpmnModel
				.getMainProcess()
				.getFlowElements()
				.stream()
				.filter(flow -> flow instanceof SequenceFlow)
				.map(flow -> (SequenceFlow) flow)
				.collect(Collectors.toList());
		
		flows.forEach(association -> {
			int usedSourceRef = historicActivityInstanceList.indexOf(association.getSourceRef());
			// target ref should be next started activity
			if (usedSourceRef >= 0) {
				int usedTargetRef = historicActivityInstanceList.indexOf(association.getTargetRef());
				if (usedTargetRef >= 0) {
					HistoricActivityInstance sourceActivity = activitiCache.get(association.getSourceRef());
					HistoricActivityInstance targetActivity = activitiCache.get(association.getTargetRef());
					if (sourceActivity.getStartTime().compareTo(targetActivity.getStartTime()) <= 0) {
						highLightedFlows.add(association.getId());
					}
				}
			}
		});

		if (isProcessActive) {
			return currentHighLightedActivities;
		}
		return historicActivityInstanceList;
	}

	private WorkflowHistoricProcessInstanceDto toDto(HistoricProcessInstance instance, boolean trimmed) {
		if (instance == null) {
			return null;
		}

		String instanceName = instance.getName();
		// If we don't have process name, then we try variable with key
		// processInstanceName
		if (instanceName == null && instance.getProcessVariables() != null && instance.getProcessVariables()
				.containsKey(WorkflowHistoricProcessInstanceService.PROCESS_INSTANCE_NAME)) {
			instanceName = (String) instance.getProcessVariables()
					.get(WorkflowHistoricProcessInstanceService.PROCESS_INSTANCE_NAME);
		}
		// If still don't have process name, then we try load variable name from
		// historic variables
		if (instanceName == null || instanceName.isEmpty()) {
			HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery()
					.processInstanceId(instance.getId())
					.variableName(WorkflowHistoricProcessInstanceService.PROCESS_INSTANCE_NAME).singleResult();
			instanceName = variableInstance != null ? (String) variableInstance.getValue() : null;
		}
		if (instanceName == null || instanceName.isEmpty()) {
			instanceName = definitionService.get(instance.getProcessDefinitionId()).getName();
		}

		WorkflowHistoricProcessInstanceDto dto = new WorkflowHistoricProcessInstanceDto();
		dto.setTrimmed(trimmed);
		dto.setId(instance.getId());
		dto.setName(instanceName);
		dto.setProcessDefinitionId(instance.getProcessDefinitionId());
		dto.setProcessDefinitionKey(instance.getProcessDefinitionKey());
		dto.setProcessVariables(instance.getProcessVariables());
		dto.setDeleteReason(instance.getDeleteReason());
		dto.setDurationInMillis(instance.getDurationInMillis());
		dto.setEndTime(instance.getEndTime());
		dto.setStartActivityId(instance.getStartActivityId());
		dto.setStartTime(instance.getStartTime());
		dto.setStartUserId(instance.getStartUserId());
		dto.setSuperProcessInstanceId(instance.getSuperProcessInstanceId());

		return dto;
	}

}
