package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Contract slice processors should implement this interface.
 * 
 * @author svandav
 *
 */
public interface ContractSliceProcessor extends EntityEventProcessor<IdmContractSliceDto> {
	
}
