package eu.bcvsolutions.idm.vs.connector.basic;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.eav.service.api.IdmFormAttributeService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcAttributeInfo;
import eu.bcvsolutions.idm.ic.api.IcConnector;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorCreate;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcConnectorRead;
import eu.bcvsolutions.idm.ic.api.IcConnectorSchema;
import eu.bcvsolutions.idm.ic.api.IcConnectorUpdate;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import eu.bcvsolutions.idm.ic.api.IcSchema;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.api.annotation.IcConnectorClass;
import eu.bcvsolutions.idm.ic.czechidm.domain.CzechIdMIcConvertUtil;
import eu.bcvsolutions.idm.ic.czechidm.domain.IcConnectorConfigurationCzechIdMImpl;
import eu.bcvsolutions.idm.ic.exception.IcException;
import eu.bcvsolutions.idm.ic.impl.IcAttributeImpl;
import eu.bcvsolutions.idm.ic.impl.IcAttributeInfoImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorObjectImpl;
import eu.bcvsolutions.idm.ic.impl.IcObjectClassImpl;
import eu.bcvsolutions.idm.ic.impl.IcObjectClassInfoImpl;
import eu.bcvsolutions.idm.ic.impl.IcSchemaImpl;
import eu.bcvsolutions.idm.ic.impl.IcUidAttributeImpl;
import eu.bcvsolutions.idm.vs.entity.VsAccount;
import eu.bcvsolutions.idm.vs.entity.VsAccount_;
import eu.bcvsolutions.idm.vs.service.api.VsAccountService;
import eu.bcvsolutions.idm.vs.service.api.dto.VsAccountDto;

