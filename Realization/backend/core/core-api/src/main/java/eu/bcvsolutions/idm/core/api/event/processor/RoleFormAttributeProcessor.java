package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleFormAttributeDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Processor for relation between role and definition of form-attribution. Is elementary part
 * of role form "subdefinition".
 * 
 * @author Vít Švanda
 *
 */
public interface RoleFormAttributeProcessor extends EntityEventProcessor<IdmRoleFormAttributeDto> {
	
}
