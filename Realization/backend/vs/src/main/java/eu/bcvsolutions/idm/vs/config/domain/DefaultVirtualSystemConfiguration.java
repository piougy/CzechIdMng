package eu.bcvsolutions.idm.vs.config.domain;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;

/**
 * Virtual system configuration - implementation
 * 
 * @author Svanda
 *
 */
@Component("virtualSystemConfiguration")
public class DefaultVirtualSystemConfiguration 
		extends AbstractConfiguration
		implements VirtualSystemConfiguration {

	@Override
	public String getPrivateValue() {
		return getConfigurationService().getValue(PROPERTY_PRIVATE);
	}

	@Override
	public String getConfidentialValue() {
		return getConfigurationService().getValue(PROPERTY_CONFIDENTIAL);
	}
}
