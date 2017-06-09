package eu.bcvsolutions.idm.core.api.exception;

import java.util.Map;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;

/**
 * Exception for role request
 * 
 * @author svandav
 */
public class RoleRequestException extends ResultCodeException {

	private static final long serialVersionUID = 1L;

	public RoleRequestException(ResultCode resultCode, Map<String, Object> parameters, Throwable throwable) {
		super(resultCode, parameters, throwable);
	}

	public RoleRequestException(ResultCode resultCode, Map<String, Object> parameters) {
		super(resultCode, parameters);
	}

	public RoleRequestException(ResultCode resultCode, String message, Map<String, Object> parameters) {
		super(resultCode, message, parameters);
	}

	public RoleRequestException(ResultCode resultCode, String message, Throwable throwable) {
		super(resultCode, message, throwable);
	}

	public RoleRequestException(ResultCode resultCode, String message) {
		super(resultCode, message);
	}

	public RoleRequestException(ResultCode resultCode, Throwable throwable) {
		super(resultCode, throwable);
	}

	public RoleRequestException(ResultCode resultCode) {
		super(resultCode);
	}

}
