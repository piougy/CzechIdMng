package eu.bcvsolutions.idm.vs.domain;

import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Type of request on virtual system
 * 
 * @author Svanda
 *
 */
public enum VsRequestEventType implements EventType {
	
	CREATE,
	UPDATE,
	DELETE,
	DISABLE,
	ENABLE,
	RESET_PASSWORD;
	
}
