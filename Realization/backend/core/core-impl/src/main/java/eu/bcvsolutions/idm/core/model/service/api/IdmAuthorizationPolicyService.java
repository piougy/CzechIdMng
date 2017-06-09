package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.filter.AuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Assign authorization evaluator to role.
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmAuthorizationPolicyService
		extends ReadWriteDtoService<IdmAuthorizationPolicyDto, AuthorizationPolicyFilter>,
		AuthorizableService<IdmAuthorizationPolicyDto> {
	
	/**
	 * Returns all enabled policies for given identity and entity type
	 * 
	 * @param identityId identity's id
	 * @param entityType
	 * @return
	 */
	List<IdmAuthorizationPolicyDto> getEnabledPolicies(UUID identityId, Class<? extends Identifiable> entityType);
	
	/**
	 * Returns active role's authorities by configured policies for given identity
	 * 
	 * @param identityId
	 * @param role
	 */
	Set<GrantedAuthority> getEnabledRoleAuthorities(UUID identityId, UUID roleId);
	
	/**
	 * Returns role policies
	 * 
	 * @param roleId
	 * @param disabled
	 * @return
	 */
	List<IdmAuthorizationPolicyDto> getRolePolicies(UUID roleId, boolean disabled);
	
	/**
	 * Returns active and persisted role's authorities by configured policies for given identity.
	 * Persisted means persisted policies outside current transaction.
	 * 
	 * @param role
	 */
	Set<GrantedAuthority> getEnabledPersistedRoleAuthorities(UUID identityId, UUID roleId);
	
	/**
	 * Returns authorities from default user role by configuration {@value #PROPERTY_DEFAULT_ROLE} for given identity.
	 * 
	 * Attention: Doesn't returns authorities from subroles
	 * 
	 * @return
	 */
	Set<GrantedAuthority> getDefaultAuthorities(UUID identityId);
	
	/**
	 * Returns policies from default user role by configuration {@value IdmRoleService#PROPERTY_DEFAULT_ROLE}.
	 * 
	 * Attention: Doesn't returns policies from subroles
	 * 
	 * @return
	 */
	List<IdmAuthorizationPolicyDto> getDefaultPolicies(Class<? extends Identifiable> entityType);

	/**
	 * Returns a set of granted authorities from enabled authorization policies for given identity.
	 * 
	 * @param identityId
	 * @param policies
	 * @return
	 */
	Set<GrantedAuthority> getGrantedAuthorities(UUID identityId, List<IdmAuthorizationPolicyDto> policies);
	
	
}
