package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccTreeAccount;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Relation tree node on account
 * 
 * @author Svanda
 *
 */
public interface AccTreeAccountRepository extends AbstractEntityRepository<AccTreeAccount> {
	
	/**
	 * Removes mapping by given account
	 * 
	 * @param account
	 * @return
	 */
	int deleteByAccount(@Param("account") AccAccount account);
}
