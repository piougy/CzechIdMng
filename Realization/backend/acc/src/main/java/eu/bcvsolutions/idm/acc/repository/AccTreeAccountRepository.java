package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.dto.filter.TreeAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccTreeAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Relation tree node on account
 * 
 * @author Svanda
 *
 */
public interface AccTreeAccountRepository extends AbstractEntityRepository<AccTreeAccount, TreeAccountFilter> {
	
	/**
	 * @deprecated "Use DefaultAccTreeAccountService (uses criteria api)"
	 */
	@Override
	@Deprecated
	@Query(value = "select e from AccTreeAccount e")
	default Page<AccTreeAccount> find(TreeAccountFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use DefaultAccTreeAccountService (uses criteria api)");
	}
	
	/**
	 * Removes mapping by given account
	 * 
	 * @param account
	 * @return
	 */
	int deleteByAccount(@Param("account") AccAccount account);

	
	/**
	 * Clears roleSystem
	 * 
	 * @param roleSystem
	 * @return
	 */
	@Modifying
	@Query("update AccTreeAccount e set e.roleSystem = null where e.roleSystem = :roleSystem")
	int clearRoleSystem(@Param("roleSystem") SysRoleSystem roleSystem);
}
