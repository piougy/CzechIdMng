package eu.bcvsolutions.idm.core.scheduler.api.service;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;

/**
 * Long running task administration
 * 
 * @author Radek Tomiška
 */
public interface LongRunningTaskManager {
	
	/**
	 * Cancels all previously ran tasks
	 */
	void init();

	/**
	 * * Executes given task asynchronously
	 * 
	 * @param <V> expected result type
	 * @param taskExecutor
	 * @return
	 */
	<V> LongRunningFutureTask<V> execute(LongRunningTaskExecutor<V> taskExecutor);
	
	/**
	 * * Executes given task asynchronously
	 * 
	 * @param <V> expected result type
	 * @param taskExecutor
	 * @return
	 */
	<V> V executeSync(LongRunningTaskExecutor<V> taskExecutor);
	
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
	 * @true Return true, when thread for long running task was found and interrupt was called
	 */
	boolean interrupt(UUID longRunningTaskId);
	
	/**
	 * Executes prepared task from long running task queue
	 * 
	 * @return Returns currently executed tasks
	 */
	List<LongRunningFutureTask<?>> processCreated();
	
	/**
	 * Schedule {@link #processCreated()} only
	 */
	void scheduleProcessCreated();
}
