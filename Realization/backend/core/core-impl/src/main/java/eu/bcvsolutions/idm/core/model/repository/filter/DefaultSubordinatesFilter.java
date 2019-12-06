package eu.bcvsolutions.idm.core.model.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;

/**
 * Subordinates criteria builder:
 * - by guarantee and tree structure - finds parent tree node standardly by tree structure
 * 
 * @author Radek Tomiška
 * 
 */
@Component(DefaultSubordinatesFilter.FILTER_NAME)
@Description("Filter for find subordinates for given identity. "
		+ "Supports subordinates by guarantee and tree structure - finds parent tree node standardly by tree structure.")
public class DefaultSubordinatesFilter extends AbstractFilterBuilder<IdmIdentity, IdmIdentityFilter> {
	
	public static final String FILTER_NAME = "defaultSubordinatesFilter";
	//
	@Autowired @Lazy FilterManager filterManager;
	
	@Override
	public String getName() {
		return IdmIdentityFilter.PARAMETER_SUBORDINATES_FOR;
	}
	
	@Autowired
	public DefaultSubordinatesFilter(IdmIdentityRepository repository) {
		super(repository);
	}

	@Override
	public Predicate getPredicate(Root<IdmIdentity> root, AbstractQuery<?> query, CriteriaBuilder builder, IdmIdentityFilter filter) {
		if (filter.getSubordinatesFor() == null) {
			return null;
		}
		//
		// identity has to have identity contract
		IdmIdentityContractFilter subFilter = new IdmIdentityContractFilter(filter.getData());
		Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
		Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
		subquery.select(subRoot);
		//
		Predicate contractPredicate = filterManager
				.getBuilder(
					IdmIdentityContract.class, 
					IdmIdentityContractFilter.PARAMETER_SUBORDINATES_FOR)
				.getPredicate(subRoot, subquery, builder, subFilter);
		// 
		subquery.where(
                builder.and(
                		builder.equal(subRoot.get(IdmIdentityContract_.identity), root), // correlation attr - identity has identity contract
                		contractPredicate
                		)
        );
		//		
		return builder.exists(subquery);
	}
}
