package eu.bcvsolutions.idm.core.security.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;

/**
 * Authenticate identity
 * 
 * @author svandav
 * @author Radek Tomiška
 */
public interface LoginService {

	String PROPERTY_EXPIRATION_TIMEOUT = "idm.sec.security.jwt.expirationTimeout";
	int DEFAULT_EXPIRATION_TIMEOUT = 10 * 60 * 1000; // default is 10 minutes

	/**
	 * Login identity and returns assigned token
	 * 
	 * @param loginDto
	 * @return
	 */
	LoginDto login(LoginDto loginDto);

	/**
	 * Login with remote token an get the CIDMST token. Remote token can be obtained by external authentication system (e.g. OpenAM, OAuth).
	 * Security context cointains logged identity thanks to authentication filters
	 * 
	 * @return
	 */
	LoginDto loginAuthenticatedUser();
	
	/**
	 * Logout currently logged identity and disable currently used token.
	 * 
	 * @since 8.2.0
	 */
	void logout();
	
	/**
	 * Logout given logged identity and disable currently used token.
	 * 
	 * @since 8.2.0
	 */
	void logout(IdmTokenDto token);
}
