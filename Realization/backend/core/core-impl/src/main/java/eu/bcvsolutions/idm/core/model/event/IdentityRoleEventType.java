package eu.bcvsolutions.idm.core.model.event;

import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;

/**
 * Supported identity events
 * 
 * @author Radek Tomiška
 *
 */
public enum IdentityRoleEventType implements EventType<IdmIdentityRole> {
	SAVE, DELETE // TODO: split SAVE to UPDATE / CREATE?
}
