package eu.bcvsolutions.idm.core.bulk.action.impl.eav;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormDefinitionFilter;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.test.api.AbstractExportBulkActionTest;

/**
 * Export form definition
 * 
 * @author Ondrej Husnik
 *
 */
public class FormDefinitionExportBulkActionIntegrationTest extends AbstractExportBulkActionTest {

	@Autowired
	private IdmFormDefinitionService formDefService;

	@Before
	public void login() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testExportImportFormDefinition() {
		IdmFormDefinitionDto formDef = getHelper().createFormDefinition("type_" + getHelper().createName(), true);
		Assert.assertNotNull(formDef.getId());

		final String testAttrName = "testFormAttr" + getHelper().createName();
		IdmFormAttributeDto formAttr = new IdmFormAttributeDto();
		formAttr.setFormDefinition(formDef.getId());
		formAttr.setName(testAttrName);
		formAttr.setCode(testAttrName);
		formAttr.setPlaceholder(testAttrName);

		formAttr.setPersistentType(PersistentType.INT);
		formAttr.setDefaultValue("500000");
		formAttr.setMax(new BigDecimal(1000000));
		formAttr.setMin(new BigDecimal(1000));

		formAttr.setRequired(true);
		formAttr.setRegex("testRegExp");
		formAttr.setValidationMessage("testRegExp");
		List<IdmFormAttributeDto> formAttrs = new ArrayList<IdmFormAttributeDto>();
		formAttrs.add(formAttr);
		formDef.setFormAttributes(formAttrs);
		formDef = formDefService.updateDefinition(formDef);

		executeExportAndImport(formDef, FormDefinitionExportBulkAction.NAME);

		IdmFormDefinitionFilter filter = new IdmFormDefinitionFilter();
		filter.setId(formDef.getId());

		List<IdmFormDefinitionDto> importedDtos = formDefService.find(filter, null).getContent();
		Assert.assertEquals(1, importedDtos.size());

		// Form definition equivalence
		IdmFormDefinitionDto importedFormDef = importedDtos.get(0);
		Assert.assertEquals(formDef.getId(), importedFormDef.getId());
		Assert.assertEquals(formDef.getCode(), importedFormDef.getCode());
		Assert.assertEquals(formDef.getName(), importedFormDef.getName());
		Assert.assertEquals(formDef.isMain(), importedFormDef.isMain());

		// Form attribute equivalence
		IdmFormAttributeDto importedFormAttr = formDef.getMappedAttributeByCode(testAttrName);
		Assert.assertNotNull(importedFormAttr);
		Assert.assertEquals(formAttr.getCode(), importedFormAttr.getCode());
		Assert.assertEquals(formAttr.getName(), importedFormAttr.getName());
		Assert.assertEquals(formAttr.getFormDefinition(), importedFormAttr.getFormDefinition());
		Assert.assertEquals(formAttr.getPlaceholder(), importedFormAttr.getPlaceholder());
		Assert.assertEquals(formAttr.getPersistentType(), importedFormAttr.getPersistentType());
		Assert.assertEquals(formAttr.getDefaultValue(), importedFormAttr.getDefaultValue());
		Assert.assertEquals(formAttr.getMax(), importedFormAttr.getMax());
		Assert.assertEquals(formAttr.getMin(), importedFormAttr.getMin());
		Assert.assertEquals(formAttr.isRequired(), importedFormAttr.isRequired());
		Assert.assertEquals(formAttr.getRegex(), importedFormAttr.getRegex());
		Assert.assertEquals(formAttr.getValidationMessage(), importedFormAttr.getValidationMessage());
	}

}
