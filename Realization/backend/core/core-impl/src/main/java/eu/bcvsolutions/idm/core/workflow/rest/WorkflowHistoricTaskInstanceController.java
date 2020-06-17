package eu.bcvsolutions.idm.core.workflow.rest;

import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDto;
import org.springframework.beans.factory.annotation.Autowired;
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
import eu.bcvsolutions.idm.core.api.service.DelegationManager;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmDelegation_;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricTaskInstanceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;

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
		tags = {WorkflowHistoricTaskInstanceController.TAG},
		description = "Read WF audit",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class WorkflowHistoricTaskInstanceController extends AbstractReadDtoController<WorkflowHistoricTaskInstanceDto, WorkflowFilterDto> {

	protected static final String TAG = "Workflow - task instances history";

	@Autowired
	private DelegationManager delegationManager;

	@Autowired
	public WorkflowHistoricTaskInstanceController(
			WorkflowHistoricTaskInstanceService service) {
		super(service);
	}

	/**
	 * Search historic instances of tasks with for logged user.Use quick search api
	 *
	 * @param parameters
	 * @param pageable
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/search/quick")
	@ApiOperation(
			value = "Search historic task instances",
			nickname = "searchQuickHistoricTaskInstances",
			tags = {WorkflowHistoricTaskInstanceController.TAG})
	public Resources<?> searchQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return find(parameters, pageable);
	}

	/**
	 * Get detail historic task instance by his ID (for logged user)
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{backendId}")
	@ApiOperation(
			value = "Historic task instance detail",
			nickname = "getHistoricTaskInstance",
			response = WorkflowHistoricTaskInstanceDto.class,
			tags = {WorkflowHistoricTaskInstanceController.TAG})
	@Override
	public ResponseEntity<?> get(
			@ApiParam(value = "Historic task instance id.", required = true)
			@PathVariable String backendId) {
		return super.get(backendId);
	}

	@Override
	public Page<WorkflowHistoricTaskInstanceDto> find(WorkflowFilterDto filter, Pageable pageable, BasePermission permission) {
		Page<WorkflowHistoricTaskInstanceDto> results = super.find(filter, pageable, permission); 
		// Add delegation to a tasks. 
		results.getContent()
				.forEach(task -> this.addDelegationToTask(task, permission));
		
		return results;
	}
	

	@Override
	public WorkflowHistoricTaskInstanceDto getDto(Serializable backendId) {
		WorkflowHistoricTaskInstanceDto dto = super.getDto(backendId);
		// Add delegation to a task.
		addDelegationToTask(dto, IdmBasePermission.READ);

		return dto;
	}

	/**
	 * Find and add definition of the delegation connected with this task.
	 *
	 * @param dto
	 */
	private void addDelegationToTask(WorkflowTaskInstanceDto dto, BasePermission... permission) {
		if (dto != null && dto.getId() != null) {
			// We need to create mock task, because DTO is instance of historic task here.
			WorkflowTaskInstanceDto mockTask = new WorkflowTaskInstanceDto();
			mockTask.setId(dto.getId());
			List<IdmDelegationDto> delegations = delegationManager.findDelegationForOwner(mockTask, permission);
			// TODO: ONLY first delegation definition is sets to the task!
			if (!CollectionUtils.isEmpty(delegations)) {
				IdmDelegationDto delegation = delegations.get(0);
				IdmDelegationDefinitionDto definition = DtoUtils.getEmbedded(delegation,
						IdmDelegation_.definition.getName(), IdmDelegationDefinitionDto.class);
				dto.setDelegationDefinition(definition);
			}
		}
	}

}
