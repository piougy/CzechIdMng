package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Repository for identities
 * 
 * @author Radek Tomiška 
 *
 */
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
	        + " roles.role.id = :role")
	List<IdmIdentity> findAllByRole(@Param(value = "role") UUID role);
	
	/**
	 * Find identities where the IdmAuthorityChange relation does not
	 * exist.
	 * @param identities
	 * @return
	 */
	@Query(value = "select e from IdmIdentity e where e.id in (:identities) and e not in"
			+ "(select z.identity from IdmAuthorityChange z where z.identity.id in (:identities))")
	List<IdmIdentity> findAllWithoutAuthorityChange(@Param("identities") List<UUID> identities);
	
	@Transactional
	@Modifying
	@Query(value = "update IdmAuthorityChange e set e.authChangeTimestamp = :authorityChange"
			+ " where e.identity.id in (:identities)")
	void setIdmAuthorityChangeForIdentity(@Param("identities") List<UUID> identities,
			@Param("authorityChange") DateTime authorityChange);
}
