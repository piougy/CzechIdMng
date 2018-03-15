package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleRequestType;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleRequestFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for automatic role request
 * 
 * @author svandav
 * 
 */
public interface IdmAutomaticRoleRequestService
		extends ReadWriteDtoService<IdmAutomaticRoleRequestDto, IdmAutomaticRoleRequestFilter>,
		AuthorizableService<IdmAutomaticRoleRequestDto>, RequestService<IdmAutomaticRoleRequestDto> {

	/**
	 * Creates and executes request for delete of the given automatic role
	 * @param automaticRole
	 * @param type
	 */
	void deleteAutomaticRole(AbstractIdmAutomaticRoleDto automaticRole, AutomaticRoleRequestType type);

	/**
	 * Creates and executes request for create new tree automatic role
	 * @param tree automatic role
	 */
	IdmRoleTreeNodeDto createTreeAutomaticRole(IdmRoleTreeNodeDto dto);

}
