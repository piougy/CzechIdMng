package eu.bcvsolutions.idm.acc.service.api;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakItems;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningBreakConfigFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * Service for configure provisioning break
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface SysProvisioningBreakConfigService
		extends ReadWriteDtoService<SysProvisioningBreakConfigDto, SysProvisioningBreakConfigFilter> {

	/**
	 * Return break configuration for {@link ProvisioningEventType} and system id
	 * 
	 * @param event
	 * @param systemId
	 * @return
	 */
	SysProvisioningBreakConfigDto getConfig(ProvisioningEventType event, UUID systemId);
	
	/**
	 * Get cache of processed items for system id
	 * 
	 * @param systemId
	 * @return
	 */
	SysProvisioningBreakItems getCacheProcessedItems(UUID systemId);

	/**
	 * Save cache
	 * 
	 * @param systemId
	 * @param cache
	 */
	void saveCacheProcessedItems(UUID systemId, SysProvisioningBreakItems cache);
}
