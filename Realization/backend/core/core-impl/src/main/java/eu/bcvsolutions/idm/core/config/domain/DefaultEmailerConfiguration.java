package eu.bcvsolutions.idm.core.config.domain;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.EmailerConfiguration;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Configuration for {@link eu.bcvsolutions.idm.core.notification.api.service.Emailer}.
 * 
 * @author Radek Tomiška
 *
 */
@Component("emailerConfiguration")
public class DefaultEmailerConfiguration extends AbstractConfiguration implements EmailerConfiguration {
	
	@Override
	public String getProtocol() {
		return getConfigurationService().getValue(PROPERTY_PROTOCOL, DEFAULT_PROTOCOL);
	}

	@Override
	public String getHost() {
		return getConfigurationService().getValue(PROPERTY_HOST, DEFAULT_HOST);
	}
	
	@Override
	public String getPort() {
		return getConfigurationService().getValue(PROPERTY_PORT, DEFAULT_PORT);
	}
	
	@Override
	public String getFrom() {
		return getConfigurationService().getValue(PROPERTY_FROM);
	}
	
	@Override
	public String getUsername() {
		return getConfigurationService().getValue(PROPERTY_USERNAME);
	}
	
	@Override
	public GuardedString getPassword() {
		return getConfigurationService().getGuardedValue(PROPERTY_PASSWORD);
	}
	
	@Override
	public boolean isTestEnabled() {
		return getConfigurationService().getBooleanValue(PROPERTY_TEST_ENABLED, true);
	}
}
