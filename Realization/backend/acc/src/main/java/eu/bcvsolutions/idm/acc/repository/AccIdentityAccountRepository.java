package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Target system configuration
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(//
		path = "identity-accounts", //
		collectionResourceRel = "identityAccounts", //
		itemResourceRel = "identityAccount", //
		// excerptProjection=AccIdentityAccountExcerpt.class,
		exported = false // we are using repository metadata, but we want expose rest endpoint manually
	)
public interface AccIdentityAccountRepository extends AbstractEntityRepository<AccIdentityAccount, IdentityAccountFilter> {
	
	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from AccIdentityAccount e left join e.identityRole ir" +
	        " where" +
	        " (?#{[0].accountId} is null or e.account.id = ?#{[0].accountId})" +
	        " and" +
	        " (?#{[0].identityId} is null or e.identity.id = ?#{[0].identityId})" +
	        " and" +
	        " (?#{[0].roleId} is null or ir.role.id = ?#{[0].roleId})" + 
	        " and" +
	        " (?#{[0].identityRoleId} is null or ir.id = ?#{[0].identityRoleId})" + 
	        " and" +
	        " (?#{[0].roleSystemId} is null or e.roleSystem.id = ?#{[0].roleSystemId})" + 
	        " and" +
	        " (?#{[0].systemId} is null or e.account.system.id = ?#{[0].systemId})" + 
	        " and" +
	        " (?#{[0].ownership} is null or e.ownership = ?#{[0].ownership})")
	Page<AccIdentityAccount> find(IdentityAccountFilter filter, Pageable pageable);
	
	/**
	 * Removes mapping by given account
	 * 
	 * @param account
	 * @return
	 */
	int deleteByAccount(@Param("account") AccAccount account);
	
	/**
	 * Removes mapping by given identity
	 * 
	 * @param identity
	 * @return
	 */
	int deleteByIdentity(@Param("identity") IdmIdentity identity);
	
	/**
	 * Clears roleSystem
	 * 
	 * @param roleSystem
	 * @return
	 */
	@Modifying
	@Query("update AccIdentityAccount e set e.roleSystem = null where e.roleSystem = :roleSystem")
	int clearRoleSystem(@Param("roleSystem") SysRoleSystem roleSystem);
}
