package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Contract position processors should implement this interface.
 * 
 * @author Radek Tomiška
 *
 */
public interface ContractPositionProcessor extends EntityEventProcessor<IdmContractPositionDto> {
	
}
