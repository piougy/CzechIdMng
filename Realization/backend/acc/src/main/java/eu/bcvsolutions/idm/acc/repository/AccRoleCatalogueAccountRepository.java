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
	
	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from AccRoleCatalogueAccount e" +
	        " where" +
	        " (?#{[0].accountId} is null or e.account.id = ?#{[0].accountId})" +
	        " and" +
	        " (?#{[0].roleCatalogueId} is null or e.roleCatalogue.id = ?#{[0].roleCatalogueId})"+
	        " and" +
	        " (?#{[0].systemId} is null or e.account.system.id = ?#{[0].systemId})" + 
	        " and" +
	        " (?#{[0].ownership} is null or e.ownership = ?#{[0].ownership})")
	Page<AccRoleCatalogueAccount> find(RoleCatalogueAccountFilter filter, Pageable pageable);
	
	/**
	 * Removes mapping by given account
	 * 
	 * @param account
	 * @return
	 */
	int deleteByAccount(@Param("account") AccAccount account);

}
