package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.SysSyncActionLogDto;
import eu.bcvsolutions.idm.acc.dto.filter.SyncActionLogFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * Synchronization action log service
 * @author svandav
 *
 */
public interface SysSyncActionLogService extends ReadWriteDtoService<SysSyncActionLogDto, SyncActionLogFilter> {

}
