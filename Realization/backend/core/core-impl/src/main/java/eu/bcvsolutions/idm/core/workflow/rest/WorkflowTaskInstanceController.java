package eu.bcvsolutions.idm.core.workflow.rest;

import eu.bcvsolutions.idm.core.api.bulk.action.BulkActionManager;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import java.io.Serializable;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.DelegationManager;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmDelegation_;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.workflow.model.dto.FormDataWrapperDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceAbstractDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.activiti.engine.task.IdentityLinkType;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;

/**
 * Rest controller for workflow instance tasks
 *
 * TODO: secure endpoints
 *
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/workflow-tasks")
@Api(
		value = WorkflowTaskInstanceController.TAG,
		tags = {WorkflowTaskInstanceController.TAG},
		description = "Running WF tasks",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class WorkflowTaskInstanceController extends AbstractReadDtoController<WorkflowTaskInstanceDto, WorkflowFilterDto> {

	protected static final String TAG = "Workflow - task instances";

	@Autowired
	private DelegationManager delegationManager;
	@Autowired
	private BulkActionManager bulkActionManager;
	@Autowired
	private SecurityService securityService;
	private final WorkflowTaskInstanceService workflowTaskInstanceService;

	@Autowired
	public WorkflowTaskInstanceController(
			WorkflowTaskInstanceService entityService) {
		super(entityService);
		//
		this.workflowTaskInstanceService = entityService;
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.WORKFLOW_TASK_READ + "')")
	@ApiOperation(
			value = "Search task instances",
			nickname = "searchTaskInstances",
			tags = {WorkflowTaskInstanceController.TAG},
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
			@AuthorizationScope(scope = CoreGroupPermission.WORKFLOW_TASK_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
			@AuthorizationScope(scope = CoreGroupPermission.WORKFLOW_TASK_READ, description = "")})
			})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{backendId}")
	@ApiOperation(
			value = "Historic task instance detail",
			nickname = "getHistoricTaskInstance",
			response = WorkflowTaskInstanceDto.class,
			tags = {WorkflowTaskInstanceController.TAG})
	public ResponseEntity<?> get(
			@ApiParam(value = "Task instance id.", required = true)
			@PathVariable String backendId) {
		return super.get(backendId);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/{backendId}/complete")
	@ApiOperation(
			value = "Complete task instance",
			nickname = "completeTaskInstance",
			tags = {WorkflowTaskInstanceController.TAG},
			notes = "Complete task with given decision.")
	public void completeTask(
			@ApiParam(value = "Task instance id.", required = true)
			@PathVariable String backendId,
			@ApiParam(value = "Complete decision, variables etc.", required = true)
			@RequestBody FormDataWrapperDto formData) {
		workflowTaskInstanceService.completeTask(backendId, formData.getDecision(), formData.getFormData(), formData.getVariables());
		// 
		// TODO: no content should be returned
		// return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{backendId}/permissions")
	@ApiOperation(
			value = "Historic task instance detail",
			nickname = "getHistoricTaskInstance",
			response = WorkflowTaskInstanceDto.class,
			tags = {WorkflowTaskInstanceController.TAG})
	@Override
	public Set<String> getPermissions(
			@ApiParam(value = "Task instance id.", required = true)
			@PathVariable String backendId) {
		WorkflowTaskInstanceDto taskInstanceDto = workflowTaskInstanceService.get(backendId);
		return workflowTaskInstanceService.getPermissions(taskInstanceDto);
	}

	@Override
	// We need override that method (#1320). Parent using lookup service for get DTO. Lookup
	// service get DTO without permissions and after that check permission on READ.
	// Without permissions is loaded Task with all buttons (canExecute is true). We
	// need call direct service with Permission.READ!
	public WorkflowTaskInstanceDto getDto(Serializable backendId) {
		WorkflowTaskInstanceDto dto = getService().get(backendId, IdmBasePermission.READ);
		// Add delegation to a task.
		addDelegationToTask(dto, IdmBasePermission.READ);

		return dto;
	}

	@Override
	public Page<WorkflowTaskInstanceDto> find(WorkflowFilterDto filter, Pageable pageable, BasePermission permission) {
		Page<WorkflowTaskInstanceDto> results = super.find(filter, pageable, permission);
		// Add delegation to a tasks.
		results.getContent()
				.forEach(task -> addDelegationToTask(task, permission));

		return results;
	}
	
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.WORKFLOW_TASK_READ + "')")
	@ApiOperation(
			value = "Get available bulk actions", 
			nickname = "availableBulkAction", 
			tags = { WorkflowTaskInstanceController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
			@AuthorizationScope(scope = CoreGroupPermission.WORKFLOW_TASK_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
			@AuthorizationScope(scope = CoreGroupPermission.WORKFLOW_TASK_READ, description = "")})
			})
	@Override
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return bulkActionManager.getAvailableActionsForDto(WorkflowTaskInstanceAbstractDto.class);
	}
	
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.WORKFLOW_TASK_READ + "')")
	@ApiOperation(
			value = "Process bulk action", 
			nickname = "bulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { WorkflowTaskInstanceController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
			@AuthorizationScope(scope = CoreGroupPermission.WORKFLOW_TASK_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
			@AuthorizationScope(scope = CoreGroupPermission.WORKFLOW_TASK_READ, description = "")})
			})
	@Override
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		// Set DTO name to the action directly.
		bulkAction.setDtoClass(WorkflowTaskInstanceAbstractDto.class.getCanonicalName());
		
		return super.bulkAction(bulkAction);
	}
	
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.WORKFLOW_TASK_READ + "')")
	@ApiOperation(
			value = "Prevalidate bulk action", 
			nickname = "prevalidateBulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { WorkflowTaskInstanceController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
			@AuthorizationScope(scope = CoreGroupPermission.WORKFLOW_TASK_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
			@AuthorizationScope(scope = CoreGroupPermission.WORKFLOW_TASK_READ, description = "")})
			})
	@Override
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		// Set DTO name to the action directly.
		bulkAction.setDtoClass(WorkflowTaskInstanceAbstractDto.class.getCanonicalName());
		
		return super.prevalidateBulkAction(bulkAction);
	}

	/**
	 * Find and add definition of the delegation connected with this task.
	 *
	 * @param dto
	 */
	private void addDelegationToTask(WorkflowTaskInstanceDto dto, BasePermission... permission) {
		if (dto != null && dto.getId() != null) {
			// We need to create mock task, because DTO can be instance of historic task here.
			WorkflowTaskInstanceDto mockTask = new WorkflowTaskInstanceDto();
			mockTask.setId(dto.getId());
			
			UUID currentUserId = securityService.getCurrentId();
			
			boolean currentUserIsCandidate = dto.getIdentityLinks().stream()
				.filter(identityLink -> IdentityLinkType.CANDIDATE.equals(identityLink.getType())
				|| IdentityLinkType.ASSIGNEE.equals(identityLink.getType()))
				.filter(identityLink -> currentUserId != null
						&& UUID.fromString(identityLink.getUserId()).equals(currentUserId))
				.findFirst()
				.isPresent();
			
			boolean filterOnlyForCurrentUser = currentUserIsCandidate && !workflowTaskInstanceService.canReadAllTask(permission);
			
			List<IdmDelegationDto> delegations = delegationManager.findDelegationForOwner(mockTask, permission)
					.stream()
					.filter(delegation -> {
						// Filter only delegation where delegator or delegate is logged user (and user is not admin).
						if (!filterOnlyForCurrentUser) {
							return true;
						}
						IdmDelegationDefinitionDto definition = DtoUtils.getEmbedded(delegation,
								IdmDelegation_.definition.getName(), IdmDelegationDefinitionDto.class);
						
						return definition.getDelegate().equals(currentUserId)
								|| definition.getDelegator().equals(currentUserId);
					})
					.sorted(Comparator.comparing(IdmDelegationDto::getCreated))
					.collect(Collectors.toList());
			
			// TODO: ONLY first delegation definition is sets to the task!
			if (!CollectionUtils.isEmpty(delegations)) {
				Collections.reverse(delegations);
				IdmDelegationDto delegation = delegations.get(0);
				IdmDelegationDefinitionDto definition = DtoUtils.getEmbedded(delegation,
						IdmDelegation_.definition.getName(), IdmDelegationDefinitionDto.class);
				dto.setDelegationDefinition(definition);
			}
		}
	}

	@Override
	protected WorkflowFilterDto toFilter(MultiValueMap<String, Object> parameters) {
		WorkflowFilterDto filter = super.toFilter(parameters);
		
		if (filter == null) {
			return new WorkflowFilterDto();
		}
		return filter;
	}
	
}
