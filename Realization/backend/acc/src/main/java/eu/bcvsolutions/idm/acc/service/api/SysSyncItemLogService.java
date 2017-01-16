package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.SyncItemLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * Synchronization item log service
 * @author svandav
 *
 */
public interface SysSyncItemLogService extends ReadWriteEntityService<SysSyncItemLog, SyncItemLogFilter> {

}
