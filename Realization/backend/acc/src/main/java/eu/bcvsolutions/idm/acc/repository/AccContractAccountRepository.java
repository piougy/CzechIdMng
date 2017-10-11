package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccContractAccount;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Relation Contract on account
 * 
 * @author Svanda
 *
 */
public interface AccContractAccountRepository extends AbstractEntityRepository<AccContractAccount> {
	
	/**
	 * Removes mapping by given account
	 * 
	 * @param account
	 * @return
	 */
	int deleteByAccount(@Param("account") AccAccount account);
}
