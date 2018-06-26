package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for contract slice
 * 
 * @author svandav
 *
 */
public class ContractSliceEvent extends CoreEvent<IdmContractSliceDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported events
	 *
	 */
	public enum ContractSliceEventType implements EventType {
		CREATE, UPDATE, DELETE, EAV_SAVE, NOTIFY
	}
	
	public ContractSliceEvent(ContractSliceEventType operation, IdmContractSliceDto content) {
		super(operation, content);
	}
	
	public ContractSliceEvent(ContractSliceEventType operation, IdmContractSliceDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}