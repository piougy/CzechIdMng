package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.dto.filter.RoleCatalogueRoleFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;

/**
 * Default repository for intersection table between role catalogue and role
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
public interface IdmRoleCatalogueRoleRepository extends AbstractEntityRepository<IdmRoleCatalogueRole> {
	
	@Query(value = "select e from IdmRoleCatalogueRole e " +
	        " where" +
	        " (?#{[0].roleCatalogueId} is null or e.roleCatalogue.id = ?#{[0].roleCatalogueId})" + 
	        " and (?#{[0].roleId} is null or e.role.id = ?#{[0].roleId})" + 
	        " and (?#{[0].roleCatalogueCode} is null or e.roleCatalogue.code = ?#{[0].roleCatalogueCode})")
	Page<IdmRoleCatalogueRole> find(RoleCatalogueRoleFilter filter, Pageable pageable);
	
	/**
	 * Get all roleCatalogueRole for role given in parameter
	 * 
	 * @param role
	 * @return
	 */
	List<IdmRoleCatalogueRole> findAllByRole_Id(@Param(value = "roleId") UUID roleId);
	
	/**
	 * Get all roleCatalogueRole for roleCatalogue given in parameter
	 * 
	 * @param roleCatalogue
	 * @return
	 */
	List<IdmRoleCatalogueRole> findAllByRoleCatalogue_Id(@Param(value = "roleCatalogueId") UUID roleCatalogueId);
	
	/**
	 * Delete all rows that contain role given in parameter
	 * 
	 * @param role
	 * @return
	 */
	int deleteAllByRole_Id(@Param(value = "roleId") UUID roleId);
	
	/**
	 * Delete all rows that contain roleCatalogue given in parameter
	 * @param roleCatalogue
	 * @return
	 */
	int deleteAllByRoleCatalogue_Id(@Param(value = "roleCatalogueId") UUID roleCatalogueId);
}
