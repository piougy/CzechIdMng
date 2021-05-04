package eu.bcvsolutions.idm.core.scheduler.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
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
	 * Task can be executed repetitively without reschedule is needed.
	 * When task is canceled (e.g. by server is restarted), then task can be executed again (~recovered).
	 * 
	 * @param id
	 * @return
	 * @throws 
	 * @since 10.2.0
	 */
	LongRunningFutureTask<?> recover(UUID longRunningTaskId);
	
	/**
	 * Returns long running task by id. Authorization policies are evaluated (if given).
	 * Item counts (success, warning etc.) are loaded.
	 * 
	 * @param longRunningTaskId
	 * @param permission permissions to evaluate (AND)
	 * @return task
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmLongRunningTaskDto getLongRunningTask(UUID longRunningTaskId, BasePermission... permission);
	
	/**
	 * Returns underlying long running task for given taskExecutor. Authorization policies are evaluated (if given).
	 * Item counts (success, warning etc.) are loaded.
	 * 
	 * @param taskExecutor
	 * @param permission permissions to evaluate (AND)
	 * @return task
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmLongRunningTaskDto getLongRunningTask(LongRunningTaskExecutor<?> taskExecutor, BasePermission... permission);
	
	/**
	 * Returns underlying long running task for given future task. Authorization policies are evaluated (if given).
	 * Item counts (success, warning etc.) are loaded.
	 * 
	 * @param futureTask
	 * @param permission permissions to evaluate (AND)
	 * @return task
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmLongRunningTaskDto getLongRunningTask(LongRunningFutureTask<?> futureTask, BasePermission... permission);
	
	/**
	 * Save long running task.
	 * 
	 * @param longRunningTask task to save
	 * @param permission permissions to evaluate (AND)
	 * @return saved task
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 * @since 10.6.0
	 */
	IdmLongRunningTaskDto saveLongRunningTask(IdmLongRunningTaskDto longRunningTask, BasePermission... permission);
	
	/**
	 * Returns page of long running tasks by given filter, authorization permission will be evaluated. 
	 * Never throws {@link ForbiddenEntityException} - returning available dtos by given permissions (AND).
	 * 
	 * Alias to {@link IdmLongRunningTaskService#find(Pageable, BasePermission...)}. Manager can be used instead service.
	 * 
	 * @see IdmLongRunningTaskService#find(Pageable, BasePermission...)
	 * @param filter
	 * @param pageable
	 * @param permission base permissions to evaluate (AND)
	 * @return
	 * @since 10.4.0
	 */
	Page<IdmLongRunningTaskDto> findLongRunningTasks(IdmLongRunningTaskFilter filter, Pageable pageable, BasePermission... permission);
	
	/**
	 * Prepares [and saves] or load executor's LRT.
	 * If executor has LRT already, then LRT loaded and returned only.
	 *
	 * @param taskExecutor LRT executor
	 * @param sheduledTaskId [optional] scheduled task identifier - newly created LRT will be added for scheduled task.
	 * @param state [optional] newly created LRT state. Sync - RUNNING, Async - CREATED (default) => prevent to execute synchronous task twice by asynchronous processing
	 * @return LRT instance (newly created or loaded)
	 * @since 10.4.0
	 */
	public IdmLongRunningTaskDto resolveLongRunningTask(
			LongRunningTaskExecutor<?> taskExecutor, 
			UUID sheduledTaskId,
			OperationState state);
	
	/**
	 * Returns true, if asynchronous long running task processing is enabled.
	 * Asynchronous long running tasks can be turned off mainly for testing purposes.
	 * Synchronous task are executed on the same instance as initiator runs (e.g. as scheduler runs).
	 * 
	 * @return
	 * @since 8.2.0
	 */
	boolean isAsynchronous();
	
	/**
	 * Asynchronous task processing is stopped.
	 * Asynchronous task processing is stopped, when instance for processing is switched => prevent to process asynchronous task in the meantime.
	 * Asynchronous task processing can be stopped for testing or debugging purposes.
	 * Asynchronous task are still created in queue, but they are not processed.
	 * 
	 * @since 11.1.0
	 * @return true - asynchronous events are not processed 
	 */
	boolean isStopProcessing();

	/**
	 * Get attachment for long running task. {@link IdmLongRunningTaskDto} must exists,
	 * otherwise is throw error {@link EntityNotFoundException}. Also {@link IdmAttachmentDto}
	 * must exists otherwise is throw error {@link EntityNotFoundException}.
	 *
	 * @param longRunningTaskId
	 * @param attachmentId
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 * @since 9.4.0
	 */
	IdmAttachmentDto getAttachment(UUID longRunningTaskId, UUID attachmentId, BasePermission... permission);
	
	/**
	 * Switch instanceId for processing long running tasks.
	 * All tasks created for previous instance will be moved to new instance.
	 * 
	 * @param previousInstanceId previously used instance
	 * @param newInstanceId [optional] currently configured instance will be used as default
	 * @return updated tasks count
	 * @since 11.1.0
	 */
	int switchInstanceId(String previousInstanceId, String newInstanceId);
}
