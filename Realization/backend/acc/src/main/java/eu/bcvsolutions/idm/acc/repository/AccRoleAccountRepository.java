package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.dto.filter.RoleAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccRoleAccount;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Relation role on account
 * 
 * @author Svanda
 *
 */
public interface AccRoleAccountRepository extends AbstractEntityRepository<AccRoleAccount, RoleAccountFilter> {
	
	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from AccRoleAccount e" +
	        " where" +
	        " (?#{[0].accountId} is null or e.account.id = ?#{[0].accountId})" +
	        " and" +
	        " (?#{[0].roleId} is null or e.role.id = ?#{[0].roleId})" +
	        " and" +
	        " (?#{[0].systemId} is null or e.account.system.id = ?#{[0].systemId})" + 
	        " and" +
	        " (?#{[0].ownership} is null or e.ownership = ?#{[0].ownership})")
	Page<AccRoleAccount> find(RoleAccountFilter filter, Pageable pageable);
	
	/**
	 * Removes mapping by given account
	 * 
	 * @param account
	 * @return
	 */
	int deleteByAccount(@Param("account") AccAccount account);
}
