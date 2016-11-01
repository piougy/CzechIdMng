package eu.bcvsolutions.idm.icf.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorInfoDto;
import eu.bcvsolutions.idm.icf.service.api.IcfConfigurationFacade;
import eu.bcvsolutions.idm.icf.service.api.IcfConfigurationService;
import eu.bcvsolutions.idm.icf.service.api.IcfConnectorService;

/**
 * Facade for get available connectors configuration
 * @author svandav
 *
 */
@Service
public class DefaultIcfConfigurationFacade implements IcfConfigurationFacade {

	private Map<String, IcfConfigurationService> icfConfigs = new HashMap<>();
	// Connector infos are cached
	private Map<String, List<IcfConnectorInfo>> icfLocalConnectorInfos;

	/**
	 * @return Configuration services for all ICFs
	 */
	public Map<String, IcfConfigurationService> getIcfConfigs() {
		return icfConfigs;
	}

	/**
	 * Return available local connectors for all ICF implementations
	 *
	 */
	public Map<String, List<IcfConnectorInfo>> getAvailableLocalConnectors() {
		if (icfLocalConnectorInfos == null) {
			icfLocalConnectorInfos = new HashMap<>();
			for (IcfConfigurationService config : icfConfigs.values()) {
				icfLocalConnectorInfos.put(config.getIcfType(), config.getAvailableLocalConnectors());
			}
		}
		return icfLocalConnectorInfos;
	}

}
