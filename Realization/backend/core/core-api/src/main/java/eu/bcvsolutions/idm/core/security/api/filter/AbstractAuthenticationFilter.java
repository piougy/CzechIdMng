package eu.bcvsolutions.idm.core.security.api.filter;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Super class for all common authentication filters in IdM.
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractAuthenticationFilter implements IdmAuthenticationFilter {

	@Autowired private ConfigurationService configurationService;
	
	@Override
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}
	
}
