package eu.bcvsolutions.idm.acc.domain;

import org.springframework.http.HttpStatus;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.ResultCode;

/**
 * Enum class for formatting response messages (mainly errors). 
 * Every enum contains a string message and corresponding https HttpStatus code.
 */
public enum AccResultCode implements ResultCode {
	// connector
	CONNECTOR_KEY_FOR_SYSTEM_NOT_FOUND(HttpStatus.BAD_REQUEST, "Connector key for system %s not found!"),
	CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND(HttpStatus.BAD_REQUEST, "Connector configuration for system %s not found!"),
	CONNECTOR_SCHEMA_FOR_SYSTEM_NOT_FOUND(HttpStatus.BAD_REQUEST, "Connector schema for system %s not found!"),
	CONNECTOR_FORM_DEFINITION_NOT_FOUND(HttpStatus.BAD_REQUEST, "Configuration for remote server %s not found!"),
	// remote server
	REMOTE_SERVER_INVALID_CREDENTIAL(HttpStatus.BAD_REQUEST, "Invalid password for server %s!"),
	REMOTE_SERVER_NOT_FOUND(HttpStatus.BAD_REQUEST, "Remote connector server %s, not found, or isn't running."),
	REMOTE_SERVER_CANT_CONNECT(HttpStatus.BAD_REQUEST, "Can't connecto to remote server %s!"),
	REMOTE_SERVER_UNEXPECTED_ERROR(HttpStatus.BAD_REQUEST, "Unexpected error on server %s!"),
	//
	// system
	SYSTEM_DELETE_FAILED_HAS_ENTITIES(HttpStatus.BAD_REQUEST, "System [%s] has system entities assigned, cannot be deleted."),
	SYSTEM_DELETE_FAILED_HAS_ACCOUNTS(HttpStatus.BAD_REQUEST, "System [%s] has accounts assigned, cannot be deleted."),
	// attribute mapping
	ATTRIBUTE_MAPPING_DELETE_FAILED_USED_IN_SYNC(HttpStatus.BAD_REQUEST, "Attribute [%s] cannot be deleted. It is used in synchronization on this system!"),
	SYSTEM_MAPPING_DELETE_FAILED_USED_IN_SYNC(HttpStatus.BAD_REQUEST, "System mapping [%s] cannot be deleted. It is used in synchronization on this system!"),
	SYSTEM_MAPPING_TREE_TYPE_DELETE_FAILED(HttpStatus.CONFLICT, "Tree type [%s] has assigned mapping on system [%s], cannot be deleted. Remove them at first."),
	
