package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationLogFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * Synchronization log service
 * @author svandav
 *
 */
public interface SysSyncLogService extends ReadWriteDtoService<SysSyncLogDto, SynchronizationLogFilter> {

}
