package eu.bcvsolutions.idm.core.scheduler.api.service;

import java.util.UUID;

/**
 * Long running task administration
 * 
 * @author Radek Tomiška
 *
 */
public interface LongRunningTaskManager {

	/**
	 * Executes given task asynchronously
	 *  
	 * @param executor
	 */
	void execute(LongRunningTaskExecutor taskExecutor);
	
	/**
	 * Cancels given task. Task flag will be set only and task needs to interact with this state and stop in next iteration.
	 * 
	 * @param taskId
	 */
	void cancel(UUID longRunningTaskId);
	
	/**
	 * Interrupts given task (thread interruption).
	 * 
	 * @param taskId
	 */
	void interrupt(UUID longRunningTaskId);
	
	/**
	 * Executes prepared task from long running task queue
	 */
	void processCreated();
}
