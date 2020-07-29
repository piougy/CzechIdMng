package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Tests for filter {@link SysSystemAttributeMappingFilter} in service {@link DefaultSysSystemAttributeMappingService}
 *
 * @author Ondrej Kopr
 *
 */
public class DefaultSysSystemAttributeMappingServiceFilterTest extends AbstractIntegrationTest {

	@Autowired
	private TestHelper testHelper;
	@Autowired
	private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;

	@Before
	public void login() {
		this.getHelper().loginAdmin();
	}

	@After
	public void logout() {
		this.getHelper().logout();
	}

	@Test
	public void testFilterBySystemMapping() {
		SysSystemDto system = this.getHelper().createTestResourceSystem(true, getHelper().createName());
		// Generate second system for add mapped attributes
		this.getHelper().createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filterMapping = new SysSystemMappingFilter();
		filterMapping.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = systemMappingService.find(filterMapping, null).getContent();
		assertEquals(1, mappings.size());

		SysSystemMappingDto mappingDto = mappings.get(0);
		
		SysSystemAttributeMappingFilter filterAttributeMapping = new SysSystemAttributeMappingFilter();
		filterAttributeMapping.setSystemMappingId(mappingDto.getId());
		List<SysSystemAttributeMappingDto> attributeMappings = systemAttributeMappingService.find(filterAttributeMapping, null).getContent();
		assertEquals(6, attributeMappings.size()); // Six is default for standard test resource
	}

