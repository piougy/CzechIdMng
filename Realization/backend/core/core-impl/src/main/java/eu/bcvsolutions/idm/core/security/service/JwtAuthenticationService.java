package eu.bcvsolutions.idm.core.security.service;


import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;

/**
 * This class provides methods for internal IdM authentication by JWT tokens.
 * @author Alena Peterová
 *
 */
public interface JwtAuthenticationService {

	/**
	 * The method handles internal authentication in IdM.
	 * It can be used for authenticators and authentication filters after
	 * user credentials are properly validated.
	 * The method creates JWT token and calls internal IdM authentication manager. 
	 *  
	 * @param loginDto Login credentials which were used to authenticate.
	 * @param identity The DTO of identity which will be logged in.
	 * @param module The module which authenticated the user.
	 * @return loginDto with added JWT token
	 */
	LoginDto createJwtAuthenticationAndAuthenticate(LoginDto loginDto, IdmIdentityDto identity, String module);
}
