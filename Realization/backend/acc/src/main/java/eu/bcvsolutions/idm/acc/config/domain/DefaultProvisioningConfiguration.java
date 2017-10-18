package eu.bcvsolutions.idm.acc.config.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;

/**
 * Configuration for provisioning
 * 
 * @author Radek Tomiška
 *
 */
@Component("provisioningConfiguration")
public class DefaultProvisioningConfiguration extends AbstractConfiguration implements ProvisioningConfiguration {
	
	@Autowired private ProvisioningBreakConfiguration provisioningBreakConfiguration;
	
	@Override
	public boolean isSupportSendPasswordAttributes() {
		return getConfigurationService().getBooleanValue(PROPERTY_SUPPORT_SEND_PASSWORD_ATTRIBUTES, DEFAULT_SUPPORT_SEND_PASSWORD_ATTRIBUTES);
	}

	@Override
	public ProvisioningBreakConfiguration getBeakConfiguration() {
		return provisioningBreakConfiguration;
	}
}
