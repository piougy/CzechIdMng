package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
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
public class DefaultSysSystemMappingServiceTest extends AbstractIntegrationTest {

	@Autowired private DefaultSysSystemMappingService mappingService;
	@Autowired private SysSystemService systemService;
	@Autowired private SysSchemaObjectClassService schemaObjectClassService;
	@Autowired private IdmTreeTypeService treeTypeService;

	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void textFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;

		SysSystemDto system = createRoleSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);

		SysSystemMappingDto mappingSystem1 = createMappingSystem(entityType, objectClass);
		mappingSystem1.setName("Name01");
		mappingService.save(mappingSystem1);
		SysSystemMappingDto mappingSystem2 = createMappingSystem(entityType, objectClass);
		mappingSystem2.setName("Name02");
		mappingService.save(mappingSystem2);
		SysSystemMappingDto mappingSystem3 = createMappingSystem(entityType, objectClass);
		mappingSystem3.setName("Name22");
		mappingService.save(mappingSystem3);

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setText("Name0");

		Page<SysSystemMappingDto> result = mappingService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(mappingSystem2));
	}

	@Test
	public void typeFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;

		SysSystemDto system = createRoleSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);

		createMappingSystem(SystemEntityType.CONTRACT, objectClass);
		SysSystemMappingDto mappingSystem2 = createMappingSystem(SystemEntityType.CONTRACT, objectClass);
		SysSystemMappingDto mappingSystem3 = createMappingSystem(SystemEntityType.ROLE, objectClass);

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setEntityType(SystemEntityType.CONTRACT);

		Page<SysSystemMappingDto> result = mappingService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(mappingSystem2));
		assertFalse(result.getContent().contains(mappingSystem3));

		filter.setEntityType(SystemEntityType.ROLE_CATALOGUE);
		Page<SysSystemMappingDto> result2 = mappingService.find(filter, null, permission);
		assertEquals(0, result2.getTotalElements());
	}

	@Test
	public void operationTypeFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;

		SysSystemDto system = createRoleSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);

		SysSystemMappingDto mappingSystem1 = createMappingSystem(entityType, objectClass);
		SysSystemMappingDto mappingSystem2 = createProvisioningMappingSystem(SystemEntityType.CONTRACT, objectClass);
		SysSystemMappingDto mappingSystem3 = createProvisioningMappingSystem(entityType, objectClass);

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setOperationType(SystemOperationType.PROVISIONING);

		Page<SysSystemMappingDto> result = mappingService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(mappingSystem3));
		assertFalse(result.getContent().contains(mappingSystem1));
	}

	@Test
	public void systemIdFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;

		SysSystemDto system = createRoleSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);
		SysSystemMappingDto mappingSystem = createMappingSystem(entityType, objectClass);
		createMappingSystem(entityType, objectClass);

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setSystemId(system.getId());
		Page<SysSystemMappingDto> result = mappingService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(mappingSystem));
	}

	@Test
	public void objectClassFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;

		SysSystemDto system = createRoleSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);
		SysSystemMappingDto mappingSystem = createMappingSystem(entityType, objectClass);

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setObjectClassId(mappingSystem.getObjectClass());
		Page<SysSystemMappingDto> result = mappingService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
	}

	@Test
	public void treeTypeIdFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;
		IdmTreeTypeDto treeType = new IdmTreeTypeDto();
		treeType.setName("SomeTreeTypeName");
		treeType.setCode("CodeCodeCodeCode");
		treeTypeService.save(treeType);

		SysSystemDto system = createRoleSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);

		SysSystemMappingDto mappingSystem = createMappingSystem(entityType, objectClass);
		mappingSystem.setTreeType(treeType.getId());
		mappingService.save(mappingSystem);

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setTreeTypeId(mappingSystem.getTreeType());
		Page<SysSystemMappingDto> result = mappingService.find(filter, null, permission);
		assertTrue(result.getContent().contains(mappingSystem));
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

	private SysSystemMappingDto createMappingSystem(SystemEntityType type, SysSchemaObjectClassDto objectClass) {
		// system mapping
		SysSystemMappingDto mapping = new SysSystemMappingDto();
		mapping.setName("Name" + UUID.randomUUID());
		mapping.setEntityType(type);
		mapping.setTreeType(UUID.randomUUID());
		mapping.setObjectClass(objectClass.getId());
		mapping.setOperationType(SystemOperationType.SYNCHRONIZATION);
		return mappingService.save(mapping);
	}

	private SysSystemMappingDto createProvisioningMappingSystem(SystemEntityType type, SysSchemaObjectClassDto objectClass) {
		// system mapping
		SysSystemMappingDto mapping = new SysSystemMappingDto();
		mapping.setName("Name" + UUID.randomUUID());
		mapping.setEntityType(type);
		mapping.setObjectClass(objectClass.getId());
		mapping.setOperationType(SystemOperationType.PROVISIONING);
		return mappingService.save(mapping);
	}
}
