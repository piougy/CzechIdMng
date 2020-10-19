package eu.bcvsolutions.idm.acc.domain;

import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Empty provisioning type - used in filter.
 * 
 * @author Radek Tomiška
 * @since 10.6.0
 */
public enum EmptyProvisioningType implements EventType {
	
	EMPTY, // ~ without attributes
	NON_EMPTY, // ~ with attributes
	NOT_PROCESSED; // ~ attributes not computed
}
