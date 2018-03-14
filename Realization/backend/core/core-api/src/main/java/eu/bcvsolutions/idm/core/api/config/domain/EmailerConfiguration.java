package eu.bcvsolutions.idm.core.api.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Configuration for {@link eu.bcvsolutions.idm.notification.service.api.Emailer}.
 * 
 * @author Radek Tomiška
 */
public interface EmailerConfiguration extends Configurable {
	
	/**
	 * Property keys in configuration
	 */
	String PROPERTY_PROTOCOL = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.emailer.protocol";	
	String DEFAULT_PROTOCOL = "smtp";
	//
	String PROPERTY_HOST = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.emailer.host";
	String DEFAULT_HOST = "localhost";
	//
	String PROPERTY_PORT = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.emailer.port";
	String DEFAULT_PORT = "25";
	//
	String PROPERTY_USERNAME = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.emailer.username";	
	String PROPERTY_PASSWORD = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.emailer.password";	
	String PROPERTY_FROM = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.emailer.from";
	//
	String PROPERTY_TEST_ENABLED = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.emailer.test.enabled";	
	boolean DEFAULT_TEST_ENABLED = true;
	
	@Override
	default java.lang.String getConfigurableType() {
		return "emailer";
	}
	
	@Override
	default boolean isDisableable() {
		return false;
	}
	
	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>(); // we are not using superclass properties - enable and order does not make a sence here
		properties.add(getPropertyName(PROPERTY_PROTOCOL));
		properties.add(getPropertyName(PROPERTY_HOST));
		properties.add(getPropertyName(PROPERTY_PORT));
		properties.add(getPropertyName(PROPERTY_USERNAME));
		properties.add(getPropertyName(PROPERTY_PASSWORD));
		properties.add(getPropertyName(PROPERTY_FROM));
		properties.add(getPropertyName(PROPERTY_TEST_ENABLED));
		return properties;
	}
	
	/**
	 * Email server protocol
	 * 
	 * @return
	 */
	String getProtocol();
	
	/**
	 * Email server
	 * 
	 * @return
	 */
	String getHost();
	
	/**
	 * Email server port
	 * 
	 * @return
	 */
	String getPort();
	
	/**
	 * Default email sender
	 * 
	 * @return
	 */
	String getFrom();
	
	/**
	 * Email - username
	 * 
	 * @return
	 */
	String getUsername();
	
	/**
	 * Email - password
	 * 
	 * @return
	 */
	GuardedString getPassword();
	
	/**
	 * Test mode
	 * 
	 * @return
	 */
	boolean isTestEnabled();
}
