package eu.bcvsolutions.idm.core.eav.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.CommonFormService;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.service.impl.DefaultCommonFormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Common forms are used for filters and congifurable properties
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultCommonFormServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private ApplicationContext context;
	@Autowired private FormService formService;
	@Autowired private LookupService lookupService;
	//
	private CommonFormService commonFormService;
	
	@Before
	public void init() {
		commonFormService = context.getAutowireCapableBeanFactory().createBean(DefaultCommonFormService.class);
	}
	
	@Test
	@Transactional
	public void testCreateForm() {
		Codeable owner = helper.createIdentity();
		IdmFormAttributeDto attribute = createDefinition();
		IdmFormValueDto formValue = new IdmFormValueDto(attribute);
		formValue.setValue("testOne");
		IdmFormDto formOne = new IdmFormDto();
		formOne.setName("test");
		formOne.setFormDefinition(attribute.getFormDefinition());
		formOne.setValues(Lists.newArrayList(formValue));
		formOne.setOwnerCode(owner.getCode());
		//
		commonFormService.saveForm(owner, formOne);
		formOne = commonFormService.getForms(owner).get(0);
		//
		Assert.assertNotNull(formOne.getId());
		Assert.assertEquals(owner.getCode(), formOne.getOwnerCode());
		Assert.assertEquals(lookupService.lookupEntity(owner.getClass(), owner.getId()).getClass().getCanonicalName(), formOne.getOwnerType());
		Assert.assertEquals(owner.getId(), formOne.getOwnerId());
		Assert.assertEquals(formValue.getValue(), formOne.getValues().get(0).getValue());
		//
		commonFormService.deleteForms(owner);
		Assert.assertTrue(commonFormService.getForms(owner).isEmpty());
	}

	private IdmFormAttributeDto createDefinition() {		
		IdmFormAttributeDto attributeDefinitionOne = new IdmFormAttributeDto();
		attributeDefinitionOne.setCode(helper.createName());
		attributeDefinitionOne.setName(attributeDefinitionOne.getCode());
		attributeDefinitionOne.setPersistentType(PersistentType.TEXT);
		IdmFormDefinitionDto formDefinitionOne = formService.createDefinition(
				IdmIdentity.class.getCanonicalName(),
				helper.createName(), 
				Lists.newArrayList(attributeDefinitionOne));
		return formDefinitionOne.getMappedAttributeByCode(attributeDefinitionOne.getCode());
	}
}
