package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.service.api.SubordinatesCriteriaBuilder;

/**
 * Subordinates criteria builder.
 * 
 * TODO: jpa metamodel generator
 * TODO: search subordinates and manager recursively by forest index on tree structures
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
			subPredicates.add(builder.equal(subRoot.get("guarantee").get("username"), subordinatesFor));
		}
		//  manager from tree structure - only direct subordinate are supported now			
		Subquery<IdmIdentityContract> subqueryWp = query.subquery(IdmIdentityContract.class);
		Root<IdmIdentityContract> subqueryWpRoot = subqueryWp.from(IdmIdentityContract.class);
		subqueryWp.select(subqueryWpRoot.get("workPosition"));
		Predicate identityPredicate = builder.equal(subqueryWpRoot.get("identity").get("username"), subordinatesFor);
		if (byTreeType == null) {
			subqueryWp.where(identityPredicate);
		} else {
			subqueryWp.where(builder.and(
					identityPredicate,
					builder.equal(subqueryWpRoot.get("workPosition").get("treeType"), byTreeType)
					));
		}			
		subPredicates.add(subRoot.get("workPosition").get("parent").in(subqueryWp));			
		// 
		subquery.where(
                builder.and(
                		builder.equal(subRoot.get("identity"), root), // correlation attr
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
                    		builder.equal(subRoot.get("guarantee"), root), // correlation attr
                    		builder.equal(subRoot.get("identity").get("username"), managersFor)
                    		)	           
					);
		}
		// manager from tree structure - only direct managers are supported now
		Subquery<IdmIdentityContract> subqueryWp = query.subquery(IdmIdentityContract.class);
		Root<IdmIdentityContract> subqueryWpRoot = subqueryWp.from(IdmIdentityContract.class);
		subqueryWp.select(subqueryWpRoot.get("workPosition").get("parent"));			
		Predicate identityPredicate = builder.equal(subqueryWpRoot.get("identity").get("username"), managersFor);
		if (byTreeType == null) {
			subqueryWp.where(identityPredicate);	
		} else {
			subqueryWp.where(builder.and(
					identityPredicate,
					builder.equal(subqueryWpRoot.get("workPosition").get("treeType"), byTreeType)
					));	
		}					
		subPredicates.add(
                builder.and(
                		builder.equal(subRoot.get("identity"), root), // correlation attr
                		subRoot.get("workPosition").in(subqueryWp)
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