	// system entity
	SYSTEM_ENTITY_DELETE_FAILED_HAS_OPERATIONS(HttpStatus.BAD_REQUEST, "System entity [%s] on system [%s] cannot be deleted. It is used in active provisioning operations!"),
	//
	// Provisioning
	PROVISIONING_IDM_FIELD_NOT_FOUND(HttpStatus.NOT_FOUND, "IDM field %s for entity %s not found!"),
	PROVISIONING_PASSWORD_FIELD_NOT_FOUND(HttpStatus.NOT_FOUND, "Mapped attribute for password (___PASSWORD__) field for entity %s not found!"),
	PROVISIONING_SCHEMA_ATTRIBUTE_IS_NOT_UPDATEABLE(HttpStatus.BAD_REQUEST, "Schema attribute %s for entity %s is not updateable!"),
	PROVISIONING_SCHEMA_ATTRIBUTE_IS_FOUND(HttpStatus.BAD_REQUEST, "Schema attribute %s not found!"),
	PROVISIONING_RESOURCE_ATTRIBUTE_NOT_FOUND(HttpStatus.BAD_REQUEST, "Attribute %s for entity %s on resource not found!"),
	PROVISIONING_PASSWORD_WRONG_TYPE(HttpStatus.BAD_REQUEST, "Password attribute must be GuardedString type!"),
	PROVISIONING_NEW_PASSWORD_FOR_ACCOUNT(HttpStatus.OK, "For object with uid [%s] on system [%s] was set password: [%s]."),
	PROVISIONING_ATTRIBUTE_VALUE_WRONG_TYPE(HttpStatus.BAD_REQUEST, "Schema attribute %s defines typ %s, but value is type %s!"),
	PROVISIONING_ATTRIBUTE_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "Schema attribute %s defines typ %s, but we were unable to load this class!"),
	PROVISIONING_ATTRIBUTE_MORE_UID(HttpStatus.CONFLICT, "More then one UID attribute was found for system %s. Only one UID attribute can be defined!"),
	PROVISIONING_ROLE_ATTRIBUTE_MORE_UID(HttpStatus.CONFLICT, "More then one UID attribute was found for role %s and system %s. Only one UID attribute can be defined!"),
	PROVISIONING_ATTRIBUTE_UID_IS_NOT_STRING(HttpStatus.BAD_REQUEST, "Value of UID attribute must be String, but value is %s."),
	PROVISIONING_ATTRIBUTE_UID_NOT_FOUND(HttpStatus.NOT_FOUND, "UID attribute was not found for system %s. UID attribute is mandatory for provisioning!"),
	PROVISIONING_GENERATED_UID_IS_NULL(HttpStatus.NOT_FOUND, "Generated UID is null (for system [%s])!"),
	PROVISIONING_DUPLICATE_ROLE_MAPPING(HttpStatus.CONFLICT, "Was found more attribute definitions for same UID for same role %s, system %s and entity type %s!"),
	PROVISIONING_DIFFERENT_UIDS_FROM_CONNECTOR(HttpStatus.BAD_REQUEST, "After provisioning for UID %s, connector returned more UID "
			+ "(for more object classes). This returned UIDs but isn't same [%s]. This is inconsistent state."),
	PROVISIONING_SYSTEM_DISABLED(HttpStatus.LOCKED, "Provisioning operation for object with uid [%s] on system [%s] is canceled. System is disabled."),
	PROVISIONING_SYSTEM_READONLY(HttpStatus.LOCKED, "Provisioning operation for object with uid [%s] on system [%s] is canceled. System is readonly."),
	PROVISIONING_PREPARE_ACCOUNT_ATTRIBUTES_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Preparing attribubes for object with uid [%s] on system [%s] (operation type [%s], object class [%s]) failed."),
	PROVISIONING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Provisioning operation for object with uid [%s] on system [%s], operation type [%s], object class [%s] failed."),
	PROVISIONING_SUCCEED(HttpStatus.OK, "Provisioning operation for object with uid [%s] on system [%s], operation type [%s], object class [%s] is sucessfully completed."),
	PROVISIONING_IS_IN_QUEUE(HttpStatus.ACCEPTED, "Provisioning operation for object with uid [%s] on system [%s], operation type [%s], object class [%s] was already in queue. Addind new operation request into queue."),
	PROVISIONING_MERGE_ATTRIBUTE_IS_NOT_MULTIVALUE(HttpStatus.BAD_REQUEST, "Object [%s]. For MERGE strategy must be attribute [%s] multivalued!"),
	PROVISIONING_ATTRIBUTE_STRATEGY_CONFLICT(HttpStatus.CONFLICT, "Strategies [%s] and [%s] are in conflict, for attribute [%s]!"),
	PROVISIONING_TREE_PARENT_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "Account for parent node [%s] was not found!"),
	PROVISIONING_TREE_TOO_MANY_PARENT_ACCOUNTS(HttpStatus.NOT_FOUND, "Too many accounts for parent node [%s] was found! Excepted only one for same system!"),
	//
	// Synchronization
	SYNCHRONIZATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Synchronization [%s] not found!"),
	SYNCHRONIZATION_IS_NOT_ENABLED(HttpStatus.LOCKED, "Synchronization [%s] is not enabled!"),
	SYNCHRONIZATION_SYSTEM_IS_NOT_ENABLED(HttpStatus.LOCKED, "Synchronization [%s] cannot be start because system [%s] is disabled!"),
	CONFIDENTIAL_VALUE_IS_NOT_GUARDED_STRING(HttpStatus.BAD_REQUEST, "Confidentila value for attribute [%s] is not GuardedString [%s]!"),
	SYNCHRONIZATION_CORRELATION_TO_MANY_RESULTS(HttpStatus.BAD_REQUEST, "Synchronization - to many entities found by correlation attribute [%s] for value [%s]!"),
	SYNCHRONIZATION_CORRELATION_BAD_VALUE(HttpStatus.BAD_REQUEST, "Synchronization - value: [%s] for find correlation attribute cant be converted!"),
	SYNCHRONIZATION_TOKEN_ATTRIBUTE_NOT_FOUND(HttpStatus.BAD_REQUEST, "For synchronization by own filter is token attribute mandatory!"),
	SYNCHRONIZATION_IS_RUNNING(HttpStatus.BAD_REQUEST, "Synchronization [%s] already running!"),
	SYNCHRONIZATION_IS_NOT_RUNNING(HttpStatus.BAD_REQUEST, "Synchronization [%s] is not running!"),
	SYNCHRONIZATION_TO_MANY_SYSTEM_ENTITY(HttpStatus.CONFLICT, "To many system entity items for same uid [%s]. Only one item is allowed!"),
	SYNCHRONIZATION_FILTER_VALUE_WRONG_TYPE(HttpStatus.BAD_REQUEST, "Synchronization filter must be instance of IcFilter, but value is type %s!"), 
	SYNCHRONIZATION_ERROR_DURING_SYNC_ITEM(HttpStatus.BAD_REQUEST, "Error during synchronization item with UID %s (%s)!"),
	SYNCHRONIZATION_TO_MANY_ACC_ACCOUNT(HttpStatus.CONFLICT, "To many acc account items for same uid [%s]. Only one item is allowed!"),
	SYNCHRONIZATION_IDM_FIELD_NOT_SET(HttpStatus.NOT_FOUND, "IDM field %s for entity %s cannot be set!"),
	SYNCHRONIZATION_IDM_FIELD_NOT_GET(HttpStatus.NOT_FOUND, "IDM field %s for entity %s cannot be get!"),
	SYNCHRONIZATION_TREE_ROOT_FILTER_VALUE_WRONG_TYPE(HttpStatus.BAD_REQUEST, "Synchronization root filter result must be instance of Boolean, but value is type %s!"), 
	SYNCHRONIZATION_ATTRIBUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "IDM attribute %s not found!"),
	//
	// authentication against system
	AUTHENTICATION_AGAINST_SYSTEM_FAILED(HttpStatus.BAD_REQUEST, "Authentication failed! For system [%s] and username [%s]."),
	AUTHENTICATION_USERNAME_DONT_EXISTS(HttpStatus.BAD_REQUEST, "Authentication failed! For username [%s] dont find username on system [%s]."),
	AUTHENTICATION_AUTHENTICATION_ATTRIBUTE_DONT_SET(HttpStatus.BAD_REQUEST, "Authentication failed! System [%s] hasn't set authentication attribute"),
	// Protection account system
	ACCOUNT_CANNOT_BE_DELETED_IS_PROTECTED(HttpStatus.BAD_REQUEST, "Account [%s] cannot be deleted. Is protected before delete!");
	
	private final HttpStatus status;
	private final String message;
	
	private AccResultCode(HttpStatus status, String message) {
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
