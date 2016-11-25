package eu.bcvsolutions.idm.security.exception;

import java.util.Map;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.api.exception.ErrorModel;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

/**
 * Security exception. Used for example on check groovy script validity.
 * @author svandav
 *
 */
public class IdmSecurityException extends ResultCodeException {

	private static final long serialVersionUID = 1L;

	public IdmSecurityException(ErrorModel resultModel, Throwable throwable) {
		super(resultModel, throwable);
	}

	public IdmSecurityException(ErrorModel resultModel) {
		super(resultModel);
	}

	public IdmSecurityException(ResultCode resultCode, Map<String, Object> parameters, Throwable throwable) {
		super(resultCode, parameters, throwable);
	}

	public IdmSecurityException(ResultCode resultCode, Map<String, Object> parameters) {
		super(resultCode, parameters);
	}

	public IdmSecurityException(ResultCode resultCode, String message, Map<String, Object> parameters,
			Throwable throwable) {
		super(resultCode, message, parameters, throwable);
	}

	public IdmSecurityException(ResultCode resultCode, String message, Map<String, Object> parameters) {
		super(resultCode, message, parameters);
	}

	public IdmSecurityException(ResultCode resultCode, String message, Throwable throwable) {
		super(resultCode, message, throwable);
	}

	public IdmSecurityException(ResultCode resultCode, String message) {
		super(resultCode, message);
	}

	public IdmSecurityException(ResultCode resultCode, Throwable throwable) {
		super(resultCode, throwable);
	}

	public IdmSecurityException(ResultCode resultCode) {
		super(resultCode);
	}

	
}
