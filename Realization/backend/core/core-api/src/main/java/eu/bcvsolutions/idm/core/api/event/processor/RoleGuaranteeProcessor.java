package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Role guarantee by identity processors should implement this interface.
 * 
 * @author Radek Tomiška
 *
 */
public interface RoleGuaranteeProcessor extends EntityEventProcessor<IdmRoleGuaranteeDto> {
	
}
