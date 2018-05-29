package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
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
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute_;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType_;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmTreeNodeFormValue;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmTreeNodeFormValue_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;

/**
 * Managers criteria builder.
 * - by guarantee and tree structure - finds parent tree node by code in eav attribute value
 * - only "valid" identity can be manager
 * 
 * @author Radek Tomiška
 *
 */
@Component("eavCodeManagersFilter")
public class EavCodeManagersFilter 
		extends AbstractFilterBuilder<IdmIdentity, IdmIdentityFilter> {
	
	@Autowired private GuaranteeManagersFilter guaranteeManagersFilter;
	
	@Override
	public String getName() {
		return IdmIdentityFilter.PARAMETER_MANAGERS_FOR;
	}
	
	@Autowired
	public EavCodeManagersFilter(IdmIdentityRepository repository) {
		super(repository);
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> props = super.getPropertyNames();
		props.add(EavCodeSubordinatesFilter.PROPERTY_FORM_DEFINITION);
		props.add(EavCodeSubordinatesFilter.PROPERTY_FORM_ATTRIBUTE);
		props.add(EavCodeSubordinatesFilter.PROPERTY_PERSISTENT_TYPE);
		return props;
	}

	@Override
	public Predicate getPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmIdentityFilter filter) {
		if (filter.getManagersFor() == null) {
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
		if (filter.getManagersByTreeType() == null && filter.isIncludeGuarantees()) {
			// manager as guarantee
			subPredicates.add(guaranteeManagersFilter.getGuaranteesPredicate(root, query, builder, filter));
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
		Path<IdmTreeNode> wp = joinContracts.get(IdmIdentityContract_.workPosition);
		subqueryEav.where(builder.and(
						filter.getManagersByContract() != null // concrete contract id only
			    			? builder.equal(joinContracts.get(IdmIdentityContract_.id), filter.getManagersByContract())
			    			: builder.conjunction(),
						builder.equal(subRootTree.get(IdmTreeNode_.code), 
								subRootEav.get(getConfigurationValue(EavCodeSubordinatesFilter.PROPERTY_PERSISTENT_TYPE, EavCodeSubordinatesFilter.DEFAULT_PERSISTENT_TYPE))
								),
						builder.equal(joinContracts.get(IdmIdentityContract_.identity).get(IdmIdentity_.id), filter.getManagersFor()),
						// by tree type structure
						filter.getManagersByTreeType() == null 
							? builder.conjunction() 
							: builder.equal(wp.get(IdmTreeNode_.treeType).get(IdmTreeType_.id), filter.getManagersByTreeType()),
						builder.equal(
								extDef.get(IdmFormDefinition_.code), 
								getConfigurationValue(EavCodeSubordinatesFilter.PROPERTY_FORM_DEFINITION, FormService.DEFAULT_DEFINITION_CODE)),
						builder.equal(
								eavAttr.get(IdmFormAttribute_.code), 
								getConfigurationValue(EavCodeSubordinatesFilter.PROPERTY_FORM_ATTRIBUTE, EavCodeSubordinatesFilter.DEFAULT_FORM_ATTRIBUTE_CODE))
						));
		//
		subPredicates.add(builder.exists(subqueryEav));
		//
		subquery.where(
				builder.and(
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
    					builder.equal(subRoot.get(IdmIdentityContract_.identity), root),
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
