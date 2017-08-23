package eu.bcvsolutions.idm.core.security.evaluator.role;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleRequest;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractTransitiveEvaluator;

/**
 * Permissions to concept role requests by role request
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Permissions to concept role requests by role request")
public class ConceptRoleRequestByRoleRequestEvaluator extends AbstractTransitiveEvaluator<IdmConceptRoleRequest> {

	@Autowired private AuthorizationManager authorizationManager;
	@Autowired private SecurityService securityService;
	
	@Override
	protected Identifiable getOwner(IdmConceptRoleRequest entity) {
		return entity.getRoleRequest();
	}
	
	@Override
	protected Class<? extends Identifiable> getOwnerType() {
		return IdmRoleRequest.class;
	}
	
	@Override
	public Predicate getPredicate(Root<IdmConceptRoleRequest> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		if (!hasAuthority(securityService.getCurrentId(), policy, permission)) {
			return null;
		}
		// role request subquery
		Subquery<IdmRoleRequest> subquery = query.subquery(IdmRoleRequest.class);
		Root<IdmRoleRequest> subRoot = subquery.from(IdmRoleRequest.class);
		subquery.select(subRoot);		
		subquery.where(builder.and(
				authorizationManager.getPredicate(subRoot, query, builder, permission),
				builder.equal(root.get(IdmConceptRoleRequest_.roleRequest), subRoot) // correlation attribute
				));
		//
		return builder.exists(subquery);
	}
}
