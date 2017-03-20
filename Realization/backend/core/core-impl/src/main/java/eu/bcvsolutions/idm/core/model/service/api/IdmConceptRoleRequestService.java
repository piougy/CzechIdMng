package eu.bcvsolutions.idm.core.model.service.api;

import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;

/**
 * Service for concept role request
 * @author svandav
 *
 */
public interface IdmConceptRoleRequestService extends ReadWriteDtoService<IdmConceptRoleRequestDto, IdmConceptRoleRequest, ConceptRoleRequestFilter> {
	
	public static final String IDENTITY_CONTRACT_FIELD = "identityContract";
	public static final String ROLE_REQUEST_FIELD = "roleRequest";

	void addToLog(Loggable logItem, String text);
	
}
