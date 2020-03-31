package eu.bcvsolutions.idm.core.eav.api.event.processor;

import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;

/**
 * All form projection processors should implement this interface.
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
public interface FormProjectionProcessor extends EntityEventProcessor<IdmFormProjectionDto> {
	
}
