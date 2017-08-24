package eu.bcvsolutions.idm.acc.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Identity accounts
 * 
 * @author Radek Tomiška
 *
 */
public interface AccIdentityAccountRepository extends AbstractEntityRepository<AccIdentityAccount, IdentityAccountFilter> {
	
	/**
	 * @deprecated Use AccIdentityAccountService (uses criteria api)
	 */
	@Override
	@Deprecated
	@Query(value = "select e from #{#entityName} e")
	default Page<AccIdentityAccount> find(IdentityAccountFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use AccIdentityAccountService (uses criteria api)");
	}
	
	List<AccIdentityAccount> findAllByAccount_Id(UUID accountId);
	
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
