package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.AbstractEntityEvent;
import eu.bcvsolutions.idm.core.api.event.RoleOperationType;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Events for role
 * 
 * @author Radek Tomiška
 *
 */
public class RoleEvent extends AbstractEntityEvent<IdmRole> {

	public RoleEvent(RoleOperationType operation, IdmRole content) {
		super(operation, content);
	}
	
	public RoleEvent(RoleOperationType operation, IdmRole content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}