package eu.bcvsolutions.idm.vs.domain;

import org.springframework.http.HttpStatus;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;

/**
 * Enum class for formatting response messages (mainly errors). 
 * Every enum contains a string message and corresponding https HttpStatus code.
 * 
 * Used http codes:
 * - 2xx - success
 * - 4xx - client errors (validations, conflicts ...)
 * - 5xx - server errors
 * 
 * @author Radek Tomiška
 */
public enum VirtualSystemResultCode implements ResultCode {
	
	EXAMPLE_CLIENT_ERROR(HttpStatus.BAD_REQUEST, "Virtual system client error, bad value given [%s]"),
	EXAMPLE_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Virtual system server error with parameter [%s]");
	
	private final HttpStatus status;
	private final String message;
	
	private VirtualSystemResultCode(HttpStatus status, String message) {
		this.message = message;
		this.status = status;
	}
	
	public String getCode() {
		return this.name();
	}
	
	public String getModule() {
		return "vs";
	}
	
	public HttpStatus getStatus() {
		return status;
	}
	
	public String getMessage() {
		return message;
	}	
}
