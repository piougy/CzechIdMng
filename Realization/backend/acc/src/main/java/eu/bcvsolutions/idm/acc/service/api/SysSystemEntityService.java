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
import eu.bcvsolutions.idm.ic.api.IcObjectClass;

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

	/**
	 * This method determines {@link IcObjectClass} for given {@link SysSystemEntityDto}.
	 *
	 * There are two ways by which a {@link SysSystemEntityDto} can be created in IdM - provisioning and synchronization.
	 * Based on this, this method looks for provisioning and synchronization mappings and returns {@link IcObjectClass} which
	 * should be valid for given account following this logic
	 *
	 * 1. If there is a provisioning mapping for given entity type on system, then it obtains {@link IcObjectClass} from related schema
	 * 2. Othervise it finds all active synchronization mappings for same entity type on given system and selects random one of them.
	 *    After that the result {@link IcObjectClass} is obtained from related schema of this randomly selected mapping.
	 *
	 * Note that this method only guesses {@link IcObjectClass} for account as there is no way of getting it otherwise.
	 *
	 * @param systemEntityDto {@link SysSystemEntityDto} for which should object class be determined
	 * @return {@link IcObjectClass} corresponding to this account.
	 */
	IcObjectClass getObjectClassForSystemEntity(SysSystemEntityDto systemEntityDto, BasePermission... permissions);
	
}
