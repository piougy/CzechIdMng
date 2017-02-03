package eu.bcvsolutions.idm.core.config.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.config.domain.EmailerConfiguration;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Configuration for {@link eu.bcvsolutions.idm.core.notification.service.api.Emailer}.
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class DefaultEmailerConfiguration implements EmailerConfiguration {

	protected static final String DEFAULT_PROTOCOL = "smtp";
	protected static final String DEFAULT_HOST = "localhost";
	protected static final String DEFAULT_PORT = "25";
	protected static final boolean DEFAULT_TEST_ENABLED = true;
	
	private final ConfigurationService configurationService;
	
	@Autowired
	public DefaultEmailerConfiguration(ConfigurationService configurationService) {
		Assert.notNull(configurationService, "Configuration is required for loading default email configuration");
		//
		this.configurationService = configurationService;
	}
	
	@Override
	public String getProtocol() {
		return configurationService.getValue(PROPERTY_PROTOCOL, DEFAULT_PROTOCOL);
	}

	@Override
	public String getHost() {
		return configurationService.getValue(PROPERTY_HOST, DEFAULT_HOST);
	}
	
	@Override
	public String getPort() {
		return configurationService.getValue(PROPERTY_PORT, DEFAULT_PORT);
	}
	
	@Override
	public String getFrom() {
		return configurationService.getValue(PROPERTY_FROM);
	}
	
	@Override
	public String getUsername() {
		return configurationService.getValue(PROPERTY_USERNAME);
	}
	
	@Override
	public GuardedString getPassword() {
		return configurationService.getGuardedValue(PROPERTY_PASSWORD);
	}
	
	@Override
	public boolean isTestEnabled() {
		return configurationService.getBooleanValue(PROPERTY_TEST_ENABLED, true);
	}
}
