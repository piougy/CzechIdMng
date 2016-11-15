package eu.bcvsolutions.idm.acc.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemFormValue;
import eu.bcvsolutions.idm.eav.domain.PersistentType;
import eu.bcvsolutions.idm.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.repository.IdmFormAttributeDefinitionRepository;
import eu.bcvsolutions.idm.eav.service.FormService;
import eu.bcvsolutions.idm.eav.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Target system tests
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultSysSystemServiceTest extends AbstractIntegrationTest {
	
	private static final String SYSTEM_NAME_ONE = "test_system_one_" + System.currentTimeMillis();
	private static final String SYSTEM_NAME_TWO = "test_system_two_" + System.currentTimeMillis();
	
	@Autowired
	private SysSystemService sysSystemService;
	
	@Autowired
	private IdmFormDefinitionService formDefinitionService;
	
	@Autowired
	private IdmFormAttributeDefinitionRepository formAttributeDefinitionRepository;
	
	@Autowired
	private FormService formService;

	/**
	 * Test add and delete extended attributes to owner
	 */
	@Test
	public void testFormAttributes() {
		// create owner
		SysSystem system = new SysSystem();
		system.setName(SYSTEM_NAME_ONE);		
		sysSystemService.save(system);		
		SysSystem systemOne = sysSystemService.getByName(SYSTEM_NAME_ONE);		
		assertEquals(SYSTEM_NAME_ONE, systemOne.getName());
		//
		// create definition one
		IdmFormDefinition formDefinitionOne = new IdmFormDefinition();
		formDefinitionOne.setType(SysSystem.class.getCanonicalName());
		formDefinitionOne.setName("v1");
		formDefinitionOne = formDefinitionService.save(formDefinitionOne);
		
		IdmFormAttribute attributeDefinitionOne = new IdmFormAttribute();
		attributeDefinitionOne.setFormDefinition(formDefinitionOne);
		attributeDefinitionOne.setName("name_" + System.currentTimeMillis());
		attributeDefinitionOne.setDisplayName(attributeDefinitionOne.getName());
		attributeDefinitionOne.setPersistentType(PersistentType.TEXT);			
		attributeDefinitionOne = formAttributeDefinitionRepository.save(attributeDefinitionOne);
		//
		// create definition two
		IdmFormDefinition formDefinitionTwo = new IdmFormDefinition();
		formDefinitionTwo.setType(SysSystem.class.getCanonicalName());
		formDefinitionTwo.setName("v2");
		formDefinitionTwo = formDefinitionService.save(formDefinitionTwo);
		
		IdmFormAttribute attributeDefinitionTwo = new IdmFormAttribute();
		attributeDefinitionTwo.setFormDefinition(formDefinitionTwo);
		attributeDefinitionTwo.setName("name_" + System.currentTimeMillis());
		attributeDefinitionTwo.setDisplayName(attributeDefinitionTwo.getName());
		attributeDefinitionTwo.setPersistentType(PersistentType.TEXT);			
		attributeDefinitionTwo = formAttributeDefinitionRepository.save(attributeDefinitionTwo);
		//
		// fill extended attributes
		List<SysSystemFormValue> values = new ArrayList<>();
		
		SysSystemFormValue value1 = new SysSystemFormValue();
		value1.setOwner(systemOne);
		value1.setFormAttribute(attributeDefinitionOne);
		value1.setStringValue("test1");
		values.add(value1);
		
		SysSystemFormValue value2 = new SysSystemFormValue();
		value2.setOwner(systemOne);
		value2.setFormAttribute(attributeDefinitionTwo);
		value2.setStringValue("test2");
		values.add(value2);
		
		formService.saveValues(systemOne, values);
		
		List<AbstractFormValue<SysSystem>> savedValues = formService.getValues(systemOne);
		
		assertEquals(2, savedValues.size());
		assertEquals("test1", formService.getValues(systemOne, formDefinitionOne).get(0).getStringValue());
		assertEquals("test2", formService.getValues(systemOne, formDefinitionTwo).get(0).getStringValue());
		//
		// create second owner
		SysSystem systemTwo = new SysSystem();
		systemTwo.setName(SYSTEM_NAME_TWO);		
		systemTwo = sysSystemService.save(systemTwo);
		
		assertEquals(0, formService.getValues(systemTwo).size());
		assertEquals(2, formService.getValues(systemOne).size());
		
		sysSystemService.delete(systemTwo);
		
		assertEquals(0, formService.getValues(systemTwo).size());
		assertEquals(2, formService.getValues(systemOne).size());
		
		formService.deleteValues(systemOne, formDefinitionOne);		
		assertEquals(0, formService.getValues(systemOne, formDefinitionOne).size());
		assertEquals("test2", formService.getValues(systemOne, formDefinitionTwo).get(0).getStringValue());
		
		sysSystemService.delete(systemOne);
		
		assertEquals(0, formService.getValues(systemOne).size());
		
	}
}
