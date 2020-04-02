package eu.bcvsolutions.idm.core.model.repository.filter;

import java.time.ZonedDateTime;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.CreatedTillFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.filter.BaseFilterBuilder;

/**
 * Filter for filtering entities created till given time stamp  (auditable.created <= createdTill).
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
@Component
public class CreatedTillFilterBuilder<E extends AbstractEntity> extends BaseFilterBuilder<E, DataFilter> {

	@Override
	public String getName() {
		return CreatedTillFilter.PARAMETER_CREATED_TILL;
	}

	@Override
	public Predicate getPredicate(Root<E> root, AbstractQuery<?> query, CriteriaBuilder builder, DataFilter filter) {
		ZonedDateTime createdTill = filter.getCreatedTill();
		
		if (createdTill == null) {
			return null;
		}

		return builder.lessThanOrEqualTo(root.get(AbstractEntity_.created), createdTill);
	}

	@Override
	public Page<E> find(DataFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Find by created till only is not supported, use concrete service instead.");
	}
}
