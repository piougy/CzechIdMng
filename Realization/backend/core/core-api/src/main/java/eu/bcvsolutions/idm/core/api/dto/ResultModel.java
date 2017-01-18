package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;

/**
 * Universal operation result response
 * 
 * @author Radek Tomiška 
 *
 */
public interface ResultModel extends Serializable {

	/**
	 * Unique model id
	 * 
	 * @return
	 */
	UUID getId();
	
	/**
	 * This model creation date
	 * 
	 * @return
	 */
	DateTime getCreation();

	/**
	 * Internal message
	 * 
	 * @return
	 */
	String getMessage();

	/**
	 * Idm status message / code - can be used for localization on client
	 * 
	 * @return
	 */
	String getStatusEnum();

	/**
	 * Message parameters
	 * 
	 * @return
	 */
	Map<String, Object> getParameters();
	
	/**
	 * Module identifier (core, ... etc)
	 * 
	 * @return
	 */
	String getModule();
	
	/**
	 * http status code
	 * 
	 * @return
	 */
	int getStatusCode();
	
	/**
	 * textual http status code 
	 * 
	 * @return
	 */
	HttpStatus getStatus();
}
