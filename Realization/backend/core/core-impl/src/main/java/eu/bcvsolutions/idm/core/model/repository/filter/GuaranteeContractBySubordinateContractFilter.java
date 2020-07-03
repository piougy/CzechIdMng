package eu.bcvsolutions.idm.core.model.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;

/**
 * Filter for find managers' contracts for given subordinate contract:
 * - by guarantee only
 * - only "valid" contract can be manager
 * - only valid or valid in future contracts can have managers
 * - additional filter parameter - IdmIdentityContractFilter.PARAMETER_VALID_CONTRACT_MANAGERS
 * 
 * @author Radek Tomiška
 * @since 10.4.0 
 */
@Component(GuaranteeContractBySubordinateContractFilter.FILTER_NAME)
@Description("Filter for find managers' contracts for given subordinate contract. "
		+ "Finds all direct managers' contracts by guarantee only. "
		+ "Only valid contract can be manager.")
public class GuaranteeContractBySubordinateContractFilter 
		extends AbstractFilterBuilder<IdmIdentityContract, IdmIdentityContractFilter> {
	
	public static final String FILTER_NAME = "guarantee-contract-by-subordinate-contract-filter";
	
	@Override
	public String getName() {
		return IdmIdentityContractFilter.PARAMETER_MANAGERS_BY_CONTRACT;
	}
	
	@Autowired
	public GuaranteeContractBySubordinateContractFilter(IdmIdentityContractRepository repository) {
		super(repository);
	}
	
	public Predicate getValidNowOrInFuturePredicate(
			Path<IdmIdentityContract> pathIc, 
			CriteriaBuilder builder, 
			Boolean validContractManagers) {
		// not set => nullable filter
		if (validContractManagers == null) {
			return builder.conjunction();
		}
		// valid
		Predicate validContractManagersPredicate = RepositoryUtils.getValidNowOrInFuturePredicate(pathIc, builder);
		// invalid
		if (BooleanUtils.isFalse(validContractManagers)) {
			validContractManagersPredicate = builder.not(validContractManagersPredicate);
		}
		return validContractManagersPredicate;
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
		// find direct guarantees
		Subquery<IdmIdentity> subqueryGuarantee = query.subquery(IdmIdentity.class);
		Root<IdmContractGuarantee> subRootGuarantee = subqueryGuarantee.from(IdmContractGuarantee.class);
		subqueryGuarantee.select(subRootGuarantee.get(IdmContractGuarantee_.guarantee));
		//
		Path<IdmIdentityContract> pathIc = subRootGuarantee.get(IdmContractGuarantee_.identityContract);
		subqueryGuarantee.where(
              builder.and(
            		  getValidNowOrInFuturePredicate(pathIc, builder, filter.getValidContractManagers()),
            		  builder.equal(pathIc.get(IdmIdentityContract_.id), filter.getManagersByContract()),
            		  //
      				  // valid identity only
      				  builder.equal(subRootGuarantee.get(IdmContractGuarantee_.guarantee).get(IdmIdentity_.disabled), Boolean.FALSE)
              		));
		// join to contracts
		Subquery<IdmIdentityContract> subqueryGuaranteeContracts = query.subquery(IdmIdentityContract.class);
		Root<IdmIdentityContract> subRootGuaranteeContracts = subqueryGuaranteeContracts.from(IdmIdentityContract.class);
		subqueryGuaranteeContracts.select(subRootGuaranteeContracts);
		subqueryGuaranteeContracts.where(
	              builder.and(
	            		  getValidNowOrInFuturePredicate(subRootGuaranteeContracts, builder, filter.getValidContractManagers()),
	            		  subRootGuaranteeContracts.get(IdmIdentityContract_.identity).in(subqueryGuarantee),
	            		  // not disabled, not excluded contract
	            		  builder.equal(subRootGuaranteeContracts.get(IdmIdentityContract_.disabled), Boolean.FALSE),
	            		  builder.or(
	            				  builder.notEqual(subRootGuaranteeContracts.get(IdmIdentityContract_.state), ContractState.EXCLUDED),
	            				  builder.isNull(subRootGuaranteeContracts.get(IdmIdentityContract_.state))
	            		  ),
	            		  builder.equal(subRootGuaranteeContracts, root) // correlation
	              		));
		//
		return builder.exists(subqueryGuaranteeContracts);
	}
	
	@Override
	public int getOrder() {
		return 5;
	}
}
