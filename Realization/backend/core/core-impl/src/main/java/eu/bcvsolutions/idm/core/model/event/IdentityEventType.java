package eu.bcvsolutions.idm.core.model.event;

import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Supported identity events
 * 
 * @author Radek Tomiška
 *
 */
public enum IdentityEventType implements EventType<IdmIdentity> {
	SAVE, DELETE, PASSWORD // TODO: split SAVE to UPDATE / CREATE?
}
