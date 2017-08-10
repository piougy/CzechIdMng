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
	/**
	 * @deprecated Use DefaultAccRoleAccountService (uses criteria api)
	 */
	@Override
	@Deprecated
	@Query(value = "select e from AccRoleAccount e")
	default Page<AccRoleAccount> find(RoleAccountFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use DefaultAccRoleAccountService (uses criteria api)");
	}
	
	/**
	 * Removes mapping by given account
	 * 
	 * @param account
	 * @return
	 */
	int deleteByAccount(@Param("account") AccAccount account);
}
