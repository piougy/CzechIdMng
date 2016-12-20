package eu.bcvsolutions.idm.security.service;

import eu.bcvsolutions.idm.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.security.dto.LoginDto;

/**
 * Authenticate identity
 * 
 * @author svandav
 *
 */
public interface LoginService {

	/**
	 * Login identity and returns assigned token
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public LoginDto login(String username, GuardedString password);

}
