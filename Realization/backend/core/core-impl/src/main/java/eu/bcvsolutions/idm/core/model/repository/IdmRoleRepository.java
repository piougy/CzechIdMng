package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.projection.IdmRoleExcerpt;

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
	
	IdmRole findOneByName(@Param("name") String name);
	
	@Override
	@Query(value = "select e from IdmRole e" +
	        " where" +
	        " (?#{[0].text} is null or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')})" +
	        " and (?#{[0].roleType} is null or e.roleType = ?#{[0].roleType})" +
	        " and (?#{[0].roleCatalogue} is null or e.roleCatalogue = ?#{[0].roleCatalogue})")
	Page<IdmRole> find(RoleFilter filter, Pageable pageable);
}
