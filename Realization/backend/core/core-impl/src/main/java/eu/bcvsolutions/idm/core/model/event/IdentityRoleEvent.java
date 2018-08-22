package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for identity roles
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityRoleEvent extends CoreEvent<IdmIdentityRoleDto> {

	private static final long serialVersionUID = 1L;
	//
	public static final String PROPERTY_PROCESSED_ROLES = "idm:processed-roles"; // event property, contains Set<UUID>

	/**
	 * Supported identity events
	 *
	 */
	public enum IdentityRoleEventType implements EventType {
		CREATE, UPDATE, DELETE, NOTIFY
	}
	
	public IdentityRoleEvent(IdentityRoleEventType operation, IdmIdentityRoleDto content) {
		super(operation, content);
	}
	
	public IdentityRoleEvent(IdentityRoleEventType operation, IdmIdentityRoleDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}