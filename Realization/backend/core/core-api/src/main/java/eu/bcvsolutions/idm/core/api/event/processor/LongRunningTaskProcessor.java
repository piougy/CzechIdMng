package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;

/**
 * LRT processors should implement this interface.
 * 
 * @author Radek Tomiška
 *
 */
public interface LongRunningTaskProcessor extends EntityEventProcessor<IdmLongRunningTaskDto> {
	
}
