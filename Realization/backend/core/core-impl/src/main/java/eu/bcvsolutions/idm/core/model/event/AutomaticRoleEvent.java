package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for automatic roles
 * 
 * @author Radek Tomiška
 *
 */
public class AutomaticRoleEvent extends CoreEvent<AbstractIdmAutomaticRoleDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported entity events
	 *
	 */
	public enum AutomaticRoleEventType implements EventType {
		CREATE, UPDATE, DELETE
	}
	
	public AutomaticRoleEvent(AutomaticRoleEventType operation, AbstractIdmAutomaticRoleDto content) {
		super(operation, content);
	}
	
	public AutomaticRoleEvent(AutomaticRoleEventType operation, AbstractIdmAutomaticRoleDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}