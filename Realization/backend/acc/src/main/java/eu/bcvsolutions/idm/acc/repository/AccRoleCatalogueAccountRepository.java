package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccRoleCatalogueAccount;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Relation role catalogue on account
 * 
 * @author Svanda
 *
 */
public interface AccRoleCatalogueAccountRepository extends AbstractEntityRepository<AccRoleCatalogueAccount> {

	/**
	 * Removes mapping by given account
	 * 
	 * @param account
	 * @return
	 */
	int deleteByAccount(@Param("account") AccAccount account);

}
