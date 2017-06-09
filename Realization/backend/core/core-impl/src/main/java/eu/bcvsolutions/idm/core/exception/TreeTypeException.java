package eu.bcvsolutions.idm.core.exception;

import java.util.Map;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

public class TreeTypeException extends ResultCodeException {
	
	private static final long serialVersionUID = -2264533983464265702L;

	public TreeTypeException(ResultCode resultCode, String message, Map<String, Object> parameters) {
		super(resultCode, message, parameters);
	}

	public TreeTypeException(ResultCode code, Map<String, Object> of) {
		super(code, of);
	}
	
	public TreeTypeException(ResultCode code, String message) {
		super(code, message);
	}
}
