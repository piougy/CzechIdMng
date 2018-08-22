package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for role composition
 * 
 * @author Radek Tomiška
 * @since 9.0.0
 */
public class RoleCompositionEvent extends CoreEvent<IdmRoleCompositionDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported core identity events
	 *
	 */
	public enum RoleCompositionEventType implements EventType {
		CREATE, 
		UPDATE, // prolong expiration / disable
		DELETE,
		NOTIFY
	}
	
	public RoleCompositionEvent(RoleCompositionEventType operation, IdmRoleCompositionDto content) {
		super(operation, content);
	}
	
	public RoleCompositionEvent(RoleCompositionEventType operation, IdmRoleCompositionDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}