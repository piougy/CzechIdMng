package eu.bcvsolutions.idm.core.security.evaluator;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Returns roles, where logged user is in role guarantees
 * 
 * TODO: logged user as input
 * TODO: integration tests
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class RoleGuaranteeEvaluator extends AbstractAuthorizationEvaluator<IdmRole> {
	
	@Autowired
	private SecurityService securityService;

	/**
	 * Read predicate
	 * 
	 * @param root
	 * @param query
	 * @param builder
	 * @return
	 */
	@Override
	public Predicate getPredicate(Root<IdmRole> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		Subquery<IdmRoleGuarantee> subquery = query.subquery(IdmRoleGuarantee.class);
		Root<IdmRoleGuarantee> subRoot = subquery.from(IdmRoleGuarantee.class);
		subquery.select(subRoot);
		
		subquery.where(
                builder.and(
                		builder.equal(subRoot.get("role"), root), // correlation attr
                		builder.equal(subRoot.get("guarantee").get("id"), securityService.getAuthentication().getCurrentIdentity().getId())
                		)
        );	
		return builder.exists(subquery);
	}
}
