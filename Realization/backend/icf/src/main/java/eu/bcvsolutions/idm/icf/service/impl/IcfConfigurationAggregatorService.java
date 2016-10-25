package eu.bcvsolutions.idm.icf.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorInfoDto;
import eu.bcvsolutions.idm.icf.service.api.IcfConfigurationService;

@Service
public class IcfConfigurationAggregatorService {
	
	private Map<String, IcfConfigurationService> icfs = new HashMap<>();

	/**
	 * @return the icfs
	 */
	public Map<String, IcfConfigurationService> getIcfs() {
		return icfs;
	}
	
	/**
	 * Return available local connectors for all ICF implementations
	 * @return 
	 * 
	 * @return
	 */
	public Map<String, List<IcfConnectorInfo>> getAvailableLocalConnectors() {
		Map<String, List<IcfConnectorInfo>> allInfos = new HashMap<>();
		for(IcfConfigurationService config : icfs.values()){
			allInfos.put(config.getIcfType(), config.getAvailableLocalConnectors());
		}
		return allInfos;
	}
}
