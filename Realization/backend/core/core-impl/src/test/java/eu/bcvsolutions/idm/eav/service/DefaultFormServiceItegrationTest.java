package eu.bcvsolutions.idm.eav.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitDemoData;
import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityFormValue;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.service.api.FormService;
import eu.bcvsolutions.idm.eav.service.api.IdmFormDefinitionService;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Target system tests
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultFormServiceItegrationTest extends AbstractIntegrationTest {
	
	private final static String FORM_VALUE_ONE = "one";
	private final static String FORM_VALUE_TWO = "two";
	
	@Autowired
	private IdmIdentityService identityService;
	
	@Autowired
	private FormService formService;
	
	@Autowired
	private IdmFormDefinitionService formDefinitionService;	
	
	@Before
	public void login() {
		loginAsAdmin(InitTestData.TEST_USER_1);
	}
	
	@After 
	public void logout() {
		super.logout();
	}
	
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
		
		assertEquals(FORM_VALUE_ONE, formService.getValues(owner, formDefinitionOne).get(0).getStringValue());
		assertEquals(FORM_VALUE_TWO, formService.getValues(owner, formDefinitionTwo).get(0).getStringValue());
		//
		// create second owner
		FormableEntity owner2 = createTestOwner("test2");
		
		assertEquals(0, formService.getValues(owner2, formDefinitionOne).size());
		assertEquals(1, formService.getValues(owner, formDefinitionOne).size());
		assertEquals(1, formService.getValues(owner, formDefinitionTwo).size());
		
		identityService.delete((IdmIdentity) owner2);
		
		assertEquals(0, formService.getValues(owner2, formDefinitionOne).size());
		assertEquals(1, formService.getValues(owner, formDefinitionOne).size());
		assertEquals(1, formService.getValues(owner, formDefinitionTwo).size());
		
		formService.deleteValues(owner, formDefinitionOne);		
		assertEquals(0, formService.getValues(owner, formDefinitionOne).size());
		assertEquals(1, formService.getValues(owner, formDefinitionTwo).size());
		assertEquals(FORM_VALUE_TWO, formService.getValues(owner, formDefinitionTwo).get(0).getStringValue());
		
		identityService.delete((IdmIdentity) owner);
		
		assertEquals(0, formService.getValues(owner, formDefinitionOne).size());
		assertEquals(0, formService.getValues(owner, formDefinitionTwo).size());
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
		value1.setSeq((short) 0);
		
		AbstractFormValue value2 = new IdmIdentityFormValue(multiAttribite);
		value2.setValue(FORM_VALUE_TWO);
		value2.setSeq((short) 1);
		
		formService.saveValues(owner, formDefinitionOne, Lists.newArrayList(value1, value2));
		
		Map<String, ? extends List> m = formService.toValueMap(formService.getValues(owner, formDefinitionOne));
		
		// check order
		assertEquals(2, m.get(multiAttributeName).size());
		assertEquals(FORM_VALUE_ONE, ((AbstractFormValue)m.get(multiAttributeName).get(0)).getValue());
		assertEquals(FORM_VALUE_TWO, ((AbstractFormValue)m.get(multiAttributeName).get(1)).getValue());
		
		formService.saveValues(owner, formDefinitionOne, Lists.newArrayList(value1));
		
		// check delete unsaved multiple values
		m = formService.toValueMap(formService.getValues(owner, formDefinitionOne));
		assertEquals(1, m.get(multiAttributeName).size());
		assertEquals(FORM_VALUE_ONE, ((AbstractFormValue)m.get(multiAttributeName).get(0)).getValue());
		
		// checks value map
		Map<String, ? extends List> v = formService.toPersistentValueMap(formService.getValues(owner, formDefinitionOne));
		assertEquals(1, v.get(multiAttributeName).size());
		assertEquals(FORM_VALUE_ONE, v.get(multiAttributeName).get(0));
	}
	
	@Test
	public void testDefaultDefinitionType() {
		assertEquals(IdmIdentity.class.getCanonicalName(), formService.getDefaultDefinitionType(IdmIdentity.class));
	}
	
	@Test
	public void testReadDefaultDefinition() {		
		IdmFormDefinition formDefinition = formService.getDefinition(IdmIdentity.class);
		
		assertNotNull(formDefinition);
		assertEquals(IdmFormDefinitionService.DEFAULT_DEFINITION_NAME, formDefinition.getName());
		assertEquals(PersistentType.TEXT, formDefinition.getMappedAttributeByName(InitDemoData.FORM_ATTRIBUTE_PHONE).getPersistentType());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testOwnerWithoutId() {
		// unpersisted identity
		FormableEntity owner = new IdmIdentity();
		formService.getValues(owner);
	}
	
	@Test(expected = InvalidDataAccessApiUsageException.class)
	public void testUnpersistedOwnerWithId() {
		// unpersisted identity
		FormableEntity owner = new IdmIdentity(UUID.randomUUID());
		formService.getValues(owner);
	}
	
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testReadDefaultDefinitionValue() {
		FormableEntity owner = createTestOwner("test4");
		IdmFormDefinition formDefinition = formService.getDefinition(IdmIdentity.class);
		// save value into default owner and default form definition
		AbstractFormValue value1 = new IdmIdentityFormValue(formDefinition.getMappedAttributeByName(InitDemoData.FORM_ATTRIBUTE_PHONE));
		value1.setValue(FORM_VALUE_ONE);		
		
		formService.saveValues(owner, formDefinition, Lists.newArrayList(value1));
		
		List<AbstractFormValue<FormableEntity>> savedValues = formService.getValues(owner);
		assertEquals(1, savedValues.size());
		assertEquals(FORM_VALUE_ONE, formService.toSinglePersistentValue(savedValues));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test(expected = IllegalArgumentException.class)
	public void testReadDefaultDefinitionValueNotSingle() {
		FormableEntity owner = createTestOwner("test5");
		IdmFormDefinition formDefinition = formService.getDefinition(IdmIdentity.class);
		// save value into default owner and default form definition
		AbstractFormValue value1 = new IdmIdentityFormValue(formDefinition.getMappedAttributeByName(InitDemoData.FORM_ATTRIBUTE_WWW));
		value1.setValue(FORM_VALUE_ONE);
		AbstractFormValue value2 = new IdmIdentityFormValue(formDefinition.getMappedAttributeByName(InitDemoData.FORM_ATTRIBUTE_WWW));
		value2.setValue(FORM_VALUE_TWO);
		
		formService.saveValues(owner, formDefinition, Lists.newArrayList(value1, value2));
		
		List<AbstractFormValue<FormableEntity>> savedValues = formService.getValues(owner);
		assertEquals(2, savedValues.size());
		formService.toSinglePersistentValue(savedValues);
	}
	
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testReadConfidentialFormValue() {
		FormableEntity owner = createTestOwner("test6");
		IdmFormDefinition formDefinition = formService.getDefinition(IdmIdentity.class);
		// save password
		AbstractFormValue value1 = new IdmIdentityFormValue(formDefinition.getMappedAttributeByName(InitDemoData.FORM_ATTRIBUTE_PASSWORD));
		value1.setValue(FORM_VALUE_ONE);
		
		formService.saveValues(owner, formDefinition, Lists.newArrayList(value1));
		
		List<AbstractFormValue<FormableEntity>> savedValues = formService.getValues(owner);
		assertEquals(1, savedValues.size());
		assertEquals(GuardedString.SECRED_PROXY_STRING, formService.toSinglePersistentValue(savedValues));
	}
	
	@Test(expected = ResultCodeException.class)
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testDeleteDefinitionWithFormValues() {
		FormableEntity owner = createTestOwner("test7");
		IdmFormDefinition formDefinition = formService.getDefinition(IdmIdentity.class);
		
		// save password
		AbstractFormValue value1 = new IdmIdentityFormValue(formDefinition.getMappedAttributeByName(InitDemoData.FORM_ATTRIBUTE_PASSWORD));
		value1.setValue(FORM_VALUE_ONE);
		
		formService.saveValues(owner, formDefinition, Lists.newArrayList(value1));
		
		List<AbstractFormValue<FormableEntity>> savedValues = formService.getValues(owner);
		assertEquals(1, savedValues.size());
		
		formDefinitionService.delete(formDefinition);
	}
	
	@Test
	public void testSaveSingleAttributeValues() {
		FormableEntity owner = createTestOwner("test8");
		IdmFormDefinition formDefinition = formService.getDefinition(IdmIdentity.class);
		IdmFormAttribute attribute = formDefinition.getMappedAttributeByName(InitDemoData.FORM_ATTRIBUTE_PHONE);
		// save value
		List<AbstractFormValue<FormableEntity>> attributeValues = formService.saveValues(owner, attribute, Lists.newArrayList(FORM_VALUE_ONE));
		
		assertEquals(1, attributeValues.size());
		assertEquals(FORM_VALUE_ONE, attributeValues.get(0).getValue());
		
		attributeValues = formService.getValues(owner, attribute);
		
		assertEquals(1, attributeValues.size());
		assertEquals(FORM_VALUE_ONE, attributeValues.get(0).getValue());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSaveMultipleAttributeValuesToSingleAttribute() {
		FormableEntity owner = createTestOwner("test9");
		IdmFormDefinition formDefinition = formService.getDefinition(IdmIdentity.class);
		IdmFormAttribute attribute = formDefinition.getMappedAttributeByName(InitDemoData.FORM_ATTRIBUTE_PHONE);
		// save value
		formService.saveValues(owner, attribute, Lists.newArrayList(FORM_VALUE_ONE, FORM_VALUE_TWO));
	}
	
	@Test
	public void testDeleteSingleAttributeValues() {
		FormableEntity owner = createTestOwner("test10");
		IdmFormDefinition formDefinition = formService.getDefinition(IdmIdentity.class);
		IdmFormAttribute attribute = formDefinition.getMappedAttributeByName(InitDemoData.FORM_ATTRIBUTE_PHONE);
		IdmFormAttribute attributeWWW = formDefinition.getMappedAttributeByName(InitDemoData.FORM_ATTRIBUTE_WWW);
		// save value
		formService.saveValues(owner, attribute, Lists.newArrayList(FORM_VALUE_ONE));
		formService.saveValues(owner, attributeWWW, Lists.newArrayList(FORM_VALUE_ONE, FORM_VALUE_TWO));
		//
		List<AbstractFormValue<FormableEntity>> attributeValues = formService.getValues(owner, attribute);		
		assertEquals(1, attributeValues.size());
		assertEquals(FORM_VALUE_ONE, attributeValues.get(0).getValue());
		List<AbstractFormValue<FormableEntity>> attributeWWWValues = formService.getValues(owner, formDefinition, InitDemoData.FORM_ATTRIBUTE_WWW);		
		assertEquals(2, attributeWWWValues.size());
		assertEquals(FORM_VALUE_ONE, attributeWWWValues.get(0).getValue());
		//
		formService.deleteValues(owner, attribute);
		//
		attributeValues = formService.getValues(owner, attribute);		
		assertEquals(0, attributeValues.size());
		attributeWWWValues = formService.getValues(owner, attributeWWW);		
		assertEquals(2, attributeWWWValues.size());
		assertEquals(FORM_VALUE_ONE, attributeWWWValues.get(0).getValue());
	}
	
	@Test
	public void testEditMultipleAttributeValues() {
		FormableEntity owner = createTestOwner("test11");
		// save value
		formService.saveValues(owner, InitDemoData.FORM_ATTRIBUTE_WWW, Lists.newArrayList(FORM_VALUE_ONE, FORM_VALUE_TWO));
		//
		List<AbstractFormValue<FormableEntity>> attributeWWWValues = formService.getValues(owner, InitDemoData.FORM_ATTRIBUTE_WWW);
		assertEquals(2, attributeWWWValues.size());
		assertEquals(FORM_VALUE_ONE, attributeWWWValues.get(0).getValue());
		// update
		formService.saveValues(owner, InitDemoData.FORM_ATTRIBUTE_WWW, Lists.newArrayList(FORM_VALUE_TWO));
		//
		attributeWWWValues = formService.getValues(owner, InitDemoData.FORM_ATTRIBUTE_WWW);		
		assertEquals(1, attributeWWWValues.size());
		assertEquals(FORM_VALUE_TWO, attributeWWWValues.get(0).getValue());
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
