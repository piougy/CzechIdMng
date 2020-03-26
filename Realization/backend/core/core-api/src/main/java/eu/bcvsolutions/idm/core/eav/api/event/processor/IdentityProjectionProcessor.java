package eu.bcvsolutions.idm.core.eav.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.projection.IdmIdentityProjectionDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * All identity projection processors should implement this interface.
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
public interface IdentityProjectionProcessor extends EntityEventProcessor<IdmIdentityProjectionDto> {
	
}
