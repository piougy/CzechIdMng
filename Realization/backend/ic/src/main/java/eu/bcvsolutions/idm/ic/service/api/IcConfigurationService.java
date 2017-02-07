package eu.bcvsolutions.idm.ic.service.api;

import java.util.List;

import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcSchema;

public interface IcConfigurationService {

	/**
	 * Return key defined IC implementation
	 * @return
	 */
	String getImplementationType();

	/**
	 * Return available local connectors for this IC implementation
	 * @return
	 */
	List<IcConnectorInfo> getAvailableLocalConnectors();

	/**
	 * Return find connector default configuration by connector key
	 * @param info
	 * @return
	 */
	IcConnectorConfiguration getConnectorConfiguration(IcConnectorKey key);
	

	/**
	 * Return schema for connector and given configuration. Schema contains list of attribute definitions in object classes.
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @return
	 */
	IcSchema getSchema(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration);
	
	/**
	 * Check if is connector configuration valid
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 */
	void validate(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration);
	
	/**
	 * Check if is connector works fine
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 */
	void test(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration);

}