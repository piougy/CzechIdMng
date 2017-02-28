package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleCatalogueRoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;

/**
 * Default repository for intersection table between role catalogue and role
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RepositoryRestResource( //
		collectionResourceRel = "roleCatalogueRole", // 
		path = "role-catalogue-role", //
		itemResourceRel = "roleCatalogueRoles", //
		exported = false
		)
public interface IdmRoleCatalogueRoleRepository extends AbstractEntityRepository<IdmRoleCatalogueRole, RoleCatalogueRoleFilter> {
	
	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from IdmRoleCatalogueRole e " +
	        " where" +
	        " (?#{[0].role} is null or e.role = ?#{[0].role})" + 
	        " and (?#{[0].roleCatalogue} is null or e.roleCatalogue = ?#{[0].roleCatalogue})")
	Page<IdmRoleCatalogueRole> find(RoleCatalogueRoleFilter filter, Pageable pageable);
	
	/**
	 * Get all roleCatalogueRole for role given in parameter
	 * 
	 * @param role
	 * @return
	 */
	@Query(value = "SELECT e FROM IdmRoleCatalogueRole e WHERE "
			+ "e.role = :role")
	List<IdmRoleCatalogueRole> findAllByRole(@Param(value = "role") IdmRole role);
	
	/**
	 * Get all roleCatalogueRole for roleCatalogue given in parameter
	 * 
	 * @param roleCatalogue
	 * @return
	 */
	@Query(value = "SELECT e FROM IdmRoleCatalogueRole e WHERE "
			+ "e.roleCatalogue = :roleCatalogue")
	List<IdmRoleCatalogueRole> findAllByRoleCatalogue(@Param(value = "roleCatalogue") IdmRoleCatalogue roleCatalogue);
	
	/**
	 * Delete all rows that contain role given in parameter
	 * 
	 * @param role
	 * @return
	 */
	int deleteAllByRole(@Param(value = "role") IdmRole role);
	
	/**
	 * Delete all rows that contain roleCatalogue given in parameter
	 * @param roleCatalogue
	 * @return
	 */
	int deleteAllByRoleCatalogue(@Param(value = "roleCatalogue") IdmRoleCatalogue roleCatalogue);
}
