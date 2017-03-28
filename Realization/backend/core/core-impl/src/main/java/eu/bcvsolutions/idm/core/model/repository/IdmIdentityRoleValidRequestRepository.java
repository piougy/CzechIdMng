package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRoleValidRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

@RepositoryRestResource( //
		collectionResourceRel = "identityRoleValidRequest", // 
		path = "identity-role-valid-request", //
		itemResourceRel = "identityRoleValidRequests", //
		exported = false
		)
public interface IdmIdentityRoleValidRequestRepository extends AbstractEntityRepository<IdmIdentityRoleValidRequest, EmptyFilter> {
	
	@Override
	@Query(value = "SELECT e FROM IdmIdentityRoleValidRequest e")
	Page<IdmIdentityRoleValidRequest> find(EmptyFilter filter, Pageable pageable);
	
	/**
	 * Find all {@link IdmIdentityRoleValidRequest} thats is valid from given in parameter.
	 * @param from
	 * @return
	 */
	@Query(value = "SELECT e FROM IdmIdentityRoleValidRequest e "
	        + "WHERE "
	        + "e.identityRole.validFrom <= :from")
	List<IdmIdentityRoleValidRequest> findAllValidFrom(@Param("from") LocalDate from);
	
	/**
	 * Find all {@link IdmIdentityRoleValidRequest} for identity
	 * @param identity
	 * @return
	 */
	@Query(value = "SELECT e FROM IdmIdentityRoleValidRequest e "
	        + "WHERE "
	        + "e.identityRole.identityContract.identity = :identity")
	List<IdmIdentityRoleValidRequest> findAllByIdentity(@Param("identity") IdmIdentity identity);
	
	/**
	 * Find all {@link IdmIdentityRoleValidRequest} for role
	 * @param role
	 * @return
	 */
	@Query(value = "SELECT e FROM IdmIdentityRoleValidRequest e "
	        + "WHERE "
	        + "e.identityRole.role = :role")
	List<IdmIdentityRoleValidRequest> findAllByRole(@Param("role") IdmRole role);
	
	/**
	 * Find all {@link IdmIdentityRoleValidRequest} for identityRole
	 * @param role
	 * @return
	 */
	@Query(value = "SELECT e FROM IdmIdentityRoleValidRequest e "
	        + "WHERE "
	        + "e.identityRole = :identityRole")
	List<IdmIdentityRoleValidRequest> findAllByIdentityRole(@Param("identityRole") IdmIdentityRole identityRole);
	
	/**
	 * Find all {@link IdmIdentityRoleValidRequest} for identityContract
	 * @param identityContract
	 * @return
	 */
	@Query(value = "SELECT e FROM IdmIdentityRoleValidRequest e "
	        + "WHERE "
	        + "e.identityRole.identityContract = :identityContract")
	List<IdmIdentityRoleValidRequest> findAllByIdentityContract(@Param("identityContract") IdmIdentityContract identityContract);
	
	
	IdmIdentityRoleValidRequest findOneByIdentityRole(@Param("identityRole") IdmIdentityRole identityRole);
}
