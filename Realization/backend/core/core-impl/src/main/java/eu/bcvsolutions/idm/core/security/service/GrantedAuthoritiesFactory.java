package eu.bcvsolutions.idm.core.security.service;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;

/**
 * Granted authorities for users
 * 
 * @author svandav
 */
public interface GrantedAuthoritiesFactory {

	/**
	 * Returns unique set authorities by assigned active roles to given identity
	 * 
	 * @param username
	 * @return
	 */
	List<GrantedAuthority> getGrantedAuthorities(String username);
}
