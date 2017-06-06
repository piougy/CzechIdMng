package eu.bcvsolutions.idm.core.config.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.domain.RecaptchaConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Default implementation of configuration for ReCaptcha.
 * 
 * @author Filip Mestanek
 */
@Component
public class DefaultRecaptchaConfiguration implements RecaptchaConfiguration {

	protected final ConfigurationService configurationService;

	@Autowired
	public DefaultRecaptchaConfiguration(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
	
	@Override
	public String getUrl() {
		return configurationService.getValue(PROPERTY_URL, "https://www.google.com/recaptcha/api/siteverify");
	}

	@Override
	public GuardedString getSecretKey() {
		GuardedString sk = configurationService.getGuardedValue(PROPERTY_SECRETKEY, null);
		
		if (sk == null) {
			throw new ResultCodeException(CoreResultCode.WRONG_CONFIGURATION_DATA,
					ImmutableMap.of("prop", PROPERTY_SECRETKEY, "message", "No value provided"));
		}
		
		return sk;
	}
}
