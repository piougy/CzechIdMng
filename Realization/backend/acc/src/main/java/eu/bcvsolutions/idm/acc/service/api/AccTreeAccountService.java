package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AccTreeAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccTreeAccountFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Tree accounts on target system
 * 
 * @author Svanda
 *
 */
public interface AccTreeAccountService extends EntityAccountService<AccTreeAccountDto, AccTreeAccountFilter>, ScriptEnabled,
		AuthorizableService<AccTreeAccountDto> {
}
	
