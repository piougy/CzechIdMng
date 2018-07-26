package eu.bcvsolutions.idm.core.security.evaluator.profile;

import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmProfile;
import eu.bcvsolutions.idm.core.model.entity.IdmProfile_;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractAuthorizationEvaluator;

/**
 * Currently logged user - self profile permissions.
 * 
 * @author Radek Tomiška
 *
 */
@Component(SelfProfileEvaluator.EVALUATOR_NAME)
@Description("Currently logged user - self profile permissions.")
public class SelfProfileEvaluator extends AbstractAuthorizationEvaluator<IdmProfile> {

	public static final String EVALUATOR_NAME = "core-self-profile-evaluator";
	private final SecurityService securityService;
	
	@Autowired
	public SelfProfileEvaluator(SecurityService securityService) {
		Assert.notNull(securityService);
		//
		this.securityService = securityService;
	}
	
	@Override
	public String getName() {
		return EVALUATOR_NAME;
	}
	
	@Override
	public Predicate getPredicate(Root<IdmProfile> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		if (!hasPermission(policy, permission)) {
			return null;
		}
		if (!securityService.isAuthenticated()) {
			return null;
		}
		return builder.equal(root.get(IdmProfile_.identity).get(IdmIdentity_.id), securityService.getCurrentId());
	}
	
	@Override
	public Set<String> getPermissions(IdmProfile entity, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(entity, policy);
		if (entity == null || entity.getIdentity() == null || !securityService.isAuthenticated()) {
			return permissions;
		}
		if (securityService.getCurrentId().equals(entity.getIdentity().getId())) {
			permissions.addAll(policy.getPermissions());
		}
		return permissions;
	}
}
