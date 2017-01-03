package eu.bcvsolutions.idm.core.exception;

import java.util.Map;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

/**
 * Default error model for Tree nodes
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class TreeNodeException extends ResultCodeException {

	private static final long serialVersionUID = 6302612443559279536L;
	
	public TreeNodeException(ResultCode resultCode, String message, Map<String, Object> parameters) {
		super(resultCode, message, parameters);
	}

	public TreeNodeException(ResultCode code, Map<String, Object> of) {
		super(code, of);
	}
	
	public TreeNodeException(ResultCode code, String message) {
		super(code, message);
	}
}
