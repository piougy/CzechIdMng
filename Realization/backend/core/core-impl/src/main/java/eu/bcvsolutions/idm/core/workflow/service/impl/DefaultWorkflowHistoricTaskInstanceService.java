package eu.bcvsolutions.idm.core.workflow.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
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

import com.google.common.base.Strings;

import eu.bcvsolutions.idm.core.rest.AbstractBaseDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.workflow.model.dto.IdentityLinkDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessDefinitionDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricTaskInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessDefinitionService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskDefinitionService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;
import java.util.Map;

/**
 * Default implementation of workflow process historic service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultWorkflowHistoricTaskInstanceService 
		extends AbstractBaseDtoService<WorkflowHistoricTaskInstanceDto, WorkflowFilterDto> 
		implements WorkflowHistoricTaskInstanceService {

	@Autowired
	private HistoryService historyService;
	@Autowired
	private SecurityService securityService;
	@Autowired
	private WorkflowTaskDefinitionService workflowTaskDefinitionService;
	@Autowired
	private WorkflowProcessDefinitionService workflowProcessDefinitionService;
	@Autowired
	private WorkflowTaskInstanceService workflowTaskInstanceService;

	@Override
	public Page<WorkflowHistoricTaskInstanceDto> find(Pageable pageable, BasePermission... permission) {
		return this.find(new WorkflowFilterDto(), pageable, permission);
	}
	
	@Override
	public Page<WorkflowHistoricTaskInstanceDto> find(WorkflowFilterDto filter, Pageable pageable,
			BasePermission... permission) {
		if (pageable == null) {
			// pageable is required
			pageable = PageRequest.of(0, Integer.MAX_VALUE);
		}
		
		String processDefinitionId = filter.getProcessDefinitionId();
		String processInstanceId = filter.getProcessInstanceId();

		HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables();

		query.includeProcessVariables();
		
		if (filter.getId() != null){
			query.taskId(filter.getId().toString());
		}
		if (processInstanceId != null) {
			query.processInstanceId(processInstanceId);
		}
		if (processDefinitionId != null) {
			query.processDefinitionId(processDefinitionId);
		}
		if (filter.getProcessDefinitionKey() != null) {
			query.processDefinitionKey(filter.getProcessDefinitionKey());
		}
		// check security ... only assigneed user to task or process applicant or implementer can work with
		// historic task instance ... admin can see all historic tasks every time
		if(!securityService.isAdmin()) {
			// TODO Now we don't have detail for historic task. When we need detail, then we will need create different projection (detail can't be read by applicant)
			
			String loggedUserId = securityService.getCurrentId().toString();
			query.taskInvolvedUser(loggedUserId);
		}
		
		String fieldForSort = null;
		boolean ascSort = false;
		boolean descSort = false;
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
			
		if (WorkflowHistoricTaskInstanceService.SORT_BY_CREATE_TIME.equals(fieldForSort)) {
			query.orderByTaskCreateTime();
		} else if (WorkflowHistoricTaskInstanceService.SORT_BY_END_TIME.equals(fieldForSort)) {
			query.orderByHistoricTaskInstanceEndTime();
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
		long count = query.count();
		
		List<HistoricTaskInstance> processInstances = query.listPage((pageable.getPageNumber()) * pageable.getPageSize(),
				pageable.getPageSize());
		List<WorkflowHistoricTaskInstanceDto> dtos = new ArrayList<>();

		if (processInstances != null) {
			processInstances.forEach((instance) -> {
				dtos.add(toResource(instance));
			});
		}

		return new PageImpl<>(dtos, pageable, count);
	}
	
	@Override
	public WorkflowHistoricTaskInstanceDto get(Serializable id, BasePermission... permission) {
		Assert.notNull(id, "Identifier is required.");
		return this.get(String.valueOf(id));
	}

	@Override
	public WorkflowHistoricTaskInstanceDto get(String historicTaskInstanceId) {
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setId(UUID.fromString(historicTaskInstanceId));
		List<WorkflowHistoricTaskInstanceDto> resources = this
				.find(filter, PageRequest.of(0, 1))
				.getContent();
		return !resources.isEmpty() ? resources.get(0) : null;
	}
	
	@Override
	public WorkflowHistoricTaskInstanceDto getTaskByProcessId(String processId) {
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setProcessInstanceId(processId);
		List<WorkflowHistoricTaskInstanceDto> resources = (List<WorkflowHistoricTaskInstanceDto>) this
				.find(filter, PageRequest.of(0, 1))
				.getContent();
		return !resources.isEmpty() ? resources.get(0) : null;
	}

	private WorkflowHistoricTaskInstanceDto toResource(HistoricTaskInstance task) {
		if (task == null) {
			return null;
		}

		WorkflowHistoricTaskInstanceDto dto = new WorkflowHistoricTaskInstanceDto();
		// Not working ... variables are not local but global in process scope
		// ... may be logged level?
		// TODO can be slow
		if (task.getTaskLocalVariables() != null) {
			if(task.getTaskLocalVariables().containsKey(WorkflowHistoricTaskInstanceService.TASK_COMPLETE_DECISION)) {
				dto.setCompleteTaskDecision((String)
						task.getTaskLocalVariables().get(WorkflowHistoricTaskInstanceService.TASK_COMPLETE_DECISION));
			}
			if(task.getTaskLocalVariables().containsKey(WorkflowHistoricTaskInstanceService.TASK_COMPLETE_MESSAGE)) {
				dto.setCompleteTaskMessage((String)
						task.getTaskLocalVariables().get(WorkflowHistoricTaskInstanceService.TASK_COMPLETE_MESSAGE));
			}
		}
		dto.setId(task.getId());
		dto.setName(task.getName());
		dto.setProcessDefinitionId(task.getProcessDefinitionId());
		dto.setPriority(task.getPriority());
		dto.setAssignee(task.getAssignee());
		dto.setCreated(task.getCreateTime());
		dto.setDescription(task.getDescription());
		dto.setProcessInstanceId(task.getProcessInstanceId());
		dto.setDeleteReason(task.getDeleteReason());
		dto.setDurationInMillis(task.getDurationInMillis());
		dto.setEndTime(task.getEndTime());
		dto.setStartTime(task.getStartTime());
		dto.setCreateTime(task.getCreateTime());
		dto.setDueDate(task.getDueDate());
		dto.setFormKey(task.getFormKey());
		
		Map<String, Object> taskVariables = task.getTaskLocalVariables();
		Map<String, Object> processVariables = task.getProcessVariables();

		// Add applicant username to task dto (for easier work)
		if (processVariables != null
				&& processVariables.containsKey(WorkflowProcessInstanceService.APPLICANT_IDENTIFIER)) {
			dto.setApplicant(
					(String) processVariables.get(WorkflowProcessInstanceService.APPLICANT_IDENTIFIER).toString());
		}
		dto.setVariables(processVariables);
		workflowTaskInstanceService.convertToDtoVariables(dto, taskVariables);
		
		// TODO: Prevent selection of the definition here (performance).
		dto.setDefinition(workflowTaskDefinitionService.searchTaskDefinitionById(task.getProcessDefinitionId(),
				task.getTaskDefinitionKey()));

		if (!Strings.isNullOrEmpty(task.getProcessDefinitionId())) {
			WorkflowProcessDefinitionDto processDefinition = workflowProcessDefinitionService
					.get(task.getProcessDefinitionId());
			if (processDefinition != null) {
				dto.setProcessDefinitionKey(processDefinition.getKey());
			}
		}

		// Search and add identity links to dto (It means all user
		// (assigned/candidates/group) for this task)
		List<HistoricIdentityLink> identityLinks = historyService.getHistoricIdentityLinksForTask(task.getId());
		if (identityLinks != null) {
			List<IdentityLinkDto> identityLinksDtos = new ArrayList<>(identityLinks.size());
			identityLinks.forEach((identityLink) -> {
				identityLinksDtos.add(this.convertHistoricIdentityLink(identityLink));
			});
			dto.getIdentityLinks().addAll(identityLinksDtos);
		}

		return dto;
	}
	
	/**
	 * Convert given activiti historic link to IdentityLinkDto.
	 * 
	 * @param link
	 * @return 
	 */
	private IdentityLinkDto convertHistoricIdentityLink(HistoricIdentityLink link) {
		if (link == null) {
			return null;
		}

		IdentityLinkDto dto = new IdentityLinkDto();
		dto.setGroupId(link.getGroupId());
		dto.setType(link.getType());
		dto.setUserId(link.getUserId());
		return dto;
	}

}
