package eu.bcvsolutions.idm.core.security.evaluator.role;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeService;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuaranteeRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuaranteeRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee_;
import eu.bcvsolutions.idm.core.security.api.domain.AbstractAuthentication;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractAuthorizationEvaluator;

/**
 * Returns roles, where logged user is in role guarantees (by identity or by role)
 * 
 * TODO: rename evaluator to RoleByGuaranteeEvaluator (change script needed ...)
 * 
 * @author Radek Tomiška
 *
 */
@Component(RoleGuaranteeEvaluator.EVALUATOR_NAME)
@Description("Returns roles, where logged user is in role guarantees (by identity or by role).")
public class RoleGuaranteeEvaluator extends AbstractAuthorizationEvaluator<IdmRole> {
	
	public static final String EVALUATOR_NAME = "core-role-by-guarantee-evaluator";
	//
	@Autowired private SecurityService securityService;
	@Autowired private IdmRoleGuaranteeService roleGuaranteeService;
	@Autowired private IdmRoleGuaranteeRoleService roleGuaranteeRoleService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	
	@Override
	public String getName() {
		return EVALUATOR_NAME;
	}

	@Override
	public Predicate getPredicate(Root<IdmRole> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		AbstractAuthentication authentication = securityService.getAuthentication();
		if (authentication == null || authentication.getCurrentIdentity() == null) {
			return null;
		}
		//
		if (hasPermission(policy, permission)) {
			//
			// by identity
			Subquery<IdmRoleGuarantee> subquery = query.subquery(IdmRoleGuarantee.class);
			Root<IdmRoleGuarantee> subRoot = subquery.from(IdmRoleGuarantee.class);
			subquery.select(subRoot);
			
			subquery.where(
	                builder.and(
	                		builder.equal(subRoot.get(IdmRoleGuarantee_.role), root), // correlation attr
	                		builder.equal(subRoot.get(IdmRoleGuarantee_.guarantee).get(AbstractEntity_.id), authentication.getCurrentIdentity().getId())
	                		)
	        );
			//
			// by role - currently logged identity has a role 
			Subquery<IdmRoleGuaranteeRole> subqueryGuaranteeRole = query.subquery(IdmRoleGuaranteeRole.class);
			Root<IdmRoleGuaranteeRole> subRootGuaranteeRole = subqueryGuaranteeRole.from(IdmRoleGuaranteeRole.class);
			subqueryGuaranteeRole.select(subRootGuaranteeRole);
			//
			// assigned roles
			Subquery<IdmRole> subqueryIdentityRole = query.subquery(IdmRole.class);
			Root<IdmIdentityRole> subrootIdentityRole = subqueryIdentityRole.from(IdmIdentityRole.class);
			subqueryIdentityRole.select(subrootIdentityRole.get(IdmIdentityRole_.role));
			final LocalDate today = LocalDate.now();
			subqueryIdentityRole.where(
					builder.and(
						builder.equal(
								subrootIdentityRole.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.identity).get(IdmIdentity_.id), 
								authentication.getCurrentIdentity().getId()),
						RepositoryUtils.getValidPredicate(subrootIdentityRole, builder, today),
						RepositoryUtils.getValidPredicate(subrootIdentityRole.get(IdmIdentityRole_.identityContract), builder, today),
						builder.equal(subrootIdentityRole.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.disabled), Boolean.FALSE)
						));
			//
			subqueryGuaranteeRole.where(
	                builder.and(
	                		builder.equal(subRootGuaranteeRole.get(IdmRoleGuaranteeRole_.role), root), // correlation attr
	                		subRootGuaranteeRole.get(IdmRoleGuaranteeRole_.guaranteeRole).in(subqueryIdentityRole)
	                		)
	        );
			//
			return builder.or(
					builder.exists(subquery),
					builder.exists(subqueryGuaranteeRole)
					);
		}
		return null;
	}
	
	@Override
	public Set<String> getPermissions(IdmRole entity, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(entity, policy);
		if (entity == null || entity.getId() == null || !securityService.isAuthenticated()) {
			return permissions;
		}
		//
		IdmRoleGuaranteeFilter filter = new IdmRoleGuaranteeFilter();
		filter.setRole(entity.getId());
		filter.setGuarantee(securityService.getCurrentId());
		//
		// by identity
		if (roleGuaranteeService.find(filter, new PageRequest(0, 1)).getTotalElements() > 0) {
			permissions.addAll(policy.getPermissions());
			return permissions;
		}
		//
		// by role
		IdmRoleGuaranteeRoleFilter filterRole = new IdmRoleGuaranteeRoleFilter();
		filterRole.setRole(entity.getId());
		Set<UUID> guaranteeRoles = roleGuaranteeRoleService
				.find(filterRole, null)
				.getContent()
				.stream()
				.map(rg -> rg.getGuaranteeRole())
				.collect(Collectors.toSet());
		// identity roles
		// TODO: create some subquery ... 
		if (identityRoleService
			.findValidRoles(securityService.getCurrentId(), null)
			.getContent()
			.stream()
			.filter(ir -> guaranteeRoles.contains(ir.getRole()))
			.findFirst()
			.orElse(null) != null) {
			permissions.addAll(policy.getPermissions());
		}
		//
		return permissions;
	}
}
