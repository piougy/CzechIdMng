package eu.bcvsolutions.idm.icf.service.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.identityconnectors.common.pooling.ObjectPoolConfiguration;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConfigurationProperty;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.icf.api.IcfConfigurationProperty;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.api.IcfObjectPoolConfiguration;
import eu.bcvsolutions.idm.icf.dto.IcfConfigurationPropertiesDto;
import eu.bcvsolutions.idm.icf.dto.IcfConfigurationPropertyDto;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorConfigurationDto;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorInfoDto;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorKeyDto;
import eu.bcvsolutions.idm.icf.dto.IcfObjectPoolConfigurationDto;
import eu.bcvsolutions.idm.icf.exception.IcfException;
import eu.bcvsolutions.idm.icf.service.api.IcfConfigurationService;
import eu.bcvsolutions.idm.security.exception.IdmAuthenticationException;

@Service
public class IcfConfigurationServiceConnId implements IcfConfigurationService {

	@Autowired
	public IcfConfigurationServiceConnId(IcfConfigurationAggregatorService icfConfigurationAggregator) {
		if (icfConfigurationAggregator.getIcfs() == null) {
			throw new IcfException("Map of ICF implementations is not defined!");
		}
		if (icfConfigurationAggregator.getIcfs().containsKey(this.getIcfType())) {
			throw new IcfException("ICF implementation duplicity for key: " + this.getIcfType());
		}
		icfConfigurationAggregator.getIcfs().put(this.getIcfType(), this);
	}

	/**
	 * Return key defined ICF implementation
	 * 
	 * @return
	 */
	@Override
	public String getIcfType() {
		return "connId";
	}

	/**
	 * Return available local connectors for this ICF implementation
	 * 
	 * @return
	 */
	@Override
	public List<IcfConnectorInfo> getAvailableLocalConnectors() {
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
				IcfConnectorKeyDto keyDto = new IcfConnectorKeyDto(getIcfType(), key.getBundleName(),
						key.getBundleVersion(), key.getConnectorName());
				IcfConnectorInfoDto infoDto = new IcfConnectorInfoDto(info.getConnectorDisplayName(),
						info.getConnectorCategory(), keyDto);
				localConnectorInfos.add(infoDto);
			}
		}
		return localConnectorInfos;
	}

	/**
	 * Return find connector default configuration by connector info
	 * 
	 * @param info
	 * @return
	 */
	@Override
	public IcfConnectorConfiguration getConnectorConfiguration(IcfConnectorInfo info) {
		Assert.notNull(info);

		for (ConnectorInfoManager manager : findAllLocalConnectorManagers()) {
			ConnectorInfo i = manager.findConnectorInfo(this.convertConnectorKeyFromDto(info.getConnectorKey()));
			if (i != null) {
				APIConfiguration apiConf = i.createDefaultAPIConfiguration();
				IcfConnectorConfiguration configDto = convertConnectorConfigurationToDto(apiConf);
				return configDto;
			}
		}
		return null;

	}

	private List<ConnectorInfoManager> findAllLocalConnectorManagers() {
		Reflections reflections = new Reflections();
		Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(ConnectorClass.class);

		List<ConnectorInfoManager> managers = new ArrayList<>();
		for (Class<?> clazz : annotated) {
			URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
			ConnectorInfoManagerFactory fact = ConnectorInfoManagerFactory.getInstance();
			ConnectorInfoManager manager = fact.getLocalManager(url);
			managers.add(manager);
		}
		return managers;
	}

	private ConnectorKey convertConnectorKeyFromDto(IcfConnectorKey dto) {
		Assert.notNull(dto);
		Assert.isTrue(this.getIcfType().equals(dto.getIcfType()));

		return new ConnectorKey(dto.getBundleName(), dto.getBundleVersion(), dto.getConnectorName());
	}

	private IcfConnectorConfiguration convertConnectorConfigurationToDto(APIConfiguration conf) {
		if (conf == null) {
			return null;
		}
		IcfConnectorConfigurationDto dto = new IcfConnectorConfigurationDto();
		dto.setConnectorPoolingSupported(conf.isConnectorPoolingSupported());
		dto.setProducerBufferSize(conf.getProducerBufferSize());

		ConfigurationProperties properties = conf.getConfigurationProperties();
		IcfConfigurationPropertiesDto propertiesDto = new IcfConfigurationPropertiesDto();
		if (properties != null && properties.getPropertyNames() != null) {
			List<String> propertyNames = properties.getPropertyNames();
			for (String name : propertyNames) {
				ConfigurationProperty property = properties.getProperty(name);
				IcfConfigurationPropertyDto propertyDto = (IcfConfigurationPropertyDto) convertConfigurationPropertyToDto(
						property);
				if (propertiesDto != null) {
					propertiesDto.getProperties().add(propertyDto);
				}
			}
		}
		dto.setConfigurationProperties(propertiesDto);
		IcfObjectPoolConfigurationDto connectorPoolConfiguration = (IcfObjectPoolConfigurationDto) convertPoolConfigurationToDto(
				conf.getConnectorPoolConfiguration());
		dto.setConnectorPoolConfiguration(connectorPoolConfiguration);
		return dto;
	}

	private IcfConfigurationProperty convertConfigurationPropertyToDto(ConfigurationProperty property) {
		if (property == null) {
			return null;
		}
		IcfConfigurationPropertyDto dto = new IcfConfigurationPropertyDto();
		dto.setConfidential(property.isConfidential());
		dto.setDisplayName(property.getDisplayName(property.getName()));
		dto.setGroup(property.getGroup(null));
		dto.setHelpMessage(property.getHelpMessage(null));
		dto.setName(property.getName());
		dto.setRequired(property.isRequired());
		dto.setType(property.getType() != null ? property.getType().getName() : null);
		dto.setValue(property.getValue());
		return dto;
	}

	private IcfObjectPoolConfiguration convertPoolConfigurationToDto(ObjectPoolConfiguration pool) {
		if (pool == null) {
			return null;
		}
		IcfObjectPoolConfigurationDto dto = new IcfObjectPoolConfigurationDto();
		dto.setMaxIdle(pool.getMaxIdle());
		dto.setMaxObjects(pool.getMaxObjects());
		dto.setMaxWait(pool.getMaxWait());
		dto.setMinEvictableIdleTimeMillis(pool.getMinEvictableIdleTimeMillis());
		dto.setMinIdle(pool.getMinIdle());
		return dto;
	}

}
