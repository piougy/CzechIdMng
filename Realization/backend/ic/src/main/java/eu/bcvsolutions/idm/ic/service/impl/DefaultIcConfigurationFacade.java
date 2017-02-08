package eu.bcvsolutions.idm.ic.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcConnectorServer;
import eu.bcvsolutions.idm.ic.api.IcSchema;
import eu.bcvsolutions.idm.ic.domain.IcResultCode;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationFacade;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationService;

/**
 * Facade for get available connectors configuration
 * 
 * @author svandav
 *
 */
@Service
public class DefaultIcConfigurationFacade implements IcConfigurationFacade {

	private Map<String, IcConfigurationService> icConfigs = new HashMap<>();
	// Connector infos are cached
	private Map<String, List<IcConnectorInfo>> icLocalConnectorInfos;
	
	/**
	 * @return Configuration services for all ICs
	 */
	@Override
	public Map<String, IcConfigurationService> getIcConfigs() {
		return icConfigs;
	}

	/**
	 * Return available local connectors for all IC implementations
	 *
	 */
	@Override
	public Map<String, List<IcConnectorInfo>> getAvailableLocalConnectors() {
		if (icLocalConnectorInfos == null) {
			icLocalConnectorInfos = new HashMap<>();
			for (IcConfigurationService config : icConfigs.values()) {
				icLocalConnectorInfos.put(config.getImplementationType(), config.getAvailableLocalConnectors());
			}
		}
		return icLocalConnectorInfos;
	}

	@Override
	public IcSchema getSchema(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration) {
		Assert.notNull(connectorInstance.getConnectorKey());
		checkIcType(connectorInstance.getConnectorKey());
		return icConfigs.get(connectorInstance.getConnectorKey().getFramework()).getSchema(connectorInstance.getConnectorKey(), connectorConfiguration);
	}
	
	@Override
	public void test(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration) {
		Assert.notNull(key);
		checkIcType(key);
		icConfigs.get(key.getFramework()).test(key, connectorConfiguration);
		
	}
	
	@Override
	public void validate(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration) {
		Assert.notNull(key);
		checkIcType(key);
		icConfigs.get(key.getFramework()).validate(key, connectorConfiguration);
	}

	private boolean checkIcType(IcConnectorKey key) {
		if (!icConfigs.containsKey(key.getFramework())) {
			throw new ResultCodeException(IcResultCode.IC_FRAMEWORK_NOT_FOUND,
					ImmutableMap.of("ic", key.getFramework()));
		}
		return true;
	}
	
	@Override
	public List<IcConnectorInfo> getAvailableRemoteConnectors(IcConnectorInstance connectorInstance) {
		List<IcConnectorInfo> remoteConnectors = new ArrayList<>();
		// get service from icConfig, get all available remote connector for service in configs
		for (IcConfigurationService config : icConfigs.values()) {
			remoteConnectors.addAll(config.getAvailableRemoteConnectors(connectorInstance.getConnectorServer()));
		}
		return remoteConnectors;
	}

	@Override
	public IcConnectorConfiguration getConnectorConfiguration(IcConnectorInstance connectorInstance) {
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorInstance.getConnectorKey().getFramework());
		checkIcType(connectorInstance.getConnectorKey()); 
		return this.getIcConfigs()
			.get(connectorInstance.getConnectorKey().getFramework()).getConnectorConfiguration(connectorInstance.getConnectorKey());
	}
}
