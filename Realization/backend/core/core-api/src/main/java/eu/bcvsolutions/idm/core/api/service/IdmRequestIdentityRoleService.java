package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for search and processing changes in assigned identity roles
 * 
 * @author Vít Švanda
 *
 */
public interface IdmRequestIdentityRoleService
		extends ReadWriteDtoService<IdmRequestIdentityRoleDto, IdmRequestIdentityRoleFilter>,
		AuthorizableService<IdmConceptRoleRequestDto> {

	IdmRequestIdentityRoleDto deleteRequestIdentityRole(IdmRequestIdentityRoleDto dto, BasePermission... permission);

}
