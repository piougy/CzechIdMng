package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCatalogueRoleFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;

/**
 * Default repository for intersection table between role catalogue and role
 * 
 * TODO: refactor filter usage
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
public interface IdmRoleCatalogueRoleRepository extends AbstractEntityRepository<IdmRoleCatalogueRole> {
	
	/**
	 * @deprecated @since 8.1.4 use filter
	 * 
	 * @param filter
	 * @param pageable
	 * @return
	 */
	@Deprecated
	@Query(value = "select e from IdmRoleCatalogueRole e " +
	        " where" +
	        " (?#{[0].roleCatalogueId} is null or e.roleCatalogue.id = ?#{[0].roleCatalogueId})" + 
	        " and (?#{[0].roleId} is null or e.role.id = ?#{[0].roleId})" + 
	        " and (?#{[0].roleCatalogueCode} is null or e.roleCatalogue.code = ?#{[0].roleCatalogueCode})")
	Page<IdmRoleCatalogueRole> find(IdmRoleCatalogueRoleFilter filter, Pageable pageable);
	
	/**
	 * Get all roleCatalogueRole for role given in parameter
	 * 
	 * @param role
	 * @return
	 * @deprecated @since 8.1.4 use filter
	 */
	@Deprecated
	List<IdmRoleCatalogueRole> findAllByRole_Id(UUID roleId);
	
	/**
	 * Get all roleCatalogueRole for roleCatalogue given in parameter
	 * 
	 * @param roleCatalogue
	 * @return
	 * @deprecated @since 8.1.4 use filter
	 */
	@Deprecated
	List<IdmRoleCatalogueRole> findAllByRoleCatalogue_Id(UUID roleCatalogueId);
	
	/**
	 * Delete all rows that contain role given in parameter
	 * 
	 * @param role
	 * @return
	 * @deprecated @since 8.1.4 use filter + delete (this method skips audit)
	 */
	@Deprecated
	int deleteAllByRole_Id(@Param(value = "roleId") UUID roleId);
	
	/**
	 * Delete all rows that contain roleCatalogue given in parameter
	 * @param roleCatalogue
	 * @return
	 * @deprecated @since 8.1.4 use filter + delete (this method skips audit)
	 */
	@Deprecated
	int deleteAllByRoleCatalogue_Id(@Param(value = "roleCatalogueId") UUID roleCatalogueId);
}
