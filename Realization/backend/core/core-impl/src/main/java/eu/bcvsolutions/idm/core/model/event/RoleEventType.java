package eu.bcvsolutions.idm.core.model.event;

import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Supported role events
 * 
 * @author Radek Tomiška
 *
 */
public enum RoleEventType implements EventType<IdmRole> {
	DELETE
}
