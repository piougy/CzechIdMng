package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Incompatible role processors should implement this interface.
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public interface IncompatibleRoleProcessor extends EntityEventProcessor<IdmIncompatibleRoleDto> {
	
}
