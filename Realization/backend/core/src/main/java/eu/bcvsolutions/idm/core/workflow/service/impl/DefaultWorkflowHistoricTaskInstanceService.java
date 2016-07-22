package eu.bcvsolutions.idm.core.workflow.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.security.service.SecurityService;
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
		// check security ... only assigneed user can work with
		// historic task instance
		String loggedUsername = securityService.getUsername();
		query.or();
		query.processVariableValueEquals(WorkflowProcessInstanceService.APPLICANT_USERNAME, loggedUsername);
		// TODO When I use two OR then is count wrong
		//query.processVariableValueEquals(WorkflowProcessInstanceService.IMPLEMENTER_USERNAME, loggedUsername);
		query.taskInvolvedUser(loggedUsername);
		// TODO admin
		query.endOr();

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

		ResourcesWrapper<WorkflowHistoricTaskInstanceDto> result = new ResourcesWrapper<>(dtos, count, totalPage,
				filter.getPageNumber(), filter.getPageSize());
		return result;
	}

	@Override
	public WorkflowHistoricTaskInstanceDto get(String historicTaskInstanceId) {
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setId(historicTaskInstanceId);
		filter.setSortAsc(true);
		ResourcesWrapper<WorkflowHistoricTaskInstanceDto> resource = this.search(filter);
		return resource.getResources() != null && !resource.getResources().isEmpty() ? resource.getResources().iterator().next() : null;
	}

	private WorkflowHistoricTaskInstanceDto toResource(HistoricTaskInstance instance) {
		if (instance == null) {
			return null;
		}

		WorkflowHistoricTaskInstanceDto dto = new WorkflowHistoricTaskInstanceDto();
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

		return dto;
	}

}
