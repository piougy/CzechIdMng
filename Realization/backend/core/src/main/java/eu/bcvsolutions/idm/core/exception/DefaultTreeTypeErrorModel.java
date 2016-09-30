package eu.bcvsolutions.idm.core.exception;

import java.util.Map;

public class DefaultTreeTypeErrorModel extends ResultCodeException {
	
	private static final long serialVersionUID = -2264533983464265702L;

	public DefaultTreeTypeErrorModel(ResultCode resultCode, String message, Map<String, Object> parameters) {
		super(resultCode, message, parameters);
	}

	public DefaultTreeTypeErrorModel(CoreResultCode code, Map<String, Object> of) {
		super(code, of);
	}
	
	public DefaultTreeTypeErrorModel(CoreResultCode code, String message) {
		super(code, message);
	}
}
