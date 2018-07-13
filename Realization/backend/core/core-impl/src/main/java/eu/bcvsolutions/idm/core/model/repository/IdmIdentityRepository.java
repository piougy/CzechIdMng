package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Repository for identities
 * 
 * @author Radek Tomiška 
 *
 */
public interface IdmIdentityRepository extends AbstractEntityRepository<IdmIdentity> {

	IdmIdentity findOneByUsername(@Param("username") String username);
	
	/**
	 * Find all identities by assigned role. Returns even identities with invalid roles (future valid and expired).
	 * 
	 * Warning: returns multiplied identities (joined by identity roles as contract). Use {@link IdmIdentityService#findAllByRole(UUID)} - prevent to multiply identities.
	 * 
	 * @param roleId
	 * @return List of identities with assigned role
	 * @deprecated @since 7.8.0 use {@link IdmIdentityService#findAllByRole(UUID)} - prevent to multiply identities.
	 */
	@Deprecated
	@Transactional(timeout = 5, readOnly = true)
	@Query(value = "SELECT e FROM #{#entityName} e"
			+ " JOIN e.contracts contracts"
			+ " JOIN contracts.roles roles"
			+ " WHERE"
	        + " roles.role.id = :role")
	List<IdmIdentity> findAllByRole(@Param(value = "role") UUID roleId);
}
