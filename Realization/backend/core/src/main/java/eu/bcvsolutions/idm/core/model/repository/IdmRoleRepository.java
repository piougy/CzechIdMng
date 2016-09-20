package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.model.domain.IdmRoleType;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.projection.IdmRoleExcerpt;

/**
 * Roles repository
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@RepositoryRestResource( //
		collectionResourceRel = "roles", // 
		path = "roles", //
		itemResourceRel = "role", //
		excerptProjection = IdmRoleExcerpt.class,
		exported = false)
public interface IdmRoleRepository extends BaseRepository<IdmRole> {
	
	public static final String ADMIN_ROLE = "superAdminRole";
	
	IdmRole findOneByName(@Param("name") String name);
	
	@Query(value = "select e from IdmRole e" +
	        " where" +
	        " (:text is null or lower(e.name) like :#{#text == null ? '%' : '%'.concat(#text.toLowerCase()).concat('%')})" +
	        " and (:roleType is null or e.roleType = :roleType)")
	Page<IdmRole> findQuick(@Param(value = "text") String text, @Param(value = "roleType") IdmRoleType roleType, Pageable pageable);
}
