package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Role processors should extend this super class.
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractRoleProcessor extends CoreEventProcessor<IdmRoleDto> {

	public AbstractRoleProcessor(EventType... type) {
		super(type);
	}
	
}
