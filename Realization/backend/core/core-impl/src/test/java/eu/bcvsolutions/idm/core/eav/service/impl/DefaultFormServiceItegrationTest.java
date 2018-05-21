package eu.bcvsolutions.idm.core.eav.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitDemoData;
import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmRoleFormValue;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmRoleFormValue_;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Target system tests
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class DefaultFormServiceItegrationTest extends AbstractIntegrationTest {
	
	private final static String FORM_VALUE_ONE = "one";
	private final static String FORM_VALUE_TWO = "two";
	private final static String FORM_VALUE_THREE = "three";
	private final static String FORM_VALUE_FOUR = "four";
	
	@Autowired private ApplicationContext context;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmFormDefinitionService formDefinitionService;	
	@Autowired private IdmRoleRepository roleRepository;
	//
	private FormService formService;
	
	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_USER_1);
		formService = context.getAutowireCapableBeanFactory().createBean(DefaultFormService.class);
	}
	
	@After 
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testFillFormValues() {
		Identifiable owner = createTestOwner("test1");
		//
		// create definition one		
		IdmFormAttributeDto attributeDefinitionOne = new IdmFormAttributeDto();
		attributeDefinitionOne.setCode("name_" + System.currentTimeMillis());
		attributeDefinitionOne.setName(attributeDefinitionOne.getCode());
		attributeDefinitionOne.setPersistentType(PersistentType.TEXT);
		IdmFormDefinitionDto formDefinitionOne = formService.createDefinition(IdmIdentity.class.getCanonicalName(), "t_v1", Lists.newArrayList(attributeDefinitionOne));
		attributeDefinitionOne = formDefinitionOne.getMappedAttributeByCode(attributeDefinitionOne.getCode());
		//
		// create definition two		
		IdmFormAttributeDto attributeDefinitionTwo = new IdmFormAttributeDto();
		attributeDefinitionTwo.setCode("name_" + System.currentTimeMillis());
		attributeDefinitionTwo.setName(attributeDefinitionTwo.getCode());
		attributeDefinitionTwo.setPersistentType(PersistentType.TEXT);		
		IdmFormDefinitionDto formDefinitionTwo = formService.createDefinition(IdmIdentity.class.getCanonicalName(), "t_v2", Lists.newArrayList(attributeDefinitionTwo));
		attributeDefinitionTwo = formDefinitionTwo.getMappedAttributeByCode(attributeDefinitionTwo.getCode());
		//		
		IdmFormValueDto value1 = new IdmFormValueDto(attributeDefinitionOne);
		value1.setValue(FORM_VALUE_ONE);
		
		IdmFormValueDto value2 = new IdmFormValueDto(attributeDefinitionTwo);
		value2.setValue(FORM_VALUE_TWO);
		
		formService.saveValues(owner, formDefinitionOne, Lists.newArrayList(value1));
		formService.saveValues(owner, formDefinitionTwo, Lists.newArrayList(value2));
		
		assertEquals(FORM_VALUE_ONE, formService.getValues(owner, formDefinitionOne).get(0).getStringValue());
		assertEquals(FORM_VALUE_TWO, formService.getValues(owner, formDefinitionTwo).get(0).getStringValue());
		//
		// create second owner
		Identifiable owner2 = createTestOwner("test2");
		
		assertEquals(0, formService.getValues(owner2, formDefinitionOne).size());
		assertEquals(1, formService.getValues(owner, formDefinitionOne).size());
		assertEquals(1, formService.getValues(owner, formDefinitionTwo).size());
		
		identityService.deleteById(owner2.getId());
		
		assertEquals(0, formService.getValues(owner2, formDefinitionOne).size());
		assertEquals(1, formService.getValues(owner, formDefinitionOne).size());
		assertEquals(1, formService.getValues(owner, formDefinitionTwo).size());
		
		formService.deleteValues(owner, formDefinitionOne);		
		assertEquals(0, formService.getValues(owner, formDefinitionOne).size());
		assertEquals(1, formService.getValues(owner, formDefinitionTwo).size());
		assertEquals(FORM_VALUE_TWO, formService.getValues(owner, formDefinitionTwo).get(0).getStringValue());
		
		identityService.deleteById(owner.getId());
		
		assertEquals(0, formService.getValues(owner, formDefinitionOne).size());
		assertEquals(0, formService.getValues(owner, formDefinitionTwo).size());		
	}
	
	/**
	 * Test multi values order and removal
	 */
	@Test
	public void testMultipleValues() {
		Identifiable owner = createTestOwner("test3");
		//
		// create definition with multi parameter	
		IdmFormAttributeDto multiAttribite = new IdmFormAttributeDto();
		String multiAttributeName = "name_" + System.currentTimeMillis();
		multiAttribite.setCode(multiAttributeName);
		multiAttribite.setName(multiAttribite.getCode());
		multiAttribite.setPersistentType(PersistentType.TEXT);
		multiAttribite.setMultiple(true);
		IdmFormDefinitionDto formDefinitionOne = formService.createDefinition(IdmIdentity.class.getCanonicalName(), "t_v3", Lists.newArrayList(multiAttribite));
		multiAttribite = formDefinitionOne.getMappedAttributeByCode(multiAttribite.getCode());
		//
		// fill values
		IdmFormValueDto value1 = new IdmFormValueDto(multiAttribite);
		value1.setValue(FORM_VALUE_ONE);
		value1.setSeq((short) 0);
		
		IdmFormValueDto value2 = new IdmFormValueDto(multiAttribite);
		value2.setValue(FORM_VALUE_TWO);
		value2.setSeq((short) 1);
		
		formService.saveValues(owner, formDefinitionOne, Lists.newArrayList(value1, value2));
		
		Map<String, List<IdmFormValueDto>> m = formService.getFormInstance(owner, formDefinitionOne).toValueMap();
		
		// check order
		assertEquals(2, m.get(multiAttributeName).size());
		assertEquals(FORM_VALUE_ONE, (m.get(multiAttributeName).get(0)).getValue());
		assertEquals(FORM_VALUE_TWO, (m.get(multiAttributeName).get(1)).getValue());
		
		formService.saveValues(owner, formDefinitionOne, Lists.newArrayList(value1));
		
		// check delete unsaved multiple values
		m = formService.getFormInstance(owner, formDefinitionOne).toValueMap();
		assertEquals(1, m.get(multiAttributeName).size());
		assertEquals(FORM_VALUE_ONE, m.get(multiAttributeName).get(0).getValue());
		
		// checks value map
		Map<String, ? extends List<Serializable>> v = formService.getFormInstance(owner, formDefinitionOne).toPersistentValueMap();
		assertEquals(1, v.get(multiAttributeName).size());
		assertEquals(FORM_VALUE_ONE, v.get(multiAttributeName).get(0));
		//
		identityService.deleteById(owner.getId());
	}
	
	@Test
	public void testDefaultDefinitionType() {
		assertEquals(IdmIdentity.class.getCanonicalName(), formService.getDefaultDefinitionType(IdmIdentity.class));
	}
	
	@Test
	public void testReadDefaultDefinition() {		
		IdmFormDefinitionDto formDefinition = formService.getDefinition(IdmIdentity.class);
		
		assertNotNull(formDefinition);
		assertEquals(IdmFormDefinitionService.DEFAULT_DEFINITION_CODE, formDefinition.getCode());
		assertEquals(IdmFormDefinitionService.DEFAULT_DEFINITION_CODE, formDefinition.getName());
		assertEquals(PersistentType.TEXT, formDefinition.getMappedAttributeByCode(InitDemoData.FORM_ATTRIBUTE_PHONE).getPersistentType());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testOwnerWithoutId() {
		// unpersisted identity
		Identifiable owner = new IdmIdentity();
		formService.getValues(owner);
	}
	
	@Test
	public void testUnpersistedOwnerWithId() {
		// unpersisted identity
		Identifiable owner = new IdmIdentity(UUID.randomUUID());
		assertTrue(formService.getValues(owner).isEmpty());
	}
	
	@Test
	public void testReadDefaultDefinitionValue() {
		Identifiable owner = createTestOwner("test4");
		IdmFormDefinitionDto formDefinition = formService.getDefinition(IdmIdentity.class);
		// save value into default owner and default form definition
		IdmFormValueDto value1 = new IdmFormValueDto(formDefinition.getMappedAttributeByCode(InitDemoData.FORM_ATTRIBUTE_PHONE));
		value1.setValue(FORM_VALUE_ONE);		
		
		formService.saveValues(owner, formDefinition, Lists.newArrayList(value1));
		
		IdmFormInstanceDto savedValues = formService.getFormInstance(owner);
		assertEquals(1, savedValues.getValues().size());
		assertEquals(FORM_VALUE_ONE, savedValues.toSinglePersistentValue(InitDemoData.FORM_ATTRIBUTE_PHONE));
		//
		identityService.deleteById(owner.getId());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testReadDefaultDefinitionValueNotSingle() {
		Identifiable owner = createTestOwner("test5");
		try {
			IdmFormDefinitionDto formDefinition = formService.getDefinition(IdmIdentity.class);
			// save value into default owner and default form definition
			IdmFormValueDto value1 = new IdmFormValueDto(formDefinition.getMappedAttributeByCode(InitDemoData.FORM_ATTRIBUTE_WWW));
			value1.setValue(FORM_VALUE_ONE);
			IdmFormValueDto value2 = new IdmFormValueDto(formDefinition.getMappedAttributeByCode(InitDemoData.FORM_ATTRIBUTE_WWW));
			value2.setValue(FORM_VALUE_TWO);
			
			formService.saveValues(owner, formDefinition, Lists.newArrayList(value1, value2));
			
			IdmFormInstanceDto savedValues = formService.getFormInstance(owner);
			assertEquals(2, savedValues.getValues().size());
			savedValues.toSinglePersistentValue(InitDemoData.FORM_ATTRIBUTE_WWW);
		} finally {
			identityService.deleteById(owner.getId());
		}
	}
	
	@Test
	public void testReadConfidentialFormValue() {
		Identifiable owner = createTestOwner("test6");
		IdmFormDefinitionDto formDefinition = formService.getDefinition(IdmIdentity.class);
		// save password
		IdmFormValueDto value1 = new IdmFormValueDto(formDefinition.getMappedAttributeByCode(InitDemoData.FORM_ATTRIBUTE_PASSWORD));
		value1.setValue(FORM_VALUE_ONE);
		
		formService.saveValues(owner, formDefinition, Lists.newArrayList(value1));
		
		IdmFormInstanceDto savedValues = formService.getFormInstance(owner);
		assertEquals(1, savedValues.getValues().size());
		assertEquals(GuardedString.SECRED_PROXY_STRING, savedValues.toSinglePersistentValue(InitDemoData.FORM_ATTRIBUTE_PASSWORD));
		//
		identityService.deleteById(owner.getId());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testDeleteDefinitionWithFormValues() {
		Identifiable owner = createTestOwner("test7");
		IdmFormDefinitionDto formDefinition = formService.getDefinition(IdmIdentity.class);
		
		// save password
		IdmFormValueDto value1 = new IdmFormValueDto(formDefinition.getMappedAttributeByCode(InitDemoData.FORM_ATTRIBUTE_PASSWORD));
		value1.setValue(FORM_VALUE_ONE);
		
		formService.saveValues(owner, formDefinition, Lists.newArrayList(value1));
		
		List<IdmFormValueDto> savedValues = formService.getValues(owner);
		assertEquals(1, savedValues.size());
		
		formDefinitionService.delete(formDefinition);
		//
		identityService.deleteById(owner.getId());
	}
	
	@Test
	public void testSaveSingleAttributeValues() {
		Identifiable owner = createTestOwner("test8");
		IdmFormDefinitionDto formDefinition = formService.getDefinition(IdmIdentity.class);
		IdmFormAttributeDto attribute = formDefinition.getMappedAttributeByCode(InitDemoData.FORM_ATTRIBUTE_PHONE);
		// save value
		List<IdmFormValueDto> attributeValues = formService.saveValues(owner, attribute, Lists.newArrayList(FORM_VALUE_ONE));
		
		assertEquals(1, attributeValues.size());
		assertEquals(FORM_VALUE_ONE, attributeValues.get(0).getValue());
		
		attributeValues = formService.getValues(owner, attribute);
		
		assertEquals(1, attributeValues.size());
		assertEquals(FORM_VALUE_ONE, attributeValues.get(0).getValue());
		//
		identityService.deleteById(owner.getId());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSaveMultipleAttributeValuesToSingleAttribute() {
		Identifiable owner = createTestOwner("test9");
		IdmFormDefinitionDto formDefinition = formService.getDefinition(IdmIdentity.class);
		IdmFormAttributeDto attribute = formDefinition.getMappedAttributeByCode(InitDemoData.FORM_ATTRIBUTE_PHONE);
		// save value
		formService.saveValues(owner, attribute, Lists.newArrayList(FORM_VALUE_ONE, FORM_VALUE_TWO));
		//
		identityService.deleteById(owner.getId());
	}
	
	@Test
	public void testDeleteSingleAttributeValues() {
		Identifiable owner = createTestOwner("test10");
		IdmFormDefinitionDto formDefinition = formService.getDefinition(IdmIdentity.class);
		IdmFormAttributeDto attribute = formDefinition.getMappedAttributeByCode(InitDemoData.FORM_ATTRIBUTE_PHONE);
		IdmFormAttributeDto attributeWWW = formDefinition.getMappedAttributeByCode(InitDemoData.FORM_ATTRIBUTE_WWW);
		// save value
		formService.saveValues(owner, attribute, Lists.newArrayList(FORM_VALUE_ONE));
		formService.saveValues(owner, attributeWWW, Lists.newArrayList(FORM_VALUE_ONE, FORM_VALUE_TWO));
		//
		List<IdmFormValueDto> attributeValues = formService.getValues(owner, attribute);		
		assertEquals(1, attributeValues.size());
		assertEquals(FORM_VALUE_ONE, attributeValues.get(0).getValue());
		List<IdmFormValueDto> attributeWWWValues = formService.getValues(owner, formDefinition, InitDemoData.FORM_ATTRIBUTE_WWW);		
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
		//
		identityService.deleteById(owner.getId());
	}
	
	@Test
	public void testEditMultipleAttributeValues() {
		Identifiable owner = createTestOwner("test11");
		// save value
		formService.saveValues(owner, InitDemoData.FORM_ATTRIBUTE_WWW, Lists.newArrayList(FORM_VALUE_ONE, FORM_VALUE_TWO));
		//
		List<IdmFormValueDto> attributeWWWValues = formService.getValues(owner, InitDemoData.FORM_ATTRIBUTE_WWW);
		assertEquals(2, attributeWWWValues.size());
		assertEquals(FORM_VALUE_ONE, attributeWWWValues.get(0).getValue());
		// update
		formService.saveValues(owner, InitDemoData.FORM_ATTRIBUTE_WWW, Lists.newArrayList(FORM_VALUE_TWO));
		//
		attributeWWWValues = formService.getValues(owner, InitDemoData.FORM_ATTRIBUTE_WWW);		
		assertEquals(1, attributeWWWValues.size());
		assertEquals(FORM_VALUE_TWO, attributeWWWValues.get(0).getValue());
		//
		identityService.deleteById(owner.getId());
	}
	
	@Test
	public void testFindOwnersByMultiStringAttributeValue() {
		Identifiable owner = createTestOwner("test12");
		Identifiable ownerTwo = createTestOwner("test13");
		Identifiable ownerThree = createTestOwner("test14");
		IdmFormDefinitionDto formDefinition = formService.getDefinition(owner.getClass());
		IdmFormAttributeDto attribute = formDefinition.getMappedAttributeByCode(InitDemoData.FORM_ATTRIBUTE_WWW);
		// save values
		formService.saveValues(owner, attribute, Lists.newArrayList(FORM_VALUE_ONE, FORM_VALUE_TWO));
		formService.saveValues(ownerTwo, attribute, Lists.newArrayList(FORM_VALUE_THREE, FORM_VALUE_TWO));
		formService.saveValues(ownerThree, attribute, Lists.newArrayList(FORM_VALUE_FOUR, FORM_VALUE_FOUR));
		//
		Page<? extends Identifiable> owners = formService.findOwners(owner.getClass(), attribute, FORM_VALUE_ONE, null);
		//
		assertEquals(1, owners.getTotalElements());
		assertEquals(owner.getId(), owners.getContent().get(0).getId());
		//
		owners = formService.findOwners(owner.getClass(), attribute.getCode(), FORM_VALUE_TWO, null);
		assertEquals(2, owners.getTotalElements());
		//
		owners = formService.findOwners(owner.getClass(), attribute, FORM_VALUE_FOUR, null);
		assertEquals(1, owners.getTotalElements());
		//
		identityService.deleteById(owner.getId());
		identityService.deleteById(ownerTwo.getId());
		identityService.deleteById(ownerThree.getId());
	}
	
	@Test
	public void testFindOwnersByStringAttributeValue() {
		IdmIdentityDto owner = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto ownerTwo = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto ownerThree = getHelper().createIdentity((GuardedString) null);
		IdmFormDefinitionDto formDefinition = formService.getDefinition(IdmIdentity.class);
		IdmFormAttributeDto attribute = formDefinition.getMappedAttributeByCode(InitDemoData.FORM_ATTRIBUTE_PHONE);
		// save values
		formService.saveValues(owner.getId(), IdmIdentity.class, attribute, Lists.newArrayList(FORM_VALUE_ONE));
		formService.saveValues(ownerTwo.getId(), IdmIdentity.class, attribute, Lists.newArrayList(FORM_VALUE_TWO));
		formService.saveValues(ownerThree.getId(), IdmIdentity.class, attribute, Lists.newArrayList(FORM_VALUE_FOUR));
		//
		Page<? extends Identifiable> owners = formService.findOwners(IdmIdentity.class, attribute, FORM_VALUE_ONE, null);
		//
		assertEquals(1, owners.getTotalElements());
		assertEquals(owner.getId(), owners.getContent().get(0).getId());
		//
		owners = formService.findOwners(IdmIdentity.class, attribute.getCode(), FORM_VALUE_TWO, null);
		assertEquals(1, owners.getTotalElements());
		//
		owners = formService.findOwners(IdmIdentity.class, attribute, FORM_VALUE_FOUR, null);
		assertEquals(1, owners.getTotalElements());
		//
		identityService.delete(owner);
		identityService.delete(ownerTwo);
		identityService.delete(ownerThree);
	}
	
	@Test
	public void testFindTreeNodesByNullAttributeValue() {
		IdmTreeNodeDto owner = getHelper().createTreeNode();
		IdmFormDefinitionDto formDefinition = formService.getDefinition(IdmTreeNode.class);
		IdmFormAttributeDto attribute = formDefinition.getFormAttributes().get(0);
		// save values
		formService.saveValues(owner.getId(), IdmTreeNode.class, attribute, Lists.newArrayList(FORM_VALUE_ONE));
		//
		Page<? extends Identifiable> owners = formService.findOwners(IdmTreeNode.class, attribute, FORM_VALUE_ONE, null);
		//
		assertEquals(1, owners.getTotalElements());
		assertEquals(owner.getId(), owners.getContent().get(0).getId());
		//
		owners = formService.findOwners(IdmTreeNode.class, attribute.getCode(), null, null);
		assertEquals(0, owners.getTotalElements());
	}
	
	@Test
	public void testFindOwnersByDateAttributeValue() {
		Identifiable owner = createTestOwner("test15");
		Identifiable ownerTwo = createTestOwner("test16");
		
		IdmFormDefinitionDto formDefinition = formService.getDefinition(owner.getClass());
		IdmFormAttributeDto attribute = formDefinition.getMappedAttributeByCode(InitDemoData.FORM_ATTRIBUTE_DATETIME);
		// save values
		DateTime now = new DateTime();
		DateTime tomorrow =  now.plusDays(1);
		formService.saveValues(owner, attribute, Lists.newArrayList(now));
		formService.saveValues(ownerTwo, attribute, Lists.newArrayList(tomorrow));
		//
		Page<? extends Identifiable> owners = formService.findOwners(owner.getClass(), InitDemoData.FORM_ATTRIBUTE_DATETIME, now, null);
		//
		assertEquals(1, owners.getTotalElements());
		assertEquals(owner.getId(), owners.getContent().get(0).getId());
		//
		owners = formService.findOwners(owner.getClass(), InitDemoData.FORM_ATTRIBUTE_DATETIME, tomorrow, null);
		//
		assertEquals(1, owners.getTotalElements());
		assertEquals(ownerTwo.getId(), owners.getContent().get(0).getId());
		//
		identityService.deleteById(owner.getId());
		identityService.deleteById(ownerTwo.getId());
	}
	
	@Test
	public void testFindOwnersByUuidAttributeValue() {
		IdmIdentityDto owner = getHelper().createIdentity((GuardedString) null);;
		IdmIdentityDto ownerTwo = getHelper().createIdentity((GuardedString) null);;
		
		IdmFormDefinitionDto formDefinition = formService.getDefinition(owner.getClass());
		IdmFormAttributeDto attribute = formDefinition.getMappedAttributeByCode(InitDemoData.FORM_ATTRIBUTE_UUID);
		// save values
		UUID one = UUID.randomUUID();
		UUID two = UUID.randomUUID();
		formService.saveValues(owner, attribute, Lists.newArrayList(one));
		formService.saveValues(ownerTwo, attribute, Lists.newArrayList(two));
		//
		Page<? extends Identifiable> owners = formService.findOwners(owner.getClass(), InitDemoData.FORM_ATTRIBUTE_UUID, one, null);
		//
		assertEquals(1, owners.getTotalElements());
		assertEquals(owner.getId(), owners.getContent().get(0).getId());
		//
		owners = formService.findOwners(owner.getClass(), InitDemoData.FORM_ATTRIBUTE_UUID, two, null);
		//
		assertEquals(1, owners.getTotalElements());
		assertEquals(ownerTwo.getId(), owners.getContent().get(0).getId());
		//
		identityService.deleteById(owner.getId());
		identityService.deleteById(ownerTwo.getId());
	}
	
	@Test
	@Transactional
	public void testFindOwnersByShortTextAttributeValue() {
		IdmIdentityDto owner = getHelper().createIdentity((GuardedString) null);;
		IdmIdentityDto ownerTwo = getHelper().createIdentity((GuardedString) null);;
		
		IdmFormAttributeDto shortTextAttr = new IdmFormAttributeDto();
		String shortTextAttrName = getHelper().createName();
		shortTextAttr.setCode(shortTextAttrName);
		shortTextAttr.setName(shortTextAttrName);
		shortTextAttr.setPersistentType(PersistentType.SHORTTEXT);
		IdmFormAttributeDto attribute = formService.saveAttribute(owner.getClass(), shortTextAttr);
		// save values
		String one = "one";
		String two = "two";
		formService.saveValues(owner, attribute, Lists.newArrayList(one));
		formService.saveValues(ownerTwo, attribute, Lists.newArrayList(two));
		//
		Page<? extends Identifiable> owners = formService.findOwners(owner.getClass(), shortTextAttrName, one, null);
		//
		assertEquals(1, owners.getTotalElements());
		assertEquals(owner.getId(), owners.getContent().get(0).getId());
		//
		owners = formService.findOwners(owner.getClass(), shortTextAttrName, two, null);
		//
		assertEquals(1, owners.getTotalElements());
		assertEquals(ownerTwo.getId(), owners.getContent().get(0).getId());
	}
	
	@Test
	public void testFindAttribute() {
		IdmFormAttributeDto attribute = formService.getAttribute(IdmIdentity.class, InitDemoData.FORM_ATTRIBUTE_DATETIME);
		//
		assertNotNull(attribute);
		assertEquals(PersistentType.DATETIME, attribute.getPersistentType());
	}
	
	@Test
	public void testFindAttributeNotExist() {
		IdmFormAttributeDto attribute = formService.getAttribute(IdmIdentity.class, "notExist_test");
		//
		assertNull(attribute);
	}
	
	@Test
	public void testSaveAttribute() {
		IdmFormAttributeDto attribute = new IdmFormAttributeDto();
		String attributeName = "name_" + System.currentTimeMillis();
		attribute.setCode(attributeName);
		attribute.setName(attribute.getCode());
		attribute.setPersistentType(PersistentType.TEXT);
		//
		attribute = formService.saveAttribute(IdmIdentity.class, attribute);
		//
		IdmFormAttributeDto savedAttr = formService.getAttribute(IdmIdentity.class, attribute.getCode());
		//
		assertNotNull(savedAttr);
		assertEquals(PersistentType.TEXT, savedAttr.getPersistentType());
		assertEquals(formService.getDefinition(IdmIdentity.class).getId(), savedAttr.getFormDefinition());
		//
		formService.deleteAttribute(attribute);
		//
		savedAttr = formService.getAttribute(IdmIdentity.class, attribute.getCode());
		//
		assertNull(savedAttr);
	}
	
	@Test
	public void findOwnerByCriteria() {
		IdmRoleDto owner = getHelper().createRole();
		IdmRoleDto ownerTwo = getHelper().createRole();
		
		IdmFormDefinitionDto formDefinition = formService.getDefinition(IdmRole.class);
		IdmFormAttributeDto attribute = formDefinition.getFormAttributes().get(0);
		//
		formService.saveValues(owner.getId(), IdmRole.class, attribute, Lists.newArrayList("test"));
		formService.saveValues(ownerTwo.getId(), IdmRole.class, attribute, Lists.newArrayList("test2"));
		
		Specification<IdmRole> criteria = new Specification<IdmRole>() {
			public Predicate toPredicate(Root<IdmRole> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				Subquery<IdmRoleFormValue> subquery = query.subquery(IdmRoleFormValue.class);
				Root<IdmRoleFormValue> subRoot = subquery.from(IdmRoleFormValue.class);
				subquery.select(subRoot);
				
				Predicate predicate = builder.and(
						builder.equal(subRoot.get(IdmRoleFormValue_.owner), root),
						builder.equal(subRoot.get(IdmRoleFormValue_.formAttribute).get(IdmFormAttribute_.id), attribute.getId()),
						builder.equal(subRoot.get(IdmRoleFormValue_.stringValue), "test"));				
				subquery.where(predicate);
				//
				return query.where(builder.exists(subquery)).getRestriction();
			}
		};
		List<IdmRole> roles = roleRepository.findAll(criteria, (Pageable) null).getContent();
		assertEquals(1, roles.size());
		assertEquals(owner.getId(), roles.get(0).getId());
	}
	
	@Test
	public void testSaveValuesByOwnerId() {
		Identifiable owner = createTestOwner("test8");
		IdmFormDefinitionDto formDefinition = formService.getDefinition(IdmIdentity.class);
		IdmFormAttributeDto attribute = formDefinition.getMappedAttributeByCode(InitDemoData.FORM_ATTRIBUTE_PHONE);
		// save value
		List<IdmFormValueDto> attributeValues = formService.saveValues(
				(UUID) owner.getId(), owner.getClass(), attribute, Lists.newArrayList(FORM_VALUE_ONE));
		
		assertEquals(1, attributeValues.size());
		assertEquals(FORM_VALUE_ONE, attributeValues.get(0).getValue());
		
		List<IdmFormValueDto> getValues = formService.getValues((UUID) owner.getId(), owner.getClass(), attribute);
		
		assertEquals(1, getValues.size());
		assertEquals(FORM_VALUE_ONE, ((IdmFormValueDto) getValues.get(0)).getValue());
		//
		identityService.deleteById(owner.getId());
	}
	
	@Test
	public void testFindDefinitionByDtoClass() {
		IdmFormDefinitionDto definitionByEntityClass = formService.getDefinition(IdmIdentity.class, null);
		IdmFormDefinitionDto definitionByDtoClass = formService.getDefinition(IdmIdentityDto.class, null);
		//
		Assert.assertNotNull(definitionByEntityClass);
		Assert.assertEquals(definitionByEntityClass, definitionByDtoClass);
	}
	
	/**
	 * #1051 - Change value persistent type, when single attribute is saved and attribute persistent type changed
	 */
	@Test
	public void testChangeAttributePersistentType() {
		Identifiable owner = createTestOwner("user");
		//
		// create definition with attribute
		IdmFormAttributeDto attribute = new IdmFormAttributeDto();
		String attributeName = getHelper().createName();
		attribute.setCode(attributeName);
		attribute.setName(attribute.getCode());
		attribute.setPersistentType(PersistentType.SHORTTEXT);
		IdmFormDefinitionDto formDefinitionOne = formService.createDefinition(IdmIdentity.class.getCanonicalName(), "t_v3", Lists.newArrayList(attribute));
		attribute = formDefinitionOne.getMappedAttributeByCode(attribute.getCode());
		//
		// fill values		
		formService.saveValues(owner, attribute, Lists.newArrayList(FORM_VALUE_ONE));
		
		Map<String, List<IdmFormValueDto>> m = formService.getFormInstance(owner, formDefinitionOne).toValueMap();
		
		// check value and persistent type
		assertEquals(1, m.get(attributeName).size());
		assertEquals(FORM_VALUE_ONE, (m.get(attributeName).get(0)).getValue());
		assertEquals(attribute.getPersistentType(), (m.get(attributeName).get(0)).getPersistentType());
		//
		// change attribute persistentn type
		attribute.setPersistentType(PersistentType.TEXT);
		attribute = formService.saveAttribute(attribute);
		// update value
		formService.saveValues(owner, attribute, Lists.newArrayList(FORM_VALUE_TWO));
		m = formService.getFormInstance(owner, formDefinitionOne).toValueMap();
		//
		// check value and persistent type
		assertEquals(1, m.get(attributeName).size());
		assertEquals(FORM_VALUE_TWO, (m.get(attributeName).get(0)).getValue());
		assertEquals(attribute.getPersistentType(), (m.get(attributeName).get(0)).getPersistentType());
		//
		identityService.deleteById(owner.getId());
	}
	
	private Identifiable createTestOwner(String name) {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername(name + "_" + System.currentTimeMillis());
		identity.setPassword(new GuardedString("heslo"));
		identity.setFirstName("Test");
		identity.setLastName("Identity");
		identity = identityService.save(identity);
		return identityService.get(identity.getId());
	}
}
