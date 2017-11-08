package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;

/**
 * Accounts on target system
 * 
 * @author Radek Tomiška
 *
 */
public interface AccAccountService extends 
		ReadWriteDtoService<AccAccountDto, AccAccountFilter>, 
		ScriptEnabled,
		AuthorizableService<AccAccountDto>
		{

	@Deprecated
	/**
	 * Delete AccAccount
	 * @param account
	 * @param deleteTargetAccount If is true, then will be call provisioning 
	 *  and deleted account on target system
	 * @param entityId - Id of entity connected to the account. Can be null, but provisioning archive will not have correct information.
	 * @deprecated Will be moved to event. This method will be removed!
	 */
	void delete(AccAccountDto account, boolean deleteTargetAccount, UUID entityId);
	
	/**
	 * Get accounts for identity on system.
	 * @param systemId
	 * @param identityId
	 * @return
	 */
	List<AccAccountDto> getAccounts(UUID systemId, UUID identityId);

	/**
	 * Find account by UID on given system.
	 * @param uid
	 * @param systemId
	 * @return
	 */
	AccAccountDto getAccount(String uid, UUID systemId);
	
	/**
	 * Returns accounts with expired protection. Account has to be in protection mode.
	 * 
	 * @param expirationDate
	 * @param pageable
	 * @return
	 */
	Page<AccAccountDto> findExpired(DateTime expirationDate, Pageable pageable);

	/**
	 * Load object from the connector
	 * @param account
	 * @return
	 */
	IcConnectorObject getConnectorObject(AccAccountDto account);
}
