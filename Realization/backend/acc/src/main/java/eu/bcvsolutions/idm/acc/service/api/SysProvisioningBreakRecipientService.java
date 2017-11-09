package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakRecipientDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningBreakRecipientFilter;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for configure recipients for provisioning break
 * 
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface SysProvisioningBreakRecipientService
		extends ReadWriteDtoService<SysProvisioningBreakRecipientDto, SysProvisioningBreakRecipientFilter>,
		AuthorizableService<SysProvisioningBreakRecipientDto> {

	/**
	 * Find all {@link SysProvisioningBreakRecipientService} for given
	 * provisioning break config id
	 * 
	 * @param provisioningBreakConfig
	 * @return
	 */
	List<SysProvisioningBreakRecipientDto> findAllByBreakConfig(UUID provisioningBreakConfig);

	/**
	 * Method delete all records for break config
	 * 
	 * @param provisioningBreakConfig
	 */
	void deleteAllByBreakConfig(UUID provisioningBreakConfig);
	
	/**
	 * Find all {@link IdmIdentityDto} for given UUID provisioningBreakConfig.
	 * 
	 * @param provisioningBreakConfig
	 * @return
	 */
	List<IdmIdentityDto> getAllRecipients(UUID provisioningBreakConfig);
	
	/**
	 * Find all {@link IdmIdentityDto} for given {@link ProvisioningEventType} and global configuration.
	 * 
	 * @param eventType
	 * @return
	 */
	List<IdmIdentityDto> getAllRecipientsForGlobalConfiguration(ProvisioningEventType eventType);
}
