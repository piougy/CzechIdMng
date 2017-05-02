package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute_;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition_;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmTreeNodeFormValue;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmTreeNodeFormValue_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;

/**
 * Managers criteria builder.
 * - by guarantee and tree structure - finds parent tree node by code in eav attribute value
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class EavCodeManagersFilter 
		extends AbstractFilterBuilder<IdmIdentity, IdentityFilter> 
		implements ManagersFilter {
	
	public static final String PROPERTY_FORM_DEFINITION = "formDefinition";
	public static final String PROPERTY_FORM_ATTRIBUTE = "formAttribute";
	public static final String DEFAULT_FORM_ATTRIBUTE = "parentCode";
	
	@Autowired
	public EavCodeManagersFilter(IdmIdentityRepository repository) {
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
		if (filter.getManagersFor() == null) {
			return null;
		}
		//
		Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
		Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
		subquery.select(subRoot);
		//
		List<Predicate> subPredicates = new ArrayList<>();
		if (filter.getManagersByTreeType() == null) {
			// manager as guarantee
			Subquery<IdmIdentityContract> subqueryGuarantee = query.subquery(IdmIdentityContract.class);
			Root<IdmContractGuarantee> subRootGuarantee = subqueryGuarantee.from(IdmContractGuarantee.class);
			Path<IdmIdentityContract> pathIc = subRootGuarantee.get(IdmContractGuarantee_.identityContract);
			subqueryGuarantee.select(pathIc);
			
			subqueryGuarantee.where(
	              builder.and(
	            		  builder.equal(pathIc.get(IdmIdentityContract_.identity), filter.getManagersFor()),
	            		  builder.equal(subRootGuarantee.get(IdmContractGuarantee_.guarantee), root)
	              		));
			subPredicates.add(builder.exists(subqueryGuarantee));
		}		
		// manager from tree structure - only direct managers are supported now
		Subquery<UUID> subqueryWp = query.subquery(UUID.class);
		Root<IdmIdentityContract> subqueryWpRoot = subqueryWp.from(IdmIdentityContract.class);
		subqueryWp.select(subqueryWpRoot.get(IdmIdentityContract_.id)); 		
		Subquery<String> subqueryEav = query.subquery(String.class);
		Root<IdmTreeNodeFormValue> subRootEav = subqueryEav.from(IdmTreeNodeFormValue.class);
		subqueryEav.select(subRootEav.get(IdmTreeNodeFormValue_.stringValue));
		Path<IdmFormAttribute> eavAttr = subRootEav.get(IdmTreeNodeFormValue_.formAttribute);	
		Path<IdmTreeNode> wp = subqueryWpRoot.get(IdmIdentityContract_.workPosition);
		subqueryEav.where(builder.and(
						builder.equal(subRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.code), subRootEav.get(IdmTreeNodeFormValue_.stringValue)),
						builder.equal(subqueryWpRoot.get(IdmIdentityContract_.identity), filter.getManagersFor()),
						builder.equal(subRootEav.get(IdmTreeNodeFormValue_.owner), wp),
						filter.getManagersByTreeType() == null ? builder.conjunction() : builder.equal(wp.get(IdmTreeNode_.treeType), filter.getManagersByTreeType()),
						builder.equal(
								eavAttr.get(IdmFormAttribute_.formDefinition).get(IdmFormDefinition_.name), 
								getConfigurationProperty(PROPERTY_FORM_DEFINITION, FormService.DEFAULT_DEFINITION_NAME)),
						builder.equal(
								eavAttr.get(IdmFormAttribute_.name), 
								getConfigurationProperty(PROPERTY_FORM_ATTRIBUTE, DEFAULT_FORM_ATTRIBUTE))
						));
		subqueryWp.where(builder.exists(subqueryEav));
		//
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
    					builder.equal(subRoot.get(IdmIdentityContract_.identity), root),
    					builder.exists(subqueryWp)
                		)
        );		
		subquery.where(builder.or(subPredicates.toArray(new Predicate[subPredicates.size()])));
		//
		return builder.exists(subquery);
	}
	
	@Override
	public int getOrder() {
		return 10;
	}
}
