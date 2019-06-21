package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Service for search and processing changes in assigned identity roles
 * 
 * @author Vít Švanda
 *
 */
public interface IdmRequestIdentityRoleService
		extends ReadWriteDtoService<IdmRequestIdentityRoleDto, IdmRequestIdentityRoleFilter>{

	/**
	 * Delete method. In this case we need to use method where parameter is DTO (not
	 * ID only, as in standard internalDelete method).
	 * 
	 * @param dto
	 * @param permission
	 * @return
	 */
	IdmRequestIdentityRoleDto deleteRequestIdentityRole(IdmRequestIdentityRoleDto dto, BasePermission... permission);

}
