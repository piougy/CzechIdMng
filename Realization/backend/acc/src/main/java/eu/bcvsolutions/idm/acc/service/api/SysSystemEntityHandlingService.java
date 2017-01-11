package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SystemEntityHandlingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * System entity handling service
 * @author svandav
 *
 */
public interface SysSystemEntityHandlingService extends ReadWriteEntityService<SysSystemEntityHandling, SystemEntityHandlingFilter> {

	public List<SysSystemEntityHandling> findBySystem(SysSystem system, SystemOperationType operation, SystemEntityType entityType);
	
	public List<SysSystemEntityHandling> findByObjectClass(SysSchemaObjectClass objectClass, SystemOperationType operation, SystemEntityType entityType);
}
