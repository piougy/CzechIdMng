package eu.bcvsolutions.idm.tool.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.tool.domain.ToolResultCode;

/**
 * Release exception
 * 
 * @author Radek Tomiška
 *
 */
public class ReleaseException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;

	public ReleaseException(Throwable exception) {
		super(ToolResultCode.RELEASE_FAILED, exception);
	}
	
	public ReleaseException(String message) {
		super(ToolResultCode.RELEASE_FAILED, ImmutableMap.of("message", message));
	}
	
	public ReleaseException(String message, Throwable exception) {
		super(ToolResultCode.RELEASE_FAILED, ImmutableMap.of("message", message), exception);
	}
}
