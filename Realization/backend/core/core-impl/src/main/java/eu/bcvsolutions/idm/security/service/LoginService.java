package eu.bcvsolutions.idm.security.service;

import eu.bcvsolutions.idm.security.dto.LoginDto;

/**
 * Authenticate identity
 * 
 * @author svandav
 *
 */
public interface LoginService {

	public LoginDto login(String username, String password);

}
