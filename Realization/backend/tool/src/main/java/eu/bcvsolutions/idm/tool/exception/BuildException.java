package eu.bcvsolutions.idm.tool.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.tool.domain.ToolResultCode;

/**
 * Build exception.
 * 
 * @author Radek Tomiška
 * @since 10.1.0
 */
public class BuildException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;

	public BuildException(Throwable exception) {
		super(ToolResultCode.BUILD_FAILED, exception);
	}
	
	public BuildException(String message) {
		super(ToolResultCode.BUILD_FAILED, ImmutableMap.of("message", message));
	}
	
	public BuildException(String message, Throwable exception) {
		super(ToolResultCode.BUILD_FAILED, ImmutableMap.of("message", message), exception);
	}
}
