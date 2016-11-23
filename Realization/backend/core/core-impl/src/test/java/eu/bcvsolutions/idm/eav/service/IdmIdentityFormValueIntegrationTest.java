package eu.bcvsolutions.idm.eav.service;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityFormValue;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.eav.domain.PersistentType;
import eu.bcvsolutions.idm.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.service.api.FormService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Target system tests
 * 
 * @author Radek Tomiška
 *
 */
public class IdmIdentityFormValueIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired
	private IdmIdentityService identityService;
	
	@Autowired
	private FormService formService;
	
	@Test
	public void testFillFormValues() {
		IdmIdentity identity = new IdmIdentity();
		identity.setUsername("test_" + System.currentTimeMillis());
		identity.setPassword("heslo".getBytes(Charsets.UTF_8));
		identity.setFirstName("Test");
		identity.setLastName("Identity");
		identity = identityService.save(identity);	
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
		IdmIdentityFormValue value1 = new IdmIdentityFormValue(attributeDefinitionOne);
		value1.setValue("test1");
		
		IdmIdentityFormValue value2 = new IdmIdentityFormValue(attributeDefinitionTwo);
		value2.setValue("test2");
		
		formService.saveValues(identity, formDefinitionOne, Lists.newArrayList(value1));
		formService.saveValues(identity, formDefinitionTwo, Lists.newArrayList(value2));
		
		List<AbstractFormValue<IdmIdentity>> savedValues = formService.getValues(identity);
		
		assertEquals(2, savedValues.size());
		assertEquals("test1", formService.getValues(identity, formDefinitionOne).get(0).getStringValue());
		assertEquals("test2", formService.getValues(identity, formDefinitionTwo).get(0).getStringValue());
		//
		// create second owner
		IdmIdentity otherIdentity = new IdmIdentity();
		otherIdentity.setUsername("test2_" + System.currentTimeMillis());
		otherIdentity.setPassword("heslo".getBytes(Charsets.UTF_8));
		otherIdentity.setFirstName("Test2");
		otherIdentity.setLastName("Identity");
		otherIdentity = identityService.save(otherIdentity);
		
		assertEquals(0, formService.getValues(otherIdentity).size());
		assertEquals(2, formService.getValues(identity).size());
		
		identityService.delete(otherIdentity);
		
		assertEquals(0, formService.getValues(otherIdentity).size());
		assertEquals(2, formService.getValues(identity).size());
		
		formService.deleteValues(identity, formDefinitionOne);		
		assertEquals(0, formService.getValues(identity, formDefinitionOne).size());
		assertEquals(1, formService.getValues(identity).size());
		assertEquals("test2", formService.getValues(identity, formDefinitionTwo).get(0).getStringValue());
		
		identityService.delete(identity);
		
		assertEquals(0, formService.getValues(identity).size());
	}	
}
