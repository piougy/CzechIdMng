package eu.bcvsolutions.idm.core.eav.api.event.processor;

import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;

/**
 * All form instance processors should implement this interface.
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public interface FormInstanceProcessor extends EntityEventProcessor<IdmFormInstanceDto> {
	
}
