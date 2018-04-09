package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Event for {@link IdmContractGuaranteeDto} save and update.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class ContractGuaranteeEvent extends CoreEvent<IdmContractGuaranteeDto> {

	private static final long serialVersionUID = -428359305246018679L;

	/**
	 * Supported events
	 *
	 */
	public enum ContractGuaranteeEventType implements EventType {
		CREATE, UPDATE, DELETE, NOTIFY
	}
	
	public ContractGuaranteeEvent(ContractGuaranteeEventType type, IdmContractGuaranteeDto content) {
		super(type, content);
	}
	
	public ContractGuaranteeEvent(ContractGuaranteeEventType type, IdmContractGuaranteeDto content, Map<String, Serializable> properties) {
		super(type, content, properties);
	}
}
