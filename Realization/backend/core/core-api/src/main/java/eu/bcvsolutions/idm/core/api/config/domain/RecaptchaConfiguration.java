package eu.bcvsolutions.idm.core.api.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Configuration for ReCaptcha
 * 
 * @author Filip Mestanek
 * @author Radek Tomiška
 */
public interface RecaptchaConfiguration extends Configurable {
	
	static final String PROPERTY_URL = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "security.recaptcha.url";
	static final String DEFAULT_URL = "https://www.google.com/recaptcha/api/siteverify";
	//
	static final String PROPERTY_SECRET_KEY = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "security.recaptcha.secretKey";
	
	@Override
	default String getConfigurableType() {
		return "recaptcha";
	}
	
	@Override
	default String getModule() {
		return "secirity";
	}
	
	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>(); // we are not using superclass properties - enable and order does not make a sence here
		properties.add(getPropertyName(PROPERTY_URL));
		properties.add(getPropertyName(PROPERTY_SECRET_KEY));
		return properties;
	}

	/**
	 * Returns google verify API URL.
	 */
	String getUrl();

	/**
	 * Returns secret key for this installation.  
	 */
	GuardedString getSecretKey();
}
