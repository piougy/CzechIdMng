package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.acc.dto.filter.AccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * Accounts on target system
 * 
 * @author Radek Tomiška
 *
 */
public interface AccAccountService extends ReadWriteEntityService<AccAccount, AccountFilter>, ScriptEnabled {

	/**
	 * Delete AccAccount
	 * @param account
	 * @param deleteTargetAccount If is true, then will be call provisioning 
	 *  and deleted account on target system
	 */
	void delete(AccAccount account, boolean deleteTargetAccount);
	
	/**
	 * Get accounts for identity on system.
	 * @param systemId
	 * @param identityId
	 * @return
	 */
	List<AccAccount> getAccouts(UUID systemId, UUID identityId);

	/**
	 * Find account by UID on given system.
	 * @param uid
	 * @param systemId
	 * @return
	 */
	AccAccount getAccount(String uid, UUID systemId);
	
	/**
	 * Returns accounts with expired protection. Account has to be in protection mode.
	 * 
	 * @param expirationDate
	 * @param pageable
	 * @return
	 */
	Page<AccAccount> findExpired(DateTime expirationDate, Pageable pageable);
}
