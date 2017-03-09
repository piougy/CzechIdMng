package eu.bcvsolutions.idm.core.security.api.authentication;

import org.springframework.core.Ordered;

import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;

/**
 * Authentication manager get {@link Authenticator} over all modules, sorted by {@link Ordered}.
 * Calls method authenticate from {@link Authenticator}.
 * 
 * TODO: method for FE find all {@link Authenticator} and create DTO.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface AuthenticationManager {
	
	/**
	 * Process authenticate over all founded {@link Authenticator},
	 * 
	 * @param loginDto
	 */
	LoginDto authenticate(LoginDto loginDto);
}
