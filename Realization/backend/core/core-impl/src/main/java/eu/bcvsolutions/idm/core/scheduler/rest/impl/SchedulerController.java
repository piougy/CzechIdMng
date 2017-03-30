package eu.bcvsolutions.idm.core.scheduler.rest.impl;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.AbstractTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.CronTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.SimpleTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;

/**
 * Scheduler administration
 * 
 * @author Radek Tomiška
 */
@RestController
@RequestMapping(BaseController.BASE_PATH + "/scheduler-tasks")
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", matchIfMissing = true)
public class SchedulerController implements BaseController {

	@Autowired
	private SchedulerManager schedulerService;
	
	/**
	 * Returns all registered tasks
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/search/supported")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	public Resources<Task> getSupportedTasks() {
		return new Resources<>(schedulerService.getSupportedTasks());
	}

	/**
	 * Returns all scheduled tasks
	 *
	 * @return all tasks
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	public Resources<Task> getAll() {
		return new Resources<>(schedulerService.getAllTasks());
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/{taskId}")
	public Task getTask(@PathVariable String taskId) {
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
	public Task createTask(@Valid @RequestBody Task task) {
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
	public ResponseEntity<?> deleteTask(@PathVariable String taskId) {
		schedulerService.deleteTask(taskId);
		//
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/{taskId}/run")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_EXECUTE + "')")
	public AbstractTaskTrigger runTask(@PathVariable String taskId) {
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
	public AbstractTaskTrigger createSimpleTrigger(@PathVariable String taskId, @Valid @RequestBody SimpleTaskTrigger trigger) {
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
	public AbstractTaskTrigger createCronTrigger(@PathVariable String taskId, @Valid @RequestBody CronTaskTrigger trigger) {
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
	public ResponseEntity<?> deleteTrigger(@PathVariable String taskId, @PathVariable String triggerName) {
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
	public ResponseEntity<?> pauseTrigger(@PathVariable String taskId, @PathVariable String triggerName) {
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
	public ResponseEntity<?> resumeTrigger(@PathVariable String taskId, @PathVariable String triggerName) {
		schedulerService.resumeTrigger(taskId, triggerName);
		//
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
}
