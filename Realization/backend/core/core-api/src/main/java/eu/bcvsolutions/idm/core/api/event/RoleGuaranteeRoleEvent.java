package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeRoleDto;

/**
 * Events for role guarantee by role
 * 
 * @author Radek Tomiška
 *
 */
public class RoleGuaranteeRoleEvent extends CoreEvent<IdmRoleGuaranteeRoleDto> {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Supported event types
	 */
	public enum RoleGuaranteeRoleEventType implements EventType {
		CREATE, UPDATE, DELETE
	}

	public RoleGuaranteeRoleEvent(RoleGuaranteeRoleEventType operation, IdmRoleGuaranteeRoleDto content) {
		super(operation, content);
	}
	
	public RoleGuaranteeRoleEvent(RoleGuaranteeRoleEventType operation, IdmRoleGuaranteeRoleDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}