package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.ProvisioningAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.impl.IcConnectorObjectImpl;
import eu.bcvsolutions.idm.ic.impl.IcObjectClassImpl;
import eu.bcvsolutions.idm.ic.impl.IcUidAttributeImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Tests:
 * - disabled system provisioning
 * - readonly system provisioning
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultProvisioningExecutorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired
	private TestHelper helper;
	@Autowired
	private ApplicationContext context;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysSystemEntityService systemEntityService;
	@Autowired
	private SysSystemAttributeMappingService attributeMappingService;
	@Autowired
	private IcConnectorFacade connectorFacade;
	@Autowired
	private ConfidentialStorage confidentialStorage;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private SysSchemaObjectClassService schemaObjectClassService;
	//	
	private SysProvisioningOperationService sysProvisioningOperationService;
	private ProvisioningExecutor provisioningExecutor;
	private SysSystem system = null;
	private SysSystemMappingDto systemMapping = null;
	private SysSystemAttributeMappingDto nameAttributeMapping = null;
	private SysSystemAttributeMappingDto firstNameAttributeMapping = null;
	private SysSystemAttributeMappingDto lastNameAttributeMapping = null;
	private SysSystemAttributeMappingDto passwordAttributeMapping = null;
	
	@Before
	public void init() {	
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		sysProvisioningOperationService = context.getAutowireCapableBeanFactory().createBean(DefaultSysProvisioningOperationService.class);
		provisioningExecutor = context.getAutowireCapableBeanFactory().createBean(DefaultProvisioningExecutor.class);
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	private void initSystem() {
		// prepare test system
		system = helper.createTestResourceSystem(true);
		systemMapping = helper.getDefaultMapping(system);
		//
		nameAttributeMapping = attributeMappingService.findBySystemMappingAndName(systemMapping.getId(), TestHelper.ATTRIBUTE_MAPPING_NAME);
		firstNameAttributeMapping = attributeMappingService.findBySystemMappingAndName(systemMapping.getId(), TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME);
		lastNameAttributeMapping = attributeMappingService.findBySystemMappingAndName(systemMapping.getId(), TestHelper.ATTRIBUTE_MAPPING_LASTNAME);
		passwordAttributeMapping = attributeMappingService.findBySystemMappingAndName(systemMapping.getId(), TestHelper.ATTRIBUTE_MAPPING_PASSWORD);
	}
	
	private Map<ProvisioningAttributeDto, Object> createAccountObject(SysSystemEntity systemEntity) {
		Map<ProvisioningAttributeDto, Object> accoutObject = new HashMap<>();		
		accoutObject.put(new ProvisioningAttributeDto(
				schemaAttributeService.get(nameAttributeMapping.getSchemaAttribute()).getName(),
				nameAttributeMapping.getStrategyType()), systemEntity.getUid());
		//
		accoutObject.put(new ProvisioningAttributeDto(
				schemaAttributeService.get(firstNameAttributeMapping.getSchemaAttribute()).getName(),
				firstNameAttributeMapping.getStrategyType()), "firstOne");
		//
		accoutObject.put(new ProvisioningAttributeDto(
				schemaAttributeService.get(lastNameAttributeMapping.getSchemaAttribute()).getName(),
				lastNameAttributeMapping.getStrategyType()), "lastOne");
		//
		accoutObject.put(new ProvisioningAttributeDto(
				schemaAttributeService.get(passwordAttributeMapping.getSchemaAttribute()).getName(),
				passwordAttributeMapping.getStrategyType()), new GuardedString("password"));
		//
		return accoutObject;
	}
	
	@Test
	public void testGreenLineAccountProvisioning() {
		initSystem();
		//
		// create test provisioning context
		ProvisioningContext context = new ProvisioningContext();
		SysSystemEntity systemEntity = new SysSystemEntity("oneUid", SystemEntityType.IDENTITY);
		systemEntity.setSystem(system);
		systemEntity.setWish(true);
		systemEntity = systemEntityService.save(systemEntity);
		//
		ProvisioningAttributeDto passwordAttributeMappingKey = new ProvisioningAttributeDto(
				schemaAttributeService.get(passwordAttributeMapping.getSchemaAttribute()).getName(),
				AttributeMappingStrategyType.SET);
		//
		ProvisioningAttributeDto firstNameAttributeMappingKey = new ProvisioningAttributeDto(
				schemaAttributeService.get(firstNameAttributeMapping.getSchemaAttribute()).getName(),
				AttributeMappingStrategyType.SET);
		//
		ProvisioningAttributeDto lastNameAttributeMappingKey = new ProvisioningAttributeDto(
				schemaAttributeService.get(lastNameAttributeMapping.getSchemaAttribute()).getName(),
				AttributeMappingStrategyType.SET);
		//
		Map<ProvisioningAttributeDto, Object> accoutObject = createAccountObject(systemEntity);
		context.setAccountObject(accoutObject);
		GuardedString password = (GuardedString) accoutObject.get(passwordAttributeMappingKey);
		//
		// publish event
		
		IcObjectClass objectClass = new IcObjectClassImpl(schemaObjectClassService.get(systemMapping.getObjectClass()).getObjectClassName());
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(null, objectClass, null);
		SysProvisioningOperation.Builder operationBuilder = new SysProvisioningOperation.Builder()
				.setOperationType(ProvisioningOperationType.CREATE)
				.setSystemEntity(systemEntity)
				.setProvisioningContext(new ProvisioningContext(accoutObject, connectorObject));
		provisioningExecutor.execute(operationBuilder.build());
		//
		// check target account
		IcUidAttribute uidAttribute = new IcUidAttributeImpl(null, systemEntity.getUid(), null);
		IcConnectorObject existsConnectorObject = connectorFacade.readObject(
				system.getConnectorInstance(), 
				systemService.getConnectorConfiguration(system), 
				objectClass, 
				uidAttribute);
		//
		assertNotNull(existsConnectorObject);
		assertEquals(systemEntity.getUid(), existsConnectorObject.getUidValue());
		assertEquals(accoutObject.get(firstNameAttributeMappingKey), 
				existsConnectorObject.getAttributeByName(firstNameAttributeMapping.getName()).getValue());
		assertEquals(accoutObject.get(lastNameAttributeMappingKey), 
				existsConnectorObject.getAttributeByName(lastNameAttributeMapping.getName()).getValue());
		// authenticate for password check
		IcUidAttribute attribute = connectorFacade.authenticateObject(
				system.getConnectorInstance(), 
				systemService.getConnectorConfiguration(system), 
				objectClass,
				systemEntity.getUid(), password);
		assertNotNull(attribute);
		assertEquals(systemEntity.getUid(), attribute.getUidValue());
		//
		// check system entity
		systemEntity = systemEntityService.get(systemEntity.getId());
		assertFalse(systemEntity.isWish());
	}
	
	@Test
	public void testDisabledSystem() {
		initSystem();
		system.setDisabled(true);
		system = systemService.save(system);
		//
		// create test provisioning context
		ProvisioningContext context = new ProvisioningContext();
		String systemEntityUid = "twoUid";
		SysSystemEntity systemEntity = new SysSystemEntity(systemEntityUid, SystemEntityType.IDENTITY);
		systemEntity.setSystem(system);
		systemEntity.setWish(true);
		systemEntityService.save(systemEntity);
		Map<ProvisioningAttributeDto, Object> accoutObject = createAccountObject(systemEntity);
		context.setAccountObject(accoutObject);
		
		ProvisioningAttributeDto passwordAttributeMappingKey = new ProvisioningAttributeDto(
				schemaAttributeService.get(passwordAttributeMapping.getSchemaAttribute()).getName(),
				AttributeMappingStrategyType.SET);
		ProvisioningAttributeDto firstNameAttributeMappingKey = new ProvisioningAttributeDto(
				schemaAttributeService.get(firstNameAttributeMapping.getSchemaAttribute()).getName(),
				AttributeMappingStrategyType.SET);
		ProvisioningAttributeDto lastNameAttributeMappingKey = new ProvisioningAttributeDto(
				schemaAttributeService.get(lastNameAttributeMapping.getSchemaAttribute()).getName(),
				AttributeMappingStrategyType.SET);

		GuardedString password = (GuardedString) accoutObject.get(passwordAttributeMappingKey);
		//
		// publish event
		IcObjectClass objectClass = new IcObjectClassImpl(schemaObjectClassService.get(systemMapping.getObjectClass()).getObjectClassName());
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(null, objectClass, null);
		SysProvisioningOperation.Builder operationBuilder = new SysProvisioningOperation.Builder()
				.setOperationType(ProvisioningOperationType.CREATE)
				.setSystemEntity(systemEntity)
				.setProvisioningContext(new ProvisioningContext(accoutObject, connectorObject));
		SysProvisioningOperation operation = provisioningExecutor.execute(operationBuilder.build());
		//
		assertEquals(OperationState.NOT_EXECUTED, operation.getResultState());
		assertEquals(AccResultCode.PROVISIONING_SYSTEM_DISABLED.name(), operation.getResult().getModel().getStatusEnum());
		//
		IcUidAttribute uidAttribute = new IcUidAttributeImpl(null, systemEntityUid, null);
		IcConnectorObject existsConnectorObject = connectorFacade.readObject(
				system.getConnectorInstance(), 
				systemService.getConnectorConfiguration(system), 
				objectClass, 
				uidAttribute);
		//
		assertNull(existsConnectorObject);
		// password is stored in confidential storage
		assertNotNull(confidentialStorage.get(operation.getId(), operation.getClass(), sysProvisioningOperationService.createAccountObjectPropertyKey(passwordAttributeMappingKey.getKey(), 0)));
		//
		system.setDisabled(false);
		systemService.save(system);
		//
		provisioningExecutor.execute(operation);
		//
		// check target account
		existsConnectorObject = connectorFacade.readObject(
				system.getConnectorInstance(), 
				systemService.getConnectorConfiguration(system), 
				objectClass, 
				uidAttribute);
		//
		assertNotNull(existsConnectorObject);
		assertEquals(systemEntityUid, existsConnectorObject.getUidValue());
		assertEquals(accoutObject.get(firstNameAttributeMappingKey), 
				existsConnectorObject.getAttributeByName(firstNameAttributeMapping.getName()).getValue());
		assertEquals(accoutObject.get(lastNameAttributeMappingKey), 
				existsConnectorObject.getAttributeByName(lastNameAttributeMapping.getName()).getValue());
		// authenticate for password check
		IcUidAttribute attribute = connectorFacade.authenticateObject(
				system.getConnectorInstance(), 
				systemService.getConnectorConfiguration(system), 
				objectClass,
				systemEntityUid, password);
		assertNotNull(attribute);
		assertEquals(systemEntityUid, attribute.getUidValue());
		// password is removed in confidential storage
		assertNull(confidentialStorage.get(operation.getId(), operation.getClass(), sysProvisioningOperationService.createAccountObjectPropertyKey(passwordAttributeMappingKey.getKey(), 0)));
	}
	
	@Test
	public void testReadonlySystem() {
		initSystem();
		system.setReadonly(true);
		system = systemService.save(system);
		//
		// create test provisioning context
		ProvisioningContext context = new ProvisioningContext();
		String systemEntityUid = "threeUid";
		SysSystemEntity systemEntity = new SysSystemEntity(systemEntityUid, SystemEntityType.IDENTITY);
		systemEntity.setSystem(system);
		systemEntity.setWish(true);
		systemEntityService.save(systemEntity);
		Map<ProvisioningAttributeDto, Object> accoutObject = createAccountObject(systemEntity);
		context.setAccountObject(accoutObject);
		
		ProvisioningAttributeDto passwordAttributeMappingKey = new ProvisioningAttributeDto(
				schemaAttributeService.get(passwordAttributeMapping.getSchemaAttribute()).getName(),
				AttributeMappingStrategyType.SET);
		ProvisioningAttributeDto firstNameAttributeMappingKey = new ProvisioningAttributeDto(
				schemaAttributeService.get(firstNameAttributeMapping.getSchemaAttribute()).getName(),
				AttributeMappingStrategyType.SET);
		ProvisioningAttributeDto lastNameAttributeMappingKey = new ProvisioningAttributeDto(
				schemaAttributeService.get(lastNameAttributeMapping.getSchemaAttribute()).getName(),
				AttributeMappingStrategyType.SET);

		GuardedString password = (GuardedString) accoutObject.get(passwordAttributeMappingKey);
		//
		// publish event
		IcObjectClass objectClass = new IcObjectClassImpl(schemaObjectClassService.get(systemMapping.getObjectClass()).getObjectClassName());
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(null, objectClass, null);
		SysProvisioningOperation.Builder operationBuilder = new SysProvisioningOperation.Builder()
				.setOperationType(ProvisioningOperationType.CREATE)
				.setSystemEntity(systemEntity)
				.setProvisioningContext(new ProvisioningContext(accoutObject, connectorObject));
		SysProvisioningOperation operation = provisioningExecutor.execute(operationBuilder.build());
		//
		assertEquals(OperationState.NOT_EXECUTED, operation.getResultState());
		assertEquals(AccResultCode.PROVISIONING_SYSTEM_READONLY.name(), operation.getResult().getModel().getStatusEnum());
		//
		IcUidAttribute uidAttribute = new IcUidAttributeImpl(null, systemEntityUid, null);
		IcConnectorObject existsConnectorObject = connectorFacade.readObject(
				system.getConnectorInstance(), 
				systemService.getConnectorConfiguration(system), 
				objectClass, 
				uidAttribute);
		//
		assertNull(existsConnectorObject);
		// passwords are stored in confidential storage
		assertNotNull(confidentialStorage.get(operation.getId(), operation.getClass(), sysProvisioningOperationService.createAccountObjectPropertyKey( passwordAttributeMappingKey.getKey(), 0)));
		assertNotNull(confidentialStorage.get(operation.getId(), operation.getClass(), sysProvisioningOperationService.createConnectorObjectPropertyKey(operation.getProvisioningContext().getConnectorObject().getAttributeByName(passwordAttributeMappingKey.getSchemaAttributeName()), 0)));
		//
		system.setReadonly(false);
		systemService.save(system);
		//
		provisioningExecutor.execute(operation);
		//
		// check target account
		existsConnectorObject = connectorFacade.readObject(
				system.getConnectorInstance(), 
				systemService.getConnectorConfiguration(system), 
				objectClass, 
				uidAttribute);
		//
		assertNotNull(existsConnectorObject);
		assertEquals(systemEntityUid, existsConnectorObject.getUidValue());
		assertEquals(accoutObject.get(firstNameAttributeMappingKey), 
				existsConnectorObject.getAttributeByName(firstNameAttributeMapping.getName()).getValue());
		assertEquals(accoutObject.get(lastNameAttributeMappingKey), 
				existsConnectorObject.getAttributeByName(lastNameAttributeMapping.getName()).getValue());
		// authenticate for password check
		IcUidAttribute attribute = connectorFacade.authenticateObject(
				system.getConnectorInstance(), 
				systemService.getConnectorConfiguration(system), 
				objectClass,
				systemEntityUid, password);
		assertNotNull(attribute);
		assertEquals(systemEntityUid, attribute.getUidValue());
		// passwords are removed in confidential storage
		assertNull(confidentialStorage.get(operation.getId(), operation.getClass(),
				sysProvisioningOperationService.createAccountObjectPropertyKey(
						schemaAttributeService.get(passwordAttributeMapping.getSchemaAttribute()).getName(), 0)));
		assertNull(
				confidentialStorage
						.get(operation.getId(), operation.getClass(),
								sysProvisioningOperationService.createConnectorObjectPropertyKey(
										operation.getProvisioningContext().getConnectorObject()
												.getAttributeByName(schemaAttributeService
														.get(passwordAttributeMapping.getSchemaAttribute()).getName()),
										0)));
	}
	
	// TODO: batch test - create, update, update, delete - all has to be processed, batch needs to be cleared
}
