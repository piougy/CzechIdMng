package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemEntityFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;

/**
 * Entities on target system
 * 
 * @author Radek Tomiška
 *
 */
public interface SysSystemEntityService extends ReadWriteDtoService<SysSystemEntityDto, SysSystemEntityFilter>, ScriptEnabled {

	/**
	 * Returns {@link SysSystemEntityDto} by given system, entityType, and uid
	 * 
	 * @param uid
	 * @param entityType
	 * @return
	 */
	SysSystemEntityDto getBySystemAndEntityTypeAndUid(SysSystemDto system, SystemEntityType entityType, String uid);
	
	/**
	 * Returns {@link SysSystemEntityDto} by given provisioning operation
	 * 
	 * @param operation
	 * @return
	 */
	SysSystemEntityDto getByProvisioningOperation(ProvisioningOperation operation);

	/**
	 * Load object from the connector
	 * @param systemEntity
	 * @param permissions
	 * @return
	 */
	IcConnectorObject getConnectorObject(SysSystemEntityDto systemEntity, BasePermission... permissions);
	
}
