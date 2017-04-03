package eu.bcvsolutions.idm.core.security.evaluator;

import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Adds base permissions by policy configuration
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Simple permission evaluator - evaluates selected permissions on selected entity type")
public class BasePermissionEvaluator extends AbstractAuthorizationEvaluator<BaseEntity> {
	
	@Override
	public Predicate getPredicate(AuthorizationPolicy policy, BasePermission permission, Root<BaseEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		if (hasPermission(policy, permission)) {
			return builder.conjunction();
		}
		return null;
	}
	
	@Override
	public Set<String> getPermissions(AuthorizationPolicy policy, BaseEntity entity) {
		final Set<String> permissions = super.getPermissions(policy, entity);
		permissions.addAll(getBasePermissions(policy));
		return permissions;
	}
}
