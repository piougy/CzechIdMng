package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestByIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.ResolvedIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.core.security.api.service.ExceptionProcessable;

/**
 * Service for role request
 * 
 * @author svandav
 * 
 */
public interface IdmRoleRequestService extends 
		ReadWriteDtoService<IdmRoleRequestDto, IdmRoleRequestFilter>, AuthorizableService<IdmRoleRequestDto>,
		ExceptionProcessable<IdmRoleRequestDto> {


	/**
	 * Start approval process for given request. Request approving will be started
	 * in new transaction.
	 * 
	 * @param requestId
	 * @param checkRight
	 *            - If is true, then will be check right for immediately execution
	 *            (if is requires)
	 */
	IdmRoleRequestDto startRequest(UUID requestId, boolean checkRight);
	
	/**
	 * Start approval process for given request. Request approving will be started
	 * in new transaction.
	 * 
	 * @param event
	 * @return
	 */
	IdmRoleRequestDto startRequest(EntityEvent<IdmRoleRequestDto> event);

	/**
	 * Internal start request. Not accessible from REST.
	 * 
	 * @param requestId
	 * @param checkRight
	 *            - If is true, then will be check right for immediately execution
	 *            (if is requires)
	 */
	IdmRoleRequestDto startRequestInternal(UUID requestId, boolean checkRight);
	
	/**
	 * Internal start request. Not accessible from REST.
	 * 
	 * @see PriorityType#IMMEDIATE
	 * @param requestId
	 * @param checkRight- If is true, then will be check right for immediately execution (if is requires)
	 * @param immediate - will be executed synchronously
	 */
	IdmRoleRequestDto startRequestInternal(UUID requestId, boolean checkRight, boolean immediate);
	
	/**
	 * Internal start request. 
	 * 
	 * @param event
	 * @return
	 */
	IdmRoleRequestDto startRequestInternal(EntityEvent<IdmRoleRequestDto> event);

	/**
	 * Add record to request log
	 * 
	 * @param logItem
	 * @param text
	 */
	void addToLog(Loggable logItem, String text);

	/**
	 * Realization of request (applying the requested changes).
	 * 
	 * @param requestId
	 * @return
	 */
	IdmRoleRequestDto executeRequest(UUID requestId);
	
	/**
	 * Realization of request (applying the requested changes).
	 * 
	 * @param requestEvent
	 * @return
	 */
	IdmRoleRequestDto executeRequest(EntityEvent<IdmRoleRequestDto> requestEvent);
	
	/**
	 * Execute concepts via request - usable programmatically, where identity roles are added / updated / removed.
	 * 
	 * @param applicant
	 * @param concepts
	 * @return
	 * @since 9.6.0
	 */
	IdmRoleRequestDto executeConceptsImmediate(UUID applicant, List<IdmConceptRoleRequestDto> concepts);

	/**
	 * Start approval procces for this request.
	 * 
	 * @param request
	 * @param checkRight
	 * @param event
	 * @param wfDefinition
	 * @return
	 */
	boolean startApprovalProcess(IdmRoleRequestDto request, boolean checkRight, EntityEvent<IdmRoleRequestDto> event,
			String wfDefinition);

	/**
	 * Set request state to CANCELED and stop workflow process (connected to this
	 * request)
	 * 
	 * @param dto
	 */
	void cancel(IdmRoleRequestDto dto);

	/**
	 * Creates request by given contract. If some roles are given, then will be
	 * create role request concepts for them.
	 * 
	 * @param contract
	 * @param roles
	 * @return
	 */
	IdmRoleRequestDto createRequest(IdmIdentityContractDto contract, IdmRoleDto... roles);

	/**
	 * Copy roles from identity, by whole identity or contract or selected identity roles.
	 *
	 * @param requestByIdentityDto
	 * @return
	 */
	IdmRoleRequestDto copyRolesByIdentity(IdmRoleRequestByIdentityDto requestByIdentityDto);

	/**
	 * Request and concept validation
	 *  
	 * @param request
	 */
	void validate(IdmRoleRequestDto request);

	/**
	 * Incompatible roles are resolved from currently assigned identity roles and the current request concepts.
	 * 
	 * @param request
	 * @param permissions
	 * @return
	 */
	Set<ResolvedIncompatibleRoleDto> getIncompatibleRoles(IdmRoleRequestDto request, IdmBasePermission... permissions);

}
