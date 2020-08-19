package eu.bcvsolutions.idm.core.model.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;

/**
 * Filter for identity without work position - without contract or with contract without work position is set.
 *
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component
@Description("Filter for identity without work position - without contract or with contract without work position is set.")
public class IdentityWithoutWorkPositionFilterBuilder extends AbstractFilterBuilder<IdmIdentity, IdmIdentityFilter> {

	@Autowired
	public IdentityWithoutWorkPositionFilterBuilder(IdmIdentityRepository repository) {
		super(repository);
	}

	@Override
	public String getName() {
		return IdmIdentityFilter.PARAMETER_WITHOUT_WORK_POSITION;
	}

	@Override
	public Predicate getPredicate(Root<IdmIdentity> root, AbstractQuery<?> query, CriteriaBuilder builder, IdmIdentityFilter filter) {
		Boolean withoutWorkPosition = filter.getWithoutWorkPosition();
		if (withoutWorkPosition == null) {
			return null;
		}
		//
		// exists come contract + not
		Subquery<IdmIdentityContract> subqueryExist = query.subquery(IdmIdentityContract.class);
		Root<IdmIdentityContract> subRootExist = subqueryExist.from(IdmIdentityContract.class);
		subqueryExist.select(subRootExist);
		subqueryExist.where(builder.equal(subRootExist.get(IdmIdentityContract_.identity), root));	
		//
		// all contracts with work position + not
		Subquery<IdmIdentityContract> subqueryFilledWorkPosition = query.subquery(IdmIdentityContract.class);
		Root<IdmIdentityContract> subRootFilledWorkPosition = subqueryFilledWorkPosition.from(IdmIdentityContract.class);
		subqueryFilledWorkPosition.select(subRootFilledWorkPosition);
		subqueryFilledWorkPosition.where(
                builder.and(
                		builder.equal(subRootFilledWorkPosition.get(IdmIdentityContract_.identity), root), // correlation attr
                		builder.isNotNull(subRootFilledWorkPosition.get(IdmIdentityContract_.workPosition))
                )
        );			
		// without
		if (withoutWorkPosition) {
			return builder.or(
					builder.not(builder.exists(subqueryExist)), // without contract
					builder.not(builder.exists(subqueryFilledWorkPosition)) // with contract without work position is set.
			);
		} 
		// with
		return builder.exists(subqueryFilledWorkPosition);
	}
}
