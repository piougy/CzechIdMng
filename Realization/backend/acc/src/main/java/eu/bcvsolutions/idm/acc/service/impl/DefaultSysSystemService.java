package eu.bcvsolutions.idm.acc.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.SchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.repository.SysSchemaAttributeRepository;
import eu.bcvsolutions.idm.acc.repository.SysSchemaObjectClassRepository;
import eu.bcvsolutions.idm.acc.repository.SysSystemRepository;
import eu.bcvsolutions.idm.acc.service.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.QuickFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.icf.api.IcfAttributeInfo;
import eu.bcvsolutions.idm.icf.api.IcfConfigurationProperties;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.api.IcfObjectClassInfo;
import eu.bcvsolutions.idm.icf.api.IcfSchema;
import eu.bcvsolutions.idm.icf.dto.IcfConfigurationPropertiesDto;
import eu.bcvsolutions.idm.icf.dto.IcfConfigurationPropertyDto;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorConfigurationDto;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorInfoDto;
import eu.bcvsolutions.idm.icf.dto.IcfConnectorKeyDto;
import eu.bcvsolutions.idm.icf.service.impl.DefaultIcfConfigurationFacade;

/**
 * Deafult target system configuration service
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysSystemService extends AbstractReadWriteEntityService<SysSystem, QuickFilter>
		implements SysSystemService {

	private SysSystemRepository systemRepository;
	private DefaultIcfConfigurationFacade icfConfigurationAggregatorService;
	private SysSchemaObjectClassRepository objectClassRepository;
	private SysSchemaAttributeRepository attributeRepository;

	@Autowired
	public DefaultSysSystemService(SysSystemRepository systemRepository,
			DefaultIcfConfigurationFacade icfConfigurationAggregatorService,
			SysSchemaObjectClassRepository objectClassRepository, SysSchemaAttributeRepository attributeRepository) {
		super();
		this.systemRepository = systemRepository;
		this.icfConfigurationAggregatorService = icfConfigurationAggregatorService;
		this.objectClassRepository = objectClassRepository;
		this.attributeRepository = attributeRepository;
	}

	@Override
	protected BaseRepository<SysSystem, QuickFilter> getRepository() {
		return systemRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public SysSystem getByName(String name) {
		return systemRepository.findOneByName(name);
	}

	@Override
	public IcfConnectorInfo getConnectorInfo(SysSystem system) {
		// TODO Mockup connector info
		IcfConnectorInfoDto info = new IcfConnectorInfoDto();
		IcfConnectorKeyDto key = new IcfConnectorKeyDto();
		key.setConnectorName("net.tirasa.connid.bundles.db.table.DatabaseTableConnector");
		key.setBundleVersion("2.2.4");
		key.setIcfType("connId");
		key.setBundleName("net.tirasa.connid.bundles.db.table");
		info.setConnectorKey(key);
		return info;
	}

	@Override
	public IcfConnectorConfiguration getConnectorConfiguration(SysSystem system) {
		// TODO Mockup connector configuration
		IcfConnectorConfigurationDto icfConf = new IcfConnectorConfigurationDto();
		IcfConfigurationProperties properties = new IcfConfigurationPropertiesDto();
		icfConf.setConfigurationProperties(properties);
		// Set all of the ConfigurationProperties needed by the connector.
		properties.getProperties().add(new IcfConfigurationPropertyDto("host", "localhost"));
		properties.getProperties().add(new IcfConfigurationPropertyDto("port", "5432"));
		properties.getProperties().add(new IcfConfigurationPropertyDto("user", "idmadmin"));
		properties.getProperties().add(new IcfConfigurationPropertyDto("password",
				new org.identityconnectors.common.security.GuardedString("idmadmin".toCharArray())));
		properties.getProperties().add(new IcfConfigurationPropertyDto("database", "bcv_idm_storage"));
		properties.getProperties().add(new IcfConfigurationPropertyDto("table", "system_users"));
		properties.getProperties().add(new IcfConfigurationPropertyDto("keyColumn", "name"));
		properties.getProperties().add(new IcfConfigurationPropertyDto("passwordColumn", "password"));
		properties.getProperties().add(new IcfConfigurationPropertyDto("allNative", true));
		properties.getProperties().add(new IcfConfigurationPropertyDto("jdbcDriver", "org.postgresql.Driver"));
		properties.getProperties()
				.add(new IcfConfigurationPropertyDto("jdbcUrlTemplate", "jdbc:postgresql://%h:%p/%d"));
		properties.getProperties().add(new IcfConfigurationPropertyDto("rethrowAllSQLExceptions", true));

		return icfConf;

	}

	@Override
	public void generateSchema(SysSystem system) {
		Assert.notNull(system);

		// Find connector identification persist in system
		IcfConnectorInfo connectorInfo = getConnectorInfo(system);
		if (connectorInfo == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_INFO_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Find connector configuration persist in system
		IcfConnectorConfiguration connectorConfig = getConnectorConfiguration(system);
		if (connectorConfig == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Call ICF module and find schema for given connector key and
		// configuration
		IcfSchema icfSchema = icfConfigurationAggregatorService.getSchema(connectorInfo.getConnectorKey(),
				connectorConfig);
		if (icfSchema == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_SCHEMA_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Load existing object class from system
		SchemaObjectClassFilter objectClassFilter = new SchemaObjectClassFilter();
		objectClassFilter.setSystemId(system.getId());
		List<SysSchemaObjectClass> sysObjectClassesInSystem = null;
		Page<SysSchemaObjectClass> page = objectClassRepository.find(objectClassFilter, null);
		sysObjectClassesInSystem = page.getContent();

		// Convert ICF schema to ACC entities
		List<SysSchemaObjectClass> sysObjectClasses = new ArrayList<SysSchemaObjectClass>();
		List<SysSchemaAttribute> sysAttributes = new ArrayList<SysSchemaAttribute>();
		for (IcfObjectClassInfo objectClass : icfSchema.getDeclaredObjectClasses()) {
			SysSchemaObjectClass sysObjectClass = null;
			// If existed some object class in system, then we will compared every object with object class in resource
			// If will be same (same name), then we do only refresh object values from resource 
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
			// Convert ICF object class to ACC (if is null, then will be created new instance)
			sysObjectClass = convertIcfObjectClassInfo(objectClass, sysObjectClass);
			sysObjectClass.setSystem(system);
			sysObjectClasses.add(sysObjectClass);

			List<SysSchemaAttribute> attributesInSystem = null;
			// Load existing attributes for existing object class in system
			if (sysObjectClass.getId() != null) {
				SchemaAttributeFilter attFilter = new SchemaAttributeFilter();
				attFilter.setSystemId(system.getId());
				attFilter.setObjectClassId(sysObjectClass.getId());

				Page<SysSchemaAttribute> attributesInSystemPage = attributeRepository.find(attFilter, null);
				attributesInSystem = attributesInSystemPage.getContent();
			}
			for (IcfAttributeInfo attribute : objectClass.getAttributeInfos()) {
				// If will be ICF and ACC attribute same (same name), then we will do only refresh object values from resource 
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
		sysObjectClasses = (List<SysSchemaObjectClass>) objectClassRepository.save(sysObjectClasses);
		sysAttributes = (List<SysSchemaAttribute>) attributeRepository.save(sysAttributes);
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
}
