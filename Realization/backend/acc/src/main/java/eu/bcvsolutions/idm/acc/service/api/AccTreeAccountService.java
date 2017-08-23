package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AccTreeAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.TreeAccountFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Tree accounts on target system
 * 
 * @author Svanda
 *
 */
public interface AccTreeAccountService extends ReadWriteDtoService<AccTreeAccountDto, TreeAccountFilter>, ScriptEnabled,
		AuthorizableService<AccTreeAccountDto> {

	/**
	 * Delete tree node account
	 * @param entity
	 * @param deleteAccount  If is true, then will be deleted (call provisioning) account on target system.
	 */
	void delete(AccTreeAccountDto entity, boolean deleteAccount, BasePermission... permission);
}
	