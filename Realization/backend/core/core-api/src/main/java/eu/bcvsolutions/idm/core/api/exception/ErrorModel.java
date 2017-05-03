package eu.bcvsolutions.idm.core.api.exception;

import org.springframework.http.HttpStatus;

import eu.bcvsolutions.idm.core.api.dto.ResultModel;

/**
 * Adds http status to universal response result model
 * 
 * @author Radek Tomiška 
 */
public interface ErrorModel extends ResultModel {

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