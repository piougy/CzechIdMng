package eu.bcvsolutions.idm.core.scheduler.rest.impl;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.scheduler.api.dto.AbstractTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.CronTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.SimpleTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.service.api.SchedulerService;

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
	private SchedulerService schedulerService;
	
	/**
	 * Returns all registered tasks
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/search/supported")
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
	public ResponseEntity<?> deleteTask(@PathVariable String taskId) {
		schedulerService.deleteTask(taskId);
		//
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/{taskId}/run")
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
	public ResponseEntity<?> resumeTrigger(@PathVariable String taskId, @PathVariable String triggerName) {
		schedulerService.resumeTrigger(taskId, triggerName);
		//
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
}
