package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Assign authorization evaluator to role.
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmAuthorizationPolicyService extends
		EventableDtoService<IdmAuthorizationPolicyDto,
		IdmAuthorizationPolicyFilter>,
		AuthorizableService<IdmAuthorizationPolicyDto> {
	
	/**
	 * Returns all enabled policies for given identity and entity type.
	 * 
	 * @param identityId identity's id
	 * @param entityType
	 * @return
	 */
	List<IdmAuthorizationPolicyDto> getEnabledPolicies(UUID identityId, Class<? extends Identifiable> entityType);
	
	/**
	 * Returns active role's authorities by configured policies for given identity.
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
	 * Returns authorities from default user role by configuration {@value #PROPERTY_DEFAULT_ROLE} for given identity.
	 * Sub roles are supported @since 10.5.0.
	 * Authorities are loaded on login only.
	 * 
	 * @param identityId logged identity
	 * @return default role authorities.
	 * @see RoleConfiguration#getDefaultRole()
	 */
	Set<GrantedAuthority> getDefaultAuthorities(UUID identityId);
	
	/**
	 * Returns policies from default user role by configuration {@value IdmRoleService#PROPERTY_DEFAULT_ROLE}.
	 * Sub roles are supported @since 10.5.0.
	 * 
	 * @param entityType policies by given entity type (~authorizable type)
	 * @return
	 * @see RoleConfiguration#getDefaultRole()
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
	
	/**
	 * Policy is configured the same way by configured properties (~ equals on properties configurable and related to evaluating access).
	 * 
	 * @param policyOne first - not null
	 * @param policyTwo second - not null
	 * @return true - policies are configured the same way
	 * @since 10.7.0
	 */
	default boolean hasSameConfiguration(IdmAuthorizationPolicyDto policyOne, IdmAuthorizationPolicyDto policyTwo) {		
		Assert.notNull(policyOne, "Policy one is required.");
		Assert.notNull(policyTwo, "Policy two is required.");
		//
		return Objects.equals(policyOne.getAuthorizableType(), policyTwo.getAuthorizableType())
				&& Objects.equals(policyOne.getEvaluatorType(), policyTwo.getEvaluatorType())
				&& Objects.equals(policyOne.getGroupPermission(), policyTwo.getGroupPermission())
				&& Objects.equals(policyOne.getPermissions(), policyTwo.getPermissions())
				&& Objects.equals(policyOne.getEvaluatorProperties(), policyTwo.getEvaluatorProperties())
				&& Objects.equals(policyOne.getSeq(), policyTwo.getSeq())
				&& Objects.equals(policyOne.isDisabled(), policyTwo.isDisabled())
				&& Objects.equals(policyOne.getDescription(), policyTwo.getDescription());
	}
}
