package eu.bcvsolutions.idm.ic.connid.domain;

import org.springframework.http.HttpStatus;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.ic.IcModuleDescriptor;

/**
 * Enum class for formatting response messages (mainly errors). 
 * Every enum contains a string message and corresponding https HttpStatus code.
 *
 */

public enum IcResultCode implements ResultCode {
	
	// default IC error
	IC_DEFAULT_ERROR(HttpStatus.BAD_REQUEST, "IC module default error."),
	// remote server
	REMOTE_SERVER_INVALID_CREDENTIAL(HttpStatus.BAD_REQUEST, "Invalid password for server %s!"),
	REMOTE_SERVER_NOT_FOUND(HttpStatus.BAD_REQUEST, "Remote connector server %s, not found, or isn't running."),
	REMOTE_SERVER_CANT_CONNECT(HttpStatus.BAD_REQUEST, "Can't connecto to remote server %s!"),
	REMOTE_SERVER_UNEXPECTED_ERROR(HttpStatus.BAD_REQUEST, "Unexpected error on server %s!");
	
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
		return IcModuleDescriptor.MODULE_ID;
	}
	
	public HttpStatus getStatus() {
		return status;
	}
	
	public String getMessage() {
		return message;
	}	
}
