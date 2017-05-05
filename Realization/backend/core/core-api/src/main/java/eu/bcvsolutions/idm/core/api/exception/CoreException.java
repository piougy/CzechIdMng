package eu.bcvsolutions.idm.core.api.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic exception with named parameters
 * 
 * @author Radek Tomiška 
 */
public class CoreException extends RuntimeException {

	private static final long serialVersionUID = 9124558345493748993L;
	private final Map<String, Object> details = new HashMap<>();
	
	public CoreException() {
	}

	public CoreException(Throwable cause) {
		this(null, null, cause);
	}

	public CoreException(String message, Throwable cause) {
		this(message, null, cause);
	}

	public CoreException(String message) {
		this(message, null, null);
	}
	
	public CoreException(String message, Map<String, Object> details) {
		this(message, details, null);
	}
	
	public CoreException(String message, Map<String, Object> details, Throwable cause) {
		super(message, cause);
		if (details != null) {
			this.details.putAll(details);
		}
	}

	public Map<String, Object> getDetails() {
		return Collections.unmodifiableMap(details);
	}

}