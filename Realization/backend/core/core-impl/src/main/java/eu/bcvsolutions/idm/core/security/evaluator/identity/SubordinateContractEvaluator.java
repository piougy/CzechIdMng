package eu.bcvsolutions.idm.core.security.evaluator.identity;

import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractAuthorizationEvaluator;

/**
 * Permissions to subordinate contracts.
 * 
 * Lookout: Prevent to combine with {@link SubordinatesEvaluator} - configure one of them.
 * {@link SubordinateContractEvaluator} is more flexibile - contracts can be secured by manager (by tree structure or by guarantee).
 * If {@link IdentityRoleByContractEvaluator} is configured, then logged identity can see / edit roles assigned to managed contracts.
 * 
 * @author Radek Tomiška
 * @since 10.3.0
 */
@Component(SubordinateContractEvaluator.EVALUATOR_NAME)
@Description("Permissions to subordinate contracts.")
public class SubordinateContractEvaluator extends AbstractAuthorizationEvaluator<IdmIdentityContract> {
	
	public static final String EVALUATOR_NAME = "core-subordinate-contract-evaluator";
	//
	@Autowired private SecurityService securityService;
	@Autowired private FilterManager filterManager;
	@Autowired private IdmIdentityService identityService;
	
	@Override
	public String getName() {
		return EVALUATOR_NAME;
	}

	@Override
	public Predicate getPredicate(Root<IdmIdentityContract> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		if (!hasPermission(policy, permission)) {
			return null;
		}
		if (!securityService.isAuthenticated()) {
			return null;
		}
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setSubordinatesFor(securityService.getAuthentication().getCurrentIdentity().getId());
		//
		return filterManager
				.getBuilder(IdmIdentityContract.class, IdmIdentityContractFilter.PARAMETER_SUBORDINATES_FOR)
				.getPredicate(root, query, builder, filter);
	}
	
	@Override
	public Set<String> getPermissions(IdmIdentityContract entity, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(entity, policy);
		if (entity == null || !securityService.isAuthenticated()) {
			return permissions;
		}
		IdmIdentity identity = entity.getIdentity();
		if (identity == null) {
			// new contract is saved together with idenitity - identity is not created now.
			return permissions;
		}
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setManagersFor(identity.getId()); // required - filter is registered to this property
		filter.setManagersByContract(entity.getId());
		filter.setUsername(securityService.getUsername());
		boolean isManager = identityService
				.findIds(filter, PageRequest.of(0, 1))
				.getTotalElements() > 0;
		if (isManager) {
			permissions.addAll(policy.getPermissions());
		}
		return permissions;
	}
}
