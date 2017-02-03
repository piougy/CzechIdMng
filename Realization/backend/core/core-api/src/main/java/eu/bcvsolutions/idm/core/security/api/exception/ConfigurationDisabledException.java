package eu.bcvsolutions.idm.core.security.api.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Configuration property is disabled
 * 
 * @author Radek Tomiška
 * 
 * @see ConfigurationService
 *
 */
public class ConfigurationDisabledException extends ResultCodeException  {

	private static final long serialVersionUID = 1L;
	private final String property;

	public ConfigurationDisabledException(String property) {
		super(CoreResultCode.CONFIGURATION_DISABLED, ImmutableMap.of("property", property));
		this.property = property;
	}

	public String getProperty() {
		return property;
	}

}
