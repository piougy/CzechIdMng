package eu.bcvsolutions.idm.acc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.SysConnectorKey;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Acc / Provisioning test helper
 * 
 * @author Radek Tomiška
 */
@Component
public class DefaultTestHelper implements TestHelper {
	
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private SysSystemService systemService;
	@Autowired private SysSystemMappingService systemMappingService;
	@Autowired private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired private SysSchemaAttributeService schemaAttributeService;
	@Autowired private EntityManager entityManager;
	@Autowired private SysRoleSystemService roleSystemService;
	@Autowired private FormService formService;
	@Autowired private DataSource dataSource;

	@Override
	public IdmIdentityDto createIdentity() {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername("test" + "-" + UUID.randomUUID());
		identity.setFirstName("Test");
		identity.setLastName("Identity");
		identity.setPassword(new GuardedString("password"));
		return identityService.save(identity);
	}
	
	@Override
	public IdmRoleDto createRole() {
		IdmRoleDto role = new IdmRoleDto();
		role.setName("test" + "-" + UUID.randomUUID());
		return roleService.save(role);
	}
	
	@Override
	public IdmIdentityRoleDto createIdentityRole(IdmIdentityDto identity, IdmRoleDto role) {
		return createIdentityRole(identityContractService.getPrimeContract(identity.getId()), role);
	}
	
	private IdmIdentityRoleDto createIdentityRole(IdmIdentityContractDto identityContract, IdmRoleDto role) {
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		identityRole.setIdentityContract(identityContract.getId());
		identityRole.setRole(role.getId());
		return identityRoleService.save(identityRole);
	}
	
	/**
	 * Create test system connected to same database (using configuration from dataSource)
	 * Generated system name will be used.
	 * 
	 * @return
	 */
	@Override
	public SysSystem createSystem(String tableName) {
		return createSystem(tableName, null);
	}
	
