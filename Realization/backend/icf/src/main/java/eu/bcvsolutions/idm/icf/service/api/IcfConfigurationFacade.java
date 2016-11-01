package eu.bcvsolutions.idm.icf.service.api;

import java.util.List;
import java.util.Map;

import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;

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
	public Map<String, List<IcfConnectorInfo>> getAvailableLocalConnectors();

	/**
	 * Return find connector default configuration by connector info
	 * @param info
	 * @return
	 */
	public Map<String, IcfConfigurationService> getIcfConfigs();

}