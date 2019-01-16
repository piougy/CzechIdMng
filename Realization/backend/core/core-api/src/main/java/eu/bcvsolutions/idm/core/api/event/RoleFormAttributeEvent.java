package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleFormAttributeDto;

/**
 * Events for relation between role and definition of form-attribution. Is elementary part
 * of role form "subdefinition".
 * 
 * @author Vít Švanda
 *
 */
public class RoleFormAttributeEvent extends CoreEvent<IdmRoleFormAttributeDto> {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Supported event types
	 */
	public enum RoleFormAttributeEventType implements EventType {
		CREATE, UPDATE, DELETE
	}

	public RoleFormAttributeEvent(RoleFormAttributeEventType operation, IdmRoleFormAttributeDto content) {
		super(operation, content);
	}
	
	public RoleFormAttributeEvent(RoleFormAttributeEventType operation, IdmRoleFormAttributeDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}