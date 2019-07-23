package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Archived provisioning operation
 * 
 * @author Radek Tomiška
 *
 */
public interface SysProvisioningArchiveService extends 
		ReadWriteDtoService<SysProvisioningArchiveDto, SysProvisioningOperationFilter>,
		AuthorizableService<SysProvisioningArchiveDto>,
		ScriptEnabled {

	/**
	 * Archives provisioning operation
	 * 
	 * @param provisioningOperation
	 * @return
	 */
	SysProvisioningArchiveDto archive(SysProvisioningOperationDto provisioningOperation);

	/**
	 * Optimize - system can be pre-loaded in DTO.
	 * 
	 * @param archive
	 * @return
	 */
	SysSystemDto getSystem(SysProvisioningArchiveDto archive);
}
