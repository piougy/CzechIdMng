package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Events for role
 * 
 * @author Radek Tomiška
 *
 */
public class RoleEvent extends CoreEvent<IdmRole> {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Supported event types
	 */
	public enum RoleEventType implements EventType {
		DELETE
	}

	public RoleEvent(RoleEventType operation, IdmRole content) {
		super(operation, content);
	}
	
	public RoleEvent(RoleEventType operation, IdmRole content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}