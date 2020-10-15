package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.event.SystemMappingEvent;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Searching entities, using filters
 *
 * @author Petr Hanák
 * @author Vít Švanda
 */
@Transactional
public class DefaultSysSystemMappingServiceTest extends AbstractIntegrationTest {

	@Autowired
	private SysSystemMappingService mappingService;
	@Autowired
	private SysSystemAttributeMappingService mappingAttributeService;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysSchemaObjectClassService schemaObjectClassService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private IdmTreeTypeService treeTypeService;
	@Autowired
	private TestHelper testHelper;

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

		SysSystemMappingDto mappingSystem1 = testHelper.createMappingSystem(SystemEntityType.CONTRACT, objectClass);
		SysSystemMappingDto mappingSystem2 = testHelper.createMappingSystem(SystemEntityType.CONTRACT, objectClass);
		SysSystemMappingDto mappingSystem3 = testHelper.createMappingSystem(SystemEntityType.TREE, objectClass);

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setSystemId(system.getId());
		filter.setEntityType(SystemEntityType.CONTRACT);

		Page<SysSystemMappingDto> result = mappingService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(mappingSystem1));
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
		createProvisioningMappingSystem(SystemEntityType.TREE, objectClass);
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

	@Test
	public void testAutomaticGenerateOfMappedAttributesDisabled() {
		SysSystemDto system = testHelper.createSystem(testHelper.createName());
		SysSchemaObjectClassDto schema = this.createObjectClass(system);

		createSchemaAttribute("__NAME__", schema);
		createSchemaAttribute("first_name", schema);
		createSchemaAttribute("surname", schema); // redundant to lastname
		createSchemaAttribute("lastname", schema);
		createSchemaAttribute("__UID__", schema); // redundant to __NAME__
		createSchemaAttribute("email", schema);
		createSchemaAttribute("titleBefore", schema);
		createSchemaAttribute("title_after", schema);


		SysSystemMappingDto mappingDto = this.createProvisioningMappingSystem(SystemEntityType.IDENTITY, schema);
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(mappingDto.getId());

		List<SysSystemAttributeMappingDto> mappingAttributes = mappingAttributeService.find(attributeMappingFilter, null).getContent();
		// Automatic attribute generating is disabled by default.
		assertEquals(0, mappingAttributes.size());
	}

	@Test
	public void testAutomaticGenerateOfMappedAttributes() {
		SysSystemDto system = testHelper.createSystem(testHelper.createName());
		SysSchemaObjectClassDto schema = this.createObjectClass(system);

		createSchemaAttribute("__NAME__", schema);
		createSchemaAttribute("first_name", schema);
		createSchemaAttribute("surname", schema); // redundant to lastname
		createSchemaAttribute("lastname", schema);
		createSchemaAttribute("__UID__", schema); // redundant to __NAME__
		createSchemaAttribute("email", schema);
		createSchemaAttribute("titleBefore", schema);
		createSchemaAttribute("title_after", schema);
		createSchemaAttribute("not_exist", schema);

		SysSystemMappingDto mappingDto = new SysSystemMappingDto();
		mappingDto.setName(testHelper.createName());
		mappingDto.setEntityType(SystemEntityType.IDENTITY);
		mappingDto.setObjectClass(schema.getId());
		mappingDto.setOperationType(SystemOperationType.PROVISIONING);

		mappingDto = mappingService.publish(
				new SystemMappingEvent(
						SystemMappingEvent.SystemMappingEventType.CREATE,
						mappingDto,
						ImmutableMap.of(SysSystemMappingService.ENABLE_AUTOMATIC_CREATION_OF_MAPPING, true)))
				.getContent();

		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(mappingDto.getId());

		List<SysSystemAttributeMappingDto> mappingAttributes = mappingAttributeService.find(attributeMappingFilter, null).getContent();
		// Automatic attribute generating is enabled.
		assertEquals(6, mappingAttributes.size());

		SysSystemAttributeMappingDto usernameAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("__NAME__"))
				.findFirst()
				.orElse(null);

		assertNotNull(usernameAttribute);
		assertTrue(usernameAttribute.isUid());
		assertEquals(IdmIdentity_.username.getName(), usernameAttribute.getIdmPropertyName());

		SysSystemAttributeMappingDto lastnameAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("lastname"))
				.findFirst()
				.orElse(null);

		assertNotNull(lastnameAttribute);
		assertFalse(lastnameAttribute.isUid());
		assertEquals(IdmIdentity_.lastName.getName(), lastnameAttribute.getIdmPropertyName());

		SysSystemAttributeMappingDto firstNameAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("first_name"))
				.findFirst()
				.orElse(null);

		assertNotNull(firstNameAttribute);
		assertFalse(firstNameAttribute.isUid());
		assertEquals(IdmIdentity_.firstName.getName(), firstNameAttribute.getIdmPropertyName());

		SysSystemAttributeMappingDto titleBeforeAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("titleBefore"))
				.findFirst()
				.orElse(null);

		assertNotNull(titleBeforeAttribute);
		assertFalse(titleBeforeAttribute.isUid());
		assertEquals(IdmIdentity_.titleBefore.getName(), titleBeforeAttribute.getIdmPropertyName());

		SysSystemAttributeMappingDto titleAfterAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("title_after"))
				.findFirst()
				.orElse(null);

		assertNotNull(titleAfterAttribute);
		assertFalse(titleAfterAttribute.isUid());
		assertEquals(IdmIdentity_.titleAfter.getName(), titleAfterAttribute.getIdmPropertyName());

		SysSystemAttributeMappingDto emailAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("email"))
				.findFirst()
				.orElse(null);

		assertNotNull(emailAttribute);
		assertFalse(emailAttribute.isUid());
		assertEquals(IdmIdentity_.email.getName(), emailAttribute.getIdmPropertyName());
	}

	@Test
	public void testAutomaticGenerateOfMappedAttributesTree() {
		SysSystemDto system = testHelper.createSystem(testHelper.createName());
		SysSchemaObjectClassDto schema = this.createObjectClass(system);

		createSchemaAttribute("__NAME__", schema);
		createSchemaAttribute("parent", schema);
		createSchemaAttribute("name", schema);
		createSchemaAttribute("code", schema); // redundant to __NAME__
		createSchemaAttribute("description", schema);
		createSchemaAttribute("not_exist", schema);

		SysSystemMappingDto mappingDto = new SysSystemMappingDto();
		mappingDto.setName(testHelper.createName());
		mappingDto.setEntityType(SystemEntityType.TREE);
		mappingDto.setObjectClass(schema.getId());
		mappingDto.setOperationType(SystemOperationType.PROVISIONING);

		mappingDto = mappingService.publish(
				new SystemMappingEvent(
						SystemMappingEvent.SystemMappingEventType.CREATE,
						mappingDto,
						ImmutableMap.of(SysSystemMappingService.ENABLE_AUTOMATIC_CREATION_OF_MAPPING, true)))
				.getContent();

		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(mappingDto.getId());

		List<SysSystemAttributeMappingDto> mappingAttributes = mappingAttributeService.find(attributeMappingFilter, null).getContent();
		// Automatic attribute generating is enabled.
		assertEquals(3, mappingAttributes.size());

		SysSystemAttributeMappingDto primaryAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("__NAME__"))
				.findFirst()
				.orElse(null);

		assertNotNull(primaryAttribute);
		assertTrue(primaryAttribute.isUid());
		assertEquals(IdmTreeNode_.code.getName(), primaryAttribute.getIdmPropertyName());

		SysSystemAttributeMappingDto nameAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("name"))
				.findFirst()
				.orElse(null);

		assertNotNull(nameAttribute);
		assertFalse(nameAttribute.isUid());
		assertEquals(IdmTreeNode_.name.getName(), nameAttribute.getIdmPropertyName());

		SysSystemAttributeMappingDto parentAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("parent"))
				.findFirst()
				.orElse(null);

		assertNotNull(parentAttribute);
		assertFalse(parentAttribute.isUid());
		assertEquals(IdmTreeNode_.parent.getName(), parentAttribute.getIdmPropertyName());
	}

	@Test
	public void testAutomaticGenerateOfMappedAttributesRoleCatalogue() {
		SysSystemDto system = testHelper.createSystem(testHelper.createName());
		SysSchemaObjectClassDto schema = this.createObjectClass(system);

		createSchemaAttribute("__NAME__", schema);
		createSchemaAttribute("parent", schema);
		createSchemaAttribute("name", schema);
		createSchemaAttribute("code", schema); // redundant to __NAME__
		createSchemaAttribute("description", schema);
		createSchemaAttribute("not_exist", schema);

		SysSystemMappingDto mappingDto = new SysSystemMappingDto();
		mappingDto.setName(testHelper.createName());
		mappingDto.setEntityType(SystemEntityType.ROLE_CATALOGUE);
		mappingDto.setObjectClass(schema.getId());
		mappingDto.setOperationType(SystemOperationType.PROVISIONING);

		mappingDto = mappingService.publish(
				new SystemMappingEvent(
						SystemMappingEvent.SystemMappingEventType.CREATE,
						mappingDto,
						ImmutableMap.of(SysSystemMappingService.ENABLE_AUTOMATIC_CREATION_OF_MAPPING, true)))
				.getContent();

		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(mappingDto.getId());

		List<SysSystemAttributeMappingDto> mappingAttributes = mappingAttributeService.find(attributeMappingFilter, null).getContent();
		// Automatic attribute generating is enabled.
		assertEquals(4, mappingAttributes.size());

		SysSystemAttributeMappingDto primaryAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("__NAME__"))
				.findFirst()
				.orElse(null);

		assertNotNull(primaryAttribute);
		assertTrue(primaryAttribute.isUid());
		assertEquals(IdmRoleCatalogue_.code.getName(), primaryAttribute.getIdmPropertyName());

		SysSystemAttributeMappingDto nameAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("name"))
				.findFirst()
				.orElse(null);

		assertNotNull(nameAttribute);
		assertFalse(nameAttribute.isUid());
		assertEquals(IdmRoleCatalogue_.name.getName(), nameAttribute.getIdmPropertyName());

		SysSystemAttributeMappingDto parentAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("parent"))
				.findFirst()
				.orElse(null);

		assertNotNull(parentAttribute);
		assertFalse(parentAttribute.isUid());
		assertEquals(IdmRoleCatalogue_.parent.getName(), parentAttribute.getIdmPropertyName());

		SysSystemAttributeMappingDto descriptionAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("description"))
				.findFirst()
				.orElse(null);

		assertNotNull(descriptionAttribute);
		assertFalse(descriptionAttribute.isUid());
		assertEquals(IdmRoleCatalogue_.description.getName(), descriptionAttribute.getIdmPropertyName());
	}

	private SysSchemaAttributeDto createSchemaAttribute(String name, SysSchemaObjectClassDto schema) {
		SysSchemaAttributeDto attributeDto = new SysSchemaAttributeDto();
		attributeDto.setObjectClass(schema.getId());
		attributeDto.setName(name);
		attributeDto.setNativeName(name);
		attributeDto.setClassType(String.class.getCanonicalName());
		attributeDto.setMultivalued(false);

		return schemaAttributeService.save(attributeDto);
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
