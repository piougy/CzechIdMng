package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.rest.projection.IdmRoleExcerpt;

/**
 * Roles repository
 * 
 * @author Radek Tomiška 
 *
 */
@RepositoryRestResource( //
		collectionResourceRel = "roles", // 
		path = "roles", //
		itemResourceRel = "role", //
		excerptProjection = IdmRoleExcerpt.class,
		exported = false)
public interface IdmRoleRepository extends AbstractEntityRepository<IdmRole, RoleFilter> {
	
	public static final String ADMIN_ROLE = "superAdminRole"; // TODO: move to configurationService
	
	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from IdmRole e" +
	        " where"
	        + " (?#{[0].text} is null or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')})"
	        + " and (?#{[0].roleType} is null or e.roleType = ?#{[0].roleType})"
	        + " and (?#{[0].roleCatalogue} is null or e.roleCatalogue = ?#{[0].roleCatalogue})"
	        + " and"
	        + "	("
	        	+ "?#{[0].guarantee} is null"
	        	+ " or"
	        	+ " exists (from IdmRoleGuarantee rg where rg.role = e and rg.guarantee = ?#{[0].guarantee})"
        	+ " )")
	Page<IdmRole> find(RoleFilter filter, Pageable pageable);
	
	IdmRole findOneByName(@Param("name") String name);
	
	/**
	 * Clears role catalogue
	 * 
	 * @param roleCatalogue
	 * @return
	 */
	@Modifying
	@Query("update #{#entityName} e set e.roleCatalogue = null where e.roleCatalogue = :roleCatalogue")
	int clearCatalogue(@Param("roleCatalogue") IdmRoleCatalogue roleCatalogue);
}
