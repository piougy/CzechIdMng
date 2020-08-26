package eu.bcvsolutions.idm.core.security.api.authentication;

import org.springframework.core.Ordered;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;

/**
 * Authentication manager get {@link Authenticator} over all modules, sorted by {@link Ordered}.
 * Calls method authenticate from {@link Authenticator}.
 * <p>
 * TODO: method for FE find all {@link Authenticator} and create DTO.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */

public interface AuthenticationManager {
	
	String TEMPLATE_LOGIN_IS_BLOCKED = "loginBlocked";
	
	/**
	 * Process authenticate over all founded {@link Authenticator}.
	 * 
	 * @param loginDto
	 */
	LoginDto authenticate(LoginDto loginDto);
	
	/**
	 * Validate over all founded {@link Authenticator},
	 * return true if authentication is success.
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @deprecated @since 10.5.0 - skip must change flag can be controlled
	 * @see #validate(LoginDto)
	 */
	boolean validate(String username, GuardedString password);
	
	/**
	 * Validate over all founded {@link Authenticator},
	 * return true if authentication is success.
	 * 
	 * @param loginDto credentials
	 * @return
	 * @since 10.5.0
	 */
	boolean validate(LoginDto loginDto);
	
	/**
	 * Logout - process logout over all registered {@link Authenticator}. 
	 */
	void logout();
}
