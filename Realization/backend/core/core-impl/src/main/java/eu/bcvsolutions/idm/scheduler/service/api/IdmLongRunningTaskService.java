package eu.bcvsolutions.idm.scheduler.service.api;

import java.util.UUID;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.scheduler.dto.filter.LongRunningTaskFilter;
import eu.bcvsolutions.idm.scheduler.entity.IdmLongRunningTask;

/**
 * Long running task administation
 * 
 * @author Radek Tomiška
 *
 */
@Service
public interface IdmLongRunningTaskService extends ReadWriteEntityService<IdmLongRunningTask, LongRunningTaskFilter> {
	
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
