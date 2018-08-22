package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;

/**
 * Events for role guarantee by identity
 * 
 * @author Radek Tomiška
 *
 */
public class RoleGuaranteeEvent extends CoreEvent<IdmRoleGuaranteeDto> {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Supported event types
	 */
	public enum RoleGuaranteeEventType implements EventType {
		CREATE, UPDATE, DELETE
	}

	public RoleGuaranteeEvent(RoleGuaranteeEventType operation, IdmRoleGuaranteeDto content) {
		super(operation, content);
	}
	
	public RoleGuaranteeEvent(RoleGuaranteeEventType operation, IdmRoleGuaranteeDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}