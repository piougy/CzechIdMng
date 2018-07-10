package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Token processors should implement this interface.
 * 
 * @author Radek Tomiška
 * @since 8.2.0
 */
public interface TokenProcessor extends EntityEventProcessor<IdmTokenDto> {
	
}
