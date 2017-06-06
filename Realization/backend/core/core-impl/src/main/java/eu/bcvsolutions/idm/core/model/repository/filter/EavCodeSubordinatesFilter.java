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
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute_;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition_;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee_;
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
 * Subordinates criteria builder:
 * - by guarantee and tree structure - finds parent tree node by code in eav attribute value
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class EavCodeSubordinatesFilter 
		extends AbstractFilterBuilder<IdmIdentity, IdentityFilter> {
	
	protected static final String PROPERTY_FORM_DEFINITION = "formDefinition";
	protected static final String PROPERTY_FORM_ATTRIBUTE = "formAttribute";
	protected static final String DEFAULT_FORM_ATTRIBUTE_CODE = "parentCode";
	
	@Override
	public String getName() {
		return IdentityFilter.PARAMETER_SUBORDINATES_FOR;
	}
	
	@Autowired
	public EavCodeSubordinatesFilter(IdmIdentityRepository repository) {
		super(repository);
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> props = super.getPropertyNames();
		props.add(PROPERTY_FORM_DEFINITION);
		props.add(PROPERTY_FORM_ATTRIBUTE);
		return props;
	}

	@Override
	public Predicate getPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdentityFilter filter) {
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
		if (filter.getSubordinatesByTreeType() == null) {
			// manager as guarantee
			Subquery<IdmIdentityContract> subqueryGuarantees = query.subquery(IdmIdentityContract.class);
			Root<IdmContractGuarantee> subRootGuarantees = subqueryGuarantees.from(IdmContractGuarantee.class);
			subqueryGuarantees.select(subRootGuarantees.get(IdmContractGuarantee_.identityContract));
			//
			subqueryGuarantees.where(
	                builder.and(
	                		builder.equal(subRootGuarantees.get(IdmContractGuarantee_.identityContract), subRoot), // correlation attr
	                		builder.equal(subRootGuarantees.get(IdmContractGuarantee_.guarantee).get(IdmIdentity_.id), filter.getSubordinatesFor())
	                		)
	        );
			subPredicates.add(builder.exists(subqueryGuarantees));
		}		
		//
		Subquery<IdmTreeNode> subqueryWp = query.subquery(IdmTreeNode.class);
		Root<IdmIdentityContract> subqueryWpRoot = subqueryWp.from(IdmIdentityContract.class);
		subqueryWp.select(subqueryWpRoot.get(IdmIdentityContract_.workPosition));
		//
		Subquery<String> subqueryEav = query.subquery(String.class);
		Root<IdmTreeNodeFormValue> subRootEav = subqueryEav.from(IdmTreeNodeFormValue.class);
		subqueryEav.select(subRootEav.get(IdmTreeNodeFormValue_.stringValue));
		Path<IdmFormAttribute> eavAttr = subRootEav.get(IdmTreeNodeFormValue_.formAttribute);
		subqueryEav.where(builder.and(
						builder.equal(subRootEav.get(IdmTreeNodeFormValue_.owner), subRoot.get(IdmIdentityContract_.workPosition)),
						builder.equal(
								eavAttr.get(IdmFormAttribute_.formDefinition).get(IdmFormDefinition_.code), 
								getConfigurationValue(PROPERTY_FORM_DEFINITION, FormService.DEFAULT_DEFINITION_CODE)),
						builder.equal(
								eavAttr.get(IdmFormAttribute_.code), 
								getConfigurationValue(PROPERTY_FORM_ATTRIBUTE, DEFAULT_FORM_ATTRIBUTE_CODE))
						));
		//
		Path<IdmTreeNode> wp = subqueryWpRoot.get(IdmIdentityContract_.workPosition);
		subqueryWp.where(builder.and(
				// valid contract only
				builder.or(
						builder.isNull(subqueryWpRoot.get(IdmIdentityContract_.validFrom)),
						builder.lessThanOrEqualTo(subqueryWpRoot.get(IdmIdentityContract_.validFrom), new LocalDate())
						),
				builder.or(
						builder.isNull(subqueryWpRoot.get(IdmIdentityContract_.validTill)),
						builder.greaterThanOrEqualTo(subqueryWpRoot.get(IdmIdentityContract_.validTill), new LocalDate())
						),
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
                		builder.equal(subRoot.get(IdmIdentityContract_.identity), root), // correlation attr - identity has identity contract
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
