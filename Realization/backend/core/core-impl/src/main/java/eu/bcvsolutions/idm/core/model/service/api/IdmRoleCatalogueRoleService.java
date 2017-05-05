package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.RoleCatalogueRoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;

/**
 * Default servervice for intersection between role catalogue and role
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface IdmRoleCatalogueRoleService extends ReadWriteEntityService<IdmRoleCatalogueRole, RoleCatalogueRoleFilter> {
	
	/**
	 * Get list of {@link IdmRoleCatalogueRole} by role given in parameter.
	 * 
	 * @param role
	 * @return
	 */
	List<IdmRoleCatalogueRole> getRoleCatalogueRoleByRole(IdmRole role);
	
	/**
	 * Get list of {@link IdmRoleCatalogueRole} by role catalogue given in parameter.
	 * 
	 * @param roleCatalogue
	 * @return
	 */
	List<IdmRoleCatalogueRole> getRoleCatalogueRoleByCatalogue(IdmRoleCatalogue roleCatalogue);
	
	/**
	 * Get list of {@link IdmRole} for role catalogue given in parameter.
	 * 
	 * @param roleCatalogue
	 * @return
	 */
	List<IdmRole> getRoleByRoleCatalogue(IdmRoleCatalogue roleCatalogue);
	
	/**
	 * Get list of {@link IdmRoleCatalogue} for role given in parameter.
	 * 
	 * @param roleCatalogue
	 * @return
	 */
	List<IdmRoleCatalogue> getRoleCatalogueByRole(IdmRole role);
	
}
