package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for identity contract
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityContractEvent extends CoreEvent<IdmIdentityContractDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported identity events
	 *
	 */
	public enum IdentityContractEventType implements EventType {
		CREATE, UPDATE, DELETE, EAV_SAVE, NOTIFY
	}
	
	public IdentityContractEvent(IdentityContractEventType operation, IdmIdentityContractDto content) {
		super(operation, content);
	}
	
	public IdentityContractEvent(IdentityContractEventType operation, IdmIdentityContractDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}