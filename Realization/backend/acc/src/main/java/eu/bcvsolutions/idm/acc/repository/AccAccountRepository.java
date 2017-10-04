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
	
	@Query(value = "select e from AccAccount e left join e.systemEntity se " +
	        " where" +
	        " (?#{[0].id} is null or e.id = ?#{[0].id})" +
	        " and" +
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
	        " ((?#{[0].identityId} is null and ?#{[0].ownership} is null) or exists"
	        	+ " ("
	        	+ 	" from AccIdentityAccount ia where ia.account = e "
	        		+ "	and (?#{[0].identityId} is null or ia.identity.id = ?#{[0].identityId})"
	        		+ " and (?#{[0].ownership} is null or ia.ownership = ?#{[0].ownership})"
	        	+ " )" +
        	" )" +
	        " and" +
	        " (?#{[0].accountType} is null or e.accountType = ?#{[0].accountType})" +
	        " and" +   
	        " ((?#{[0].supportChangePassword} is null or ?#{[0].supportChangePassword} = false) or exists"
	        	+ " ("
	        	  + " from SysSystemAttributeMapping sam join sam.systemMapping sm join sam.schemaAttribute sa where sm.objectClass.system = e.system "
	        	  + " and sm.operationType = 'PROVISIONING'"
	        	  + " and sa.name = '" + ProvisioningService.PASSWORD_SCHEMA_PROPERTY_NAME + "'"
	        	+ " )" +
        	" )" +
        	" and" +
	        " (?#{[0].entityType} is null or e.entityType = ?#{[0].entityType})")
	Page<AccAccount> find(AccAccountFilter filter, Pageable pageable);
	
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
