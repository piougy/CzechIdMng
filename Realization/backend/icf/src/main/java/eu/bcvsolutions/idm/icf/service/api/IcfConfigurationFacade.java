package eu.bcvsolutions.idm.icf.service.api;

import java.util.List;
import java.util.Map;

import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.api.IcfSchema;

/**
 * Facade for get available connectors configuration
 * @author svandav
 *
 */
public interface IcfConfigurationFacade {

	/**
	 * Return available local connectors for all ICF implementations
	 *
	 */
	Map<String, List<IcfConnectorInfo>> getAvailableLocalConnectors();

	/**
	 * Return find connector default configuration by connector info
	 * @param info
	 * @return
	 */
	Map<String, IcfConfigurationService> getIcfConfigs();

	/**
	 * Return schema for connector and given configuration. Schema contains list of attribute definitions in object classes.
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @return
	 */
	IcfSchema getSchema(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration);

}