package eu.bcvsolutions.idm.core.security.api.authentication;

import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
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
	
	public static int DEFAULT_AUTHENTICATOR_ORDER = 0;
	
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
	 * Try to authenticate identity and return {@link LoginDto} with {@link IdmJwtAuthenticationDto}
	 * 
	 * @param loginDto
	 * @return
	 */
	LoginDto authenticate(LoginDto loginDto);
	
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
