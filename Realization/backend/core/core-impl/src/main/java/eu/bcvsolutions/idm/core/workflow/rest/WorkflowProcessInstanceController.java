package eu.bcvsolutions.idm.core.workflow.rest;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Rest controller for workflow instance processes
 * 
 * TODO: secure endpoints
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/workflow-processes")
@Api(
		value = WorkflowProcessInstanceController.TAG,  
		tags = { WorkflowProcessInstanceController.TAG }, 
		description = "Running WF processes",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class WorkflowProcessInstanceController extends AbstractReadWriteDtoController<WorkflowProcessInstanceDto, WorkflowFilterDto> {

	protected static final String TAG = "Workflow - process instances";
	//
	@Autowired
	private LookupService entityLookupService;
	private final WorkflowProcessInstanceService workflowProcessInstanceService;
	
	@Autowired
	public WorkflowProcessInstanceController(
			WorkflowProcessInstanceService workflowProcessInstanceService) {
		super(workflowProcessInstanceService);
		//
		Assert.notNull(workflowProcessInstanceService);
		//
		this.workflowProcessInstanceService = workflowProcessInstanceService;
	}

	/**
	 * Search workflow processes instances for given identity username and
	 * process definition key
	 * 
	 * @param size
	 * @param page
	 * @param sort
	 * @param identity
	 * @param processDefinitionKey
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/search/quick")
	@ApiOperation(
			value = "Search process instances", 
			nickname = "searchQuickProcessInstances", 
			tags = { WorkflowProcessInstanceController.TAG })
	public Resources<?> searchQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{backendId}")
	@ApiOperation(
			value = "Delete process instances", 
			nickname = "deleteProcessInstances", 
			tags = { WorkflowProcessInstanceController.TAG })
	public ResponseEntity<WorkflowProcessInstanceDto> delete(
			@ApiParam(value = "Process instance id.", required = true)
			@PathVariable @NotNull String backendId) {
		return new ResponseEntity<WorkflowProcessInstanceDto>(
				workflowProcessInstanceService.delete(backendId, null), HttpStatus.OK);
	}
	
	@Override
	protected WorkflowFilterDto toFilter(MultiValueMap<String, Object> parameters) {
		WorkflowFilterDto filter = super.toFilter(parameters);
		String applicant = getParameterConverter().toString(parameters, "identity");
		if (applicant != null) {
				IdmIdentityDto identityDto = (IdmIdentityDto) entityLookupService.lookupDto(IdmIdentityDto.class, applicant);
				filter.getEqualsVariables().put(WorkflowProcessInstanceService.APPLICANT_IDENTIFIER, identityDto.getId());
		}
		
		return filter;
	}

}
