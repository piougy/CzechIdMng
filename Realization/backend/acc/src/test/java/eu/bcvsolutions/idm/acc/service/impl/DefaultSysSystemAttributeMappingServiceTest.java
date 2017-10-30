package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

/**
 * Searching entities, using filters
 *
 * @author Petr Hanák
 *
 */
public class DefaultSysSystemAttributeMappingServiceTest extends AbstractIntegrationTest {

	@Autowired
	private TestHelper helper;
	@Autowired private SysSchemaObjectClassService schemaObjectClassService;
	@Autowired private SysSchemaAttributeService attributeService;
	@Autowired private SysSystemService systemService;
	@Autowired private DefaultSysSystemMappingService mappingService;
	@Autowired private DefaultSysSystemAttributeMappingService attributeMappingService;

	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void idFilterTest() {

	}

	@Test
	public void textFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;
		AttributeMappingStrategyType strategyType = AttributeMappingStrategyType.MERGE;

		SysSystemDto system = createRoleSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);
		SysSystemMappingDto systemMapping = createMappingSystem(entityType, objectClass);
		SysSchemaAttributeDto schemaAttribute = createSchemaAttribute(objectClass);

		SysSystemAttributeMappingDto attributeMapping1 = createAttributeMappingSystem(systemMapping, strategyType, schemaAttribute.getId());
		attributeMapping1.setName("Name01");
		attributeMappingService.save(attributeMapping1);

		SysSystemAttributeMappingDto attributeMapping2 = createAttributeMappingSystem(systemMapping, AttributeMappingStrategyType.CREATE, schemaAttribute.getId());
		attributeMapping2.setName("Name21");
		attributeMappingService.save(attributeMapping2);

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setText("Name0");

		Page<SysSystemAttributeMappingDto> result = attributeMappingService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(attributeMapping1));
		assertFalse(result.getContent().contains(attributeMapping2));
	}

	@Test
	public void schemaAttributeIdFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;
		AttributeMappingStrategyType strategyType = AttributeMappingStrategyType.MERGE;

		SysSystemDto system = createRoleSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);
		SysSystemMappingDto systemMapping = createMappingSystem(entityType, objectClass);

		SysSchemaAttributeDto schemaAttribute = createSchemaAttribute(objectClass);
		SysSchemaAttributeDto schemaAttribute2 = createSchemaAttribute(objectClass);

		SysSystemAttributeMappingDto attributeMapping = createAttributeMappingSystem(systemMapping, strategyType, schemaAttribute.getId());
		SysSystemAttributeMappingDto attributeMapping2 = createAttributeMappingSystem(systemMapping, strategyType, schemaAttribute2.getId());

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSchemaAttributeId(schemaAttribute.getId());

		Page<SysSystemAttributeMappingDto> result = attributeMappingService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(attributeMapping));
		assertFalse(result.getContent().contains(attributeMapping2));
	}

	@Test
	public void systemMappingIdFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;
		AttributeMappingStrategyType strategyType = AttributeMappingStrategyType.MERGE;

		SysSystemDto system = createRoleSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);
		SysSystemMappingDto systemMapping1 = createMappingSystem(entityType, objectClass);
		SysSystemMappingDto systemMapping2 = createMappingSystem(entityType, objectClass);
		SysSchemaAttributeDto schemaAttribute = createSchemaAttribute(objectClass);

		SysSystemAttributeMappingDto attributeMapping1 = createAttributeMappingSystem(systemMapping1, strategyType, schemaAttribute.getId());
		SysSystemAttributeMappingDto attributeMapping2 = createAttributeMappingSystem(systemMapping2, strategyType, schemaAttribute.getId());

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemMappingId(systemMapping1.getId());

		Page<SysSystemAttributeMappingDto> result = attributeMappingService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(attributeMapping1));
		assertFalse(result.getContent().contains(attributeMapping2));
	}

	@Test
	public void systemIdFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;
		AttributeMappingStrategyType strategyType = AttributeMappingStrategyType.MERGE;

		SysSystemDto system1 = createRoleSystem();
		SysSystemDto system2 = createRoleSystem();
		SysSchemaObjectClassDto objectClass1 = createObjectClass(system1);
		SysSchemaObjectClassDto objectClass2 = createObjectClass(system2);

		SysSystemMappingDto systemMapping1 = createMappingSystem(entityType, objectClass1);
		SysSystemMappingDto systemMapping2 = createMappingSystem(entityType, objectClass2);
		SysSchemaAttributeDto schemaAttribute1 = createSchemaAttribute(objectClass1);
		SysSchemaAttributeDto schemaAttribute2 = createSchemaAttribute(objectClass2);

		SysSystemAttributeMappingDto attributeMapping1 = createAttributeMappingSystem(systemMapping1, strategyType, schemaAttribute1.getId());
		SysSystemAttributeMappingDto attributeMapping2 = createAttributeMappingSystem(systemMapping2, strategyType, schemaAttribute2.getId());

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(system1.getId());

		Page<SysSystemAttributeMappingDto> result = attributeMappingService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(attributeMapping1));
		assertFalse(result.getContent().contains(attributeMapping2));

	}

	@Test
	public void idmPropertyNameFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;
		AttributeMappingStrategyType strategyType = AttributeMappingStrategyType.MERGE;

		SysSystemDto system = createRoleSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);
		SysSystemMappingDto systemMapping = createMappingSystem(entityType, objectClass);
		SysSchemaAttributeDto schemaAttribute = createSchemaAttribute(objectClass);

		SysSystemAttributeMappingDto attributeMapping1 = createAttributeMappingSystem(systemMapping, strategyType, schemaAttribute.getId());
		attributeMapping1.setIdmPropertyName("PropName31");
		attributeMappingService.save(attributeMapping1);
		SysSystemAttributeMappingDto attributeMapping2 = createAttributeMappingSystem(systemMapping, AttributeMappingStrategyType.CREATE, schemaAttribute.getId());
		attributeMapping2.setIdmPropertyName("PropName42");
		attributeMappingService.save(attributeMapping2);

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setIdmPropertyName("PropName4");

		Page<SysSystemAttributeMappingDto> result = attributeMappingService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(attributeMapping2));
		assertFalse(result.getContent().contains(attributeMapping1));
	}

	@Test
	public void isUidFitlerTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;
		AttributeMappingStrategyType strategyType = AttributeMappingStrategyType.MERGE;

		SysSystemDto system = createRoleSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);
		SysSystemMappingDto systemMapping1 = createMappingSystem(entityType, objectClass);
		SysSystemMappingDto systemMapping2 = createMappingSystem(entityType, objectClass);
		SysSchemaAttributeDto schemaAttribute = createSchemaAttribute(objectClass);

		SysSystemAttributeMappingDto attributeMapping1 = createAttributeMappingSystem(systemMapping1, AttributeMappingStrategyType.CREATE, schemaAttribute.getId());
		SysSystemAttributeMappingDto attributeMapping2 = createAttributeMappingSystem(systemMapping2, strategyType, schemaAttribute.getId());
		attributeMapping2.setUid(false);
		attributeMappingService.save(attributeMapping2);
		createAttributeMappingSystem(systemMapping1, AttributeMappingStrategyType.SET, schemaAttribute.getId());

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setIsUid(false);

		Page<SysSystemAttributeMappingDto> result = attributeMappingService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(attributeMapping2));
		assertFalse(result.getContent().contains(attributeMapping1));
	}

	private SysSystemDto createRoleSystem() {
		SysSystemDto system = new SysSystemDto();
		system.setName("system_" + UUID.randomUUID());
		return systemService.save(system);
	}

	private SysSchemaObjectClassDto createObjectClass(SysSystemDto system) {
		SysSchemaObjectClassDto objectClass = new SysSchemaObjectClassDto();
		objectClass.setSystem(system.getId());
		objectClass.setObjectClassName("__ACCOUNT__");
		return schemaObjectClassService.save(objectClass);
	}

	private SysSchemaAttributeDto createSchemaAttribute(SysSchemaObjectClassDto objectClass) {
		SysSchemaAttributeDto schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setName("Name" + UUID.randomUUID());
		schemaAttribute.setObjectClass(objectClass.getId());
		schemaAttribute.setClassType("SomeType");
		return attributeService.save(schemaAttribute);
	}

	private SysSystemMappingDto createMappingSystem(SystemEntityType type, SysSchemaObjectClassDto objectClass) {
		// system mapping
		SysSystemMappingDto mapping = new SysSystemMappingDto();
		mapping.setName("Name" + UUID.randomUUID());
		mapping.setEntityType(type);
		mapping.setObjectClass(objectClass.getId());
		mapping.setOperationType(SystemOperationType.SYNCHRONIZATION);
		return mappingService.save(mapping);
	}

	private SysSystemAttributeMappingDto createAttributeMappingSystem(SysSystemMappingDto systemMapping, AttributeMappingStrategyType mappingStrategyType, UUID schemaAttribute) {
		SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
		attributeMapping.setName("Name" + UUID.randomUUID());
		attributeMapping.setSystemMapping(systemMapping.getId());
		attributeMapping.setSchemaAttribute(schemaAttribute);
		attributeMapping.setStrategyType(mappingStrategyType);
		attributeMapping.setUid(true);
		return attributeMappingService.save(attributeMapping);
	}

}
