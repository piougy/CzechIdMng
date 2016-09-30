package eu.bcvsolutions.idm.core.exception;

import java.util.Map;

/**
 * Default error model for Tree nodes
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class DefaultTreeNodeErrorModel extends ResultCodeException {

	private static final long serialVersionUID = 6302612443559279536L;
	
	public DefaultTreeNodeErrorModel(ResultCode resultCode, String message, Map<String, Object> parameters) {
		super(resultCode, message, parameters);
	}

	public DefaultTreeNodeErrorModel(CoreResultCode code, Map<String, Object> of) {
		super(code, of);
	}
	
	public DefaultTreeNodeErrorModel(CoreResultCode code, String message) {
		super(code, message);
	}
}
