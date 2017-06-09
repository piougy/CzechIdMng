package eu.bcvsolutions.idm.acc.event;

import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for synchronization
 * @author svandav
 *
 */
public enum SynchronizationEventType implements EventType {
	
	START,
	START_ITEM,
	CANCEL;
}
