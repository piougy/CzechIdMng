package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AccRoleAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.RoleAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccRoleAccount;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Role accounts on target system
 * 
 * @author Svanda
 *
 */
public interface AccRoleAccountService extends ReadWriteDtoService<AccRoleAccountDto, RoleAccountFilter> {

	/**
	 * Delete role account
	 * @param entity
	 * @param deleteAccount  If is true, then will be deleted (call provisioning) account on target system.
	 */
	void delete(AccRoleAccountDto entity, boolean deleteAccount, BasePermission... permission);
}
	