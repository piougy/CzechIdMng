package eu.bcvsolutions.idm.ic.domain;

import org.springframework.http.HttpStatus;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;

/**
 * Enum class for formatting response messages (mainly errors). 
 * Every enum contains a string message and corresponding https HttpStatus code.
 */
public enum IcResultCode implements ResultCode {
	//IC
	IC_FRAMEWORK_NOT_FOUND(HttpStatus.BAD_REQUEST, "IC framework %s not found!"),
	// auth errors
	AUTH_FAILED(HttpStatus.UNAUTHORIZED, "Authentication failed - bad credentials.");

	private final HttpStatus status;
	private final String message;
	
	private IcResultCode(HttpStatus status, String message) {
		this.message = message;
		this.status = status;
	}
	
	public String getCode() {
		return this.name();
	}
	
	public String getModule() {
		return "ic";
	}
	
	public HttpStatus getStatus() {
		return status;
	}
	
	public String getMessage() {
		return message;
	}	
}
