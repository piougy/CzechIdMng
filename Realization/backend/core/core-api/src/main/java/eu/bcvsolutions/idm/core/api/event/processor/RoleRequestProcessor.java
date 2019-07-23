package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Role request processors should implement this interface.
 * 
 * @author Vít Švanda
 *
 */
public interface RoleRequestProcessor extends EntityEventProcessor<IdmRoleRequestDto> {
	
	public static final String SYSTEM_STATE_RESOLVED_KEY = "system-state-resolved";
	
}
