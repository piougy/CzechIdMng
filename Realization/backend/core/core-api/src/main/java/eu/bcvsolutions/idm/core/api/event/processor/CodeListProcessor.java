package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListDto;

/**
 * Code list processors should implement this interface.
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public interface CodeListProcessor extends EntityEventProcessor<IdmCodeListDto> {
	
}
