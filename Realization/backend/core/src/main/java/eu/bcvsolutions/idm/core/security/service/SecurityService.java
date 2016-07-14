package eu.bcvsolutions.idm.core.security.service;

import java.util.Set;

import eu.bcvsolutions.idm.core.security.domain.AbstractAuthentication;

/**
 * Security context helper methods 
 * 
 * @author svandav
 */
public interface SecurityService {

	/**
	 * Obtains the currently authentication request token.
	 *
	 * @return the <code>Authentication</code> or <code>null</code> if no authentication
	 * information is available
	 */
	AbstractAuthentication getAuthentication();
	
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

}
