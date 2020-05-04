package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute_;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType_;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmTreeNodeFormValue;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmTreeNodeFormValue_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;

/**
 * Subordinates criteria builder:
 * - by guarantee and tree structure - finds parent tree node by code in eav attribute value
 * 
 * @author Radek Tomiška
 * @since 9.7.0
 */
@Component(EavCodeContractByManagerFilter.BEAN_NAME)
@Description("Filter for find subordinates (contracts) for given manager (identity). "
		+ "Supports subordinate contracts by guarantee and tree structure - finds parent tree node by code in eav attribute value.")
public class EavCodeContractByManagerFilter 
		extends AbstractFilterBuilder<IdmIdentityContract, IdmIdentityContractFilter> {
	
	public static final String BEAN_NAME = "eav-code-contract-by-manager-filter";
	//
	protected static final String PROPERTY_FORM_DEFINITION = "formDefinition";
	protected static final String PROPERTY_FORM_ATTRIBUTE = "formAttribute";
	protected static final String PROPERTY_PERSISTENT_TYPE = "persistentType";
	protected static final String DEFAULT_FORM_ATTRIBUTE_CODE = "parentCode";
	protected static final String DEFAULT_PERSISTENT_TYPE = "stringValue";
	//
	@Autowired private ContractByGuaranteeFilter contractByGuaranteeFilter;
	
	@Override
	public String getName() {
		return IdmIdentityContractFilter.PARAMETER_SUBORDINATES_FOR;
	}
	
	@Autowired
	public EavCodeContractByManagerFilter(IdmIdentityContractRepository repository) {
		super(repository);
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> props = super.getPropertyNames();
		props.add(PROPERTY_FORM_DEFINITION);
		props.add(PROPERTY_FORM_ATTRIBUTE);
		props.add(PROPERTY_PERSISTENT_TYPE);
		return props;
	}

	@Override
	public Predicate getPredicate(Root<IdmIdentityContract> root, AbstractQuery<?> query, 
			CriteriaBuilder builder, IdmIdentityContractFilter filter) {
		if (filter.getSubordinatesFor() == null) {
			return null;
		}
		//
		// identity has to have identity contract
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
		Subquery<IdmTreeNode> subqueryWp = query.subquery(IdmTreeNode.class);
		Root<IdmIdentityContract> subqueryWpRoot = subqueryWp.from(IdmIdentityContract.class);
		subqueryWp.select(subqueryWpRoot.get(IdmIdentityContract_.workPosition));
		//
		Subquery<String> subqueryEav = query.subquery(String.class);
		Root<IdmTreeNodeFormValue> subRootEav = subqueryEav.from(IdmTreeNodeFormValue.class);
		subqueryEav.select(subRootEav.get(getConfigurationValue(PROPERTY_PERSISTENT_TYPE, DEFAULT_PERSISTENT_TYPE)));
		// prevent to generate cross joins by default
		Join<IdmTreeNodeFormValue, IdmFormAttribute> eavAttr = subRootEav.join(IdmTreeNodeFormValue_.formAttribute);
		Join<IdmFormAttribute, IdmFormDefinition> extDef = eavAttr.join(IdmFormAttribute_.formDefinition);
		//
		subqueryEav.where(builder.and(
						builder.equal(subRootEav.get(IdmTreeNodeFormValue_.owner), subRoot.get(IdmIdentityContract_.workPosition)),
						builder.equal(
								extDef.get(IdmFormDefinition_.code), 
								getConfigurationValue(PROPERTY_FORM_DEFINITION, FormService.DEFAULT_DEFINITION_CODE)),
						builder.equal(
								eavAttr.get(IdmFormAttribute_.code), 
								getConfigurationValue(PROPERTY_FORM_ATTRIBUTE, DEFAULT_FORM_ATTRIBUTE_CODE))
						));
		//
		Path<IdmTreeNode> wp = subqueryWpRoot.get(IdmIdentityContract_.workPosition);
		subqueryWp.where(builder.and(
				// valid contract only
				RepositoryUtils.getValidNowOrInFuturePredicate(subqueryWpRoot, builder),
        		builder.equal(subqueryWpRoot.get(IdmIdentityContract_.disabled), Boolean.FALSE),
				//
				(filter.getSubordinatesByTreeType() == null) 
					? builder.conjunction() 
					: builder.equal(wp.get(IdmTreeNode_.treeType).get(IdmTreeType_.id), filter.getSubordinatesByTreeType()),
				builder.equal(wp.get(IdmTreeNode_.code), subqueryEav), // eav attribute
				builder.equal(subqueryWpRoot.get(IdmIdentityContract_.identity).get(IdmIdentity_.id), filter.getSubordinatesFor())
				));
		subPredicates.add(builder.exists(subqueryWp));
		// 
		subquery.where(
                builder.and(
                		builder.equal(subRoot, root), // correlation attr
                		builder.or(subPredicates.toArray(new Predicate[subPredicates.size()]))
                		)
        );
		//		
		return builder.exists(subquery);
	}
	
	@Override
	public int getOrder() {
		return 10;
	}
}
