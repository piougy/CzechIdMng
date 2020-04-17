package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;

/**
 * Subordinate contracts criteria builder:
 * - by guarantee and tree structure - finds parent tree node standardly by tree structure
 * 
 * @author Radek Tomiška
 * @since 9.7.0
 */
@Component(DefaultContractByManagerFilter.FILTER_NAME)
public class DefaultContractByManagerFilter extends AbstractFilterBuilder<IdmIdentityContract, IdmIdentityContractFilter> {
	
	public static final String FILTER_NAME = "default-contract-by-manager-filter";
	@Deprecated // @since 10.0.0 - use FILTER_NAME
	public static final String BEAN_NAME = FILTER_NAME;
	//
	@Autowired private ContractByGuaranteeFilter contractByGuaranteeFilter;
	
	@Override
	public String getName() {
		return IdmIdentityContractFilter.PARAMETER_SUBORDINATES_FOR;
	}
	
	@Autowired
	public DefaultContractByManagerFilter(IdmIdentityContractRepository repository) {
		super(repository);
	}

	@Override
	public Predicate getPredicate(
			Root<IdmIdentityContract> root, 
			AbstractQuery<?> query, 
			CriteriaBuilder builder, 
			IdmIdentityContractFilter filter) {
		if (filter.getSubordinatesFor() == null) {
			return null;
		}
		//
		// by guarantee
		Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
		Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
		subquery.select(subRoot);
		//
		List<Predicate> subPredicates = new ArrayList<>();
		if (filter.getSubordinatesByTreeType() == null && filter.isIncludeGuarantees()) {
			// manager as guarantee
			subPredicates.add(contractByGuaranteeFilter.getPredicate(root, query, builder, filter));
		}
		//
		// managers from tree structure
		Subquery<IdmTreeNode> subqueryWp = query.subquery(IdmTreeNode.class);
		Root<IdmIdentityContract> subqueryWpRoot = subqueryWp.from(IdmIdentityContract.class);
		subqueryWp.select(subqueryWpRoot.get(IdmIdentityContract_.workPosition));
		//
		// prevent to generate cross joins by default
		Join<IdmIdentityContract, IdmTreeNode> wp = subqueryWpRoot.join(IdmIdentityContract_.workPosition);
		Join<IdmIdentityContract, IdmTreeNode> wpRoot = subRoot.join(IdmIdentityContract_.workPosition, JoinType.LEFT);
		subqueryWp.where(builder.and(
				// valid contract only
				RepositoryUtils.getValidNowOrInFuturePredicate(subqueryWpRoot, builder),
				builder.equal(subqueryWpRoot.get(IdmIdentityContract_.disabled), Boolean.FALSE),
				//
				(filter.getSubordinatesByTreeType() == null) // only id tree type is specified
					? builder.conjunction() 
					: builder.equal(wp.get(IdmTreeNode_.treeType).get(IdmTreeType_.id), filter.getSubordinatesByTreeType()),
				builder.equal(wp, wpRoot.get(IdmTreeNode_.parent)),
				builder.equal(subqueryWpRoot.get(IdmIdentityContract_.identity).get(IdmIdentity_.id), filter.getSubordinatesFor())
				));
		subPredicates.add(builder.exists(subqueryWp));		
		// 
		subquery.where(
                builder.and(
                		builder.equal(subRoot, root), // correlation attr - identity has identity contract
                		builder.or(subPredicates.toArray(new Predicate[subPredicates.size()]))
                		)
        );
		//		
		return builder.exists(subquery);
	}
}
