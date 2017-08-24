package eu.bcvsolutions.idm.acc.security.evaluator;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractTransitiveEvaluator;

/**
 * Permissions to identity accounts by identity
 * 
 * @author Radek Tomiška
 */
@Component
@Description("Permissions to identity accounts by identity")
public class IdentityAccountByIdentityEvaluator extends AbstractTransitiveEvaluator<AccIdentityAccount> {

	@Autowired private AuthorizationManager authorizationManager;
	@Autowired private SecurityService securityService;
	
	@Override
	protected Identifiable getOwner(AccIdentityAccount entity) {
		return entity.getIdentity();
	}
	
	@Override
	protected Class<? extends Identifiable> getOwnerType() {
		return IdmIdentity.class;
	}
	
	@Override
	public Predicate getPredicate(Root<AccIdentityAccount> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		if (!hasAuthority(securityService.getCurrentId(), policy, permission)) {
			return null;
		}
		// role subquery
		Subquery<IdmIdentity> subquery = query.subquery(IdmIdentity.class);
		Root<IdmIdentity> subRoot = subquery.from(IdmIdentity.class);
		subquery.select(subRoot);		
		subquery.where(builder.and(
				authorizationManager.getPredicate(subRoot, query, builder, permission),
				builder.equal(root.get(AccIdentityAccount_.identity), subRoot) // correlation attribute
				));
		//
		return builder.exists(subquery);
	}

}

