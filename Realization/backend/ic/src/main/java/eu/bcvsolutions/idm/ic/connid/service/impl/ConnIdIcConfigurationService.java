package eu.bcvsolutions.idm.ic.connid.service.impl;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcSchema;
import eu.bcvsolutions.idm.ic.connid.domain.ConnIdIcConvertUtil;
import eu.bcvsolutions.idm.ic.exception.IcException;
import eu.bcvsolutions.idm.ic.impl.IcConnectorInfoImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorKeyImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationFacade;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationService;

@Service
public class ConnIdIcConfigurationService implements IcConfigurationService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConnIdIcConfigurationService.class);

	// Cached local connid managers
	private List<ConnectorInfoManager> managers;
	@Value("#{'${ic.localconnector.packages}'.split(',')}")
	private List<String> localConnectorsPackages;

	@Autowired
	public ConnIdIcConfigurationService(IcConfigurationFacade icConfigurationAggregator) {
		if (icConfigurationAggregator.getIcConfigs() == null) {
			throw new IcException("Map of IC implementations is not defined!");
		}
		if (icConfigurationAggregator.getIcConfigs().containsKey(IMPLEMENTATION_TYPE)) {
			throw new IcException(
					MessageFormat.format("IC implementation duplicity for key: {0}", IMPLEMENTATION_TYPE));
		}
		icConfigurationAggregator.getIcConfigs().put(IMPLEMENTATION_TYPE, this);
	}

	final private static String IMPLEMENTATION_TYPE = "connId";

	/**
	 * Return key defined IC implementation
	 * 
	 * @return
	 */
	@Override
	public String getImplementationType() {
		return IMPLEMENTATION_TYPE;
	}

	/**
	 * Return available local connectors for this IC implementation
	 * 
	 * @return
	 */
	@Override
	public List<IcConnectorInfo> getAvailableLocalConnectors() {
		log.info("Get Available local connectors - ConnId");
		List<IcConnectorInfo> localConnectorInfos = new ArrayList<>();
		List<ConnectorInfoManager> managers = findAllLocalConnectorManagers();

		for (ConnectorInfoManager manager : managers) {
			List<ConnectorInfo> infos = manager.getConnectorInfos();
			if (infos == null) {
				continue;
			}
			for (ConnectorInfo info : infos) {
				ConnectorKey key = info.getConnectorKey();
				if (key == null) {
					continue;
				}
				IcConnectorKeyImpl keyDto = new IcConnectorKeyImpl(getImplementationType(), key.getBundleName(),
						key.getBundleVersion(), key.getConnectorName());
				IcConnectorInfoImpl infoDto = new IcConnectorInfoImpl(info.getConnectorDisplayName(),
						info.getConnectorCategory(), keyDto);
				localConnectorInfos.add(infoDto);
			}
		}
		return localConnectorInfos;
	}

	/**
	 * Return find connector default configuration by connector info
	 * 
	 * @param key
	 * @return
	 */
	@Override
	public IcConnectorConfiguration getConnectorConfiguration(IcConnectorKey key) {
		Assert.notNull(key);

		ConnectorInfo i = getConnIdConnectorInfo(key);
		if (i != null) {
			APIConfiguration apiConf = i.createDefaultAPIConfiguration();
			return ConnIdIcConvertUtil.convertConnIdConnectorConfiguration(apiConf);
		}
		return null;
	}

	public ConnectorInfo getConnIdConnectorInfo(IcConnectorKey key) {
		Assert.notNull(key);

		for (ConnectorInfoManager manager : findAllLocalConnectorManagers()) {
			ConnectorInfo i = manager.findConnectorInfo(
					ConnIdIcConvertUtil.convertConnectorKeyFromDto(key, this.getImplementationType()));
			if (i != null) {
				return i;
			}
		}
		return null;
	}

	@Override
	public IcSchema getSchema(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration) {
		Assert.notNull(key);
		Assert.notNull(connectorConfiguration);
		log.info(MessageFormat.format("Get Schema - ConnId ({0})", key.toString()));
		ConnectorFacade conn = getConnectorFacade(key, connectorConfiguration);
		Schema schema = conn.schema();
		return ConnIdIcConvertUtil.convertConnIdSchema(schema);
	}

	private List<ConnectorInfoManager> findAllLocalConnectorManagers() {
		if (managers == null) {
			managers = new ArrayList<>();
			List<Class<?>> annotated = new ArrayList<>();
			// Find all class with annotation ConnectorClass under specific
			// packages
			localConnectorsPackages.forEach(packageWithConnectors -> {
				Reflections reflections = new Reflections(packageWithConnectors);
				annotated.addAll(reflections.getTypesAnnotatedWith(ConnectorClass.class));
			});

			log.info(MessageFormat.format("Found annotated classes with ConnectorClass [{0}]", annotated));

			for (Class<?> clazz : annotated) {
				URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
				ConnectorInfoManagerFactory fact = ConnectorInfoManagerFactory.getInstance();
				ConnectorInfoManager manager = fact.getLocalManager(url);
				managers.add(manager);
			}
			log.info(MessageFormat.format("Found all local connector managers [{0}]", managers.toString()));
		}
		return managers;
	}

	private ConnectorFacade getConnectorFacade(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration) {
		Assert.notNull(key);
		Assert.notNull(connectorConfiguration);
		ConnectorInfo connIdInfo = this.getConnIdConnectorInfo(key);
		Assert.notNull(connIdInfo, "ConnId connector info not found!");
		APIConfiguration config = connIdInfo.createDefaultAPIConfiguration();
		Assert.notNull(config.getConfigurationProperties(), "ConnId connector configuration properties not found!");
		config = ConnIdIcConvertUtil.convertIcConnectorConfiguration(connectorConfiguration, config);
		// Use the ConnectorFacadeFactory's newInstance() method to get a new
		// connector.
		ConnectorFacade conn = ConnectorFacadeFactory.getManagedInstance().newInstance(config);

		// Make sure we have set up the Configuration properly
		conn.validate();
		return conn;
	}

}
