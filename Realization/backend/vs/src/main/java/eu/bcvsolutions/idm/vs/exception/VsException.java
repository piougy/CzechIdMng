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
		// TODO Auto-generated constructor stub
	}

	public VsException(ErrorModel resultModel) {
		super(resultModel);
		// TODO Auto-generated constructor stub
	}

	public VsException(ResultCode resultCode, Map<String, Object> parameters, Throwable throwable) {
		super(resultCode, parameters, throwable);
		// TODO Auto-generated constructor stub
	}

	public VsException(ResultCode resultCode, Map<String, Object> parameters) {
		super(resultCode, parameters);
		// TODO Auto-generated constructor stub
	}

	public VsException(ResultCode resultCode, String message, Map<String, Object> parameters, Throwable throwable) {
		super(resultCode, message, parameters, throwable);
		// TODO Auto-generated constructor stub
	}

	public VsException(ResultCode resultCode, String message, Map<String, Object> parameters) {
		super(resultCode, message, parameters);
		// TODO Auto-generated constructor stub
	}

	public VsException(ResultCode resultCode, String message, Throwable throwable) {
		super(resultCode, message, throwable);
		// TODO Auto-generated constructor stub
	}

	public VsException(ResultCode resultCode, String message) {
		super(resultCode, message);
		// TODO Auto-generated constructor stub
	}

	public VsException(ResultCode resultCode, Throwable throwable) {
		super(resultCode, throwable);
		// TODO Auto-generated constructor stub
	}

	public VsException(ResultCode resultCode) {
		super(resultCode);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 1L;
	
	


}
