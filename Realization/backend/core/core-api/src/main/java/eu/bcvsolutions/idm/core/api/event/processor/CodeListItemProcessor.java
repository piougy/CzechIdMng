package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListItemDto;

/**
 * Code list item processors should implement this interface.
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public interface CodeListItemProcessor extends EntityEventProcessor<IdmCodeListItemDto> {
	
}
