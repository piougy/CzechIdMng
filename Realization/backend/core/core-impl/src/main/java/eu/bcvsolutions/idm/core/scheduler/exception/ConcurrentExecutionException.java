package eu.bcvsolutions.idm.core.scheduler.exception;

import java.util.Map;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;

/**
 * Scheduler exception - concurrent task is running
 * 
 * @author Radek Tomiška
 * @deprecated use {@link eu.bcvsolutions.idm.core.scheduler.api.exception.ConcurrentExecutionException}
 */
public class ConcurrentExecutionException extends eu.bcvsolutions.idm.core.scheduler.api.exception.ConcurrentExecutionException {

	private static final long serialVersionUID = -9114230584353922445L;

	public ConcurrentExecutionException(ResultCode resultCode, Map<String, Object> parameters, Throwable throwable) {
		super(resultCode, parameters, throwable);
	}
	
	public ConcurrentExecutionException(ResultCode resultCode, Map<String, Object> parameters) {
		this(resultCode, parameters, null);
	}
	
	public ConcurrentExecutionException(ResultCode resultCode, Throwable throwable) {
		this(resultCode, null, throwable);
	}
}
