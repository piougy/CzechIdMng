package eu.bcvsolutions.idm.ic.connid.domain;

import java.util.ArrayList;
import java.util.LinkedList;
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
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AndFilter;
import org.identityconnectors.framework.common.objects.filter.ContainsAllValuesFilter;
import org.identityconnectors.framework.common.objects.filter.ContainsFilter;
import org.identityconnectors.framework.common.objects.filter.EndsWithFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.GreaterThanFilter;
import org.identityconnectors.framework.common.objects.filter.LessThanFilter;
import org.identityconnectors.framework.common.objects.filter.NotFilter;
import org.identityconnectors.framework.common.objects.filter.OrFilter;
import org.identityconnectors.framework.common.objects.filter.StartsWithFilter;
import org.identityconnectors.framework.impl.api.APIConfigurationImpl;
import org.identityconnectors.framework.impl.api.ConfigurationPropertyImpl;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcAttributeInfo;
import eu.bcvsolutions.idm.ic.api.IcConfigurationProperties;
import eu.bcvsolutions.idm.ic.api.IcConfigurationProperty;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcEnabledAttribute;
import eu.bcvsolutions.idm.ic.api.IcLoginAttribute;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import eu.bcvsolutions.idm.ic.api.IcObjectPoolConfiguration;
import eu.bcvsolutions.idm.ic.api.IcPasswordAttribute;
import eu.bcvsolutions.idm.ic.api.IcSchema;
import eu.bcvsolutions.idm.ic.api.IcSyncDelta;
import eu.bcvsolutions.idm.ic.api.IcSyncToken;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.filter.api.IcFilter;
import eu.bcvsolutions.idm.ic.filter.impl.IcAndFilter;
import eu.bcvsolutions.idm.ic.filter.impl.IcAttributeFilter;
import eu.bcvsolutions.idm.ic.filter.impl.IcContainsAllValuesFilter;
import eu.bcvsolutions.idm.ic.filter.impl.IcContainsFilter;
import eu.bcvsolutions.idm.ic.filter.impl.IcEndsWithFilter;
import eu.bcvsolutions.idm.ic.filter.impl.IcEqualsFilter;
import eu.bcvsolutions.idm.ic.filter.impl.IcGreaterThanFilter;
import eu.bcvsolutions.idm.ic.filter.impl.IcLessThanFilter;
import eu.bcvsolutions.idm.ic.filter.impl.IcNotFilter;
import eu.bcvsolutions.idm.ic.filter.impl.IcOrFilter;
import eu.bcvsolutions.idm.ic.filter.impl.IcStartsWithFilter;
import eu.bcvsolutions.idm.ic.impl.IcAttributeImpl;
import eu.bcvsolutions.idm.ic.impl.IcAttributeInfoImpl;
import eu.bcvsolutions.idm.ic.impl.IcConfigurationPropertiesImpl;
import eu.bcvsolutions.idm.ic.impl.IcConfigurationPropertyImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorConfigurationImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorObjectImpl;
import eu.bcvsolutions.idm.ic.impl.IcEnabledAttributeImpl;
import eu.bcvsolutions.idm.ic.impl.IcLoginAttributeImpl;
import eu.bcvsolutions.idm.ic.impl.IcObjectClassImpl;
import eu.bcvsolutions.idm.ic.impl.IcObjectClassInfoImpl;
import eu.bcvsolutions.idm.ic.impl.IcObjectPoolConfigurationImpl;
import eu.bcvsolutions.idm.ic.impl.IcPasswordAttributeImpl;
import eu.bcvsolutions.idm.ic.impl.IcSchemaImpl;
import eu.bcvsolutions.idm.ic.impl.IcSyncDeltaImpl;
import eu.bcvsolutions.idm.ic.impl.IcSyncDeltaTypeEnum;
import eu.bcvsolutions.idm.ic.impl.IcSyncTokenImpl;
import eu.bcvsolutions.idm.ic.impl.IcUidAttributeImpl;

