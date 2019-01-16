package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.model.event.RoleCompositionEvent.RoleCompositionEventType;

/**
 * Events for incompatible role
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public class IncompatibleRoleEvent extends CoreEvent<IdmIncompatibleRoleDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported core event types
	 *
	 */
	public enum IncompatibleRoleEventType implements EventType {
		CREATE, 
		UPDATE,
		DELETE,
		NOTIFY
	}
	
	public IncompatibleRoleEvent(RoleCompositionEventType operation, IdmIncompatibleRoleDto content) {
		super(operation, content);
	}
	
	public IncompatibleRoleEvent(RoleCompositionEventType operation, IdmIncompatibleRoleDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}