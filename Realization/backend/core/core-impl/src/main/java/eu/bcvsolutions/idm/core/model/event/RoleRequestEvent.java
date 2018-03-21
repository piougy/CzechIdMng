package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

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
		CREATE, UPDATE, DELETE, EXCECUTE // i know EXECUTE!, but it too late ...
	}
	
	public RoleRequestEvent(RoleRequestEventType operation, IdmRoleRequestDto content) {
		super(operation, content);
	}
	
	public RoleRequestEvent(RoleRequestEventType operation, IdmRoleRequestDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}