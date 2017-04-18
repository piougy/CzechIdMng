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
	
	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from AccTreeAccount e" +
	        " where" +
	        " (?#{[0].accountId} is null or e.account.id = ?#{[0].accountId})" +
	        " and" +
	        " (?#{[0].treeNodeId} is null or e.treeNode.id = ?#{[0].treeNodeId})" +
	        " and" +
	        " (?#{[0].roleSystemId} is null or e.roleSystem.id = ?#{[0].roleSystemId})" + 
	        " and" +
	        " (?#{[0].systemId} is null or e.account.system.id = ?#{[0].systemId})" + 
	        " and" +
	        " (?#{[0].ownership} is null or e.ownership = ?#{[0].ownership})")
	Page<AccTreeAccount> find(TreeAccountFilter filter, Pageable pageable);
	
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
