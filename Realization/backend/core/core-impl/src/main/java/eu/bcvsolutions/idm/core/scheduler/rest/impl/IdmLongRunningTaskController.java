package eu.bcvsolutions.idm.core.scheduler.rest.impl;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.dto.filter.LongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskService;
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
	extends AbstractReadWriteDtoController<IdmLongRunningTaskDto, LongRunningTaskFilter> {
	
	protected static final String TAG = "Long running tasks";
	private final LongRunningTaskManager longRunningTaskManager;
	
	@Autowired
	public IdmLongRunningTaskController(
			LookupService entityLookupService,
			IdmLongRunningTaskService service,
			LongRunningTaskManager longRunningTaskManager) {
		super(service);
		//
		Assert.notNull(longRunningTaskManager);
		//
		this.longRunningTaskManager = longRunningTaskManager;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@ApiOperation(value = "Search LRTs (/search/quick alias)", nickname = "searchLongRunningTasks", tags={ IdmLongRunningTaskController.TAG }, authorizations = {
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
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@ApiOperation(value = "Search LRTs", nickname = "searchQuickLongRunningTasks", tags={ IdmLongRunningTaskController.TAG }, authorizations = {
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
}