/**
 * Convert utility for ConnId implementation
 * 
 * @author svandav
 *
 */
public class ConnIdIcConvertUtil {

	public static ConnectorKey convertConnectorKeyFromDto(IcConnectorKey dto, String icImplementationType) {
		Assert.notNull(dto);
		Assert.notNull(icImplementationType);
		Assert.isTrue(icImplementationType.equals(dto.getFramework()));

		return new ConnectorKey(dto.getBundleName(), dto.getBundleVersion(), dto.getConnectorName());
	}

	public static IcConnectorConfiguration convertConnIdConnectorConfiguration(APIConfiguration conf) {
		if (conf == null) {
			return null;
		}
		IcConnectorConfigurationImpl dto = new IcConnectorConfigurationImpl();
		dto.setConnectorPoolingSupported(conf.isConnectorPoolingSupported());
		dto.setProducerBufferSize(conf.getProducerBufferSize());

		ConfigurationProperties properties = conf.getConfigurationProperties();
		IcConfigurationPropertiesImpl propertiesDto = new IcConfigurationPropertiesImpl();
		if (properties != null && properties.getPropertyNames() != null) {
			List<String> propertyNames = properties.getPropertyNames();
			for (String name : propertyNames) {
				ConfigurationProperty property = properties.getProperty(name);
				IcConfigurationPropertyImpl propertyDto = (IcConfigurationPropertyImpl) convertConnIdConfigurationProperty(
						property);
				if (propertiesDto != null) {
					propertiesDto.getProperties().add(propertyDto);
				}
			}
		}
		dto.setConfigurationProperties(propertiesDto);
		IcObjectPoolConfigurationImpl connectorPoolConfiguration = (IcObjectPoolConfigurationImpl) convertConnIdPoolConfiguration(
				conf.getConnectorPoolConfiguration());
		dto.setConnectorPoolConfiguration(connectorPoolConfiguration);
		return dto;
	}

