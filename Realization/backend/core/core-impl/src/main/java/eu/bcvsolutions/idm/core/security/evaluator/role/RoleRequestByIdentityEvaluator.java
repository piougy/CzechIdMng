package eu.bcvsolutions.idm.core.security.evaluator.role;

import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleRequest_;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractTransitiveEvaluator;

/**
 * Permissions to role requests by identity. 
 * {@link IdentityBasePermission#CHANGEPERMISSION} is evaluated on transitive identity.
 * 
 * @see {@link IdentityBasePermission}
 * @author Radek Tomiška
 *
 */
@Component
@Description("Permissions to role requests by identity")
public class RoleRequestByIdentityEvaluator extends AbstractTransitiveEvaluator<IdmRoleRequest> {

	@Autowired private AuthorizationManager authorizationManager;
	@Autowired private SecurityService securityService;
	
	@Override
	protected Identifiable getOwner(IdmRoleRequest entity) {
		return entity.getApplicant();
	}
	
	@Override
	protected Class<? extends Identifiable> getOwnerType() {
		return IdmIdentity.class;
	}
	
	@Override
	public Predicate getPredicate(Root<IdmRoleRequest> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		if (!hasAuthority(securityService.getCurrentId(), policy, permission)) {
			return null;
		}
		// identity subquery
		Subquery<IdmIdentity> subquery = query.subquery(IdmIdentity.class);
		Root<IdmIdentity> subRoot = subquery.from(IdmIdentity.class);
		subquery.select(subRoot);		
		subquery.where(builder.and(
				authorizationManager.getPredicate(subRoot, query, builder, permission),
				builder.equal(root.get(IdmRoleRequest_.applicant), subRoot) // correlation attribute
				));
		//
		return builder.exists(subquery);
	}
	
	@Override
	public Set<String> getPermissions(IdmRoleRequest entity, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(entity, policy);
		// add permissions, when CHANGEPERMISSION is available
		if (PermissionUtils.hasPermission(permissions, IdentityBasePermission.CHANGEPERMISSION)) {
			permissions.add(IdmBasePermission.READ.getName());
			permissions.add(IdmBasePermission.CREATE.getName());
			permissions.add(IdmBasePermission.UPDATE.getName());
			permissions.add(IdmBasePermission.DELETE.getName());
		}
		return permissions;
	}
	
	@Override
	public Set<String> getAuthorities(UUID identityId, AuthorizationPolicy policy) {
		Set<String> authorities = super.getAuthorities(identityId, policy);
		// add permissions, when CHANGEPERMISSION is available
		if (PermissionUtils.hasPermission(authorities, IdentityBasePermission.CHANGEPERMISSION)) {
			authorities.add(IdmBasePermission.READ.getName());
			authorities.add(IdmBasePermission.CREATE.getName());
			authorities.add(IdmBasePermission.UPDATE.getName());
			authorities.add(IdmBasePermission.DELETE.getName());
		}
		return authorities;
	}
}
