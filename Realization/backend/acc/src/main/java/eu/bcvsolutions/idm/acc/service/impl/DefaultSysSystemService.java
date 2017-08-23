package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.SysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysConnectorKey;
import eu.bcvsolutions.idm.acc.entity.SysConnectorServer;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemFormValue;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.repository.AccAccountRepository;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningArchiveRepository;
import eu.bcvsolutions.idm.acc.repository.SysSystemEntityRepository;
import eu.bcvsolutions.idm.acc.repository.SysSystemRepository;
import eu.bcvsolutions.idm.acc.service.api.FormPropertyManager;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemFormValueService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormableService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcAttributeInfo;
import eu.bcvsolutions.idm.ic.api.IcConfigurationProperties;
import eu.bcvsolutions.idm.ic.api.IcConfigurationProperty;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import eu.bcvsolutions.idm.ic.api.IcSchema;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.impl.IcConfigurationPropertiesImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorConfigurationImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorKeyImpl;
import eu.bcvsolutions.idm.ic.impl.IcObjectClassImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationFacade;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Default target system configuration service
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysSystemService extends AbstractFormableService<SysSystem, SysSystemFilter>
		implements SysSystemService {

	private final SysSystemRepository systemRepository;
	private final IcConfigurationFacade icConfigurationFacade;
	private final SysSchemaObjectClassService objectClassService;
	private final SysSchemaAttributeService attributeService;
	private final SysSystemEntityRepository systemEntityRepository;
	private final AccAccountRepository accountRepository;
	private final SysSyncConfigService synchronizationConfigService;
	private final FormPropertyManager formPropertyManager;
	private final SysProvisioningArchiveRepository provisioningArchiveRepository;
	private final ConfidentialStorage confidentialStorage;
	private final IcConnectorFacade connectorFacade;
	private final EntityManager entityManager;
	private final SysSystemFormValueService systemFormValueService;
	private final SysSystemMappingService systemMappingService;
	private final SysSystemAttributeMappingService systemAttributeMappingService;

	@Autowired
	public DefaultSysSystemService(SysSystemRepository systemRepository, FormService formService,
			IcConfigurationFacade icConfigurationFacade, SysSchemaObjectClassService objectClassService,
			SysSchemaAttributeService attributeService, SysSystemEntityRepository systemEntityRepository,
			AccAccountRepository accountRepository, SysSyncConfigService synchronizationConfigService,
			FormPropertyManager formPropertyManager, SysProvisioningArchiveRepository provisioningArchiveRepository,
			ConfidentialStorage confidentialStorage, IcConnectorFacade connectorFacade, EntityManager entityManager,
			SysSystemFormValueService systemFormValueService, SysSystemMappingService systemMappingService,
			SysSystemAttributeMappingService systemAttributeMappingService) {
		super(systemRepository, formService);
		//
		Assert.notNull(icConfigurationFacade);
		Assert.notNull(objectClassService);
		Assert.notNull(attributeService);
		Assert.notNull(systemEntityRepository);
		Assert.notNull(accountRepository);
		Assert.notNull(synchronizationConfigService);
		Assert.notNull(formPropertyManager);
		Assert.notNull(provisioningArchiveRepository);
		Assert.notNull(confidentialStorage);
		Assert.notNull(connectorFacade);
		Assert.notNull(entityManager);
		Assert.notNull(systemFormValueService);
		Assert.notNull(systemMappingService);
		Assert.notNull(systemAttributeMappingService);
		//
		this.systemRepository = systemRepository;
		this.icConfigurationFacade = icConfigurationFacade;
		this.objectClassService = objectClassService;
		this.attributeService = attributeService;
		this.systemEntityRepository = systemEntityRepository;
		this.accountRepository = accountRepository;
		this.synchronizationConfigService = synchronizationConfigService;
		this.formPropertyManager = formPropertyManager;
		this.provisioningArchiveRepository = provisioningArchiveRepository;
		this.confidentialStorage = confidentialStorage;
		this.connectorFacade = connectorFacade;
		this.entityManager = entityManager;
		this.systemFormValueService = systemFormValueService;
		this.systemMappingService = systemMappingService;
		this.systemAttributeMappingService = systemAttributeMappingService;
	}

	@Override
	public SysSystem save(SysSystem entity) {
		// create default connector server
		if (entity.getConnectorServer() == null) {
			entity.setConnectorServer(new SysConnectorServer());
		}
		if (entity.getConnectorKey() == null) {
			entity.setConnectorKey(new SysConnectorKey());
		}
		//
		SysSystem newSystem = super.save(entity);
		//
		// after save entity save password to confidential storage
		// save password from remote connector server to confidential storage
		if (entity.getConnectorServer().getPassword() != null) {
			// save for newSystem
			confidentialStorage.save(newSystem.getId(), SysSystem.class, REMOTE_SERVER_PASSWORD,
					entity.getConnectorServer().getPassword().asString());
			//
			// set asterix
			newSystem.getConnectorServer().setPassword(new GuardedString(GuardedString.SECRED_PROXY_STRING));
		}
		return newSystem;
	}

	@Override
	public SysSystem get(Serializable id) {
		SysSystem entity = super.get(id);
		//
		// found if entity has filled password
		Object password = confidentialStorage.get(entity.getId(), SysSystem.class,
				SysSystemService.REMOTE_SERVER_PASSWORD);
		if (password != null && entity.getConnectorServer() != null) {
			entity.getConnectorServer().setPassword(new GuardedString(GuardedString.SECRED_PROXY_STRING));
		}
		//
		return entity;
	}

	@Override
	@Transactional
	public void delete(SysSystem system) {
		Assert.notNull(system);
		//
		// if exists accounts or system entities, then system could not be
		// deleted
		if (systemEntityRepository.countBySystem_Id(system.getId()) > 0) {
			throw new ResultCodeException(AccResultCode.SYSTEM_DELETE_FAILED_HAS_ENTITIES,
					ImmutableMap.of("system", system.getName()));
		}
		if (accountRepository.countBySystem(system) > 0) {
			throw new ResultCodeException(AccResultCode.SYSTEM_DELETE_FAILED_HAS_ACCOUNTS,
					ImmutableMap.of("system", system.getName()));
		}
		// delete synchronization configs
		SynchronizationConfigFilter synchronizationConfigFilter = new SynchronizationConfigFilter();
		synchronizationConfigFilter.setSystemId(system.getId());
		synchronizationConfigService.find(synchronizationConfigFilter, null).forEach(config -> {
			synchronizationConfigService.delete(config);
		});
		
		// delete schema
		SchemaObjectClassFilter filter = new SchemaObjectClassFilter();
		filter.setSystemId(system.getId());
		objectClassService.find(filter, null).forEach(schemaObjectClass -> {
			objectClassService.delete(schemaObjectClass);
		});
		// delete archived provisioning operations
		provisioningArchiveRepository.deleteBySystem(system);
		//
		super.delete(system);
	}

	@Override
	@Transactional(readOnly = true)
	public SysSystem getByCode(String name) {
		return systemRepository.findOneByName(name);
	}

	@Override
	@Transactional
	public IcConnectorConfiguration getConnectorConfiguration(SysSystem system) {
		Assert.notNull(system);

		if (system.getConnectorKey() == null) {
			return null;
		}
		IcConnectorConfiguration connectorConfig = null;
		// load connector properties, different between local and remote
		IcConnectorInstance connectorInstance = system.getConnectorInstance();
		if (connectorInstance.getConnectorServer() != null) {
			connectorInstance.getConnectorServer().setPassword(confidentialStorage.getGuardedString(system.getId(),
					SysSystem.class, SysSystemService.REMOTE_SERVER_PASSWORD));
		}
		connectorConfig = icConfigurationFacade.getConnectorConfiguration(connectorInstance);

		// load filled form values
		IdmFormDefinition formDefinition = getConnectorFormDefinition(system.getConnectorInstance());
		List<AbstractFormValue<SysSystem>> formValues = getFormService().getValues(system, formDefinition);
		Map<String, List<AbstractFormValue<SysSystem>>> attributeValues = getFormService().toValueMap(formValues);
		// fill connector configuration from form values
		IcConnectorConfigurationImpl icConf = new IcConnectorConfigurationImpl();
		IcConfigurationProperties properties = new IcConfigurationPropertiesImpl();
		icConf.setConfigurationProperties(properties);
		//
		for (short seq = 0; seq < connectorConfig.getConfigurationProperties().getProperties().size(); seq++) {
			IcConfigurationProperty propertyConfig = connectorConfig.getConfigurationProperties().getProperties()
					.get(seq);
			IdmFormAttribute formAttribute = formDefinition.getMappedAttributeByName(propertyConfig.getName());
			List<AbstractFormValue<SysSystem>> eavAttributeValues = attributeValues.get(formAttribute.getCode());
			// create property instance from configuration
			IcConfigurationProperty property = formPropertyManager.toConnectorProperty(propertyConfig,
					eavAttributeValues);
			if (property.getValue() != null) {
				// only filled values to configuration
				properties.getProperties().add(property);
			}
		}
		return icConf;
	}

	@Override
	@Transactional
	public void checkSystem(SysSystem system) {
		Assert.notNull(system);

		// Find connector identification persisted in system
		IcConnectorKey connectorKey = system.getConnectorKey();
		if (connectorKey == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_KEY_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Find connector configuration persisted in system
		IcConnectorConfiguration connectorConfig = getConnectorConfiguration(system);
		if (connectorConfig == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Call IC module
		icConfigurationFacade.test(system.getConnectorInstance(), connectorConfig);
	}

	@Override
	@Transactional
	public List<SysSchemaObjectClass> generateSchema(SysSystem system) {
		Assert.notNull(system);

		// Find connector identification persisted in system
		IcConnectorKey connectorKey = system.getConnectorKey();
		if (connectorKey == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_KEY_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Find connector configuration persisted in system
		IcConnectorConfiguration connectorConfig = getConnectorConfiguration(system);
		if (connectorConfig == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Call IC module and find schema for given connector key and
		// configuration
		IcSchema icSchema = icConfigurationFacade.getSchema(system.getConnectorInstance(), connectorConfig);
		if (icSchema == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_SCHEMA_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Load existing object class from system
		SchemaObjectClassFilter objectClassFilter = new SchemaObjectClassFilter();
		objectClassFilter.setSystemId(system.getId());
		List<SysSchemaObjectClass> sysObjectClassesInSystem = null;
		Page<SysSchemaObjectClass> page = objectClassService.find(objectClassFilter, null);
		sysObjectClassesInSystem = page.getContent();

		// Convert IC schema to ACC entities
		List<SysSchemaObjectClass> sysObjectClasses = new ArrayList<SysSchemaObjectClass>();
		List<SysSchemaAttribute> sysAttributes = new ArrayList<SysSchemaAttribute>();
		for (IcObjectClassInfo objectClass : icSchema.getDeclaredObjectClasses()) {

			// We can create only IC schemas, it means only schemas created for
			// __ACCOUNT__ and __GROUP__
			if (!(objectClass.getType().startsWith("__") && objectClass.getType().endsWith("__"))) {
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
			// Convert IC object class to ACC (if is null, then will be created
			// new instance)
			sysObjectClass = convertIcObjectClassInfo(objectClass, sysObjectClass);
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
			for (IcAttributeInfo attribute : objectClass.getAttributeInfos()) {
				// If will be IC and ACC attribute same (same name), then we
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
				sysAttribute = convertIcAttributeInfo(attribute, sysAttribute);
				sysAttribute.setObjectClass(sysObjectClass);
				sysAttributes.add(sysAttribute);
			}
		}

		// Persist generated schema to system
		objectClassService.saveAll(sysObjectClasses);
		attributeService.saveAll(sysAttributes);
		return sysObjectClasses;
	}

	private SysSchemaObjectClass convertIcObjectClassInfo(IcObjectClassInfo objectClass,
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

	private SysSchemaAttribute convertIcAttributeInfo(IcAttributeInfo attributeInfo, SysSchemaAttribute sysAttribute) {
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
	public IdmFormDefinition getConnectorFormDefinition(IcConnectorInstance connectorInstance) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		//
		IdmFormDefinition formDefinition = getFormService().getDefinition(SysSystem.class.getName(),
				connectorInstance.getConnectorKey().getFullName());
		//
		if (formDefinition == null) {
			// we creates new form definition
			formDefinition = createConnectorFormDefinition(connectorInstance);
			formDefinition.setUnmodifiable(true);
		}
		return formDefinition;
	}

	/**
	 * Create form definition to given connectorInstance by connector properties
	 * 
	 * @param connectorKey
	 * @return
	 */
	private synchronized IdmFormDefinition createConnectorFormDefinition(IcConnectorInstance connectorInstance) {
		IcConnectorConfiguration conf = icConfigurationFacade.getConnectorConfiguration(connectorInstance);
		if (conf == null) {
			throw new IllegalStateException(MessageFormat.format("Connector with key [{0}] was not found on classpath.",
					connectorInstance.getConnectorKey().getFullName()));
		}
		//
		List<IdmFormAttribute> formAttributes = new ArrayList<>();
		for (short seq = 0; seq < conf.getConfigurationProperties().getProperties().size(); seq++) {
			IcConfigurationProperty property = conf.getConfigurationProperties().getProperties().get(seq);
			IdmFormAttribute attribute = formPropertyManager.toFormAttribute(property);
			attribute.setSeq(seq);
			formAttributes.add(attribute);
		}
		return getFormService().createDefinition(SysSystem.class.getName(),
				connectorInstance.getConnectorKey().getFullName(), formAttributes);
	}

	@Override
	@Transactional
	public IcConnectorObject readObject(SysSystem system, SysSystemMapping systemMapping, IcUidAttribute uidAttribute) {
		IcObjectClass objectClass = new IcObjectClassImpl(systemMapping.getObjectClass().getObjectClassName());
		return connectorFacade.readObject(system.getConnectorInstance(), this.getConnectorConfiguration(system),
				objectClass, uidAttribute);
	}

	@Override
	@Transactional
	public SysSystem duplicate(UUID id) {
		SysSystem originalSystem = this.get(id);
		Asserts.notNull(originalSystem, "System must be found!");

		// Clone and save system
		SysSystem clone = this.clone(id);
		String name = MessageFormat.format("{0}{1}", "Copy-of-", clone.getName());
		name = this.duplicateName(name, 0);

		clone.setName(name);
		// Set as inactive system
		clone.setDisabled(true);
		SysSystem system = this.save(clone);

		// Cache old and new IDs
		Map<UUID, UUID> schemaAttributesCache = new HashMap<UUID, UUID>();
		Map<UUID, UUID> mappedAttributesCache = new HashMap<UUID, UUID>();

		// Duplicate connector configuration values in EAV
		IcConnectorInstance connectorInstance = originalSystem.getConnectorInstance();
		
		if(connectorInstance != null && connectorInstance.getConnectorKey() != null && connectorInstance.getConnectorKey().getFramework() != null){
			IdmFormDefinition formDefinition = getConnectorFormDefinition(connectorInstance);
			List<AbstractFormValue<SysSystem>> originalFormValues = this.getFormService().getValues(id, SysSystem.class,
					formDefinition);
			originalFormValues.stream().forEach(value -> {
				systemFormValueService.duplicate(value.getId(), system);
			});
		}
		
		// Duplicate schema
		SchemaObjectClassFilter objectClassFilter = new SchemaObjectClassFilter();
		objectClassFilter.setSystemId(id);
		objectClassService.find(objectClassFilter, null).getContent().stream().forEach(schema -> {
			UUID originalSchemaId = schema.getId();
			SysSchemaObjectClass duplicatedSchema = this.duplicateSchema(originalSchemaId, system,
					schemaAttributesCache);

			// Duplicate mapped attributes
			SystemMappingFilter systemMappingFilter = new SystemMappingFilter();
			systemMappingFilter.setSystemId(id);
			systemMappingService.find(systemMappingFilter, null).getContent().stream().filter(mapping -> {
				
				// Find mapping for this schema
				return mapping.getObjectClass().getId().equals(originalSchemaId);
			}).forEach(mapping -> {
				final UUID originalMappingId = mapping.getId();
				SysSystemMapping duplicatedMapping = this.duplicateMapping(originalMappingId, duplicatedSchema,
						schemaAttributesCache, mappedAttributesCache);

				// Duplicate sync configs
				List<SysSyncConfigDto> syncConfigs = findSyncConfigs(id);
				syncConfigs.stream().filter(syncConfig -> {
					
					// Find configuration of sync for this mapping
					return syncConfig.getSystemMapping().equals(originalMappingId);
				}).forEach(syncConfig -> {
					UUID syncConfigId = syncConfig.getId();
					duplicateSyncConf(syncConfigId, duplicatedMapping, mappedAttributesCache);
				});
			});
		});

		return system;
	}

	@Override
	public SysSystem clone(UUID id) {
		SysSystem originalSystem = this.get(id);
		Asserts.notNull(originalSystem, "System must be found!");

		// We do detach this entity (and set id to null)
		entityManager.detach(originalSystem);
		originalSystem.setId(null);
		EntityUtils.clearAuditFields(originalSystem);
		return originalSystem;
	}

	/**
	 * Duplication of schema attributes. Is not in attribute schema service, because we need use IDs cache (Old vs New IDs)
	 * @param id
	 * @param system
	 * @param schemaAttributesIds
	 * @return
	 */
	private SysSchemaObjectClass duplicateSchema(UUID id, SysSystem system, Map<UUID, UUID> schemaAttributesIds) {
		Assert.notNull(id, "Id of duplication schema, must be filled!");
		Assert.notNull(system, "Parent system must be filled!");
		SysSchemaObjectClass clonedSchema = objectClassService.clone(id);
		clonedSchema.setSystem(system);
		SysSchemaObjectClass schema = objectClassService.save(clonedSchema);

		SchemaAttributeFilter schemaAttributesFilter = new SchemaAttributeFilter();
		schemaAttributesFilter.setObjectClassId(id);
		attributeService.find(schemaAttributesFilter, null).forEach(schemaAttribute -> {
			UUID originalSchemaAttributId = schemaAttribute.getId();
			SysSchemaAttribute clonedAttribut = attributeService.clone(originalSchemaAttributId);
			clonedAttribut.setObjectClass(schema);
			clonedAttribut = attributeService.save(clonedAttribut);
			// Put original and new id to cache
			schemaAttributesIds.put(originalSchemaAttributId, clonedAttribut.getId());
		});

		return schema;
	}

	/**
	 * Duplication of mapping attributes. Is not in attribute mapping service, because we need use IDs cache (Old vs New IDs)
	 * @param id
	 * @param schema
	 * @param schemaAttributesIds
	 * @param mappedAttributesIds
	 * @return
	 */
	private SysSystemMapping duplicateMapping(UUID id, SysSchemaObjectClass schema, Map<UUID, UUID> schemaAttributesIds,
			Map<UUID, UUID> mappedAttributesIds) {
		Assert.notNull(id, "Id of duplication mapping, must be filled!");
		Assert.notNull(schema, "Parent schema must be filled!");
		SysSystemMapping clonedMapping = systemMappingService.clone(id);
		clonedMapping.setObjectClass(schema);
		SysSystemMapping mapping = this.systemMappingService.save(clonedMapping);

		// Clone mapped attributes
		SystemAttributeMappingFilter attributesFilter = new SystemAttributeMappingFilter();
		attributesFilter.setSystemMappingId(id);
		systemAttributeMappingService.find(attributesFilter, null).forEach(attribute -> {
			UUID originalAttributeId = attribute.getId();
			SysSystemAttributeMapping clonedAttribute = systemAttributeMappingService.clone(originalAttributeId);
			// Find cloned schema attribute in cache (by original Id)
			SysSchemaAttribute clonedSchemaAttribute = attributeService
					.get(schemaAttributesIds.get(clonedAttribute.getSchemaAttribute().getId()));

			clonedAttribute.setSystemMapping(mapping);
			clonedAttribute.setSchemaAttribute(clonedSchemaAttribute);
			clonedAttribute = systemAttributeMappingService.save(clonedAttribute);
			// Put original and new id to cache
			mappedAttributesIds.put(originalAttributeId, clonedAttribute.getId());
		});

		return mapping;
	}
	
	/**
	 * Duplication of sync configuration. Is not in sync service, because we need use IDs cache (Old vs New IDs)
	 * @param syncConfigId
	 * @param duplicatedMapping
	 * @param mappedAttributesCache
	 */
	private void duplicateSyncConf(UUID syncConfigId, SysSystemMapping duplicatedMapping,
			Map<UUID, UUID> mappedAttributesCache) {
		SysSyncConfigDto clonedSyncConfig = synchronizationConfigService.clone(syncConfigId);
		clonedSyncConfig.setSystemMapping(duplicatedMapping.getId());
		//
		clonedSyncConfig.setFilterAttribute(this.getNewAttributeByOld(
				systemAttributeMappingService.get(clonedSyncConfig.getFilterAttribute()), mappedAttributesCache).getId());
		//
		clonedSyncConfig.setCorrelationAttribute(
				this.getNewAttributeByOld(systemAttributeMappingService.get(clonedSyncConfig.getCorrelationAttribute()),
						mappedAttributesCache).getId());
		//
		clonedSyncConfig.setTokenAttribute(
				this.getNewAttributeByOld(systemAttributeMappingService.get(clonedSyncConfig.getTokenAttribute()),
						mappedAttributesCache).getId());
		//
		// Disabled cloned sync
		clonedSyncConfig.setEnabled(false);
		synchronizationConfigService.save(clonedSyncConfig);
	}
	
	/**
	 * Find new mapped attribute by old mapped attribute (uses cache ids)
	 * @param oldAttribute
	 * @param mappedAttributesCache
	 * @return
	 */
	private SysSystemAttributeMapping getNewAttributeByOld(SysSystemAttributeMapping oldAttribute, Map<UUID, UUID> mappedAttributesCache) {
		if(oldAttribute == null){
			return null;
		}
		UUID newAttributeId = mappedAttributesCache.get(oldAttribute.getId());
		return systemAttributeMappingService.get(newAttributeId);
	}

	/**
	 * Find sync configs for given system ID and do detach.
	 * @param id
	 * @return
	 */
	private List<SysSyncConfigDto> findSyncConfigs(UUID id) {
		SynchronizationConfigFilter syncConfigFilter = new SynchronizationConfigFilter();
		syncConfigFilter.setSystemId(id);
		List<SysSyncConfigDto> syncConfigs = synchronizationConfigService.find(syncConfigFilter, null).getContent();
		syncConfigs.forEach(syncConfig -> {
			// I have to do detach for all sync configurations
			entityManager.detach(syncConfig);
		});
		return syncConfigs;
	}

	/**
	 * Create new system name for duplicate
	 * 
	 * @param name
	 * @param i
	 * 
	 * @return
	 */
	private String duplicateName(String name, int i) {
		SysSystemFilter filter = new SysSystemFilter();
		if (i > 0) {
			filter.setText(MessageFormat.format("{0}{1}", name, i));
		} else {
			filter.setText(name);
		}
		if (!this.find(filter, null).hasContent()) {
			return filter.getText();
		}
		return duplicateName(name, i + 1);

	}

	@Deprecated
	@Transactional
	public SysSystem createTestSystem() {
		// create owner
		SysSystem system = new SysSystem();
		system.setName("sysOne_" + System.currentTimeMillis());
		system.setConnectorKey(new SysConnectorKey(getTestConnectorKey()));
		save(system);

		IdmFormDefinition savedFormDefinition = getConnectorFormDefinition(system.getConnectorInstance());

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
	public IcConnectorKey getTestConnectorKey() {
		IcConnectorKeyImpl key = new IcConnectorKeyImpl();
		key.setFramework("connId");
		key.setConnectorName("net.tirasa.connid.bundles.db.table.DatabaseTableConnector");
		key.setBundleName("net.tirasa.connid.bundles.db.table");
		key.setBundleVersion("2.2.4");
		return key;
	}

}
