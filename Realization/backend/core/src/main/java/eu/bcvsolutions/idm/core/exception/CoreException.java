package eu.bcvsolutions.idm.core.exception;

/**
 * Basic exception
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public class CoreException extends RuntimeException {

	private static final long serialVersionUID = 9124558345493748993L;
	private Object[] details;
	
	public CoreException() {
	}

	public CoreException(Throwable cause) {
		super(cause);
	}

	public CoreException(String message, Throwable cause) {
		super(message, cause);
	}

	public CoreException(String message) {
		super(message);
	}
	
	public CoreException(String message, Object[] details) {
		super(message);
		this.details = details;
	}
	
	public CoreException(String message, Object[] details, Throwable cause) {
		super(message, cause);
		this.details = details;
	}

	public Object[] getDetails() {
		return details;
	}

	public void setDetails(Object[] details) {
		this.details = details;
	}
}