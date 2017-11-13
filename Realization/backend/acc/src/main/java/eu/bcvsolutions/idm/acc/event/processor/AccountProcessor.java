package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Acc account's processors should implement this interface.
 * 
 * @author svandav
 *
 */
public interface AccountProcessor extends EntityEventProcessor<AccAccountDto> {
	
}
