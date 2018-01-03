package eu.bcvsolutions.idm.core.api.domain;

import java.io.Serializable;

/**
 * Configuration class. Keeps configuration. 
 * @author svandav
 *
 */
public interface ConfigurationClass extends Serializable {

	
	/**
	 * Validate configuration. When is configuration invalid, then throw runtime exception.
	 */
	public void validate();
}
