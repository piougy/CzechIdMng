package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationConfigFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * Synchronization config service
 * @author svandav
 *
 */
public interface SysSynchronizationConfigService extends ReadWriteEntityService<SysSyncConfig, SynchronizationConfigFilter> {

	boolean isRunning(SysSyncConfig config);

}
