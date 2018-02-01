package eu.bcvsolutions.idm.core.workflow.rest;

import java.io.IOException;
import java.io.InputStream;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
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
public class WorkflowHistoricProcessInstanceController extends AbstractReadDtoController<WorkflowHistoricProcessInstanceDto, WorkflowFilterDto> {

	protected static final String TAG = "Workflow - process instances history";
	//
	@Value("${spring.data.rest.defaultPageSize}")
	private int defaultPageSize;

	private WorkflowHistoricProcessInstanceService workflowHistoricProcessInstanceService;

	@Autowired
	public WorkflowHistoricProcessInstanceController(
			WorkflowHistoricProcessInstanceService service) {
		super(service);
		//
		this.workflowHistoricProcessInstanceService = service;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/search/quick")
	@ApiOperation(
			value = "Search historic process instances", 
			nickname = "searchQuickHistoricProcessInstances", 
			tags = { WorkflowHistoricProcessInstanceController.TAG })
	public Resources<?> searchQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return find(parameters, pageable);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{backendId}")
	@ApiOperation(
			value = "Historic process instance detail", 
			nickname = "getHistoricProcessInstance", 
			response = WorkflowHistoricProcessInstanceDto.class, 
			tags = { WorkflowHistoricProcessInstanceController.TAG })
	public ResponseEntity<?> get(
			@ApiParam(value = "Historic process instance id.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
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
