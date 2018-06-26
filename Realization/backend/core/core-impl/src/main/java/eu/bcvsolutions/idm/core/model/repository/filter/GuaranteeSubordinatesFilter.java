package eu.bcvsolutions.idm.core.model.repository.filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;

/**
 * Subordinates criteria builder:
 * - by guarantees only
 * 
 * @author Radek Tomiška
 *
 */
@Component(GuaranteeSubordinatesFilter.BEAN_NAME)
public class GuaranteeSubordinatesFilter 
		extends AbstractFilterBuilder<IdmIdentity, IdmIdentityFilter> {
	
	public static final String BEAN_NAME = "guaranteeSubordinatesFilter";
	
	@Override
	public String getName() {
		return IdmIdentityFilter.PARAMETER_SUBORDINATES_FOR;
	}
	
	@Autowired
	public GuaranteeSubordinatesFilter(IdmIdentityRepository repository) {
		super(repository);
	}
	
	/**
	 * Predicate for subordin as guarantee
	 * 
	 * @param root
	 * @param query
	 * @param builder
	 * @param filter
	 * @return
	 */
	protected Predicate getGuaranteesPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmIdentityFilter filter) {
		// identity has to have identity contract
		// manager as guarantee
		Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
		Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
		subquery.select(subRoot);
		//
		// manager as guarantee
		Join<IdmIdentityContract, IdmContractGuarantee> subRootGuarantees = subRoot.join(IdmIdentityContract_.guarantees);
		//
		subquery.where(
				builder.equal(subRoot.get(IdmIdentityContract_.identity), root), // correlation attr - identity has identity contract
				builder.equal(subRootGuarantees.get(IdmContractGuarantee_.guarantee).get(IdmIdentity_.id), filter.getSubordinatesFor())
				);
		//		
		return builder.exists(subquery);
	}

	@Override
	public Predicate getPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmIdentityFilter filter) {
		if (filter.getSubordinatesFor() == null) {
			return null;
		}
		if (filter.getSubordinatesByTreeType() != null || !filter.isIncludeGuarantees()) {
			// guarantees is not needed
			return builder.disjunction();
		}
		//		
		return getGuaranteesPredicate(root, query, builder, filter);
	}
	
	@Override
	public int getOrder() {
		return 5;
	}
}
