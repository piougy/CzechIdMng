package eu.bcvsolutions.idm.ic.czechidm.service.impl;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.ic.api.IcConnector;
import eu.bcvsolutions.idm.ic.api.IcConnectorClass;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcConnectorServer;
import eu.bcvsolutions.idm.ic.api.IcSchema;
import eu.bcvsolutions.idm.ic.connid.service.impl.ConnIdIcConfigurationService;
import eu.bcvsolutions.idm.ic.exception.IcException;
import eu.bcvsolutions.idm.ic.impl.IcConfigurationPropertiesImpl;
import eu.bcvsolutions.idm.ic.impl.IcConfigurationPropertyImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorConfigurationImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorInfoImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorKeyImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationFacade;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationService;

/**
 * CzechIdM connector framework implementation
 * 
 * @author svandav
 *
 */
@Service
public class CzechIdMIcConfigurationService implements IcConfigurationService {

	private static final Logger LOG = LoggerFactory.getLogger(ConnIdIcConfigurationService.class);

	// Cached local connectorInfos
	private Set<IcConnectorInfo> connectorInfos;
	// Cached local default connector configurations
	private Map<String, IcConnectorConfiguration> connectorsConfigurations;
	@Value("#{'${ic.localconnector.packages}'.split(',')}")
	private List<String> localConnectorsPackages;

	@Autowired
	public CzechIdMIcConfigurationService(IcConfigurationFacade icConfigurationAggregator) {
		if (icConfigurationAggregator.getIcConfigs() == null) {
			throw new IcException("Map of IC implementations is not defined!");
		}
		if (icConfigurationAggregator.getIcConfigs().containsKey(IMPLEMENTATION_TYPE)) {
			throw new IcException(
					MessageFormat.format("IC implementation duplicity for key: {0}", IMPLEMENTATION_TYPE));
		}
		// Disable for now
		icConfigurationAggregator.getIcConfigs().put(IMPLEMENTATION_TYPE, this);
	}

	final private static String IMPLEMENTATION_TYPE = "czechidm";

	/**
	 * Return key defined IC implementation
	 * 
	 * @return
	 */
	@Override
	public String getFramework() {
		return IMPLEMENTATION_TYPE;
	}

	/**
	 * Return available local connectors for this IC implementation
	 * 
	 * @return
	 */
	@Override
	public Set<IcConnectorInfo> getAvailableLocalConnectors() {	
		connectorInfos = null;
		if (connectorInfos == null) {
			connectorInfos = new HashSet<>();
			connectorsConfigurations = new HashMap<>();
			List<Class<?>> annotated = new ArrayList<>();
			// Find all class with annotation IcConnectorClass under specific
			// packages
			localConnectorsPackages.forEach(packageWithConnectors -> {
				Reflections reflections = new Reflections(packageWithConnectors);
				annotated.addAll(reflections.getTypesAnnotatedWith(IcConnectorClass.class));
			});

			LOG.info(MessageFormat.format("Found annotated classes with IcConnectorClass [{0}]", annotated));

			for (Class<?> clazz : annotated) {
				URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
				IcConnectorClass connectorAnnotation = clazz.getAnnotation(IcConnectorClass.class);
				if(!this.getFramework().equals(connectorAnnotation.framework())){
					continue;
				}
				
				IcConnectorKey key = new IcConnectorKeyImpl(connectorAnnotation.framework(), clazz.getName(),
						connectorAnnotation.version(), connectorAnnotation.name());
				IcConnectorInfo info = new IcConnectorInfoImpl(connectorAnnotation.displayName(),
						connectorAnnotation.framework(), key);
				connectorInfos.add(info);
				if(!IcConnector.class.isAssignableFrom(clazz)){
					throw new IcException(MessageFormat.format("Cannot create instance of CzechIdM connector [{0}]! Connector class must be child of [0]!", IcConnector.class.getSimpleName()));
				}
		
				try {
					IcConnector connector = (IcConnector) clazz.newInstance();
					connectorsConfigurations.put(info.getConnectorKey().getFullName(), connector.getDefaultConfiguration());
				} catch (InstantiationException | IllegalAccessException e) {
					throw new IcException(MessageFormat.format("Cannot create instance of CzechIdM connector [{0}]!", connectorAnnotation.name()), e);
				}
			}
			LOG.info(MessageFormat.format("Found all local connector connectorInfos [{0}]", connectorInfos.toString()));
		}
		return connectorInfos;
	}

	/**
	 * Return find connector default configuration by connector key
	 * 
	 * @param key
	 * @return
	 */
	@Override
	public IcConnectorConfiguration getConnectorConfiguration(IcConnectorInstance connectorInstance) {
		Assert.notNull(connectorInstance.getConnectorKey());
		//
		return this.connectorsConfigurations.get(connectorInstance.getConnectorKey().getFullName());

	}

	@Override
	public IcSchema getSchema(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration) {
		return null;
	}

	@Override
	public IcConnectorConfiguration getRemoteConnectorConfiguration(IcConnectorInstance connectorInstance) {
		return null;
	}

	@Override
	public Set<IcConnectorInfo> getAvailableRemoteConnectors(IcConnectorServer server) {
		return null;
	}

	public void validate(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void test(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration) {
		// TODO Auto-generated method stub
	}
}