	public static IcConfigurationProperty convertConnIdConfigurationProperty(ConfigurationProperty property) {
		if (property == null) {
			return null;
		}
		IcConfigurationPropertyImpl dto = new IcConfigurationPropertyImpl();
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

	public static ConfigurationProperty convertIcConfigurationProperty(IcConfigurationProperty property)
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

	public static IcObjectPoolConfiguration convertConnIdPoolConfiguration(ObjectPoolConfiguration pool) {
		if (pool == null) {
			return null;
		}
		IcObjectPoolConfigurationImpl dto = new IcObjectPoolConfigurationImpl();
		dto.setMaxIdle(pool.getMaxIdle());
		dto.setMaxObjects(pool.getMaxObjects());
		dto.setMaxWait(pool.getMaxWait());
		dto.setMinEvictableIdleTimeMillis(pool.getMinEvictableIdleTimeMillis());
		dto.setMinIdle(pool.getMinIdle());
		return dto;
	}

	public static ObjectPoolConfiguration convertIcPoolConfiguration(IcObjectPoolConfiguration pool) {
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

	public static APIConfiguration convertIcConnectorConfiguration(IcConnectorConfiguration icConf,
			APIConfiguration defaultConnIdConf) {
		if (icConf == null) {
			return null;
		}
		((APIConfigurationImpl) defaultConnIdConf).setConnectorPoolingSupported(icConf.isConnectorPoolingSupported());
		defaultConnIdConf.setProducerBufferSize(icConf.getProducerBufferSize());

		IcConfigurationProperties properties = icConf.getConfigurationProperties();
		ConfigurationProperties connIdProperties = defaultConnIdConf.getConfigurationProperties();
		if (properties != null && properties.getProperties() != null) {
			for (IcConfigurationProperty icProperty : properties.getProperties()) {
				if (connIdProperties != null) {
					connIdProperties.setPropertyValue(icProperty.getName(), icProperty.getValue());
				}
			}
		}
		ObjectPoolConfiguration connectorPoolConfiguration = convertIcPoolConfiguration(
				icConf.getConnectorPoolConfiguration());
		((APIConfigurationImpl) defaultConnIdConf).setConnectorPoolConfiguration(connectorPoolConfiguration);
		return defaultConnIdConf;
	}

	public static Attribute convertIcAttribute(IcAttribute icAttribute) {
		if (icAttribute == null) {
			return null;
		}
		if (icAttribute instanceof IcEnabledAttribute && ((IcEnabledAttribute) icAttribute).getEnabled() != null) {
			return AttributeBuilder.buildEnabled(((IcEnabledAttribute) icAttribute).getEnabled());
		}
		if (icAttribute instanceof IcEnabledAttribute && ((IcEnabledAttribute) icAttribute).getEnabledDate() != null) {
			return AttributeBuilder.buildEnableDate(((IcEnabledAttribute) icAttribute).getEnabledDate());
		}
		if (icAttribute instanceof IcEnabledAttribute && ((IcEnabledAttribute) icAttribute).getDisabledDate() != null) {
			return AttributeBuilder.buildDisableDate(((IcEnabledAttribute) icAttribute).getDisabledDate());
		}
		if (icAttribute instanceof IcPasswordAttribute) {
			return AttributeBuilder.buildPassword(((IcPasswordAttribute) icAttribute).getPasswordValue() != null
					? ((IcPasswordAttribute) icAttribute).getPasswordValue().asString().toCharArray()
					: "".toCharArray());
		}
		if (icAttribute instanceof IcLoginAttribute) {
			return new Name((String) icAttribute.getValue());
		}
		if (!icAttribute.isMultiValue() && icAttribute.getValue() == null) {
			return AttributeBuilder.build(icAttribute.getName());
		}
		if (!icAttribute.isMultiValue() && icAttribute.getValue() != null) {
			return AttributeBuilder.build(icAttribute.getName(), icAttribute.getValue());
		}
		if (icAttribute.isMultiValue() && icAttribute.getValues() != null) {
			return AttributeBuilder.build(icAttribute.getName(), icAttribute.getValues());
		}

		return null;
	}

	public static IcAttribute convertConnIdAttribute(Attribute attribute) {
		if (attribute == null) {
			return null;
		}
		List<Object> value = attribute.getValue();
		if (attribute.is(Name.NAME)) {
			if (value == null || value.size() != 1 || !(value.get(0) instanceof String)) {
				throw new IllegalArgumentException("Login attribute must be fill and a single String value.");
			}
			return new IcLoginAttributeImpl(Name.NAME, (String) attribute.getValue().get(0));
		}
		if (attribute.is(OperationalAttributes.PASSWORD_NAME)) {
			eu.bcvsolutions.idm.security.api.domain.GuardedString password = null;
			if (value != null && value.size() == 1 && value.get(0) instanceof GuardedString) {
				password = new eu.bcvsolutions.idm.security.api.domain.GuardedString(
						((GuardedString) value.get(0)).toString());
			}
			return new IcPasswordAttributeImpl(password);
		}
		if (attribute.is(OperationalAttributes.ENABLE_NAME)) {
			Boolean enabled = Boolean.FALSE;
			if (value != null && value.size() == 1 && value.get(0) instanceof Boolean) {
				enabled = (Boolean) value.get(0);
			}
			return new IcEnabledAttributeImpl(enabled, OperationalAttributes.ENABLE_NAME);
		}
		if (value == null || value.isEmpty()) {
			return new IcAttributeImpl(attribute.getName(), null);
		}
		if (value.size() == 1) {
			return new IcAttributeImpl(attribute.getName(), value.get(0));
		} else {
			return new IcAttributeImpl(attribute.getName(), value);
		}
	}

	public static IcUidAttribute convertConnIdUid(Uid uid) {
		if (uid == null) {
			return null;
		}
		return new IcUidAttributeImpl(uid.getName(), uid.getUidValue(), uid.getRevision());
	}

	public static ObjectClass convertIcObjectClass(IcObjectClass objectClass) {
		if (objectClass == null) {
			return null;
		}
		return new ObjectClass(objectClass.getType());
	}

	public static IcObjectClass convertConnIdObjectClass(ObjectClass objectClass) {
		if (objectClass == null) {
			return null;
		}
		IcObjectClassImpl objectClassDto = new IcObjectClassImpl(objectClass.getObjectClassValue());
		objectClassDto.setDisplayName(objectClass.getDisplayNameKey());
		return objectClassDto;
	}

	public static Uid convertIcUid(IcUidAttribute uid) {
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

	public static IcConnectorObject convertConnIdConnectorObject(ConnectorObject connObject) {
		if (connObject == null) {
			return null;
		}
		IcObjectClass icClass = ConnIdIcConvertUtil.convertConnIdObjectClass(connObject.getObjectClass());
		Set<Attribute> attributes = connObject.getAttributes();
		List<IcAttribute> icAttributes = new ArrayList<>();
		if (attributes != null) {
			for (Attribute a : attributes) {
				icAttributes.add(ConnIdIcConvertUtil.convertConnIdAttribute(a));
			}
		}
		return new IcConnectorObjectImpl(connObject.getUid().getUidValue(), icClass, icAttributes);
	}

	public static IcSchema convertConnIdSchema(Schema schema) {
		if (schema == null) {
			return null;
		}
		IcSchemaImpl icSchema = new IcSchemaImpl();
		List<IcObjectClassInfo> objectClasses = icSchema.getDeclaredObjectClasses();

		for (ObjectClassInfo classInfo : schema.getObjectClassInfo()) {
			objectClasses.add(ConnIdIcConvertUtil.convertConnIdObjectClassInfo(classInfo));
		}
		if (schema.getSupportedObjectClassesByOperation() != null) {
			for (Class<? extends APIOperation> operation : schema.getSupportedObjectClassesByOperation().keySet()) {
				List<String> objectClassesForOperation = new ArrayList<>();
				Set<ObjectClassInfo> objectClasesConnid = schema.getSupportedObjectClassesByOperation().get(operation);
				for (ObjectClassInfo oci : objectClasesConnid) {
					objectClassesForOperation.add(oci.getType());
				}
				icSchema.getSupportedObjectClassesByOperation().put(operation.getSimpleName(),
						objectClassesForOperation);
			}
		}
		return icSchema;
	}

	public static IcObjectClassInfo convertConnIdObjectClassInfo(ObjectClassInfo objectClass) {
		if (objectClass == null) {
			return null;
		}

		IcObjectClassInfoImpl icObjectClass = new IcObjectClassInfoImpl();
		Set<AttributeInfo> attributeInfos = objectClass.getAttributeInfo();
		if (attributeInfos != null) {
			for (AttributeInfo attributeInfo : attributeInfos) {
				icObjectClass.getAttributeInfos().add(ConnIdIcConvertUtil.convertConnIdAttributeInfo(attributeInfo));
			}
		}

		icObjectClass.setType(objectClass.getType());
		icObjectClass.setAuxiliary(objectClass.isAuxiliary());
		icObjectClass.setContainer(objectClass.isContainer());
		return icObjectClass;
	}

	public static IcAttributeInfo convertConnIdAttributeInfo(AttributeInfo attribute) {
		if (attribute == null) {
			return null;
		}
		IcAttributeInfoImpl icAttribute = new IcAttributeInfoImpl();
		if (attribute.getType() != null) {
			if (GuardedString.class.isAssignableFrom(attribute.getType())) {
				// We do converse between BCV GuardedString and ConnId
				// GuardedString
				icAttribute.setClassType(eu.bcvsolutions.idm.security.api.domain.GuardedString.class.getName());
			} else {
				icAttribute.setClassType(attribute.getType().getName());
			}
		}
		icAttribute.setCreateable(attribute.isCreateable());
		icAttribute.setMultivalued(attribute.isMultiValued());
		icAttribute.setName(attribute.getName());
		icAttribute.setNativeName(attribute.getNativeName());
		icAttribute.setReadable(attribute.isReadable());
		icAttribute.setRequired(attribute.isRequired());
		icAttribute.setReturnedByDefault(attribute.isReturnedByDefault());
		icAttribute.setUpdateable(attribute.isUpdateable());
		return icAttribute;
	}

	public static IcSyncDelta convertConnIdSyncDelta(SyncDelta delta) {
		if (delta == null) {
			return null;
		}
		IcSyncToken token = ConnIdIcConvertUtil.convertConnIdSyncToken(delta.getToken());
		IcSyncDeltaTypeEnum deltaType = IcSyncDeltaTypeEnum.valueOf(delta.getDeltaType().name());
		IcUidAttribute previousUid = ConnIdIcConvertUtil.convertConnIdUid(delta.getPreviousUid());
		IcObjectClass objectClass = ConnIdIcConvertUtil.convertConnIdObjectClass(delta.getObjectClass());
		IcUidAttribute uid = ConnIdIcConvertUtil.convertConnIdUid(delta.getUid());
		IcConnectorObject object = ConnIdIcConvertUtil.convertConnIdConnectorObject(delta.getObject());

		return new IcSyncDeltaImpl(token, deltaType, previousUid, objectClass, uid, object);

	}

	public static IcSyncToken convertConnIdSyncToken(SyncToken token) {
		if (token == null) {
			return null;
		}

		return new IcSyncTokenImpl(token.getValue());
	}

	public static SyncToken convertIcSyncToken(IcSyncToken token) {
		if (token == null) {
			return null;
		}

		return new SyncToken(token.getValue());
	}

	public static Filter convertIcFilter(IcFilter filter) {
		if (filter == null) {
			return null;
		}
		if(filter instanceof IcAndFilter){
			List<IcFilter> subFilters = (List<IcFilter>) ((IcAndFilter) filter).getFilters();
			LinkedList<Filter> subFiltersConnId = new LinkedList<>();
			
			if(!subFilters.isEmpty()){
				subFilters.forEach(subFilter -> {
					subFiltersConnId.add(ConnIdIcConvertUtil.convertIcFilter(subFilter));
				});
			}
			return new AndFilter(subFiltersConnId);
		}
		
		if(filter instanceof IcOrFilter){
			List<IcFilter> subFilters = (List<IcFilter>) ((IcOrFilter) filter).getFilters();
			LinkedList<Filter> subFiltersConnId = new LinkedList<>();
			
			if(!subFilters.isEmpty()){
				subFilters.forEach(subFilter -> {
					subFiltersConnId.add(ConnIdIcConvertUtil.convertIcFilter(subFilter));
				});
			}
			return new OrFilter(subFiltersConnId);
		}
		
		if(filter instanceof IcNotFilter){
			return new NotFilter(ConnIdIcConvertUtil.convertIcFilter(((IcNotFilter) filter).getFilter()));
		}
		
		if(filter instanceof IcAttributeFilter){
			
			Attribute attr = ConnIdIcConvertUtil.convertIcAttribute(((IcAttributeFilter) filter).getAttribute());
			if(filter instanceof IcEqualsFilter){
				return new EqualsFilter(attr);
			}
			if(filter instanceof IcContainsFilter){
				return new ContainsFilter(attr);
			}
			if(filter instanceof IcEndsWithFilter){
				return new EndsWithFilter(attr);
			}
			if(filter instanceof IcContainsAllValuesFilter){
				return new ContainsAllValuesFilter(attr);
			}
			if(filter instanceof IcStartsWithFilter){
				return new StartsWithFilter(attr);
			}
			if(filter instanceof IcGreaterThanFilter){
				return new GreaterThanFilter(attr);
			}
			if(filter instanceof IcLessThanFilter){
				return new LessThanFilter(attr);
			}
		}
		
		
		
		return null;
		
	}
}
