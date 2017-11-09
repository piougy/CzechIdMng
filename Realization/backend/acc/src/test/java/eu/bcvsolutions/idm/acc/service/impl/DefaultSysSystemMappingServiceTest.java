package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.DefaultTestHelper;
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
	@Autowired private DefaultTestHelper testHelper;

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

		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);

		SysSystemMappingDto mappingSystem1 = testHelper.createMappingSystem(entityType, objectClass);
		mappingSystem1.setName("SomeName01");
		mappingService.save(mappingSystem1);
		SysSystemMappingDto mappingSystem2 = testHelper.createMappingSystem(entityType, objectClass);
		mappingSystem2.setName("SomeName02");
		mappingService.save(mappingSystem2);
		SysSystemMappingDto mappingSystem3 = testHelper.createMappingSystem(entityType, objectClass);
		mappingSystem3.setName("SomeName22");
		mappingService.save(mappingSystem3);

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setText("SomeName0");

		Page<SysSystemMappingDto> result = mappingService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(mappingSystem1));
		assertTrue(result.getContent().contains(mappingSystem2));
	}

	@Test
	public void typeFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;

		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);

		testHelper.createMappingSystem(SystemEntityType.CONTRACT, objectClass);
		SysSystemMappingDto mappingSystem2 = testHelper.createMappingSystem(SystemEntityType.CONTRACT, objectClass);
		SysSystemMappingDto mappingSystem3 = testHelper.createMappingSystem(SystemEntityType.ROLE, objectClass);

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setEntityType(SystemEntityType.CONTRACT);

		Page<SysSystemMappingDto> result = mappingService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(mappingSystem2));
		assertFalse(result.getContent().contains(mappingSystem3));
	}

	@Test
	public void operationTypeFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;

		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);

		SysSystemMappingDto mappingSystem1 = testHelper.createMappingSystem(entityType, objectClass);
		SysSystemMappingDto mappingSystem2 = createProvisioningMappingSystem(SystemEntityType.ROLE, objectClass);
		SysSystemMappingDto mappingSystem3 = createProvisioningMappingSystem(entityType, objectClass);

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setOperationType(SystemOperationType.PROVISIONING);
		filter.setSystemId(system.getId());

		Page<SysSystemMappingDto> result = mappingService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(mappingSystem3));
		assertFalse(result.getContent().contains(mappingSystem1));
	}

	@Test
	public void systemIdFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;

		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);
		SysSystemDto system2 = createSystem();
		SysSchemaObjectClassDto objectClass2 = createObjectClass(system2);

		SysSystemMappingDto mappingSystem1 = testHelper.createMappingSystem(entityType, objectClass);
		SysSystemMappingDto mappingSystem2 = testHelper.createMappingSystem(entityType, objectClass2);

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setSystemId(system.getId());
		Page<SysSystemMappingDto> result = mappingService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(mappingSystem1));
		assertFalse(result.getContent().contains(mappingSystem2));
	}

	@Test
	public void objectClassFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;

		SysSystemDto system = createSystem();
		SysSystemDto system2 = createSystem();

		SysSchemaObjectClassDto objectClass = createObjectClass(system);
		SysSchemaObjectClassDto objectClass2 = createObjectClass(system2);

		SysSystemMappingDto mappingSystem1 = testHelper.createMappingSystem(entityType, objectClass);
		SysSystemMappingDto mappingSystem2 = testHelper.createMappingSystem(entityType, objectClass2);

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setObjectClassId(mappingSystem1.getObjectClass());
		Page<SysSystemMappingDto> result = mappingService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(mappingSystem1));
		assertFalse(result.getContent().contains(mappingSystem2));
	}

	@Test
	public void treeTypeIdFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SystemEntityType entityType = SystemEntityType.IDENTITY;

		IdmTreeTypeDto treeType = new IdmTreeTypeDto();
		treeType.setName("SomeTreeTypeName");
		treeType.setCode("CodeCodeCodeCode");
		treeType = treeTypeService.save(treeType);

		IdmTreeTypeDto treeType2 = new IdmTreeTypeDto();
		treeType2.setName("SomeTreeTypeName2");
		treeType2.setCode("CodeCodeCodeCode2");
		treeType2 = treeTypeService.save(treeType2);

		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);

		SysSystemMappingDto mappingSystem1 = testHelper.createMappingSystem(entityType, objectClass);
		mappingSystem1.setTreeType(treeType.getId());
		mappingSystem1 = mappingService.save(mappingSystem1);
		SysSystemMappingDto mappingSystem2 = testHelper.createMappingSystem(entityType, objectClass);
		mappingSystem2.setTreeType(treeType2.getId());
		mappingSystem2 = mappingService.save(mappingSystem2);

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setTreeTypeId(mappingSystem1.getTreeType());
		Page<SysSystemMappingDto> result = mappingService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(mappingSystem1));
		assertFalse(result.getContent().contains(mappingSystem2));
	}

	private SysSystemDto createSystem() {
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
