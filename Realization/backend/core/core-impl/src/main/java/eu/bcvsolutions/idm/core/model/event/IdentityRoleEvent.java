package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;

/**
 * Events for identity roles
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityRoleEvent extends CoreEvent<IdmIdentityRole> {

	/**
	 * Supported identity events
	 *
	 */
	public enum IdentityRoleEventType implements EventType<IdmIdentityRole> {
		SAVE, DELETE // TODO: split SAVE to UPDATE / CREATE?
	}
	
	public IdentityRoleEvent(IdentityRoleEventType operation, IdmIdentityRole content) {
		super(operation, content);
	}
	
	public IdentityRoleEvent(IdentityRoleEventType operation, IdmIdentityRole content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}