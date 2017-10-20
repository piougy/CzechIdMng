package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Identity contract processors should implement this interface.
 * 
 * @author Radek Tomiška
 *
 */
public interface IdentityContractProcessor extends EntityEventProcessor<IdmIdentityContractDto> {
	
}
