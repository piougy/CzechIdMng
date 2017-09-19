package eu.bcvsolutions.idm.core.security.api.service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;

/**
 * Load identity's granted authorities
 * 
 * @author svandav
 */
public interface GrantedAuthoritiesFactory {

	/**
	 * Returns unique set of valid authorities by assigned active roles for given identity.
	 * TODO: Sub roles are also processed - check implementation after sub roles will be enabled.
	 * 
	 * @param username
	 * @return
	 */
	List<GrantedAuthority> getGrantedAuthorities(String username);

	/**
	 * @see GrantedAuthoritiesFactory#getGrantedAuthorities(String)
	 */
	Collection<GrantedAuthority> getActiveRoleAuthorities(UUID identityId, IdmRoleDto role);

	/**
	 * @see GrantedAuthoritiesFactory#getGrantedAuthorities(String)
	 */
	Collection<GrantedAuthority> getGrantedAuthoritiesForIdentity(UUID identityId);

	/**
	 * @see GrantedAuthoritiesFactory#getGrantedAuthorities(String)
	 */
	Collection<GrantedAuthority> getGrantedAuthoritiesForValidRoles(UUID identityId, Collection<IdmIdentityRoleDto> identityRoles);
	
	/**
	 * Decides whether the original collection contains all authorities
	 * in the given subset.
	 *  
	 * @param original
	 * @param subset
	 * @return
	 */
	boolean containsAllAuthorities(Collection<? extends GrantedAuthority> original,
								   Collection<? extends GrantedAuthority> subset);
}
