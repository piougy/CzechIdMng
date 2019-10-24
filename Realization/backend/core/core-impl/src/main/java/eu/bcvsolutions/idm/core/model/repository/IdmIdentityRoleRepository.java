package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Identity roles
 * 
 * @author Radek Tomiška
 */
public interface IdmIdentityRoleRepository extends AbstractEntityRepository<IdmIdentityRole> {
	
	/**
	 * @param identity
	 * @param pageable
	 * @return
	 * @deprecated @since 10.0.0 use find method in service.
	 */
	@Deprecated
	Page<IdmIdentityRole> findByIdentityContract_Identity(@Param("identity") IdmIdentity identity, Pageable pageable);
	
	List<IdmIdentityRole> findAllByIdentityContract_Id(@Param("identityContractId") UUID identityContractId, Sort sort);
	
	List<IdmIdentityRole> findAllByIdentityContract_Identity_Id(@Param("identityId") UUID identityId, Sort sort);

	/**
	 * @param username
	 * @param pageable
	 * @return
	 * @deprecated @since 10.0.0 use find method in service.
	 */
	@Deprecated
	Page<IdmIdentityRole> findByIdentityContract_Identity_Username(@Param("username") String username, Pageable pageable);
	
	/**
	 * Count assigned roles by role identifier.
	 * 
	 * @param roleId
	 * @return
	 */
	Long countByRole_Id(UUID roleId);
	
	/**
	 * Assigned roles by role identifier.
	 * 
	 * @param role
	 * @param pageable
	 * @return
	 * @deprecated @since 10.0.0 use find method in service.
	 */
	@Deprecated
	Page<IdmIdentityRole> findByRole(@Param("role") IdmRole role, Pageable pageable);
	
	/**
	 * Returns assigned roles by given automatic role.
	 * 
	 * @param roleTreeNodeId
	 * @param pageable
	 * @return
	 */
	Page<IdmIdentityRole> findByAutomaticRole_Id(@Param("automaticRoleId") UUID automaticRoleId, Pageable pageable);
	
	/**
	 * @param identity
	 * @param role
	 * @return
	 * @deprecated @since 10.0.0 use find method in service.
	 */
	@Deprecated
	List<IdmIdentityRole> findAllByIdentityContract_IdentityAndRole(@Param("identity") IdmIdentity identity, @Param("role") IdmRole role);

	@Query(value = "select e from #{#entityName} e"
			+ " where e.validTill is not null and e.validTill < :expirationDate")
	Page<IdmIdentityRole> findExpiredRoles(@Param("expirationDate") LocalDate expirationDate, Pageable page);
}
