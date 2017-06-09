package eu.bcvsolutions.idm.ic.domain;

import eu.bcvsolutions.idm.ic.filter.impl.IcContainsFilter;
import eu.bcvsolutions.idm.ic.filter.impl.IcEndsWithFilter;
import eu.bcvsolutions.idm.ic.filter.impl.IcEqualsFilter;
import eu.bcvsolutions.idm.ic.filter.impl.IcGreaterThanFilter;
import eu.bcvsolutions.idm.ic.filter.impl.IcLessThanFilter;
import eu.bcvsolutions.idm.ic.filter.impl.IcStartsWithFilter;

/**
 * Type of filter operation
 * 
 * @author Svanda
 *
 */
public enum IcFilterOperationType {

	
	EQUAL_TO(IcEqualsFilter.class),
	CONTAINS(IcContainsFilter.class),
	GREATER_THAN(IcGreaterThanFilter.class),
	LESS_THAN(IcLessThanFilter.class),
	ENDS_WITH(IcEndsWithFilter.class),
	STARTS_WITH(IcStartsWithFilter.class);
	
	private Class<?> implementation;
	
	private IcFilterOperationType(Class<?> implementation) {
		this.implementation = implementation;
	}

	public Class<?> getImplementation() {
		return implementation;
	}
	
}
