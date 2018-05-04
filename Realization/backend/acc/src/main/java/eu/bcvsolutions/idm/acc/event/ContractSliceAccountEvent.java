package eu.bcvsolutions.idm.acc.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.acc.dto.AccContractSliceAccountDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for contract slice account
 * 
 * @author Svanda
 *
 */
public class ContractSliceAccountEvent extends CoreEvent<AccContractSliceAccountDto> {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Supported identity events
	 *
	 */
	public enum ContractSliceAccountEventType implements EventType {
		CREATE, 
		UPDATE, 
		DELETE;
	}
	
	public ContractSliceAccountEvent(ContractSliceAccountEventType operation, AccContractSliceAccountDto content) {
		super(operation, content);
	}
	
	public ContractSliceAccountEvent(ContractSliceAccountEventType operation, AccContractSliceAccountDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}