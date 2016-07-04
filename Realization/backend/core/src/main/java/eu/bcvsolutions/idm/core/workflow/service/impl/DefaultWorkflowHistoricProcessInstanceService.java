package eu.bcvsolutions.idm.core.workflow.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.security.service.SecurityService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

/**
 * Default implementation of workflow process historic service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultWorkflowHistoricProcessInstanceService implements WorkflowHistoricProcessInstanceService {

	@Autowired
	private HistoryService historyService;

	@Autowired
	private SecurityService securityService;

	@Override
	public ResourcesWrapper<WorkflowHistoricProcessInstanceDto> search(WorkflowFilterDto filter) {
		String processDefinitionId = filter.getProcessDefinitionId();
		String processInstanceId = filter.getId();

		Map<String, Object> equalsVariables = filter.getEqualsVariables();

		HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

		query.includeProcessVariables();

		if (processInstanceId != null) {
			query.processInstanceId(processInstanceId);
		}
		if (processDefinitionId != null) {
			query.processDefinitionId(processDefinitionId);
		}
		if (filter.getProcessDefinitionKey() != null) {
			query.processDefinitionKey(filter.getProcessDefinitionKey());
		}
		if (equalsVariables != null) {
			for (String key : equalsVariables.keySet()) {
				query.variableValueEquals(key, equalsVariables.get(key));
			}
		}
		// check security ... only involved user or applicant can work with
		// historic process instance
		query.or();
		query.involvedUser(securityService.getUsername());
		query.variableValueEquals(WorkflowProcessInstanceService.APPLICANT_USERNAME,
				securityService.getOriginalUsername());
		query.endOr();

		if (WorkflowHistoricProcessInstanceService.SORT_BY_START_TIME.equals(filter.getSortByFields())) {
			query.orderByProcessInstanceStartTime();
		} else if (WorkflowHistoricProcessInstanceService.SORT_BY_END_TIME.equals(filter.getSortByFields())) {
			query.orderByProcessInstanceEndTime();
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
		List<HistoricProcessInstance> processInstances = query.listPage((filter.getPageNumber()) * filter.getPageSize(),
				filter.getPageSize());
		List<WorkflowHistoricProcessInstanceDto> dtos = new ArrayList<>();

		if (processInstances != null) {
			for (HistoricProcessInstance instance : processInstances) {
				dtos.add(toResource(instance));
			}
		}
		double totalPageDouble = ((double) count / filter.getPageSize());
		double totlaPageFlorred = Math.floor(totalPageDouble);
		long totalPage = 0;
		if (totalPageDouble > totlaPageFlorred) {
			totalPage = (long) (totlaPageFlorred + 1);
		}

		ResourcesWrapper<WorkflowHistoricProcessInstanceDto> result = new ResourcesWrapper<>(dtos, count, totalPage,
				filter.getPageNumber(), filter.getPageSize());
		return result;
	}
	
	@Override
	public WorkflowHistoricProcessInstanceDto get(String historicProcessInstanceId) {
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setId(historicProcessInstanceId);
		filter.setSortAsc(true);
		ResourcesWrapper<WorkflowHistoricProcessInstanceDto> resource = this.search(filter);
		return resource.getResources() != null ? resource.getResources().iterator().next() : null;
	}

	private WorkflowHistoricProcessInstanceDto toResource(HistoricProcessInstance instance) {
		if (instance == null) {
			return null;
		}

		WorkflowHistoricProcessInstanceDto dto = new WorkflowHistoricProcessInstanceDto();
		dto.setId(instance.getId());
		dto.setName(instance.getName());
		dto.setProcessDefinitionId(instance.getProcessDefinitionId());
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
