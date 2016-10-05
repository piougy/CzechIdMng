package eu.bcvsolutions.idm.core.config.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.service.IdmConfigurationService;

/**
 * Configuration for {@link eu.bcvsolutions.idm.notification.service.Emailer}.
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class EmailerConfiguration {

	@Autowired
	private IdmConfigurationService configurationService;
	
	public static final String PROPERTY_PROTOCOL = "idm.sec.core.emailer.protocol";
	protected static final String DEFAULT_PROTOCOL = "smtp"; 
	
	public static final String PROPERTY_HOST = "idm.sec.core.emailer.host";
	protected static final String DEFAULT_HOST = "localhost"; 

	public static final String PROPERTY_PORT = "idm.sec.core.emailer.port";
	protected static final String DEFAULT_PORT = "25";
	
	public static final String PROPERTY_USERNAME = "idm.sec.core.emailer.username";
	
	public static final String PROPERTY_PASSWORD = "idm.sec.core.emailer.password";
	
	public static final String PROPERTY_FROM = "idm.sec.core.emailer.from";
	
	public static final String PROPERTY_TEST_ENABLED = "idm.sec.core.emailer.test.enabled";
	protected static final boolean DEFAULT_TEST_ENABLED = true;
	
	
	public String getProtocol() {
		return configurationService.getValue(PROPERTY_PROTOCOL, DEFAULT_PROTOCOL);
	}
	
	public String getHost() {
		return configurationService.getValue(PROPERTY_HOST, DEFAULT_HOST);
	}
	
	public String getPort() {
		return configurationService.getValue(PROPERTY_PORT, DEFAULT_PORT);
	}
	
	public String getFrom() {
		return configurationService.getValue(PROPERTY_FROM);
	}
	
	public String getUsername() {
		return configurationService.getValue(PROPERTY_USERNAME);
	}
	
	public String getPassword() {
		return configurationService.getValue(PROPERTY_PASSWORD);
	}
	
	public boolean isTestEnabled() {
		return configurationService.getBooleanValue(PROPERTY_TEST_ENABLED, true);
	}
}