	@Test
	public void testFilterByUid() {
		SysSystemDto system = this.getHelper().createTestResourceSystem(true, getHelper().createName());
		// Generate second system for add mapped attributes
		this.getHelper().createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filterMapping = new SysSystemMappingFilter();
		filterMapping.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = systemMappingService.find(filterMapping, null).getContent();
		assertEquals(1, mappings.size());

		SysSystemMappingDto mappingDto = mappings.get(0);
		
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemMappingId(mappingDto.getId());
		filter.setIsUid(Boolean.TRUE);
		List<SysSystemAttributeMappingDto> attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributeMappings.size());
		SysSystemAttributeMappingDto attributeMappingDto = attributeMappings.get(0);
		assertEquals(TestHelper.ATTRIBUTE_MAPPING_NAME, attributeMappingDto.getName());
	}

	@Test
	public void testFilterBySystemId() {
		SysSystemDto system = this.getHelper().createTestResourceSystem(true, getHelper().createName());
		// Generate second system for add mapped attributes
		this.getHelper().createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filterMapping = new SysSystemMappingFilter();
		filterMapping.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = systemMappingService.find(filterMapping, null).getContent();
		assertEquals(1, mappings.size());
		
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(system.getId());
		List<SysSystemAttributeMappingDto> attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(6, attributeMappings.size()); // Six is default for standard test resource
	}

	@Test
	public void testFilterByNameSecond() {
		SysSystemDto system = this.getHelper().createTestResourceSystem(true, getHelper().createName());
		// Generate second system for add mapped attributes
		this.getHelper().createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filterMapping = new SysSystemMappingFilter();
		filterMapping.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = systemMappingService.find(filterMapping, null).getContent();
		assertEquals(1, mappings.size());

		SysSystemMappingDto mappingDto = mappings.get(0);
		
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(system.getId());
		filter.setName(TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME);
		List<SysSystemAttributeMappingDto> attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributeMappings.size());
		SysSystemAttributeMappingDto attributeMappingDto = attributeMappings.get(0);
		assertEquals(TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME, attributeMappingDto.getName());
		assertEquals(mappingDto.getId(), attributeMappingDto.getSystemMapping());
	}

	@Test
	public void testFilterByNameNotFound() {
		SysSystemDto system = this.getHelper().createTestResourceSystem(true, getHelper().createName());
		// Generate second system for add mapped attributes
		this.getHelper().createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filterMapping = new SysSystemMappingFilter();
		filterMapping.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = systemMappingService.find(filterMapping, null).getContent();
		assertEquals(1, mappings.size());

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(system.getId());
		filter.setName(IdmIdentity_.state.getName());
		List<SysSystemAttributeMappingDto> attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(0, attributeMappings.size());
	}

	@Test
	public void testFilterBySchemaName() {
		SysSystemDto system = this.getHelper().createTestResourceSystem(true, getHelper().createName());
		// Generate second system for add mapped attributes
		this.getHelper().createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filterMapping = new SysSystemMappingFilter();
		filterMapping.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = systemMappingService.find(filterMapping, null).getContent();
		assertEquals(1, mappings.size());

		SysSystemMappingDto mappingDto = mappings.get(0);
		
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemMappingId(mappingDto.getId());
		filter.setName(TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME);
		List<SysSystemAttributeMappingDto> attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributeMappings.size());
		SysSystemAttributeMappingDto attributeMappingDto = attributeMappings.get(0);
		assertEquals(TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME, attributeMappingDto.getName());
		assertEquals(mappingDto.getId(), attributeMappingDto.getSystemMapping());
	}

	@Test
	public void testFilterByOnlySchemaName() {
		SysSystemDto system = this.getHelper().createTestResourceSystem(true, getHelper().createName());
		// Generate second system for add mapped attributes
		this.getHelper().createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filterMapping = new SysSystemMappingFilter();
		filterMapping.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = systemMappingService.find(filterMapping, null).getContent();
		assertEquals(1, mappings.size());

		SysSystemMappingDto mappingDto = mappings.get(0);
		
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemMappingId(mappingDto.getId());
		filter.setSchemaAttributeName(TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME);
		List<SysSystemAttributeMappingDto> attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributeMappings.size());
		SysSystemAttributeMappingDto attributeMappingDto = attributeMappings.get(0);
		assertEquals(TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME, attributeMappingDto.getName());
		assertEquals(mappingDto.getId(), attributeMappingDto.getSystemMapping());

		SysSchemaAttributeDto schemaAttributeDto = schemaAttributeService.get(attributeMappingDto.getSchemaAttribute());
		String newName = getHelper().createName();
		schemaAttributeDto.setName(newName);
		schemaAttributeService.save(schemaAttributeDto);

		attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(0, attributeMappings.size());

		filter.setSchemaAttributeName(newName);
		attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributeMappings.size());
		attributeMappingDto = attributeMappings.get(0);
		assertEquals(schemaAttributeDto.getId(), attributeMappingDto.getSchemaAttribute());
		assertEquals(mappingDto.getId(), attributeMappingDto.getSystemMapping());
		
		// Empty filter
		filter = new SysSystemAttributeMappingFilter();
		filter.setSchemaAttributeName(newName);
		attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributeMappings.size());
		attributeMappingDto = attributeMappings.get(0);
		assertEquals(schemaAttributeDto.getId(), attributeMappingDto.getSchemaAttribute());
		assertEquals(mappingDto.getId(), attributeMappingDto.getSystemMapping());
	}

	@Test
	public void testFilterBySchemaAttributeId() {
		SysSystemDto system = this.getHelper().createTestResourceSystem(true, getHelper().createName());
		// Generate second system for add mapped attributes
		this.getHelper().createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filterMapping = new SysSystemMappingFilter();
		filterMapping.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = systemMappingService.find(filterMapping, null).getContent();
		assertEquals(1, mappings.size());

		SysSystemMappingDto mappingDto = mappings.get(0);
		
		SysSchemaAttributeFilter schemaFilter = new SysSchemaAttributeFilter();
		schemaFilter.setSystemId(system.getId());
		schemaFilter.setName(TestHelper.ATTRIBUTE_MAPPING_EMAIL);
		List<SysSchemaAttributeDto> schemaAttributes = schemaAttributeService.find(schemaFilter, null).getContent();
		assertEquals(1, schemaAttributes.size());
		SysSchemaAttributeDto schemaAttributeDto = schemaAttributes.get(0);
		
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSchemaAttributeId(schemaAttributeDto.getId());
		List<SysSystemAttributeMappingDto> attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributeMappings.size());
		SysSystemAttributeMappingDto attributeMappingDto = attributeMappings.get(0);
		assertEquals(TestHelper.ATTRIBUTE_MAPPING_EMAIL, attributeMappingDto.getName());
		assertEquals(mappingDto.getId(), attributeMappingDto.getSystemMapping());
		assertEquals(schemaAttributeDto.getId(), attributeMappingDto.getSchemaAttribute());
	}

	@Test
	public void testFilterBySendOnPasswordChange() {
		SysSystemDto system = this.getHelper().createTestResourceSystem(true, getHelper().createName());
		// Generate second system for add mapped attributes
		this.getHelper().createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filterMapping = new SysSystemMappingFilter();
		filterMapping.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = systemMappingService.find(filterMapping, null).getContent();
		assertEquals(1, mappings.size());

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(system.getId());
		List<SysSystemAttributeMappingDto> attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(6, attributeMappings.size()); // Six is default for standard test resource

		filter.setPasswordAttribute(Boolean.TRUE);
		attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributeMappings.size());
		SysSystemAttributeMappingDto attributeMappingDto = attributeMappings.get(0);
		assertEquals(TestHelper.ATTRIBUTE_MAPPING_PASSWORD, attributeMappingDto.getName());
		assertTrue(attributeMappingDto.isPasswordAttribute());

		filter.setPasswordAttribute(Boolean.FALSE);
		attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(5, attributeMappings.size()); // Withotu password
	}

	@Test
	public void testFilterByPasswordAttribute() {
		SysSystemDto system = this.getHelper().createTestResourceSystem(true, getHelper().createName());
		// Generate second system for add mapped attributes
		this.getHelper().createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filterMapping = new SysSystemMappingFilter();
		filterMapping.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = systemMappingService.find(filterMapping, null).getContent();
		assertEquals(1, mappings.size());

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(system.getId());
		filter.setPasswordAttribute(Boolean.TRUE);
		List<SysSystemAttributeMappingDto> attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributeMappings.size());

		SysSystemAttributeMappingDto attributeMappingDto = attributeMappings.get(0);
		assertEquals(TestHelper.ATTRIBUTE_MAPPING_PASSWORD, attributeMappingDto.getName());
		assertTrue(attributeMappingDto.isPasswordAttribute());
	}

	@Test
	public void testFilterByDisable() {
		SysSystemDto system = this.getHelper().createTestResourceSystem(true, getHelper().createName());
		// Generate second system for add mapped attributes
		this.getHelper().createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filterMapping = new SysSystemMappingFilter();
		filterMapping.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = systemMappingService.find(filterMapping, null).getContent();
		assertEquals(1, mappings.size());

		SysSystemMappingDto mappingDto = mappings.get(0);

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemMappingId(mappingDto.getId());
		filter.setName(TestHelper.ATTRIBUTE_MAPPING_LASTNAME);
		List<SysSystemAttributeMappingDto> attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributeMappings.size());

		SysSystemAttributeMappingDto attributeMappingDto = attributeMappings.get(0);
		assertEquals(TestHelper.ATTRIBUTE_MAPPING_LASTNAME, attributeMappingDto.getName());
		assertFalse(attributeMappingDto.isDisabledAttribute());
		
		filter.setDisabledAttribute(Boolean.FALSE);
		attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributeMappings.size());

		attributeMappingDto = attributeMappings.get(0);
		assertEquals(TestHelper.ATTRIBUTE_MAPPING_LASTNAME, attributeMappingDto.getName());
		assertFalse(attributeMappingDto.isDisabledAttribute());

		filter.setDisabledAttribute(Boolean.TRUE);
		attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(0, attributeMappings.size());

		attributeMappingDto.setDisabledAttribute(true);
		attributeMappingDto = systemAttributeMappingService.save(attributeMappingDto);
		
		attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributeMappings.size());

		attributeMappingDto = attributeMappings.get(0);
		assertEquals(TestHelper.ATTRIBUTE_MAPPING_LASTNAME, attributeMappingDto.getName());
		assertTrue(attributeMappingDto.isDisabledAttribute());
	}

	@Test
	public void testFilterByOperationTypeNotFound() {
		SysSystemDto system = this.getHelper().createTestResourceSystem(true, getHelper().createName());
		// Generate second system for add mapped attributes
		this.getHelper().createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filterMapping = new SysSystemMappingFilter();
		filterMapping.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = systemMappingService.find(filterMapping, null).getContent();
		assertEquals(1, mappings.size());
		
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(system.getId());
		filter.setOperationType(SystemOperationType.SYNCHRONIZATION);
		List<SysSystemAttributeMappingDto> attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(0, attributeMappings.size());
	}

	@Test
	public void testFilterByOperationTypeFound() {
		SysSystemDto system = this.getHelper().createTestResourceSystem(true, getHelper().createName());
		// Generate second system for add mapped attributes
		this.getHelper().createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filterMapping = new SysSystemMappingFilter();
		filterMapping.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = systemMappingService.find(filterMapping, null).getContent();
		assertEquals(1, mappings.size());
		
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(system.getId());
		filter.setOperationType(SystemOperationType.PROVISIONING);
		List<SysSystemAttributeMappingDto> attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(6, attributeMappings.size()); // Six is default for standard test resource
	}

	@Test
	public void testFilterByEntityTypeNotFound() {
		SysSystemDto system = this.getHelper().createTestResourceSystem(true, getHelper().createName());
		// Generate second system for add mapped attributes
		this.getHelper().createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filterMapping = new SysSystemMappingFilter();
		filterMapping.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = systemMappingService.find(filterMapping, null).getContent();
		assertEquals(1, mappings.size());
		
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(system.getId());
		filter.setEntityType(SystemEntityType.CONTRACT);
		List<SysSystemAttributeMappingDto> attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(0, attributeMappings.size());
	}

	@Test
	public void testFilterByEntityTypeFound() {
		SysSystemDto system = this.getHelper().createTestResourceSystem(true, getHelper().createName());
		// Generate second system for add mapped attributes
		this.getHelper().createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filterMapping = new SysSystemMappingFilter();
		filterMapping.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = systemMappingService.find(filterMapping, null).getContent();
		assertEquals(1, mappings.size());
		
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(system.getId());
		filter.setEntityType(SystemEntityType.IDENTITY);
		List<SysSystemAttributeMappingDto> attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(6, attributeMappings.size()); // Six is default for standard test resource
	}

	@Test
	public void testFilterByEntityTypeAndOperationFound() {
		SysSystemDto system = this.getHelper().createTestResourceSystem(true, getHelper().createName());
		// Generate second system for add mapped attributes
		this.getHelper().createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filterMapping = new SysSystemMappingFilter();
		filterMapping.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = systemMappingService.find(filterMapping, null).getContent();
		assertEquals(1, mappings.size());
		
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(system.getId());
		filter.setEntityType(SystemEntityType.IDENTITY);
		filter.setOperationType(SystemOperationType.PROVISIONING);
		List<SysSystemAttributeMappingDto> attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(6, attributeMappings.size()); // Six is default for standard test resource
	}

	@Test
	public void testFilterByEntityTypeAndOperationNotFound() {
		SysSystemDto system = this.getHelper().createTestResourceSystem(true, getHelper().createName());
		// Generate second system for add mapped attributes
		this.getHelper().createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filterMapping = new SysSystemMappingFilter();
		filterMapping.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = systemMappingService.find(filterMapping, null).getContent();
		assertEquals(1, mappings.size());
		
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(system.getId());
		filter.setEntityType(SystemEntityType.IDENTITY);
		filter.setOperationType(SystemOperationType.SYNCHRONIZATION);
		List<SysSystemAttributeMappingDto> attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(0, attributeMappings.size());
	}

	@Test
	public void testFilterByEntityTypeAndOperationAndUid() {
		SysSystemDto system = this.getHelper().createTestResourceSystem(true, getHelper().createName());
		// Generate second system for add mapped attributes
		this.getHelper().createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filterMapping = new SysSystemMappingFilter();
		filterMapping.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = systemMappingService.find(filterMapping, null).getContent();
		assertEquals(1, mappings.size());
		
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(system.getId());
		filter.setEntityType(SystemEntityType.IDENTITY);
		filter.setOperationType(SystemOperationType.PROVISIONING);
		filter.setIsUid(Boolean.FALSE);
		List<SysSystemAttributeMappingDto> attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(5, attributeMappings.size()); // Without UID attribute

		filter.setIsUid(Boolean.TRUE);
		attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributeMappings.size());

		SysSystemAttributeMappingDto attributeMappingDto = attributeMappings.get(0);
		assertTrue(attributeMappingDto.isUid());
		assertEquals(TestHelper.ATTRIBUTE_MAPPING_NAME, attributeMappingDto.getName());
	}

	@Test
	public void testFilterByAuthenticationAttributeNotFound() {
		SysSystemDto system = this.getHelper().createTestResourceSystem(true, getHelper().createName());
		// Generate second system for add mapped attributes
		this.getHelper().createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filterMapping = new SysSystemMappingFilter();
		filterMapping.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = systemMappingService.find(filterMapping, null).getContent();
		assertEquals(1, mappings.size());
		
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(system.getId());
		filter.setAuthenticationAttribute(Boolean.TRUE);
		List<SysSystemAttributeMappingDto> attributeMappings = systemAttributeMappingService.find(filter, null).getContent();
		assertEquals(0, attributeMappings.size());
	}

	@Override
	protected TestHelper getHelper() {
		return testHelper;
	}
}
