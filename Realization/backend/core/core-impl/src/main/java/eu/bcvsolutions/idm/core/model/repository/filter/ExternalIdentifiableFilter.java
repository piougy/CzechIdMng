package eu.bcvsolutions.idm.core.model.repository.filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.exception.EntityTypeNotExternalIdentifiableException;
import eu.bcvsolutions.idm.core.api.repository.filter.BaseFilterBuilder;

/**
 * Common filter on external identifier.
 * 
 * @author Radek Tomiška
 */
@Component
public class ExternalIdentifiableFilter<E extends AbstractEntity> extends BaseFilterBuilder<E, DataFilter> {

	public static final String PROPERTY_NAME = ExternalIdentifiable.PROPERTY_EXTERNAL_ID;
	
	@Override
	public String getName() {
		return PROPERTY_NAME;
	}
	
	@Override
	public Predicate getPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder, DataFilter filter) {
		String externalId = (String) filter.getData().getFirst(ExternalIdentifiable.PROPERTY_EXTERNAL_ID);
		if (StringUtils.isEmpty(externalId)) {
			return null;
		}
		if (!ExternalIdentifiable.class.isAssignableFrom(root.getJavaType())) {
			throw new EntityTypeNotExternalIdentifiableException(root.getJavaType().getCanonicalName());
		}
		//
		return builder.equal(root.get(ExternalIdentifiable.PROPERTY_EXTERNAL_ID), externalId);
	}
	
	@Override
	public Page<E> find(DataFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Find by external identifier only is not supported, use concrete service instead.");
	}
}
