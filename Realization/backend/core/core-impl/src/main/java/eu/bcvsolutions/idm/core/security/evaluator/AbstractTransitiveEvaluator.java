package eu.bcvsolutions.idm.core.security.evaluator;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Transitive permission evaluator. Evaluate authorization by entity's owner. Should be used for relations etc.
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractTransitiveEvaluator<E extends Identifiable> extends AbstractAuthorizationEvaluator<E> {

	/**
	 * Only selected permissions can be used from owner permissions transitively.
	 * Implement new behavior in {@link #getPredicate(javax.persistence.criteria.Root, javax.persistence.criteria.CriteriaQuery, javax.persistence.criteria.CriteriaBuilder, AuthorizationPolicy, eu.bcvsolutions.idm.core.security.api.domain.BasePermission...)} method.
	 * 
	 * @since 10.3.0
	 */
	public static final String PARAMETER_INCLUDE_PERMISSIONS = "include-permissions";
	//
	@Autowired private AuthorizationManager authorizationManager;
	@Autowired private SecurityService securityService;
	
	/**
	 * Returns owning entity for authorization evaluation
	 * 
	 * @param entity
	 * @return
	 */
	protected abstract Identifiable getOwner(E entity);
	
	/**
	 * Returns owning entity type
	 * 
	 * @return
	 */
	protected abstract Class<? extends Identifiable> getOwnerType();
	
	/**
	 * Transitive evaluator doesn't support base permissions from ui
	 * 
	 * @see #getAuthorities(UUID, AuthorizationPolicy)
	 * @see #getPermissions(Identifiable, AuthorizationPolicy)
	 */
	@Override
	public boolean supportsPermissions() {
		return false;
	}
	
	/**
	 * Returns transitive permissions by entity's owner
	 * @param entity
	 * @param policy
	 * @return 
	 */
	@Override
	public Set<String> getPermissions(E entity, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(entity, policy);
		if (entity == null || !securityService.isAuthenticated()) {
			return permissions;
		}
		// evaluates permissions on owner class
		Identifiable owner = getOwner(entity);
		// If is owner null, then now permissions well be granted.
		if (owner == null) {
			return Sets.newHashSet();
		}
		Set<String> transitivePermissions =  authorizationManager.getPermissions(owner);
		// configured permissions - reduce permissions (intersection)
		Set<String> includePermissions = getIncludePermissions(policy);
		if (includePermissions.isEmpty()) {
			return transitivePermissions;
		}
		//
		Set<String> result = transitivePermissions
				.stream()
				.filter(includePermissions::contains)
				.collect(Collectors.toSet());
		return result;
	}
	
	/**
	 * Returns transitive authorities by entity's owner type
	 */
	@Override
	public Set<String> getAuthorities(UUID identityId, AuthorizationPolicy policy) {
		// evaluates authorities on owner type class
		return authorizationManager.getAuthorities(identityId, getOwnerType());
	}
	
	/**
	 * Include selected owner permission only configuration form attribute.
	 * 
	 * @return attribute definition
	 * @since 10.3.0
	 */
	protected IdmFormAttributeDto getIncludePermissionsFormAttribute() {
		IdmFormAttributeDto includePermissions = new IdmFormAttributeDto(
				PARAMETER_INCLUDE_PERMISSIONS, 
				PARAMETER_INCLUDE_PERMISSIONS,
				PersistentType.ENUMERATION,
				BaseFaceType.BASE_PERMISSION_ENUM
		);
		includePermissions.setMultiple(true);
		//
		return includePermissions;
	}
	
	/**
	 * Configured included permissions - only selected permissions can be used from owner permissions transitively.
	 * 
	 * @param policy
	 * @return
	 * @since 10.3.0
	 */
	protected Set<String> getIncludePermissions(AuthorizationPolicy policy) {
		Set<String> permissions = Sets.newHashSet();
		Object includePermissions = policy.getEvaluatorProperties().get(PARAMETER_INCLUDE_PERMISSIONS);
		if (includePermissions == null) {
			return permissions;
		}
		//
		return Arrays
			.stream(includePermissions.toString().split(","))
			.filter(StringUtils::isNotBlank)
			.map(String::trim)
			.map(String::toUpperCase)
			.collect(Collectors.toSet());
	}
}
