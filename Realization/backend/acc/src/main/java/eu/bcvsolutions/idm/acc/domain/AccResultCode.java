package eu.bcvsolutions.idm.acc.domain;

import org.springframework.http.HttpStatus;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;

/**
 * Enum class for formatting response messages (mainly errors). 
 * Every enum contains a string message and corresponding https HttpStatus code.
 */
public enum AccResultCode implements ResultCode {
	CONNECTOR_INFO_FOR_SYSTEM_NOT_FOUND(HttpStatus.BAD_REQUEST, "Connector info for system %s not found!"),
	CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND(HttpStatus.BAD_REQUEST, "Connector configuration for system %s not found!"),
	CONNECTOR_SCHEMA_FOR_SYSTEM_NOT_FOUND(HttpStatus.BAD_REQUEST, "Connector schema for system %s not found!"),
	
	// Provisioning
	PROVISIONING_IDM_FIELD_NOT_FOUND(HttpStatus.BAD_REQUEST, "IDM field %property for entity %uid not found!"),
	PROVISIONING_SCHEMA_ATTRIBUTE_IS_NOT_UPDATEABLE(HttpStatus.BAD_REQUEST, "Schema attribute %property for entity %uid is not updateable!"),
	PROVISIONING_RESOURCE_ATTRIBUTE_NOT_FOUND(HttpStatus.BAD_REQUEST, "Attribute %attribute for entity %uid on resource not found!"),
	PROVISIONING_PASSWORD_WRONG_TYPE(HttpStatus.BAD_REQUEST, "Password attribute must be GuardedString type!"), 
	PROVISIONING_ATTRIBUTE_VALUE_WRONG_TYPE(HttpStatus.BAD_REQUEST, "Schema attribute %attribute defines typ %schamaAttributeType, but value is type %valueType!"),
	PROVISIONING_ATTRIBUTE_TYPE_NOT_FOUND(HttpStatus.BAD_REQUEST, "Schema attribute %attribute defines typ %schamaAttributeType, but we were unable to load this class!");
	
	
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
		return "acc";
	}
	
	public HttpStatus getStatus() {
		return status;
	}
	
	public String getMessage() {
		return message;
	}	
}
