package eu.bcvsolutions.idm.core.api.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterKey;

/**
 * Filter property is not supported.
 * 
 * @author Radek Tomiška
 * @since 10.4.0
 */
public class FilterNotSupportedException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;
	private final FilterKey filterKey;
	
	public FilterNotSupportedException(FilterKey filterKey) {
		super(CoreResultCode.FILTER_PROPERTY_NOT_SUPPORTED,
				ImmutableMap.of(
						"propertyName", filterKey.getName(),
						"entityClass", filterKey.getEntityClass().getSimpleName()
						));
		this.filterKey = filterKey;
	}
	
	public FilterKey getFilterKey() {
		return filterKey;
	}
}
