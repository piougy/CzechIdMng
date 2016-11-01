package eu.bcvsolutions.idm.icf.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorInfoDto;
import eu.bcvsolutions.idm.icf.service.api.IcfConfigurationService;
import eu.bcvsolutions.idm.icf.service.api.IcfConnectorService;

@Service
public class IcfConfigurationAggregatorService {

	private Map<String, IcfConfigurationService> icfConfigs = new HashMap<>();
	private Map<String, IcfConnectorService> icfConnectors = new HashMap<>();
	// Connector infos are cached
	private Map<String, List<IcfConnectorInfo>> icfLocalConnectorInfos;

	/**
	 * @return Configuration services for all ICFs
	 */
	public Map<String, IcfConfigurationService> getIcfConfigs() {
		return icfConfigs;
	}

	/**
	 * @return Connector services for all ICFs
	 */
	public Map<String, IcfConnectorService> getIcfConnectors() {
		return icfConnectors;
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
