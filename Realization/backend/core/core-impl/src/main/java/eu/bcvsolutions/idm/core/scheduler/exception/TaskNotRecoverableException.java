package eu.bcvsolutions.idm.core.scheduler.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;

/**
 * Thrown, when not recoverable task is executed again. 
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
public class TaskNotRecoverableException extends SchedulerException {
	
	private static final long serialVersionUID = 1L;
	private final IdmLongRunningTaskDto longRunningTask;	
	
	public TaskNotRecoverableException(CoreResultCode resulCode, IdmLongRunningTaskDto longRunningTask) {
		super(resulCode, 
				ImmutableMap.of(
					"taskId", longRunningTask.getId(), 
					"taskType", longRunningTask.getTaskType()
				));
		this.longRunningTask = longRunningTask;
	}
	
	public IdmLongRunningTaskDto getLongRunningTask() {
		return longRunningTask;
	}
}
