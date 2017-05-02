package eu.bcvsolutions.idm.core.security.auth.filter;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;

/**
 * A one-per-request container for Idm authentication information.
 * If the Idm JWT token authentication is successful, the token
 * is set as an instance variable and is available to all other authentication
 * filters in chain. If an error is raised during authentication, it is
 * also stored for evaluation.
 * 
 * The {@link ExtendExpirationFilter} class mainly benefits from storing
 * authentication token and exceptions, since it handles token expirations
 * and login error states. Without request-scoped storage it would be
 * necessary to parse the token again from the request.
 * 
 * @author Jan Helbich
 *
 */
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AuthenticationExceptionContext {

	private ResultCodeException codeEx;
	private AuthenticationException authEx;
	private IdmJwtAuthenticationDto token;

	public boolean isHasIdMToken() {
		return token != null;
	}
	
	public IdmJwtAuthenticationDto getToken() {
		return token;
	}

	public void setToken(IdmJwtAuthenticationDto token) {
		this.token = token;
	}

	public ResultCodeException getCodeEx() {
		return codeEx;
	}

	public void setCodeEx(ResultCodeException codeEx) {
		this.codeEx = codeEx;
	}

	public AuthenticationException getAuthEx() {
		return authEx;
	}

	public void setAuthEx(AuthenticationException authEx) {
		this.authEx = authEx;
	}

	public boolean isExpired() {
		return codeEx != null && getStatusEnum(codeEx).equals(CoreResultCode.AUTH_EXPIRED.getCode());
	}

	public boolean isAuthoritiesChanged() {
		return codeEx != null && getStatusEnum(codeEx).equals(CoreResultCode.AUTHORITIES_CHANGED.getCode());
	}

	public boolean isDisabledOrNotExists() {
		return authEx != null;
	}

	private String getStatusEnum(ResultCodeException e) {
		return e.getError().getError().getStatusEnum();
	}
	
}
