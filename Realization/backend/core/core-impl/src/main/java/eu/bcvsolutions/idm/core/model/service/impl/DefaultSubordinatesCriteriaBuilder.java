package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.service.api.SubordinatesCriteriaBuilder;

/**
 * Subordinates criteria builder.
 * 
 * TODO: search subordinates and manager recursively by forest index on tree structures
 * TODO: better api
 * TODO: username -> UUID!
 * TODO: managers validity
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultSubordinatesCriteriaBuilder implements SubordinatesCriteriaBuilder {
	
	private final IdmIdentityRepository repository;
	
	public DefaultSubordinatesCriteriaBuilder(IdmIdentityRepository repository) {
		Assert.notNull(repository);
		//
		this.repository = repository; 
	}

	@Override
	public Predicate getSubordinatesPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, 
			String subordinatesFor, IdmTreeType byTreeType) {
		Assert.notNull(subordinatesFor);
		//
		Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
		Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
		subquery.select(subRoot);
		//
		List<Predicate> subPredicates = new ArrayList<>();
		if (byTreeType == null) {
			// manager as guarantee
			Subquery<IdmIdentityContract> subqueryGuarantees = query.subquery(IdmIdentityContract.class);
			Root<IdmContractGuarantee> subRootGuarantees = subqueryGuarantees.from(IdmContractGuarantee.class);
			subqueryGuarantees.select(subRootGuarantees.get(IdmContractGuarantee_.identityContract));
			//
			subqueryGuarantees.where(
	                builder.and(
	                		builder.equal(subRootGuarantees.get(IdmContractGuarantee_.identityContract), subRoot), // correlation attr
	                		builder.equal(subRootGuarantees.get(IdmContractGuarantee_.guarantee).get(IdmIdentity_.username), subordinatesFor)
	                		)
	        );
			subPredicates.add(builder.exists(subqueryGuarantees));
		}
		//  manager from tree structure - only direct subordinate are supported now			
		Subquery<IdmTreeNode> subqueryWp = query.subquery(IdmTreeNode.class);
		Root<IdmIdentityContract> subqueryWpRoot = subqueryWp.from(IdmIdentityContract.class);
		subqueryWp.select(subqueryWpRoot.get(IdmIdentityContract_.workPosition));
		Predicate identityPredicate = builder.equal(
				subqueryWpRoot.get(IdmIdentityContract_.identity).get(IdmIdentity_.username), 
				subordinatesFor);
		if (byTreeType == null) {
			subqueryWp.where(identityPredicate);
		} else {
			subqueryWp.where(builder.and(
					identityPredicate,
					builder.equal(subqueryWpRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.treeType), byTreeType)
					));
		}			
		subPredicates.add(subRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.parent).in(subqueryWp));			
		// 
		subquery.where(
                builder.and(
                		builder.equal(subRoot.get(IdmIdentityContract_.identity), root), // correlation attr
                		builder.or(subPredicates.toArray(new Predicate[subPredicates.size()]))
                		)
        );
		//		
		return builder.exists(subquery);
	}

	@Override
	public Predicate getManagersPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			String managersFor, IdmTreeType byTreeType) {
		Assert.notNull(managersFor);
		//
		Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
		Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
		subquery.select(subRoot);
		//
		List<Predicate> subPredicates = new ArrayList<>();
		if (byTreeType == null) {
			// manager as guarantee			
			subPredicates.add(
                    builder.and(
                    		builder.equal(subRoot.join(IdmIdentityContract_.guarantees, JoinType.LEFT).get(IdmContractGuarantee_.guarantee), root), // correlation attr
                    		builder.equal(subRoot.get(IdmIdentityContract_.identity).get(IdmIdentity_.username), managersFor)
                    		)	           
					);
		}
		// manager from tree structure - only direct managers are supported now
		Subquery<IdmTreeNode> subqueryWp = query.subquery(IdmTreeNode.class);
		Root<IdmIdentityContract> subqueryWpRoot = subqueryWp.from(IdmIdentityContract.class);
		subqueryWp.select(subqueryWpRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.parent));			
		Predicate identityPredicate = builder.equal(
				subqueryWpRoot.get(IdmIdentityContract_.identity).get(IdmIdentity_.username), 
				managersFor);
		if (byTreeType == null) {
			subqueryWp.where(identityPredicate);	
		} else {
			subqueryWp.where(builder.and(
					identityPredicate,
					builder.equal(subqueryWpRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.treeType), byTreeType)
					));	
		}					
		subPredicates.add(
                builder.and(
                		builder.equal(subRoot.get(IdmIdentityContract_.identity), root), // correlation attr
                		subRoot.get(IdmIdentityContract_.workPosition).in(subqueryWp)
                		)
        );			
		subquery.where(builder.or(subPredicates.toArray(new Predicate[subPredicates.size()])));
		//
		return builder.exists(subquery);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<IdmIdentity> getManagers(String managersFor, IdmTreeType byTreeType , Pageable pageable) {
		// transform filter to criteria
		Specification<IdmIdentity> criteria = new Specification<IdmIdentity>() {
			public Predicate toPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				Predicate predicate = DefaultSubordinatesCriteriaBuilder.this.getManagersPredicate(root, query, builder, managersFor, byTreeType);
				return query.where(predicate).getRestriction();
			}
		};
		return repository.findAll(criteria, pageable);
	}
}
