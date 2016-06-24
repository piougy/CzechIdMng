package eu.bcvsolutions.idm.core.security.exception;

import org.springframework.security.core.AuthenticationException;

public class IdmAuthenticationException extends AuthenticationException {

	/**
	 * Default Idm authentication 
	 */
	private static final long serialVersionUID = -2560698903646341229L;

	public IdmAuthenticationException(String msg) {
		super(msg);
	}

}
