package eu.bcvsolutions.idm.acc.service.impl;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for system mapping validation
 *
 * @author Patrik Stloukal
 *
 */
public class SysSystemMappingServiceValidationTest extends AbstractIntegrationTest {

	@Autowired
	private SysSystemAttributeMappingService attributeMappingService;
	@Autowired
	private SysSystemMappingService mappingService;
	@Autowired
	private DefaultSysSystemService systemService;
	@Autowired
	private SysSchemaObjectClassService schemaService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;

	@Before
	public void login() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test(expected = ResultCodeException.class)
	public void testSystemMappingValidationMissingIdentifier() {
		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto schema = createSchema(system.getId());
		SysSystemMappingDto mapping = createMapping(schema.getId(), SystemOperationType.PROVISIONING);
		SysSchemaAttributeDto schemaAttribute = createSchemaAttribute(schema.getId());
		createAttributeMapping(mapping.getId(), schemaAttribute.getId(), false, "");
		mappingService.validate(mapping.getId());
	}
	
	@Test
	public void testSystemMappingValidationNotMissingIdentifier() {
		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto schema = createSchema(system.getId());
		SysSystemMappingDto mapping = createMapping(schema.getId(), SystemOperationType.PROVISIONING);
		SysSchemaAttributeDto schemaAttribute = createSchemaAttribute(schema.getId());
		createAttributeMapping(mapping.getId(), schemaAttribute.getId(), true, "");
		mappingService.validate(mapping.getId());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testSystemMappingValidationSynchronizationMissingOwner() {
		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto schema = createSchema(system.getId());
		SysSystemMappingDto mapping = createMapping(schema.getId(), SystemOperationType.SYNCHRONIZATION);
		SysSchemaAttributeDto schemaAttribute = createSchemaAttribute(schema.getId());
		createAttributeMapping(mapping.getId(), schemaAttribute.getId(), true, "");
		mappingService.validate(mapping.getId());
	}
	
	@Test
	public void testSystemMappingValidationSynchronizationNotMissingOwner() {
		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto schema = createSchema(system.getId());
		SysSystemMappingDto mapping = createMapping(schema.getId(), SystemOperationType.SYNCHRONIZATION);
		SysSchemaAttributeDto schemaAttribute = createSchemaAttribute(schema.getId());
		createAttributeMapping(mapping.getId(), schemaAttribute.getId(), true, "identity");
		mappingService.validate(mapping.getId());
	}

	/*
	 * Creates and returns system 
	 * 
	 * @return system
	*/
	private SysSystemDto createSystem() {
		SysSystemDto system = new SysSystemDto();
		String systemName = "system" + System.currentTimeMillis();
		system.setName(systemName);
		system = systemService.save(system);
		return system;
	}

	/*
	 * Creates and returns system mapping
	 * 
	 * @param objectClass
	 * @return mapping
	*/
	private SysSystemMappingDto createMapping(UUID objectClass, SystemOperationType type) {
		SysSystemMappingDto mapping = new SysSystemMappingDto();
		String systemName = "mapping" + System.currentTimeMillis();
		mapping.setName(systemName);
		mapping.setOperationType(type);
		mapping.setObjectClass(objectClass);
		mapping.setEntityType(SystemEntityType.IDENTITY);
		mapping = mappingService.save(mapping);
		return mapping;
	}

	/*
	 * Creates and returns system mapping schema
	 * 
	 * @param system
	 * @return schema
	*/
	private SysSchemaObjectClassDto createSchema(UUID system) {
		SysSchemaObjectClassDto schema = new SysSchemaObjectClassDto();
		schema.setObjectClassName("_Test" + System.currentTimeMillis());
		schema.setSystem(system);
		return schemaService.save(schema);
	}

	/*
	 * Creates and returns system attribute mapping
	 * 
	 * @param mapping, schemaAttribute, uid
	 * @return attributeMapping
	*/
	private SysSystemAttributeMappingDto createAttributeMapping(UUID mapping, UUID schemaAttribute, boolean uid, String propertyName) {
		SysSystemAttributeMappingDto attribute = new SysSystemAttributeMappingDto();
		attribute.setName("name" + System.currentTimeMillis());
		attribute.setSystemMapping(mapping);
		attribute.setSchemaAttribute(schemaAttribute);
		attribute.setEntityAttribute(true);
		attribute.setStrategyType(AttributeMappingStrategyType.SET);
		attribute.setUid(uid);
		attribute.setIdmPropertyName(propertyName);
		return attributeMappingService.save(attribute);
	}

	/*
	 * Creates and returns attribute schema
	 * 
	 * @param schemaObject
	 * @return schemaAttribute
	*/
	private SysSchemaAttributeDto createSchemaAttribute(UUID schemaObject) {
		SysSchemaAttributeDto schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setName("name_test" + System.currentTimeMillis());
		schemaAttribute.setClassType("String");
		schemaAttribute.setObjectClass(schemaObject);
		return schemaAttributeService.save(schemaAttribute);
	}
}
