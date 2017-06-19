package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.filter.SystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.core.api.service.CloneableService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * System entity handling service
 * @author svandav
 *
 */
public interface SysSystemMappingService extends ReadWriteEntityService<SysSystemMapping, SystemMappingFilter>, CloneableService<SysSystemMapping> {

	public List<SysSystemMapping> findBySystem(SysSystem system, SystemOperationType operation, SystemEntityType entityType);
	
	public List<SysSystemMapping> findByObjectClass(SysSchemaObjectClass objectClass, SystemOperationType operation, SystemEntityType entityType);

	/**
	 * Is enabled protection of account against delete
	 * @param account
	 * @return
	 */
	boolean isEnabledProtection(AccAccount account);

	/**
	 * Interval of protection against account delete
	 * @param account
	 * @return
	 */
	Integer getProtectionInterval(AccAccount account);


	/**
	 * Duplicate (create/persist new) mapping with all attributes
	 * @param id
	 * @param schema
	 * @return
	 */
	public SysSystemMapping duplicate(UUID id, SysSchemaObjectClass schema);
}
