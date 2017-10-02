package eu.bcvsolutions.idm.vs.exception;

import java.util.Map;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.api.exception.ErrorModel;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

/**
 * Base exception for VS module
 * @author svandav
 *
 */
public class VsException extends ResultCodeException {

	
	public VsException(ErrorModel resultModel, Throwable throwable) {
		super(resultModel, throwable);
	}

	public VsException(ErrorModel resultModel) {
		super(resultModel);
	}

	public VsException(ResultCode resultCode, Map<String, Object> parameters, Throwable throwable) {
		super(resultCode, parameters, throwable);
	}

	public VsException(ResultCode resultCode, Map<String, Object> parameters) {
		super(resultCode, parameters);
	}

	public VsException(ResultCode resultCode, String message, Map<String, Object> parameters, Throwable throwable) {
		super(resultCode, message, parameters, throwable);
	}

	public VsException(ResultCode resultCode, String message, Map<String, Object> parameters) {
		super(resultCode, message, parameters);
	}

	public VsException(ResultCode resultCode, String message, Throwable throwable) {
		super(resultCode, message, throwable);
	}

	public VsException(ResultCode resultCode, String message) {
		super(resultCode, message);
	}

	public VsException(ResultCode resultCode, Throwable throwable) {
		super(resultCode, throwable);
	}

	public VsException(ResultCode resultCode) {
		super(resultCode);
	}

	private static final long serialVersionUID = 1L;
	
	


}
