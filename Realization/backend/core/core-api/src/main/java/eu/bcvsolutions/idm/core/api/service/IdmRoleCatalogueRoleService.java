package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCatalogueRoleFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Default service for relation between role catalogue and role
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface IdmRoleCatalogueRoleService extends 
		ReadWriteDtoService<IdmRoleCatalogueRoleDto, IdmRoleCatalogueRoleFilter>,
		AuthorizableService<IdmRoleCatalogueRoleDto> {
	
	/**
	 * Get list of {@link IdmRoleCatalogueRoleDto} by role given in parameter.
	 * 
	 * @param role
	 * @return
	 */
	List<IdmRoleCatalogueRoleDto> findAllByRole(UUID roleId);
	
	/**
	 * Get list of {@link IdmRoleCatalogueRoleDto} by role catalogue given in parameter.
	 * 
	 * @param roleCatalogue
	 * @return
	 */
	List<IdmRoleCatalogueRoleDto> findAllByRoleCatalogue(UUID roleCatalogueId);	
}
