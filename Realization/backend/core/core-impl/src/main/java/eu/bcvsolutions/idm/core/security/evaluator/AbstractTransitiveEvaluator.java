package eu.bcvsolutions.idm.core.security.evaluator;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Transitive permission evaluator
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
	
	@Override
	public Set<String> getPermissions(E entity, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(entity, policy);
		if (entity == null || !securityService.isAuthenticated()) {
			return permissions;
		}
		return authorizationManager.getPermissions(getOwner(entity));
	}
	
	@Override
	public Set<String> getAuthorities(UUID identityId, AuthorizationPolicy policy) {
		// given policy permissions have the highest priority 
		if (!policy.getPermissions().isEmpty()) {
			return super.getAuthorities(identityId, policy);
		}
		//
		// evaluate permissions on IdmIdentity.class (evaluate authorities)
		return authorizationManager.getAuthorities(identityId, getOwnerType());
	}
}
