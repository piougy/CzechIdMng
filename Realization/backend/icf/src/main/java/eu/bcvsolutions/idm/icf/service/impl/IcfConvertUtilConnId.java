package eu.bcvsolutions.idm.icf.service.impl;

import java.util.List;

import org.identityconnectors.common.pooling.ObjectPoolConfiguration;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConfigurationProperty;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.impl.api.APIConfigurationImpl;
import org.identityconnectors.framework.impl.api.ConfigurationPropertyImpl;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.icf.api.IcfAttribute;
import eu.bcvsolutions.idm.icf.api.IcfConfigurationProperties;
import eu.bcvsolutions.idm.icf.api.IcfConfigurationProperty;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.api.IcfEnabledAttribute;
import eu.bcvsolutions.idm.icf.api.IcfObjectPoolConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfPasswordAttribute;
import eu.bcvsolutions.idm.icf.api.IcfUidAttribute;
import eu.bcvsolutions.idm.icf.dto.IcfConfigurationPropertiesDto;
import eu.bcvsolutions.idm.icf.dto.IcfConfigurationPropertyDto;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorConfigurationDto;
import eu.bcvsolutions.idm.icf.dto.IcfObjectPoolConfigurationDto;
import eu.bcvsolutions.idm.icf.dto.IcfUidAttributeDto;

public class IcfConvertUtilConnId {

	public static ConnectorKey convertConnectorKeyFromDto(IcfConnectorKey dto, String icfImplementationType) {
		Assert.notNull(dto);
		Assert.notNull(icfImplementationType);
		Assert.isTrue(icfImplementationType.equals(dto.getIcfType()));

		return new ConnectorKey(dto.getBundleName(), dto.getBundleVersion(), dto.getConnectorName());
	}

	public static IcfConnectorConfiguration convertConnIdConnectorConfiguration(APIConfiguration conf) {
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
				IcfConfigurationPropertyDto propertyDto = (IcfConfigurationPropertyDto) convertConnIdConfigurationProperty(
						property);
				if (propertiesDto != null) {
					propertiesDto.getProperties().add(propertyDto);
				}
			}
		}
		dto.setConfigurationProperties(propertiesDto);
		IcfObjectPoolConfigurationDto connectorPoolConfiguration = (IcfObjectPoolConfigurationDto) convertConnIdPoolConfiguration(
				conf.getConnectorPoolConfiguration());
		dto.setConnectorPoolConfiguration(connectorPoolConfiguration);
		return dto;
	}

	public static IcfConfigurationProperty convertConnIdConfigurationProperty(ConfigurationProperty property) {
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

	public static ConfigurationProperty convertIcfConfigurationProperty(IcfConfigurationProperty property)
			throws ClassNotFoundException {
		if (property == null) {
			return null;
		}
		ConfigurationPropertyImpl dto = new ConfigurationPropertyImpl();
		dto.setConfidential(property.isConfidential());
		// dto.setDisplayName(property.getDisplayName());
		// dto.setGroup(property.getGroup(null));
		// dto.setHelpMessage(property.getHelpMessage(null));
		dto.setName(property.getName());
		dto.setRequired(property.isRequired());
		dto.setType(property.getType() != null ? Class.forName(property.getType()) : null);
		dto.setValue(property.getValue());
		return dto;
	}

	public static IcfObjectPoolConfiguration convertConnIdPoolConfiguration(ObjectPoolConfiguration pool) {
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

	public static ObjectPoolConfiguration convertIcfPoolConfiguration(IcfObjectPoolConfiguration pool) {
		if (pool == null) {
			return null;
		}
		ObjectPoolConfiguration dto = new ObjectPoolConfiguration();
		dto.setMaxIdle(pool.getMaxIdle());
		dto.setMaxObjects(pool.getMaxObjects());
		dto.setMaxWait(pool.getMaxWait());
		dto.setMinEvictableIdleTimeMillis(pool.getMinEvictableIdleTimeMillis());
		dto.setMinIdle(pool.getMinIdle());
		return dto;
	}

	public static APIConfiguration convertIcfConnectorConfiguration(IcfConnectorConfiguration icfConf,
			APIConfiguration defaultConnIdConf) {
		if (icfConf == null) {
			return null;
		}
		((APIConfigurationImpl) defaultConnIdConf).setConnectorPoolingSupported(icfConf.isConnectorPoolingSupported());
		defaultConnIdConf.setProducerBufferSize(icfConf.getProducerBufferSize());

		IcfConfigurationProperties properties = icfConf.getConfigurationProperties();
		ConfigurationProperties connIdProperties = defaultConnIdConf.getConfigurationProperties();
		if (properties != null && properties.getProperties() != null) {
			for (IcfConfigurationProperty icfProperty : properties.getProperties()) {
				if (connIdProperties != null) {
					connIdProperties.setPropertyValue(icfProperty.getName(), icfProperty.getValue());
				}
			}
		}
		ObjectPoolConfiguration connectorPoolConfiguration = convertIcfPoolConfiguration(
				icfConf.getConnectorPoolConfiguration());
		((APIConfigurationImpl) defaultConnIdConf).setConnectorPoolConfiguration(connectorPoolConfiguration);
		return defaultConnIdConf;
	}

	public static Attribute convertIcfAttribute(IcfAttribute icfAttribute) {
		if (icfAttribute == null) {
			return null;
		}
		if (icfAttribute instanceof IcfEnabledAttribute && ((IcfEnabledAttribute) icfAttribute).getEnabled() != null) {
			return AttributeBuilder.buildEnabled(((IcfEnabledAttribute) icfAttribute).getEnabled());
		}
		if (icfAttribute instanceof IcfEnabledAttribute
				&& ((IcfEnabledAttribute) icfAttribute).getEnabledDate() != null) {
			return AttributeBuilder.buildEnableDate(((IcfEnabledAttribute) icfAttribute).getEnabledDate());
		}
		if (icfAttribute instanceof IcfEnabledAttribute
				&& ((IcfEnabledAttribute) icfAttribute).getDisabledDate() != null) {
			return AttributeBuilder.buildDisableDate(((IcfEnabledAttribute) icfAttribute).getDisabledDate());
		}
		if (icfAttribute instanceof IcfPasswordAttribute) {
			return AttributeBuilder.buildPassword(((IcfPasswordAttribute) icfAttribute).getPasswordValue() != null
					? ((IcfPasswordAttribute) icfAttribute).getPasswordValue().asString().toCharArray() : null);
		}
		if (!icfAttribute.isMultiValue() && icfAttribute.isLogin()) {
			return new Name((String) icfAttribute.getValue());
		}
		if (!icfAttribute.isMultiValue() && icfAttribute.getValue() == null) {
			return AttributeBuilder.build(icfAttribute.getName());
		}
		if (!icfAttribute.isMultiValue() && icfAttribute.getValue() != null) {
			return AttributeBuilder.build(icfAttribute.getName(), icfAttribute.getValue());
		}
		if (icfAttribute.isMultiValue() && icfAttribute.getValue() != null) {
			return AttributeBuilder.build(icfAttribute.getName(), icfAttribute.getValues());
		}

		return null;
	}

	public static IcfUidAttribute convertConnIdUid(Uid uid) {
		if(uid == null){
			return null;
		}
		IcfUidAttributeDto icfUid = new IcfUidAttributeDto(uid.getName(), uid.getUidValue(), uid.getRevision());
		return icfUid;
	}
}
