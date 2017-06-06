package eu.bcvsolutions.idm.core.config.domain;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.RecaptchaConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Default implementation of configuration for ReCaptcha.
 * 
 * @author Filip Mestanek
 * @author Radek Tomiška
 */
@Component("recaptchaConfiguration")
public class DefaultRecaptchaConfiguration extends AbstractConfiguration implements RecaptchaConfiguration {

	@Override
	public String getUrl() {
		return getConfigurationService().getValue(PROPERTY_URL, DEFAULT_URL);
	}

	@Override
	public GuardedString getSecretKey() {
		GuardedString sk = getConfigurationService().getGuardedValue(PROPERTY_SECRET_KEY);
		//
		if (sk == null) {
			throw new ResultCodeException(CoreResultCode.RECAPTCHA_SECRET_KEY_MISSING,
					ImmutableMap.of("property", PROPERTY_SECRET_KEY));
		}		
		return sk;
	}
}
