package eu.bcvsolutions.idm.core.security.evaluator.delegation;


import eu.bcvsolutions.idm.core.model.entity.IdmDelegationDefinition;
import eu.bcvsolutions.idm.core.model.entity.IdmDelegationDefinition_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractAuthorizationEvaluator;
import java.util.Set;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

/**
 * Permissions to self delegation definition by delegator.
 *
 * @author Vít Švanda
 */
@Component
@Description("Permissions to self delegation definition by delegator.")
public class SelfDelegationDefinitionByDelegatorEvaluator extends AbstractAuthorizationEvaluator<IdmDelegationDefinition> {

	@Autowired
	private SecurityService securityService;

	@Override
	public Predicate getPredicate(Root<IdmDelegationDefinition> root, CriteriaQuery<?> query,
								  CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		if (!hasAuthority(securityService.getCurrentId(), policy, permission)) {
			return null;
		}

		return builder.equal(root.get(IdmDelegationDefinition_.delegator).get(IdmIdentity_.id), securityService.getCurrentId());
	}

	@Override
	public Set<String> getPermissions(IdmDelegationDefinition entity, AuthorizationPolicy policy) {

		Set<String> permissions = super.getPermissions(entity, policy);
		if (entity == null || entity.getDelegator() == null || !securityService.isAuthenticated()) {
			return permissions;
		}

		if (securityService.getCurrentId().equals(entity.getDelegator().getId())) {
			permissions.addAll(policy.getPermissions());
		}
		return permissions;
	}

}
