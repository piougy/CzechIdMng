package eu.bcvsolutions.idm.vs.exception;

import org.springframework.http.HttpStatus;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.ResultCode;

/**
 * Enum class for formatting response messages (mainly errors). 
 * Every enum contains a string message and corresponding https HttpStatus code.
 */
public enum VsResultCode implements ResultCode {
	// connector
	VS_IMPLEMENTER_WAS_NOT_FOUND(HttpStatus.BAD_REQUEST, "Implementer for UUID [%s] not found!"),
	VS_REQUEST_REALIZE_WRONG_STATE(HttpStatus.BAD_REQUEST, "For realize must be request in state: [%s], but he is in state: [#s]!"),
	VS_REQUEST_CANCEL_WRONG_STATE(HttpStatus.BAD_REQUEST, "For cancel must be request in state: [%s], but he is in state: [#s]!");
	
	
	private final HttpStatus status;
	private final String message;
	
	private VsResultCode(HttpStatus status, String message) {
		this.message = message;
		this.status = status;
	}
	
	public String getCode() {
		return this.name();
	}
	
	public String getModule() {
		return AccModuleDescriptor.MODULE_ID;
	}
	
	public HttpStatus getStatus() {
		return status;
	}
	
	public String getMessage() {
		return message;
	}	
}
