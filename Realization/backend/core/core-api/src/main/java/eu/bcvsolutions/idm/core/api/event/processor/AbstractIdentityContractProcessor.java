package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Identity contract processors should extend this super class.
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractIdentityContractProcessor extends CoreEventProcessor<IdmIdentityContractDto> {

	public AbstractIdentityContractProcessor(EventType... type) {
		super(type);
	}
	
}
