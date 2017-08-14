package eu.bcvsolutions.idm.ic.api;

/**
 * Connector class - basic interface for every connector for CzechIdM framework
 * @author svandav
 *
 */
public interface IcConnector {
	
	/**
	 * Init connector by given configuration
	 * @param configuration
	 */
	public void init(IcConnectorConfiguration configuration);


}
