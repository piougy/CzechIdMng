package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceGuaranteeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Event for {@link IdmContractSliceGuaranteeDto} save and update.
 * 
 * @author svandav
 *
 */

public class ContractSliceGuaranteeEvent extends CoreEvent<IdmContractSliceGuaranteeDto> {



	private static final long serialVersionUID = 1L;

	/**
	 * Supported events
	 *
	 */
	public enum ContractSliceGuaranteeEventType implements EventType {
		CREATE, UPDATE, DELETE, NOTIFY
	}
	
	public ContractSliceGuaranteeEvent(ContractSliceGuaranteeEventType type, IdmContractSliceGuaranteeDto content) {
		super(type, content);
	}
	
	public ContractSliceGuaranteeEvent(ContractSliceGuaranteeEventType type, IdmContractSliceGuaranteeDto content, Map<String, Serializable> properties) {
		super(type, content, properties);
	}
}
