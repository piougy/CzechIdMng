package eu.bcvsolutions.idm.ic.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcSchema;
import eu.bcvsolutions.idm.ic.domain.IcResultCode;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationFacade;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationService;

/**
 * Facade for get available connectors configuration
 * 
 * @author svandav
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIcConfigurationFacade implements IcConfigurationFacade {

	// registered ic configuration services
	private final PluginRegistry<IcConfigurationService, String> configurationServices;
	// local connector infos are cached
	private final Map<String, Set<IcConnectorInfo>> icLocalConnectorInfos  = new HashMap<>();
	
	@Autowired
	public DefaultIcConfigurationFacade(List<? extends IcConfigurationService> configurationServices) {
		this.configurationServices = OrderAwarePluginRegistry.create(configurationServices);
		for (IcConfigurationService config : this.configurationServices.getPlugins()) {
			icLocalConnectorInfos.put(config.getFramework(), config.getAvailableLocalConnectors());
		}
	}
	
	/**
	 * 
	 * @return Configuration services for all ICs
	 */
	@Override
	public Map<String, IcConfigurationService> getIcConfigs() {
		return configurationServices
				.getPlugins()
				.stream()
				.collect(Collectors.toMap(service -> service.getFramework(), service -> service));
	}

	/**
	 * Return available local connectors for all IC implementations
	 *
	 */
	@Override
	public Map<String, Set<IcConnectorInfo>> getAvailableLocalConnectors() {
		return icLocalConnectorInfos;
	}

	@Override
	public Set<IcConnectorInfo> getAvailableRemoteConnectors(IcConnectorInstance connectorInstance) {
		Set<IcConnectorInfo> remoteConnectors = new HashSet<>();
		// get service from icConfig, get all available remote connector for service in configs
		for (IcConfigurationService config : configurationServices.getPlugins()) {
			remoteConnectors.addAll(config.getAvailableRemoteConnectors(connectorInstance.getConnectorServer()));
		}
		return remoteConnectors;
	}

	@Override
	public IcSchema getSchema(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration) {
		Assert.notNull(connectorInstance.getConnectorKey());
		checkIcType(connectorInstance.getConnectorKey());
		//
		return configurationServices.getPluginFor(connectorInstance.getConnectorKey().getFramework()).getSchema(connectorInstance, connectorConfiguration);
	}
	
	@Override
	public void test(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration) {
		Assert.notNull(connectorInstance.getConnectorKey());
		checkIcType(connectorInstance.getConnectorKey());
		if (connectorInstance.isRemote()) {
			Assert.notNull(connectorInstance.getConnectorServer());
		}
		configurationServices.getPluginFor(connectorInstance.getConnectorKey().getFramework()).test(connectorInstance, connectorConfiguration);
		
	}
	
	@Override
	public void validate(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration) {
		Assert.notNull(connectorInstance.getConnectorKey());
		checkIcType(connectorInstance.getConnectorKey());
		configurationServices.getPluginFor(connectorInstance.getConnectorKey().getFramework()).validate(connectorInstance, connectorConfiguration);
	}

	private boolean checkIcType(IcConnectorKey key) {
		if (!configurationServices.hasPluginFor(key.getFramework())) {
			throw new ResultCodeException(IcResultCode.IC_FRAMEWORK_NOT_FOUND,
					ImmutableMap.of("ic", key.getFramework()));
		}
		return true;
	}
	

	@Override
	public IcConnectorConfiguration getConnectorConfiguration(IcConnectorInstance connectorInstance) {
		Assert.notNull(connectorInstance.getConnectorKey());
		Assert.notNull(connectorInstance.getConnectorKey().getFramework());
		checkIcType(connectorInstance.getConnectorKey());
		//
		return configurationServices.getPluginFor(connectorInstance.getConnectorKey().getFramework()).getConnectorConfiguration(connectorInstance);
	}
}
