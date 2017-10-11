package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Identity processors should extend this super class.
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractIdentityProcessor extends CoreEventProcessor<IdmIdentityDto> {

	public AbstractIdentityProcessor(EventType... type) {
		super(type);
	}
	
}
