package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for role
 * 
 * @author Radek Tomiška
 *
 */
public class RoleEvent extends CoreEvent<IdmRoleDto> {
	
	private static final long serialVersionUID = 1L;
	//
	public static final String PROPERTY_PROCESSED_ROLES = "idm:processed-roles"; // event property, contains Set<UUID> of processed roles (used for crole composition processing for the prevent cycles)

	/**
	 * Supported event types
	 */
	public enum RoleEventType implements EventType {
		CREATE, 
		UPDATE, 
		DELETE, 
		EAV_SAVE,
		NOTIFY,
		DUPLICATE // duplicate a role ("clone" keyword is not used - role is not cloned 1:1 - depends on registered processors)
	}

	public RoleEvent(RoleEventType operation, IdmRoleDto content) {
		super(operation, content);
	}
	
	public RoleEvent(RoleEventType operation, IdmRoleDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}