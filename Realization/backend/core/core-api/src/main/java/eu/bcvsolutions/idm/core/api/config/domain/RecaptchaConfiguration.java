package eu.bcvsolutions.idm.core.api.config.domain;

import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Configuration for ReCaptcha
 * 
 * @author Filip Mestanek
 */
public interface RecaptchaConfiguration {

	public static final String BASE_PATH = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "recaptcha" + ConfigurationService.PROPERTY_SEPARATOR;
	
	public static final String PROPERTY_URL = BASE_PATH + "url";
	public static final String PROPERTY_SECRETKEY = BASE_PATH + "secretKey";

	/**
	 * Returns google verify API URL.
	 */
	String getUrl();

	/**
	 * Returns secret key for this installation.  
	 */
	GuardedString getSecretKey();
}
