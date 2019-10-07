package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.zaxxer.hikari.HikariDataSource;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.ProvisioningAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningArchive_;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningAttribute;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation_;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningAttributeRepository;
import eu.bcvsolutions.idm.acc.scheduler.task.impl.ProvisioningQueueTaskExecutor;
import eu.bcvsolutions.idm.acc.scheduler.task.impl.RetryProvisioningTaskExecutor;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.scheduler.api.config.SchedulerConfiguration;
import eu.bcvsolutions.idm.core.scheduler.api.dto.CronTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
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
 * - fill provisioning attributes
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
	@Autowired private SysProvisioningBatchService provisioningBatchService;
	@Autowired private TestProvisioningExceptionProcessor testProvisioningExceptionProcessor;
	@Autowired private FormService formService;
	@Autowired private DataSource dataSource;
	@Autowired private EntityEventManager entityEventManager;
	@Autowired private SysProvisioningArchiveService provisioningArchiveService;
	@Autowired private ConfigurationService configurationService;
	@Autowired private SchedulerManager schedulerManager;
	@Autowired private SysProvisioningAttributeRepository provisioningAttributeRepository;
	@Autowired private IdmIdentityService identitySerivce;
	//
	private SysProvisioningOperationService provisioningOperationService;
	private ProvisioningExecutor provisioningExecutor;
	
	@Before
	public void init() {	
		provisioningOperationService = context.getAutowireCapableBeanFactory().createBean(DefaultSysProvisioningOperationService.class);
		provisioningExecutor = context.getAutowireCapableBeanFactory().createBean(DefaultProvisioningExecutor.class);
	}
	
	@Test
	public void testReferentiralIntegrity() {
		SysSystemDto system = getHelper().createTestResourceSystem(true);
		
		SysProvisioningOperationDto operationOne = provisioningOperationService.save(
				createProvisioningOperation(system, getHelper().createName()));
		SysProvisioningOperationDto operationTwo = provisioningOperationService.save(
				createProvisioningOperation(system, getHelper().createName()));
		SysProvisioningAttribute attributeOne = new SysProvisioningAttribute(operationOne.getId(), getHelper().createName());
		attributeOne = provisioningAttributeRepository.save(attributeOne);
		SysProvisioningAttribute attributeTwo = new SysProvisioningAttribute(operationTwo.getId(), getHelper().createName());
		attributeTwo = provisioningAttributeRepository.save(attributeTwo);
		//
		provisioningOperationService.delete(operationOne);
		//
		Assert.assertFalse(provisioningAttributeRepository.existsById(attributeOne.getId()));
		Assert.assertTrue(provisioningAttributeRepository.existsById(attributeTwo.getId()));
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
		SysProvisioningOperationDto provisioningOperation = createProvisioningOperation(system, "firstname");
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
		SysProvisioningOperationDto provisioningOperation = createProvisioningOperation(system, "firstname");
		IcObjectClass objectClass = provisioningOperation.getProvisioningContext().getConnectorObject().getObjectClass();
		Map<ProvisioningAttributeDto, Object> accoutObject = provisioningOperation.getProvisioningContext().getAccountObject();
		String uid = (String) accoutObject.get(usernameAttribute);
		GuardedString password = (GuardedString) accoutObject.get(passwordAttribute);
		//
		// publish event
		provisioningExecutor.execute(provisioningOperation);
		// is necessary to get again operation from service
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemEntity(provisioningOperation.getSystemEntity());
		filter.setSystemId(system.getId());
		SysProvisioningOperationDto operation = provisioningOperationService.find(filter, null).getContent().get(0);
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
		SysProvisioningOperationDto provisioningOperation = createProvisioningOperation(system, "firstname");
		IcObjectClass objectClass = provisioningOperation.getProvisioningContext().getConnectorObject().getObjectClass();
		Map<ProvisioningAttributeDto, Object> accoutObject = provisioningOperation.getProvisioningContext().getAccountObject();
		String uid = (String) accoutObject.get(usernameAttribute);
		GuardedString password = (GuardedString) accoutObject.get(passwordAttribute);
		//
		// publish event
		provisioningExecutor.execute(provisioningOperation);
		// is necessary to get again operation from service
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemEntity(provisioningOperation.getSystemEntity());
		filter.setSystemId(system.getId());
		SysProvisioningOperationDto readOnlyoperation = provisioningOperationService.find(filter, null).getContent().get(0);
		//
		Assert.assertEquals(OperationState.NOT_EXECUTED, readOnlyoperation.getResultState());
		Assert.assertEquals(AccResultCode.PROVISIONING_SYSTEM_READONLY.name(), readOnlyoperation.getResult().getModel().getStatusEnum());
		//
		IcUidAttribute uidAttribute = new IcUidAttributeImpl(null, uid, null);
		IcConnectorObject existsConnectorObject = connectorFacade.readObject(
				system.getConnectorInstance(), 
				systemService.getConnectorConfiguration(system), 
				objectClass, 
				uidAttribute);
		//
		Assert.assertNull(existsConnectorObject);
		// passwords are stored in confidential storage
		Assert.assertNotNull(confidentialStorage.get(readOnlyoperation.getId(), SysProvisioningOperation.class, provisioningOperationService.createAccountObjectPropertyKey( passwordAttribute.getKey(), 0)));
		Assert.assertNotNull(confidentialStorage.get(readOnlyoperation.getId(), SysProvisioningOperation.class, provisioningOperationService.createConnectorObjectPropertyKey(readOnlyoperation.getProvisioningContext().getConnectorObject().getAttributeByName(passwordAttribute.getSchemaAttributeName()), 0)));
		//
		system.setReadonly(false);
		system = systemService.save(system);
		//
		provisioningExecutor.execute(readOnlyoperation);
		// is necessary to get again operation from service
		Assert.assertNull(provisioningOperationService.get(readOnlyoperation.getId()));
		//
		// check target account
		existsConnectorObject = connectorFacade.readObject(
				system.getConnectorInstance(), 
				systemService.getConnectorConfiguration(system), 
				objectClass, 
				uidAttribute);
		//
		Assert.assertNotNull(existsConnectorObject);
		Assert.assertEquals(uid, existsConnectorObject.getUidValue());
		Assert.assertEquals(accoutObject.get(firstNameAttribute), 
				existsConnectorObject.getAttributeByName(TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME).getValue());
		Assert.assertEquals(accoutObject.get(lastNameAttribute), 
				existsConnectorObject.getAttributeByName(TestHelper.ATTRIBUTE_MAPPING_LASTNAME).getValue());
		// authenticate for password check
		IcUidAttribute attribute = connectorFacade.authenticateObject(
				system.getConnectorInstance(), 
				systemService.getConnectorConfiguration(system), 
				objectClass,
				uid, password);
		Assert.assertNotNull(attribute);
		Assert.assertEquals(uid, attribute.getUidValue());
		// passwords are removed in confidential storage
		Assert.assertNull(confidentialStorage.get(readOnlyoperation.getId(), SysProvisioningOperation.class,
				provisioningOperationService.createAccountObjectPropertyKey(TestHelper.ATTRIBUTE_MAPPING_PASSWORD, 0)));
		//
		String connectorObjectPropertyKey = provisioningOperationService.createConnectorObjectPropertyKey(
				readOnlyoperation.getProvisioningContext().getConnectorObject().getAttributeByName(TestHelper.ATTRIBUTE_MAPPING_PASSWORD),
				0);
		//
		Assert.assertNull(confidentialStorage.get(readOnlyoperation.getId(), SysProvisioningOperation.class, connectorObjectPropertyKey));
	}
	
	@Test
	public void testAsynchronousSystem() {
		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setQueue(true);
		system = systemService.save(system);
		//
		// create test provisioning context
		SysProvisioningOperationDto provisioningOperation = createProvisioningOperation(system, "firstname");
		Map<ProvisioningAttributeDto, Object> accoutObject = provisioningOperation.getProvisioningContext().getAccountObject();
		String uid = (String) accoutObject.get(getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_NAME));
		//
		// publish event
		provisioningExecutor.execute(provisioningOperation);
		// is necessary to get again operation from service
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemEntity(provisioningOperation.getSystemEntity());
		filter.setSystemId(system.getId());
		SysProvisioningOperationDto operation = provisioningOperationService.find(filter, null).getContent().get(0);
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
		IdmLongRunningTaskDto lrt = longRunningTaskManager.getLongRunningTask(provisioningQueueExecutor.getLongRunningTaskId());
		assertEquals(0L, lrt.getCount().longValue());
		systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, SystemEntityType.IDENTITY, uid);
		assertTrue(systemEntity.isWish());
		assertNull(helper.findResource(uid));
		//
		// execute LRT with correct setting
		provisioningQueueExecutor = new ProvisioningQueueTaskExecutor();
		result = longRunningTaskManager.executeSync(provisioningQueueExecutor);
		assertTrue(result);
		lrt = longRunningTaskManager.getLongRunningTask(provisioningQueueExecutor);
		assertEquals(1L, lrt.getCount().longValue());
		systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, SystemEntityType.IDENTITY, uid);
		assertFalse(systemEntity.isWish());
		assertNotNull(helper.findResource(uid));
	}
	
	@Test
	public void testClearProvisioningBatchOnReadonlySystem() {
		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setReadonly(true);
		system = systemService.save(system);
		String firstname = "firstname";
		SysProvisioningOperationDto provisioningOperation = createProvisioningOperation(system, firstname);
		Map<ProvisioningAttributeDto, Object> accoutObject = provisioningOperation.getProvisioningContext().getAccountObject();
		String uid = (String) accoutObject.get(getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_NAME));
		//
		// publish event
		provisioningExecutor.execute(provisioningOperation); // 1 - create
		// is necessary to get again operation from service
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemEntity(provisioningOperation.getSystemEntity());
		filter.setSystemId(system.getId());
		SysProvisioningOperationDto readOnlyOperation = provisioningOperationService.find(filter, null).getContent().get(0);
		assertEquals(OperationState.NOT_EXECUTED, readOnlyOperation.getResultState());
		assertEquals(AccResultCode.PROVISIONING_SYSTEM_READONLY.name(), readOnlyOperation.getResult().getModel().getStatusEnum());
		SysSystemEntityDto systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, SystemEntityType.IDENTITY, uid);
		provisioningExecutor.execute(updateProvisioningOperation(systemEntity, firstname + 2)); // 2 - update
		provisioningExecutor.execute(updateProvisioningOperation(systemEntity, firstname + 3)); // 3 - update
		//
		systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, SystemEntityType.IDENTITY, uid);
		assertTrue(systemEntity.isWish());
		assertNull(helper.findResource(uid));
		//
		// check batch
		SysProvisioningBatchDto batch = provisioningBatchService.findBatch(systemEntity.getId());
		Assert.assertNotNull(batch);
		//
		// check provisioning operation requests
		List<SysProvisioningOperationDto> requests = provisioningOperationService.findByBatchId(batch.getId(), null).getContent();
		Assert.assertEquals(3, requests.size());
		//
		// execute first operation - create
		system.setReadonly(false);
		system = systemService.save(system);
		provisioningExecutor.execute(readOnlyOperation);
		Assert.assertNull(provisioningOperationService.get(readOnlyOperation.getId()));
		//
		systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, SystemEntityType.IDENTITY, uid);
		assertFalse(systemEntity.isWish());
		TestResource resource = helper.findResource(uid);
		assertNotNull(resource);
		Assert.assertEquals(firstname, resource.getFirstname());
		Assert.assertEquals(2, provisioningOperationService.findByBatchId(batch.getId(), null).getContent().size());
		//
		// execute whole batch
		provisioningExecutor.execute(batch);
		//
		resource = helper.findResource(uid);
		Assert.assertEquals(firstname + 3, resource.getFirstname());
		Assert.assertEquals(0, provisioningOperationService.findByBatchId(batch.getId(), null).getTotalElements());
		Assert.assertNull(provisioningOperationService.get(readOnlyOperation.getId()));
		batch = provisioningBatchService.get(batch.getId());
		Assert.assertNull(batch.getNextAttempt());
	}
	
	@Test
	public void testRetryProvisioning() {
		testProvisioningExceptionProcessor.setDisabled(false);
		try {
			SysSystemDto system = helper.createTestResourceSystem(true);
			SysProvisioningOperationDto provisioningOperation = createProvisioningOperation(system, "firstname");
			Map<ProvisioningAttributeDto, Object> accoutObject = provisioningOperation.getProvisioningContext().getAccountObject();
			String uid = (String) accoutObject.get(getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_NAME));
			ZonedDateTime now = ZonedDateTime.now();
			//
			// publish event
			provisioningExecutor.execute(provisioningOperation); // 1 - create
			// is necessary to get again operation from service
			SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
			filter.setSystemEntityUid(provisioningOperation.getSystemEntityUid());
			filter.setSystemId(system.getId());
			SysProvisioningOperationDto operation = provisioningOperationService.find(filter, null).getContent().get(0);
			SysProvisioningBatchDto batch = provisioningBatchService.findBatch(operation.getSystemEntity());
			Assert.assertEquals(OperationState.EXCEPTION, operation.getResultState());
			Assert.assertEquals(AccResultCode.PROVISIONING_FAILED.name(), operation.getResult().getModel().getStatusEnum());
			Assert.assertEquals(1, operation.getCurrentAttempt());
			Assert.assertTrue(operation.getMaxAttempts() > 1);
			Assert.assertTrue(batch.getNextAttempt().isAfter(now));
			SysSystemEntityDto systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, SystemEntityType.IDENTITY, uid);
			Assert.assertTrue(systemEntity.isWish());
			Assert.assertNull(helper.findResource(uid));
			// check failed operation is in archive too
			List<SysProvisioningArchiveDto> archived = provisioningArchiveService
					.find(
						filter, 
						PageRequest.of(0, 10, new Sort(Direction.DESC, SysProvisioningArchive_.created.getName()))
					)
					.getContent();
			Assert.assertEquals(1, archived.size());
			Assert.assertEquals(OperationState.EXCEPTION, archived.get(0).getResultState());
			Assert.assertEquals(AccResultCode.PROVISIONING_FAILED.name(), archived.get(0).getResult().getModel().getStatusEnum());
			//
			batch.setNextAttempt(ZonedDateTime.now());
			provisioningBatchService.save(batch);
			//
			// retry - the same exception expected
			RetryProvisioningTaskExecutor retryProvisioningTaskExecutor = new RetryProvisioningTaskExecutor();
			Boolean result = longRunningTaskManager.executeSync(retryProvisioningTaskExecutor);
			Assert.assertTrue(result);
			operation = provisioningOperationService.get(operation.getId());
			batch = provisioningBatchService.findBatch(systemEntity.getId());
			Assert.assertEquals(2, operation.getCurrentAttempt());
			Assert.assertNotNull(batch.getNextAttempt());
			Assert.assertTrue(batch.getNextAttempt().isAfter(now));
			archived = provisioningArchiveService
					.find(
						filter, 
						PageRequest.of(0, 10, new Sort(Direction.DESC, SysProvisioningArchive_.created.getName()))
					)
					.getContent();
			Assert.assertEquals(2, archived.size());
			Assert.assertEquals(OperationState.EXCEPTION, archived.get(0).getResultState());
			Assert.assertEquals(AccResultCode.PROVISIONING_FAILED.name(), archived.get(0).getResult().getModel().getStatusEnum());
			//
			batch.setNextAttempt(ZonedDateTime.now());
			provisioningBatchService.save(batch);
			//
			// retry - expected success now
			testProvisioningExceptionProcessor.setDisabled(true);
			retryProvisioningTaskExecutor = new RetryProvisioningTaskExecutor();
			longRunningTaskManager.execute(retryProvisioningTaskExecutor); // async way, but turned off
			//
			systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, SystemEntityType.IDENTITY, uid);
			Assert.assertFalse(systemEntity.isWish());
			Assert.assertNotNull(helper.findResource(uid));
			batch = provisioningBatchService.get(batch.getId());
			Assert.assertNull(batch.getNextAttempt());
			archived = provisioningArchiveService
					.find(
						filter, 
						PageRequest.of(0, 10, new Sort(Direction.DESC, SysProvisioningArchive_.created.getName()))
					)
					.getContent();
			Assert.assertEquals(3, archived.size());
			Assert.assertTrue(archived.stream().anyMatch(a -> a.getResultState() == OperationState.EXECUTED));
		} finally {
			testProvisioningExceptionProcessor.setDisabled(true);
		}
	}
	
	@Test
	public void testRetryProvisioningAfterPrepareConnectorObjectFailed() {
		SysSystemDto system = helper.createTestResourceSystem(true);
		// set the wrong password
		IdmFormDefinitionDto savedFormDefinition = systemService.getConnectorFormDefinition(system.getConnectorInstance());
		List<IdmFormValueDto> values = new ArrayList<>();
		IdmFormValueDto password = new IdmFormValueDto(savedFormDefinition.getMappedAttributeByCode("password"));
		password.setValue("wrong");
		values.add(password);
		formService.saveValues(system, savedFormDefinition, values);
		//
		SysProvisioningOperationDto provisioningOperation = createProvisioningOperation(system, "firstname");
		Map<ProvisioningAttributeDto, Object> accoutObject = provisioningOperation.getProvisioningContext().getAccountObject();
		String uid = (String) accoutObject.get(getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_NAME));
		ZonedDateTime now = ZonedDateTime.now();
		//
		// publish event
		// publish event
		provisioningExecutor.execute(provisioningOperation); // 1 - create
		// is necessary to get again operation from service
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemEntity(provisioningOperation.getSystemEntity());
		filter.setSystemId(system.getId());
		SysProvisioningOperationDto operation = provisioningOperationService.find(filter, null).getContent().get(0);
		SysProvisioningBatchDto batch = provisioningBatchService.findBatch(operation.getSystemEntity());
		Assert.assertEquals(OperationState.EXCEPTION, operation.getResultState());
		Assert.assertEquals(AccResultCode.PROVISIONING_FAILED.name(), operation.getResult().getModel().getStatusEnum());
		Assert.assertEquals(1, operation.getCurrentAttempt());
		Assert.assertTrue(operation.getMaxAttempts() > 1);
		Assert.assertTrue(batch.getNextAttempt().isAfter(now));
		SysSystemEntityDto systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, SystemEntityType.IDENTITY, uid);
		Assert.assertTrue(systemEntity.isWish());
		Assert.assertNull(helper.findResource(uid));
		//
		batch.setNextAttempt(ZonedDateTime.now());
		provisioningBatchService.save(batch);
		//
		// retry - the same exception expected
		RetryProvisioningTaskExecutor retryProvisioningTaskExecutor = new RetryProvisioningTaskExecutor();
		Boolean result = longRunningTaskManager.executeSync(retryProvisioningTaskExecutor);
		Assert.assertTrue(result);
		operation = provisioningOperationService.get(operation.getId());
		batch = provisioningBatchService.findBatch(systemEntity.getId());
		Assert.assertEquals(2, operation.getCurrentAttempt());
		Assert.assertNotNull(batch.getNextAttempt());
		Assert.assertTrue(batch.getNextAttempt().isAfter(now));
		//
		batch.setNextAttempt(ZonedDateTime.now());
		provisioningBatchService.save(batch);
		//
		// retry - expected success now - set the good password
		password.setValue(((HikariDataSource) dataSource).getPassword());
		formService.saveValues(system, savedFormDefinition, values);
		//
		retryProvisioningTaskExecutor = new RetryProvisioningTaskExecutor();
		result = longRunningTaskManager.executeSync(retryProvisioningTaskExecutor);
		Assert.assertTrue(result);
		//
		systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, SystemEntityType.IDENTITY, uid);
		Assert.assertFalse(systemEntity.isWish());
		Assert.assertNotNull(helper.findResource(uid));
		batch = provisioningBatchService.get(batch.getId());
		Assert.assertNull(batch.getNextAttempt());
	}
	
	@Test
	public void testRunningOperationInQueue() {
		SysSystemDto system = getHelper().createTestResourceSystem(true);
		system.setQueue(true);
		system = systemService.save(system);
		//
		// create test provisioning context
		SysProvisioningOperationDto provisioningOperation = createProvisioningOperation(system, "firstname");
		Map<ProvisioningAttributeDto, Object> accoutObject = provisioningOperation.getProvisioningContext().getAccountObject();
		String uid = (String) accoutObject.get(getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_NAME));
		//
		// publish event
		provisioningExecutor.execute(provisioningOperation);
		// is necessary to get again operation from service
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemEntity(provisioningOperation.getSystemEntity());
		filter.setSystemId(system.getId());
		SysProvisioningOperationDto operation = provisioningOperationService.find(filter, null).getContent().get(0);
		assertEquals(OperationState.CREATED, operation.getResultState());
		SysSystemEntityDto systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, SystemEntityType.IDENTITY, uid);
		assertTrue(systemEntity.isWish());
		assertNull(getHelper().findResource(uid));
		SysProvisioningBatchDto batch = DtoUtils.getEmbedded(operation, SysProvisioningOperation_.batch);
		Assert.assertNull(batch.getNextAttempt());
		//
		// set operation state to running manually
		operation.getResult().setState(OperationState.RUNNING);
		operation = provisioningOperationService.save(operation);
		//
		// created the second operation
		ProvisioningContext context = new ProvisioningContext();
		String firstname = "firstname2";
		accoutObject = createAccountObject(systemEntity, firstname);
		context.setAccountObject(accoutObject);
		//
		// prepare provisioning operation
		SysSystemMappingDto systemMapping = getHelper().getDefaultMapping(system);
		IcObjectClass objectClass = new IcObjectClassImpl(schemaObjectClassService.get(systemMapping.getObjectClass()).getObjectClassName());
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(null, objectClass, null);
		SysProvisioningOperationDto.Builder operationBuilder = new SysProvisioningOperationDto.Builder()
				.setOperationType(ProvisioningOperationType.CREATE)
				.setSystemEntity(systemEntity)
				.setProvisioningContext(new ProvisioningContext(accoutObject, connectorObject));
		SysProvisioningOperationDto secondOperation = operationBuilder.build();
		secondOperation.setId(UUID.randomUUID()); // for quick search
		//
		// publish event
		provisioningExecutor.execute(secondOperation);
		//
		secondOperation = provisioningOperationService.get(secondOperation);
		batch = provisioningBatchService.get(batch);
		Assert.assertNotNull(batch.getNextAttempt());
		//
		// retry - the operation is still running
		RetryProvisioningTaskExecutor retryProvisioningTaskExecutor = new RetryProvisioningTaskExecutor();
		Boolean result = longRunningTaskManager.executeSync(retryProvisioningTaskExecutor);
		Assert.assertTrue(result);
		operation = provisioningOperationService.get(operation);
		secondOperation = provisioningOperationService.get(secondOperation);
		//
		Assert.assertEquals(OperationState.RUNNING, operation.getResultState());
		Assert.assertEquals(OperationState.NOT_EXECUTED, secondOperation.getResultState());
		//
		operation.getResult().setState(OperationState.EXECUTED);
		operation = provisioningOperationService.save(operation);
		// Account cannot exist now
		TestResource resource = getHelper().findResource(uid);
		Assert.assertNull(resource);
		//
		// retry - expected success now
		retryProvisioningTaskExecutor = new RetryProvisioningTaskExecutor();
		result = longRunningTaskManager.executeSync(retryProvisioningTaskExecutor);
		Assert.assertTrue(result);
		//
		systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, SystemEntityType.IDENTITY, uid);
		Assert.assertFalse(systemEntity.isWish());
		resource = getHelper().findResource(uid);
		Assert.assertNotNull(resource);
		Assert.assertEquals(firstname, resource.getFirstname());
		batch = provisioningBatchService.get(batch.getId());
		Assert.assertNull(batch.getNextAttempt());
	}
	
	@Test
	public void testAsynchronousQueueSynchronized() throws Exception {
		//
		// schedule task to process queue async
		Task task = new Task();
		task.setInstanceId(configurationService.getInstanceId());
		task.setTaskType(ProvisioningQueueTaskExecutor.class);
		task = schedulerManager.createTask(task);
		CronTaskTrigger trigger = new CronTaskTrigger();
		trigger.setTaskId(task.getId());
		trigger.setCron("0/5 * * * * ?");
		//
		schedulerManager.createTrigger(task.getId(), trigger);
		//
		// turn on async processing
		getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, true);
		getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, true);
		//
		try {
			int count = 50;
			// system with asynchronous provisioning
			SysSystemDto system = getHelper().createTestResourceSystem(true);
			system.setQueue(true);
			system = systemService.save(system);
			//
			IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
			IdmRoleDto role = getHelper().createRole();
			getHelper().createRoleSystem(role, system);
			getHelper().createIdentityRole(identity, role);
			//
			for (int index = 0; index < count; index++) {
				// async provisioning by the notify event
				entityEventManager.changedEntity(identity);
				Thread.sleep(25);
			}
			//
			SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
			filter.setEntityIdentifier(identity.getId());
			//
			// wait for the active operations are processed
			getHelper().waitForResult(res -> {
				List<SysProvisioningArchiveDto> executedOperations = provisioningArchiveService.find(filter, null).getContent();
				//
				return executedOperations.size() != count + 1;
			});
			//
			// check archive is executed
			Assert.assertTrue(provisioningArchiveService.find(filter, null).getContent().stream().allMatch(a -> a.getResultState().isSuccessful()));
		} finally {
			schedulerManager.deleteTask(task.getId());
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, false);
			getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, false);
		}
	}
	
	@Test
	public void testDeleteOperations() {
		SysSystemDto systemOne = getHelper().createTestResourceSystem(true);
		SysSystemDto systemTwo = getHelper().createTestResourceSystem(true);
		//
		SysProvisioningOperationDto createProvisioningOperationOne = createProvisioningOperation(systemOne, getHelper().createName());
		createProvisioningOperationOne.setResult(new OperationResult(OperationState.BLOCKED));
		createProvisioningOperationOne = provisioningOperationService.save(createProvisioningOperationOne);
		SysProvisioningAttribute attributeOne = provisioningAttributeRepository.save(
				new SysProvisioningAttribute(createProvisioningOperationOne.getId(), getHelper().createName()));
		SysProvisioningOperationDto createProvisioningOperationTwo = createProvisioningOperation(systemTwo, getHelper().createName());
		createProvisioningOperationTwo.setResult(new OperationResult(OperationState.BLOCKED));
		createProvisioningOperationTwo = provisioningOperationService.save(createProvisioningOperationTwo);
		SysProvisioningAttribute attributeTwo = provisioningAttributeRepository.save(
				new SysProvisioningAttribute(createProvisioningOperationTwo.getId(), getHelper().createName()));
		
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(systemOne.getId());
		Assert.assertEquals(1, provisioningOperationService.find(filter,  null).getTotalElements());
		filter.setSystemId(systemTwo.getId());
		Assert.assertEquals(1, provisioningOperationService.find(filter,  null).getTotalElements());
		//
		provisioningOperationService.deleteOperations(systemOne.getId());
		Assert.assertNull(provisioningAttributeRepository.findById(attributeOne.getId()).orElse(null));
		Assert.assertNotNull(provisioningAttributeRepository.findById(attributeTwo.getId()).get());
		//
		filter.setSystemId(systemOne.getId());
		Assert.assertEquals(0, provisioningOperationService.find(filter,  null).getTotalElements());
		filter.setSystemId(systemTwo.getId());
		Assert.assertEquals(1, provisioningOperationService.find(filter,  null).getTotalElements());
	}
	
	@Test
	public void testFillProvisioningAttributes() {
		SysSystemDto system = getHelper().createTestResourceSystem(true);
		system.setReadonly(true);
		system = systemService.save(system);
		SysProvisioningOperationDto provisioningOperation = createProvisioningOperation(system, getHelper().createName());
		// publish event
		provisioningExecutor.execute(provisioningOperation);
		//
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemEntity(provisioningOperation.getSystemEntity());
		filter.setSystemId(system.getId());
		filter.setEmptyProvisioning(Boolean.FALSE);
		//
		Assert.assertFalse(provisioningOperationService.find(filter, null).getContent().isEmpty());
	}
	
	@Test
	public void testNotExecutedOperationOnDisabledSystem() {
		SysSystemDto system = getHelper().createTestResourceSystem(true);
		system.setReadonly(true);
		system = systemService.save(system);

		IdmRoleDto role = getHelper().createRole();
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		getHelper().createRoleSystem(role, system);
		getHelper().assignRoles(contract, role);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());

		List<SysProvisioningOperationDto> provisionings = provisioningOperationService.find(filter, null).getContent();
		assertEquals(1, provisionings.size());
		assertEquals(OperationState.NOT_EXECUTED, provisionings.get(0).getResultState());
		String uid = provisionings.get(0).getSystemEntityUid();
		
		// Account cannot exist now
		TestResource resource = getHelper().findResource(uid);
		Assert.assertNull(resource);
		
		// Set system as enabled.
		system.setReadonly(false);
		system = systemService.save(system);
		
		// Execute the provisioning -> System is enabled now, but in provisioning queue
		// is active operation -> so next provisioning cannot be executed!
		identitySerivce.save(identity);

		// Account cannot exist now
		resource = getHelper().findResource(uid);
		Assert.assertNull(resource);

		provisionings = provisioningOperationService.find(filter, null).getContent();
		assertEquals(2, provisionings.size());
		assertEquals(OperationState.NOT_EXECUTED, provisionings.get(0).getResultState());
		assertEquals(OperationState.NOT_EXECUTED, provisionings.get(1).getResultState());

	}
	
	/**
	 * Provisioning content - account object
	 * 
	 * @param systemEntity
	 * @return
	 */
	private Map<ProvisioningAttributeDto, Object> createAccountObject(SysSystemEntityDto systemEntity, String firstname) {
		ProvisioningAttributeDto nameAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_NAME);
		ProvisioningAttributeDto firstNameAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME);
		ProvisioningAttributeDto lastNameAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_LASTNAME);
		ProvisioningAttributeDto passwordAttribute = getProvisioningAttribute(TestHelper.ATTRIBUTE_MAPPING_PASSWORD);
		//
		Map<ProvisioningAttributeDto, Object> accoutObject = new HashMap<>();		
		accoutObject.put(nameAttribute, systemEntity.getUid());
		accoutObject.put(firstNameAttribute, firstname == null ? "firstOne" : firstname);
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
	private SysProvisioningOperationDto createProvisioningOperation(SysSystemDto system, String firstname) {
		ProvisioningContext context = new ProvisioningContext();
		SysSystemEntityDto systemEntity = helper.createSystemEntity(system);
		Map<ProvisioningAttributeDto, Object> accoutObject = createAccountObject(systemEntity, firstname);
		context.setAccountObject(accoutObject);
		//
		// prepare provisioning operation
		SysSystemMappingDto systemMapping = helper.getDefaultMapping(system);
		IcObjectClass objectClass = new IcObjectClassImpl(schemaObjectClassService.get(systemMapping.getObjectClass()).getObjectClassName());
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(null, objectClass, null);
		SysProvisioningOperationDto.Builder operationBuilder = new SysProvisioningOperationDto.Builder()
				.setSystem(system.getId())
				.setOperationType(ProvisioningOperationType.CREATE)
				.setSystemEntity(systemEntity)
				.setProvisioningContext(new ProvisioningContext(accoutObject, connectorObject));
		SysProvisioningOperationDto operation = operationBuilder.build();
		// set default result state
		operation.setResult(new OperationResult(OperationState.CREATED));
		//
		return operation;
	}
	
	private SysProvisioningOperationDto updateProvisioningOperation(SysSystemEntityDto systemEntity, String firstname) {
		ProvisioningContext context = new ProvisioningContext();
		Map<ProvisioningAttributeDto, Object> accoutObject = createAccountObject(systemEntity, firstname);
		context.setAccountObject(accoutObject);
		//
		// prepare provisioning operation
		SysSystemMappingDto systemMapping = helper.getDefaultMapping(systemEntity.getSystem());
		IcObjectClass objectClass = new IcObjectClassImpl(schemaObjectClassService.get(systemMapping.getObjectClass()).getObjectClassName());
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(null, objectClass, null);
		SysProvisioningOperationDto.Builder operationBuilder = new SysProvisioningOperationDto.Builder()
				.setOperationType(ProvisioningOperationType.UPDATE)
				.setSystemEntity(systemEntity)
				.setProvisioningContext(new ProvisioningContext(accoutObject, connectorObject));
		return operationBuilder.build();
	}
	
	/**
	 * Return provisioning attribute by default mapping and strategy
	 * 
	 * @return
	 */
	private ProvisioningAttributeDto getProvisioningAttribute(String name) {
		// load attribute mapping is not needed now - name is the same on both (tree) sides
		return new ProvisioningAttributeDto(name, AttributeMappingStrategyType.SET);
	}
	
	public eu.bcvsolutions.idm.acc.TestHelper getHelper() {
		return (eu.bcvsolutions.idm.acc.TestHelper) super.getHelper();
	}
}