	/**
	 * 
	 * 
	 * @param tableName
	 * @param systemName
	 * @return
	 */
	@Override
	public SysSystem createSystem(String tableName, String systemName) {
		// create owner
		org.apache.tomcat.jdbc.pool.DataSource tomcatDataSource = ((org.apache.tomcat.jdbc.pool.DataSource) dataSource);
		SysSystem system = new SysSystem();
		system.setName(systemName == null ? tableName + "_" + System.currentTimeMillis() : systemName);

		system.setConnectorKey(new SysConnectorKey(systemService.getTestConnectorKey()));
		system = systemService.save(system);

		IdmFormDefinitionDto savedFormDefinition = systemService.getConnectorFormDefinition(system.getConnectorInstance());

		List<IdmFormValueDto> values = new ArrayList<>();

		IdmFormValueDto jdbcUrlTemplate = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("jdbcUrlTemplate"));
		jdbcUrlTemplate.setValue(tomcatDataSource.getUrl());
		values.add(jdbcUrlTemplate);
		IdmFormValueDto jdbcDriver = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("jdbcDriver"));
		jdbcDriver.setValue(tomcatDataSource.getDriverClassName());
		values.add(jdbcDriver);

		IdmFormValueDto user = new IdmFormValueDto(savedFormDefinition.getMappedAttributeByCode("user"));
		user.setValue(tomcatDataSource.getUsername());
		values.add(user);
		IdmFormValueDto password = new IdmFormValueDto(savedFormDefinition.getMappedAttributeByCode("password"));
		password.setValue(tomcatDataSource.getPoolProperties().getPassword());
		values.add(password);
		IdmFormValueDto table = new IdmFormValueDto(savedFormDefinition.getMappedAttributeByCode("table"));
		table.setValue(tableName);
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
		allNative.setValue(true);
		values.add(allNative);
		IdmFormValueDto rethrowAllSQLExceptions = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("rethrowAllSQLExceptions"));
		rethrowAllSQLExceptions.setValue(true);
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
		IdmFormValueDto changeLogColumnValue = new IdmFormValueDto(
				savedFormDefinition.getMappedAttributeByCode("changeLogColumn"));
		changeLogColumnValue.setValue(null);
		values.add(changeLogColumnValue);

		formService.saveValues(system, savedFormDefinition, values);

		return system;
	}
	
	@Override
	public SysSystem createTestResourceSystem(boolean withMapping) {
		return createTestResourceSystem(withMapping, null);
	}
	
	@Override
	public SysSystem createTestResourceSystem(boolean withMapping, String systemName) {
		// create test system
		SysSystem system = createSystem(TestResource.TABLE_NAME, systemName);
		//
		if (!withMapping) {
			return system;
		}		
		//
		// generate schema for system
		List<SysSchemaObjectClassDto> objectClasses = systemService.generateSchema(system);
		//
		SysSystemMappingDto systemMapping = new SysSystemMappingDto();
		systemMapping.setName("default_" + System.currentTimeMillis());
		systemMapping.setEntityType(SystemEntityType.IDENTITY);
		systemMapping.setOperationType(SystemOperationType.PROVISIONING);
		systemMapping.setObjectClass(objectClasses.get(0).getId());
		systemMapping = systemMappingService.save(systemMapping);

		SchemaAttributeFilter schemaAttributeFilter = new SchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		
		Page<SysSchemaAttributeDto> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		for(SysSchemaAttributeDto schemaAttr : schemaAttributesPage) {
			if (ATTRIBUTE_MAPPING_NAME.equals(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setUid(true);
				attributeMapping.setEntityAttribute(true);
				attributeMapping.setIdmPropertyName(IdmIdentity_.username.getName());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(systemMapping.getId());
				systemAttributeMappingService.save(attributeMapping);
			} else if (ATTRIBUTE_MAPPING_ENABLE.equals(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setUid(false);
				attributeMapping.setEntityAttribute(true);
				attributeMapping.setIdmPropertyName("disabled");
				attributeMapping.setTransformToResourceScript("return String.valueOf(!attributeValue);");
				attributeMapping.setTransformFromResourceScript("return !attributeValue;");
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(systemMapping.getId());
				systemAttributeMappingService.save(attributeMapping);
			} else if (ATTRIBUTE_MAPPING_PASSWORD.equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setIdmPropertyName("password");
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSystemMapping(systemMapping.getId());
				systemAttributeMappingService.save(attributeMapping);
			} else if (ATTRIBUTE_MAPPING_FIRSTNAME.equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setIdmPropertyName(IdmIdentity_.firstName.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSystemMapping(systemMapping.getId());
				systemAttributeMappingService.save(attributeMapping);
			} else if (ATTRIBUTE_MAPPING_LASTNAME.equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setIdmPropertyName(IdmIdentity_.lastName.getName());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(systemMapping.getId());
				systemAttributeMappingService.save(attributeMapping);
			} else if (ATTRIBUTE_MAPPING_EMAIL.equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setIdmPropertyName(IdmIdentity_.email.getName());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(systemMapping.getId());
				systemAttributeMappingService.save(attributeMapping);
			}
		}		
		return system;
	}
	
	@Override
	public SysSystemMappingDto getDefaultMapping(SysSystem system) {
		List<SysSystemMappingDto> mappings = systemMappingService.findBySystem(system, SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY);
		if(mappings.isEmpty()) {
			throw new CoreException(String.format("Default mapping for system[%s] not found", system.getId()));
		}
		//
		return mappings.get(0);
	}
	
	@Override
	public SysRoleSystemDto createRoleSystem(IdmRoleDto role, SysSystem system) {
		SysRoleSystemDto roleSystem = new SysRoleSystemDto();
		roleSystem.setRole(role.getId());
		roleSystem.setSystem(system.getId());
		// default mapping
		List<SysSystemMappingDto> mappings = systemMappingService.findBySystem(system, SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY);
		//
		roleSystem.setSystemMapping(mappings.get(0).getId());
		return roleSystemService.save(roleSystem);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public TestResource findResource(String uid) {
		return entityManager.find(TestResource.class, uid);
	}
}
