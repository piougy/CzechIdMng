package eu.bcvsolutions.idm.core.security.api.service;

import java.util.Set;

import eu.bcvsolutions.idm.core.security.api.domain.AbstractAuthentication;

/**
 * Security context helper methods
 * 
 * TODO: logout - clear authentication
 * 
 * @author svandav
 */
public interface SecurityService {
	
	/**
	 * Name which is used as current/original username
	 */
	public static final String SYSTEM_NAME = "[SYSTEM]";
	public static final String GUEST_NAME = "[GUEST]";
	
	/**
	 * Changes the currently authenticated principal, or removes the authentication
	 * information.
	 *
	 * @param authentication the new <code>Authentication</code> token, or
	 * <code>null</code> if no further authentication information should be stored
	 */
	void setAuthentication(AbstractAuthentication authentication);
	
	/**
	 * Login as system (in scheduler etc.)
	 */
	void setSystemAuthentication();

	/**
	 * Obtains the currently authenticated principal, or an authentication request token.
	 *
	 * @return the <code>AbstractAuthentication</code> or <code>null</code> if no authentication of type AbstractAuthentication
	 * information is available
	 */
	AbstractAuthentication getAuthentication();
	
	/**
	 * Returns true, if identity is logged
	 * 
	 * @return
	 */
	boolean isAuthenticated();
	
	/**
	 * Returns currently logged identity's username
	 * 
	 * @return logged identity's username or <code>null</code> if no authentication
	 */
	String getUsername();

	/**
	 * Returns originally logged identity's username (before identity was switched)
	 * 
	 * @return originally logged identity's username
	 */
	String getOriginalUsername();
	
	/**
	 * Returns currently logged identity's authorities
	 * 
	 * @return authorities names
	 */
	Set<String> getAllAuthorities();

	/**
	 * Returns true, if currently logged identity has at least one of given authority
	 * 
	 * @param authorities
	 * @return true, if currently logged identity has at least one of given authority
	 */
	boolean hasAnyAuthority(String... authorities);
	
	/**
	 * Returns true, if logged identity is super administrator. Could be used for single user mode.
	 * 
	 * @return
	 */
	boolean isAdmin();
}
