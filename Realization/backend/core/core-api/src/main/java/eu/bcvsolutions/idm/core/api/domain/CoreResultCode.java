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
	BAD_UUID(HttpStatus.BAD_REQUEST, "The value %s is not uuid!"),
	NAME_CONFLICT(HttpStatus.CONFLICT, "%s for given name already exists."),
	CONFLICT(HttpStatus.CONFLICT, "%s"),
	NULL_ATTRIBUTE(HttpStatus.BAD_REQUEST, "Attribute '%s' is NULL."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "%s not found."),
	WF_WARNING(HttpStatus.BAD_REQUEST, "Warning occured during workflow execution: %s"),
	BAD_FILTER(HttpStatus.BAD_REQUEST, "The filter is wrong!"),
	UNMODIFIABLE_ATTRIBUTE_CHANGE(HttpStatus.BAD_REQUEST, "Attribute %s for class %s can't be changed!"),
	// http
	ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "The given endpoint doesn't exist!"),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Method is not allowed!"),
	// auth errors
	AUTH_FAILED(HttpStatus.UNAUTHORIZED, "Authentication failed - bad credentials."),
	AUTH_EXPIRED(HttpStatus.UNAUTHORIZED, "Authentication expired."),
	AUTHORITIES_CHANGED(HttpStatus.UNAUTHORIZED, "Authorities changed, log in again."),
	LOG_IN(HttpStatus.UNAUTHORIZED, "You need to be logged in."),
	XSRF(HttpStatus.UNAUTHORIZED, "XSRF cookie failed."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden."),
	// data
	SEARCH_ERROR(HttpStatus.BAD_REQUEST, "Error during searching entities. Error: %s"),
	UNMODIFIABLE_LOCKED(HttpStatus.CONFLICT, "This entity [%s] cannot be modified (is locked)!"),
	// filter
	FILTER_IMPLEMENTATION_NOT_FOUND(HttpStatus.CONFLICT, "Filter implementation [%s] for property [%s] not found. Repair configuration property [%s]."),
	// password change
	PASSWORD_CHANGE_NO_SYSTEM(HttpStatus.BAD_REQUEST, "No system selected."),
	PASSWORD_CHANGE_CURRENT_FAILED_IDM(HttpStatus.BAD_REQUEST, "Given current password doesn't match to current idm password."),
	PASSWORD_RESET_FAILED(HttpStatus.BAD_REQUEST, "Password reset failed: %s."),
	PASSWORD_RESET_GENERATE_TOKEN(HttpStatus.BAD_REQUEST, "Reset token generate failed: %s."),
	PASSWORD_CHANGE_FAILED(HttpStatus.BAD_REQUEST, "Password change failed: %s."),
	PASSWORD_EXPIRED(HttpStatus.UNAUTHORIZED, "Password expired"),
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
	ROLE_CATALOGUE_BAD_NICE_NAME(HttpStatus.CONFLICT, "Nice name [%s] is found at same level."),
	//
	MODULE_NOT_DISABLEABLE(HttpStatus.BAD_REQUEST, "Module [%s] is not disableable."),
	MODULE_DISABLED(HttpStatus.BAD_REQUEST, "Module [%s] is disabled."),
	CONFIGURATION_DISABLED(HttpStatus.BAD_REQUEST, "Configuration [%s] is disabled."),
	// role
	ROLE_DELETE_FAILED_IDENTITY_ASSIGNED(HttpStatus.CONFLICT, "Role [%s] cannot be deleted - some identites have role assigned."),
	// groovy script
	GROOVY_SCRIPT_VALIDATION(HttpStatus.BAD_REQUEST, "Script contains compillation errors"),
	GROOVY_SCRIPT_SECURITY_VALIDATION(HttpStatus.BAD_REQUEST, "Script did not pass security inspection!"),
	GROOVY_SCRIPT_NOT_ACCESSIBLE_CLASS(HttpStatus.BAD_REQUEST, "Class [%s] isn't accessible!"),
	GROOVY_SCRIPT_NOT_ACCESSIBLE_SERVICE(HttpStatus.BAD_REQUEST, "Service [%s] isn't accessible!"),
	// eav
	FORM_VALUE_WRONG_TYPE(HttpStatus.BAD_REQUEST, "Form value [%s] for attribute [%s] has to be type of [%s], given [%s]"),
	FORM_ATTRIBUTE_DELETE_FAILED_HAS_VALUES(HttpStatus.CONFLICT, "Form attribute [%s] cannot be deleted - some form values already using this attribute."),
	FORM_ATTRIBUTE_DELETE_FAILED_SYSTEM_ATTRIBUTE(HttpStatus.CONFLICT, "Form attribute [%s] cannot be deleted - this attribute is flaged as system attribute."),
	FORM_DEFINITION_DELETE_FAILED_SYSTEM_DEFINITION(HttpStatus.CONFLICT, "Form definition [%s] cannot be deleted - this definition is flaged as system definition."),
	// audit
	AUDIT_REVISION_NOT_SAME(HttpStatus.BAD_REQUEST, "Audit revision are not same."),
	//
	PASSWORD_POLICY_DEFAULT_TYPE(HttpStatus.CONFLICT, "Default password policy is exist. Name [%s]"),
	PASSWORD_POLICY_DEFAULT_TYPE_NOT_EXIST(HttpStatus.NOT_FOUND, "Default password policy is not exist."),
	PASSWORD_POLICY_BAD_TYPE(HttpStatus.BAD_REQUEST, "Password policy has bad type: [%s]."),
	PASSWORD_POLICY_VALIDATION(HttpStatus.I_AM_A_TEAPOT, "Password policy validation problem."),
	PASSWORD_POLICY_MAX_LENGTH_LOWER(HttpStatus.BAD_REQUEST, "Password policy has max length lower than min length."),
	PASSWORD_POLICY_ALL_MIN_REQUEST_ARE_HIGHER(HttpStatus.BAD_REQUEST, "Password policy has sum of all minimum request higher than maximum length."),
	PASSWORD_POLICY_MAX_AGE_LOWER(HttpStatus.BAD_REQUEST, "Password policy has max password age lower than min age."),
	PASSWORD_POLICY_MAX_RULE(HttpStatus.BAD_REQUEST, "Password policy: minimum rules to fulfill must be [%s] or lower."),
	//
	SCHEDULER_INVALID_CRON_EXPRESSION(HttpStatus.BAD_REQUEST, "Cron expression [%s] is invalid."),
	SCHEDULER_UNSUPPORTED_TASK_TRIGGER(HttpStatus.BAD_REQUEST, "Task trigger [%s] is not supported."),
	SCHEDULER_CREATE_TASK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "%s"),
	SCHEDULER_DELETE_TASK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "%s"),
	SCHEDULER_INTERRUPT_TASK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "%s"),
	SCHEDULER_CREATE_TRIGGER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "%s"),
	SCHEDULER_DELETE_TRIGGER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "%s"),
	SCHEDULER_PAUSE_TRIGGER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "%s"),
	SCHEDULER_RESUME_TRIGGER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "%s"),
	//
	LONG_RUNNING_TASK_NOT_FOUND(HttpStatus.BAD_REQUEST, "Task type [%s] can not be instantiated"),
	LONG_RUNNING_TASK_NOT_RUNNING(HttpStatus.BAD_REQUEST, "Task [%s] not running - can not be innterrupt or cancel"),
	LONG_RUNNING_TASK_DIFFERENT_INSTANCE(HttpStatus.BAD_REQUEST, "Task [%s] has different instance [%s], can not be accessed from this instance [%s]."),
	LONG_RUNNING_TASK_IS_RUNNING(HttpStatus.BAD_REQUEST, "Task [%s] is running already - can not started twice"),
	LONG_RUNNING_TASK_IS_PROCESSED(HttpStatus.BAD_REQUEST, "Task [%s] is already processed - can not started twice"),
	LONG_RUNNING_TASK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Task [%s] type [%s] ended on instance [%s] with exception."),
	LONG_RUNNING_TASK_CANCELED_BY_RESTART(HttpStatus.GONE, "Task [%s] type [%s] on instance [%s] was canceled during restart."),
	LONG_RUNNING_TASK_INTERRUPT(HttpStatus.INTERNAL_SERVER_ERROR, "Task [%s] type [%s] on instance [%s] was intarrupt."),
	LONG_RUNNING_TASK_INIT_FAILED(HttpStatus.BAD_REQUEST, "Task [%s] type [%s] has invalid properties."),
	//
	// forest index
	FOREST_INDEX_DISABLED(HttpStatus.BAD_REQUEST, "Forest index is disabled. Enable configuration property [%s]."),
	FOREST_INDEX_RUNNING(HttpStatus.CONFLICT, "Rebuilding index for forest tree type [%s] already running."),
	//
	// notification
	NOTIFICATION_SYSTEM_TEMPLATE_DELETE_FAILED(HttpStatus.BAD_REQUEST, "System template [%s] can't be deleted."),
	NOTIFICATION_TOPIC_AND_LEVEL_EXISTS(HttpStatus.CONFLICT, "Configuration can not be saved. Topic [%s] and null level exists!"),
	//
	// Role request
	ROLE_REQUEST_NO_EXECUTE_IMMEDIATELY_RIGHT(HttpStatus.FORBIDDEN, "You do not have right for immidiately execute role request [%s]!"),
	ROLE_REQUEST_EXECUTE_WRONG_STATE(HttpStatus.BAD_REQUEST, "Request is in state [%s], only state APPROVED and CONCEPT can be executed!"),
	ROLE_REQUEST_APPLICANTS_NOT_SAME(HttpStatus.BAD_REQUEST, "Some concept/s role in role request [%s] have different applicant than [%s]!"),
	ROLE_REQUEST_EXECUTED_CANNOT_DELETE(HttpStatus.BAD_REQUEST, "Request [%s] in EXECUTED state cannot be deleted!"),
	ROLE_REQUEST_AUTOMATICALLY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "Field 'requested by' in request [%s] cannot be 'AUTOMATICALLY' via REST API!"),
	//
	// Crypt
	CRYPT_DEMO_KEY_NOT_FOUND(HttpStatus.BAD_REQUEST, "Demo key: [%s] cannot be found! Please create primary key: [%s] or fix demo key."),
	CRYPT_INITIALIZATION_PROBLEM(HttpStatus.BAD_REQUEST, "Initialization problem, algorithm [%s]."),
	//
	AUTHORIZATION_POLICY_GROUP_AUTHORIZATION_TYPE(HttpStatus.BAD_REQUEST, "When authorization type is filled [%s] then groupPermission has to be filled too [%s]."),
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
