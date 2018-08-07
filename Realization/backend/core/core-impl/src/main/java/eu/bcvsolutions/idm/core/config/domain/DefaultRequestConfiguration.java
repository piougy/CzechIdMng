package eu.bcvsolutions.idm.core.config.domain;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.RequestConfiguration;

/**
 * Configuration for requests.
 * 
 * @author svandav
 *
 */
@Service(value="requestConfiguration")
public class DefaultRequestConfiguration extends AbstractConfiguration implements RequestConfiguration {	
	
	
	@Override
	public boolean isRoleRequestEnabled() {
		return getConfigurationService().getBooleanValue(PROPERTY_ROLE_ENABLE, false);
	}
	
}
