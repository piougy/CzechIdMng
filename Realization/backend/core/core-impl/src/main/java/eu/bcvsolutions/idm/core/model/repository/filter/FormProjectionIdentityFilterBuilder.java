package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.UUID;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormProjection_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;

/**
 * Filter by identity's projection (~type).
 *
 * @author Radek Tomiška
 * @since 10.2.0
 *
 */
@Component
@Description("Filter by identity's projection (~type).")
public class FormProjectionIdentityFilterBuilder extends AbstractFilterBuilder<IdmIdentity, IdmIdentityFilter> {

	@Autowired
	public FormProjectionIdentityFilterBuilder(IdmIdentityRepository repository) {
		super(repository);
	}

	@Override
	public String getName() {
		return IdmIdentityFilter.PARAMETER_FORM_PROJECTION;
	}

	@Override
	public Predicate getPredicate(Root<IdmIdentity> root, AbstractQuery<?> query, CriteriaBuilder builder, IdmIdentityFilter filter) {
		UUID projection = filter.getFormProjection();
		if (projection == null) {
			return null;
		}
		return builder.equal(root.get(IdmIdentity_.formProjection).get(IdmFormProjection_.id), projection);
	}
}
