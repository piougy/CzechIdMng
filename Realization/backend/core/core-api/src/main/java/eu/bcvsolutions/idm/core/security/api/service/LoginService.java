package eu.bcvsolutions.idm.core.security.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
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
	 * Login identity and returns assigned token.
	 * 
	 * @param loginDto
	 * @return
	 */
	LoginDto login(LoginDto loginDto);

	/**
	 * Login with remote token an get the CIDMST token. Remote token can be obtained by external authentication system (e.g. OpenAM, OAuth).
	 * Security context contains logged identity thanks to authentication filters.
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
	
	/**
	 * Login as other identity.
	 * 
	 * @param identity target identity
	 * @param permission permissions to evaluate (AND)
	 * @return switched login dto
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 * @since 10.5.0
	 */
	LoginDto switchUser(IdmIdentityDto identity, BasePermission... permission);
	
	/**
	 * Logout other identity - return back to original user.
	 * 
	 * @return switched login dto
	 * @since 10.5.0
	 */ 
	LoginDto switchUserLogout();
}
