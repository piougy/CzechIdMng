package eu.bcvsolutions.idm.core.security.evaluator;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
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
	 * Transitive evaluator doesn't support base perrmissions from ui
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
	 */
	@Override
	public Set<String> getPermissions(E entity, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(entity, policy);
		if (entity == null || !securityService.isAuthenticated()) {
			return permissions;
		}
		// evaluates permissions on IdmIdentity.class
		return authorizationManager.getPermissions(getOwner(entity));
	}
	
	/**
	 * Returns transitive authorities by entity's owner type
	 */
	@Override
	public Set<String> getAuthorities(UUID identityId, AuthorizationPolicy policy) {
		// evaluates authorities on owner type class
		return authorizationManager.getAuthorities(identityId, getOwnerType());
	}
}
