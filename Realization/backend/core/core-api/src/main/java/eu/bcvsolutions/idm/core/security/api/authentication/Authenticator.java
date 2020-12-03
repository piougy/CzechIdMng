package eu.bcvsolutions.idm.core.security.api.authentication;

import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.security.api.domain.AuthenticationResponseEnum;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;

/**
 * Interface for authenticator that will be called in {@link AuthenticationManager}.
 * Authenticator is ordered by {@link Ordered}, default order is in
 * {@link Authenticator.DEFAULT_AUTHENTICATOR_ORDER}
 * <p>
 * TODO: better ordered {@link PriorityOrdered}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */

public interface Authenticator extends Ordered {
	
	int DEFAULT_AUTHENTICATOR_ORDER = 0;
	
	/**
	 * Return name of authenticator
	 * 
	 * @return
	 */
	String getName();
	
	/**
	 * Return module id from {@link ModuleDescriptor}
	 * 
	 * @return
	 */
	String getModule();
	
	/**
	 * Authenticate identity and return {@link LoginDto} with set authentication {@link IdmJwtAuthenticationDto}.
	 * 
	 * 
	 * @param loginDto credentials
	 * @return
	 */
	LoginDto authenticate(LoginDto loginDto);
	
	/**
	 * Validate given credentials.
	 * 
	 * @param loginDto credentials
	 * @return true - credentials are valid, false otherwise
	 * @since 10.7.0
	 */
	default boolean validate(LoginDto loginDto) {
		// Lookout: Identity should be not logged in, credentials should be validated only.
		// Authenticate is called for maintain backward compatibility only!
		// Implement validate method in your Authenticator properly!
		return authenticate(loginDto) != null;
	}
	
	/**
	 * Logout.  Override, when logout feature is provided by Authenticator implementation.
	 * 
	 * @param token current IdM token - token contains username, external token id
	 */
	default void logout(IdmTokenDto token) {
		// nothing by default
	}
	
	/**
	 * Authenticator excepted result
	 * 
	 * @return
	 */
	AuthenticationResponseEnum getExceptedResult();
	
	/**
	 * Return if authenticator is enabled by module
	 * 
	 * @return
	 */
	boolean isDisabled();
}
