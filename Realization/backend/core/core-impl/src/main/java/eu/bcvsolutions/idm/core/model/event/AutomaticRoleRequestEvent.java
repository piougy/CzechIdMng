package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleRequestDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for automatic role request
 * @author svandav
 *
 */
public class AutomaticRoleRequestEvent extends CoreEvent<IdmAutomaticRoleRequestDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported events
	 *
	 */
	public enum AutomaticRoleRequestEventType implements EventType {
		CREATE, UPDATE, DELETE, EXCECUTE
	}
	
	public AutomaticRoleRequestEvent(AutomaticRoleRequestEventType operation, IdmAutomaticRoleRequestDto content) {
		super(operation, content);
	}
	
	public AutomaticRoleRequestEvent(AutomaticRoleRequestEventType operation, IdmAutomaticRoleRequestDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}