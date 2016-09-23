package eu.bcvsolutions.idm.security.exception;

import eu.bcvsolutions.idm.core.exception.CoreException;

public class ConfigurationDisabledException extends CoreException {

	private static final long serialVersionUID = 1L;
	private final String property;

	public ConfigurationDisabledException(String property) {
		super("Configuration property [" + property + "] is disabled");
		this.property = property;
	}

	public String getProperty() {
		return property;
	}

}
