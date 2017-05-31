package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AccRoleCatalogueAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.RoleCatalogueAccountFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Role catalogue accounts on target system
 * 
 * @author Svanda
 *
 */
public interface AccRoleCatalogueAccountService extends ReadWriteDtoService<AccRoleCatalogueAccountDto, RoleCatalogueAccountFilter> {

	/**
	 * Delete role catalogue account
	 * @param entity
	 * @param deleteAccount  If is true, then will be deleted (call provisioning) account on target system.
	 */
	void delete(AccRoleCatalogueAccountDto entity, boolean deleteAccount, BasePermission... permission);
}
	