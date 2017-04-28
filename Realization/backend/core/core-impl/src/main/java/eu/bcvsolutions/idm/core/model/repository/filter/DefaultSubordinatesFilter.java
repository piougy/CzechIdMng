package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.joda.time.LocalDate;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee;
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
 * TODO: search subordinates and manager recursively by forest index on tree structures
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultSubordinatesFilter 
		extends AbstractFilterBuilder<IdmIdentity, IdentityFilter>
		implements SubordinatesFilter {
	
	public DefaultSubordinatesFilter(IdmIdentityRepository repository) {
		super(repository);
	}

	@Override
	public Predicate getPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdentityFilter filter) {
		Assert.notNull(filter.getSubordinatesFor());
		// identity has to have identity contract
		Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
		Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
		subquery.select(subRoot);
		//
		List<Predicate> subPredicates = new ArrayList<>();
		if (filter.getSubordinatesByTreeType() == null) {
			// manager as guarantee
			Subquery<IdmIdentityContract> subqueryGuarantees = query.subquery(IdmIdentityContract.class);
			Root<IdmContractGuarantee> subRootGuarantees = subqueryGuarantees.from(IdmContractGuarantee.class);
			subqueryGuarantees.select(subRootGuarantees.get(IdmContractGuarantee_.identityContract));
			//
			subqueryGuarantees.where(
	                builder.and(
	                		builder.equal(subRootGuarantees.get(IdmContractGuarantee_.identityContract), subRoot), // correlation attr
	                		builder.equal(subRootGuarantees.get(IdmContractGuarantee_.guarantee), filter.getSubordinatesFor())
	                		)
	        );
			subPredicates.add(builder.exists(subqueryGuarantees));
			//
			// managers from tree structure
			Subquery<IdmTreeNode> subqueryWp = query.subquery(IdmTreeNode.class);
			Root<IdmIdentityContract> subqueryWpRoot = subqueryWp.from(IdmIdentityContract.class);
			subqueryWp.select(subqueryWpRoot.get(IdmIdentityContract_.workPosition));
			subqueryWp.where(builder.and(
					// valid contract only
					builder.or(
							builder.isNull(subqueryWpRoot.get(IdmIdentityContract_.validFrom)),
							builder.lessThanOrEqualTo(subqueryWpRoot.get(IdmIdentityContract_.validFrom), new LocalDate())
							),
					builder.or(
							builder.isNull(subqueryWpRoot.get(IdmIdentityContract_.validTill)),
							builder.greaterThanOrEqualTo(subqueryWpRoot.get(IdmIdentityContract_.validTill), new LocalDate())
							),
					//
					builder.equal(subqueryWpRoot.get(IdmIdentityContract_.workPosition), subRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.parent)),
					builder.equal(subqueryWpRoot.get(IdmIdentityContract_.identity), filter.getSubordinatesFor())
					));
			subPredicates.add(builder.exists(subqueryWp));
		} else {
			//
			// managers from tree structure only
			Subquery<IdmTreeNode> subqueryWp = query.subquery(IdmTreeNode.class);
			Root<IdmIdentityContract> subqueryWpRoot = subqueryWp.from(IdmIdentityContract.class);
			subqueryWp.select(subqueryWpRoot.get(IdmIdentityContract_.workPosition));
			subqueryWp.where(builder.and(
					// valid contract only
					builder.or(
							builder.isNull(subqueryWpRoot.get(IdmIdentityContract_.validFrom)),
							builder.lessThanOrEqualTo(subqueryWpRoot.get(IdmIdentityContract_.validFrom), new LocalDate())
							),
					builder.or(
							builder.isNull(subqueryWpRoot.get(IdmIdentityContract_.validTill)),
							builder.greaterThanOrEqualTo(subqueryWpRoot.get(IdmIdentityContract_.validTill), new LocalDate())
							),
					//
					builder.equal(subqueryWpRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.treeType), filter.getSubordinatesByTreeType()),
					builder.equal(subqueryWpRoot.get(IdmIdentityContract_.workPosition), subRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.parent)),
					builder.equal(subqueryWpRoot.get(IdmIdentityContract_.identity), filter.getSubordinatesFor())
					));
			subPredicates.add(builder.exists(subqueryWp));
		}		
		// 
		subquery.where(
                builder.and(
                		builder.equal(subRoot.get(IdmIdentityContract_.identity), root), // correlation attr - identity has identity contract
                		builder.or(subPredicates.toArray(new Predicate[subPredicates.size()]))
                		)
        );
		//		
		return builder.exists(subquery);
	}
}
