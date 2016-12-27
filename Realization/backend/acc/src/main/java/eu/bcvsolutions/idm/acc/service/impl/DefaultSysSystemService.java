package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.SchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.entity.SysConnectorKey;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemFormValue;
import eu.bcvsolutions.idm.acc.repository.AccAccountRepository;
import eu.bcvsolutions.idm.acc.repository.SysSystemEntityRepository;
import eu.bcvsolutions.idm.acc.repository.SysSystemRepository;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityHandlingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.service.api.FormService;
import eu.bcvsolutions.idm.eav.service.impl.AbstractFormableService;
import eu.bcvsolutions.idm.icf.api.IcfAttributeInfo;
import eu.bcvsolutions.idm.icf.api.IcfConfigurationProperties;
import eu.bcvsolutions.idm.icf.api.IcfConfigurationProperty;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.api.IcfObjectClassInfo;
import eu.bcvsolutions.idm.icf.api.IcfSchema;
import eu.bcvsolutions.idm.icf.impl.IcfConfigurationPropertiesImpl;
import eu.bcvsolutions.idm.icf.impl.IcfConfigurationPropertyImpl;
import eu.bcvsolutions.idm.icf.impl.IcfConnectorConfigurationImpl;
import eu.bcvsolutions.idm.icf.impl.IcfConnectorKeyImpl;
import eu.bcvsolutions.idm.icf.service.api.IcfConfigurationFacade;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

