package eu.bcvsolutions.idm.core.model.repository.filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.filter.BaseFilterBuilder;

/**
 * Common filter on entity uuid
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class UuidFilter<E extends AbstractEntity> extends BaseFilterBuilder<E, DataFilter> {

	public static final String PROPERTY_NAME = DataFilter.PARAMETER_ID;
	
	@Override
	public String getName() {
		return PROPERTY_NAME;
	}
	
	@Override
	public Predicate getPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder, DataFilter filter) {
		if (filter.getId() == null) {
			return null;
		}
		return builder.equal(root.get(AbstractEntity_.id), filter.getId());
	}	

	@Override
	public Page<E> find(DataFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Find by uuid only is not supported, use LookupService instead.");
	}
}
