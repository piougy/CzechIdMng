package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.AbstractEntityEvent;
import eu.bcvsolutions.idm.core.api.event.IdentityOperationType;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Events for identity
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityEvent extends AbstractEntityEvent<IdmIdentity> {

	public IdentityEvent(IdentityOperationType operation, IdmIdentity content) {
		super(operation, content);
	}
	
	public IdentityEvent(IdentityOperationType operation, IdmIdentity content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}