package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncIdentityConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.test.TestHelper;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
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
	@Autowired private IdmFormAttributeService formAttributeService;	
	@Autowired private FormService formService;	
	@Autowired private IcConfigurationFacade icConfigurationAggregatorService;
	@Autowired private SysSchemaObjectClassService schemaObjectClassService;
	@Autowired private SysSchemaAttributeService schemaAttributeService;
	@Autowired private SysRoleSystemService roleSystemService;
	@Autowired private SysSystemMappingService systemMappingService;
	@Autowired private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired private SysRoleSystemAttributeService roleSystemAttributeService;
	@Autowired private AccAccountService accountService;
	@Autowired private SysSystemEntityService systemEntityService;
	@Autowired private SysSyncConfigService syncConfigService;
	@Autowired private SysSystemAttributeMappingService schemaAttributeMappingService;
	@Autowired private SysProvisioningOperationService provisioningOperationService;
	
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
		SysSystemDto system = new SysSystemDto();
		String systemName = "t_s_" + System.currentTimeMillis();
		system.setName(systemName);
		system = systemService.save(system);
		// object class
		SysSchemaObjectClassDto objectClass = new SysSchemaObjectClassDto();
		objectClass.setSystem(system.getId());
		objectClass.setObjectClassName("obj_class");
		objectClass = schemaObjectClassService.save(objectClass);	
		SysSchemaObjectClassFilter objectClassFilter = new SysSchemaObjectClassFilter();
		objectClassFilter.setSystemId(system.getId());
		// schema attribute
		SysSchemaAttributeDto schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setObjectClass(objectClass.getId());
		schemaAttribute.setName("name");
		schemaAttribute.setClassType("class");
		schemaAttribute = schemaAttributeService.save(schemaAttribute);
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());	
		// system entity handling
		SysSystemMappingDto systemMapping = new SysSystemMappingDto();
		systemMapping.setName("default_" + System.currentTimeMillis());
		systemMapping.setObjectClass(objectClass.getId());
		systemMapping.setOperationType(SystemOperationType.PROVISIONING);
		systemMapping.setEntityType(SystemEntityType.IDENTITY);
		systemMapping = systemMappingService.save(systemMapping);
		SysSystemMappingFilter entityHandlingFilter = new SysSystemMappingFilter();
		entityHandlingFilter.setSystemId(system.getId());
		// schema attribute handling
		SysSystemAttributeMappingDto schemaAttributeHandling = new SysSystemAttributeMappingDto();
		schemaAttributeHandling.setSchemaAttribute(schemaAttribute.getId());
		schemaAttributeHandling.setSystemMapping(systemMapping.getId());
		schemaAttributeHandling.setName("name");
		schemaAttributeHandling.setIdmPropertyName("name");
		schemaAttributeHandling = systemAttributeMappingService.save(schemaAttributeHandling);
		SysSystemAttributeMappingFilter schemaAttributeHandlingFilter = new SysSystemAttributeMappingFilter(); 
		schemaAttributeHandlingFilter.setSystemId(system.getId());		
		// role system
		IdmRoleDto role = helper.createRole();
		SysRoleSystemDto roleSystem = new SysRoleSystemDto();
		roleSystem.setSystem(system.getId());
		roleSystem.setRole(role.getId());
		roleSystem.setSystemMapping(systemMapping.getId());
		roleSystem = roleSystemService.save(roleSystem);
		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setRoleId(role.getId());
		// role system attributes
		SysRoleSystemAttributeDto roleSystemAttribute = new SysRoleSystemAttributeDto();
		roleSystemAttribute.setRoleSystem(roleSystem.getId());
		roleSystemAttribute.setSystemAttributeMapping(schemaAttributeHandling.getId());
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
		SysSystemDto system = new SysSystemDto();
		String systemName = "t_s_" + System.currentTimeMillis();
		system.setName(systemName);
		system = systemService.save(system);
		// account
		AccAccountDto account = new AccAccountDto();
		account.setSystem(system.getId());
		account.setUid("test_uid_" + System.currentTimeMillis());
		account.setAccountType(AccountType.PERSONAL);
		account = accountService.save(account);
		
		systemService.delete(system);
	}
	
	@Test
	public void testReferentialIntegritySystemEntityExists() {
		SysSystemDto system = new SysSystemDto();
		String systemName = "t_s_" + System.currentTimeMillis();
		system.setName(systemName);
		system = systemService.save(system);
		// system entity
		SysSystemEntityDto systemEntity = new SysSystemEntityDto();
		systemEntity.setSystem(system.getId());
		systemEntity.setEntityType(SystemEntityType.IDENTITY);
		systemEntity.setUid("se_uid_" + System.currentTimeMillis());
		systemEntity = systemEntityService.save(systemEntity);
		
		systemService.delete(system);
		
		assertNull(systemService.getByCode(system.getCode()));
		assertNull(systemEntityService.get(systemEntity.getId()));
	}
	
	@Test(expected = ResultCodeException.class)
	public void testReferentialIntegrityProvisioningOperationExists() {
		SysSystemDto system = new SysSystemDto();
		String systemName = "t_s_" + System.currentTimeMillis();
		system.setName(systemName);
		system = systemService.save(system);

		// system entity
		SysSystemEntityDto systemEntity = new SysSystemEntityDto();
		systemEntity.setUid("test");
		systemEntity.setSystem(system.getId());
		systemEntity.setEntityType(SystemEntityType.IDENTITY);		
		systemEntity = systemEntityService.save(systemEntity);
		
		SysProvisioningOperationDto provisioningOperation = new SysProvisioningOperationDto();
		provisioningOperation.setSystem(system.getId());
		provisioningOperation.setEntityType(SystemEntityType.IDENTITY);
		provisioningOperation.setOperationType(ProvisioningEventType.CREATE);
		provisioningOperation.setSystemEntity(systemEntity.getId());
		provisioningOperation.setEntityIdentifier(UUID.randomUUID());
		provisioningOperation.setProvisioningContext(new ProvisioningContext());
		provisioningOperation.setResult(new OperationResult());
		provisioningOperationService.save(provisioningOperation);
		//
		systemService.delete(system);
	}
	
	/**
	 * Test add and delete extended attributes to owner
	 */
	@Test
	public void testFormAttributes() {
		// create owner
		SysSystemDto system = new SysSystemDto();
		system.setName(SYSTEM_NAME_ONE);
		system = systemService.save(system);	
		SysSystemDto systemOne = systemService.getByCode(SYSTEM_NAME_ONE);		
		assertEquals(SYSTEM_NAME_ONE, systemOne.getName());
		//
		// create definition one
		IdmFormDefinitionDto formDefinitionOne = new IdmFormDefinitionDto();
		formDefinitionOne.setType(SysSystem.class.getCanonicalName());
		formDefinitionOne.setCode("v1");
		formDefinitionOne = formDefinitionService.save(formDefinitionOne);
		
		IdmFormAttributeDto attributeDefinitionOne = new IdmFormAttributeDto();
		attributeDefinitionOne.setFormDefinition(formDefinitionOne.getId());
		attributeDefinitionOne.setCode("name_" + System.currentTimeMillis());
		attributeDefinitionOne.setName(attributeDefinitionOne.getCode());
		attributeDefinitionOne.setPersistentType(PersistentType.TEXT);			
		attributeDefinitionOne = formAttributeService.save(attributeDefinitionOne);
		formDefinitionOne = formDefinitionService.get(formDefinitionOne.getId());
		//
		// create definition two
		IdmFormDefinitionDto formDefinitionTwo = new IdmFormDefinitionDto();
		formDefinitionTwo.setType(SysSystem.class.getCanonicalName());
		formDefinitionTwo.setCode("v2");
		formDefinitionTwo = formDefinitionService.save(formDefinitionTwo);
		
		IdmFormAttributeDto attributeDefinitionTwo = new IdmFormAttributeDto();
		attributeDefinitionTwo.setFormDefinition(formDefinitionTwo.getId());
		attributeDefinitionTwo.setCode("name_" + System.currentTimeMillis());
		attributeDefinitionTwo.setName(attributeDefinitionTwo.getCode());
		attributeDefinitionTwo.setPersistentType(PersistentType.TEXT);			
		attributeDefinitionTwo = formAttributeService.save(attributeDefinitionTwo);
		formDefinitionTwo = formDefinitionService.get(formDefinitionTwo.getId());
		//		
		IdmFormValueDto value1 = new IdmFormValueDto(attributeDefinitionOne);
		value1.setValue("test1");
		
		IdmFormValueDto value2 = new IdmFormValueDto(attributeDefinitionTwo);
		value2.setValue("test2");
		
		formService.saveValues(system, formDefinitionOne, Lists.newArrayList(value1));
		formService.saveValues(system, formDefinitionTwo, Lists.newArrayList(value2));
		
		assertEquals("test1", formService.getValues(system, formDefinitionOne).get(0).getStringValue());
		assertEquals("test2", formService.getValues(system, formDefinitionTwo).get(0).getStringValue());
		assertEquals("test2", formService.getValues(system, formDefinitionTwo, attributeDefinitionTwo.getName()).get(0).getValue());
		//
		// create second owner
		SysSystemDto systemTwo = new SysSystemDto();
		systemTwo.setName(SYSTEM_NAME_TWO);		
		systemTwo = systemService.save(systemTwo);

		assertEquals(0, formService.getValues(systemTwo, formDefinitionOne).size());
		assertEquals(0, formService.getValues(systemTwo, formDefinitionTwo).size());
		assertEquals(1, formService.getValues(system, formDefinitionOne).size());
		assertEquals(1, formService.getValues(system, formDefinitionTwo).size());
		
		systemService.delete(systemTwo);
		
		assertEquals(1, formService.getValues(system, formDefinitionOne).size());
		assertEquals(1, formService.getValues(system, formDefinitionTwo).size());
		
		formService.deleteValues(system, formDefinitionOne);		
		assertEquals(0, formService.getValues(system, formDefinitionOne).size());
		assertEquals("test2", formService.getValues(system, formDefinitionTwo).get(0).getStringValue());
		
		systemService.delete(system);
	}
	
	@Test
	public void testCreateConnectorConfiguration() {
		// TODO: test system will be moved here, after UI eav form implementation
		@SuppressWarnings("deprecation")
		IcConnectorKey connectorKey = systemService.getTestConnectorKey();
		
		// create connector instance impl with connector key
		IcConnectorInstance connectorInstance = new IcConnectorInstanceImpl(null, connectorKey, false);
		
		IcConnectorConfiguration conf = icConfigurationAggregatorService.getConnectorConfiguration(connectorInstance);
		
		IdmFormDefinitionDto savedFormDefinition = systemService.getConnectorFormDefinition(connectorInstance);
		
		assertEquals(conf.getConfigurationProperties().getProperties().size(), savedFormDefinition.getFormAttributes().size());
		assertEquals(conf.getConfigurationProperties().getProperties().get(3).getDisplayName(), savedFormDefinition.getFormAttributes().get(3).getName());
	}
	
	@Test
	public void testFillConnectorConfiguration() {
		// create owner
		@SuppressWarnings("deprecation")
		SysSystemDto system =  systemService.createTestSystem();		
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
		SysSystemDto system = new SysSystemDto();
		system.setName(SYSTEM_NAME_ONE + "_" + System.currentTimeMillis());
		system = systemService.save(system);
		formService.getValues(system);
	}
	
	@Test
	public void checkSystemValid() {
		// create test system
		SysSystemDto system = helper.createSystem(TestResource.TABLE_NAME);
		// do test system
		systemService.checkSystem(system);
	}
	
	@Test(expected = RuntimeException.class)
	public void checkSystemUnValid() {
		// create test system
		SysSystemDto system =  helper.createSystem(TestResource.TABLE_NAME);
		
		// set wrong password
		formService.saveValues(system, "password", ImmutableList.of("wrongPassword"));
		
		// do test system
		systemService.checkSystem(system);
	}
	
	@Test
	public void duplicateSystem(){
		// create test system
		SysSystemDto system = helper.createTestResourceSystem(true);
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		// Number of schema attributes on original system
		int numberOfSchemaAttributesOrig = schemaAttributeService.find(schemaAttributeFilter, null).getContent().size();
		SysSystemMappingDto mappingOrig = helper.getDefaultMapping(system);
		// Number of mapping attributes on original system
		int numberOfMappingAttributesOrig = systemAttributeMappingService.findBySystemMapping(mappingOrig).size();
		
		SysSystemDto duplicatedSystem = systemService.duplicate(system.getId());
		// check duplicate
		systemService.checkSystem(duplicatedSystem);
		
		Assert.assertNotEquals(system.getId(), duplicatedSystem.getId());
		
		schemaAttributeFilter.setSystemId(duplicatedSystem.getId());
		// Number of schema attributes on duplicated system
		int numberOfSchemaAttributes = schemaAttributeService.find(schemaAttributeFilter, null).getContent().size();
		Assert.assertEquals(numberOfSchemaAttributesOrig, numberOfSchemaAttributes);
		
		SysSystemMappingDto mapping = helper.getDefaultMapping(duplicatedSystem);
		// Number of mapping attributes on duplicated system
		int numberOfMappingAttributes = systemAttributeMappingService.findBySystemMapping(mapping).size();
		Assert.assertEquals(numberOfMappingAttributesOrig, numberOfMappingAttributes);
	}

	@Test
	public void duplicateSystemWithSynchronization(){
		String syncName = "test-sync-config";
		// create test system
		SysSystemDto system = helper.createTestResourceSystem(true);
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		// Number of schema attributes on original system
		int numberOfSchemaAttributesOrig = schemaAttributeService.find(schemaAttributeFilter, null).getContent().size();
		SysSystemMappingDto mappingOrig = helper.getDefaultMapping(system);
		// Number of mapping attributes on original system
		int numberOfMappingAttributesOrig = systemAttributeMappingService.findBySystemMapping(mappingOrig).size();
		
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(mappingOrig.getId());
		
		List<SysSystemAttributeMappingDto> attributes = schemaAttributeMappingService.find(attributeMappingFilter, null)
				.getContent();
		SysSystemAttributeMappingDto nameAttribute = attributes.stream().filter(attribute -> {
			return attribute.getName().equals(TestHelper.ATTRIBUTE_MAPPING_NAME);
		}).findFirst().get();

		SysSystemAttributeMappingDto firstNameAttribute = attributes.stream().filter(attribute -> {
			return attribute.getName().equals(TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME);
		}).findFirst().get();
		
		SysSystemAttributeMappingDto emailAttribute = attributes.stream().filter(attribute -> {
			return attribute.getName().equals(TestHelper.ATTRIBUTE_MAPPING_EMAIL);
		}).findFirst().get();
		
		// create synchronization config
		AbstractSysSyncConfigDto syncConfigDuplicate = new SysSyncIdentityConfigDto();
		syncConfigDuplicate.setCustomFilter(true);
		syncConfigDuplicate.setSystemMapping(mappingOrig.getId());
		syncConfigDuplicate.setCorrelationAttribute(nameAttribute.getId());
		syncConfigDuplicate.setTokenAttribute(firstNameAttribute.getId());
		syncConfigDuplicate.setFilterAttribute(emailAttribute.getId());
		syncConfigDuplicate.setReconciliation(true);
		syncConfigDuplicate.setName(syncName);
		syncConfigDuplicate.setLinkedAction(SynchronizationLinkedActionType.IGNORE);
		syncConfigDuplicate.setUnlinkedAction(SynchronizationUnlinkedActionType.IGNORE);
		syncConfigDuplicate.setMissingEntityAction(SynchronizationMissingEntityActionType.CREATE_ENTITY);
		syncConfigDuplicate.setMissingAccountAction(ReconciliationMissingAccountActionType.IGNORE);

		syncConfigDuplicate = syncConfigService.save(syncConfigDuplicate);
		
		SysSystemDto duplicatedSystem = systemService.duplicate(system.getId());
		// check duplicate
		systemService.checkSystem(duplicatedSystem);
		
		Assert.assertNotEquals(system.getId(), duplicatedSystem.getId());
		
		schemaAttributeFilter.setSystemId(duplicatedSystem.getId());
		// Number of schema attributes on duplicated system
		int numberOfSchemaAttributes = schemaAttributeService.find(schemaAttributeFilter, null).getContent().size();
		Assert.assertEquals(numberOfSchemaAttributesOrig, numberOfSchemaAttributes);
		
		SysSystemMappingDto mapping = helper.getDefaultMapping(duplicatedSystem);
		// Number of mapping attributes on duplicated system
		int numberOfMappingAttributes = systemAttributeMappingService.findBySystemMapping(mapping).size();
		Assert.assertEquals(numberOfMappingAttributesOrig, numberOfMappingAttributes);
		
		// check synchronization config
		SysSyncConfigFilter syncFilter = new SysSyncConfigFilter();
		syncFilter.setSystemId(duplicatedSystem.getId());
		List<AbstractSysSyncConfigDto> configs = syncConfigService.find(syncFilter, null).getContent();
		Assert.assertEquals(1, configs.size());
		
		
		Assert.assertEquals(1, configs.size());
		AbstractSysSyncConfigDto configNew = configs.get(0);
		Assert.assertFalse(configNew.isEnabled());
		
		Assert.assertTrue(configNew.isReconciliation());
		Assert.assertEquals(syncName, configNew.getName());
		Assert.assertTrue(configNew.isCustomFilter());

		Assert.assertEquals(syncConfigDuplicate.getLinkedAction(), configNew.getLinkedAction());
		Assert.assertEquals(syncConfigDuplicate.getUnlinkedAction(), configNew.getUnlinkedAction());
		Assert.assertEquals(syncConfigDuplicate.getMissingEntityAction(), configNew.getMissingEntityAction());
		Assert.assertEquals(syncConfigDuplicate.getMissingAccountAction(), configNew.getMissingAccountAction());

		SysSystemAttributeMappingDto correlationAtt = schemaAttributeMappingService.get(configNew.getCorrelationAttribute());
		SysSystemAttributeMappingDto tokenAtt = schemaAttributeMappingService.get(configNew.getTokenAttribute());
		SysSystemAttributeMappingDto filterAtt = schemaAttributeMappingService.get(configNew.getFilterAttribute());
		
		Assert.assertEquals(nameAttribute.getName(), correlationAtt.getName());
		Assert.assertEquals(nameAttribute.getIdmPropertyName(), correlationAtt.getIdmPropertyName());
		
		Assert.assertEquals(firstNameAttribute.getName(), tokenAtt.getName());
		Assert.assertEquals(firstNameAttribute.getIdmPropertyName(), tokenAtt.getIdmPropertyName());
		
		Assert.assertEquals(emailAttribute.getName(), filterAtt.getName());
		Assert.assertEquals(emailAttribute.getIdmPropertyName(), filterAtt.getIdmPropertyName());
	}
	
}
