package eu.bcvsolutions.idm.eav.service;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityFormValue;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.eav.domain.PersistentType;
import eu.bcvsolutions.idm.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.eav.entity.FormableEntity;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.service.api.FormService;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Target system tests
 * 
 * @author Radek Tomiška
 *
 */
public class IdmIdentityFormValueIntegrationTest extends AbstractIntegrationTest {
	
	private final static String FORM_VALUE_ONE = "one";
	private final static String FORM_VALUE_TWO = "two";
	
	@Autowired
	private IdmIdentityService identityService;
	
	@Autowired
	private FormService formService;
	
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testFillFormValues() {
		FormableEntity owner = createTestOwner("test1");
		//
		// create definition one		
		IdmFormAttribute attributeDefinitionOne = new IdmFormAttribute();
		attributeDefinitionOne.setName("name_" + System.currentTimeMillis());
		attributeDefinitionOne.setDisplayName(attributeDefinitionOne.getName());
		attributeDefinitionOne.setPersistentType(PersistentType.TEXT);
		IdmFormDefinition formDefinitionOne = formService.createDefinition(IdmIdentity.class.getCanonicalName(), "t_v1", Lists.newArrayList(attributeDefinitionOne));
		//
		// create definition two		
		IdmFormAttribute attributeDefinitionTwo = new IdmFormAttribute();
		attributeDefinitionTwo.setName("name_" + System.currentTimeMillis());
		attributeDefinitionTwo.setDisplayName(attributeDefinitionTwo.getName());
		attributeDefinitionTwo.setPersistentType(PersistentType.TEXT);		
		IdmFormDefinition formDefinitionTwo = formService.createDefinition(IdmIdentity.class.getCanonicalName(), "t_v2", Lists.newArrayList(attributeDefinitionTwo));
		//		
		AbstractFormValue value1 = new IdmIdentityFormValue(attributeDefinitionOne);
		value1.setValue(FORM_VALUE_ONE);
		
		AbstractFormValue value2 = new IdmIdentityFormValue(attributeDefinitionTwo);
		value2.setValue(FORM_VALUE_TWO);
		
		formService.saveValues(owner, formDefinitionOne, Lists.newArrayList(value1));
		formService.saveValues(owner, formDefinitionTwo, Lists.newArrayList(value2));
		
		List savedValues = formService.getValues(owner);
		
		assertEquals(2, savedValues.size());
		assertEquals(FORM_VALUE_ONE, formService.getValues(owner, formDefinitionOne).get(0).getStringValue());
		assertEquals(FORM_VALUE_TWO, formService.getValues(owner, formDefinitionTwo).get(0).getStringValue());
		//
		// create second owner
		FormableEntity owner2 = createTestOwner("test2");
		
		assertEquals(0, formService.getValues(owner2).size());
		assertEquals(2, formService.getValues(owner).size());
		
		identityService.delete((IdmIdentity) owner2);
		
		assertEquals(0, formService.getValues(owner2).size());
		assertEquals(2, formService.getValues(owner).size());
		
		formService.deleteValues(owner, formDefinitionOne);		
		assertEquals(0, formService.getValues(owner, formDefinitionOne).size());
		assertEquals(1, formService.getValues(owner).size());
		assertEquals(FORM_VALUE_TWO, formService.getValues(owner, formDefinitionTwo).get(0).getStringValue());
		
		identityService.delete((IdmIdentity) owner);
		
		assertEquals(0, formService.getValues(owner).size());
	}
	
	/**
	 * Test multi values order and removal
	 */
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testMultipleValues() {
		FormableEntity owner = createTestOwner("test3");
		//
		// create definition with multi parameter	
		IdmFormAttribute multiAttribite = new IdmFormAttribute();
		String multiAttributeName = "name_" + System.currentTimeMillis();
		multiAttribite.setName(multiAttributeName);
		multiAttribite.setDisplayName(multiAttribite.getName());
		multiAttribite.setPersistentType(PersistentType.TEXT);
		multiAttribite.setMultiple(true);
		IdmFormDefinition formDefinitionOne = formService.createDefinition(IdmIdentity.class.getCanonicalName(), "t_v3", Lists.newArrayList(multiAttribite));
		//
		// fill values
		AbstractFormValue value1 = new IdmIdentityFormValue(multiAttribite);
		value1.setValue(FORM_VALUE_ONE);
		value1.setSeq(0);
		
		AbstractFormValue value2 = new IdmIdentityFormValue(multiAttribite);
		value2.setValue(FORM_VALUE_TWO);
		value2.setSeq(1);
		
		formService.saveValues(owner, formDefinitionOne, Lists.newArrayList(value1, value2));
		
		Map<String, ? extends List> m = formService.toAttributeMap(formService.getValues(owner, formDefinitionOne));
		
		// check order
		assertEquals(2, m.get(multiAttributeName).size());
		assertEquals(FORM_VALUE_ONE, ((AbstractFormValue)m.get(multiAttributeName).get(0)).getValue());
		assertEquals(FORM_VALUE_TWO, ((AbstractFormValue)m.get(multiAttributeName).get(1)).getValue());
		
		formService.saveValues(owner, formDefinitionOne, Lists.newArrayList(value1));
		
		// check delete unsaved multiple values
		m = formService.toAttributeMap(formService.getValues(owner, formDefinitionOne));
		assertEquals(1, m.get(multiAttributeName).size());
		assertEquals(FORM_VALUE_ONE, ((AbstractFormValue)m.get(multiAttributeName).get(0)).getValue());
	}
	
	private FormableEntity createTestOwner(String name) {
		IdmIdentity identity = new IdmIdentity();
		identity.setUsername(name + "_" + System.currentTimeMillis());
		identity.setPassword(new GuardedString("heslo"));
		identity.setFirstName("Test");
		identity.setLastName("Identity");
		identity = identityService.save(identity);
		return identity;
	}
}
