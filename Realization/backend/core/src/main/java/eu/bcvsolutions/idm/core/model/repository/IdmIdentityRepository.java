package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.projection.IdmIdentityExcerpt;

/**
 * Repository for identities
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@RepositoryRestResource(//
	collectionResourceRel = "identities", //
	path = "identities", //
	itemResourceRel = "identity", //
	collectionResourceDescription = @Description("IdM identities Collection") , //
	itemResourceDescription = @Description("IdM identity - main IdM resource"), //
	excerptProjection = IdmIdentityExcerpt.class //
)
public interface IdmIdentityRepository extends BaseRepository<IdmIdentity> {

	IdmIdentity findOneByUsername(@Param("username") String username);

	@Query(value = "select e from IdmIdentity e" +
	        " where" +
	        " lower(e.username) like :#{#text == null ? '%' : '%'.concat(#text.toLowerCase()).concat('%')}" +
	        " or lower(e.firstName) like :#{#text == null ? '%' : '%'.concat(#text.toLowerCase()).concat('%')}" +
	        " or lower(e.lastName) like :#{#text == null ? '%' : '%'.concat(#text.toLowerCase()).concat('%')}" +
	        " or lower(e.email) like :#{#text == null ? '%' : '%'.concat(#text.toLowerCase()).concat('%')}" +
	        " or lower(e.description) like :#{#text == null ? '%' : '%'.concat(#text.toLowerCase()).concat('%')}")
	@RestResource(path = "quick", rel = "quick")
	Page<IdmIdentity> findByFulltext(@Param(value = "text") String text, Pageable pageable);
	
	@Override
	@RestResource(exported = false)
	void delete(Long id);

	@Override
	@RestResource(exported = false)
	void delete(IdmIdentity entity);

}
