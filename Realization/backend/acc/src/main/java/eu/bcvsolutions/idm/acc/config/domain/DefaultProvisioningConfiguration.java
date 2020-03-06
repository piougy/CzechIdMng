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
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultProvisioningConfiguration.class);
	//
	@Autowired private ProvisioningBreakConfiguration provisioningBreakConfiguration;
	
	@Override
	public boolean isSendPasswordAttributesTogether() {
		return getConfigurationService().getBooleanValue(PROPERTY_SEND_PASSWORD_ATTRIBUTES_TOGETHER, DEFAULT_SEND_PASSWORD_ATTRIBUTES_TOGETHER);
	}

	@Override
	public ProvisioningBreakConfiguration getBeakConfiguration() {
		return provisioningBreakConfiguration;
	}

	@Override
	public boolean isAllowedAutoMappingOnExistingAccount() {
		return getConfigurationService().getBooleanValue(PROPERTY_ALLOW_AUTO_MAPPING_ON_EXISTING_ACCOUNT, DEFAULT_ALLOW_AUTO_MAPPING_ON_EXISTING_ACCOUNT);
	}
	
	@Override
	public long getTimeout() {
		long timeout = getConfigurationService().getLongValue(PROPERTY_TIMEOUT, DEFAULT_TIMEOUT);
		if (timeout <= 1000) {
			LOG.warn("Configured provisioning timeout has to be greater than [1000]ms, given [{}]. Default timeout [{}] will be used, change your configuration property [{}].",
					timeout, DEFAULT_TIMEOUT, PROPERTY_TIMEOUT);
			return DEFAULT_TIMEOUT;
		}
		//
		return timeout;
	}
}
