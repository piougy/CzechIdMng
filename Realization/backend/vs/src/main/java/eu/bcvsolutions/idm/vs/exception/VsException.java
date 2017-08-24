package eu.bcvsolutions.idm.vs.exception;

import java.util.Map;

import eu.bcvsolutions.idm.core.api.exception.CoreException;

/**
 * Base exception for VS module
 * @author svandav
 *
 */
public class VsException extends CoreException {

	
	private static final long serialVersionUID = 1L;
	
	
	public VsException(String message, Map<String, Object> details, Throwable cause) {
		super(message, details, cause);
	}


	public VsException(String message, Map<String, Object> details) {
		super(message, details);
	}


	public VsException(String message, Throwable cause) {
		super(message, cause);
	}


	public VsException(String message) {
		super(message);
	}


	public VsException(Throwable cause) {
		super(cause);
	}


	public VsException() {
	}

}
