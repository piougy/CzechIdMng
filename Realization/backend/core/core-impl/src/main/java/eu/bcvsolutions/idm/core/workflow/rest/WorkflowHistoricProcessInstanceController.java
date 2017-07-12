package eu.bcvsolutions.idm.core.workflow.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

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
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Rest controller for workflow historic instance processes
 * 
 * TODO: secure endpoints
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/workflow-history-processes")
@Api(
		value = WorkflowHistoricProcessInstanceController.TAG,  
		tags = { WorkflowHistoricProcessInstanceController.TAG }, 
		description = "Read WF audit",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class WorkflowHistoricProcessInstanceController {

	protected static final String TAG = "Workflow - process instances history";
	//
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
	@ApiOperation(
			value = "Search historic process instances (/search/quick alias)", 
			nickname = "searchHistoricProcessInstances", 
			tags = { WorkflowHistoricProcessInstanceController.TAG })
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
	@ApiOperation(
			value = "Search historic process instances", 
			nickname = "searchQuickHistoricProcessInstances", 
			tags = { WorkflowHistoricProcessInstanceController.TAG })
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowHistoricProcessInstanceDto>>> searchQuick(
			@RequestParam(required = false) Integer size, 
			@RequestParam(required = false) Integer page, 
			@RequestParam(required = false) String sort,
			@RequestParam(required = false) String name, 
			@RequestParam(required = false) String processDefinition, 
			@RequestParam(required = false) String superProcessInstanceId) {
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

	@RequestMapping(method = RequestMethod.GET, value = "/{backendId}")
	@ApiOperation(
			value = "Historic process instance detail", 
			nickname = "getHistoricProcessInstance", 
			response = WorkflowHistoricProcessInstanceDto.class, 
			tags = { WorkflowHistoricProcessInstanceController.TAG })
	public ResponseEntity<ResourceWrapper<WorkflowHistoricProcessInstanceDto>> get(
			@ApiParam(value = "Historic process instance id.", required = true)
			@PathVariable @NotNull String backendId) {
		ResourceWrapper<WorkflowHistoricProcessInstanceDto> resource = new ResourceWrapper<WorkflowHistoricProcessInstanceDto>(
				workflowHistoricProcessInstanceService.get(backendId));
		return new ResponseEntity<ResourceWrapper<WorkflowHistoricProcessInstanceDto>>(resource, HttpStatus.OK);
	}

	/**
	 * Generate process instance diagram image
	 * 
	 * @param historicProcessInstanceId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/diagram", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
	@ApiOperation(
			value = "Historic process instance diagram", 
			nickname = "getHistoricProcessInstanceDiagram", 
			response = WorkflowHistoricProcessInstanceDto.class, 
			tags = { WorkflowHistoricProcessInstanceController.TAG })
	public ResponseEntity<InputStreamResource> getDiagram(
			@ApiParam(value = "Historic process instance id.", required = true)
			@PathVariable @NotNull String backendId) {
		// check rights
		WorkflowHistoricProcessInstanceDto result = workflowHistoricProcessInstanceService.get(backendId);
		if (result == null) {
			throw new ResultCodeException(CoreResultCode.FORBIDDEN);
		}
		InputStream is = workflowHistoricProcessInstanceService.getDiagram(backendId);
		try {
			return ResponseEntity.ok().contentLength(is.available()).contentType(MediaType.IMAGE_PNG)
					.body(new InputStreamResource(is));
		} catch (IOException e) {
			throw new ResultCodeException(CoreResultCode.INTERNAL_SERVER_ERROR, e);
		}
	}

}