//@Component - we want control create connector instances
@IcConnectorClass(displayName = "Virtual system for CzechIdM", framework = "czechidm", name = "virtual-system-basic", version = "0.2.0", configurationClass = BasicVirtualConfiguration.class)
public class BasicVirtualConnector
		implements IcConnector, IcConnectorRead, IcConnectorCreate, IcConnectorUpdate, IcConnectorSchema {

	@Autowired
	private FormService formService;
	@Autowired
	private IdmFormAttributeService formAttributeService;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private VsAccountService accountService;
	private BasicVirtualConfiguration virtualConfiguration;
	private IdmFormDefinition formDefinition;
	private String virtualSystemKey;
	private String connectorKey;
	private UUID systemId;

	@Override
	public void init(IcConnectorConfiguration configuration) {
		Assert.notNull(configuration);
		if (!(configuration instanceof IcConnectorConfigurationCzechIdMImpl)) {
			throw new IcException(
					MessageFormat.format("Connector configuration for virtual system must be instance of [{0}]",
							IcConnectorConfigurationCzechIdMImpl.class.getName()));
		}

		systemId = ((IcConnectorConfigurationCzechIdMImpl) configuration).getSystemId();
		if (systemId == null) {
			throw new IcException("System ID cannot be null (for virtual system)");
		}
		SysSystem system = this.systemService.get(systemId);
		if (system == null) {
			throw new IcException("System cannot be null (for virtual system)");
		}

		IcConnectorClass connectorAnnotation = this.getClass().getAnnotation(IcConnectorClass.class);
		IcConnectorInfo info = CzechIdMIcConvertUtil.convertConnectorClass(connectorAnnotation, this.getClass());

		// Load configuration object
		virtualConfiguration = (BasicVirtualConfiguration) CzechIdMIcConvertUtil
				.convertIcConnectorConfiguration(configuration, connectorAnnotation.configurationClass());

		// Validate configuration
		virtualConfiguration.validate();

		connectorKey = info.getConnectorKey().getFullName();
		virtualSystemKey = MessageFormat.format("{0}:systemId={1}", connectorKey, systemId.toString());
		String type = VsAccount.class.getName();

		// Create/Update form definition and attributes
		formDefinition = updateFormDefinition(virtualSystemKey, type, system, virtualConfiguration);
	}

	@Override
	public IcUidAttribute update(IcUidAttribute uid, IcObjectClass objectClass, List<IcAttribute> attributes) {
		Assert.notNull(objectClass, "Object class cannot be null!");
		Assert.notNull(attributes, "Attributes cannot be null!");
		
		if (!IcObjectClassInfo.ACCOUNT.equals(objectClass.getType())) {
			throw new IcException("Only ACCOUNT object class is supported now!");
		}
		String uidValue = uid.getUidValue();

		if (uidValue == null) {
			throw new IcException("UID value cannot be null!");
		}

		// Find account by UID and System ID
		VsAccountDto account = accountService.findByUidSystem(uidValue, systemId);
		if (account == null) {
			throw new IcException(MessageFormat.format("Vs account was not found for UID [{0}] and system ID [{1}]!", uidValue, systemId));
		}
		
		IcAttribute uidAttribute = geAttribute(attributes, IcAttributeInfo.NAME);

		// Update UID - if is different
		if (uidAttribute != null) {
			Object attributeUidValue = uidAttribute.getValue();
			if (!(attributeUidValue instanceof String)) {
				throw new IcException(MessageFormat.format("UID attribute value [{0}] must be String!", attributeUidValue));
			}
			if(!uidValue.equals(attributeUidValue)){
				account.setUid((String) attributeUidValue);
				account = accountService.save(account);
			}
		
		}

		UUID accountId = account.getId();
		
		// Update extended attributes
		Arrays.asList(virtualConfiguration.getAttributes()).forEach(virtualAttirbute -> {
			updateFormAttributeValue(uidValue, virtualAttirbute, accountId, attributes);
		});
		
		return new IcUidAttributeImpl(IcAttributeInfo.NAME, account.getUid(), null);
	}

	@Override
	public IcUidAttribute create(IcObjectClass objectClass, List<IcAttribute> attributes) {
		Assert.notNull(objectClass, "Object class cannot be null!");
		Assert.notNull(attributes, "Attributes cannot be null!");
		if (!IcObjectClassInfo.ACCOUNT.equals(objectClass.getType())) {
			throw new IcException("Only ACCOUNT object class is supported now!");
		}
		IcAttribute uidAttribute = geAttribute(attributes, IcAttributeInfo.NAME);

		if (uidAttribute == null) {
			throw new IcException("UID attribute was not found!");
		}
		Object uidValue = uidAttribute.getValue();
		if (!(uidValue instanceof String)) {
			throw new IcException(MessageFormat.format("UID attribute value [{0}] must be String!", uidValue));
		}

		VsAccountDto account = new VsAccountDto();
		account.setUid((String) uidValue);
		account.setSystemId(this.systemId);
		account.setConnectorKey(connectorKey);

		account = accountService.save(account);
		UUID accountId = account.getId();

		// Attributes from definition and configuration
		Arrays.asList(virtualConfiguration.getAttributes()).forEach(virtualAttirbute -> {
			updateFormAttributeValue(uidValue, virtualAttirbute, accountId, attributes);
		});
		
		return new IcUidAttributeImpl(IcAttributeInfo.NAME, account.getUid(), null);
	}

	@Override
	public IcConnectorObject read(IcUidAttribute uid, IcObjectClass objectClass) {
		Assert.notNull(objectClass, "Object class cannot be null!");
		if (!IcObjectClassInfo.ACCOUNT.equals(objectClass.getType())) {
			throw new IcException("Only ACCOUNT object class is supported now!");
		}
		String uidValue = uid.getUidValue();

		if (uidValue == null) {
			throw new IcException("UID value cannot be null!");
		}

		// Find account by UID and System ID
		VsAccountDto account = accountService.findByUidSystem(uidValue, systemId);
		if (account == null) {
			return null;
		}

		UUID accountId = account.getId();

		IcConnectorObjectImpl connectorObject = new IcConnectorObjectImpl();
		connectorObject.setUidValue(account.getUid());
		connectorObject.setObjectClass(new IcObjectClassImpl(IcObjectClassInfo.ACCOUNT));
		List<IcAttribute> attributes = connectorObject.getAttributes();

		// Attributes from definition and configuration
		Arrays.asList(virtualConfiguration.getAttributes()).forEach(virtualAttirbute -> {
			IdmFormAttribute attributeDefinition = this.formAttributeService.findAttribute(formDefinition.getType(), formDefinition.getCode(), virtualAttirbute);
			List<AbstractFormValue<VsAccount>> values = this.formService.getValues(accountId, VsAccount.class, this.formDefinition, virtualAttirbute);
			if(CollectionUtils.isEmpty(values)){
				return;
			}
		
			List<Object> valuesObject = values.stream()
			.map(AbstractFormValue::getValue)
			.collect(Collectors.toList());	
	
			IcAttributeImpl attribute = new IcAttributeImpl();
			attribute.setMultiValue(attributeDefinition.isMultiple());
			attribute.setName(virtualAttirbute);
			attribute.setValues(valuesObject);
			attributes.add(attribute);
		});
		
		return connectorObject;

	}

	@Override
	public IcSchema schema() {
		if (this.formDefinition == null) {
			return null;
		}

		IcSchemaImpl schema = new IcSchemaImpl();
		List<IcObjectClassInfo> objectClasses = schema.getDeclaredObjectClasses();
		IcObjectClassInfoImpl objectClass = new IcObjectClassInfoImpl();
		objectClass.setType(IcObjectClassInfo.ACCOUNT);
		List<IcAttributeInfo> attributes = objectClass.getAttributeInfos();
		// Create UID schema attribute
		IcAttributeInfoImpl attributeUid = new IcAttributeInfoImpl();
		attributeUid.setClassType(String.class.getName());
		attributeUid.setCreateable(true);
		attributeUid.setMultivalued(false);
		attributeUid.setName(IcAttributeInfo.NAME);
		attributeUid.setNativeName(VsAccount_.uid.getName());
		attributeUid.setReadable(true);
		attributeUid.setRequired(true);
		attributeUid.setReturnedByDefault(true);
		attributeUid.setUpdateable(true);

		attributes.add(attributeUid);

		// Create UID schema attribute
		IcAttributeInfoImpl attributeDisabled = new IcAttributeInfoImpl();
		attributeDisabled.setClassType(Boolean.class.getName());
		attributeDisabled.setCreateable(true);
		attributeDisabled.setMultivalued(false);
		attributeDisabled.setName(IcAttributeInfo.ENABLE);
		attributeDisabled.setNativeName(VsAccount_.enable.getName());
		attributeDisabled.setReadable(true);
		attributeDisabled.setRequired(false);
		attributeDisabled.setReturnedByDefault(false);
		attributeDisabled.setUpdateable(true);

		attributes.add(attributeDisabled);

		// Attributes from definition and configuration
		Arrays.asList(virtualConfiguration.getAttributes()).forEach(virtualAttirbute -> {
			IdmFormAttribute formAttribute = formAttributeService.findAttribute(VsAccount.class.getName(),
					formDefinition.getCode(), virtualAttirbute);
			if (formAttribute == null) {
				return;
			}
			IcAttributeInfoImpl attribute = new IcAttributeInfoImpl();
			String classType = this.convertToSchemaClassType(formAttribute.getPersistentType());
			attribute.setClassType(classType);
			attribute.setCreateable(!formAttribute.isReadonly());
			attribute.setMultivalued(formAttribute.isMultiple());
			attribute.setName(virtualAttirbute);
			attribute.setNativeName(virtualAttirbute);
			attribute.setReadable(true);
			attribute.setRequired(formAttribute.isRequired());
			attribute.setReturnedByDefault(true);
			attribute.setUpdateable(!formAttribute.isReadonly());

			attributes.add(attribute);
		});

		objectClasses.add(objectClass);

		return schema;
	}

	/**
	 * Find UID attribute
	 * 
	 * @param attributes
	 * @return
	 */
	private IcAttribute geAttribute(List<IcAttribute> attributes, String name) {
		Assert.notNull(attributes);
		Assert.notNull(name);

		return attributes.stream().filter(attribute -> name.equals(attribute.getName())).findFirst().orElse(null);
	}

	private String convertToSchemaClassType(PersistentType persistentType) {
		switch (persistentType) {
		case INT:
			return Integer.class.getName();
		case LONG:
			return Long.class.getName();
		case BOOLEAN:
			return Boolean.class.getName();
		case DATE:
		case DATETIME:
			return DateTime.class.getName();
		case DOUBLE:
		case CURRENCY:
			return Double.class.getName();
		case CHAR:
			return Character.class.getName();
		case BYTEARRAY: {
			return byte[].class.getName();
		}
		default:
			return String.class.getName();
		}
	}

	/**
	 * Create/Update form definition and attributes
	 * 
	 * @param key
	 * @param type
	 * @param system
	 * @param virtualConfiguration
	 * @return
	 */
	private IdmFormDefinition updateFormDefinition(String key, String type, SysSystem system,
			BasicVirtualConfiguration virtualConfiguration) {
		// TODO: delete attribute definitions

		IdmFormDefinition definition = this.formService.getDefinition(type, key);
		List<IdmFormAttribute> formAttributes = new ArrayList<>();
		Arrays.asList(virtualConfiguration.getAttributes()).forEach(virtualAttirbute -> {
			IdmFormAttribute formAttribute = formAttributeService.findAttribute(type, key, virtualAttirbute);
			if (formAttribute == null) {
				formAttribute = createFromAttribute(virtualAttirbute);
				formAttribute.setFormDefinition(definition);
				formAttributes.add(formAttribute);
			}
		});

		if (definition == null) {
			IdmFormDefinition createdDefinition = this.formService.createDefinition(type, key, formAttributes);
			createdDefinition.setName(MessageFormat.format("Virtual system for [{0}]", system.getName()));
			createdDefinition.setUnmodifiable(true);
			return this.formService.saveDefinition(createdDefinition);
		} else {
			formAttributes.forEach(formAttribute -> {
				this.formService.saveAttribute(formAttribute);
			});
			return definition;
		}
	}
	
	private void updateFormAttributeValue(Object uidValue, String virtualAttirbute, UUID accountId,
			List<IcAttribute> attributes) {
		IcAttribute attribute = geAttribute(attributes, virtualAttirbute);
		if(attribute == null){
			return;
		}
		List<Object> values = attribute.getValues();
		List<Serializable> serializableValues = new ArrayList<>();
		if (values != null) {
			values.forEach(value -> {
				if (!(value instanceof Serializable)) {
					throw new IcException(MessageFormat.format(
							"Ic attribute value [{0}] is not Serializable! For account with UID [{1}].", value,
							uidValue));
				}
				serializableValues.add((Serializable) value);
			});
		}
		
		formService.saveValues(accountId, VsAccount.class, this.formDefinition, virtualAttirbute, serializableValues);
	}

	private IdmFormAttribute createFromAttribute(String virtualAttirbute) {
		IdmFormAttribute formAttribute = new IdmFormAttribute();
		formAttribute.setCode(virtualAttirbute);
		formAttribute.setConfidential(false);
		formAttribute.setPersistentType(PersistentType.TEXT);
		formAttribute.setMultiple(false);
		formAttribute.setName(virtualAttirbute);
		formAttribute.setRequired(false);
		return formAttribute;
	}
}
