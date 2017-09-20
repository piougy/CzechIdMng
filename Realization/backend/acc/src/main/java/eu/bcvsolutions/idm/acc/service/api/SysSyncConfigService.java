package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.SysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.core.api.service.CloneableService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * Synchronization config service
 * @author svandav
 *
 */
public interface SysSyncConfigService extends ReadWriteDtoService<SysSyncConfigDto, SysSyncConfigFilter>, CloneableService<SysSyncConfigDto> {

	/**
	 * Method check if synchronization with given config running.
	 * 
	 * @param config
	 * @return
	 */
	boolean isRunning(SysSyncConfigDto config);

}
