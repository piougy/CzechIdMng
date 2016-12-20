package eu.bcvsolutions.idm.acc.exception;

import java.util.Map;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

/**
 * Exception for provisioning
 * 
 * @author svandav
 *
 */
public class ProvisioningException extends ResultCodeException {

	private static final long serialVersionUID = 1L;

	public ProvisioningException(ResultCode resultCode, Map<String, Object> parameters, Throwable throwable) {
		super(resultCode, parameters, throwable);
	}

	public ProvisioningException(ResultCode resultCode, Map<String, Object> parameters) {
		super(resultCode, parameters);
	}

	public ProvisioningException(ResultCode resultCode, String message, Map<String, Object> parameters) {
		super(resultCode, message, parameters);
	}

	public ProvisioningException(ResultCode resultCode, String message, Throwable throwable) {
		super(resultCode, message, throwable);
	}

	public ProvisioningException(ResultCode resultCode, String message) {
		super(resultCode, message);
	}

	public ProvisioningException(ResultCode resultCode, Throwable throwable) {
		super(resultCode, throwable);
	}

	public ProvisioningException(ResultCode resultCode) {
		super(resultCode);
	}

}
