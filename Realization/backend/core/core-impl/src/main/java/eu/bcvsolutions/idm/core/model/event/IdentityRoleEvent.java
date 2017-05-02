package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityRoleDto;

/**
 * Events for identity roles
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityRoleEvent extends CoreEvent<IdmIdentityRoleDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported identity events
	 *
	 */
	public enum IdentityRoleEventType implements EventType {
		CREATE, UPDATE, DELETE
	}
	
	public IdentityRoleEvent(IdentityRoleEventType operation, IdmIdentityRoleDto content) {
		super(operation, content);
	}
	
	public IdentityRoleEvent(IdentityRoleEventType operation, IdmIdentityRoleDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}