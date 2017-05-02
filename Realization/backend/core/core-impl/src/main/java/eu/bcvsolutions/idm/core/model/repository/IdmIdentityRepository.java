package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.rest.projection.IdmIdentityExcerpt;

/**
 * Repository for identities
 * 
 * @author Radek Tomiška 
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "identities", //
		path = "identities", //
		itemResourceRel = "identity", //
		excerptProjection = IdmIdentityExcerpt.class,
		exported = false // we are using repository metadata, but we want expose rest endpoint manually
	)
public interface IdmIdentityRepository extends AbstractEntityRepository<IdmIdentity, IdentityFilter> {

	IdmIdentity findOneByUsername(@Param("username") String username);

	/**
	 * @deprecated use IdmIdentityService (uses criteria api)
	 */
	@Override
	@Deprecated
	@Query(value = "select e from #{#entityName} e")
	default Page<IdmIdentity> find(IdentityFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use IdmIdentityService (uses criteria api)");
	}
	
	@Transactional(timeout = 5, readOnly = true)
	@Query(value = "SELECT e FROM IdmIdentity e"
			+ " JOIN e.contracts contracts"
			+ " JOIN contracts.roles roles"
			+ " WHERE"
	        + " roles.role = :role")
	List<IdmIdentity> findAllByRole(@Param(value = "role") IdmRole role);
	
	/**
	 * Find identities where the IdmAuthorityChange relation does not
	 * exist.
	 * @param identities
	 * @return
	 */
	@Query(value = "select e from IdmIdentity e where e in (:identities) and e not in"
			+ "(select z.identity from IdmAuthorityChange z where z.identity in (:identities))")
	List<IdmIdentity> findAllWithoutAuthorityChange(@Param("identities") List<IdmIdentity> identities);
	
	@Transactional
	@Modifying
	@Query(value = "update IdmAuthorityChange e set e.authChangeTimestamp = :authorityChange"
			+ " where e.identity in (:identities)")
	void setIdmAuthorityChangeForIdentity(@Param("identities") List<IdmIdentity> identities,
			@Param("authorityChange") DateTime authorityChange);
}
