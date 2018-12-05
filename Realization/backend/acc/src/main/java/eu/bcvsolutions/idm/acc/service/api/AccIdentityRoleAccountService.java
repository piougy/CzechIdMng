package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AccIdentityRoleAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityRoleAccountFilter;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Identity-role-account on target system
 * 
 * @author Svanda
 *
 */
public interface AccIdentityRoleAccountService extends 
	EntityAccountService<AccIdentityRoleAccountDto, AccIdentityRoleAccountFilter>,
	AuthorizableService<AccIdentityRoleAccountDto> {

	/**
	 * Delete identity-role-account
	 * @param entity
	 * @param deleteAccount  If true, then the account on the target system will be deleted (call provisioning).
	 */
	void delete(AccIdentityRoleAccountDto entity, boolean deleteAccount, BasePermission... permission);
}
	
