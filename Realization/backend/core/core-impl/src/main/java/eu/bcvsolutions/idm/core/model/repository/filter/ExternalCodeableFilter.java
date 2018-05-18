package eu.bcvsolutions.idm.core.model.repository.filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.ExternalCodeable;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.exception.EntityTypeNotExternalCodeableException;
import eu.bcvsolutions.idm.core.api.repository.filter.BaseFilterBuilder;

/**
 * Common filter on external code.
 * 
 * @author Radek Tomiška
 */
@Component
public class ExternalCodeableFilter<E extends AbstractEntity> extends BaseFilterBuilder<E, DataFilter> {

	public static final String PROPERTY_NAME = ExternalCodeable.PROPERTY_EXTERNAL_CODE;
	
	@Override
	public String getName() {
		return PROPERTY_NAME;
	}
	
	@Override
	public Predicate getPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder, DataFilter filter) {
		String externalCode = (String) filter.getData().getFirst(ExternalCodeable.PROPERTY_EXTERNAL_CODE);
		if (StringUtils.isEmpty(externalCode)) {
			return null;
		}
		if (!ExternalCodeable.class.isAssignableFrom(root.getJavaType())) {
			throw new EntityTypeNotExternalCodeableException(root.getJavaType().getCanonicalName());
		}
		//
		return builder.equal(root.get(ExternalCodeable.PROPERTY_EXTERNAL_CODE), externalCode);
	}
	
	@Override
	public Page<E> find(DataFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Find by external code only is not supported, use concrete service instead.");
	}
}
