package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.filter.SyncActionLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * Synchronization action log service
 * @author svandav
 *
 */
public interface SysSyncActionLogService extends ReadWriteEntityService<SysSyncActionLog, SyncActionLogFilter> {

}
