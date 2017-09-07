package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.SysConnectorKeyDto;
import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
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
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
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
import eu.bcvsolutions.idm.ic.czechidm.domain.IcConnectorConfigurationCzechIdMImpl;
import eu.bcvsolutions.idm.ic.impl.IcConfigurationPropertiesImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorConfigurationImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorKeyImpl;
import eu.bcvsolutions.idm.ic.impl.IcUidAttributeImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationFacade;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Default target system configuration service
 * 
 * @author Radek Tomiška
 *
 */
@Service

public class DefaultSysSystemService extends AbstractReadWriteDtoService<SysSystemDto, SysSystem, SysSystemFilter>

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
	private final SysSystemFormValueService systemFormValueService;
	private final SysSystemMappingService systemMappingService;
	private final SysSystemAttributeMappingService systemAttributeMappingService;
	private final SysSchemaObjectClassService schemaObjectClassService;
	private final FormService formService;

	@Autowired
	public DefaultSysSystemService(SysSystemRepository systemRepository, FormService formService,
			IcConfigurationFacade icConfigurationFacade, SysSchemaObjectClassService objectClassService,
			SysSchemaAttributeService attributeService, SysSystemEntityRepository systemEntityRepository,
			AccAccountRepository accountRepository, SysSyncConfigService synchronizationConfigService,
			FormPropertyManager formPropertyManager, SysProvisioningArchiveRepository provisioningArchiveRepository,
			ConfidentialStorage confidentialStorage, IcConnectorFacade connectorFacade,
			SysSystemFormValueService systemFormValueService, SysSystemMappingService systemMappingService,
			SysSystemAttributeMappingService systemAttributeMappingService,
			SysSchemaObjectClassService schemaObjectClassService) {
		super(systemRepository);
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
		Assert.notNull(systemFormValueService);
		Assert.notNull(systemMappingService);
		Assert.notNull(systemAttributeMappingService);
		Assert.notNull(schemaObjectClassService);
		Assert.notNull(formService);
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
		this.systemFormValueService = systemFormValueService;
		this.systemMappingService = systemMappingService;
		this.systemAttributeMappingService = systemAttributeMappingService;
		this.schemaObjectClassService = schemaObjectClassService;
		this.formService = formService;
	}

	@Override
	public SysSystemDto save(SysSystemDto dto, BasePermission... permission) {
		// create default connector server
		if (dto.getConnectorServer() == null) {
			dto.setConnectorServer(new SysConnectorServerDto());
		}
		if (dto.getConnectorKey() == null) {
			dto.setConnectorKey(new SysConnectorKeyDto());
		}
		//
		SysSystemDto newSystem = super.save(dto, permission);
		//
		// after save entity save password to confidential storage
		// save password from remote connector server to confidential storage
		if (dto.getConnectorServer().getPassword() != null) {
			// save for newSystem
			confidentialStorage.save(newSystem.getId(), SysSystem.class, REMOTE_SERVER_PASSWORD,
					dto.getConnectorServer().getPassword().asString());
			//
			// set asterix
			newSystem.getConnectorServer().setPassword(new GuardedString(GuardedString.SECRED_PROXY_STRING));
		}
		return newSystem;
	}

	@Override
	public SysSystemDto get(Serializable id, BasePermission... permission) {
		SysSystemDto entity = super.get(id, permission);
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
	public void delete(SysSystemDto system, BasePermission... permission) {
		Assert.notNull(system);
		//
		// if exists accounts or system entities, then system could not be
		// deleted
		if (systemEntityRepository.countBySystem_Id(system.getId()) > 0) {
			throw new ResultCodeException(AccResultCode.SYSTEM_DELETE_FAILED_HAS_ENTITIES,
					ImmutableMap.of("system", system.getName()));
		}
		SysSystem sytemEntity = getEntity(system.getId());
		if (accountRepository.countBySystem(sytemEntity) > 0) {
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
		provisioningArchiveRepository.deleteBySystem(sytemEntity);
		//
		formService.deleteValues(system);
		//
		super.delete(system, permission);

	}

	@Override
	@Transactional(readOnly = true)
	public SysSystemDto getByCode(String name) {
		return toDto(systemRepository.findOneByName(name));
	}

	@Override
	@Transactional
	public IcConnectorConfiguration getConnectorConfiguration(SysSystemDto system) {
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
		IdmFormDefinitionDto formDefinition = getConnectorFormDefinition(system.getConnectorInstance());
		IdmFormInstanceDto formValues = getFormService().getFormInstance(system, formDefinition);
		Map<String, List<IdmFormValueDto>> attributeValues = formValues.toValueMap();

		// fill connector configuration from form values
		IcConnectorConfigurationImpl icConf = null;
		if(SysSystemService.CONNECTOR_FRAMEWORK_CZECHIDM.equals(connectorInstance.getConnectorKey().getFramework())){
			// For CzechIdM connector framework is needs system ID (exactly for virtual systems).
			 icConf = new IcConnectorConfigurationCzechIdMImpl();
			 ((IcConnectorConfigurationCzechIdMImpl)icConf).setSystemId(system.getId());
		}else {
			 icConf = new IcConnectorConfigurationImpl();
		}
		IcConfigurationProperties properties = new IcConfigurationPropertiesImpl();
		icConf.setConfigurationProperties(properties);
		//
		for (short seq = 0; seq < connectorConfig.getConfigurationProperties().getProperties().size(); seq++) {
			IcConfigurationProperty propertyConfig = connectorConfig.getConfigurationProperties().getProperties()
					.get(seq);

			IdmFormAttributeDto formAttribute = formDefinition.getMappedAttributeByCode(propertyConfig.getName());
			List<IdmFormValueDto> eavAttributeValues = attributeValues.get(formAttribute.getCode());

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
	public IcConnectorObject readConnectorObject(UUID systemId, String uid, IcObjectClass objectClass){
		Assert.notNull(systemId, "System ID cannot be null!");
		Assert.notNull(uid, "Account UID cannot be null!");
		Assert.notNull(objectClass, "Object class cannot be null!");
		
		SysSystemDto system = this.get(systemId);
		Assert.notNull(system, "System cannot be null!");
		
		return connectorFacade.readObject(system.getConnectorInstance(), this.getConnectorConfiguration(system),
				objectClass, new IcUidAttributeImpl(null, uid, null));
		
	}
	

	@Override
	@Transactional
	public void checkSystem(SysSystemDto system) {
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
	public List<SysSchemaObjectClassDto> generateSchema(SysSystemDto system) {
		Assert.notNull(system);
		Assert.notNull(system.getId());
		
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
		List<SysSchemaObjectClassDto> sysObjectClassesInSystem = null;
		Page<SysSchemaObjectClassDto> page = objectClassService.find(objectClassFilter, null);
		sysObjectClassesInSystem = page.getContent();

		// Convert IC schema to ACC entities
		List<SysSchemaObjectClassDto> sysObjectClasses = new ArrayList<SysSchemaObjectClassDto>();
		List<SysSchemaAttributeDto> sysAttributes = new ArrayList<SysSchemaAttributeDto>();
		for (IcObjectClassInfo objectClass : icSchema.getDeclaredObjectClasses()) {

			// We can create only IC schemas, it means only schemas created for
			// __ACCOUNT__ and __GROUP__
			if (!(objectClass.getType().startsWith("__") && objectClass.getType().endsWith("__"))) {
				continue;
			}
			SysSchemaObjectClassDto sysObjectClass = null;
			// If existed some object class in system, then we will compared
			// every object with object class in resource
			// If will be same (same name), then we do only refresh object
			// values from resource
			if (sysObjectClassesInSystem != null) {
				Optional<SysSchemaObjectClassDto> objectClassSame = sysObjectClassesInSystem.stream()
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
			sysObjectClass.setSystem(system.getId());
			
			// object class may not exist 
			sysObjectClass = schemaObjectClassService.save(sysObjectClass);
			
			sysObjectClasses.add(sysObjectClass);

			List<SysSchemaAttributeDto> attributesInSystem = null;
			// Load existing attributes for existing object class in system
			if (sysObjectClass.getId() != null) {
				SchemaAttributeFilter attFilter = new SchemaAttributeFilter();
				attFilter.setSystemId(system.getId());
				attFilter.setObjectClassId(sysObjectClass.getId());

				Page<SysSchemaAttributeDto> attributesInSystemPage = attributeService.find(attFilter, null);
				attributesInSystem = attributesInSystemPage.getContent();
			}
			for (IcAttributeInfo attribute : objectClass.getAttributeInfos()) {
				// If will be IC and ACC attribute same (same name), then we
				// will do only refresh object values from resource
				SysSchemaAttributeDto sysAttribute = null;
				if (attributesInSystem != null) {
					Optional<SysSchemaAttributeDto> sysAttributeOptional = attributesInSystem.stream().filter(a -> {
						return a.getName().equals(attribute.getName());
					}).findFirst();
					if (sysAttributeOptional.isPresent()) {
						sysAttribute = sysAttributeOptional.get();
					}
				}
				sysAttribute = convertIcAttributeInfo(attribute, sysAttribute);
				sysAttribute.setObjectClass(sysObjectClass.getId());
				sysAttributes.add(sysAttribute);
			}
		}

		// Persist generated schema to system
		sysObjectClasses = (List<SysSchemaObjectClassDto>) objectClassService.saveAll(sysObjectClasses);
		attributeService.saveAll(sysAttributes);
		return sysObjectClasses;
	}

	@Override
	@Transactional
	public IdmFormDefinitionDto getConnectorFormDefinition(IcConnectorInstance connectorInstance) {
		Assert.notNull(connectorInstance);
		Assert.notNull(connectorInstance.getConnectorKey());
		//
		IdmFormDefinitionDto formDefinition = getFormService().getDefinition(SysSystem.class.getName(),
				connectorInstance.getConnectorKey().getFullName());
		//
		if (formDefinition == null) {
			// we creates new form definition
			formDefinition = createConnectorFormDefinition(connectorInstance);
			formDefinition.setUnmodifiable(true);
		}
		return formDefinition;
	}

	@Override
	@Transactional
	public SysSystemDto duplicate(UUID id) {
		SysSystemDto originalSystem = this.get(id);
		Asserts.notNull(originalSystem, "System must be found!");

		// Clone and save system
		SysSystemDto clone = this.clone(id);
		String name = MessageFormat.format("{0}{1}", "Copy-of-", clone.getName());
		name = this.duplicateName(name, 0);

		clone.setName(name);
		// Set as inactive system
		clone.setDisabled(true);
		SysSystemDto system = this.save(clone);

		// Cache old and new IDs
		Map<UUID, UUID> schemaAttributesCache = new HashMap<UUID, UUID>();
		Map<UUID, UUID> mappedAttributesCache = new HashMap<UUID, UUID>();

		// Duplicate connector configuration values in EAV
		IcConnectorInstance connectorInstance = originalSystem.getConnectorInstance();
		
		if(connectorInstance != null && connectorInstance.getConnectorKey() != null && connectorInstance.getConnectorKey().getFramework() != null){
			IdmFormDefinitionDto formDefinition = getConnectorFormDefinition(connectorInstance);
			List<IdmFormValueDto> originalFormValues = this.getFormService().getValues(id, SysSystem.class,
					formDefinition);
			SysSystem systemEntity = getEntity(system.getId());
			originalFormValues.stream().forEach(value -> {
				systemFormValueService.duplicate(value.getId(), systemEntity);
			});
		}
		
		// Duplicate schema
		SchemaObjectClassFilter objectClassFilter = new SchemaObjectClassFilter();
		objectClassFilter.setSystemId(id);
		objectClassService.find(objectClassFilter, null).getContent().stream().forEach(schema -> {
			UUID originalSchemaId = schema.getId();
			SysSchemaObjectClassDto duplicatedSchema = this.duplicateSchema(originalSchemaId, system,
					schemaAttributesCache);

			// Duplicate mapped attributes
			SystemMappingFilter systemMappingFilter = new SystemMappingFilter();
			systemMappingFilter.setSystemId(id);
			systemMappingService.find(systemMappingFilter, null).getContent().stream().filter(mapping -> {
				
				// Find mapping for this schema
				return mapping.getObjectClass().equals(originalSchemaId);
			}).forEach(mapping -> {
				final UUID originalMappingId = mapping.getId();
				SysSystemMappingDto duplicatedMapping = this.duplicateMapping(originalMappingId, duplicatedSchema,
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
	public SysSystemDto clone(UUID id) {
		SysSystemDto originalSystem = this.get(id);
		Asserts.notNull(originalSystem, "System must be found!");

		originalSystem.setId(null);
		EntityUtils.clearAuditFields(originalSystem);
		return originalSystem;
	}
	
	private SysSchemaObjectClassDto convertIcObjectClassInfo(IcObjectClassInfo objectClass,
			SysSchemaObjectClassDto sysObjectClass) {
		if (objectClass == null) {
			return null;
		}
		if (sysObjectClass == null) {
			sysObjectClass = new SysSchemaObjectClassDto();
		}
		sysObjectClass.setObjectClassName(objectClass.getType());
		sysObjectClass.setAuxiliary(objectClass.isAuxiliary());
		sysObjectClass.setContainer(objectClass.isContainer());
		return sysObjectClass;
	}

	private SysSchemaAttributeDto convertIcAttributeInfo(IcAttributeInfo attributeInfo, SysSchemaAttributeDto sysAttribute) {
		if (attributeInfo == null) {
			return null;
		}
		if (sysAttribute == null) {
			sysAttribute = new SysSchemaAttributeDto();
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
	
	/**
	 * Create form definition to given connectorInstance by connector properties
	 * 
	 * @param connectorKey
	 * @return
	 */
	private synchronized IdmFormDefinitionDto createConnectorFormDefinition(IcConnectorInstance connectorInstance) {
		IcConnectorConfiguration conf = icConfigurationFacade.getConnectorConfiguration(connectorInstance);
		if (conf == null) {
			throw new IllegalStateException(MessageFormat.format("Connector with key [{0}] was not found on classpath.",
					connectorInstance.getConnectorKey().getFullName()));
		}
		//
		List<IdmFormAttributeDto> formAttributes = new ArrayList<>();
		for (short seq = 0; seq < conf.getConfigurationProperties().getProperties().size(); seq++) {
			IcConfigurationProperty property = conf.getConfigurationProperties().getProperties().get(seq);
			IdmFormAttributeDto attribute = formPropertyManager.toFormAttribute(property);
			attribute.setSeq(seq);
			formAttributes.add(attribute);
		}
		return getFormService().createDefinition(SysSystem.class.getName(),
				connectorInstance.getConnectorKey().getFullName(), formAttributes);
	}

	/**
	 * Duplication of schema attributes. Is not in attribute schema service, because we need use IDs cache (Old vs New IDs)
	 * @param id
	 * @param system
	 * @param schemaAttributesIds
	 * @return
	 */
	private SysSchemaObjectClassDto duplicateSchema(UUID id, SysSystemDto system, Map<UUID, UUID> schemaAttributesIds) {
		Assert.notNull(id, "Id of duplication schema, must be filled!");
		Assert.notNull(system, "Parent system must be filled!");
		SysSchemaObjectClassDto clonedSchema = objectClassService.clone(id);
		clonedSchema.setSystem(system.getId());
		SysSchemaObjectClassDto schema = objectClassService.save(clonedSchema);

		SchemaAttributeFilter schemaAttributesFilter = new SchemaAttributeFilter();
		schemaAttributesFilter.setObjectClassId(id);
		attributeService.find(schemaAttributesFilter, null).forEach(schemaAttribute -> {
			UUID originalSchemaAttributId = schemaAttribute.getId();
			SysSchemaAttributeDto clonedAttribut = attributeService.clone(originalSchemaAttributId);
			clonedAttribut.setObjectClass(schema.getId());
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
	private SysSystemMappingDto duplicateMapping(UUID id, SysSchemaObjectClassDto schema, Map<UUID, UUID> schemaAttributesIds,
			Map<UUID, UUID> mappedAttributesIds) {
		Assert.notNull(id, "Id of duplication mapping, must be filled!");
		Assert.notNull(schema, "Parent schema must be filled!");
		SysSystemMappingDto clonedMapping = systemMappingService.clone(id);
		clonedMapping.setObjectClass(schema.getId());
		SysSystemMappingDto mapping = this.systemMappingService.save(clonedMapping);

		// Clone mapped attributes
		SystemAttributeMappingFilter attributesFilter = new SystemAttributeMappingFilter();
		attributesFilter.setSystemMappingId(id);
		systemAttributeMappingService.find(attributesFilter, null).forEach(attribute -> {
			UUID originalAttributeId = attribute.getId();
			SysSystemAttributeMappingDto clonedAttribute = systemAttributeMappingService.clone(originalAttributeId);
			// Find cloned schema attribute in cache (by original Id)
			SysSchemaAttributeDto clonedSchemaAttribute = attributeService
					.get(schemaAttributesIds.get(clonedAttribute.getSchemaAttribute()));

			clonedAttribute.setSystemMapping(mapping.getId());
			clonedAttribute.setSchemaAttribute(clonedSchemaAttribute.getId());
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
	private void duplicateSyncConf(UUID syncConfigId, SysSystemMappingDto duplicatedMapping,
			Map<UUID, UUID> mappedAttributesCache) {
		SysSyncConfigDto clonedSyncConfig = synchronizationConfigService.clone(syncConfigId);
		clonedSyncConfig.setSystemMapping(duplicatedMapping.getId());
		//
		if (clonedSyncConfig.getFilterAttribute() != null) {
			clonedSyncConfig.setFilterAttribute(this.getNewAttributeByOld(
					systemAttributeMappingService.get(clonedSyncConfig.getFilterAttribute()), mappedAttributesCache).getId());
		}
		//
		if (clonedSyncConfig.getCorrelationAttribute() != null) {
			clonedSyncConfig.setCorrelationAttribute(
					this.getNewAttributeByOld(systemAttributeMappingService.get(clonedSyncConfig.getCorrelationAttribute()),
							mappedAttributesCache).getId());
		}
		//
		if (clonedSyncConfig.getTokenAttribute() != null) {
			clonedSyncConfig.setTokenAttribute(
					this.getNewAttributeByOld(systemAttributeMappingService.get(clonedSyncConfig.getTokenAttribute()),
							mappedAttributesCache).getId());
		}
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
	private SysSystemAttributeMappingDto getNewAttributeByOld(SysSystemAttributeMappingDto oldAttribute, Map<UUID, UUID> mappedAttributesCache) {
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
		return synchronizationConfigService.find(syncConfigFilter, null).getContent();
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
	public SysSystemDto createTestSystem() {
		// create owner
		SysSystemDto system = new SysSystemDto();
		system.setName("sysOne_" + System.currentTimeMillis());
		system.setConnectorKey(new SysConnectorKeyDto(getTestConnectorKey()));
		system = save(system);

		IdmFormDefinitionDto savedFormDefinition = getConnectorFormDefinition(system.getConnectorInstance());

		List<IdmFormValueDto> values = new ArrayList<>();
		IdmFormValueDto host = new IdmFormValueDto(savedFormDefinition.getMappedAttributeByCode("host"));
		host.setValue("localhost");
		values.add(host);
		IdmFormValueDto port = new IdmFormValueDto(savedFormDefinition.getMappedAttributeByCode("port"));
		port.setValue("5432");
		values.add(port);
		IdmFormValueDto user = new IdmFormValueDto(savedFormDefinition.getMappedAttributeByCode("user"));
		user.setValue("idmadmin");
		values.add(user);
		IdmFormValueDto password = new IdmFormValueDto(savedFormDefinition.getMappedAttributeByCode("password"));
		password.setValue("idmadmin");
		values.add(password);
		IdmFormValueDto database = new IdmFormValueDto(savedFormDefinition.getMappedAttributeByCode("database"));
		database.setValue("bcv_idm_storage");
		values.add(database);
		IdmFormValueDto table = new IdmFormValueDto(savedFormDefinition.getMappedAttributeByCode("table"));
		table.setValue("system_users");
		values.add(table);
		IdmFormValueDto keyColumn = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("keyColumn"));
		keyColumn.setValue("name");
		values.add(keyColumn);
		IdmFormValueDto passwordColumn = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("passwordColumn"));
		passwordColumn.setValue("password");
		values.add(passwordColumn);
		IdmFormValueDto allNative = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("allNative"));
		allNative.setValue(Boolean.TRUE);
		values.add(allNative);
		IdmFormValueDto jdbcDriver = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("jdbcDriver"));
		jdbcDriver.setValue("org.postgresql.Driver");
		values.add(jdbcDriver);
		IdmFormValueDto jdbcUrlTemplate = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("jdbcUrlTemplate"));
		jdbcUrlTemplate.setValue("jdbc:postgresql://%h:%p/%d");
		values.add(jdbcUrlTemplate);
		IdmFormValueDto rethrowAllSQLExceptions = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("rethrowAllSQLExceptions"));
		rethrowAllSQLExceptions.setValue(Boolean.TRUE);
		values.add(rethrowAllSQLExceptions);
		IdmFormValueDto statusColumn = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("statusColumn"));
		statusColumn.setValue("status");
		values.add(statusColumn);
		IdmFormValueDto disabledStatusValue = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("disabledStatusValue"));
		disabledStatusValue.setValue("disabled");
		values.add(disabledStatusValue);
		IdmFormValueDto enabledStatusValue = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("enabledStatusValue"));
		enabledStatusValue.setValue("enabled");
		values.add(enabledStatusValue);

		// TODO: eav to DTO
		getFormService().saveValues(getEntity(system.getId()), savedFormDefinition, values);

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


	protected FormService getFormService() {
		return formService;
	}
}
