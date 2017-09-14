package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.dto.filter.RoleCatalogueAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccRoleCatalogueAccount;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Relation role catalogue on account
 * 
 * @author Svanda
 *
 */
public interface AccRoleCatalogueAccountRepository extends AbstractEntityRepository<AccRoleCatalogueAccount, RoleCatalogueAccountFilter> {

	/**
	 * @deprecated "Use DefaultAccRoleCatalogueAccountService (uses criteria api)"
	 */
	@Override
	@Deprecated
	@Query(value = "select e from #{#entityName} e")
	default Page<AccRoleCatalogueAccount> find(RoleCatalogueAccountFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use DefaultAccRoleCatalogueAccountService (uses criteria api)");
	}

	/**
	 * Removes mapping by given account
	 * 
	 * @param account
	 * @return
	 */
	int deleteByAccount(@Param("account") AccAccount account);

}
