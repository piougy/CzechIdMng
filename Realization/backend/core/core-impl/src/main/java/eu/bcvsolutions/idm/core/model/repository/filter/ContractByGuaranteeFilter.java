package eu.bcvsolutions.idm.core.model.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;

/**
 * Subordinate contracts criteria builder:
 * - by guarantees only
 * 
 * @author Radek Tomiška
 * @since 9.7.0
 */
@Component(ContractByGuaranteeFilter.BEAN_NAME)
public class ContractByGuaranteeFilter 
		extends AbstractFilterBuilder<IdmIdentityContract, IdmIdentityContractFilter> {
	
	public static final String BEAN_NAME = "contract-by-guarantee-filter";
	
	@Override
	public String getName() {
		return IdmIdentityContractFilter.PARAMETER_SUBORDINATES_FOR;
	}
	
	@Autowired
	public ContractByGuaranteeFilter(IdmIdentityContractRepository repository) {
		super(repository);
	}

	@Override
	public Predicate getPredicate(Root<IdmIdentityContract> root, AbstractQuery<?> query, CriteriaBuilder builder, IdmIdentityContractFilter filter) {
		if (filter.getSubordinatesFor() == null) {
			return null;
		}
		if (filter.getSubordinatesByTreeType() != null || !filter.isIncludeGuarantees()) {
			// guarantees is not needed
			return builder.disjunction();
		}
		//
		// manager as guarantee
		Subquery<IdmContractGuarantee> subquery = query.subquery(IdmContractGuarantee.class);
		Root<IdmContractGuarantee> subRoot = subquery.from(IdmContractGuarantee.class);
		subquery.select(subRoot);
		//
		subquery.where(
				builder.equal(subRoot.get(IdmContractGuarantee_.identityContract), root), // correlation attr
				builder.equal(subRoot.get(IdmContractGuarantee_.guarantee).get(IdmIdentity_.id), filter.getSubordinatesFor())
				);
		//		
		return builder.exists(subquery);
	}
	
	@Override
	public int getOrder() {
		return 5;
	}
}
