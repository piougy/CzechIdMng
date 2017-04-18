package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Identity accounts on target system
 * 
 * @author Svanda
 *
 */
public interface AccIdentityAccountService extends ReadWriteDtoService<AccIdentityAccountDto, AccIdentityAccount, IdentityAccountFilter> {

	/**
	 * Delete identity account
	 * @param entity
	 * @param deleteAccount  If is true, then will be deleted (call provisioning) account on target system.
	 */
	void delete(AccIdentityAccountDto entity, boolean deleteAccount, BasePermission... permission);
}
	