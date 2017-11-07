package eu.bcvsolutions.idm.acc.repository;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Accounts on target system
 * 
 * @author Radek Tomiška
 *
 */
public interface AccAccountRepository extends AbstractEntityRepository<AccAccount> {
	

	/**
	 * 
	 * @param system
	 * @deprecated use {@link #countBySystem_Id(UUID)}
	 */
	@Deprecated
	Long countBySystem(SysSystem system);
	
	/**
	 * Count accounts by system.
	 * 
	 * @param systemId
	 * @return
	 */
	Long countBySystem_Id(UUID systemId);
	
	/**
	 * Clears system entity
	 * 
	 * @param systemEntity
	 * @return
	 */
	@Modifying
	@Query("update #{#entityName} e set e.systemEntity = null where e.systemEntity.id = :systemEntityId")
	int clearSystemEntity(@Param("systemEntityId") UUID systemEntityId);
	
	/**
	 * Find all {@link AccAccount} by identity id and system id.
	 * All parameters are required.
	 * 
	 * @param identityId
	 * @param systemId
	 * @return
	 */
	@Query("SELECT e FROM AccAccount e WHERE "
			+ "e.system.id = :systemId "
			+ "AND "
			+ "exists (from AccIdentityAccount ia where ia.account = e and ia.identity.id = :identityId)")
	List<AccAccount> findAccountBySystemAndIdentity(@Param("identityId") UUID identityId, @Param("systemId") UUID systemId);
	
	/**
	 * Returns accounts with expired protection. Account has to be in protection mode.
	 * 
	 * @param endOfProtection
	 * @param pageable
	 * @return
	 */
	Page<AccAccount> findByEndOfProtectionLessThanAndInProtectionIsTrue(@Param("endOfProtection") DateTime endOfProtection, Pageable pageable);
}
