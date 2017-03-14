package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;

/**
 * Event type (save, delete, etc.)
 * 
 * Its not strongly checked and coupled with entity type - only constant representation.
 * 
 * @see EntityEvent
 * @see AbstractEntityEvent
 * @author Radek Tomiška
 * 
 */
public interface EventType extends Serializable {

	/**
	 * Returns constant event name
	 * 
	 * @return
	 */
	String name();
}
