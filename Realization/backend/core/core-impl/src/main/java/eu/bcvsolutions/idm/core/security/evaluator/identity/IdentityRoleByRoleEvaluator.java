package eu.bcvsolutions.idm.core.security.evaluator.identity;

import java.util.HashSet;
import java.util.List;
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

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.RoleBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractTransitiveEvaluator;

/**
 * Permissions to assigned roles by roles (usable e.g. with can be requested permission).
 * 
 * @author Radek Tomiška
 * @since 9.7.12
 *
 */
@Component
@Description("Permissions to assigned roles by roles (usable e.g. with can be requested permission).")
public class IdentityRoleByRoleEvaluator extends AbstractTransitiveEvaluator<IdmIdentityRole> {

	public static final String EVALUATOR_NAME = "core-identity-role-by-role-evaluator";
	public static final String PARAMETER_CAN_BE_REQUESTED_ONLY = "can-be-requested-only"; // get can be requested permission only
	//
	@Autowired private AuthorizationManager authorizationManager;
	@Autowired private SecurityService securityService;
	
	@Override
	public String getName() {
		return EVALUATOR_NAME;
	}
	
	@Override
	protected Identifiable getOwner(IdmIdentityRole entity) {
		return entity.getRole();
	}
	
	@Override
	protected Class<? extends Identifiable> getOwnerType() {
		return IdmRole.class;
	}
	
	@Override
	public Predicate getPredicate(Root<IdmIdentityRole> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		if (!hasAuthority(securityService.getCurrentId(), policy, permission)) {
			return null;
		}
		// identity subquery
		Subquery<IdmRole> subquery = query.subquery(IdmRole.class);
		Root<IdmRole> subRoot = subquery.from(IdmRole.class);
		subquery.select(subRoot);		
		subquery.where(builder.and(
				authorizationManager.getPredicate(subRoot, query, builder, permission),
				builder.equal(root.get(IdmIdentityRole_.role), subRoot)
				));
		//
		return builder.exists(subquery);
	}
	
	@Override
	public Set<String> getPermissions(IdmIdentityRole entity, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(entity, policy);
		if (!isCanBeRequestedOnly(policy)) {
			return super.getPermissions(entity, policy);
		}
		// return can be requested permission only
		Set<String> result = new HashSet<>();
		if (PermissionUtils.hasPermission(permissions, RoleBasePermission.CANBEREQUESTED)) {
			result.add(RoleBasePermission.CANBEREQUESTED.getName());
		}
		return result;
	}
	
	@Override
	public Set<String> getAuthorities(UUID identityId, AuthorizationPolicy policy) {
		Set<String> authorities = super.getAuthorities(identityId, policy);
		//
		if (!isCanBeRequestedOnly(policy)) {
			return authorities;
		}
		// return can be requested authorities only
		Set<String> result = new HashSet<>();
		if (PermissionUtils.hasPermission(authorities, RoleBasePermission.CANBEREQUESTED)) {
			result.add(RoleBasePermission.CANBEREQUESTED.getName());
		}
		return result;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_CAN_BE_REQUESTED_ONLY);
		return parameters;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto canBeRequestedOnly = new IdmFormAttributeDto(PARAMETER_CAN_BE_REQUESTED_ONLY, PARAMETER_CAN_BE_REQUESTED_ONLY, PersistentType.BOOLEAN);
		canBeRequestedOnly.setDefaultValue(Boolean.TRUE.toString());
		//
		return Lists.newArrayList(canBeRequestedOnly);
	}
	
	private boolean isCanBeRequestedOnly(AuthorizationPolicy policy) {
		return policy.getEvaluatorProperties().getBoolean(PARAMETER_CAN_BE_REQUESTED_ONLY);
	}
}
