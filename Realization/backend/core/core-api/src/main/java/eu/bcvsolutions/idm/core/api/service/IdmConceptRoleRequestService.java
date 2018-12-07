package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for concept role request
 * 
 * @author svandav
 *
 */
public interface IdmConceptRoleRequestService
		extends ReadWriteDtoService<IdmConceptRoleRequestDto, IdmConceptRoleRequestFilter>,
		AuthorizableService<IdmConceptRoleRequestDto> {

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

	/**
	 * Set concept state to CANCELED and stop workflow process (connected to this
	 * concept)
	 * 
	 * @param dto
	 */
	IdmConceptRoleRequestDto cancel(IdmConceptRoleRequestDto dto);

	/**
	 * Return form instance for given concept. Values contains changes evaluated
	 * against the identity-role form values.
	 * 
	 * @param dto
	 * @param checkChanges If true, then changes against the identity role will be evaluated.
	 * @return
	 */
	IdmFormInstanceDto getRoleAttributeValues(IdmConceptRoleRequestDto dto, boolean checkChanges);

	/**
	 * Validate form attributes for given concept
	 * @param concept
	 * @return
	 */
	List<InvalidFormAttributeDto> validateFormAttributes(IdmConceptRoleRequestDto concept);

}
