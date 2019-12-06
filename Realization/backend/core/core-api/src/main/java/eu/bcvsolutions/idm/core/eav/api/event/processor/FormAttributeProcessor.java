package eu.bcvsolutions.idm.core.eav.api.event.processor;

import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;

/**
 * All form attribute processors should implement this interface.
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public interface FormAttributeProcessor extends EntityEventProcessor<IdmFormAttributeDto> {
	
}
