package eu.bcvsolutions.idm.core.scheduler.exception;

import java.util.Map;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

/**
 * Scheduler exception - concurrent task is running
 * 
 * @author Radek Tomiška
 *
 */
public class ConcurrentExecutionException extends ResultCodeException {

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
