package eu.bcvsolutions.idm.core.security.api.service;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Service supports authorization evaluation.
 * 
 * @author Radek Tomiška
 */
public interface AuthorizableService<E extends Identifiable> {

	/**
	 * Secured type
	 * 
	 * @return
	 */
	AuthorizableType getAuthorizableType();	
}
