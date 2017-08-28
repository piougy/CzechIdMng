package eu.bcvsolutions.idm.acc.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.filter.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.entity.SysSystemFormValue;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.repository.IdmFormAttributeRepository;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.eav.service.api.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.ic.api.IcConfigurationProperty;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.impl.IcConnectorInstanceImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationFacade;
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
	
	@Autowired private TestHelper helper;
	@Autowired private SysSystemService systemService;	
	@Autowired private IdmFormDefinitionService formDefinitionService;	
	@Autowired private IdmFormAttributeRepository formAttributeDefinitionRepository;	
	@Autowired private FormService formService;	
	@Autowired private IcConfigurationFacade icConfigurationAggregatorService;
	@Autowired private SysSchemaObjectClassService schemaObjectClassService;
	@Autowired private SysSchemaAttributeService schemaAttributeService;
	@Autowired private IdmRoleRepository roleRepository;
	@Autowired private SysRoleSystemService roleSystemService;
	@Autowired private SysSystemMappingService systemMappingService;
	@Autowired private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired private SysRoleSystemAttributeService roleSystemAttributeService;
	@Autowired private AccAccountService accountService;
	@Autowired private SysSystemEntityService systemEntityService;
	
	@Before
	public void login() {
		loginAsAdmin(InitTestData.TEST_USER_1);
	}
	
	@After 
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testReferentialIntegrity() {
		SysSystem system = new SysSystem();
		String systemName = "t_s_" + System.currentTimeMillis();
		system.setName(systemName);
		system = systemService.save(system);
		// object class
		SysSchemaObjectClass objectClass = new SysSchemaObjectClass();
		objectClass.setSystem(system);
		objectClass.setObjectClassName("obj_class");
		objectClass = schemaObjectClassService.save(objectClass);	
		SchemaObjectClassFilter objectClassFilter = new SchemaObjectClassFilter();
		objectClassFilter.setSystemId(system.getId());
		// schema attribute
		SysSchemaAttribute schemaAttribute = new SysSchemaAttribute();
		schemaAttribute.setObjectClass(objectClass);
		schemaAttribute.setName("name");
		schemaAttribute.setClassType("class");
		schemaAttributeService.save(schemaAttribute);
		SchemaAttributeFilter schemaAttributeFilter = new SchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());	
		// system entity handling
		SysSystemMapping systemMapping = new SysSystemMapping();
		systemMapping.setName("default_" + System.currentTimeMillis());
		systemMapping.setObjectClass(objectClass);
		systemMapping.setOperationType(SystemOperationType.PROVISIONING);
		systemMapping.setEntityType(SystemEntityType.IDENTITY);
		systemMapping = systemMappingService.save(systemMapping);
		SystemMappingFilter entityHandlingFilter = new SystemMappingFilter();
		entityHandlingFilter.setSystemId(system.getId());
		// schema attribute handling
		SysSystemAttributeMapping schemaAttributeHandling = new SysSystemAttributeMapping();
		schemaAttributeHandling.setSchemaAttribute(schemaAttribute);
		schemaAttributeHandling.setSystemMapping(systemMapping);
		schemaAttributeHandling.setName("name");
		schemaAttributeHandling.setIdmPropertyName("name");
		systemAttributeMappingService.save(schemaAttributeHandling);
		SystemAttributeMappingFilter schemaAttributeHandlingFilter = new SystemAttributeMappingFilter(); 
		schemaAttributeHandlingFilter.setSystemId(system.getId());		
		// role system
		IdmRoleDto role = helper.createRole();
		SysRoleSystem roleSystem = new SysRoleSystem();
		roleSystem.setSystem(system);
		roleSystem.setRole(roleRepository.findOne(role.getId()));
		roleSystem.setSystemMapping(systemMapping);
		roleSystemService.save(roleSystem);
		RoleSystemFilter roleSystemFilter = new RoleSystemFilter();
		roleSystemFilter.setRoleId(role.getId());
		// role system attributes
		SysRoleSystemAttribute roleSystemAttribute = new SysRoleSystemAttribute();
		roleSystemAttribute.setRoleSystem(roleSystem);
		roleSystemAttribute.setSystemAttributeMapping(schemaAttributeHandling);
		roleSystemAttribute.setName("name");
		roleSystemAttribute.setIdmPropertyName("name");
		roleSystemAttribute = roleSystemAttributeService.save(roleSystemAttribute);
		
		assertEquals(systemName, systemService.getByCode(systemName).getName());
		assertEquals(1, schemaObjectClassService.find(objectClassFilter, null).getTotalElements());
		assertEquals(1, schemaAttributeService.find(schemaAttributeFilter, null).getTotalElements());
		assertEquals(1, systemMappingService.find(entityHandlingFilter, null).getTotalElements());
		assertEquals(1, systemAttributeMappingService.find(schemaAttributeHandlingFilter, null).getTotalElements());
		assertEquals(1, roleSystemService.find(roleSystemFilter, null).getTotalElements());
		assertNotNull(roleSystemAttributeService.get(roleSystemAttribute.getId()));
		
		systemService.delete(system);
		
		assertNull(systemService.getByCode(systemName));
		assertEquals(0, schemaObjectClassService.find(objectClassFilter, null).getTotalElements());
		assertEquals(0, schemaAttributeService.find(schemaAttributeFilter, null).getTotalElements());
		assertEquals(0, systemMappingService.find(entityHandlingFilter, null).getTotalElements());
		assertEquals(0, systemAttributeMappingService.find(schemaAttributeHandlingFilter, null).getTotalElements());
		assertEquals(0, roleSystemService.find(roleSystemFilter, null).getTotalElements());
		assertNull(roleSystemAttributeService.get(roleSystemAttribute.getId()));
	}
	
	@Test(expected = ResultCodeException.class)
	public void testReferentialIntegrityAccountExists() {
		SysSystem system = new SysSystem();
		String systemName = "t_s_" + System.currentTimeMillis();
		system.setName(systemName);
		system = systemService.save(system);
		// account
		AccAccount account = new AccAccount();
		account.setSystem(system);
		account.setUid("test_uid_" + System.currentTimeMillis());
		account.setAccountType(AccountType.PERSONAL);
		account = accountService.save(account);
		
		systemService.delete(system);
	}
	
	@Test(expected = ResultCodeException.class)
	public void testReferentialIntegritySystemEntityExists() {
		SysSystem system = new SysSystem();
		String systemName = "t_s_" + System.currentTimeMillis();
		system.setName(systemName);
		system = systemService.save(system);
		// system entity
		SysSystemEntity systemEntity = new SysSystemEntity();
		systemEntity.setSystem(system);
		systemEntity.setEntityType(SystemEntityType.IDENTITY);
		systemEntity.setUid("se_uid_" + System.currentTimeMillis());
		systemEntityService.save(systemEntity);
		
		systemService.delete(system);
	}
	
	/**
	 * Test add and delete extended attributes to owner
	 */
	@Test
	public void testFormAttributes() {
		// create owner
		SysSystem system = new SysSystem();
		system.setName(SYSTEM_NAME_ONE);
		systemService.save(system);	
		SysSystem systemOne = systemService.getByCode(SYSTEM_NAME_ONE);		
		assertEquals(SYSTEM_NAME_ONE, systemOne.getName());
		//
		// create definition one
		IdmFormDefinition formDefinitionOne = new IdmFormDefinition();
		formDefinitionOne.setType(SysSystem.class.getCanonicalName());
		formDefinitionOne.setCode("v1");
		formDefinitionOne = formDefinitionService.save(formDefinitionOne);
		
		IdmFormAttribute attributeDefinitionOne = new IdmFormAttribute();
		attributeDefinitionOne.setFormDefinition(formDefinitionOne);
		attributeDefinitionOne.setCode("name_" + System.currentTimeMillis());
		attributeDefinitionOne.setName(attributeDefinitionOne.getCode());
		attributeDefinitionOne.setPersistentType(PersistentType.TEXT);			
		attributeDefinitionOne = formAttributeDefinitionRepository.save(attributeDefinitionOne);
		formDefinitionOne = formDefinitionService.get(formDefinitionOne.getId());
		//
		// create definition two
		IdmFormDefinition formDefinitionTwo = new IdmFormDefinition();
		formDefinitionTwo.setType(SysSystem.class.getCanonicalName());
		formDefinitionTwo.setCode("v2");
		formDefinitionTwo = formDefinitionService.save(formDefinitionTwo);
		
		IdmFormAttribute attributeDefinitionTwo = new IdmFormAttribute();
		attributeDefinitionTwo.setFormDefinition(formDefinitionTwo);
		attributeDefinitionTwo.setCode("name_" + System.currentTimeMillis());
		attributeDefinitionTwo.setName(attributeDefinitionTwo.getCode());
		attributeDefinitionTwo.setPersistentType(PersistentType.TEXT);			
		attributeDefinitionTwo = formAttributeDefinitionRepository.save(attributeDefinitionTwo);
		formDefinitionTwo = formDefinitionService.get(formDefinitionTwo.getId());
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
		systemTwo = systemService.save(systemTwo);
		
		assertEquals(0, formService.getValues(systemTwo, formDefinitionOne).size());
		assertEquals(0, formService.getValues(systemTwo, formDefinitionTwo).size());
		assertEquals(1, formService.getValues(systemOne, formDefinitionOne).size());
		assertEquals(1, formService.getValues(systemOne, formDefinitionTwo).size());
		
		systemService.delete(systemTwo);
		
		assertEquals(0, formService.getValues(systemTwo, formDefinitionOne).size());
		assertEquals(0, formService.getValues(systemTwo, formDefinitionTwo).size());
		assertEquals(1, formService.getValues(systemOne, formDefinitionOne).size());
		assertEquals(1, formService.getValues(systemOne, formDefinitionTwo).size());
		
		formService.deleteValues(systemOne, formDefinitionOne);		
		assertEquals(0, formService.getValues(systemOne, formDefinitionOne).size());
		assertEquals("test2", formService.getValues(systemOne, formDefinitionTwo).get(0).getStringValue());
		
		systemService.delete(systemOne);
		
		assertEquals(0, formService.getValues(systemOne, formDefinitionOne).size());
		assertEquals(0, formService.getValues(systemOne, formDefinitionTwo).size());
	}
	
	@Test
	public void testCreateConnectorConfiguration() {
		// TODO: test system will be moved here, after UI eav form implementation
		@SuppressWarnings("deprecation")
		IcConnectorKey connectorKey = systemService.getTestConnectorKey();
		
		// create connector instance impl with connector key
		IcConnectorInstance connectorInstance = new IcConnectorInstanceImpl(null, connectorKey, false);
		
		IcConnectorConfiguration conf = icConfigurationAggregatorService.getConnectorConfiguration(connectorInstance);
		
		IdmFormDefinition savedFormDefinition = systemService.getConnectorFormDefinition(connectorInstance);
		
		assertEquals(conf.getConfigurationProperties().getProperties().size(), savedFormDefinition.getFormAttributes().size());
		assertEquals(conf.getConfigurationProperties().getProperties().get(3).getDisplayName(), savedFormDefinition.getFormAttributes().get(3).getName());
	}
	
	@Test
	public void testFillConnectorConfiguration() {
		// create owner
		@SuppressWarnings("deprecation")
		SysSystem system =  systemService.createTestSystem();		
		IcConnectorConfiguration connectorConfiguration = systemService.getConnectorConfiguration(system);		
		assertEquals(15, connectorConfiguration.getConfigurationProperties().getProperties().size());
		//
		// check all supported data types
		// TODO: add all supported types
		Integer checked = 0;
		for(IcConfigurationProperty property : connectorConfiguration.getConfigurationProperties().getProperties()) {
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
		systemService.save(system);
		formService.getValues(system);
	}
	
	@Test
	public void checkSystemValid() {
		// create test system
		SysSystem system = helper.createSystem(TestResource.TABLE_NAME);
		// do test system
		systemService.checkSystem(system);
	}
	
	@Test(expected = RuntimeException.class)
	public void checkSystemUnValid() {
		// create test system
		SysSystem system =  helper.createSystem(TestResource.TABLE_NAME);
		// set wrong password
		IdmFormDefinition savedFormDefinition = systemService.getConnectorFormDefinition(system.getConnectorInstance());
		List<AbstractFormValue<SysSystem>> values = formService.getValues(system, savedFormDefinition);
		AbstractFormValue<SysSystem> changeLogColumn = values.stream().filter(value -> {return "password".equals(value.getFormAttribute().getCode());}).findFirst().get();
		formService.saveValues(system, changeLogColumn.getFormAttribute(), ImmutableList.of("wrongPassword"));
		
		// do test system
		systemService.checkSystem(system);
	}
	
	@Test
	public void duplicateSystem(){
		// create test system
		SysSystem system = helper.createTestResourceSystem(true);
		SchemaAttributeFilter schemaAttributeFilter = new SchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		// Number of schema attributes on original system
		int numberOfSchemaAttributesOrig = schemaAttributeService.find(schemaAttributeFilter, null).getContent().size();
		SysSystemMapping mappingOrig = helper.getDefaultMapping(system);
		// Number of mapping attributes on original system
		int numberOfMappingAttributesOrig = systemAttributeMappingService.findBySystemMapping(mappingOrig).size();
		
		SysSystem duplicatedSystem = systemService.duplicate(system.getId());
		// check duplicate
		systemService.checkSystem(duplicatedSystem);
		
		Assert.assertNotEquals(system.getId(), duplicatedSystem.getId());
		
		schemaAttributeFilter.setSystemId(duplicatedSystem.getId());
		// Number of schema attributes on duplicated system
		int numberOfSchemaAttributes = schemaAttributeService.find(schemaAttributeFilter, null).getContent().size();
		Assert.assertEquals(numberOfSchemaAttributesOrig, numberOfSchemaAttributes);
		
		SysSystemMapping mapping = helper.getDefaultMapping(duplicatedSystem);
		// Number of mapping attributes on duplicated system
		int numberOfMappingAttributes = systemAttributeMappingService.findBySystemMapping(mapping).size();
		Assert.assertEquals(numberOfMappingAttributesOrig, numberOfMappingAttributes);
	}

}
