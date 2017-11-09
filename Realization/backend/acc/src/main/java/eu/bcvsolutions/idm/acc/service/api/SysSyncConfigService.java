package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.core.api.service.CloneableService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * Synchronization config service
 * @author svandav
 *
 */
public interface SysSyncConfigService extends ReadWriteDtoService<AbstractSysSyncConfigDto, SysSyncConfigFilter>, CloneableService<AbstractSysSyncConfigDto> {

	/**
	 * Method check if synchronization with given config running.
	 * 
	 * @param config
	 * @return
	 */
	boolean isRunning(AbstractSysSyncConfigDto config);
	
	/**
	 * Return count of {@link AbstractSysSyncConfigDto} for {@link SysSystemMappingDto}
	 * 
	 * @param mappingDto
	 * @return
	 */
	Long countBySystemMapping(SysSystemMappingDto mappingDto);
}
