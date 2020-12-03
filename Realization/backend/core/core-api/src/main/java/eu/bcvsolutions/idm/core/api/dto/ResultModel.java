package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import java.time.ZonedDateTime;
import org.springframework.http.HttpStatus;

import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;

/**
 * Universal operation result response
 * 
 * @author Radek Tomiška 
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
	ZonedDateTime getCreation();

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
	
	/**
	 * Message level - override level by http status code.
	 * 
	 * @return level
	 * @since 10.7.0
	 */
	default NotificationLevel getLevel() {
		return null;
	}
}
