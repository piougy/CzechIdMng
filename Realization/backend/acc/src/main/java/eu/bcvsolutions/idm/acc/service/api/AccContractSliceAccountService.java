package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AccContractSliceAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccContractSliceAccountFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Contract-slice-account on target system
 * 
 * @author Svanda
 *
 */
public interface AccContractSliceAccountService extends 
	ReadWriteDtoService<AccContractSliceAccountDto, AccContractSliceAccountFilter>,
	AuthorizableService<AccContractSliceAccountDto> {

	/**
	 * Delete contract-account
	 * @param entity
	 * @param deleteAccount  If is true, then will be deleted (call provisioning) account on target system.
	 */
	void delete(AccContractSliceAccountDto entity, boolean deleteAccount, BasePermission... permission);
}
	