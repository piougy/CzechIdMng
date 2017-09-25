package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakRecipientDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningBreakRecipientFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * Service for configure recipients for provisioning break
 * 
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface SysProvisioningBreakRecipientService
		extends ReadWriteDtoService<SysProvisioningBreakRecipientDto, SysProvisioningBreakRecipientFilter> {

	/**
	 * Find all {@link SysProvisioningBreakRecipientService} that has for given provisioning break config
	 * 
	 * @param provisioningBreakConfig
	 * @return
	 */
	List<SysProvisioningBreakRecipientDto> findAllByBreakConfig(UUID provisioningBreakConfig);
}
