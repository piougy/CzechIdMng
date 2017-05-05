package eu.bcvsolutions.idm.core.api.domain;

import org.springframework.http.HttpStatus;

/**
 * Enum class for formatting response messages (mainly errors). 
 * Every enum contains a string message and corresponding https status code.
 * 
 * @author Radek Tomiška 
 */
public interface ResultCode {
	
	/**
	 * Unique error code
	 * 
	 * @return
	 */
	String getCode();
	
	/**
	 * Module identifier (core, ... etc)
	 * 
	 * @return
	 */
	String getModule();
	
	/**
	 * HTTP status code
     *
	 * @return
	 */
	HttpStatus getStatus();
	
	/**
	 * Error description
     *
	 * @return
	 */
	String getMessage();
}
