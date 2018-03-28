package eu.bcvsolutions.idm.acc.sync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.TestSchemaResource;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Generate schema and check created eav attributes
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class GenerateSchemaWithEavTest extends AbstractIntegrationTest {

	@Autowired
	private SysSystemService systemService;
	@Autowired
	private TestHelper helper;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysSystemAttributeMappingService schemaAttributeMappingService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private IdmFormAttributeService formAttributeService;
	@Autowired
	private FormService formService;
	
	@Before
	public void init() {
		loginAsAdmin("admin");
	}

	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testFormAttributeType() {
		IdmFormDefinitionDto definition = formService.getDefinition(IdmIdentity.class);
		IdmFormAttributeFilter filter = new IdmFormAttributeFilter();
		filter.setDefinitionId(definition.getId());
		//
		SysSystemDto systemDto = this.initData();
		filter.setText("in resource " + systemDto.getName());
		//
		long totalFormAttributesSecond = formAttributeService.find(filter, null).getTotalElements();
		//
		// 9 new eav attribute
		assertEquals(9, totalFormAttributesSecond);
		//
		for (IdmFormAttributeDto attribute : formAttributeService.find(filter, null)) {
			if (attribute.getCode().toLowerCase().equals("boolean_value")) {
				assertEquals(PersistentType.BOOLEAN, attribute.getPersistentType());
			} else if (attribute.getCode().toLowerCase().equals("byte_value")) {
				assertEquals(PersistentType.BYTEARRAY, attribute.getPersistentType());
			} else if (attribute.getCode().toLowerCase().equals("date_value")) {
				// TODO: date value is saved as text
				assertEquals(PersistentType.TEXT, attribute.getPersistentType());
			} else if (attribute.getCode().toLowerCase().equals("double_value")) {
				assertEquals(PersistentType.DOUBLE, attribute.getPersistentType());
			} else if (attribute.getCode().toLowerCase().equals("int_value")) {
					assertEquals(PersistentType.INT, attribute.getPersistentType());
			} else if (attribute.getCode().toLowerCase().equals("long_value")) {
				assertEquals(PersistentType.LONG, attribute.getPersistentType());
			} else if (attribute.getCode().toLowerCase().equals("short_text_value")) {
				// TODO: now is short text saved as TEXT
				assertEquals(PersistentType.TEXT, attribute.getPersistentType());
			} else if (attribute.getCode().toLowerCase().equals("string_value")) {
				assertEquals(PersistentType.TEXT, attribute.getPersistentType());
			} else if (attribute.getCode().toLowerCase().equals("uuid_value")) {
				assertEquals(PersistentType.BYTEARRAY, attribute.getPersistentType());
			} else {
				fail();
			}
		}
		
	}
	
	private SysSystemDto initData() {
		// create test system
		SysSystemDto system = helper.createSystem(TestSchemaResource.TABLE_NAME, null, null, "NAME");
		Assert.assertNotNull(system);

		// generate schema for system
		List<SysSchemaObjectClassDto> objectClasses = systemService.generateSchema(system);

		// Create synchronization mapping
		SysSystemMappingDto syncSystemMapping = new SysSystemMappingDto();
		syncSystemMapping.setName("default_generate_schema_" + System.currentTimeMillis());
		syncSystemMapping.setEntityType(SystemEntityType.IDENTITY);
		syncSystemMapping.setOperationType(SystemOperationType.SYNCHRONIZATION);
		syncSystemMapping.setObjectClass(objectClasses.get(0).getId());
		final SysSystemMappingDto syncMapping = systemMappingService.save(syncSystemMapping);

		createMapping(system, syncMapping);
		return system;
	}
	
	private void createMapping(SysSystemDto system, final SysSystemMappingDto entityHandlingResult) {
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());

		Page<SysSchemaAttributeDto> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		schemaAttributesPage.forEach(schemaAttr -> {
			if ("name".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setUid(true);
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);
			} else if (!schemaAttr.getName().equals("__NAME__")) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setIdmPropertyName(schemaAttr.getName());
				attributeHandlingName.setEntityAttribute(false);
				attributeHandlingName.setExtendedAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);
			}
		});
	}
}
