package eu.bcvsolutions.idm.ic.exception;

import java.util.Map;

import eu.bcvsolutions.idm.core.api.exception.CoreException;

public class IcException extends CoreException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public IcException(String message, Map<String, Object> details, Throwable cause) {
		super(message, details, cause);
	}


	public IcException(String message, Map<String, Object> details) {
		super(message, details);
		// TODO Auto-generated constructor stub
	}


	public IcException(String message, Throwable cause) {
		super(message, cause);
	}


	public IcException(String message) {
		super(message);
	}


	public IcException(Throwable cause) {
		super(cause);
	}


	public IcException() {
	}

}
