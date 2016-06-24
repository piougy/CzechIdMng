package eu.bcvsolutions.idm.core.model.dto;

import java.util.Date;
import java.util.List;

/**
 * Universal operation result response
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public interface ResultModel {

	/**
	 * Unique model id
	 * 
	 * @return
	 */
	String getId();
	
	/**
	 * This model creation date
	 * 
	 * @return
	 */
	Date getCreation();

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
	List<Object> getParameters();
	
	/**
	 * Module identifier (core, ... etc)
	 * 
	 * @return
	 */
	String getModule();
}
