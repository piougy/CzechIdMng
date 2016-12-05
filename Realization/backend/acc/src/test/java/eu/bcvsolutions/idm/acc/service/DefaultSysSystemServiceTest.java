package eu.bcvsolutions.idm.acc.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemFormValue;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.eav.domain.PersistentType;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.repository.IdmFormAttributeRepository;
import eu.bcvsolutions.idm.eav.service.api.FormService;
import eu.bcvsolutions.idm.eav.service.api.IdmFormDefinitionService;
import eu.bcvsolutions.idm.icf.api.IcfConfigurationProperty;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.service.api.IcfConfigurationFacade;
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
	private IdmFormAttributeRepository formAttributeDefinitionRepository;
	
	@Autowired
	private FormService formService;
	
	@Autowired
	private IcfConfigurationFacade icfConfigurationAggregatorService;
	
	@Before
	public void login() {
		loginAsAdmin(InitTestData.TEST_USER_1);
	}
	
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
		SysSystemFormValue value1 = new SysSystemFormValue(attributeDefinitionOne);
		value1.setValue("test1");
		
		SysSystemFormValue value2 = new SysSystemFormValue(attributeDefinitionTwo);
		value2.setValue("test2");
		
		formService.saveValues(systemOne, formDefinitionOne, Lists.newArrayList(value1));
		formService.saveValues(systemOne, formDefinitionTwo, Lists.newArrayList(value2));
		
		assertEquals("test1", formService.getValues(systemOne, formDefinitionOne).get(0).getStringValue());
		assertEquals("test2", formService.getValues(systemOne, formDefinitionTwo).get(0).getStringValue());
		assertEquals("test2", formService.getValues(systemOne, formDefinitionTwo, attributeDefinitionTwo.getName()).get(0).getValue());
		//
		// create second owner
		SysSystem systemTwo = new SysSystem();
		systemTwo.setName(SYSTEM_NAME_TWO);		
		systemTwo = sysSystemService.save(systemTwo);
		
		assertEquals(0, formService.getValues(systemTwo, formDefinitionOne).size());
		assertEquals(0, formService.getValues(systemTwo, formDefinitionTwo).size());
		assertEquals(1, formService.getValues(systemOne, formDefinitionOne).size());
		assertEquals(1, formService.getValues(systemOne, formDefinitionTwo).size());
		
		sysSystemService.delete(systemTwo);
		
		assertEquals(0, formService.getValues(systemTwo, formDefinitionOne).size());
		assertEquals(0, formService.getValues(systemTwo, formDefinitionTwo).size());
		assertEquals(1, formService.getValues(systemOne, formDefinitionOne).size());
		assertEquals(1, formService.getValues(systemOne, formDefinitionTwo).size());
		
		formService.deleteValues(systemOne, formDefinitionOne);		
		assertEquals(0, formService.getValues(systemOne, formDefinitionOne).size());
		assertEquals("test2", formService.getValues(systemOne, formDefinitionTwo).get(0).getStringValue());
		
		sysSystemService.delete(systemOne);
		
		assertEquals(0, formService.getValues(systemOne, formDefinitionOne).size());
		assertEquals(0, formService.getValues(systemOne, formDefinitionTwo).size());
	}
	
	@Test
	public void testCreateConnectorConfiguration() {
		// TODO: test system will be moved here, after UI eav form implementation
		IcfConnectorKey connectorKey = sysSystemService.getTestConnectorKey();
		
		IcfConnectorConfiguration conf = icfConfigurationAggregatorService.getConnectorConfiguration(connectorKey);
		
		IdmFormDefinition savedFormDefinition = sysSystemService.getConnectorFormDefinition(connectorKey);
		
		assertEquals(conf.getConfigurationProperties().getProperties().size(), savedFormDefinition.getFormAttributes().size());
		assertEquals(conf.getConfigurationProperties().getProperties().get(3).getDisplayName(), savedFormDefinition.getFormAttributes().get(3).getDisplayName());
	}
	
	@Test
	public void testFillConnectorConfiguration() {
		// create owner
		SysSystem system =  sysSystemService.createTestSystem();		
		IcfConnectorConfiguration connectorConfiguration = sysSystemService.getConnectorConfiguration(system);		
		assertEquals(15, connectorConfiguration.getConfigurationProperties().getProperties().size());
		//
		// check all supported data types
		// TODO: add all supported types
		Integer checked = 0;
		for(IcfConfigurationProperty property : connectorConfiguration.getConfigurationProperties().getProperties()) {
			switch(property.getName()) {
				case "host": {
					assertEquals("localhost", property.getValue());
					checked++;
					break;
				}
				case "password": {
					assertEquals(new org.identityconnectors.common.security.GuardedString("idmadmin".toCharArray()), property.getValue());
					checked++;
					break;
				}
				case "rethrowAllSQLExceptions": {
					assertEquals(true, property.getValue());
					checked++;
					break;
				}
			}
		};		
		assertEquals(Integer.valueOf(3), checked);
	}
	
	@Test
	public void testDefaultFormDefinitionNotExists() {
		assertNull(formService.getDefinition(SysSystem.class));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testReadValuesFromDefaultFormDefinitionNotExists() {
		SysSystem system = new SysSystem();
		system.setName(SYSTEM_NAME_ONE + "_" + System.currentTimeMillis());
		sysSystemService.save(system);
		formService.getValues(system);
	}
	

}
