package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Role composition processors should implement this interface.
 * 
 * @author Radek Tomiška
 * @since 9.0.0
 */
public interface RoleCompositionProcessor extends EntityEventProcessor<IdmRoleCompositionDto> {
	
}
