package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee_;
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
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class DefaultManagersFilter 
		extends AbstractFilterBuilder<IdmIdentity, IdentityFilter> 
		implements ManagersFilter {
	
	@Autowired
	public DefaultManagersFilter(IdmIdentityRepository repository) {
		super(repository);
	}

	@Override
	public Predicate getPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdentityFilter filter) {
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
			Subquery<IdmIdentityContract> subqueryGuarantee = query.subquery(IdmIdentityContract.class);
			Root<IdmContractGuarantee> subRootGuarantee = subqueryGuarantee.from(IdmContractGuarantee.class);
			Path<IdmIdentityContract> pathIc = subRootGuarantee.get(IdmContractGuarantee_.identityContract);
			subqueryGuarantee.select(pathIc);
			
			subqueryGuarantee.where(
	              builder.and(
	            		  builder.equal(pathIc.get(IdmIdentityContract_.identity).get(IdmIdentity_.id), filter.getManagersFor()),
	            		  builder.equal(subRootGuarantee.get(IdmContractGuarantee_.guarantee), root),
	            		  filter.getManagersByContract() != null // concrete contract id only
	            		  	? builder.equal(pathIc.get(IdmIdentityContract_.id), filter.getManagersByContract())
	            		  	: builder.conjunction()
	              		));
			subPredicates.add(builder.exists(subqueryGuarantee));
		}		
		// manager from tree structure - only direct managers are supported now
		Subquery<IdmTreeNode> subqueryWp = query.subquery(IdmTreeNode.class);
		Root<IdmIdentityContract> subqueryWpRoot = subqueryWp.from(IdmIdentityContract.class);
		subqueryWp.select(subqueryWpRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.parent));			
		Predicate identityPredicate = builder.and(
				builder.equal(
						subqueryWpRoot.get(IdmIdentityContract_.identity).get(IdmIdentity_.id), 
						filter.getManagersFor()),
				filter.getManagersByContract() != null // concrete contract id only
	    			? builder.equal(subqueryWpRoot.get(IdmIdentityContract_.id), filter.getManagersByContract())
	    			: builder.conjunction()
				);
		if (filter.getManagersByTreeType() == null) {
			subqueryWp.where(identityPredicate);	
		} else {
			subqueryWp.where(builder.and(
					identityPredicate,
					builder.equal(
							subqueryWpRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.treeType).get(IdmTreeType_.id), 
							filter.getManagersByTreeType())
					));	
		}
		subPredicates.add(
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
                		subRoot.get(IdmIdentityContract_.workPosition).in(subqueryWp)
                		)
        );		
		subquery.where(builder.or(subPredicates.toArray(new Predicate[subPredicates.size()])));
		//
		return builder.exists(subquery);
	}
}
