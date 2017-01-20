package eu.bcvsolutions.idm.core.api.domain;

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
	// 4xx
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
	// tree
	TREE_NODE_BAD_PARENT(HttpStatus.BAD_REQUEST, "Tree node: %s, have bad parent."),
	TREE_NODE_BAD_TYPE(HttpStatus.BAD_REQUEST, "Tree node: %s, have bad type."),
	TREE_NODE_BAD_CHILDREN(HttpStatus.BAD_REQUEST, "Tree node: %s, have bad children."),
	TREE_NODE_DELETE_FAILED_HAS_CHILDREN(HttpStatus.CONFLICT, "Tree node [%s] has children, cannot be deleted. Remove them at first."),
	TREE_NODE_DELETE_FAILED_HAS_CONTRACTS(HttpStatus.CONFLICT, "Tree node [%s] has contract assigned, cannot be deleted. Remove them at first."),
	TREE_TYPE_DELETE_FAILED_HAS_CHILDREN(HttpStatus.CONFLICT, "Tree type [%s] has children, cannot be deleted. Remove them at first."),
	TREE_TYPE_DELETE_FAILED_HAS_CONTRACTS(HttpStatus.CONFLICT, "Tree type [%s] has contract assigned, cannot be deleted. Remove them at first."),
	// role catalogs
	ROLE_CATALOGUE_BAD_PARENT(HttpStatus.BAD_REQUEST, "Role catalogue [%s] has bad parent."),
	ROLE_CATALOGUE_DELETE_FAILED_HAS_CHILDREN(HttpStatus.CONFLICT, "Role catalogue [%s] has children, cannot be deleted. Remove them at first."),
	//
	MODULE_NOT_DISABLEABLE(HttpStatus.BAD_REQUEST, "Module [%s] is not disableable."),
	MODULE_DISABLED(HttpStatus.BAD_REQUEST, "Module [%s] is disabled."),
	CONFIGURATION_DISABLED(HttpStatus.BAD_REQUEST, "Configuration [%s] is disabled."),
	// role
	ROLE_DELETE_FAILED_IDENTITY_ASSIGNED(HttpStatus.CONFLICT, "Role (%s) cannot be deleted - some identites have role assigned."),
	// groovy script
	GROOVY_SCRIPT_VALIDATION(HttpStatus.BAD_REQUEST, "Script contains compillation errors"),
	GROOVY_SCRIPT_SECURITY_VALIDATION(HttpStatus.BAD_REQUEST, "Script did not pass security inspection!"),
	// eav
	FORM_ATTRIBUTE_DELETE_FAILED_HAS_VALUES(HttpStatus.CONFLICT, "Form attribute (%s) cannot be deleted - some form values already using this attribute."),
	// audit
	AUDIT_REVISION_NOT_SAME(HttpStatus.BAD_REQUEST, "Audit revision are not same."),
	//
	PASSWORD_POLICY_DEFAULT_TYPE(HttpStatus.CONFLICT, "Default password policy is exist. Name [%s]"),
	PASSWORD_POLICY_DEFAULT_TYPE_NOT_EXIST(HttpStatus.NOT_FOUND, "Default password policy is not exist."),
	PASSWORD_POLICY_BAD_TYPE(HttpStatus.BAD_REQUEST, "Password policy has bad type: [%s]."),
	PASSWORD_POLICY_VALIDATION(HttpStatus.I_AM_A_TEAPOT, "Password policy validation problem."),
	//
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
