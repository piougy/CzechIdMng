package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeRoleDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Role guarantee by role processors should implement this interface.
 * 
 * @author Radek Tomiška
 *
 */
public interface RoleGuaranteeRoleProcessor extends EntityEventProcessor<IdmRoleGuaranteeRoleDto> {
	
}
