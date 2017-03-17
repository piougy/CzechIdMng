package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleRequestDto;

/**
 * Events for role request
 * @author svandav
 *
 */
public class RoleRequestEvent extends CoreEvent<IdmRoleRequestDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported events
	 *
	 */
	public enum RoleRequestEventType implements EventType {
		CREATE, UPDATE, DELETE, EXCECUTE
	}
	
	public RoleRequestEvent(RoleRequestEventType operation, IdmRoleRequestDto content) {
		super(operation, content);
	}
	
	public RoleRequestEvent(RoleRequestEventType operation, IdmRoleRequestDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}