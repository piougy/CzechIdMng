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

import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
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
	
	@Autowired
	private SecurityService securityService;

	@Override
	public Predicate getPredicate(AuthorizationPolicy policy, Root<IdmRole> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		if (canSearch(policy)) {
			Subquery<IdmRoleGuarantee> subquery = query.subquery(IdmRoleGuarantee.class);
			Root<IdmRoleGuarantee> subRoot = subquery.from(IdmRoleGuarantee.class);
			subquery.select(subRoot);
			
			subquery.where(
	                builder.and(
	                		builder.equal(subRoot.get("role"), root), // correlation attr
	                		builder.equal(subRoot.get("guarantee").get("id"), securityService.getAuthentication().getCurrentIdentity().getId())
	                		)
	        );	
			return builder.exists(subquery);
		}
		return null;
	}
	
	@Override
	public Set<String> evaluate(AuthorizationPolicy policy, IdmRole entity) {
		Set<String> permissions = super.evaluate(policy, entity);
		if (entity == null) {
			return permissions;
		}
		for (IdmRoleGuarantee guarantee : entity.getGuarantees()) {
			if (guarantee.getGuarantee().getId().equals(securityService.getAuthentication().getCurrentIdentity().getId())) {
				permissions.addAll(getBasePermissions(policy));
				break;
			}
		}
		return permissions;
	}
}
