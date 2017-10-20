package eu.bcvsolutions.idm.acc.exception;

import java.util.Map;

import eu.bcvsolutions.idm.core.api.exception.CoreException;

/**
 * Exception for synchronization
 * @author svandav
 *
 */

public class SynchronizationException extends CoreException {

	private static final long serialVersionUID = 1L;

	public SynchronizationException() {
		super();
	}

	public SynchronizationException(String message, Map<String, Object> details, Throwable cause) {
		super(message, details, cause);
	}

	public SynchronizationException(String message, Map<String, Object> details) {
		super(message, details);
	}

	public SynchronizationException(String message, Throwable cause) {
		super(message, cause);
	}

	public SynchronizationException(String message) {
		super(message);
	}

	public SynchronizationException(Throwable cause) {
		super(cause);
	}

}
