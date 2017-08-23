package eu.bcvsolutions.idm.ic.api;

import java.io.Serializable;

/**
 * Connector configuration class. Keeps configuration for connector. 
 * @author svandav
 *
 */
public interface IcConnectorConfigurationClass extends Serializable {

	
	/**
	 * Validate connector configuration. When is configuration invalid, then throw runtime exception.
	 */
	public void validate();
}
