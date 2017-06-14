package eu.bcvsolutions.idm.acc.repository;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.dto.filter.AccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Accounts on target system
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "accounts", //
		path = "accounts", //
		itemResourceRel = "account", //
		// excerptProjection = AccAccountExcerpt.class,
		exported = false // we are using repository metadata, but we want expose rest endpoint manually
	)
public interface AccAccountRepository extends AbstractEntityRepository<AccAccount, AccountFilter> {
	
	@Override
	@Query(value = "select e from AccAccount e left join e.systemEntity se" +
	        " where" +
	        " ("
	        + " ?#{[0].text} is null "
	        + " or lower(e.uid) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
	        + " or lower(se.uid) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
	        + " )" +
	        " and" +
	        " (?#{[0].systemId} is null or e.system.id = ?#{[0].systemId})" +
	        " and" +
	        " (?#{[0].uid} is null or e.uid = ?#{[0].uid})" +
	        " and" +
	        " (?#{[0].systemEntityId} is null or se.id = ?#{[0].systemEntityId})" +
	        " and" +
	        " (?#{[0].identityId} is null or exists (from AccIdentityAccount ia where ia.account = e and ia.identity.id = ?#{[0].identityId}))" +
	        " and" +
	        " (?#{[0].ownership} is null or exists (from AccIdentityAccount iac where iac.account = e and iac.ownership = ?#{[0].ownership}))" + 
	        " and" +
	        " (?#{[0].accountType} is null or e.accountType = ?#{[0].accountType})")
	Page<AccAccount> find(AccountFilter filter, Pageable pageable);
	
	Long countBySystem(@Param("system") SysSystem system);
	
	/**
	 * Clears system entity
	 * 
	 * @param systemEntity
	 * @return
	 */
	@Modifying
	@Query("update #{#entityName} e set e.systemEntity = null where e.systemEntity = :systemEntity")
	int clearSystemEntity(@Param("systemEntity") SysSystemEntity systemEntity);
	
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
