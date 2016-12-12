package eu.bcvsolutions.idm.icf.connid.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.identityconnectors.common.pooling.ObjectPoolConfiguration;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConfigurationProperty;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.impl.api.APIConfigurationImpl;
import org.identityconnectors.framework.impl.api.ConfigurationPropertyImpl;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.icf.api.IcfAttribute;
import eu.bcvsolutions.idm.icf.api.IcfAttributeInfo;
import eu.bcvsolutions.idm.icf.api.IcfConfigurationProperties;
import eu.bcvsolutions.idm.icf.api.IcfConfigurationProperty;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.api.IcfConnectorObject;
import eu.bcvsolutions.idm.icf.api.IcfEnabledAttribute;
import eu.bcvsolutions.idm.icf.api.IcfLoginAttribute;
import eu.bcvsolutions.idm.icf.api.IcfObjectClass;
import eu.bcvsolutions.idm.icf.api.IcfObjectClassInfo;
import eu.bcvsolutions.idm.icf.api.IcfObjectPoolConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfPasswordAttribute;
import eu.bcvsolutions.idm.icf.api.IcfSchema;
import eu.bcvsolutions.idm.icf.api.IcfUidAttribute;
import eu.bcvsolutions.idm.icf.impl.IcfAttributeImpl;
import eu.bcvsolutions.idm.icf.impl.IcfAttributeInfoImpl;
import eu.bcvsolutions.idm.icf.impl.IcfConfigurationPropertiesImpl;
import eu.bcvsolutions.idm.icf.impl.IcfConfigurationPropertyImpl;
import eu.bcvsolutions.idm.icf.impl.IcfConnectorConfigurationImpl;
import eu.bcvsolutions.idm.icf.impl.IcfConnectorObjectImpl;
import eu.bcvsolutions.idm.icf.impl.IcfEnabledAttributeImpl;
import eu.bcvsolutions.idm.icf.impl.IcfLoginAttributeImpl;
import eu.bcvsolutions.idm.icf.impl.IcfObjectClassImpl;
import eu.bcvsolutions.idm.icf.impl.IcfObjectClassInfoImpl;
import eu.bcvsolutions.idm.icf.impl.IcfObjectPoolConfigurationImpl;
import eu.bcvsolutions.idm.icf.impl.IcfPasswordAttributeImpl;
import eu.bcvsolutions.idm.icf.impl.IcfSchemaImpl;
import eu.bcvsolutions.idm.icf.impl.IcfUidAttributeImpl;

/**
 * Convert utility for ConnId implementation
 * 
 * @author svandav
 *
 */
public class ConnIdIcfConvertUtil {

	public static ConnectorKey convertConnectorKeyFromDto(IcfConnectorKey dto, String icfImplementationType) {
		Assert.notNull(dto);
		Assert.notNull(icfImplementationType);
		Assert.isTrue(icfImplementationType.equals(dto.getFramework()));

		return new ConnectorKey(dto.getBundleName(), dto.getBundleVersion(), dto.getConnectorName());
	}

	public static IcfConnectorConfiguration convertConnIdConnectorConfiguration(APIConfiguration conf) {
		if (conf == null) {
			return null;
		}
		IcfConnectorConfigurationImpl dto = new IcfConnectorConfigurationImpl();
		dto.setConnectorPoolingSupported(conf.isConnectorPoolingSupported());
		dto.setProducerBufferSize(conf.getProducerBufferSize());

		ConfigurationProperties properties = conf.getConfigurationProperties();
		IcfConfigurationPropertiesImpl propertiesDto = new IcfConfigurationPropertiesImpl();
		if (properties != null && properties.getPropertyNames() != null) {
			List<String> propertyNames = properties.getPropertyNames();
			for (String name : propertyNames) {
				ConfigurationProperty property = properties.getProperty(name);
				IcfConfigurationPropertyImpl propertyDto = (IcfConfigurationPropertyImpl) convertConnIdConfigurationProperty(
						property);
				if (propertiesDto != null) {
					propertiesDto.getProperties().add(propertyDto);
				}
			}
		}
		dto.setConfigurationProperties(propertiesDto);
		IcfObjectPoolConfigurationImpl connectorPoolConfiguration = (IcfObjectPoolConfigurationImpl) convertConnIdPoolConfiguration(
				conf.getConnectorPoolConfiguration());
		dto.setConnectorPoolConfiguration(connectorPoolConfiguration);
		return dto;
	}

