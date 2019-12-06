package eu.bcvsolutions.idm.tool.domain;

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
public enum ToolResultCode implements ResultCode {

	RELEASE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Release failed [%s]");

	private final HttpStatus status;
	private final String message;

	private ToolResultCode(HttpStatus status, String message) {
		this.message = message;
		this.status = status;
	}

	public String getCode() {
		return this.name();
	}
	
	public String getModule() {
		return "tool";
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}
}
