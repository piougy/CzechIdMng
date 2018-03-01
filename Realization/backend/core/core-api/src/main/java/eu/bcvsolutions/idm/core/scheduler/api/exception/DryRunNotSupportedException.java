package eu.bcvsolutions.idm.core.scheduler.api.exception;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

/**
 * Task does not support dry run mode
 * 
 * @author Radek Tomiška
 *
 */
public class DryRunNotSupportedException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;
	private final String taskType;	
	
	public DryRunNotSupportedException(String taskType) {
		super(CoreResultCode.SCHEDULER_DRY_RUN_NOT_SUPPORTED, ImmutableMap.of("taskType", taskType));
		this.taskType = taskType;
	}
	
	public String getTaskType() {
		return taskType;
	}
}
