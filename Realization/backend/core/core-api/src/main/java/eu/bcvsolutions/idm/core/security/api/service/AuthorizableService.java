package eu.bcvsolutions.idm.core.security.api.service;

import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Sevrvice supports authorizationevaluation.
 * 
 * @author Radek Tomiška
 *
 */
public interface AuthorizableService {

	/**
	 * Secured type
	 * 
	 * @return
	 */
	AuthorizableType getAuthorizableType();
	
}
