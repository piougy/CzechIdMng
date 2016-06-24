package eu.bcvsolutions.idm.core.workflow.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.model.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.model.dto.FormDataWrapperDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowInstanceFilterDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;

/**
 * Rest controller for workflow instance tasks
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = "/api/workflow/tasks/")
public class WorkflowTaskInstanceController {

	@Autowired
	private WorkflowTaskInstanceService workflowTaskInstanceService;

	/**
	 * Search instances of tasks with same variables and for logged user
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "search/")
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>> search(
			@RequestBody WorkflowInstanceFilterDto filter) {

		ResourcesWrapper<WorkflowTaskInstanceDto> result = workflowTaskInstanceService.search(filter);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) result.getResources();
		List<ResourceWrapper<WorkflowTaskInstanceDto>> wrappers = new ArrayList<>();

		for (WorkflowTaskInstanceDto task : tasks) {
			wrappers.add(new ResourceWrapper<WorkflowTaskInstanceDto>(task));
		}
		ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>> resources = new ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>(
				wrappers);
		resources.setPage(result.getPage());
		return new ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>>(resources, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "search/quick")
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>> searchQuick(
			@RequestParam int size, @RequestParam int page, @RequestParam String sort) {
		
		WorkflowInstanceFilterDto filter = new WorkflowInstanceFilterDto();
		filter.setPageNumber(page);
		filter.setPageSize(size);
		return this.search(filter);
	}

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>> getAll() {
		return this.search(new WorkflowInstanceFilterDto());
	}

	@RequestMapping(method = RequestMethod.GET, value = "{taskId}")
	public ResponseEntity<ResourceWrapper<WorkflowTaskInstanceDto>> get(@PathVariable String taskId) {
		ResourceWrapper<WorkflowTaskInstanceDto> resource = new ResourceWrapper<WorkflowTaskInstanceDto>(
				workflowTaskInstanceService.get(taskId));
		return new ResponseEntity<ResourceWrapper<WorkflowTaskInstanceDto>>(resource, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "{taskId}/complete")
	public void completeTask(@PathVariable String taskId, @RequestBody FormDataWrapperDto formData) {
		workflowTaskInstanceService.completeTask(taskId, formData.getDecision(), formData.getFormData());
	}

}
