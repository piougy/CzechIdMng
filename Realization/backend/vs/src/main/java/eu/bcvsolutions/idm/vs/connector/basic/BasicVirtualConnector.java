package eu.bcvsolutions.idm.vs.connector.basic;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
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
import eu.bcvsolutions.idm.ic.impl.IcAttributeInfoImpl;
import eu.bcvsolutions.idm.ic.impl.IcObjectClassInfoImpl;
import eu.bcvsolutions.idm.ic.impl.IcSchemaImpl;
import eu.bcvsolutions.idm.vs.entity.VsAccount;
import eu.bcvsolutions.idm.vs.entity.VsAccount_;
import eu.bcvsolutions.idm.vs.service.api.VsAccountService;

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

	@Override
	public void init(IcConnectorConfiguration configuration) {
		Assert.notNull(configuration);
		if (!(configuration instanceof IcConnectorConfigurationCzechIdMImpl)) {
			throw new IcException(
					MessageFormat.format("Connector configuration for virtual system must be instance of [{0}]",
							IcConnectorConfigurationCzechIdMImpl.class.getName()));
		}

		UUID systemId = ((IcConnectorConfigurationCzechIdMImpl) configuration).getSystemId();
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

		String key = MessageFormat.format("{0}:systemId={1}", info.getConnectorKey().getFullName(),
				systemId.toString());
		String type = VsAccount.class.getName();

		// Create/Update form definition and attributes
		formDefinition = updateFormDefinition(key, type, system, virtualConfiguration);
	}

	@Override
	public IcUidAttribute update(IcUidAttribute uid, IcObjectClass objectClass, List<IcAttribute> attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IcUidAttribute create(IcObjectClass objectClass, List<IcAttribute> attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IcConnectorObject read(IcUidAttribute uid, IcObjectClass objectClass) {
		// TODO Auto-generated method stub
		return null;
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
		attributeDisabled.setClassType(String.class.getName());
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
