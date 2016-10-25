package eu.bcvsolutions.idm.icf.exception;

import java.util.Map;

import eu.bcvsolutions.idm.core.api.exception.CoreException;

public class IcfException extends CoreException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public IcfException(String message, Map<String, Object> details, Throwable cause) {
		super(message, details, cause);
	}


	public IcfException(String message, Map<String, Object> details) {
		super(message, details);
		// TODO Auto-generated constructor stub
	}


	public IcfException(String message, Throwable cause) {
		super(message, cause);
	}


	public IcfException(String message) {
		super(message);
	}


	public IcfException(Throwable cause) {
		super(cause);
	}


	public IcfException() {
	}

}
