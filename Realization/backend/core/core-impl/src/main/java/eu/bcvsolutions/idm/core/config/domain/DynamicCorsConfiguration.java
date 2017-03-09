package eu.bcvsolutions.idm.core.config.domain;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;

import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;

/**
 * Cors configuration extension - allowed origins could be configured dynamically through {@link IdmConfigurationService}.
 * 
 * @author Radek Tomiška 
 *
 */
public class DynamicCorsConfiguration extends CorsConfiguration {
	
	public static final String PROPERTY_ALLOWED_ORIGIN = "idm.pub.security.allowed-origins";
	public static final String PROPERTY_ALLOWED_ORIGIN_SEPARATOR = ",";
	
	@Autowired
	private ConfigurationService configurationService;
	
	/**
	 * Check the origin of the request against the configured allowed origins. 
	 * Allowed origins could be configured dynamically through {@link IdmConfigurationService}. 
	 *  
	 * @param requestOrigin the origin to check
	 * @return the origin to use for the response, possibly {@code null} which
	 * means the request origin is not allowed
	 */
	@Override
	public String checkOrigin(String requestOrigin) {
		if (!StringUtils.hasText(requestOrigin)) {
			return null;
		}
		List<String> allowedOrigins= getAllowedOrigins();
		
		if (ObjectUtils.isEmpty(allowedOrigins)) {
			return null;
		}

		if (allowedOrigins.contains(ALL)) {
			if (!Boolean.TRUE.equals(this.getAllowCredentials())) {
				return ALL;
			}
			else {
				return requestOrigin;
			}
		}
		for (String allowedOrigin : allowedOrigins) {
			if (requestOrigin.equalsIgnoreCase(allowedOrigin)) {
				return requestOrigin;
			}
		}

		return null;
	}
	
	/**
	 * Returns configured allowed origins
	 */
	@Override
	public List<String> getAllowedOrigins() {
		String allowedOrigins = configurationService.getValue(PROPERTY_ALLOWED_ORIGIN);
		if (StringUtils.isEmpty(allowedOrigins)) {
			return Collections.<String>emptyList();
		}
		// we want to replace white spaces and split by separator
		return Arrays.asList(allowedOrigins.replaceAll("\\s*", "").split(PROPERTY_ALLOWED_ORIGIN_SEPARATOR));
	}
	
}
