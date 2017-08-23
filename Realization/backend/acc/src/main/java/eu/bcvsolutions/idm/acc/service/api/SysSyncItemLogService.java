package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.filter.SyncItemLogFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * Synchronization item log service
 * @author svandav
 *
 */
public interface SysSyncItemLogService extends ReadWriteDtoService<SysSyncItemLogDto, SyncItemLogFilter> {

}
