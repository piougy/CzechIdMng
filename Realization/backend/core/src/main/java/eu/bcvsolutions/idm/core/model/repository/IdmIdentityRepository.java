package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.transaction.annotation.Transactional;

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
		excerptProjection = IdmIdentityExcerpt.class,
		exported = false // we are using repository metadata, but we want expose rest endpoint manually
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
	

	@Transactional(timeout = 5)
	@Query(value = "SELECT e FROM IdmIdentity e "
			+ "JOIN e.roles roles "
			+ "WHERE "
	        + "roles.role.id =:roleId")
	@RestResource(path = "findAllByRole", rel = "findAllByRole")
	List<IdmIdentity> findAllByRole(@Param(value = "roleId") Long roleId);
	
	@Override
	@RestResource(exported = false)
	void delete(Long id);

	@Override
	@RestResource(exported = false)
	void delete(IdmIdentity entity);
}
