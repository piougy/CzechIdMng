package eu.bcvsolutions.idm.core.exception;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.bcvsolutions.idm.core.model.dto.DefaultResultModel;

/**
 * Adds http status to default result response
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@JsonInclude(Include.NON_NULL)
public class DefaultErrorModel extends DefaultResultModel implements ErrorModel {
	
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