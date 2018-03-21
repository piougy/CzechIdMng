package eu.bcvsolutions.idm.core.scheduler.api.service;

import java.util.List;

import eu.bcvsolutions.idm.core.scheduler.api.dto.AbstractTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;

/**
 * Interface for scheduler service.
 * 
 * @author Radek Tomiška
 */
public interface SchedulerManager {

	/**
	 * Returns registered task definitions
	 * 
	 * @return
	 */
	List<Task> getSupportedTasks();
	
	/**
	 * Returns all tasks
	 *
	 * @return all tasks
	 */
	List<Task> getAllTasks();
	
	/**
	 * Reads existed task by id.
	 * 
	 * @param taskId
	 */
	Task getTask(String taskId);
	
	/**
	 * Creates new task
	 * 
	 * @return
	 */
	Task createTask(Task task);
	
	/**
	 * Deletes task
	 * 
	 * @param taskId
	 */
	void deleteTask(String taskId);
	
	/**
	 * Run task manually
	 * 
	 * @param taskId
	 * @return
	 */
	AbstractTaskTrigger runTask(String taskId);

	/**
	 * Run task manually
	 *
	 * @param taskId
	 * @param dryRun
	 * @return
	 */
	AbstractTaskTrigger runTask(String taskId, boolean dryRun);
	
	/**
	 * Interrupt given task
	 * 
	 * @param taskId
	 * @return Returns true, then task was successfully interrupt. otherwise false
	 */
	boolean interruptTask(String taskId);

	/**
	 * Creates trigger for task
	 *
	 * @param taskId task identifier
	 * @param trigger trigger to add
	 * @return trigger containing name
	 */
	AbstractTaskTrigger createTrigger(String taskId, AbstractTaskTrigger trigger);

	/**
	 * Creates trigger for task
	 *
	 * @param taskId task identifier
	 * @param trigger trigger to add
	* @param dryRun
	 * @return trigger containing name
	 */
	AbstractTaskTrigger createTrigger(String taskId, AbstractTaskTrigger trigger, boolean dryRun);

	/**
	 * Pauses trigger
	 *
	 * @param taskId task identifier
	 * @param triggerId trigger identifier
	 */
	void pauseTrigger(String taskId, String triggerId);

	/**
	 * Resumes trigger
	 *
	 * @param taskId task identifier
	 * @param triggerId trigger identifier
	 */
	void resumeTrigger(String taskId, String triggerId);
	
	/**
	 * Deletes trigger
	 *
	 * @param taskId task identifier
	 * @param triggerId trigger identifier
	 */
	void deleteTrigger(String taskId, String triggerId);

	/**
	 * Find all task with given task type
	 * 
	 * @param taskType
	 * @return
	 */
	List<Task> getAllTasksByType(Class<?> taskType);
	
	/**
	 * Update scheduled task - only parameters and descriptions
	 * 
	 * @param taskId
	 * @param description
	 * @param parameters
	 * @return
	 */
	Task updateTask(String taskId, Task task);
}
