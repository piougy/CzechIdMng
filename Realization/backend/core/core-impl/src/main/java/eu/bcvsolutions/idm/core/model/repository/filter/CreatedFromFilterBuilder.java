package eu.bcvsolutions.idm.core.model.repository.filter;

import java.time.ZonedDateTime;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.CreatedFromFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.filter.BaseFilterBuilder;

/**
 * Filter for filtering entities created from given time stamp (auditable.created >= createdFrom).
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
@Component
public class CreatedFromFilterBuilder<E extends AbstractEntity> extends BaseFilterBuilder<E, DataFilter> {

	@Override
	public String getName() {
		return CreatedFromFilter.PARAMETER_CREATED_FROM;
	}

	@Override
	public Predicate getPredicate(Root<E> root, AbstractQuery<?> query, CriteriaBuilder builder, DataFilter filter) {
		ZonedDateTime createdFrom = filter.getCreatedFrom();
		
		if (createdFrom == null) {
			return null;
		}

		return builder.greaterThanOrEqualTo(root.get(AbstractEntity_.created), createdFrom);
	}

	@Override
	public Page<E> find(DataFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Find by created from only is not supported, use concrete service instead.");
	}
}
