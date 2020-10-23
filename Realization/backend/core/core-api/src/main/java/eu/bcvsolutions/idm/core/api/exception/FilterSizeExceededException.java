package eu.bcvsolutions.idm.core.api.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterKey;

/**
 * Filter size exceeded - more filter values than supported.
 * 
 * @author Radek Tomiška
 * @since 10.6.0
 */
public class FilterSizeExceededException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;
	private final FilterKey filterKey;
	private final int currentSize;
	private final int maximum;
	
	public FilterSizeExceededException(FilterKey filterKey, int currentSize, int maximum) {
		super(CoreResultCode.FILTER_SIZE_EXCEEDED,
				ImmutableMap.of(
						"propertyName", filterKey.getName(),
						"entityClass", filterKey.getEntityClass().getSimpleName(),
						"currentSize", String.valueOf(currentSize),
						"maximum", String.valueOf(maximum)
						));
		this.filterKey = filterKey;
		this.currentSize = currentSize;
		this.maximum = maximum;
	}
	
	public FilterKey getFilterKey() {
		return filterKey;
	}
	
	public int getCurrentSize() {
		return currentSize;
	}
	
	public int getMaximum() {
		return maximum;
	}
}
