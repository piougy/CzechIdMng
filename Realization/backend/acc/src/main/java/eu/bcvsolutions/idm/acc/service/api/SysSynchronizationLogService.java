package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSynchronizationLog;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * Synchronization log service
 * @author svandav
 *
 */
public interface SysSynchronizationLogService extends ReadWriteEntityService<SysSynchronizationLog, SynchronizationLogFilter> {

}
