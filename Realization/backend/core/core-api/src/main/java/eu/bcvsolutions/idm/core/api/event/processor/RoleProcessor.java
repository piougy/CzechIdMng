package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Role processors should implement this interface.
 * 
 * @author Radek Tomiška
 *
 */
public interface RoleProcessor extends EntityEventProcessor<IdmRoleDto> {
	
}
