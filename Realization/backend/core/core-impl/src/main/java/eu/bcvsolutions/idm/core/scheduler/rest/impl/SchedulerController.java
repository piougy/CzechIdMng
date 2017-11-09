package eu.bcvsolutions.idm.core.scheduler.rest.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.AbstractTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.CronTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.DependentTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.SimpleTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
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
	private static final String PROPERTY_TASK_TYPE = "taskType";
	private static final String PROPERTY_DESCRIPTION = "description";
	private static final String PROPERTY_INSTANCE_ID = "instanceId";
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
	public Resources<Task> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		String text = getParameterConverter().toString(parameters, DataFilter.PARAMETER_TEXT);
		List<Task> tasks = schedulerService
				.getAllTasks()
				.stream()
				.filter(task -> {
					// filter - like name or description only
					return StringUtils.isEmpty(text) || task.getTaskType().getSimpleName().toLowerCase().contains(text.toLowerCase())
							|| (task.getDescription() != null && task.getDescription().toLowerCase().contains(text.toLowerCase()));
				})
				.sorted((taskOne, taskTwo) -> {
					Sort sort = pageable.getSort();
					if (pageable.getSort() == null) {
						return 0;
					}
					int compareAscValue = 0;
					boolean asc = true;
					// "naive" sort implementation
					if (sort.getOrderFor(PROPERTY_TASK_TYPE) != null) {
						asc = sort.getOrderFor(PROPERTY_TASK_TYPE).isAscending();
						compareAscValue = taskOne.getTaskType().getSimpleName().compareTo(taskTwo.getTaskType().getSimpleName());
					}
					if (sort.getOrderFor(PROPERTY_DESCRIPTION) != null) {
						asc = sort.getOrderFor(PROPERTY_DESCRIPTION).isAscending();
						compareAscValue = taskOne.getDescription().compareTo(taskTwo.getDescription());
					}
					if (sort.getOrderFor(PROPERTY_INSTANCE_ID) != null) {
						asc = sort.getOrderFor(PROPERTY_INSTANCE_ID).isAscending();
						compareAscValue = taskOne.getInstanceId().compareTo(taskTwo.getInstanceId());
					}
					return asc ? compareAscValue : compareAscValue * -1;
				})
				.collect(Collectors.toList());
		// "naive" pagination
		int first = pageable.getPageNumber() * pageable.getPageSize();
		int last = pageable.getPageSize() + first;
		List<Task> taskPage = tasks.subList(
				first < tasks.size() ? first : tasks.size() > 0 ? tasks.size() - 1 : 0, 
				last < tasks.size() ? last : tasks.size());
		//
		return pageToResources(new PageImpl(taskPage, pageable, tasks.size()), Task.class);
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
	 * @param taskName name of task
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
	 * @param taskName name of task
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
	
	private ParameterConverter getParameterConverter() {
		if (parameterConverter == null) {
			parameterConverter = new ParameterConverter(lookupService);
		}
		return parameterConverter;
	}
	
	protected Resources<?> pageToResources(Page<Object> page, Class<?> domainType) {

		if (page.getContent().isEmpty()) {
			return pagedResourcesAssembler.toEmptyResource(page, domainType, null);
		}

		return pagedResourcesAssembler.toResource(page);
	}
}
