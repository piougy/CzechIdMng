package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakRecipientDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningBreakRecipientFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningBreakRecipient;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningBreakRecipientRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakRecipientService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;

/**
 * Default implementation for {@link SysProvisioningBreakRecipientService}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public class DefaultSysProvisioningBreakRecipientService extends
		AbstractReadWriteDtoService<SysProvisioningBreakRecipientDto, SysProvisioningBreakRecipient, SysProvisioningBreakRecipientFilter>
		implements SysProvisioningBreakRecipientService {

	@Autowired
	public DefaultSysProvisioningBreakRecipientService(
			SysProvisioningBreakRecipientRepository repository) {
		super(repository);
	}

	@Override
	public List<SysProvisioningBreakRecipientDto> findAllByBreakConfig(UUID provisioningBreakConfig) {
		SysProvisioningBreakRecipientFilter filter = new SysProvisioningBreakRecipientFilter();
		filter.setBreakConfigId(provisioningBreakConfig);
		//
		return this.find(filter, null).getContent();
	}

}
