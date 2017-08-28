package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.ConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for concept role request
 * @author svandav
 *
 */
public interface IdmConceptRoleRequestService extends 
		ReadWriteDtoService<IdmConceptRoleRequestDto, ConceptRoleRequestFilter>,
		AuthorizableService<IdmConceptRoleRequestDto>{
	
	public static final String IDENTITY_CONTRACT_FIELD = "identityContract";
	public static final String ROLE_REQUEST_FIELD = "roleRequest";

	void addToLog(Loggable logItem, String text);
	
	/**
	 * Finds all concepts for this request
	 * 
	 * @param roleRequestId
	 * @return
	 */
	List<IdmConceptRoleRequestDto> findAllByRoleRequest(UUID roleRequestId);
	
}
