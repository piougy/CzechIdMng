package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRoleValidRequest;

/**
 * 
 * @author Ondřej Kopr
 *
 */
public interface IdmIdentityRoleValidRequestRepository extends AbstractEntityRepository<IdmIdentityRoleValidRequest> {
	
	/**
	 * Find all {@link IdmIdentityRoleValidRequest} thats is valid from given in parameter.
	 * @param from
	 * @return
	 */
	@Query(value = "SELECT e FROM #{#entityName} e "
	        + "WHERE "
	        + "e.identityRole.validFrom <= :from")
	List<IdmIdentityRoleValidRequest> findAllValidFrom(@Param("from") LocalDate from);
	
	/**
	 * Find all {@link IdmIdentityRoleValidRequest} for identity
	 * @param identity
	 * @return
	 */
	List<IdmIdentityRoleValidRequest> findAllByIdentityRole_IdentityContract_Identity_Id(@Param("identityId") UUID identityId);
	
	/**
	 * Find all {@link IdmIdentityRoleValidRequest} for role
	 * @param role
	 * @return
	 */
	List<IdmIdentityRoleValidRequest> findAllByIdentityRole_Role_Id(@Param("roleId") UUID roleId);
	
	/**
	 * Find all {@link IdmIdentityRoleValidRequest} for identityRole
	 * @param role
	 * @return
	 */
	List<IdmIdentityRoleValidRequest> findAllByIdentityRole_Id(@Param("identityRoleId") UUID identityRoleId);
	
	/**
	 * Find all {@link IdmIdentityRoleValidRequest} for identityContract
	 * @param identityContract
	 * @return
	 */
	List<IdmIdentityRoleValidRequest>findAllByIdentityRole_IdentityContract_Id(@Param("identityContractId") UUID identityContractId);
	
	
	IdmIdentityRoleValidRequest findOneByIdentityRole_Id(@Param("identityRole") UUID identityRoleId);
}
