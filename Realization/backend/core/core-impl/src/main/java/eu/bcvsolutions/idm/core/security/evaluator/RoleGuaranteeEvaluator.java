package eu.bcvsolutions.idm.core.security.evaluator;

import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.security.api.domain.AbstractAuthentication;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Returns roles, where logged user is in role guarantees
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Role guarantee will have selected permissions.")
public class RoleGuaranteeEvaluator extends AbstractAuthorizationEvaluator<IdmRole> {
	
	private final SecurityService securityService;
	
	@Autowired
	public RoleGuaranteeEvaluator(SecurityService securityService) {
		Assert.notNull(securityService);
		//
		this.securityService = securityService;
	}

	@Override
	public Predicate getPredicate(Root<IdmRole> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		AbstractAuthentication authentication = securityService.getAuthentication();
		if (authentication == null || authentication.getCurrentIdentity() == null) {
			return null;
		}
		//
		if (hasPermission(policy, permission)) {
			Subquery<IdmRoleGuarantee> subquery = query.subquery(IdmRoleGuarantee.class);
			Root<IdmRoleGuarantee> subRoot = subquery.from(IdmRoleGuarantee.class);
			subquery.select(subRoot);
			
			subquery.where(
	                builder.and(
	                		builder.equal(subRoot.get("role"), root), // correlation attr
	                		builder.equal(subRoot.get("guarantee").get("id"), authentication.getCurrentIdentity().getId())
	                		)
	        );	
			return builder.exists(subquery);
		}
		return null;
	}
	
	@Override
	public Set<String> getPermissions(IdmRole entity, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(entity, policy);
		if (entity == null) {
			return permissions;
		}
		for (IdmRoleGuarantee guarantee : entity.getGuarantees()) {
			if (guarantee.getGuarantee().getId().equals(securityService.getAuthentication().getCurrentIdentity().getId())) {
				permissions.addAll(policy.getPermissions());
				break;
			}
		}
		return permissions;
	}
}
