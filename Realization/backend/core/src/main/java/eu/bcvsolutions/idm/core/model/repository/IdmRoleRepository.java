package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

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
		excerptProjection = IdmRoleExcerpt.class)

public interface IdmRoleRepository extends BaseRepository<IdmRole> {
	
	IdmRole findOneByName(@Param("name") String name);
	
	@Query(value = "select e from IdmRole e" +
	        " where" +
	        " lower(e.name) like :#{#text == null ? '%' : '%'.concat(#text.toLowerCase()).concat('%')}")
	@RestResource(path = "quick", rel = "quick")
	Page<IdmRole> findByNameLikeLowerCase(@Param(value = "text") String text, Pageable pageable);
}
