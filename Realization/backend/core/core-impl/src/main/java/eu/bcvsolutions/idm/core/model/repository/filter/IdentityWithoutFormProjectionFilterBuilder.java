package eu.bcvsolutions.idm.core.model.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;

/**
 * Filter for identity without form projection - without user type is set.
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component
@Description("Filter for identity without form projection - without user type is set.")
public class IdentityWithoutFormProjectionFilterBuilder extends AbstractFilterBuilder<IdmIdentity, IdmIdentityFilter> {

	@Autowired
	public IdentityWithoutFormProjectionFilterBuilder(IdmIdentityRepository repository) {
		super(repository);
	}

	@Override
	public String getName() {
		return IdmIdentityFilter.PARAMETER_WITHOUT_FORM_PROJECTION;
	}

	@Override
	public Predicate getPredicate(Root<IdmIdentity> root, AbstractQuery<?> query, CriteriaBuilder builder, IdmIdentityFilter filter) {
		Boolean withoutFormProjection = filter.getWithoutFormProjection();
		if (withoutFormProjection == null) {
			return null;
		}	
		// without
		if (withoutFormProjection) {
			return builder.isNull(root.get(IdmIdentity_.formProjection));
		} 
		// with
		return builder.isNotNull(root.get(IdmIdentity_.formProjection));
	}
}
