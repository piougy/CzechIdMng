package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;

/**
 * Managers criteria builder:
 * - by guarantee and tree structure - finds parent tree node standardly by tree structure
 * - manager from tree structure - only direct managers are supported now
 * - only "valid" identity can be manager
 * 
 * @author Radek Tomiška
 *
 */
@Component("defaultManagersFilter")
public class DefaultManagersFilter 
		extends AbstractFilterBuilder<IdmIdentity, IdmIdentityFilter> {
	
	@Autowired private GuaranteeManagersFilter guaranteeManagersFilter;
	
	@Override
	public String getName() {
		return IdmIdentityFilter.PARAMETER_MANAGERS_FOR;
	}
	
	@Autowired
	public DefaultManagersFilter(IdmIdentityRepository repository) {
		super(repository);
	}

	@Override
	public Predicate getPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmIdentityFilter filter) {
		if (filter.getManagersFor() == null) {
			return null;
		}
		//
		Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
		Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
		subquery.select(subRoot);
		//
		List<Predicate> subPredicates = new ArrayList<>();
		if (filter.getManagersByTreeType() == null && filter.isIncludeGuarantees()) {
			// manager as guarantee
			subPredicates.add(guaranteeManagersFilter.getGuaranteesPredicate(root, query, builder, filter));
		}		
		// manager from tree structure - only direct managers are supported now
		Subquery<IdmTreeNode> subqueryWp = query.subquery(IdmTreeNode.class);
		Root<IdmIdentityContract> subqueryWpRoot = subqueryWp.from(IdmIdentityContract.class);
		subqueryWp.select(subqueryWpRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.parent));			
		subqueryWp.where(builder.and(
				builder.equal(
						subqueryWpRoot.get(IdmIdentityContract_.identity).get(IdmIdentity_.id), 
						filter.getManagersFor()),
				filter.getManagersByContract() != null // concrete contract id only
	    			? builder.equal(subqueryWpRoot.get(IdmIdentityContract_.id), filter.getManagersByContract())
	    			: builder.conjunction()
				));
		//
		Path<IdmTreeNode> wp = subRoot.get(IdmIdentityContract_.workPosition);
		subPredicates.add(
                builder.and(
                		wp.in(subqueryWp),
                		// by tree type structure
                		filter.getManagersByTreeType() != null
                			?
                    		builder.equal(
                    				wp.get(IdmTreeNode_.treeType).get(IdmTreeType_.id), 
        							filter.getManagersByTreeType())
                    		:
                    		builder.conjunction()
                		)
        );		
		subquery.where(builder.and(
				//
				// valid identity only
				builder.equal(root.get(IdmIdentity_.disabled), Boolean.FALSE),
				//
        		// valid contract only
				RepositoryUtils.getValidPredicate(subRoot, builder),
				//
        		// not disabled, not excluded contract
        		builder.equal(subRoot.get(IdmIdentityContract_.disabled), Boolean.FALSE),
        		builder.or(
        				builder.notEqual(subRoot.get(IdmIdentityContract_.state), ContractState.EXCLUDED),
        				builder.isNull(subRoot.get(IdmIdentityContract_.state))
        		),
				//
        		builder.equal(subRoot.get(IdmIdentityContract_.identity), root), // correlation attr
        		builder.or(subPredicates.toArray(new Predicate[subPredicates.size()]))
        		));
		//
		return builder.exists(subquery);
	}
}