/**
 * Deafult target system configuration service
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysSystemService extends AbstractFormableService<SysSystem, QuickFilter>
		implements SysSystemService {

	private final SysSystemRepository systemRepository;
	private final IcfConfigurationFacade icfConfigurationFacade;
	private final SysSchemaObjectClassService objectClassService;
	private final SysSchemaAttributeService attributeService;
	private final SysRoleSystemService roleSystemService;
	private final SysSystemEntityRepository systemEntityRepository;
	private final AccAccountRepository accountRepository;
	private final SysSystemEntityHandlingService systemEntityHandlingService;
	/**
	 * Connector property type vs. eav type mapping
	 */
	private static final Map<String, ConnectorPropertyMapping> supportedConnectorPropertyMapping;

	static {
		// TODO: converter registration?
		supportedConnectorPropertyMapping = new HashMap<>();
		supportedConnectorPropertyMapping.put("java.lang.Boolean",
				new ConnectorPropertyMapping(PersistentType.BOOLEAN, false));
		supportedConnectorPropertyMapping.put("boolean", new ConnectorPropertyMapping(PersistentType.BOOLEAN, false));
		supportedConnectorPropertyMapping.put("org.identityconnectors.common.security.GuardedString",
				new ConnectorPropertyMapping(PersistentType.TEXT, false));
		supportedConnectorPropertyMapping.put(GuardedString.class.getName(),
				new ConnectorPropertyMapping(PersistentType.TEXT, false));
		supportedConnectorPropertyMapping.put("char", new ConnectorPropertyMapping(PersistentType.CHAR, false));
		supportedConnectorPropertyMapping.put("java.lang.String",
				new ConnectorPropertyMapping(PersistentType.TEXT, false));
		supportedConnectorPropertyMapping.put("[Ljava.lang.String;",
				new ConnectorPropertyMapping(PersistentType.TEXT, true));
		supportedConnectorPropertyMapping.put("int", new ConnectorPropertyMapping(PersistentType.INT, false));
		supportedConnectorPropertyMapping.put("long", new ConnectorPropertyMapping(PersistentType.LONG, false));
		// TODO: correct data type ...
		supportedConnectorPropertyMapping.put("org.identityconnectors.common.security.GuardedByteArray",
				new ConnectorPropertyMapping(PersistentType.TEXT, false)); // TODO byte[] persistent type
	}

	@Autowired
	public DefaultSysSystemService(
			SysSystemRepository systemRepository,
			FormService formService,
			IcfConfigurationFacade icfConfigurationFacade, 
			SysSchemaObjectClassService objectClassService,
			SysSchemaAttributeService attributeService,
			SysRoleSystemService roleSystemService,
			SysSystemEntityRepository systemEntityRepository,
			AccAccountRepository accountRepository,
			SysSystemEntityHandlingService systemEntityHandlingService) {
		super(systemRepository, formService);
		//
		Assert.notNull(icfConfigurationFacade);
		Assert.notNull(objectClassService);
		Assert.notNull(attributeService);
		Assert.notNull(roleSystemService);
		Assert.notNull(systemEntityRepository);
		Assert.notNull(accountRepository);
		Assert.notNull(systemEntityHandlingService);
		//
		this.systemRepository = systemRepository;
		this.icfConfigurationFacade = icfConfigurationFacade;
		this.objectClassService = objectClassService;
		this.attributeService = attributeService;
		this.roleSystemService = roleSystemService;
		this.systemEntityRepository = systemEntityRepository;
		this.accountRepository = accountRepository;
		this.systemEntityHandlingService = systemEntityHandlingService;
	}

	@Override
	@Transactional
	public void delete(SysSystem system) {
		Assert.notNull(system);
		//
		// if exists accounts or system entities, then system could not be deleted
		if (systemEntityRepository.countBySystem(system) > 0) {
			throw new ResultCodeException(AccResultCode.SYSTEM_DELETE_FAILED_HAS_ENTITIES, ImmutableMap.of("system", system.getName()));
		}
		if (accountRepository.countBySystem(system) > 0) {
			throw new ResultCodeException(AccResultCode.SYSTEM_DELETE_FAILED_HAS_ACCOUNTS, ImmutableMap.of("system", system.getName()));
		}
		// delete all mappings
		systemEntityHandlingService.findBySystem(system, null, null).forEach(systemEntityHandling -> {
			systemEntityHandlingService.delete(systemEntityHandling);
		});
		SchemaObjectClassFilter filter = new SchemaObjectClassFilter();
		filter.setSystemId(system.getId());	
		objectClassService.find(filter, null).forEach(schemaObjectClass -> {
			objectClassService.delete(schemaObjectClass);
		});
		// delete mapped roles
		RoleSystemFilter roleSystemFilter = new RoleSystemFilter();
		roleSystemFilter.setSystemId(system.getId());
		roleSystemService.find(roleSystemFilter, null).forEach(roleSystem -> {
			roleSystemService.delete(roleSystem);
		});
		//
		super.delete(system);
	}

	@Override
	@Transactional(readOnly = true)
	public SysSystem getByName(String name) {
		return systemRepository.findOneByName(name);
	}

	@Override
	@Transactional
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public IcfConnectorConfiguration getConnectorConfiguration(SysSystem system) {
		Assert.notNull(system);
		
		if(system.getConnectorKey() == null){
			return null;
		}
		// load filled form values
		IdmFormDefinition formDefinition = getConnectorFormDefinition(system.getConnectorKey());
		List<AbstractFormValue<SysSystem>> formValues = getFormService().getValues(system, formDefinition);
		Map<String, List<AbstractFormValue<SysSystem>>> attributeValues = getFormService().toValueMap(formValues);
		// fill connector configuration from form values
		IcfConnectorConfigurationImpl icfConf = new IcfConnectorConfigurationImpl();
		IcfConfigurationProperties properties = new IcfConfigurationPropertiesImpl();
		icfConf.setConfigurationProperties(properties);
		for (Entry<String, List<AbstractFormValue<SysSystem>>> attribute : attributeValues.entrySet()) {
			String attributeName = attribute.getKey();
			IdmFormAttribute formAttribute = formDefinition.getMappedAttributeByName(attributeName);
			IcfConfigurationPropertyImpl property = new IcfConfigurationPropertyImpl();
			property.setName(attributeName);
			// convert form attribute values to connector properties
			Object value = null;
			if (!attribute.getValue().isEmpty()) {
				if (formAttribute.isMultiple()) {
					List valueList = formAttribute.getEmptyList();
					for (AbstractFormValue<SysSystem> formValue : attribute.getValue()) {
						valueList.add(toPropertyValue(formValue));
					}
					if (!valueList.isEmpty()) {
						if (PersistentType.TEXT == formAttribute.getPersistentType()) {
							value = valueList.toArray(new String[]{});
						} else {
							value = valueList.toArray();
						}
					}
				} else {
					// single value
					value = toPropertyValue(attribute.getValue().get(0));
				}
			}
			if (value != null) {
				// only filled values to configuration
				property.setValue(value);
				properties.getProperties().add(property);
			}
		}
		return icfConf;
	}

	@Override
	@Transactional
	public void generateSchema(SysSystem system) {
		Assert.notNull(system);

		// Find connector identification persisted in system
		IcfConnectorKey connectorKey = system.getConnectorKey();
		if (connectorKey == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_KEY_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Find connector configuration persisted in system
		IcfConnectorConfiguration connectorConfig = getConnectorConfiguration(system);
		if (connectorConfig == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Call ICF module and find schema for given connector key and
		// configuration
		IcfSchema icfSchema = icfConfigurationFacade.getSchema(connectorKey, connectorConfig);
		if (icfSchema == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_SCHEMA_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Load existing object class from system
		SchemaObjectClassFilter objectClassFilter = new SchemaObjectClassFilter();
		objectClassFilter.setSystemId(system.getId());
		List<SysSchemaObjectClass> sysObjectClassesInSystem = null;
		Page<SysSchemaObjectClass> page = objectClassService.find(objectClassFilter, null);
		sysObjectClassesInSystem = page.getContent();

		// Convert ICF schema to ACC entities
		List<SysSchemaObjectClass> sysObjectClasses = new ArrayList<SysSchemaObjectClass>();
		List<SysSchemaAttribute> sysAttributes = new ArrayList<SysSchemaAttribute>();
		for (IcfObjectClassInfo objectClass : icfSchema.getDeclaredObjectClasses()) {
			
			// We can create only ICF schemas, it means only schemas created for __ACCOUNT__ and __GROUP__
			if(!(objectClass.getType().startsWith("__") && objectClass.getType().endsWith("__"))){
				continue;
			}
			SysSchemaObjectClass sysObjectClass = null;
			// If existed some object class in system, then we will compared
			// every object with object class in resource
			// If will be same (same name), then we do only refresh object
			// values from resource
			if (sysObjectClassesInSystem != null) {
				Optional<SysSchemaObjectClass> objectClassSame = sysObjectClassesInSystem.stream()
						.filter(objectClassInSystem -> { //
							return objectClassInSystem.getObjectClassName().equals(objectClass.getType());
						}) //
						.findFirst();
				if (objectClassSame.isPresent()) {
					sysObjectClass = objectClassSame.get();
				}
			}
			// Convert ICF object class to ACC (if is null, then will be created
			// new instance)
			sysObjectClass = convertIcfObjectClassInfo(objectClass, sysObjectClass);
			sysObjectClass.setSystem(system);
			sysObjectClasses.add(sysObjectClass);

			List<SysSchemaAttribute> attributesInSystem = null;
			// Load existing attributes for existing object class in system
			if (sysObjectClass.getId() != null) {
				SchemaAttributeFilter attFilter = new SchemaAttributeFilter();
				attFilter.setSystemId(system.getId());
				attFilter.setObjectClassId(sysObjectClass.getId());

				Page<SysSchemaAttribute> attributesInSystemPage = attributeService.find(attFilter, null);
				attributesInSystem = attributesInSystemPage.getContent();
			}
			for (IcfAttributeInfo attribute : objectClass.getAttributeInfos()) {
				// If will be ICF and ACC attribute same (same name), then we
				// will do only refresh object values from resource
				SysSchemaAttribute sysAttribute = null;
				if (attributesInSystem != null) {
					Optional<SysSchemaAttribute> sysAttributeOptional = attributesInSystem.stream().filter(a -> {
						return a.getName().equals(attribute.getName());
					}).findFirst();
					if (sysAttributeOptional.isPresent()) {
						sysAttribute = sysAttributeOptional.get();
					}
				}
				sysAttribute = convertIcfAttributeInfo(attribute, sysAttribute);
				sysAttribute.setObjectClass(sysObjectClass);
				sysAttributes.add(sysAttribute);
			}
		}

		// Persist generated schema to system
		objectClassService.saveAll(sysObjectClasses);
		attributeService.saveAll(sysAttributes);
	}

	private SysSchemaObjectClass convertIcfObjectClassInfo(IcfObjectClassInfo objectClass,
			SysSchemaObjectClass sysObjectClass) {
		if (objectClass == null) {
			return null;
		}
		if (sysObjectClass == null) {
			sysObjectClass = new SysSchemaObjectClass();
		}
		sysObjectClass.setObjectClassName(objectClass.getType());
		sysObjectClass.setAuxiliary(objectClass.isAuxiliary());
		sysObjectClass.setContainer(objectClass.isContainer());
		return sysObjectClass;
	}

	private SysSchemaAttribute convertIcfAttributeInfo(IcfAttributeInfo attributeInfo,
			SysSchemaAttribute sysAttribute) {
		if (attributeInfo == null) {
			return null;
		}
		if (sysAttribute == null) {
			sysAttribute = new SysSchemaAttribute();
		}
		sysAttribute.setClassType(attributeInfo.getClassType());
		sysAttribute.setName(attributeInfo.getName());
		sysAttribute.setMultivalued(attributeInfo.isMultivalued());
		sysAttribute.setNativeName(attributeInfo.getNativeName());
		sysAttribute.setReadable(attributeInfo.isReadable());
		sysAttribute.setRequired(attributeInfo.isRequired());
		sysAttribute.setReturnedByDefault(attributeInfo.isReturnedByDefault());
		sysAttribute.setUpdateable(attributeInfo.isUpdateable());
		sysAttribute.setCreateable(attributeInfo.isCreateable());
		return sysAttribute;
	}

	@Override
	@Transactional
	public IdmFormDefinition getConnectorFormDefinition(IcfConnectorKey connectorKey) {
		Assert.notNull(connectorKey);
		//
		// if form definition for given key already exists
		IdmFormDefinition formDefinition = getFormService().getDefinition(connectorKey.getConnectorName(),
				connectorKey.getFullName());
		if (formDefinition == null) {
			// we creates new form definition
			formDefinition = createConnectorFormDefinition(connectorKey);
		}
		return formDefinition;
	}

	/**
	 * Create form definition to given connectorKey by connector properties
	 * 
	 * @param connectorKey
	 * @return
	 */
	private synchronized IdmFormDefinition createConnectorFormDefinition(IcfConnectorKey connectorKey) {
		IcfConnectorConfiguration conf = icfConfigurationFacade.getConnectorConfiguration(connectorKey);
		if (conf == null) {
			throw new IllegalStateException(MessageFormat.format("Connector with key [{0}] was not found on classpath.",
					connectorKey.getFullName()));
		}
		//
		List<IdmFormAttribute> formAttributes = new ArrayList<>();
		for (short seq = 0; seq < conf.getConfigurationProperties().getProperties().size(); seq++) {
			IcfConfigurationProperty property = conf.getConfigurationProperties().getProperties().get(seq);
			IdmFormAttribute attribute = toAttribute(property);
			attribute.setSeq(seq);
			formAttributes.add(attribute);
		}
		return getFormService().createDefinition(connectorKey.getConnectorName(), connectorKey.getFullName(),
				formAttributes);
	}

	/**
	 * Returns eav form attribute from given connector property
	 * 
	 * @param property
	 * @return
	 */
	private IdmFormAttribute toAttribute(IcfConfigurationProperty property) {
		IdmFormAttribute attribute = new IdmFormAttribute();
		attribute.setName(property.getName());
		attribute.setDisplayName(property.getDisplayName());
		attribute.setDescription(property.getHelpMessage());
		attribute.setPersistentType(convertPropertyType(property.getType()));
		attribute.setConfidential(property.isConfidential());
		attribute.setRequired(property.isRequired());
		attribute.setMultiple(isMultipleProperty(property.getType()));
		attribute.setDefaultValue(convertDefaultValue(property));
		return attribute;
	}

	/**
	 * Converts default value by property data type. If property supports
	 * multiple values, then return multi lines string
	 * 
	 * @param property
	 * @return
	 */
	private String convertDefaultValue(IcfConfigurationProperty property) {
		if (property.getValue() == null) {
			return null;
		}
		StringBuilder result = new StringBuilder();
		if (!isMultipleProperty(property.getType())) {
			result.append(property.getValue());
		} else {
			// arrays only - see supportedConnectorPropertyMapping
			Object[] values = (Object[]) property.getValue();
			for (Object singleValue : values) {
				if (result.length() > 0) {
					result.append(System.getProperty("line.separator"));
				}
				result.append(singleValue);
			}
		}
		return result.toString();
	}

	/**
	 * Returns connector property value from given eav value
	 * 
	 * @param formValue
	 * @return
	 */
	private Object toPropertyValue(AbstractFormValue<SysSystem> formValue) {
		if (formValue == null) {
			return null;
		}
		if (formValue.isConfidential()) {
			// persistent value from confidential storage
			Serializable value = getFormService().getConfidentialPersistentValue(formValue);
			if (value == null) {
				return null;
			}
			return new org.identityconnectors.common.security.GuardedString(
					value.toString().toCharArray());
		}
		return formValue.getValue();
	}

	/**
	 * Returns true, if connector property supports multiple values
	 * 
	 * @param type
	 * @return
	 */
	private boolean isMultipleProperty(String connectorPropertyType) {
		if (!supportedConnectorPropertyMapping.containsKey(connectorPropertyType)) {
			throw new UnsupportedOperationException(
					MessageFormat.format("Unsupported connector property data type [{0}]", connectorPropertyType));
		}
		return supportedConnectorPropertyMapping.get(connectorPropertyType).multiple;
	}

	/**
	 * Returns persistent type for given connector property type
	 * 
	 * TODO: Move to convert utils
	 * 
	 * @param type
	 * @return
	 */
	public static PersistentType convertPropertyType(String connectorPropertyType) {
		if (!supportedConnectorPropertyMapping.containsKey(connectorPropertyType)) {
			throw new UnsupportedOperationException(
					MessageFormat.format("Unsupported connector property data type [{0}]", connectorPropertyType));
		}
		return supportedConnectorPropertyMapping.get(connectorPropertyType).persistentType;
	}

	/**
	 * Connector property type vs. eav type mapping
	 * 
	 * @author Radek Tomiška
	 *
	 */
	private static class ConnectorPropertyMapping {

		PersistentType persistentType;
		boolean multiple;

		public ConnectorPropertyMapping(PersistentType persistentType, boolean multiple) {
			this.persistentType = persistentType;
			this.multiple = multiple;
		}
	}

	@Deprecated
	@Transactional
	public SysSystem createTestSystem() {
		// create owner
		SysSystem system = new SysSystem();
		system.setName("sysOne_" + System.currentTimeMillis());
		system.setConnectorKey(new SysConnectorKey(getTestConnectorKey()));
		save(system);

		IdmFormDefinition savedFormDefinition = getConnectorFormDefinition(system.getConnectorKey());

		List<SysSystemFormValue> values = new ArrayList<>();
		SysSystemFormValue host = new SysSystemFormValue(savedFormDefinition.getMappedAttributeByName("host"));
		host.setValue("localhost");
		values.add(host);
		SysSystemFormValue port = new SysSystemFormValue(savedFormDefinition.getMappedAttributeByName("port"));
		port.setValue("5432");
		values.add(port);
		SysSystemFormValue user = new SysSystemFormValue(savedFormDefinition.getMappedAttributeByName("user"));
		user.setValue("idmadmin");
		values.add(user);
		SysSystemFormValue password = new SysSystemFormValue(savedFormDefinition.getMappedAttributeByName("password"));
		password.setValue("idmadmin");
		values.add(password);
		SysSystemFormValue database = new SysSystemFormValue(savedFormDefinition.getMappedAttributeByName("database"));
		database.setValue("bcv_idm_storage");
		values.add(database);
		SysSystemFormValue table = new SysSystemFormValue(savedFormDefinition.getMappedAttributeByName("table"));
		table.setValue("system_users");
		values.add(table);
		SysSystemFormValue keyColumn = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("keyColumn"));
		keyColumn.setValue("name");
		values.add(keyColumn);
		SysSystemFormValue passwordColumn = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("passwordColumn"));
		passwordColumn.setValue("password");
		values.add(passwordColumn);
		SysSystemFormValue allNative = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("allNative"));
		allNative.setValue(Boolean.TRUE);
		values.add(allNative);
		SysSystemFormValue jdbcDriver = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("jdbcDriver"));
		jdbcDriver.setValue("org.postgresql.Driver");
		values.add(jdbcDriver);
		SysSystemFormValue jdbcUrlTemplate = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("jdbcUrlTemplate"));
		jdbcUrlTemplate.setValue("jdbc:postgresql://%h:%p/%d");
		values.add(jdbcUrlTemplate);
		SysSystemFormValue rethrowAllSQLExceptions = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("rethrowAllSQLExceptions"));
		rethrowAllSQLExceptions.setValue(Boolean.TRUE);
		values.add(rethrowAllSQLExceptions);
		SysSystemFormValue statusColumn = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("statusColumn"));
		statusColumn.setValue("status");
		values.add(statusColumn);
		SysSystemFormValue disabledStatusValue = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("disabledStatusValue"));
		disabledStatusValue.setValue("disabled");
		values.add(disabledStatusValue);
		SysSystemFormValue enabledStatusValue = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("enabledStatusValue"));
		enabledStatusValue.setValue("enabled");
		values.add(enabledStatusValue);

		getFormService().saveValues(system, savedFormDefinition, values);

		return system;
	}

	/**
	 * Basic table connector
	 * 
	 * @return
	 */
	@Deprecated
	public IcfConnectorKey getTestConnectorKey() {
		IcfConnectorKeyImpl key = new IcfConnectorKeyImpl();
		key.setFramework("connId");
		key.setConnectorName("net.tirasa.connid.bundles.db.table.DatabaseTableConnector");
		key.setBundleName("net.tirasa.connid.bundles.db.table");
		key.setBundleVersion("2.2.4");
		return key;
	}
}
