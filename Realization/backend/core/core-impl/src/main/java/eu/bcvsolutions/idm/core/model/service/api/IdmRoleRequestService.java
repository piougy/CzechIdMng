package eu.bcvsolutions.idm.core.model.service.api;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleRequestFilter;

/**
 * Service for role request
 * @author svandav
 *
 */
public interface IdmRoleRequestService extends ReadWriteDtoService<IdmRoleRequestDto,  RoleRequestFilter> {

	/**
	 * Start approval process for given reqeust
	 * @param requestId
	 */
	void startRequest(UUID requestId);

	/**
	 * Internal start request. Not accessible from REST. 
	 * @param requestId
	 * @param checkRight - If is true, then will be check right for immediately execution (if is requires)
	 */
	void startRequestInternal(UUID requestId, boolean checkRight);

	/**
	 * Add record to request log
	 * @param logItem
	 * @param text
	 */
	void addToLog(Loggable logItem, String text);

	/**
	 * Realization of request (applying the requested changes) 
	 * @param requestId
	 * @return
	 */
	IdmRoleRequestDto executeRequest(UUID requestId);

	/**
	 * Start approval procces for this request
	 * @param request
	 * @param checkRight
	 * @param event
	 * @param wfDefinition
	 * @return
	 */
	boolean startApprovalProcess(IdmRoleRequestDto request, boolean checkRight, EntityEvent<IdmRoleRequestDto> event, String wfDefinition);

	/**
	 * Set request state to CANCELED and stop workflow process (connected to this request)
	 * @param dto
	 */
	void cancel(IdmRoleRequestDto dto);

	/**
	 * Internal start request. Start in new transaction 
	 * @param requestId
	 * @param checkRight - If is true, then will be check right for immediately execution (if is requires)
	 */
	void startRequestNewTransactional(UUID requestId, boolean checkRight);
}
