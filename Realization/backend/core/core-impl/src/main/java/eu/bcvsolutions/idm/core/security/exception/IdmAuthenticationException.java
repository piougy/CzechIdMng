package eu.bcvsolutions.idm.core.security.exception;

/**
 * Authentication exception.
 * 
 * @author svandav
 * @deprecated @since 10.7.0 use the same IdmAuthenticationException from api module
 */
@Deprecated 
public class IdmAuthenticationException extends eu.bcvsolutions.idm.core.security.api.exception.IdmAuthenticationException {

	private static final long serialVersionUID = -2560698903646341229L;

	public IdmAuthenticationException(String msg) {
		super(msg);
	}
	
	public IdmAuthenticationException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
