package eu.bcvsolutions.idm.core.workflow.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricTaskInstanceService;

/**
 * Rest controller for workflow historic instance tasks
 * 
 * TODO: secure endpoints
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/workflow-history-tasks")
public class WorkflowHistoricTaskInstanceController {

	@Autowired
	private WorkflowHistoricTaskInstanceService workflowHistoricTaskInstanceService;
	@Value("${spring.data.rest.defaultPageSize}")
	private int defaultPageSize;

	/**
	 * Search historic instances of tasks for logged user
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/search")
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowHistoricTaskInstanceDto>>> search(
			@RequestBody WorkflowFilterDto filter) {
		ResourcesWrapper<WorkflowHistoricTaskInstanceDto> result = workflowHistoricTaskInstanceService.search(filter);

		List<WorkflowHistoricTaskInstanceDto> processes = (List<WorkflowHistoricTaskInstanceDto>) result.getResources();
		List<ResourceWrapper<WorkflowHistoricTaskInstanceDto>> wrappers = new ArrayList<>();

		for (WorkflowHistoricTaskInstanceDto process : processes) {
			wrappers.add(new ResourceWrapper<WorkflowHistoricTaskInstanceDto>(process));
		}
		ResourcesWrapper<ResourceWrapper<WorkflowHistoricTaskInstanceDto>> resources = new ResourcesWrapper<ResourceWrapper<WorkflowHistoricTaskInstanceDto>>(
				wrappers);
		resources.setPage(result.getPage());
		return new ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowHistoricTaskInstanceDto>>>(resources,
				HttpStatus.OK);
	}

	/**
	 * Search historic instances of tasks with for logged user. Use quick search
	 * api
	 * 
	 * @param size
	 * @param page
	 * @param sort
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/search/quick")
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowHistoricTaskInstanceDto>>> searchQuick(
			@RequestParam(required = false) Integer size, @RequestParam(required = false) Integer page,
			@RequestParam(required = false) String sort, @RequestParam String processInstanceId) {

		WorkflowFilterDto filter = new WorkflowFilterDto(size != null ? size : defaultPageSize);
		if (page != null) {
			filter.setPageNumber(page);
		}
		filter.setProcessInstanceId(processInstanceId);
		filter.initSort(sort);

		return this.search(filter);
	}

	/**
	 * Get detail historic task instance by his ID (for logged user)
	 * 
	 * @param historicTaskInstanceId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{historicTaskInstanceId}")
	public ResponseEntity<ResourceWrapper<WorkflowHistoricTaskInstanceDto>> get(
			@PathVariable String historicTaskInstanceId) {
		ResourceWrapper<WorkflowHistoricTaskInstanceDto> resource = new ResourceWrapper<WorkflowHistoricTaskInstanceDto>(
				workflowHistoricTaskInstanceService.get(historicTaskInstanceId));
		return new ResponseEntity<ResourceWrapper<WorkflowHistoricTaskInstanceDto>>(resource, HttpStatus.OK);
	}

}
