package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmOrganization;
import eu.bcvsolutions.idm.core.model.repository.projection.IdmOrganizationExcerpt;

@RepositoryRestResource(//
	collectionResourceRel = "organizations", //
	path = "organizations", //
	itemResourceRel = "organization", //
	collectionResourceDescription = @Description("Organization structure") , //
	itemResourceDescription = @Description("Organization structure"), //
	excerptProjection = IdmOrganizationExcerpt.class
)
public interface IdmOrganizationRepository extends BaseRepository<IdmOrganization> {
	
	@Query(value = "select e from IdmOrganization e" +
	        " where" +
	        " lower(e.name) like :#{#text == null ? '%' : '%'.concat(#text.toLowerCase()).concat('%')}")
	@RestResource(path = "quick", rel = "quick")
	Page<IdmIdentity> findByNameOrParentName(@Param(value = "text") String text, Pageable pageable); // @Param(value = "parentName") String parentName
}
