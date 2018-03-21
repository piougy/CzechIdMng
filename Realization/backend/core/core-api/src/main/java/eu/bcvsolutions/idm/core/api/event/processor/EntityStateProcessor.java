package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Entity state processors should implement this interface.
 * 
 * @author Radek Tomiška
 *
 */
public interface EntityStateProcessor extends EntityEventProcessor<IdmEntityStateDto> {
	
}
