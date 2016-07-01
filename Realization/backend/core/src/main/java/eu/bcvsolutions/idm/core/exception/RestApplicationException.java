package eu.bcvsolutions.idm.core.exception;

import org.springframework.http.HttpStatus;

import eu.bcvsolutions.idm.core.model.dto.ResultModels;

/**
 * This error class propagates exception into front end. It contains a message and HTTP status code and unique id.
 * Every error has it's unique id under which you can find it in log.
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public final class RestApplicationException extends CoreException {
	
	private static final long serialVersionUID = -7022978890145637612L;
	/**
	 * This object holds information about this exception (a HTTP status code defined by RFC 2616, an exception message).
	 */
	private ResultModels resultModels;
	
	private RestApplicationException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
	public RestApplicationException (ResultCode resultCode) {
		this(resultCode, (String) null);
	}
	
	public RestApplicationException (ResultCode resultCode, Throwable throwable) {
		this(resultCode, (String) null, throwable);
	}
	
	public RestApplicationException (ResultCode resultCode, Object[] parameters, Throwable throwable) {
		this(new DefaultErrorModel(resultCode, parameters), throwable);
	}
	
	public RestApplicationException (ResultCode resultCode, Object[] parameters) {
		this(new DefaultErrorModel(resultCode, parameters));
	}
	
	public RestApplicationException (ResultCode resultCode, String message) {
		this(new DefaultErrorModel(resultCode, message, null), null);
	}
	
	public RestApplicationException (ResultCode resultCode, String message, Throwable throwable) {
		this(new DefaultErrorModel(resultCode, message, null), throwable);
	}
	
	public RestApplicationException (ResultCode resultCode, String message, Object[] parameters) {
		this(new DefaultErrorModel(resultCode, message, parameters));
	}
	
	public RestApplicationException (ResultCode resultCode, String message, Object[] parameters, Throwable throwable) {
		this(new DefaultErrorModel(resultCode, message, parameters), throwable);
	}
	
	public RestApplicationException(ErrorModel resultModel) {
		this(resultModel, (Throwable) null);
	}
	
	public RestApplicationException(ErrorModel resultModel, Throwable throwable) {
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