package eu.bcvsolutions.idm.core.workflow.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.AbstractReadDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
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
public class WorkflowHistoricTaskInstanceController extends AbstractReadDtoController<WorkflowHistoricTaskInstanceDto, WorkflowFilterDto>{

	protected static final String TAG = "Workflow - task instances history";
	//
	@Value("${spring.data.rest.defaultPageSize}")
	private int defaultPageSize;
	
	@Autowired
	public WorkflowHistoricTaskInstanceController(
			WorkflowHistoricTaskInstanceService service) {
		super(service);
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
	public Resources<?> searchQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return find(parameters, pageable);
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
	public ResponseEntity<?> get(
			@ApiParam(value = "Historic task instance id.", required = true)
			@PathVariable String backendId) {
		return super.get(backendId);
	}

}