	public static IcfConfigurationProperty convertConnIdConfigurationProperty(ConfigurationProperty property) {
		if (property == null) {
			return null;
		}
		IcfConfigurationPropertyImpl dto = new IcfConfigurationPropertyImpl();
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
		IcfObjectPoolConfigurationImpl dto = new IcfObjectPoolConfigurationImpl();
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
					? ((IcfPasswordAttribute) icfAttribute).getPasswordValue().asString().toCharArray()
					: "".toCharArray());
		}
		if (icfAttribute instanceof IcfLoginAttribute) {
			return new Name((String) icfAttribute.getValue());
		}
		if (!icfAttribute.isMultiValue() && icfAttribute.getValue() == null) {
			return AttributeBuilder.build(icfAttribute.getName());
		}
		if (!icfAttribute.isMultiValue() && icfAttribute.getValue() != null) {
			return AttributeBuilder.build(icfAttribute.getName(), icfAttribute.getValue());
		}
		if (icfAttribute.isMultiValue() && icfAttribute.getValues() != null) {
			return AttributeBuilder.build(icfAttribute.getName(), icfAttribute.getValues());
		}

		return null;
	}

	public static IcfAttribute convertConnIdAttribute(Attribute attribute) {
		if (attribute == null) {
			return null;
		}
		List<Object> value = attribute.getValue();
		if (attribute.is(Name.NAME)) {
			if (value == null || value.size() != 1 || !(value.get(0) instanceof String)) {
				throw new IllegalArgumentException("Login attribute must be fill and a single String value.");
			}
			return new IcfLoginAttributeImpl(Name.NAME, (String) attribute.getValue().get(0));
		}
		if (attribute.is(OperationalAttributes.PASSWORD_NAME)) {
			eu.bcvsolutions.idm.security.api.domain.GuardedString password = null;
			if (value != null && value.size() == 1 && value.get(0) instanceof GuardedString) {
				password = new eu.bcvsolutions.idm.security.api.domain.GuardedString(
						((GuardedString) value.get(0)).toString());
			}
			return new IcfPasswordAttributeImpl(password);
		}
		if (attribute.is(OperationalAttributes.ENABLE_NAME)) {
			Boolean enabled = Boolean.FALSE;
			if (value != null && value.size() == 1 && value.get(0) instanceof Boolean) {
				enabled = (Boolean) value.get(0);
			}
			return new IcfEnabledAttributeImpl(enabled, OperationalAttributes.ENABLE_NAME);
		}
		if (value == null || value.isEmpty()) {
			return new IcfAttributeImpl(attribute.getName(), null);
		}
		if (value.size() == 1) {
			return new IcfAttributeImpl(attribute.getName(), value.get(0));
		} else {
			return new IcfAttributeImpl(attribute.getName(), value);
		}
	}

	public static IcfUidAttribute convertConnIdUid(Uid uid) {
		if (uid == null) {
			return null;
		}
		return new IcfUidAttributeImpl(uid.getName(), uid.getUidValue(), uid.getRevision());
	}

	public static ObjectClass convertIcfObjectClass(IcfObjectClass objectClass) {
		if (objectClass == null) {
			return null;
		}
		return new ObjectClass(objectClass.getType());
	}

	public static IcfObjectClass convertConnIdObjectClass(ObjectClass objectClass) {
		if (objectClass == null) {
			return null;
		}
		IcfObjectClassImpl objectClassDto = new IcfObjectClassImpl(objectClass.getObjectClassValue());
		objectClassDto.setDisplayName(objectClass.getDisplayNameKey());
		return objectClassDto;
	}

	public static Uid convertIcfUid(IcfUidAttribute uid) {
		if (uid == null) {
			return null;
		}
		Uid connidUid = null;
		if (uid.getRevision() != null) {
			connidUid = new Uid(uid.getUidValue(), uid.getRevision());
		} else {
			connidUid = new Uid(uid.getUidValue());
		}
		return connidUid;
	}

	public static IcfConnectorObject convertConnIdConnectorObject(ConnectorObject connObject) {
		if (connObject == null) {
			return null;
		}
		IcfObjectClass icfClass = ConnIdIcfConvertUtil.convertConnIdObjectClass(connObject.getObjectClass());
		Set<Attribute> attributes = connObject.getAttributes();
		List<IcfAttribute> icfAttributes = new ArrayList<>();
		if (attributes != null) {
			for (Attribute a : attributes) {
				icfAttributes.add(ConnIdIcfConvertUtil.convertConnIdAttribute(a));
			}
		}
		return new IcfConnectorObjectImpl(icfClass, icfAttributes);
	}

	public static IcfSchema convertConnIdSchema(Schema schema) {
		if (schema == null) {
			return null;
		}
		IcfSchemaImpl icfSchema = new IcfSchemaImpl();
		List<IcfObjectClassInfo> objectClasses = icfSchema.getDeclaredObjectClasses();

		for (ObjectClassInfo classInfo : schema.getObjectClassInfo()) {
			objectClasses.add(ConnIdIcfConvertUtil.convertConnIdObjectClassInfo(classInfo));
		}
		if (schema.getSupportedObjectClassesByOperation() != null) {
			for (Class<? extends APIOperation> operation : schema.getSupportedObjectClassesByOperation().keySet()) {
				List<String> objectClassesForOperation = new ArrayList<>();
				Set<ObjectClassInfo> objectClasesConnid = schema.getSupportedObjectClassesByOperation().get(operation);
				for (ObjectClassInfo oci : objectClasesConnid) {
					objectClassesForOperation.add(oci.getType());
				}
				icfSchema.getSupportedObjectClassesByOperation().put(operation.getSimpleName(),
						objectClassesForOperation);
			}
		}
		return icfSchema;
	}

	public static IcfObjectClassInfo convertConnIdObjectClassInfo(ObjectClassInfo objectClass) {
		if (objectClass == null) {
			return null;
		}

		IcfObjectClassInfoImpl icfObjectClass = new IcfObjectClassInfoImpl();
		Set<AttributeInfo> attributeInfos = objectClass.getAttributeInfo();
		if (attributeInfos != null) {
			for (AttributeInfo attributeInfo : attributeInfos) {
				icfObjectClass.getAttributeInfos().add(ConnIdIcfConvertUtil.convertConnIdAttributeInfo(attributeInfo));
			}
		}

		icfObjectClass.setType(objectClass.getType());
		icfObjectClass.setAuxiliary(objectClass.isAuxiliary());
		icfObjectClass.setContainer(objectClass.isContainer());
		return icfObjectClass;
	}

	public static IcfAttributeInfo convertConnIdAttributeInfo(AttributeInfo attribute) {
		if (attribute == null) {
			return null;
		}
		IcfAttributeInfoImpl icfAttribute = new IcfAttributeInfoImpl();
		if (attribute.getType() != null) {
			if (GuardedString.class.isAssignableFrom(attribute.getType())) {
				// We do converse between BCV GuardedString and ConnId
				// GuardedString
				icfAttribute.setClassType(eu.bcvsolutions.idm.security.api.domain.GuardedString.class.getName());
			} else {
				icfAttribute.setClassType(attribute.getType().getName());
			}
		}
		icfAttribute.setCreateable(attribute.isCreateable());
		icfAttribute.setMultivalued(attribute.isMultiValued());
		icfAttribute.setName(attribute.getName());
		icfAttribute.setNativeName(attribute.getNativeName());
		icfAttribute.setReadable(attribute.isReadable());
		icfAttribute.setRequired(attribute.isRequired());
		icfAttribute.setReturnedByDefault(attribute.isReturnedByDefault());
		icfAttribute.setUpdateable(attribute.isUpdateable());
		return icfAttribute;
	}

}
