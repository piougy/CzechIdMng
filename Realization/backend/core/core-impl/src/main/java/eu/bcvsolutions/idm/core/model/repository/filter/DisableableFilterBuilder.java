package eu.bcvsolutions.idm.core.model.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DisableableFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.exception.EntityTypeNotDisableableException;
import eu.bcvsolutions.idm.core.api.repository.filter.BaseFilterBuilder;

/**
 * Filter for filtering entities created till given time stamp  (auditable.created <= createdTill).
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
@Component
public class DisableableFilterBuilder<E extends AbstractEntity> extends BaseFilterBuilder<E, DataFilter> {

	@Override
	public String getName() {
		return DisableableFilter.PARAMETER_DISABLED;
	}

	@Override
	public Predicate getPredicate(Root<E> root, AbstractQuery<?> query, CriteriaBuilder builder, DataFilter filter) {
		if (!(filter instanceof DisableableFilter)) {
			return null;
		}
		if (!Disableable.class.isAssignableFrom(root.getJavaType())) {
			throw new EntityTypeNotDisableableException(root.getJavaType().getCanonicalName());
		}
		//
		DisableableFilter disableableFilter = (DisableableFilter) filter;
		Boolean disabled = disableableFilter.getDisabled();
		if (disabled == null) {
			return null;
		}
		return builder.equal(root.get(Disableable.PROPERTY_DISABLED), disabled);
	}

	@Override
	public Page<E> find(DataFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Find by disabled only is not supported, use concrete service instead.");
	}
}
