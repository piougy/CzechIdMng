package eu.bcvsolutions.idm.core.security.service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Load identity's granted authorities
 * 
 * @author svandav
 */
public interface GrantedAuthoritiesFactory {

	/**
	 * Returns unique set of authorities by assigned active roles for given identity.
	 * Sub roles are also processed.
	 * 
	 * @param username
	 * @return
	 */
	List<GrantedAuthority> getGrantedAuthorities(String username);

	/**
	 * @see GrantedAuthoritiesFactory#getGrantedAuthorities(String)
	 */
	Collection<GrantedAuthority> getActiveRoleAuthorities(UUID identityId, IdmRole role);

	/**
	 * @see GrantedAuthoritiesFactory#getGrantedAuthorities(String)
	 */
	Collection<GrantedAuthority> getGrantedAuthoritiesForIdentity(UUID identityId);

	/**
	 * @see GrantedAuthoritiesFactory#getGrantedAuthorities(String)
	 */
	Collection<GrantedAuthority> getGrantedAuthoritiesForValidRoles(UUID identityId, Collection<IdmIdentityRoleDto> roles);
	
	/**
	 * Decides whether the original collection contains all authorities
	 * in the given subset.
	 *  
	 * @param original
	 * @param subset
	 * @return
	 */
	boolean containsAllAuthorities(Collection<GrantedAuthority> original, Collection<GrantedAuthority> subset);
}
