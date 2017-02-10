package eu.bcvsolutions.idm.ic.service.api;

import java.util.List;
import java.util.Map;

import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcSchema;

/**
 * Facade for get available connectors configuration
 * @author svandav
 *
 */
public interface IcConfigurationFacade {

	/**
	 * Return available local connectors for all IC implementations
	 *
	 */
	Map<String, List<IcConnectorInfo>> getAvailableLocalConnectors();

	/**
	 * Return all registered IC configuration service implementations
	 * @return
	 */
	Map<String, IcConfigurationService> getIcConfigs();
	
	/**
	 * Return find connector default configuration by connector key
	 * 
	 * @param key
	 * @return
	 */
	IcConnectorConfiguration getConnectorConfiguration(IcConnectorInstance connectorInstance);
	

	/**
	 * Return schema for connector and given configuration. Schema contains list of attribute definitions in object classes.
	 * @param connectorInstance - Identification of connector instance with connector key and conector server
	 * @param connectorConfiguration - Connector configuration
	 * @return
	 */
	IcSchema getSchema(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration);
	
	/**
	 * Return list of available remote connectors for IcConnectorServer
	 * 
	 * @param server
	 * @return
	 */
	List<IcConnectorInfo> getAvailableRemoteConnectors(IcConnectorInstance connectorInstance);

	
	/**
	 * Check if is connector configuration valid
	 * @param connectorInstance - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 */
	void validate(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration);
	
	/**
	 * Check if is connector works fine
	 * @param connectorInstance - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 */
	void test(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration);


}