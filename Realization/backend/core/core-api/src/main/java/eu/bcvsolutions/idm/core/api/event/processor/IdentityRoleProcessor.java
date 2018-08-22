package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Assigned role (~identity role) processors should implement this interface.
 * 
 * @author Radek Tomiška
 *
 */
public interface IdentityRoleProcessor extends EntityEventProcessor<IdmIdentityRoleDto> {
	
}
