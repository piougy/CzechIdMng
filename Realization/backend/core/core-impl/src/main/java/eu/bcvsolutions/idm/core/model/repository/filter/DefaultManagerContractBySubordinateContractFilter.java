package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;

/**
 * Find managers' contracts by subordinate contract.
 * - by guarantee and tree structure - finds parent tree node standardly by tree structure
 * - manager from tree structure - only direct managers are supported now
 * - only "valid" contract can be manager contract
 * - only valid or valid in future contracts can have managers
 * - additional filter parameter - IdmIdentityContractFilter.PARAMETER_VALID_CONTRACT_MANAGERS
 * 
 * @author Radek Tomiška
 * @since 10.4.0
 */
@Component(DefaultManagerContractBySubordinateContractFilter.FILTER_NAME)
@Description("Filter for find managers' contracts for given subordinate contract. "
		+ "Supports manager' contracts by guarantee and tree structure - finds parent tree node standardly by tree structure. "
		+ "Manager from tree structure - only direct managers' contracts are supported now. "
		+ "Only valid contract can be manager.")
public class DefaultManagerContractBySubordinateContractFilter 
		extends AbstractFilterBuilder<IdmIdentityContract, IdmIdentityContractFilter> {
	
	public static final String FILTER_NAME = "default-manager-contract-by-subordinate-contract-filter";
	//
	@Autowired private GuaranteeContractBySubordinateContractFilter guaranteeContractBySubordinateContractFilter;
	
	@Override
	public String getName() {
		return IdmIdentityContractFilter.PARAMETER_MANAGERS_BY_CONTRACT;
	}
	
	@Autowired
	public DefaultManagerContractBySubordinateContractFilter(IdmIdentityContractRepository repository) {
		super(repository);
	}

	@Override
	public Predicate getPredicate(
			Root<IdmIdentityContract> root, 
			AbstractQuery<?> query, 
			CriteriaBuilder builder, 
			IdmIdentityContractFilter filter) {
		
		if (filter.getManagersByContract() == null) {
			return null;
		}
		//
		Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
		Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
		subquery.select(subRoot);
		//
		List<Predicate> subPredicates = new ArrayList<>();
		if (filter.isIncludeGuarantees()) {
			// manager as guarantee
			subPredicates.add(guaranteeContractBySubordinateContractFilter.getPredicate(root, query, builder, filter));
		}		
		// manager from tree structure - only direct managers are supported now
		Subquery<IdmTreeNode> subqueryWp = query.subquery(IdmTreeNode.class);
		Root<IdmIdentityContract> subqueryWpRoot = subqueryWp.from(IdmIdentityContract.class);
		subqueryWp.select(subqueryWpRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.parent));			
		subqueryWp.where(builder.and(
				// future valid contracts
				guaranteeContractBySubordinateContractFilter.getValidNowOrInFuturePredicate(
						subqueryWpRoot, 
						builder, 
						filter.getValidContractManagers()),
				builder.equal(
						subqueryWpRoot.get(IdmIdentityContract_.id), 
						filter.getManagersByContract())
				));
		//
		Path<IdmTreeNode> wp = subRoot.get(IdmIdentityContract_.workPosition);
		subPredicates.add(wp.in(subqueryWp));		
		subquery.where(builder.and(
				//
        		// future valid contract only
				RepositoryUtils.getValidNowOrInFuturePredicate(subRoot, builder),
				//
        		// not disabled, not excluded contract
        		builder.equal(subRoot.get(IdmIdentityContract_.disabled), Boolean.FALSE),
        		builder.or(
        				builder.notEqual(subRoot.get(IdmIdentityContract_.state), ContractState.EXCLUDED),
        				builder.isNull(subRoot.get(IdmIdentityContract_.state))
        		),
				//
        		builder.equal(subRoot, root), // correlation attr
        		builder.or(subPredicates.toArray(new Predicate[subPredicates.size()]))
        		));
		//
		return builder.exists(subquery);
	}
}
