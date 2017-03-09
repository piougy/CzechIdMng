package eu.bcvsolutions.idm.core.model.service.api;

import java.util.UUID;

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

	void startRequest(UUID requestId);

	/**
	 * Internal start request. Not accessible from REST. 
	 * @param requestId
	 * @param checkRight - If is true, then will be check right for immediately execution (if is requires)
	 */
	void startRequestInternal(UUID requestId, boolean checkRight);
	
}
