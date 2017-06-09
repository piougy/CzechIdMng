package eu.bcvsolutions.idm.core.security.service;

import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;

/**
 * Authenticate identity
 * 
 * @author svandav
 *
 */
public interface LoginService {

	public static final String PROPERTY_EXPIRATION_TIMEOUT = "idm.sec.security.jwt.expirationTimeout";
	public static final int DEFAULT_EXPIRATION_TIMEOUT = 10 * 60 * 1000; // default is 10 minutes

	/**
	 * Login identity and returns assigned token
	 * 
	 * @param loginDto
	 * @return
	 */
	public LoginDto login(LoginDto loginDto);

	public LoginDto loginAuthenticatedUser();
}
