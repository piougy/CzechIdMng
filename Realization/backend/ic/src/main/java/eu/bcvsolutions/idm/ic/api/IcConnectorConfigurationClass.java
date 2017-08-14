package eu.bcvsolutions.idm.ic.api;

/**
 * Connector configuration class. Keeps configuration for connector. 
 * @author svandav
 *
 */
public interface IcConnectorConfigurationClass {

	
	/**
	 * Validate connector configuration. When is configuration invalid, then throw runtime exception.
	 */
	public void validate();
}
