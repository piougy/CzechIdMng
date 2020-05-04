package eu.bcvsolutions.idm.core.security.evaluator.identity;

import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractAuthorizationEvaluator;

/**
 * Permission to identity by permission to contract.
 * 
 * Lookout: Prevent to combine with {@link IdentityContractByIdentityEvaluator} - configure one of them.
 * {@link IdentityByContractEvaluator} is more flexibile - contracts can be secured by manager (by tree structure or by guarantee).
 * If {@link IdentityRoleByContractEvaluator} is configured, then logged identity can see / edit roles assigned to managed contracts.
 * 
 * @author Radek Tomiška
 * @since 10.3.0
 */
@Component(IdentityByContractEvaluator.EVALUATOR_NAME)
@Description("Permission to identity by permission to contract.")
public class IdentityByContractEvaluator extends AbstractAuthorizationEvaluator<IdmIdentity> {

	public static final String EVALUATOR_NAME = "core-identity-by-contract-evaluator";
	//
	@Autowired private SecurityService securityService;
	@Autowired private AuthorizationManager authorizationManager;
	@Autowired private IdmIdentityContractRepository contractRepository;

	@Override
	public String getName() {
		return EVALUATOR_NAME;
	}
	
	@Override
	public Predicate getPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		if (!securityService.isAuthenticated()) {
			return null;
		}
		//
		Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
		Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
		subquery.select(subRoot);		
		subquery.where(builder.and(
				authorizationManager.getPredicate(subRoot, query, builder, permission),
				builder.equal(root, subRoot.get(IdmIdentityContract_.identity)) // correlation attribute
				));
		//
		return builder.exists(subquery);
	}
	
	@Override
	public Set<String> getPermissions(IdmIdentity entity, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(entity, policy);
		if (entity == null || !securityService.isAuthenticated()) {
			return permissions;
		}
		// TODO: #2002 - change repository method ... 
		for(IdmIdentityContract contract : contractRepository.findAllByIdentity_Id(entity.getId(), null)) {
			permissions.addAll(authorizationManager.getPermissions(contract));
			// little optimization
			if (PermissionUtils.hasPermission(permissions, IdmBasePermission.ADMIN)) {
				break;
			}
		}
		
		return permissions;
	}
	
	@Override
	public boolean supportsPermissions() {
		return false;
	}
}
