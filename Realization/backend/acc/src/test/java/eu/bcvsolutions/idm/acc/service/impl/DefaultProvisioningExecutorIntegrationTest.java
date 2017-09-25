package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.scheduler.task.impl.ProvisioningQueueTaskExecutor;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
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
 * - asynchronous system provisioning
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultProvisioningExecutorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private TestHelper helper;
	@Autowired private ApplicationContext context;
	@Autowired private SysSystemService systemService;
	@Autowired private SysSystemEntityService systemEntityService;
	@Autowired private IcConnectorFacade connectorFacade;
	@Autowired private ConfidentialStorage confidentialStorage;
	@Autowired private SysSchemaObjectClassService schemaObjectClassService;
	@Autowired private LongRunningTaskManager longRunningTaskManager; 
	@Autowired private IdmLongRunningTaskService longRunningTaskService; 
	//	
	private SysProvisioningOperationService provisioningOperationService;
	private ProvisioningExecutor provisioningExecutor;
	
	@Before
	public void init() {	
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		provisioningOperationService = context.getAutowireCapableBeanFactory().createBean(DefaultSysProvisioningOperationService.class);
		provisioningExecutor = context.getAutowireCapableBeanFactory().createBean(DefaultProvisioningExecutor.class);
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testGreenLineAccountProvisioning() {
		SysSystemDto system = helper.createTestResourceSystem(true);
		ProvisioningAttributeDto usernameAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_NAME);
		ProvisioningAttributeDto firstNameAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME);
		ProvisioningAttributeDto lastNameAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_LASTNAME);
		ProvisioningAttributeDto passwordAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_PASSWORD);
		//
		// create test provisioning context
		SysProvisioningOperationDto provisioningOperation = createProvisioningOperation(system);
		IcObjectClass objectClass = provisioningOperation.getProvisioningContext().getConnectorObject().getObjectClass();
		Map<ProvisioningAttributeDto, Object> accoutObject = provisioningOperation.getProvisioningContext().getAccountObject();
		String uid = (String) accoutObject.get(usernameAttribute);
		GuardedString password = (GuardedString) accoutObject.get(passwordAttribute);
		//
		// publish event
		provisioningExecutor.execute(provisioningOperation);
		//
		// check target account
		IcUidAttribute uidAttribute = new IcUidAttributeImpl(null, uid, null);
		IcConnectorObject existsConnectorObject = connectorFacade.readObject(
				system.getConnectorInstance(), 
				systemService.getConnectorConfiguration(system), 
				objectClass, 
				uidAttribute);
		//
		assertNotNull(existsConnectorObject);
		assertEquals(uid, existsConnectorObject.getUidValue());
		assertEquals(accoutObject.get(firstNameAttribute), 
				existsConnectorObject.getAttributeByName(TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME).getValue());
		assertEquals(accoutObject.get(lastNameAttribute), 
				existsConnectorObject.getAttributeByName(TestHelper.ATTRIBUTE_MAPPING_LASTNAME).getValue());
		// authenticate for password check
		IcUidAttribute attribute = connectorFacade.authenticateObject(
				system.getConnectorInstance(), 
				systemService.getConnectorConfiguration(system), 
				objectClass,
				uid, password);
		assertNotNull(attribute);
		assertEquals(uid, attribute.getUidValue());
		//
		// check system entity
		SysSystemEntityDto systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, SystemEntityType.IDENTITY, uid);
		assertFalse(systemEntity.isWish());
	}
	
	@Test
	public void testDisabledSystem() {
		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setDisabled(true);
		system = systemService.save(system);
		//
		ProvisioningAttributeDto usernameAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_NAME);
		ProvisioningAttributeDto firstNameAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME);
		ProvisioningAttributeDto lastNameAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_LASTNAME);
		ProvisioningAttributeDto passwordAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_PASSWORD);
		//
		// create test provisioning context
		SysProvisioningOperationDto provisioningOperation = createProvisioningOperation(system);
		IcObjectClass objectClass = provisioningOperation.getProvisioningContext().getConnectorObject().getObjectClass();
		Map<ProvisioningAttributeDto, Object> accoutObject = provisioningOperation.getProvisioningContext().getAccountObject();
		String uid = (String) accoutObject.get(usernameAttribute);
		GuardedString password = (GuardedString) accoutObject.get(passwordAttribute);
		//
		// publish event
		SysProvisioningOperationDto operation = provisioningExecutor.execute(provisioningOperation);
		// is necessary to get again operation from service
		operation = provisioningOperationService.get(operation.getId());
		//
		assertEquals(OperationState.NOT_EXECUTED, operation.getResultState());
		assertEquals(AccResultCode.PROVISIONING_SYSTEM_DISABLED.name(), operation.getResult().getModel().getStatusEnum());
		//
		IcUidAttribute uidAttribute = new IcUidAttributeImpl(null, uid, null);
		IcConnectorObject existsConnectorObject = connectorFacade.readObject(
				system.getConnectorInstance(), 
				systemService.getConnectorConfiguration(system), 
				objectClass, 
				uidAttribute);
		//
		assertNull(existsConnectorObject);
		// password is stored in confidential storage
		assertNotNull(confidentialStorage.get(
				operation.getId(), 
				SysProvisioningOperation.class, 
				provisioningOperationService.createAccountObjectPropertyKey(passwordAttribute.getKey(), 0)));
		//
		system.setDisabled(false);
		system = systemService.save(system);
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
		assertEquals(uid, existsConnectorObject.getUidValue());
		assertEquals(accoutObject.get(firstNameAttribute), 
				existsConnectorObject.getAttributeByName(TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME).getValue());
		assertEquals(accoutObject.get(lastNameAttribute), 
				existsConnectorObject.getAttributeByName(TestHelper.ATTRIBUTE_MAPPING_LASTNAME).getValue());
		// authenticate for password check
		IcUidAttribute attribute = connectorFacade.authenticateObject(
				system.getConnectorInstance(), 
				systemService.getConnectorConfiguration(system), 
				objectClass,
				uid, password);
		assertNotNull(attribute);
		assertEquals(uid, attribute.getUidValue());
		// password is removed in confidential storage
		assertNull(confidentialStorage.get(
				operation.getId(), 
				SysProvisioningOperation.class, 
				provisioningOperationService.createAccountObjectPropertyKey(passwordAttribute.getKey(), 0)));
	}
	
	@Test
	public void testReadonlySystem() {
		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setReadonly(true);
		system = systemService.save(system);
		ProvisioningAttributeDto usernameAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_NAME);
		ProvisioningAttributeDto firstNameAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME);
		ProvisioningAttributeDto lastNameAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_LASTNAME);
		ProvisioningAttributeDto passwordAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_PASSWORD);
		//
		// create test provisioning context
		SysProvisioningOperationDto provisioningOperation = createProvisioningOperation(system);
		IcObjectClass objectClass = provisioningOperation.getProvisioningContext().getConnectorObject().getObjectClass();
		Map<ProvisioningAttributeDto, Object> accoutObject = provisioningOperation.getProvisioningContext().getAccountObject();
		String uid = (String) accoutObject.get(usernameAttribute);
		GuardedString password = (GuardedString) accoutObject.get(passwordAttribute);
		//
		// publish event
		SysProvisioningOperationDto operation = provisioningExecutor.execute(provisioningOperation);
		// is necessary to get again operation from service
		operation = provisioningOperationService.get(operation.getId());
		//
		assertEquals(OperationState.NOT_EXECUTED, operation.getResultState());
		assertEquals(AccResultCode.PROVISIONING_SYSTEM_READONLY.name(), operation.getResult().getModel().getStatusEnum());
		//
		IcUidAttribute uidAttribute = new IcUidAttributeImpl(null, uid, null);
		IcConnectorObject existsConnectorObject = connectorFacade.readObject(
				system.getConnectorInstance(), 
				systemService.getConnectorConfiguration(system), 
				objectClass, 
				uidAttribute);
		//
		assertNull(existsConnectorObject);
		// passwords are stored in confidential storage
		assertNotNull(confidentialStorage.get(operation.getId(), SysProvisioningOperation.class, provisioningOperationService.createAccountObjectPropertyKey( passwordAttribute.getKey(), 0)));
		assertNotNull(confidentialStorage.get(operation.getId(), SysProvisioningOperation.class, provisioningOperationService.createConnectorObjectPropertyKey(operation.getProvisioningContext().getConnectorObject().getAttributeByName(passwordAttribute.getSchemaAttributeName()), 0)));
		//
		system.setReadonly(false);
		system = systemService.save(system);
		//
		operation = provisioningExecutor.execute(operation);
		//
		// check target account
		existsConnectorObject = connectorFacade.readObject(
				system.getConnectorInstance(), 
				systemService.getConnectorConfiguration(system), 
				objectClass, 
				uidAttribute);
		//
		assertNotNull(existsConnectorObject);
		assertEquals(uid, existsConnectorObject.getUidValue());
		assertEquals(accoutObject.get(firstNameAttribute), 
				existsConnectorObject.getAttributeByName(TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME).getValue());
		assertEquals(accoutObject.get(lastNameAttribute), 
				existsConnectorObject.getAttributeByName(TestHelper.ATTRIBUTE_MAPPING_LASTNAME).getValue());
		// authenticate for password check
		IcUidAttribute attribute = connectorFacade.authenticateObject(
				system.getConnectorInstance(), 
				systemService.getConnectorConfiguration(system), 
				objectClass,
				uid, password);
		assertNotNull(attribute);
		assertEquals(uid, attribute.getUidValue());
		// passwords are removed in confidential storage
		assertNull(confidentialStorage.get(operation.getId(), SysProvisioningOperation.class,
				provisioningOperationService.createAccountObjectPropertyKey(TestHelper.ATTRIBUTE_MAPPING_PASSWORD, 0)));
		//
		String connectorObjectPropertyKey = provisioningOperationService.createConnectorObjectPropertyKey(
				operation.getProvisioningContext().getConnectorObject().getAttributeByName(TestHelper.ATTRIBUTE_MAPPING_PASSWORD),
				0);
		//
		assertNull(confidentialStorage.get(operation.getId(), SysProvisioningOperation.class, connectorObjectPropertyKey));
	}
	
	@Test
	public void testAsynchronousSystem() {
		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setQueue(true);
		system = systemService.save(system);
		//
		// create test provisioning context
		SysProvisioningOperationDto provisioningOperation = createProvisioningOperation(system);
		Map<ProvisioningAttributeDto, Object> accoutObject = provisioningOperation.getProvisioningContext().getAccountObject();
		String uid = (String) accoutObject.get(getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_NAME));
		//
		// publish event
		SysProvisioningOperationDto operation = provisioningExecutor.execute(provisioningOperation);
		assertEquals(OperationState.CREATED, operation.getResultState());
		SysSystemEntityDto systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, SystemEntityType.IDENTITY, uid);
		assertTrue(systemEntity.isWish());
		assertNull(helper.findResource(uid));
		//
		// execute LRT with incorrect setting - virtual at fist - expected no process
		ProvisioningQueueTaskExecutor provisioningQueueExecutor = new ProvisioningQueueTaskExecutor();
		provisioningQueueExecutor.setVirtual(true);
		Boolean result = longRunningTaskManager.executeSync(provisioningQueueExecutor);
		assertTrue(result);
		IdmLongRunningTaskDto lrt = longRunningTaskService.get(provisioningQueueExecutor.getLongRunningTaskId());
		assertEquals(0L, lrt.getCount().longValue());
		systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, SystemEntityType.IDENTITY, uid);
		assertTrue(systemEntity.isWish());
		assertNull(helper.findResource(uid));
		//
		// execute LRT with correct setting
		provisioningQueueExecutor = new ProvisioningQueueTaskExecutor();
		result = longRunningTaskManager.executeSync(provisioningQueueExecutor);
		assertTrue(result);
		lrt = longRunningTaskService.get(provisioningQueueExecutor.getLongRunningTaskId());
		assertEquals(1L, lrt.getCount().longValue());
		systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, SystemEntityType.IDENTITY, uid);
		assertFalse(systemEntity.isWish());
		assertNotNull(helper.findResource(uid));
	}
	
	private Map<ProvisioningAttributeDto, Object> createAccountObject(SysSystemEntityDto systemEntity) {
		ProvisioningAttributeDto nameAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_NAME);
		ProvisioningAttributeDto firstNameAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME);
		ProvisioningAttributeDto lastNameAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_LASTNAME);
		ProvisioningAttributeDto passwordAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_PASSWORD);
		//
		Map<ProvisioningAttributeDto, Object> accoutObject = new HashMap<>();		
		accoutObject.put(nameAttribute, systemEntity.getUid());
		accoutObject.put(firstNameAttribute, "firstOne");
		accoutObject.put(lastNameAttribute, "lastOne");
		accoutObject.put(passwordAttribute, new GuardedString("password"));
		//
		return accoutObject;
	}
	
	/**
	 * Prepare provisioning context and operation
	 * 
	 * @param system
	 * @return
	 */
	private SysProvisioningOperationDto createProvisioningOperation(SysSystemDto system) {
		ProvisioningContext context = new ProvisioningContext();
		SysSystemEntityDto systemEntity = helper.createSystemEntity(system);
		Map<ProvisioningAttributeDto, Object> accoutObject = createAccountObject(systemEntity);
		context.setAccountObject(accoutObject);
		//
		// prepare provisioning operation
		SysSystemMappingDto systemMapping = helper.getDefaultMapping(system);
		IcObjectClass objectClass = new IcObjectClassImpl(schemaObjectClassService.get(systemMapping.getObjectClass()).getObjectClassName());
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(null, objectClass, null);
		SysProvisioningOperationDto.Builder operationBuilder = new SysProvisioningOperationDto.Builder()
				.setOperationType(ProvisioningOperationType.CREATE)
				.setSystemEntity(systemEntity.getId())
				.setProvisioningContext(new ProvisioningContext(accoutObject, connectorObject));
		return operationBuilder.build();
	}
	
	/**
	 * Return provisiong attribute by default mapping and strategy
	 * 
	 * @return
	 */
	private ProvisioningAttributeDto getProvisioningAttribute(String name) {
		// load attribute mapping is not needed now - name is the same on both (tree) sides
		return new ProvisioningAttributeDto(name, AttributeMappingStrategyType.SET);
	}
	
	// TODO: batch test - create, update, update, delete - all has to be processed, batch needs to be cleared
}
