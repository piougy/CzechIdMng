package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Event for {@link IdmContractPositionDto} save and update.
 * 
 * @author Radek Tomiška
 *
 */
public class ContractPositionEvent extends CoreEvent<IdmContractPositionDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported events
	 *
	 */
	public enum ContractPositionEventType implements EventType {
		CREATE, UPDATE, DELETE, NOTIFY
	}
	
	public ContractPositionEvent(ContractPositionEventType type, IdmContractPositionDto content) {
		super(type, content);
	}
	
	public ContractPositionEvent(ContractPositionEventType type, IdmContractPositionDto content, Map<String, Serializable> properties) {
		super(type, content, properties);
	}
}
