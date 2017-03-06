package eu.bcvsolutions.idm.core.model.service.api;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleRequestFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleRequest;

/**
 * Service for role request
 * @author svandav
 *
 */
public interface IdmRoleRequestService extends ReadWriteDtoService<IdmRoleRequestDto, IdmRoleRequest,  RoleRequestFilter> {
	
}
