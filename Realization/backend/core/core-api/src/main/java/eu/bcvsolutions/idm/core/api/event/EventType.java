package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;

/**
 * Event type (save, delete, etc.)
 * <p>
 * Its not strongly checked and coupled with entity type - only constant representation.
 * 
 * @author Radek Tomiška
 * @see EntityEvent
 * @see AbstractEntityEvent
 */
public interface EventType extends Serializable {

	/**
	 * Returns constant event name
	 * 
	 * @return
	 */
	String name();
}
