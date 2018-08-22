package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmProfileDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Profile processors should implement this interface.
 * 
 * @author Radek Tomiška
 * @since 9.0.0
 */
public interface ProfileProcessor extends EntityEventProcessor<IdmProfileDto> {
	
}
