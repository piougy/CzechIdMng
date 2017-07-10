package eu.bcvsolutions.idm.core.workflow.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;

/**
 * Rest controller for workflow historic instance processes
 * 
 * TODO: secure endpoints
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/workflow-history-processes")
public class WorkflowHistoricProcessInstanceController {

	@Value("${spring.data.rest.defaultPageSize}")
	private int defaultPageSize;
	@Autowired
	private WorkflowHistoricProcessInstanceService workflowHistoricProcessInstanceService;

	/**
	 * Search historic instances of processes with same variables and for logged
	 * user
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/search")
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowHistoricProcessInstanceDto>>> search(
			@RequestBody WorkflowFilterDto filter) {
		ResourcesWrapper<WorkflowHistoricProcessInstanceDto> result = workflowHistoricProcessInstanceService
				.search(filter);
		;
		List<WorkflowHistoricProcessInstanceDto> processes = (List<WorkflowHistoricProcessInstanceDto>) result
				.getResources();
		List<ResourceWrapper<WorkflowHistoricProcessInstanceDto>> wrappers = new ArrayList<>();

		for (WorkflowHistoricProcessInstanceDto process : processes) {
			wrappers.add(new ResourceWrapper<WorkflowHistoricProcessInstanceDto>(process));
		}
		ResourcesWrapper<ResourceWrapper<WorkflowHistoricProcessInstanceDto>> resources = new ResourcesWrapper<ResourceWrapper<WorkflowHistoricProcessInstanceDto>>(
				wrappers);
		resources.setPage(result.getPage());
		return new ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowHistoricProcessInstanceDto>>>(resources,
				HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/search/quick")
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowHistoricProcessInstanceDto>>> searchQuick(
			@RequestParam(required = false) Integer size, @RequestParam(required = false) Integer page, @RequestParam(required = false) String sort,
			@RequestParam(required = false) String name, @RequestParam(required = false) String processDefinition, @RequestParam(required = false) String superProcessInstanceId) {

		WorkflowFilterDto filter = new WorkflowFilterDto(size != null ? size : defaultPageSize);
		if(page != null){
			filter.setPageNumber(page);
		}
		filter.setProcessDefinitionKey(processDefinition);
		filter.setSuperProcessInstanceId(superProcessInstanceId);
		filter.setName(name != null && !name.isEmpty() ? name : null);
		filter.initSort(sort);

		return this.search(filter);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{historicProcessInstanceId}")
	public ResponseEntity<ResourceWrapper<WorkflowHistoricProcessInstanceDto>> get(
			@PathVariable String historicProcessInstanceId) {
		ResourceWrapper<WorkflowHistoricProcessInstanceDto> resource = new ResourceWrapper<WorkflowHistoricProcessInstanceDto>(
				workflowHistoricProcessInstanceService.get(historicProcessInstanceId));
		return new ResponseEntity<ResourceWrapper<WorkflowHistoricProcessInstanceDto>>(resource, HttpStatus.OK);
	}

	/**
	 * Generate process instance diagram image
	 * 
	 * @param historicProcessInstanceId
	 * @return
	 */
	@RequestMapping(value = "/{historicProcessInstanceId}/diagram", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
	@ResponseBody
	public ResponseEntity<InputStreamResource> getDiagram(@PathVariable String historicProcessInstanceId) {
		// check rights
		WorkflowHistoricProcessInstanceDto result = workflowHistoricProcessInstanceService
				.get(historicProcessInstanceId);
		if (result == null) {
			throw new ResultCodeException(CoreResultCode.FORBIDDEN);
		}
		InputStream is = workflowHistoricProcessInstanceService.getDiagram(historicProcessInstanceId);
		try {
			return ResponseEntity.ok().contentLength(is.available()).contentType(MediaType.IMAGE_PNG)
					.body(new InputStreamResource(is));
		} catch (IOException e) {
			throw new ResultCodeException(CoreResultCode.INTERNAL_SERVER_ERROR, e);
		}
	}

}
