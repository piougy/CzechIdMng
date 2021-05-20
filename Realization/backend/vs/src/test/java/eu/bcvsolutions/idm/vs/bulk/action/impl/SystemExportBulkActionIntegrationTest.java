package eu.bcvsolutions.idm.vs.bulk.action.impl;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.vs.TestHelper;
import eu.bcvsolutions.idm.vs.entity.VsAccount;
import eu.bcvsolutions.idm.vs.service.api.VsSystemService;
import eu.bcvsolutions.idm.acc.bulk.action.impl.SystemExportBulkAction;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.test.api.AbstractExportBulkActionTest;

/**
 * Export VS system integration test
 * 
 * @author Ondrej Husnik
 *
 */
public class SystemExportBulkActionIntegrationTest extends AbstractExportBulkActionTest {
	
	private String RIGHTS_VS_ATTRIBUTE = "rights";
	private String ATTRIBUTE_TO_DELETE1 = "titleAfter";
	private String ATTRIBUTE_TO_DELETE2 = "titleBefore";

	@Autowired
	private FormService formService;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private TestHelper helper;
	@Autowired
	private VsSystemService vsSystemService;
	@Autowired
	private IdmFormAttributeService formAttributeService;
	
	@Before
	public void login() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}


	@Test
	public void testExportAndImportSystem() {
		SysSystemDto system = helper.createVirtualSystem(helper.createName());
		Assert.assertNotNull(system);
		Assert.assertFalse(system.isDisabled());

		// Make export, upload and import
		executeExportAndImport(system, SystemExportBulkAction.NAME);

		// test correct export and import of a vs account form attribute
		String vsType = VsAccount.class.getName();
		String vsKey = vsSystemService.createVsFormDefinitionKey(system);
		IdmFormDefinitionDto definition = this.formService.getDefinition(vsType, vsKey);
		IdmFormAttributeDto rightFormAttr = formService.getAttribute(definition, RIGHTS_VS_ATTRIBUTE);
		// multiple setting has to be preserved
		Assert.assertTrue(rightFormAttr.isMultiple());
		
		system = systemService.get(system.getId());
		Assert.assertNotNull(system);
		Assert.assertTrue(system.isDisabled());
	}
	
	
	@Test
	public void testExportAndImportSystemWithDeletedAttributes() {
		SysSystemDto system = helper.createVirtualSystem(helper.createName());
		Assert.assertNotNull(system);
		Assert.assertFalse(system.isDisabled());
		List<String> attrsToDelete = Arrays.asList(ATTRIBUTE_TO_DELETE1, ATTRIBUTE_TO_DELETE2);
		
		// delete mapping attributes
		SysSystemAttributeMappingFilter mappingFilter = new SysSystemAttributeMappingFilter();
		mappingFilter.setSystemId(system.getId());
		for (String name : attrsToDelete) {
			mappingFilter.setName(name);
			List<SysSystemAttributeMappingDto> mappings = systemAttributeMappingService.find(mappingFilter, null).getContent();
			Assert.assertEquals(1, mappings.size());
			systemAttributeMappingService.delete(mappings.get(0));;
		}
		
		// delete attributes from schema
		SysSchemaAttributeFilter schemaFilter = new SysSchemaAttributeFilter();
		schemaFilter.setSystemId(system.getId());
		for (String name : attrsToDelete) {
			schemaFilter.setName(name);
			List<SysSchemaAttributeDto> schemaAttrs = schemaAttributeService.find(schemaFilter, null).getContent();
			Assert.assertEquals(1, schemaAttrs.size());
			schemaAttributeService.delete(schemaAttrs.get(0));
		}
		
		// remove attribute from the connector form definition
		IdmFormDefinitionDto connectorFormDefinition = systemService.getConnectorFormDefinition(system);
		List<IdmFormValueDto> formValues = formService.getValues(system, connectorFormDefinition, VsSystemService.ATTRIBUTES_PROPERTY);
		formValues.stream()
			.filter(formValue -> attrsToDelete.contains(formValue.getStringValue()))
			.forEach(formValue -> formService.deleteValue(formValue));
		
				
		// VS Account form attribute
		String vsType = VsAccount.class.getName();
		String vsKey = vsSystemService.createVsFormDefinitionKey(system);
		IdmFormDefinitionDto definition = formService.getDefinition(vsType, vsKey);
		for (String name : attrsToDelete) {
			IdmFormAttributeDto attrToDelete = formAttributeService.findAttribute(vsType, vsKey, name);
			formService.deleteAttribute(attrToDelete);
		}
		
		// Make export, upload and import
		executeExportAndImport(system, SystemExportBulkAction.NAME);
		
		// Right attribute is correctly imported
		IdmFormAttributeDto rightFormAttr = formService.getAttribute(definition, RIGHTS_VS_ATTRIBUTE);
		Assert.assertTrue(rightFormAttr.isMultiple());
		
		// no previously deleted form attributes are created during the import
		for (String name : attrsToDelete) {
			IdmFormAttributeDto attrToDelete = formAttributeService.findAttribute(vsType, vsKey, name);
			Assert.assertNull(attrToDelete);
		}
		
		// no previously deleted form values from connector definition are created during the import
		formValues = formService.getValues(system, connectorFormDefinition, VsSystemService.ATTRIBUTES_PROPERTY);
		long formValueCount = formValues.stream()
			.filter(formValue -> attrsToDelete.contains(formValue.getStringValue()))
			.count();
		Assert.assertEquals(0, formValueCount);
		
		// previously deleted schema attribute is not created during the import 
		schemaFilter = new SysSchemaAttributeFilter();
		schemaFilter.setSystemId(system.getId());
		for (String name : attrsToDelete) {
			schemaFilter.setName(name);
			List<SysSchemaAttributeDto> schemaAttrs = schemaAttributeService.find(schemaFilter, null).getContent();
			Assert.assertEquals(0, schemaAttrs.size());
		}
		
		// deleted attribute mapping attributes is not created during the import
		mappingFilter = new SysSystemAttributeMappingFilter();
		mappingFilter.setSystemId(system.getId());
		for (String name : attrsToDelete) {
			mappingFilter.setName(name);
			List<SysSystemAttributeMappingDto> mappings = systemAttributeMappingService.find(mappingFilter, null).getContent();
			Assert.assertEquals(0, mappings.size());
		}

		system = systemService.get(system.getId());
		Assert.assertNotNull(system);
		Assert.assertTrue(system.isDisabled());
	}

}
