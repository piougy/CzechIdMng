package eu.bcvsolutions.idm.core.security.evaluator.identity;

import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.filter.ManagersFilter;
import eu.bcvsolutions.idm.core.model.repository.filter.SubordinatesFilter;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
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
	private IdmIdentityService identityService; // TODO: Identity to dto
	@Autowired
	private SubordinatesFilter subordinatesFilter;
	@Autowired
	private ManagersFilter managersFilter;

	@Override
	public Predicate getPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		if (!hasPermission(policy, permission)) {
			return null;
		}
		if (!securityService.isAuthenticated()) {
			return null;
		}
		IdentityFilter filter = new IdentityFilter();
		filter.setSubordinatesFor(identityService.getByUsername(securityService.getUsername()));
		return subordinatesFilter.getPredicate(root, query, builder, filter);
	}
	
	@Override
	public Set<String> getPermissions(IdmIdentity entity, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(entity, policy);
		if (entity == null || !securityService.isAuthenticated()) {
			return permissions;
		}
		IdentityFilter filter = new IdentityFilter();
		filter.setManagersFor(identityService.getByUsername(entity.getUsername()));
		boolean isManager = managersFilter.find(filter, null).getContent()
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
