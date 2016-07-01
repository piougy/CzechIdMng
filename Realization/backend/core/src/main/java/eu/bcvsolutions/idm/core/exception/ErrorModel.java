package eu.bcvsolutions.idm.core.exception;

import org.springframework.http.HttpStatus;

import eu.bcvsolutions.idm.core.model.dto.ResultModel;

/**
 * Adds http status to universal response result model
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
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