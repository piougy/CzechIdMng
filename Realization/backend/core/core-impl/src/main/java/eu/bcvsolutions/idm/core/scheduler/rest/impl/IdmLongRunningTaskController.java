package eu.bcvsolutions.idm.core.scheduler.rest.impl;

import java.io.InputStream;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.SpinalCase;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Default controller long running tasks (LRT)
 *
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/long-running-tasks")
@Api(
		value = IdmLongRunningTaskController.TAG,
		description = "Operations with long running tasks (LRT)",
		tags = { IdmLongRunningTaskController.TAG })
public class IdmLongRunningTaskController
	extends AbstractReadWriteDtoController<IdmLongRunningTaskDto, IdmLongRunningTaskFilter> {

	protected static final String TAG = "Long running tasks";
	//
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	@Autowired private AttachmentManager attachmentManager;

	@Autowired
	public IdmLongRunningTaskController(
			IdmLongRunningTaskService service) {
		super(service);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@ApiOperation(
			value = "Search LRTs (/search/quick alias)", 
			nickname = "searchLongRunningTasks", 
			tags={ IdmLongRunningTaskController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") })
			})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	/**
	 * All endpoints will support find quick method.
	 *
	 * @param parameters
	 * @param pageable
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@ApiOperation(
			value = "Search LRTs", 
			nickname = "searchQuickLongRunningTasks", 
			tags={ IdmLongRunningTaskController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") })
			})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter", 
			nickname = "countLongRunningTasks", 
			tags = { IdmLongRunningTaskController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_COUNT, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_COUNT, description = "") })
				})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@ApiOperation(
			value = "LRT detail",
			nickname = "getLongRunningTask",
			response = IdmLongRunningTaskDto.class,
			tags={ IdmLongRunningTaskController.TAG }, 
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") })
					})
	public ResponseEntity<?> get(
			@ApiParam(value = "LRT's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	public IdmLongRunningTaskDto getDto(Serializable backendId) {
		// FIXME: Propagate filter in GET method (in AbstractReadDto controller => requires lookup api improvement).
		IdmLongRunningTaskFilter filter = toFilter(null);
		//
		return getService().get(backendId, filter, IdmBasePermission.READ);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.LONGRUNNINGTASK_DELETE + "')")
	@ApiOperation(
			value = "Delete LRT", 
			nickname = "deleteLongRunningTask", 
			tags = { IdmLongRunningTaskController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.LONGRUNNINGTASK_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.LONGRUNNINGTASK_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "LRT's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnLongRunningTask", 
			tags = { IdmLongRunningTaskController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "")})
				})
	public Set<String> getPermissions(
			@ApiParam(value = "LRT's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@ApiOperation(
			value = "Get available bulk actions", 
			nickname = "availableBulkAction", 
			tags = { IdmLongRunningTaskController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") })
				})
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@ApiOperation(
			value = "Process bulk action", 
			nickname = "bulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { IdmLongRunningTaskController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "")})
				})
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@ApiOperation(
			value = "Prevalidate bulk action", 
			nickname = "prevalidateBulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { IdmLongRunningTaskController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "")})
				})
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}

	@ResponseBody
	@RequestMapping(value = "/{backendId}/download/{attachmentId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@ApiOperation(
			value = "Download result from LRT",
			nickname = "downloadReslut",
			response = IdmLongRunningTaskDto.class,
			tags={ IdmLongRunningTaskController.TAG }, 
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") })
					})
	public ResponseEntity<?> downloadResult(
			@ApiParam(value = "LRT's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@ApiParam(value = "Attachment's id.", required = true)
			@PathVariable @NotNull String attachmentId) {
		
		// check if user has permission for read the long running task
		IdmLongRunningTaskDto longRunningTaskDto = super.getDto(backendId);
		if (longRunningTaskDto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}
		//
		IdmAttachmentDto attachmentDto = longRunningTaskManager.getAttachment(
				longRunningTaskDto.getId(), 
				DtoUtils.toUuid(attachmentId), 
				IdmBasePermission.READ);
		InputStream is = attachmentManager.getAttachmentData(attachmentDto.getId(), IdmBasePermission.READ);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		String attachmentName = String.format(
				"%s-%s", 
				SpinalCase.format(longRunningTaskDto.getTaskType()),
				longRunningTaskDto.getCreated().format(formatter)
		);
		return ResponseEntity.ok()
				.contentLength(attachmentDto.getFilesize())
				.contentType(MediaType.parseMediaType(attachmentDto.getMimetype()))
				.header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s.%s\"", attachmentName, attachmentDto.getAttachmentType()))
				.body(new InputStreamResource(is));
	}

	/**
	 * Cancels running job
	 *
	 * @param taskName name of task
	 * @param triggerName name of trigger
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.PUT, value = "/{backendId}/cancel")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_UPDATE + "')")
	@ApiOperation(
			value = "Cancel running task",
			nickname = "cancelLongRunningTask",
			tags={ IdmLongRunningTaskController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_UPDATE, description = "") })
				},
			notes = "Stop running task in next internal task's iteration (when counter is incremented).")
	public ResponseEntity<?> cancel(
			@ApiParam(value = "LRT's uuid identifier.", required = true)
			@PathVariable UUID backendId) {
		longRunningTaskManager.cancel(backendId);
		//
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Kills running job
	 *
	 * @param taskName name of task
	 * @param triggerName name of trigger
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.PUT, value = "/{backendId}/interrupt")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_UPDATE + "')")
	@ApiOperation(
			value = "Interrupt running task",
			nickname = "interruptLongRunningTask",
			tags={ IdmLongRunningTaskController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_UPDATE, description = "") })
				},
			notes = "Interrupt given LRT - \"kills\" thread with running task.")
	public ResponseEntity<?> interrupt(
			@ApiParam(value = "LRT's uuid identifier.", required = true)
			@PathVariable UUID backendId) {
		longRunningTaskManager.interrupt(backendId);
		//
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Executes prepared task from long running task queue
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/action/process-created")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_EXECUTE + "')")
	@ApiOperation(
			value = "Process created LRTs",
			nickname = "processCreatedLongRunningTasks",
			tags={ IdmLongRunningTaskController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_EXECUTE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_EXECUTE, description = "") })
				},
			notes = "When LRT is created, then is added to queue with state created only."
					+ " Another scheduled task for processing prepared task will execute them."
					+ " This operation process prepared tasks immediately.")
	public ResponseEntity<?> processCreated() {
		longRunningTaskManager.processCreated();
		//
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.PUT, value = "/{backendId}/process")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_EXECUTE + "')")
	@ApiOperation(
			value = "Process created LRT",
			nickname = "oneProcessCreatedLongRunningTasks",
			tags={ IdmLongRunningTaskController.TAG },
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_EXECUTE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_EXECUTE, description = "") })
			},
			notes = "When LRT is created, then is added to queue with state created only."
					+ " Another scheduled task for processing prepared task will execute them."
					+ " This operation process prepared task by given identifier immediately.")
	public ResponseEntity<?> processCheckedCreated(
			@ApiParam(value = "LRT's uuid identifier.", required = true)
			@PathVariable UUID backendId) {
		longRunningTaskManager.processCreated(backendId);
		//
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	@Override
	protected IdmLongRunningTaskFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter(parameters, getParameterConverter());
		// counters are loaded from controller all times
		filter.setIncludeItemCounts(true);
		//
		return filter;
	}
}
