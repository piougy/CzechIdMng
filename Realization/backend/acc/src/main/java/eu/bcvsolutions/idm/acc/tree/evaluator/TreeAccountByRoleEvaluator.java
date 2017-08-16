package eu.bcvsolutions.idm.acc.tree.evaluator;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.entity.AccTreeAccount;
import eu.bcvsolutions.idm.acc.entity.AccTreeAccount_;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractTransitiveEvaluator;

/**
 * Permissions to role accounts
 *
 * @author Kučera
 *
 */
public class TreeAccountByRoleEvaluator extends AbstractTransitiveEvaluator<AccTreeAccount> {

	@Autowired
	private AuthorizationManager authorizationManager;
	@Autowired
	private SecurityService securityService;

	@Override
	protected Identifiable getOwner(AccTreeAccount entity) {
		return entity.getTreeNode();
	}

	@Override
	protected Class<? extends Identifiable> getOwnerType() {
		return IdmTreeNode.class;
	}

	@Override
	public Predicate getPredicate(Root<AccTreeAccount> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		if (!hasAuthority(securityService.getCurrentId(), policy, permission)) {
			return null;
		}
		// tree node subquery
		Subquery<IdmTreeNode> subquery = query.subquery(IdmTreeNode.class);
		Root<IdmTreeNode> subRoot = subquery.from(IdmTreeNode.class);
		subquery.select(subRoot);
		subquery.where(builder.and(
				authorizationManager.getPredicate(subRoot, query, builder, permission),
				builder.equal(root.get(AccTreeAccount_.treeNode), subRoot) // correlation attribute
		));
		//
		return builder.exists(subquery);

	}
}
