package eu.bcvsolutions.idm.icf.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.api.IcfSchema;
import eu.bcvsolutions.idm.icf.domain.IcfResultCode;
import eu.bcvsolutions.idm.icf.impl.IcfConnectorInfoImpl;
import eu.bcvsolutions.idm.icf.service.api.IcfConfigurationFacade;
import eu.bcvsolutions.idm.icf.service.api.IcfConfigurationService;
import eu.bcvsolutions.idm.icf.service.api.IcfConnectorService;

/**
 * Facade for get available connectors configuration
 * 
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
	@Override
	public Map<String, IcfConfigurationService> getIcfConfigs() {
		return icfConfigs;
	}

	/**
	 * Return available local connectors for all ICF implementations
	 *
	 */
	@Override
	public Map<String, List<IcfConnectorInfo>> getAvailableLocalConnectors() {
		if (icfLocalConnectorInfos == null) {
			icfLocalConnectorInfos = new HashMap<>();
			for (IcfConfigurationService config : icfConfigs.values()) {
				icfLocalConnectorInfos.put(config.getIcfType(), config.getAvailableLocalConnectors());
			}
		}
		return icfLocalConnectorInfos;
	}

	@Override
	public IcfSchema getSchema(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration) {
		Assert.notNull(key);
		checkIcfType(key);
		return icfConfigs.get(key.getIcfType()).getSchema(key, connectorConfiguration);
	}

	private boolean checkIcfType(IcfConnectorKey key) {
		if (!icfConfigs.containsKey(key.getIcfType())) {
			throw new ResultCodeException(IcfResultCode.ICF_IMPLEMENTATTION_NOT_FOUND,
					ImmutableMap.of("icf", key.getIcfType()));
		}
		return true;
	}

	@Override
	public IcfConnectorConfiguration getConnectorConfiguration(IcfConnectorKey key) {
		Assert.notNull(key);
		Assert.notNull(key.getIcfType());
		checkIcfType(key); 
		return this.getIcfConfigs()
			.get(key.getIcfType()).getConnectorConfiguration(key);
	}

}
