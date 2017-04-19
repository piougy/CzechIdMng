package eu.bcvsolutions.idm.core.security.evaluator.identity;

import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.service.api.SubordinatesCriteriaBuilder;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractAuthorizationEvaluator;

/**
 * Permissions to subordinates
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Permissions to subordinates.")
public class SubordinatesEvaluator extends AbstractAuthorizationEvaluator<IdmIdentity> {
	
	@Autowired
	private SecurityService securityService;
	@Autowired
	private SubordinatesCriteriaBuilder subordinatesCriteriaBuilder;

	@Override
	public Predicate getPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		if (!hasPermission(policy, permission)) {
			return null;
		}
		if (!securityService.isAuthenticated()) {
			return null;
		}		
		return subordinatesCriteriaBuilder.getSubordinatesPredicate(root, query, builder, securityService.getUsername(), null);
	}
	
	@Override
	public Set<String> getPermissions(IdmIdentity entity, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(entity, policy);
		if (entity == null || !securityService.isAuthenticated()) {
			return permissions;
		}
		// TODO: could be added to predicate directly
		boolean isManager = subordinatesCriteriaBuilder.getManagers(entity.getUsername(), null, null).getContent()
				.stream()
				.anyMatch(identity -> {
			return identity.getUsername().equals(securityService.getUsername());
		});
		if (isManager) {
			permissions.addAll(policy.getPermissions());
		}
		return permissions;
	}
}
