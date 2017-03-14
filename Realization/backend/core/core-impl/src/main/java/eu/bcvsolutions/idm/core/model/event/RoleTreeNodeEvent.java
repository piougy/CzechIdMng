package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleTreeNodeDto;

/**
 * Events for automatic roles
 * 
 * @author Radek Tomiška
 *
 */
public class RoleTreeNodeEvent extends CoreEvent<IdmRoleTreeNodeDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported identity events
	 *
	 */
	public enum RoleTreeNodeEventType implements EventType {
		CREATE, UPDATE, DELETE
	}
	
	public RoleTreeNodeEvent(RoleTreeNodeEventType operation, IdmRoleTreeNodeDto content) {
		super(operation, content);
	}
	
	public RoleTreeNodeEvent(RoleTreeNodeEventType operation, IdmRoleTreeNodeDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}