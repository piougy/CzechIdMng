package eu.bcvsolutions.idm.vs.service.api;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.vs.repository.filter.AccountFilter;
import eu.bcvsolutions.idm.vs.service.api.dto.VsAccountDto;

/**
 * Service for accounts in virtual system
 * 
 * @author Svanda
 *
 */
public interface VsAccountService extends 
		ReadWriteDtoService<VsAccountDto, AccountFilter>, AuthorizableService<VsAccountDto> {

	/**
	 * Find VS account by UID and System ID
	 * @param uidValue
	 * @param systemId
	 * @return
	 */
	VsAccountDto findByUidSystem(String uidValue, UUID systemId);


}
