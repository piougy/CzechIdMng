package eu.bcvsolutions.idm.core.api.exception;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;

/**
 * Adds http status to default result response
 *
 * @author Radek Tomiška
 */
@JsonInclude(Include.NON_NULL)
public class DefaultErrorModel extends DefaultResultModel implements ErrorModel {
	
	private static final long serialVersionUID = -4753197936461671782L;

	public DefaultErrorModel(ResultCode resultCode) {
		this(resultCode, null, null);
	}
	
	public DefaultErrorModel(ResultCode resultCode, Map<String, Object> parameters) {
		this(resultCode, null, parameters);
	}
	
	public DefaultErrorModel(ResultCode resultCode, String message) {
		this(resultCode, message, null);
	}
	
	public DefaultErrorModel(ResultCode resultCode, String message, Map<String, Object> parameters) {
		super(resultCode, message, parameters);
	}
	
}