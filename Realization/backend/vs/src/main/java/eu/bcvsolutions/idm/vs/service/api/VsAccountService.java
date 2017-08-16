package eu.bcvsolutions.idm.vs.service.api;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.vs.repository.filter.AccountFilter;
import eu.bcvsolutions.idm.vs.service.api.dto.VsAccountDto;

/**
 * Service for accounts in virtual system
 * 
 * @author Svanda
 *
 */
public interface VsAccountService extends 
		ReadWriteDtoService<VsAccountDto, AccountFilter> {


}
