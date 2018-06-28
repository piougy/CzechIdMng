package eu.bcvsolutions.idm.core.scheduler.api.service;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

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
	 * Executes given task asynchronously.
	 * Created long running task instance is accessible by returned {@link LongRunningFutureTask#getExecutor()}.
	 * 
	 * @param <V> expected result type
	 * @param taskExecutor
	 * @return
	 */
	<V> LongRunningFutureTask<V> execute(LongRunningTaskExecutor<V> taskExecutor);
	
	/**
	 * Executes given task synchronously.
	 * Created long running task instance is accessible by returned {@link LongRunningTaskExecutor#getLongRunningTaskId()}.
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
	 * Executes all prepared tasks from long running task queue
	 * 
	 * @return Returns currently executed tasks
	 */
	List<LongRunningFutureTask<?>> processCreated();

	/**
	 * Executes prepared task by given id from long running task queue
	 * 
	 * @param id
	 * @return
	 */
	LongRunningFutureTask<?> processCreated(UUID longRunningTaskId);
	
	/**
	 * Returns long running task by id. Authorization policies are evaluated (if given).
	 * 
	 * @param longRunningTaskId
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmLongRunningTaskDto getLongRunningTask(UUID longRunningTaskId, BasePermission... permission);
	
	/**
	 * Returns underlying long running task for given taskExecutor. Authorization policies are evaluated (if given).
	 * 
	 * @param taskExecutor
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmLongRunningTaskDto getLongRunningTask(LongRunningTaskExecutor<?> taskExecutor, BasePermission... permission);
	
	/**
	 * Returns underlying long running task for given future task. Authorization policies are evaluated (if given).
	 * 
	 * @param futureTask
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmLongRunningTaskDto getLongRunningTask(LongRunningFutureTask<?> futureTask, BasePermission... permission);
	
	/**
	 * Returns true, if asynchronous long running task processing is enabled.
	 * Asynchronous long running tasks can be turned off mainly for testing purposes.
	 * Synchronous task are executed on the same instance as initiator runs (e.g. as scheduler runs).
	 * 
	 * @return
	 * @since 8.2.0
	 */
	boolean isAsynchronous();
}
