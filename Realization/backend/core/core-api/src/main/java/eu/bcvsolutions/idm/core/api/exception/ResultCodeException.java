package eu.bcvsolutions.idm.core.api.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;

/**
 * This error class propagates exception into front end. It contains a message and HTTP status code and unique id.
 * Every error has it's unique id under which you can find it in log.
 * 
 * @author Radek Tomiška 
 *
 */
public class ResultCodeException extends CoreException {
	
	private static final long serialVersionUID = -7022978890145637612L;
	/**
	 * This object holds information about this exception (a HTTP status code defined by RFC 2616, an exception message).
	 */
	private ResultModels resultModels;
	
	private ResultCodeException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
	public ResultCodeException (ResultCode resultCode) {
		this(resultCode, (String) null);
	}
	
	public ResultCodeException (ResultCode resultCode, Throwable throwable) {
		this(resultCode, (String) null, throwable);
	}
	
	public ResultCodeException (ResultCode resultCode, Map<String, Object> parameters, Throwable throwable) {
		this(new DefaultErrorModel(resultCode, parameters), throwable);
	}
	
	public ResultCodeException (ResultCode resultCode, Map<String, Object> parameters) {
		this(new DefaultErrorModel(resultCode, parameters));
	}
	
	public ResultCodeException (ResultCode resultCode, String message) {
		this(new DefaultErrorModel(resultCode, message, null), null);
	}
	
	public ResultCodeException (ResultCode resultCode, String message, Throwable throwable) {
		this(new DefaultErrorModel(resultCode, message, null), throwable);
	}
	
	public ResultCodeException (ResultCode resultCode, String message, Map<String, Object> parameters) {
		this(new DefaultErrorModel(resultCode, message, parameters));
	}
	
	public ResultCodeException (ResultCode resultCode, String message, Map<String, Object> parameters, Throwable throwable) {
		this(new DefaultErrorModel(resultCode, message, parameters), throwable);
	}
	
	public ResultCodeException(ErrorModel resultModel) {
		this(resultModel, (Throwable) null);
	}
	
	public ResultCodeException(ErrorModel resultModel, Throwable throwable) {
		this(resultModel.getMessage(), throwable);
		this.resultModels = new ResultModels(resultModel);
	}
		
	public ResultModels getError() {
		return this.resultModels;
	}
	
	public HttpStatus getStatus() {
		return this.getError().getErrors().get(0).getStatus();
	}
		
	public String getId() {
		return this.getError().getErrors().get(0).getId();
	}
	
	@Override	
	public String getMessage() {
		return super.getMessage();
	}
	
	public String getErrorID() {
		return getId();
	}	
	
}