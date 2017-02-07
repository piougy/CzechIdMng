package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningRequestRepository;
import eu.bcvsolutions.idm.acc.service.DefaultSysAccountManagementServiceTest;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.notification.service.api.NotificationManager;
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
	private SysProvisioningOperationRepository repository;
	@Autowired
	private SysProvisioningRequestRepository provisioningRequestRepository;
	@Autowired
	private SysProvisioningArchiveService provisioningArchiveService;
	@Autowired
	private EntityEventManager entityEventManager;
	@Autowired
	private SysProvisioningBatchService batchService;
	@Autowired
	private NotificationManager notificationManager;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysSystemEntityService systemEntityService;
	@Autowired
	private SysSystemMappingService mappingService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private SysSystemAttributeMappingService attributeMappingService;
	@Autowired
	private IcConnectorFacade connectorFacade;
	@Autowired
	private ConfidentialStorage confidentialStorage;
	//
	private DefaultSysProvisioningOperationService sysProvisioningOperationService;
	private DefaultProvisioningExecutor provisioningExecutor;
	private SysSystem system = null;
	private SysSystemMapping systemMapping = null;
	private SysSystemAttributeMapping nameAttributeMapping = null;
	private SysSystemAttributeMapping firstNameAttributeMapping = null;
	private SysSystemAttributeMapping lastNameAttributeMapping = null;
	private SysSystemAttributeMapping passwordAttributeMapping = null;
	
	// Only for call method createTestSystem
	@Autowired
	private DefaultSysAccountManagementServiceTest defaultSysAccountManagementServiceTest;
	
	@Before
	public void init() {	
		loginAsAdmin("admin");
		sysProvisioningOperationService = new DefaultSysProvisioningOperationService(
				repository, 
				provisioningRequestRepository, 
				provisioningArchiveService, 
				batchService, 
				notificationManager, 
				confidentialStorage);
		provisioningExecutor = new DefaultProvisioningExecutor(
				repository,
				entityEventManager, 
				sysProvisioningOperationService, 
				batchService, 
				notificationManager);
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	private void initSystem() {
		// prepare test system
		system = defaultSysAccountManagementServiceTest.createTestSystem();
		// generate schema
		List<SysSchemaObjectClass> objectClasses = systemService.generateSchema(system);
		// create test mapping
		systemMapping = new SysSystemMapping();
		systemMapping.setName("default_" + System.currentTimeMillis());
		systemMapping.setEntityType(SystemEntityType.IDENTITY);
		systemMapping.setOperationType(SystemOperationType.PROVISIONING);
		systemMapping.setObjectClass(objectClasses.get(0));
		mappingService.save(systemMapping);
		
		SchemaAttributeFilter schemaAttributeFilter = new SchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		Page<SysSchemaAttribute> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		for(SysSchemaAttribute schemaAttr : schemaAttributesPage) {
			if ("__NAME__".equals(schemaAttr.getName())) {
				nameAttributeMapping = new SysSystemAttributeMapping();
				nameAttributeMapping.setUid(true);
				nameAttributeMapping.setEntityAttribute(false);
				nameAttributeMapping.setName(schemaAttr.getName());
				nameAttributeMapping.setSchemaAttribute(schemaAttr);
				nameAttributeMapping.setSystemMapping(systemMapping);
				nameAttributeMapping = attributeMappingService.save(nameAttributeMapping);

			} else if ("firstname".equalsIgnoreCase(schemaAttr.getName())) {
				firstNameAttributeMapping = new SysSystemAttributeMapping();
				firstNameAttributeMapping.setIdmPropertyName("firstName");
				firstNameAttributeMapping.setSchemaAttribute(schemaAttr);
				firstNameAttributeMapping.setName(schemaAttr.getName());
				firstNameAttributeMapping.setSystemMapping(systemMapping);
				firstNameAttributeMapping = attributeMappingService.save(firstNameAttributeMapping);

			} else if ("lastname".equalsIgnoreCase(schemaAttr.getName())) {
				lastNameAttributeMapping = new SysSystemAttributeMapping();
				lastNameAttributeMapping.setIdmPropertyName("lastName");
				lastNameAttributeMapping.setName(schemaAttr.getName());
				lastNameAttributeMapping.setSchemaAttribute(schemaAttr);
				lastNameAttributeMapping.setSystemMapping(systemMapping);
				lastNameAttributeMapping = attributeMappingService.save(lastNameAttributeMapping);

			} else if (IcConnectorFacade.PASSWORD_ATTRIBUTE_NAME.equalsIgnoreCase(schemaAttr.getName())) {
				passwordAttributeMapping = new SysSystemAttributeMapping();
				passwordAttributeMapping.setIdmPropertyName("password");
				passwordAttributeMapping.setSchemaAttribute(schemaAttr);
				passwordAttributeMapping.setName(schemaAttr.getName());
				passwordAttributeMapping.setSystemMapping(systemMapping);
				passwordAttributeMapping = attributeMappingService.save(passwordAttributeMapping);
			}
		}
		assertNotNull(nameAttributeMapping);
		assertNotNull(firstNameAttributeMapping);
		assertNotNull(lastNameAttributeMapping);
		assertNotNull(passwordAttributeMapping);
	}
	
	private Map<String, Object> createAccountObject(SysSystemEntity systemEntity) {
		Map<String, Object> accoutObject = new HashMap<>();		
		accoutObject.put(nameAttributeMapping.getSchemaAttribute().getName(), systemEntity.getUid());
		accoutObject.put(firstNameAttributeMapping.getSchemaAttribute().getName(), "firstOne");
		accoutObject.put(lastNameAttributeMapping.getSchemaAttribute().getName(), "lastOne");
		accoutObject.put(passwordAttributeMapping.getSchemaAttribute().getName(), new GuardedString("password"));		
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
		Map<String, Object> accoutObject = createAccountObject(systemEntity);
		context.setAccountObject(accoutObject);
		GuardedString password = (GuardedString) accoutObject.get(passwordAttributeMapping.getSchemaAttribute().getName());
		//
		// publish event
		IcObjectClass objectClass = new IcObjectClassImpl(systemMapping.getObjectClass().getObjectClassName());
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(null, objectClass, null);
		SysProvisioningOperation.Builder operationBuilder = new SysProvisioningOperation.Builder()
				.setOperationType(ProvisioningOperationType.CREATE)
				.setSystem(system)
				.setSystemEntity(systemEntity)
				.setProvisioningContext(new ProvisioningContext(accoutObject, connectorObject));
		provisioningExecutor.execute(operationBuilder.build());
		//
		// check target account
		IcUidAttribute uidAttribute = new IcUidAttributeImpl(null, systemEntity.getUid(), null);
		IcConnectorObject existsConnectorObject = connectorFacade.readObject(
				system.getConnectorKey(), 
				systemService.getConnectorConfiguration(system), 
				objectClass, 
				uidAttribute);
		//
		assertNotNull(existsConnectorObject);
		assertEquals(systemEntity.getUid(), existsConnectorObject.getUidValue());
		assertEquals(accoutObject.get(firstNameAttributeMapping.getSchemaAttribute().getName()), 
				existsConnectorObject.getAttributeByName(firstNameAttributeMapping.getName()).getValue());
		assertEquals(accoutObject.get(lastNameAttributeMapping.getSchemaAttribute().getName()), 
				existsConnectorObject.getAttributeByName(lastNameAttributeMapping.getName()).getValue());
		// authenticate for password check
		IcUidAttribute attribute = connectorFacade.authenticateObject(
				system.getConnectorKey(), 
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
		Map<String, Object> accoutObject = createAccountObject(systemEntity);
		context.setAccountObject(accoutObject);
		GuardedString password = (GuardedString) accoutObject.get(passwordAttributeMapping.getSchemaAttribute().getName());
		//
		// publish event
		IcObjectClass objectClass = new IcObjectClassImpl(systemMapping.getObjectClass().getObjectClassName());
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(null, objectClass, null);
		SysProvisioningOperation.Builder operationBuilder = new SysProvisioningOperation.Builder()
				.setOperationType(ProvisioningOperationType.CREATE)
				.setSystem(system)
				.setSystemEntity(systemEntity)
				.setProvisioningContext(new ProvisioningContext(accoutObject, connectorObject));
		SysProvisioningOperation operation = provisioningExecutor.execute(operationBuilder.build());
		//
		assertEquals(OperationState.NOT_EXECUTED, operation.getResultState());
		assertEquals(AccResultCode.PROVISIONING_SYSTEM_DISABLED.name(), operation.getResult().getModel().getStatusEnum());
		//
		IcUidAttribute uidAttribute = new IcUidAttributeImpl(null, systemEntityUid, null);
		IcConnectorObject existsConnectorObject = connectorFacade.readObject(
				system.getConnectorKey(), 
				systemService.getConnectorConfiguration(system), 
				objectClass, 
				uidAttribute);
		//
		assertNull(existsConnectorObject);
		// password is stored in confidential storage
		assertNotNull(confidentialStorage.get(operation, sysProvisioningOperationService.createAccountObjectPropertyKey(passwordAttributeMapping.getSchemaAttribute().getName(), 0)));
		//
		system.setDisabled(false);
		systemService.save(system);
		//
		provisioningExecutor.execute(operation);
		//
		// check target account
		existsConnectorObject = connectorFacade.readObject(
				system.getConnectorKey(), 
				systemService.getConnectorConfiguration(system), 
				objectClass, 
				uidAttribute);
		//
		assertNotNull(existsConnectorObject);
		assertEquals(systemEntityUid, existsConnectorObject.getUidValue());
		assertEquals(accoutObject.get(firstNameAttributeMapping.getSchemaAttribute().getName()), 
				existsConnectorObject.getAttributeByName(firstNameAttributeMapping.getName()).getValue());
		assertEquals(accoutObject.get(lastNameAttributeMapping.getSchemaAttribute().getName()), 
				existsConnectorObject.getAttributeByName(lastNameAttributeMapping.getName()).getValue());
		// authenticate for password check
		IcUidAttribute attribute = connectorFacade.authenticateObject(
				system.getConnectorKey(), 
				systemService.getConnectorConfiguration(system), 
				objectClass,
				systemEntityUid, password);
		assertNotNull(attribute);
		assertEquals(systemEntityUid, attribute.getUidValue());
		// password is removed in confidential storage
		assertNull(confidentialStorage.get(operation, sysProvisioningOperationService.createAccountObjectPropertyKey(passwordAttributeMapping.getSchemaAttribute().getName(), 0)));
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
		Map<String, Object> accoutObject = createAccountObject(systemEntity);
		context.setAccountObject(accoutObject);
		GuardedString password = (GuardedString) accoutObject.get(passwordAttributeMapping.getSchemaAttribute().getName());
		//
		// publish event
		IcObjectClass objectClass = new IcObjectClassImpl(systemMapping.getObjectClass().getObjectClassName());
		IcConnectorObject connectorObject = new IcConnectorObjectImpl(null, objectClass, null);
		SysProvisioningOperation.Builder operationBuilder = new SysProvisioningOperation.Builder()
				.setOperationType(ProvisioningOperationType.CREATE)
				.setSystem(system)
				.setSystemEntity(systemEntity)
				.setProvisioningContext(new ProvisioningContext(accoutObject, connectorObject));
		SysProvisioningOperation operation = provisioningExecutor.execute(operationBuilder.build());
		//
		assertEquals(OperationState.NOT_EXECUTED, operation.getResultState());
		assertEquals(AccResultCode.PROVISIONING_SYSTEM_READONLY.name(), operation.getResult().getModel().getStatusEnum());
		//
		IcUidAttribute uidAttribute = new IcUidAttributeImpl(null, systemEntityUid, null);
		IcConnectorObject existsConnectorObject = connectorFacade.readObject(
				system.getConnectorKey(), 
				systemService.getConnectorConfiguration(system), 
				objectClass, 
				uidAttribute);
		//
		assertNull(existsConnectorObject);
		// passwords are stored in confidential storage
		assertNotNull(confidentialStorage.get(operation, sysProvisioningOperationService.createAccountObjectPropertyKey(passwordAttributeMapping.getSchemaAttribute().getName(), 0)));
		assertNotNull(confidentialStorage.get(operation, sysProvisioningOperationService.createConnectorObjectPropertyKey(operation.getProvisioningContext().getConnectorObject().getAttributeByName(passwordAttributeMapping.getSchemaAttribute().getName()), 0)));
		//
		system.setReadonly(false);
		systemService.save(system);
		//
		provisioningExecutor.execute(operation);
		//
		// check target account
		existsConnectorObject = connectorFacade.readObject(
				system.getConnectorKey(), 
				systemService.getConnectorConfiguration(system), 
				objectClass, 
				uidAttribute);
		//
		assertNotNull(existsConnectorObject);
		assertEquals(systemEntityUid, existsConnectorObject.getUidValue());
		assertEquals(accoutObject.get(firstNameAttributeMapping.getSchemaAttribute().getName()), 
				existsConnectorObject.getAttributeByName(firstNameAttributeMapping.getName()).getValue());
		assertEquals(accoutObject.get(lastNameAttributeMapping.getSchemaAttribute().getName()), 
				existsConnectorObject.getAttributeByName(lastNameAttributeMapping.getName()).getValue());
		// authenticate for password check
		IcUidAttribute attribute = connectorFacade.authenticateObject(
				system.getConnectorKey(), 
				systemService.getConnectorConfiguration(system), 
				objectClass,
				systemEntityUid, password);
		assertNotNull(attribute);
		assertEquals(systemEntityUid, attribute.getUidValue());
		// passwords are removed in confidential storage
		assertNull(confidentialStorage.get(operation, sysProvisioningOperationService.createAccountObjectPropertyKey(passwordAttributeMapping.getSchemaAttribute().getName(), 0)));
		assertNull(confidentialStorage.get(operation, sysProvisioningOperationService.createConnectorObjectPropertyKey(operation.getProvisioningContext().getConnectorObject().getAttributeByName(passwordAttributeMapping.getSchemaAttribute().getName()), 0)));
	}
	
	// TODO: batch test - create, update, update, delete - all has to be processed, batch needs to be cleared
}
