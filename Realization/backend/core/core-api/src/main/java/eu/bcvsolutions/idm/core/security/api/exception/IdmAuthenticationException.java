package eu.bcvsolutions.idm.core.security.api.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Common authentication exception. Used in authentication filters and registered authenticators.
 * 
 * @author Radek Tomiška
 * @since 10.7.0
 */
public class IdmAuthenticationException extends AuthenticationException {

	private static final long serialVersionUID = 1L;

	public IdmAuthenticationException(String msg) {
		super(msg);
	}
	
	public IdmAuthenticationException(String msg, Throwable ex) {
		super(msg, ex);
	}
}