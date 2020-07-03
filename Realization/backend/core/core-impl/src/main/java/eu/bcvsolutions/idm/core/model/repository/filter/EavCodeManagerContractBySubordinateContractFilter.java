package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute_;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmTreeNodeFormValue;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmTreeNodeFormValue_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;

/**
 * Find managers' contracts by subordinate contract.
 * - by guarantee and tree structure - finds parent tree node by code in eav attribute value
 * - only "valid" contract can be manager
 * - only valid or valid in future contracts can have managers
 * - additional filter parameter - IdmIdentityContractFilter.PARAMETER_VALID_CONTRACT_MANAGERS
 * 
 * @author Radek Tomiška
 * @since 10.4.0
 */
@Component(EavCodeManagerContractBySubordinateContractFilter.FILTER_NAME)
@Description("Find managers' contracts by subordinate contract."
		+ "Supports managers' contracts by guarantee and tree structure - finds parent tree node by code in eav attribute value. "
		+ "Only valid contract can be manager.")
public class EavCodeManagerContractBySubordinateContractFilter 
		extends AbstractFilterBuilder<IdmIdentityContract, IdmIdentityContractFilter> {
	
	public static final String FILTER_NAME = "eav-code-manager-contract-by-subordinate-contract-filter";
	//
	@Autowired private GuaranteeContractBySubordinateContractFilter guaranteeContractBySubordinateContractFilter;
	
	@Override
	public String getName() {
		return IdmIdentityFilter.PARAMETER_MANAGERS_BY_CONTRACT;
	}
	
	@Autowired
	public EavCodeManagerContractBySubordinateContractFilter(IdmIdentityContractRepository repository) {
		super(repository);
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> props = super.getPropertyNames();
		props.add(EavCodeContractByManagerFilter.PROPERTY_FORM_DEFINITION);
		props.add(EavCodeContractByManagerFilter.PROPERTY_FORM_ATTRIBUTE);
		props.add(EavCodeContractByManagerFilter.PROPERTY_PERSISTENT_TYPE);
		return props;
	}

	@Override
	public Predicate getPredicate(
			Root<IdmIdentityContract> 
			root, AbstractQuery<?> query, 
			CriteriaBuilder builder, 
			IdmIdentityContractFilter filter) {
		if (filter.getManagersByContract() == null) {
			return null;
		}
		//
		Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
		Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
		Join<IdmIdentityContract, IdmTreeNode> subRootTree = subRoot.join(IdmIdentityContract_.workPosition, JoinType.LEFT);
		subquery.select(subRoot);
		//
		List<Predicate> subPredicates = new ArrayList<>();
		//
		if (filter.isIncludeGuarantees()) {
			// manager as guarantee
			subPredicates.add(guaranteeContractBySubordinateContractFilter.getPredicate(root, query, builder, filter));
		}
		//
		// manager from tree structure - only direct managers are supported now
		// manager's tree node is defined in extended attribute on tree node
		Subquery<IdmTreeNodeFormValue> subqueryEav = query.subquery(IdmTreeNodeFormValue.class);
		Root<IdmTreeNodeFormValue> subRootEav = subqueryEav.from(IdmTreeNodeFormValue.class);
		subqueryEav.select(subRootEav);
		
		ListJoin<IdmTreeNode, IdmIdentityContract> joinContracts = subRootEav.join(IdmTreeNodeFormValue_.owner).join(IdmTreeNode_.contracts);
		//
		// prevent to generate cross joins by default
		Join<IdmTreeNodeFormValue, IdmFormAttribute> eavAttr = subRootEav.join(IdmTreeNodeFormValue_.formAttribute);
		Join<IdmFormAttribute, IdmFormDefinition> extDef = eavAttr.join(IdmFormAttribute_.formDefinition);
		//
		subqueryEav.where(builder.and(
						// future valid contracts
						guaranteeContractBySubordinateContractFilter.getValidNowOrInFuturePredicate(
								joinContracts, 
								builder, 
								filter.getValidContractManagers()),
						builder.equal(joinContracts.get(IdmIdentityContract_.id), filter.getManagersByContract()),
						builder.equal(subRootTree.get(IdmTreeNode_.code), 
								subRootEav.get(getConfigurationValue(EavCodeContractByManagerFilter.PROPERTY_PERSISTENT_TYPE, EavCodeContractByManagerFilter.DEFAULT_PERSISTENT_TYPE))
								),
						builder.equal(
								extDef.get(IdmFormDefinition_.code), 
								getConfigurationValue(EavCodeContractByManagerFilter.PROPERTY_FORM_DEFINITION, FormService.DEFAULT_DEFINITION_CODE)),
						builder.equal(
								eavAttr.get(IdmFormAttribute_.code), 
								getConfigurationValue(EavCodeContractByManagerFilter.PROPERTY_FORM_ATTRIBUTE, EavCodeContractByManagerFilter.DEFAULT_FORM_ATTRIBUTE_CODE))
						));
		//
		subPredicates.add(builder.exists(subqueryEav));
		//
		subquery.where(
				builder.and(
						//
                		// valid contract only
						RepositoryUtils.getValidNowOrInFuturePredicate(subRoot, builder),
						//
                		// not disabled, not excluded contract
                		builder.equal(subRoot.get(IdmIdentityContract_.disabled), Boolean.FALSE),
                		builder.or(
                				builder.notEqual(subRoot.get(IdmIdentityContract_.state), ContractState.EXCLUDED),
                				builder.isNull(subRoot.get(IdmIdentityContract_.state))
                		),
    					//
    					builder.equal(subRoot, root),
    					builder.or(subPredicates.toArray(new Predicate[subPredicates.size()]))
    					));
		//
		return builder.exists(subquery);
	}
	
	@Override
	public int getOrder() {
		return 10;
	}
}
