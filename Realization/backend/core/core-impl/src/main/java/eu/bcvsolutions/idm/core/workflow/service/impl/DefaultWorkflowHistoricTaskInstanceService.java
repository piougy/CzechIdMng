package eu.bcvsolutions.idm.core.workflow.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.task.IdentityLinkType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricTaskInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

/**
 * Default implementation of workflow process historic service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultWorkflowHistoricTaskInstanceService implements WorkflowHistoricTaskInstanceService {

	@Autowired
	private HistoryService historyService;
	
	@Autowired
	private SecurityService securityService;

	@Override
	public ResourcesWrapper<WorkflowHistoricTaskInstanceDto> search(WorkflowFilterDto filter) {
		String processDefinitionId = filter.getProcessDefinitionId();
		String processInstanceId = filter.getProcessInstanceId();

		HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

		query.includeProcessVariables();
		
		if (filter.getId() != null){
			query.taskId(filter.getId());
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
			
			String loggedUsername = securityService.getUsername();
			query.or();
			query.processVariableValueEquals(WorkflowProcessInstanceService.APPLICANT_USERNAME, loggedUsername);
			query.processVariableValueEquals(WorkflowProcessInstanceService.IMPLEMENTER_USERNAME, loggedUsername);
			query.taskInvolvedUser(loggedUsername);
			// TODO admin
			query.endOr();
		}
		if (WorkflowHistoricTaskInstanceService.SORT_BY_CREATE_TIME.equals(filter.getSortByFields())) {
			query.orderByTaskCreateTime();
		} else if (WorkflowHistoricTaskInstanceService.SORT_BY_END_TIME.equals(filter.getSortByFields())) {
			query.orderByHistoricTaskInstanceEndTime();
		} else {
			query.orderByProcessDefinitionId();
		}
		if (filter.isSortAsc()) {
			query.asc();
		}
		if (filter.isSortDesc()) {
			query.desc();
		}
		long count = query.count();
		List<HistoricTaskInstance> processInstances = query.listPage((filter.getPageNumber()) * filter.getPageSize(),
				filter.getPageSize());
		List<WorkflowHistoricTaskInstanceDto> dtos = new ArrayList<>();

		if (processInstances != null) {
			for (HistoricTaskInstance instance : processInstances) {
				dtos.add(toResource(instance));
			}
		}
		double totalPageDouble = ((double) count / filter.getPageSize());
		double totlaPageFlorred = Math.floor(totalPageDouble);
		long totalPage = 0;
		if (totalPageDouble > totlaPageFlorred) {
			totalPage = (long) (totlaPageFlorred + 1);
		}

		return new ResourcesWrapper<>(dtos, count, totalPage, filter.getPageNumber(), filter.getPageSize());
	}

	@Override
	public WorkflowHistoricTaskInstanceDto get(String historicTaskInstanceId) {
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setId(historicTaskInstanceId);
		filter.setSortAsc(true);
		Collection<WorkflowHistoricTaskInstanceDto> resources = this.search(filter).getResources();
		return !resources.isEmpty() ? resources.iterator().next() : null;
	}
	
	@Override
	public WorkflowHistoricTaskInstanceDto getTaskByProcessId(String processId) {
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setProcessInstanceId(processId);
		filter.setSortAsc(true);
		Collection<WorkflowHistoricTaskInstanceDto> resources = this.search(filter).getResources();
		return !resources.isEmpty() ? resources.iterator().next() : null;
	}

	private WorkflowHistoricTaskInstanceDto toResource(HistoricTaskInstance instance) {
		if (instance == null) {
			return null;
		}

		WorkflowHistoricTaskInstanceDto dto = new WorkflowHistoricTaskInstanceDto();
		// Not working ... variables are not local but global in process scope
		// ... may be logged level?
		// if(instance.getTaskLocalVariables() != null &&
		// instance.getTaskLocalVariables().containsKey(WorkflowHistoricTaskInstanceService.TASK_COMPLETE_DECISION)){
		// dto.setCompleteTaskDecision((String)
		// instance.getTaskLocalVariables().get(WorkflowHistoricTaskInstanceService.TASK_COMPLETE_DECISION));
		// }	
		dto.setId(instance.getId());
		dto.setName(instance.getName());
		dto.setProcessDefinitionId(instance.getProcessDefinitionId());
		dto.setTaskVariables(instance.getTaskLocalVariables());
		dto.setDeleteReason(instance.getDeleteReason());
		dto.setDurationInMillis(instance.getDurationInMillis());
		dto.setEndTime(instance.getEndTime());
		dto.setStartTime(instance.getStartTime());
		dto.setPriority(instance.getPriority());
		dto.setAssignee(instance.getAssignee());
		dto.setCreateTime(instance.getCreateTime());
		dto.setDueDate(instance.getDueDate());
		
		List<HistoricIdentityLink> identityLinks = historyService.getHistoricIdentityLinksForTask(instance.getId());
		if (identityLinks != null && !identityLinks.isEmpty()) {
			List<String> candicateUsers = new ArrayList<>();
			for	(HistoricIdentityLink identity : identityLinks) {
				if (IdentityLinkType.CANDIDATE.equals(identity.getType())) {
					candicateUsers.add(identity.getUserId());
				}
			}
			dto.setCandicateUsers(candicateUsers);
		}
		
		return dto;
	}

}
