package eu.bcvsolutions.idm.core.scheduler.rest.impl;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
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
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.AbstractTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.CronTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.DependentTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.SimpleTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.TaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Scheduler administration
 * 
 * @author Radek Tomiška
 */
@RestController
@RequestMapping(BaseController.BASE_PATH + "/scheduler-tasks")
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", matchIfMissing = true)
@Api(value = SchedulerController.TAG, description = "Scheduled tasks administration", tags = { SchedulerController.TAG })
public class SchedulerController implements BaseController {

	protected static final String TAG = "Scheduler";
	//
	@Autowired private SchedulerManager schedulerService;
	@Autowired private LookupService lookupService;
	@Autowired private PagedResourcesAssembler<Object> pagedResourcesAssembler;
	private ParameterConverter parameterConverter = null;
	
	/**
	 * Returns all registered tasks
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/search/supported")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@ApiOperation(
			value = "Get supported tasks", 
			nickname = "getSupportedSchedulerTasks", 
			tags={ SchedulerController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") })
				})
	public Resources<Task> getSupportedTasks() {
		return new Resources<>(schedulerService.getSupportedTasks());
	}

	/**
	 * Finds scheduled tasks
	 *
	 * @return all tasks
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@ApiOperation(
			value = "Search scheduled tasks", 
			nickname = "searchSchedulerTasks", 
			tags={ SchedulerController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") })
				})
	@ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataType = "string", paramType = "query",
                value = "Results page you want to retrieve (0..N)"),
        @ApiImplicitParam(name = "size", dataType = "string", paramType = "query",
                value = "Number of records per page."),
        @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                value = "Sorting criteria in the format: property(,asc|desc). " +
                        "Default sort order is ascending. " +
                        "Multiple sort criteria are supported.")
	})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		Page tasks = schedulerService.find(toFilter(parameters), pageable);
		//
		return pageToResources(tasks, Task.class);
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/{taskId}")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@ApiOperation(
			value = "Get scheduled task detail", 
			nickname = "getSchedulerTask", 
			tags={ SchedulerController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") })
				})
	public Task getTask(
			@ApiParam(value = "Task identifier.", required = true)
			@PathVariable String taskId) {
		return schedulerService.getTask(taskId);
	}
	
	/**
	 * Creates scheduled task
	 * 
	 * @param task
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_CREATE + "')")
	@ApiOperation(
			value = "Create scheduled task", 
			nickname = "postSchedulerTask", 
			tags={ SchedulerController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_CREATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_CREATE, description = "") })
				})
	public Task createTask(
			@ApiParam(value = "Task.", required = true)
			@Valid @RequestBody Task task) {
		return schedulerService.createTask(task);
	}
	
	/**
	 * Edit scheduled task
	 * 
	 * @param task
	 * @return
	 */
	@RequestMapping(value = "/{taskId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_UPDATE + "')")
	@ApiOperation(
			value = "Update scheduled task", 
			nickname = "updateSchedulerTask", 
			tags={ SchedulerController.TAG },
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_UPDATE, description = "") })
					})
	public Task updateTask(
		@ApiParam(value = "Task identifier.", required = true)
		@PathVariable String taskId,
		@Valid @RequestBody Task task) {
		return schedulerService.updateTask(taskId, task);
	}
	
	/**
	 * Deletes scheduled task
	 * 
	 * @param taskId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.DELETE, value = "/{taskId}")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_DELETE + "')")
	@ApiOperation(
			value = "Delete scheduled task", 
			nickname = "deleteSchedulerTask", 
			tags={ SchedulerController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_DELETE, description = "") })
				})
	public ResponseEntity<?> deleteTask(
			@ApiParam(value = "Task identifier.", required = true)
			@PathVariable String taskId) {
		schedulerService.deleteTask(taskId);
		//
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/{taskId}/run")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_EXECUTE + "')")
	@ApiOperation(
			value = "Execute scheduled task", 
			nickname = "executeSchedulerTask", 
			tags={ SchedulerController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_EXECUTE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_EXECUTE, description = "") })
				},
			notes = "Create long running task (LRT) by scheduled task definition immediately. Created task will be added to LRTs queue.")
	public AbstractTaskTrigger runTask(
			@ApiParam(value = "Task identifier.", required = true)
			@PathVariable String taskId) {
		return schedulerService.runTask(taskId); 
	}
	
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/{taskId}/dry-run")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_EXECUTE + "')")
	@ApiOperation(
			value = "Execute scheduled task in dry run mode",
			nickname = "executeSchedulerTaskDryRun",
			tags={ SchedulerController.TAG },
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_EXECUTE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_EXECUTE, description = "") })
			},
			notes = "Create long running task (LRT) by scheduled task definition immediately in dry run mode. Created task will be added to LRTs queue.")
	public AbstractTaskTrigger dryRunTask(
			@ApiParam(value = "Task identifier.", required = true)
			@PathVariable String taskId) {
		return schedulerService.runTask(taskId, true);
	}

	/**
	 * Creates one time trigger for task
	 *
	 * @param taskId name of task
	 * @param trigger trigger data
	 * @return trigger data containing name
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/{taskId}/triggers/simple")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_CREATE + "')")
	@ApiOperation(
			value = "Create simple trigger", 
			nickname = "postSimpleTrigger", 
			tags={ SchedulerController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_CREATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_CREATE, description = "") })
				},
			notes = "Create simple trigger by given execution date.")
	public AbstractTaskTrigger createSimpleTrigger(
			@ApiParam(value = "Task identifier.", required = true)
			@PathVariable String taskId, 
			@ApiParam(value = "Simple trigger definition.", required = true)
			@Valid @RequestBody SimpleTaskTrigger trigger) {
		return schedulerService.createTrigger(taskId, trigger);
	}

	/**
	 * Creates cron trigger for task
	 *
	 * @param taskId name of task
	 * @param trigger trigger data
	 * @return trigger data containing name
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/{taskId}/triggers/cron")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_CREATE + "')")
	@ApiOperation(
			value = "Create cron trigger", 
			nickname = "postCronTrigger", 
			tags={ SchedulerController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_CREATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_CREATE, description = "") })
				},	
			notes = "Create trigger by given quartz cron expression.")
	public AbstractTaskTrigger createCronTrigger(
			@ApiParam(value = "Task identifier.", required = true)
			@PathVariable String taskId,
			@ApiParam(value = "Cron trigger definition.", required = true)
			@Valid @RequestBody CronTaskTrigger trigger) {
		return schedulerService.createTrigger(taskId, trigger);
	}
	
	/**
	 * Creates dependent trigger for task
	 *
	 * @param taskName name of task
	 * @param trigger trigger data
	 * @return trigger data containing name
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/{taskId}/triggers/dependent")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_CREATE + "')")
	@ApiOperation(
			value = "Create dependent trigger", 
			nickname = "postDependentTrigger", 
			tags={ SchedulerController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_CREATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_CREATE, description = "") })
				},
			notes = "Create trigger, which is triggered, when other scheduled task ends.")
	public AbstractTaskTrigger createDependentTrigger(
			@ApiParam(value = "Task identifier.", required = true)
			@PathVariable String taskId, 
			@ApiParam(value = "Cron trigger definition.", required = true)
			@Valid @RequestBody DependentTaskTrigger trigger) {
		return schedulerService.createTrigger(taskId, trigger);
	}

	/**
	 * Removes trigger
	 *
	 * @param taskId name of task
	 * @param triggerName name of trigger
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.DELETE, value = "/{taskId}/triggers/{triggerName}")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_DELETE + "')")
	@ApiOperation(
			value = "Delete trigger", 
			nickname = "deleteTrigger", 
			tags={ SchedulerController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_DELETE, description = "") })
				})
	public ResponseEntity<?> deleteTrigger(
			@ApiParam(value = "Task identifier.", required = true)
			@PathVariable String taskId, 
			@ApiParam(value = "Trigger identifier.", required = true)
			@PathVariable String triggerName) {
		schedulerService.deleteTrigger(taskId, triggerName);
		//
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Pauses trigger
	 *
	 * @param taskId name of task
	 * @param triggerName name of trigger
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.PUT, value = "/{taskId}/triggers/{triggerName}/pause")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_UPDATE + "')")
	@ApiOperation(
			value = "Pause trigger", 
			nickname = "pauseTrigger", 
			tags={ SchedulerController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_UPDATE, description = "") })
				})
	public ResponseEntity<?> pauseTrigger(
			@ApiParam(value = "Task identifier.", required = true)
			@PathVariable String taskId, 
			@ApiParam(value = "Trigger identifier.", required = true)
			@PathVariable String triggerName) {
		schedulerService.pauseTrigger(taskId, triggerName);
		//
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Resumes trigger
	 *
	 * @param taskId name of task
	 * @param triggerName name of trigger
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.PUT, value = "/{taskId}/triggers/{triggerName}/resume")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_UPDATE + "')")
	@ApiOperation(
			value = "Resume trigger", 
			nickname = "resumeTrigger", 
			tags={ SchedulerController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_UPDATE, description = "") })
				})
	public ResponseEntity<?> resumeTrigger(
			@ApiParam(value = "Task identifier.", required = true)
			@PathVariable String taskId, 
			@ApiParam(value = "Trigger identifier.", required = true)
			@PathVariable String triggerName) {
		schedulerService.resumeTrigger(taskId, triggerName);
		//
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	protected Resources<?> pageToResources(Page<Object> page, Class<?> domainType) {

		if (page.getContent().isEmpty()) {
			return pagedResourcesAssembler.toEmptyResource(page, domainType);
		}

		return pagedResourcesAssembler.toResource(page);
	}
	
	private ParameterConverter getParameterConverter() {
		if (parameterConverter == null) {
			parameterConverter = new ParameterConverter(lookupService);
		}
		return parameterConverter;
	}
	
	private TaskFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new TaskFilter(parameters, getParameterConverter());
	}
}
