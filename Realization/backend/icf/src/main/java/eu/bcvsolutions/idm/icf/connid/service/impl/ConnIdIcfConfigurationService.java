package eu.bcvsolutions.idm.icf.connid.service.impl;

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

import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.api.IcfSchema;
import eu.bcvsolutions.idm.icf.connid.domain.ConnIdIcfConvertUtil;
import eu.bcvsolutions.idm.icf.exception.IcfException;
import eu.bcvsolutions.idm.icf.impl.IcfConnectorInfoImpl;
import eu.bcvsolutions.idm.icf.impl.IcfConnectorKeyImpl;
import eu.bcvsolutions.idm.icf.service.api.IcfConfigurationFacade;
import eu.bcvsolutions.idm.icf.service.api.IcfConfigurationService;

@Service
public class ConnIdIcfConfigurationService implements IcfConfigurationService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConnIdIcfConfigurationService.class);

	// Cached local connid managers
	private List<ConnectorInfoManager> managers;
	@Value("#{'${icf.localconnector.packages}'.split(',')}")
	private List<String> localConnectorsPackages;

	@Autowired
	public ConnIdIcfConfigurationService(IcfConfigurationFacade icfConfigurationAggregator) {
		if (icfConfigurationAggregator.getIcfConfigs() == null) {
			throw new IcfException("Map of ICF implementations is not defined!");
		}
		if (icfConfigurationAggregator.getIcfConfigs().containsKey(IMPLEMENTATION_TYPE)) {
			throw new IcfException(
					MessageFormat.format("ICF implementation duplicity for key: {0}", this.getImplementationType()));
		}
		icfConfigurationAggregator.getIcfConfigs().put(IMPLEMENTATION_TYPE, this);
	}

	final private static String IMPLEMENTATION_TYPE = "connId";

	/**
	 * Return key defined ICF implementation
	 * 
	 * @return
	 */
	@Override
	public String getImplementationType() {
		return IMPLEMENTATION_TYPE;
	}

	/**
	 * Return available local connectors for this ICF implementation
	 * 
	 * @return
	 */
	@Override
	public List<IcfConnectorInfo> getAvailableLocalConnectors() {
		log.info("Get Available local connectors - ConnId");
		List<IcfConnectorInfo> localConnectorInfos = new ArrayList<>();
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
				IcfConnectorKeyImpl keyDto = new IcfConnectorKeyImpl(getImplementationType(), key.getBundleName(),
						key.getBundleVersion(), key.getConnectorName());
				IcfConnectorInfoImpl infoDto = new IcfConnectorInfoImpl(info.getConnectorDisplayName(),
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
	public IcfConnectorConfiguration getConnectorConfiguration(IcfConnectorKey key) {
		Assert.notNull(key);

		ConnectorInfo i = getConnIdConnectorInfo(key);
		if (i != null) {
			APIConfiguration apiConf = i.createDefaultAPIConfiguration();
			return ConnIdIcfConvertUtil.convertConnIdConnectorConfiguration(apiConf);
		}
		return null;
	}

	public ConnectorInfo getConnIdConnectorInfo(IcfConnectorKey key) {
		Assert.notNull(key);

		for (ConnectorInfoManager manager : findAllLocalConnectorManagers()) {
			ConnectorInfo i = manager.findConnectorInfo(
					ConnIdIcfConvertUtil.convertConnectorKeyFromDto(key, this.getImplementationType()));
			if (i != null) {
				return i;
			}
		}
		return null;
	}

	@Override
	public IcfSchema getSchema(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration) {
		Assert.notNull(key);
		Assert.notNull(connectorConfiguration);
		log.info(MessageFormat.format("Get Schema - ConnId ({0})", key.toString()));
		ConnectorFacade conn = getConnectorFacade(key, connectorConfiguration);
		Schema schema = conn.schema();
		return ConnIdIcfConvertUtil.convertConnIdSchema(schema);
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

	private ConnectorFacade getConnectorFacade(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration) {
		Assert.notNull(key);
		Assert.notNull(connectorConfiguration);
		ConnectorInfo connIdInfo = this.getConnIdConnectorInfo(key);
		Assert.notNull(connIdInfo, "ConnId connector info not found!");
		APIConfiguration config = connIdInfo.createDefaultAPIConfiguration();
		Assert.notNull(config.getConfigurationProperties(), "ConnId connector configuration properties not found!");
		config = ConnIdIcfConvertUtil.convertIcfConnectorConfiguration(connectorConfiguration, config);
		// Use the ConnectorFacadeFactory's newInstance() method to get a new
		// connector.
		ConnectorFacade conn = ConnectorFacadeFactory.getManagedInstance().newInstance(config);

		// Make sure we have set up the Configuration properly
		conn.validate();
		return conn;
	}

}
