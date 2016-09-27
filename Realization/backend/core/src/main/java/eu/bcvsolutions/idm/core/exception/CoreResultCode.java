package eu.bcvsolutions.idm.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Enum class for formatting response messages (mainly errors). 
 * Every enum contains a string message and corresponding https HttpStatus code.
 */
public enum CoreResultCode implements ResultCode {
	//
	// 2xx
	OK(HttpStatus.OK, "ok"),
	ACCEPTED(HttpStatus.ACCEPTED, "Request is accepted and will be processed asynchronously"),
	//
	// 4xx - zatim jsem zakomentoval nepouzite, abychom to potom dohledali a odstranili
	// commons
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "The value is wrong!"),
	BAD_VALUE(HttpStatus.BAD_REQUEST, "The value %s is wrong!"),
	NAME_CONFLICT(HttpStatus.CONFLICT, "%s for given name already exists."),
	CONFLICT(HttpStatus.CONFLICT, "%s"),
	NULL_ATTRIBUTE(HttpStatus.BAD_REQUEST, "Attribute '%s' is NULL."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "%s not found."),
	WF_WARNING(HttpStatus.BAD_REQUEST, "Warning occured during workflow execution: %s"),
	// http
	ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "The given endpoint doesn't exist!"),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Method is not allowed!"),
	// auth errors
	AUTH_FAILED(HttpStatus.UNAUTHORIZED, "Authentication failed - bad credentials."),
	AUTH_EXPIRED(HttpStatus.UNAUTHORIZED, "Authentication expired."),
	LOG_IN(HttpStatus.UNAUTHORIZED, "You need to be logged in."),
	XSRF(HttpStatus.UNAUTHORIZED, "XSRF cookie failed."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden."),
	// data
	SEARCH_ERROR(HttpStatus.BAD_REQUEST, "Error during searching entities. Error: %s"),
	UNMODIFIABLE_LOCKED(HttpStatus.CONFLICT, "This entity (%s) cannot be modified (is locked)!"),
	// password change
	PASSWORD_CHANGE_NO_SYSTEM(HttpStatus.BAD_REQUEST, "No system selected."),
	PASSWORD_CHANGE_CURRENT_FAILED_IDM(HttpStatus.BAD_REQUEST, "Given current password doesn't match to current idm password."),
	PASSWORD_RESET_FAILED(HttpStatus.BAD_REQUEST, "Password reset failed: %s."),
	PASSWORD_RESET_GENERATE_TOKEN(HttpStatus.BAD_REQUEST, "Reset token generate failed: %s."),
	PASSWORD_CHANGE_FAILED(HttpStatus.BAD_REQUEST, "Password change failed: %s."),
	MUST_CHANGE_IDM_PASSWORD(HttpStatus.UNAUTHORIZED, "User %s has to change password"),
	PASSWORD_DOES_NOT_MEET_POLICY(HttpStatus.BAD_REQUEST, "Password does not match password policy: %s"),
	PASSWORD_CANNOT_CHANGE(HttpStatus.BAD_REQUEST, "You cannot change your password yet. Please try it again after %s"),
	TASK_SAME_DELEGATE_AS_CURRENT_IDENTITY(HttpStatus.BAD_REQUEST, "You cannot create self delegation (%s)"),	
	//
	MODULE_NOT_DISABLEABLE(HttpStatus.BAD_REQUEST, "Module [%s] is not disableable"),
	MODULE_DISABLED(HttpStatus.BAD_REQUEST, "Module [%s] is disabled"),
	CONFIGURATION_DISABLED(HttpStatus.BAD_REQUEST, "Configuration [%s] is disabled"),
	// 5xx	
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "%s"),
	NOT_IMPLEMENTED(HttpStatus.INTERNAL_SERVER_ERROR, "Not implemented: %s"),
	WF_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Error occured during workflow execution: %s");

	private final HttpStatus status;
	private final String message;
	
	private CoreResultCode(HttpStatus status, String message) {
		this.message = message;
		this.status = status;
	}
	
	public String getCode() {
		return this.name();
	}
	
	public String getModule() {
		return "core";
	}
	
	public HttpStatus getStatus() {
		return status;
	}
	
	public String getMessage() {
		return message;
	}	
}
