package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakConfigDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningBreakConfigFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningBreakConfig;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningBreakConfigReposiotry;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakConfigService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;

/**
 * Default implementation for {@link SysProvisioningBreakConfigService}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public class DefaultSysProvisioningBreakConfigService extends
		AbstractReadWriteDtoService<SysProvisioningBreakConfigDto, SysProvisioningBreakConfig, SysProvisioningBreakConfigFilter>
		implements SysProvisioningBreakConfigService {

	@Autowired
	public DefaultSysProvisioningBreakConfigService(SysProvisioningBreakConfigReposiotry repository) {
		super(repository);
	}

}
