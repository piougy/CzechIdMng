package eu.bcvsolutions.idm.core.model.repository.filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;

/**
 * Subordinates criteria builder.
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class DefaultManagersByContractFilter 
		extends AbstractFilterBuilder<IdmIdentity, IdentityFilter> 
		implements ManagersByContractFilter {
	
	@Autowired
	public DefaultManagersByContractFilter(IdmIdentityRepository repository) {
		super(repository);
	}

	@Override
	public Predicate getPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdentityFilter filter) {
		if (filter.getManagersByContractId() == null) {
			return null;
		}
		//
		Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
		Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
		subquery.select(subRoot);
		// by tree structure
		Subquery<IdmTreeNode> subqueryWp = query.subquery(IdmTreeNode.class);
		Root<IdmIdentityContract> subqueryWpRoot = subqueryWp.from(IdmIdentityContract.class);
		subqueryWp.select(subqueryWpRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.parent));
		subqueryWp.where(
				builder.equal(subqueryWpRoot.get(IdmIdentityContract_.id), filter.getManagersByContractId())
				);
		//
		subquery.where(
                builder.and(
                		// valid contract only
    					builder.or(
    							builder.isNull(subRoot.get(IdmIdentityContract_.validFrom)),
    							builder.lessThanOrEqualTo(subRoot.get(IdmIdentityContract_.validFrom), new LocalDate())
    							),
    					builder.or(
    							builder.isNull(subRoot.get(IdmIdentityContract_.validTill)),
    							builder.greaterThanOrEqualTo(subRoot.get(IdmIdentityContract_.validTill), new LocalDate())
    							),
    					//
                		builder.equal(subRoot.get(IdmIdentityContract_.identity), root), // correlation attr
                		subRoot.get(IdmIdentityContract_.workPosition).in(subqueryWp))
        );
		if (!filter.isIncludeGuarantees()) {
			return builder.exists(subquery);
		} else {
			// by identity contract guarantees
			Subquery<IdmIdentity> subqueryGuarantee = query.subquery(IdmIdentity.class);
			Root<IdmIdentityContract> subqueryGuaranteeRoot = subqueryGuarantee.from(IdmIdentityContract.class);
			subqueryGuarantee.select(subqueryGuaranteeRoot.join(IdmIdentityContract_.guarantees).get(IdmContractGuarantee_.guarantee));
			subqueryGuarantee.where(builder.and(
					builder.equal(subqueryGuaranteeRoot.get(IdmIdentityContract_.id), filter.getManagersByContractId())
					));
			
			return builder.or(
					builder.exists(subquery),
					root.in(subqueryGuarantee)
					);
		}
	}
}
