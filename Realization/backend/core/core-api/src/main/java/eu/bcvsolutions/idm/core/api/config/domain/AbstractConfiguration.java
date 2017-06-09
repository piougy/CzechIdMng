package eu.bcvsolutions.idm.core.api.config.domain;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Common configuration unit.
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractConfiguration implements Configurable {
	
	@Autowired private ConfigurationService configurationService;
	
	@Override
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

}
