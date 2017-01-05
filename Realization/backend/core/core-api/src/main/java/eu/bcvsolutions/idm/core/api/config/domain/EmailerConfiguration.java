package eu.bcvsolutions.idm.core.api.config.domain;

import eu.bcvsolutions.idm.security.api.domain.GuardedString;

/**
 * Configuration for {@link eu.bcvsolutions.idm.notification.service.api.Emailer}.
 * 
 * @author Radek Tomiška
 *
 */
public interface EmailerConfiguration {
	
	/**
	 * Property keys in configuration
	 */
	public static final String PROPERTY_PROTOCOL = "idm.sec.core.emailer.protocol";	
	public static final String PROPERTY_HOST = "idm.sec.core.emailer.host";
	public static final String PROPERTY_PORT = "idm.sec.core.emailer.port";	
	public static final String PROPERTY_USERNAME = "idm.sec.core.emailer.username";	
	public static final String PROPERTY_PASSWORD = "idm.sec.core.emailer.password";	
	public static final String PROPERTY_FROM = "idm.sec.core.emailer.from";	
	public static final String PROPERTY_TEST_ENABLED = "idm.sec.core.emailer.test.enabled";	
	
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
