package eu.bcvsolutions.idm.acc.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Accounts on target system
 * 
 * @author Radek Tomiška
 *
 */
public interface AccAccountRepository extends AbstractEntityRepository<AccAccount> {
	
	/**
	 * Count accounts by system.
	 * 
	 * @param systemId
	 * @return
	 */
	Long countBySystem_Id(UUID systemId);
	
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
	Page<AccAccount> findByEndOfProtectionLessThanAndInProtectionIsTrue(@Param("endOfProtection") ZonedDateTime endOfProtection, Pageable pageable);
}
