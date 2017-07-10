package eu.bcvsolutions.idm.core.workflow.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricTaskInstanceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Rest controller for workflow historic instance tasks
 * 
 * TODO: secure endpoints
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/workflow-history-tasks")
@Api(
		value = WorkflowHistoricTaskInstanceController.TAG,  
		tags = { WorkflowHistoricTaskInstanceController.TAG }, 
		description = "Read WF audit",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class WorkflowHistoricTaskInstanceController {

	protected static final String TAG = "Workflow - task instances history";
	//
	@Autowired
	private WorkflowHistoricTaskInstanceService workflowHistoricTaskInstanceService;
	@Value("${spring.data.rest.defaultPageSize}")
	private int defaultPageSize;

	/**
	 * Search historic instances of tasks for logged user
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/search")
	@ApiOperation(
			value = "Search historic task instances (/search/quick alias)", 
			nickname = "searchHistoricTaskInstances", 
			tags = { WorkflowHistoricTaskInstanceController.TAG })
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
	@ApiOperation(
			value = "Search historic task instances", 
			nickname = "searchQuickHistoricTaskInstances", 
			tags = { WorkflowHistoricTaskInstanceController.TAG })
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowHistoricTaskInstanceDto>>> searchQuick(
			@RequestParam(required = false) Integer size, 
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) String sort, 
			@RequestParam String processInstanceId) {

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
	@RequestMapping(method = RequestMethod.GET, value = "/{backendId}")
	@ApiOperation(
			value = "Historic task instance detail", 
			nickname = "getHistoricTaskInstance", 
			response = WorkflowHistoricTaskInstanceDto.class, 
			tags = { WorkflowHistoricTaskInstanceController.TAG })
	public ResponseEntity<ResourceWrapper<WorkflowHistoricTaskInstanceDto>> get(
			@ApiParam(value = "Historic task instance id.", required = true)
			@PathVariable String backendId) {
		ResourceWrapper<WorkflowHistoricTaskInstanceDto> resource = new ResourceWrapper<WorkflowHistoricTaskInstanceDto>(
				workflowHistoricTaskInstanceService.get(backendId));
		return new ResponseEntity<ResourceWrapper<WorkflowHistoricTaskInstanceDto>>(resource, HttpStatus.OK);
	}

}
