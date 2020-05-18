package eu.bcvsolutions.idm.core.security.evaluator.identity;

import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractTransitiveEvaluator;

/**
 * Permissions to contracts by identity.
 * 
 * Lookout: Prevent to combine with {@link IdentityByContractEvaluator} - configure one of them.
 * {@link IdentityByContractEvaluator} is more flexibile - contracts can be secured by manager (by tree structure or by guarantee).
 * If {@link IdentityRoleByContractEvaluator} is configured, then logged identity can see / edit roles assigned to managed contracts.
 * 
 * @author Radek Tomiška
 */
@Component(IdentityContractByIdentityEvaluator.EVALUATOR_NAME)
@Description("Permissions to contracts by identity")
public class IdentityContractByIdentityEvaluator extends AbstractTransitiveEvaluator<IdmIdentityContract> {

	public static final String EVALUATOR_NAME = "core-contract-by-identity-evaluator";
	//
	@Autowired private AuthorizationManager authorizationManager;
	@Autowired private SecurityService securityService;
	
	@Override
	public String getName() {
		return EVALUATOR_NAME;
	}
	
	@Override
	protected Identifiable getOwner(IdmIdentityContract entity) {
		if (entity.getIdentity() == null) {
			// Contract can be created before identity is saved => created permission need to be evaluated properly.
			return new IdmIdentity();
		}
		return entity.getIdentity();
	}
	
	@Override
	protected Class<? extends Identifiable> getOwnerType() {
		return IdmIdentity.class;
	}
	
	@Override
	public Predicate getPredicate(Root<IdmIdentityContract> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		if (!hasAuthority(securityService.getCurrentId(), policy, permission)) {
			return null;
		}
		// Configured permissions will be used for owner permissions intersection => only selected permissions will be granted by owner.
		Set<String> includePermissions = getIncludePermissions(policy);
		if (!includePermissions.isEmpty() && !PermissionUtils.hasPermission(includePermissions, permission)) {
			return null;
		}
		// identity subquery
		Subquery<IdmIdentity> subquery = query.subquery(IdmIdentity.class);
		Root<IdmIdentity> subRoot = subquery.from(IdmIdentity.class);
		subquery.select(subRoot);		
		subquery.where(builder.and(
				authorizationManager.getPredicate(subRoot, query, builder, permission),
				builder.equal(root.get(IdmIdentityContract_.identity), subRoot) // correlation attribute
				));
		//
		return builder.exists(subquery);
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_INCLUDE_PERMISSIONS);
		//
		return parameters;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		return Lists.newArrayList(getIncludePermissionsFormAttribute());
	}
}
