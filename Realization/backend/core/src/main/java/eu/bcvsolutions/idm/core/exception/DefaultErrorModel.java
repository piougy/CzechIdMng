package eu.bcvsolutions.idm.core.exception;

import org.springframework.http.HttpStatus;

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
	
	/**
	 * Http status code
	 */
	private int statusCode;
	/**
	 * Http status name
	 */
	private HttpStatus status;
	
	public DefaultErrorModel() {
		super();
	}
	
	public DefaultErrorModel(ResultCode resultCode) {
		this(resultCode, null, null);
	}
	
	public DefaultErrorModel(ResultCode resultCode, Object[] parameters) {
		this(resultCode, null, parameters);
	}
	
	public DefaultErrorModel(ResultCode resultCode, String message) {
		this(resultCode, message, null);
	}
	
	public DefaultErrorModel(ResultCode resultCode, String message, Object[] parameters) {
		super(resultCode, message, parameters);
		this.status = resultCode.getStatus();
		this.statusCode = resultCode.getStatus().value();
	}

	public HttpStatus getStatus() {
		return status;
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
	}
	
	public int getStatusCode() {
		return statusCode;
	}
	
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
}