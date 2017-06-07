package eu.bcvsolutions.idm.acc.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.sql.DataSource;

import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;

import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SyncActionLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SyncItemLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncLog;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.entity.TestRoleResource;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.service.impl.DefaultSynchronizationService;
import eu.bcvsolutions.idm.core.api.domain.RoleType;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmRoleFormValue;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.ic.domain.IcFilterOperationType;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;


/**
 * Role synchronization and provisioning tests
 * 
 * @author Svanda
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Rollback(false)
@Service
public class DefaultRoleSynchronizationServiceTest extends AbstractIntegrationTest {

	private static final String SYNC_CONFIG_NAME = "syncConfigNameRole";
	private static final String SYSTEM_NAME = "systemRole";
	private static final String ATTRIBUTE_NAME = "__NAME__";
	private static final String CHANGED = "changed";
	private static final String ROLE_NAME_TEN = "roleName10";
	private static final String DATE_TABLE_CONNECTOR_FORMAT = "yyyy-MM-dd HH:mm:ss";
	

	@Autowired
	private ApplicationContext context;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysSystemAttributeMappingService schemaAttributeMappingService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private SysSyncConfigService syncConfigService;
	@Autowired
	private SysSyncLogService syncLogService;
	@Autowired
	private SysSyncItemLogService syncItemLogService;
	@Autowired
	private SysSyncActionLogService syncActionLogService;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private FormService formService;

	@Autowired
	DataSource dataSource;

	// Only for call method createTestSystem
	@Autowired
	private DefaultSysAccountManagementServiceTest defaultSysAccountManagementServiceTest;
	private SysSystem system;
	private SynchronizationService synchornizationService;

	@Before
	public void init() {
		loginAsAdmin("admin");
		synchornizationService = context.getAutowireCapableBeanFactory().createBean(DefaultSynchronizationService.class);
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	@Transactional
	public void doCreateSyncConfig() {
		
		initData();

		SystemMappingFilter mappingFilter = new SystemMappingFilter();
		mappingFilter.setEntityType(SystemEntityType.ROLE);
		mappingFilter.setSystemId(system.getId());
		mappingFilter.setOperationType(SystemOperationType.SYNCHRONIZATION);
		List<SysSystemMapping> mappings = systemMappingService.find(mappingFilter, null).getContent();
		Assert.assertEquals(1, mappings.size());
		SysSystemMapping mapping = mappings.get(0);
		SystemAttributeMappingFilter attributeMappingFilter = new SystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(mapping.getId());

		List<SysSystemAttributeMapping> attributes = schemaAttributeMappingService.find(attributeMappingFilter, null)
				.getContent();
		SysSystemAttributeMapping uidAttribute = attributes.stream().filter(attribute -> {
			return attribute.isUid();
		}).findFirst().orElse(null);
		
		SysSystemAttributeMapping tokenAttribute = attributes.stream().filter(attribute -> {
			return "changed".equals(attribute.getIdmPropertyName());
		}).findFirst().orElse(null);


		// Create default synchronization config
		SysSyncConfig syncConfigCustom = new SysSyncConfig();
		syncConfigCustom.setReconciliation(false);
		syncConfigCustom.setCustomFilter(true);
		syncConfigCustom.setSystemMapping(mapping);
		syncConfigCustom.setCorrelationAttribute(uidAttribute);
		syncConfigCustom.setTokenAttribute(tokenAttribute);
		syncConfigCustom.setName(SYNC_CONFIG_NAME);
		syncConfigCustom.setLinkedAction(SynchronizationLinkedActionType.IGNORE);
		syncConfigCustom.setUnlinkedAction(SynchronizationUnlinkedActionType.IGNORE);
		syncConfigCustom.setMissingEntityAction(SynchronizationMissingEntityActionType.CREATE_ENTITY);
		syncConfigCustom.setMissingAccountAction(ReconciliationMissingAccountActionType.IGNORE);

		syncConfigService.save(syncConfigCustom);

		SynchronizationConfigFilter configFilter = new SynchronizationConfigFilter();
		configFilter.setSystemId(system.getId());
		Assert.assertEquals(1, syncConfigService.find(configFilter, null).getTotalElements());
	}

	@Test
	public void doStartSyncA_MissingEntity() {
		SynchronizationConfigFilter configFilter = new SynchronizationConfigFilter();
		configFilter.setName(SYNC_CONFIG_NAME);
		List<SysSyncConfig> syncConfigs = syncConfigService.find(configFilter, null).getContent();

		Assert.assertEquals(1, syncConfigs.size());
		SysSyncConfig syncConfigCustom = syncConfigs.get(0);
		Assert.assertFalse(syncConfigService.isRunning(syncConfigCustom));
		//
		synchornizationService.setSynchronizationConfigId(syncConfigCustom.getId());
		synchornizationService.process();
		//		
		SynchronizationLogFilter logFilter = new SynchronizationLogFilter();
		logFilter.setSynchronizationConfigId(syncConfigCustom.getId());
		List<SysSyncLog> logs = syncLogService.find(logFilter, null).getContent();
		Assert.assertEquals(1, logs.size());
		SysSyncLog log = logs.get(0);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		SyncActionLogFilter actionLogFilter = new SyncActionLogFilter();
		actionLogFilter.setSynchronizationLogId(log.getId());
		List<SysSyncActionLog> actions = syncActionLogService.find(actionLogFilter, null).getContent();
		Assert.assertEquals(1, actions.size());

		SysSyncActionLog createEntityActionLog = actions.stream().filter(action -> {
			return SynchronizationActionType.CREATE_ENTITY == action.getSyncAction();
		}).findFirst().get();

		SyncItemLogFilter itemLogFilter = new SyncItemLogFilter();
		itemLogFilter.setSyncActionLogId(createEntityActionLog.getId());
		List<SysSyncItemLog> items = syncItemLogService.find(itemLogFilter, null).getContent();
		Assert.assertEquals(5, items.size());
		
		RoleFilter roleFilter = new RoleFilter();
		roleFilter.setProperty(IdmRole_.name.getName());
		roleFilter.setValue("1");
		
		Assert.assertEquals(1, roleService.find(roleFilter, null).getTotalElements());
		
		roleFilter.setValue("2");
		Assert.assertEquals(1, roleService.find(roleFilter, null).getTotalElements());
		
		roleFilter.setValue("3");
		Assert.assertEquals(1, roleService.find(roleFilter, null).getTotalElements());
		
		roleFilter.setValue("4");
		Assert.assertEquals(1, roleService.find(roleFilter, null).getTotalElements());
		
		roleFilter.setValue("5");
		Assert.assertEquals(1, roleService.find(roleFilter, null).getTotalElements());

		// Delete log
		syncLogService.delete(log);
	}

	@Test
	public void doStartSyncB_Linked_doEntityUpdate() {
		SynchronizationConfigFilter configFilter = new SynchronizationConfigFilter();
		configFilter.setName(SYNC_CONFIG_NAME);
		List<SysSyncConfig> syncConfigs = syncConfigService.find(configFilter, null).getContent();

		//Change node code to changed
		this.getBean().changeOne();

		Assert.assertEquals(1, syncConfigs.size());
		SysSyncConfig syncConfigCustom = syncConfigs.get(0);
		Assert.assertFalse(syncConfigService.isRunning(syncConfigCustom));

		// Set sync config
		syncConfigCustom.setLinkedAction(SynchronizationLinkedActionType.UPDATE_ENTITY);
		syncConfigCustom.setUnlinkedAction(SynchronizationUnlinkedActionType.IGNORE);
		syncConfigCustom.setMissingEntityAction(SynchronizationMissingEntityActionType.IGNORE);
		syncConfigCustom.setMissingAccountAction(ReconciliationMissingAccountActionType.IGNORE);
		syncConfigService.save(syncConfigCustom);

		// Check state before sync
		RoleFilter roleFilter = new RoleFilter();
		roleFilter.setProperty(IdmRole_.name.getName());
		roleFilter.setValue("1");
		Assert.assertEquals("1", roleService.find(roleFilter, null).getContent().get(0).getDescription());

		synchornizationService.setSynchronizationConfigId(syncConfigCustom.getId());
		synchornizationService.process();
		//
		SynchronizationLogFilter logFilter = new SynchronizationLogFilter();
		logFilter.setSynchronizationConfigId(syncConfigCustom.getId());
		List<SysSyncLog> logs = syncLogService.find(logFilter, null).getContent();
		Assert.assertEquals(1, logs.size());
		SysSyncLog log = logs.get(0);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		SyncActionLogFilter actionLogFilter = new SyncActionLogFilter();
		actionLogFilter.setSynchronizationLogId(log.getId());
		List<SysSyncActionLog> actions = syncActionLogService.find(actionLogFilter, null).getContent();
		Assert.assertEquals(1, actions.size());

		SysSyncActionLog actionLog = actions.stream().filter(action -> {
			return SynchronizationActionType.UPDATE_ENTITY == action.getSyncAction();
		}).findFirst().get();

		SyncItemLogFilter itemLogFilter = new SyncItemLogFilter();
		itemLogFilter.setSyncActionLogId(actionLog.getId());
		List<SysSyncItemLog> items = syncItemLogService.find(itemLogFilter, null).getContent();
		Assert.assertEquals(5, items.size());

		// Check state after sync
		Assert.assertEquals(CHANGED, roleService.find(roleFilter, null).getContent().get(0).getDescription());

		// Delete log
		syncLogService.delete(log);

	}
	
	@Test
	public void doStartSyncB_MissingAccount_DeleteEntity() {
		SynchronizationConfigFilter configFilter = new SynchronizationConfigFilter();
		configFilter.setName(SYNC_CONFIG_NAME);
		List<SysSyncConfig> syncConfigs = syncConfigService.find(configFilter, null).getContent();

		//Remove node code to changed
		this.getBean().removeOne();

		Assert.assertEquals(1, syncConfigs.size());
		SysSyncConfig syncConfigCustom = syncConfigs.get(0);
		Assert.assertFalse(syncConfigService.isRunning(syncConfigCustom));

		// Set sync config
		syncConfigCustom.setLinkedAction(SynchronizationLinkedActionType.IGNORE);
		syncConfigCustom.setUnlinkedAction(SynchronizationUnlinkedActionType.IGNORE);
		syncConfigCustom.setMissingEntityAction(SynchronizationMissingEntityActionType.IGNORE);
		syncConfigCustom.setMissingAccountAction(ReconciliationMissingAccountActionType.DELETE_ENTITY);
		syncConfigCustom.setReconciliation(true);
		syncConfigService.save(syncConfigCustom);

		// Check state before sync
		RoleFilter roleFilter = new RoleFilter();
		roleFilter.setProperty(IdmRole_.name.getName());
		roleFilter.setValue("1");
		IdmRole roleOne = roleService.find(roleFilter, null).getContent().get(0);
		Assert.assertNotNull(roleOne);
		
		synchornizationService.setSynchronizationConfigId(syncConfigCustom.getId());
		synchornizationService.process();
		//
		SynchronizationLogFilter logFilter = new SynchronizationLogFilter();
		logFilter.setSynchronizationConfigId(syncConfigCustom.getId());
		List<SysSyncLog> logs = syncLogService.find(logFilter, null).getContent();
		Assert.assertEquals(1, logs.size());
		SysSyncLog log = logs.get(0);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		SyncActionLogFilter actionLogFilter = new SyncActionLogFilter();
		actionLogFilter.setSynchronizationLogId(log.getId());
		List<SysSyncActionLog> actions = syncActionLogService.find(actionLogFilter, null).getContent();
		Assert.assertEquals(2, actions.size());

		SysSyncActionLog actionLog = actions.stream().filter(action -> {
			return SynchronizationActionType.DELETE_ENTITY == action.getSyncAction();
		}).findFirst().get();

		SyncItemLogFilter itemLogFilter = new SyncItemLogFilter();
		itemLogFilter.setSyncActionLogId(actionLog.getId());
		List<SysSyncItemLog> items = syncItemLogService.find(itemLogFilter, null).getContent();
		Assert.assertEquals(1, items.size());

		// Check state after sync
		roleOne = roleService.get(roleOne.getId());
		Assert.assertNull(roleOne);

		// Delete log
		syncLogService.delete(log);

	}
	
	@Test
	public void doStartSyncC_filterByToken() {
		SynchronizationConfigFilter configFilter = new SynchronizationConfigFilter();
		configFilter.setName(SYNC_CONFIG_NAME);
		List<SysSyncConfig> syncConfigs = syncConfigService.find(configFilter, null).getContent();

		Assert.assertEquals(1, syncConfigs.size());
		SysSyncConfig syncConfigCustom = syncConfigs.get(0);
		Assert.assertFalse(syncConfigService.isRunning(syncConfigCustom));
		
		RoleFilter roleFilter = new RoleFilter();
		roleFilter.setProperty(IdmRole_.name.getName());
		roleFilter.setValue("3");
		IdmRole roleThree = roleService.find(roleFilter, null).getContent().get(0);
		Assert.assertNotNull(roleThree);
		IdmRoleFormValue changedRole = (IdmRoleFormValue) formService.getValues(roleThree, "changed").get(0);
		Assert.assertNotNull(changedRole);

		// Set sync config
		syncConfigCustom.setReconciliation(false);
		syncConfigCustom.setCustomFilter(true);
		syncConfigCustom.setFilterOperation(IcFilterOperationType.GREATER_THAN);
		syncConfigCustom.setFilterAttribute(syncConfigCustom.getTokenAttribute());
		syncConfigCustom.setToken(changedRole.getStringValue());
		syncConfigCustom.setLinkedAction(SynchronizationLinkedActionType.UPDATE_ENTITY);
		syncConfigCustom.setUnlinkedAction(SynchronizationUnlinkedActionType.IGNORE);
		syncConfigCustom.setMissingEntityAction(SynchronizationMissingEntityActionType.IGNORE);
		syncConfigCustom.setMissingAccountAction(ReconciliationMissingAccountActionType.IGNORE);
		syncConfigService.save(syncConfigCustom);
		//
		synchornizationService.setSynchronizationConfigId(syncConfigCustom.getId());
		synchornizationService.process();
		//		
		SynchronizationLogFilter logFilter = new SynchronizationLogFilter();
		logFilter.setSynchronizationConfigId(syncConfigCustom.getId());
		List<SysSyncLog> logs = syncLogService.find(logFilter, null).getContent();
		Assert.assertEquals(1, logs.size());
		SysSyncLog log = logs.get(0);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		SyncActionLogFilter actionLogFilter = new SyncActionLogFilter();
		actionLogFilter.setSynchronizationLogId(log.getId());
		List<SysSyncActionLog> actions = syncActionLogService.find(actionLogFilter, null).getContent();
		Assert.assertEquals(1, actions.size());

		SysSyncActionLog createEntityActionLog = actions.stream().filter(action -> {
			return SynchronizationActionType.UPDATE_ENTITY == action.getSyncAction();
		}).findFirst().get();

		SyncItemLogFilter itemLogFilter = new SyncItemLogFilter();
		itemLogFilter.setSyncActionLogId(createEntityActionLog.getId());
		List<SysSyncItemLog> items = syncItemLogService.find(itemLogFilter, null).getContent();
		Assert.assertEquals(2, items.size());
	
		SysSyncItemLog item = items.stream().filter(logitem -> {
			return "4".equals(logitem.getIdentification());
		}).findFirst().orElse(null);
		Assert.assertNotNull("Log for role 4 must exist!", item);
		
		item = items.stream().filter(logitem -> {
			return "5".equals(logitem.getIdentification());
		}).findFirst().orElse(null);
		Assert.assertNotNull("Log for role 5 must exist!", item);
		
		// Delete log
		syncLogService.delete(log);
	}
	
	@Test
	@Transactional
	public void provisioningA_CreateAccount_withOutMapping() {
		
		// Delete all resource data
		this.deleteAllResourceData();
		
		
		// Create role in IDM tree
		IdmRole roleTen = new IdmRole();
		roleTen.setName(ROLE_NAME_TEN);
		roleTen.setPriority(2);
		roleService.save(roleTen);
		
		// Check state before provisioning
		TestRoleResource one = entityManager.find(TestRoleResource.class, ROLE_NAME_TEN);
		Assert.assertNull(one);
	}
	
	@Test
	public void provisioningB_CreateAccounts() {

		RoleFilter filter = new RoleFilter();
		filter.setProperty(IdmRole_.name.getName());
		filter.setValue(ROLE_NAME_TEN);

		IdmRole roleTen = roleService.find(filter, null).getContent().get(0);
		Assert.assertNotNull(roleTen);

		// Check state before provisioning
		TestRoleResource ten = entityManager.find(TestRoleResource.class, ROLE_NAME_TEN);
		Assert.assertNull(ten);

		// Create mapping for provisioning
		this.createProvisionigMapping();

		// Save IDM role (must invoke provisioning)
		roleService.save(roleTen);
		
		// Check state before provisioning
		ten = entityManager.find(TestRoleResource.class, ROLE_NAME_TEN);
		Assert.assertNotNull(ten);
	}
	
	@Test
	public void provisioningD_UpdateAccount() {
		
		RoleFilter filter = new RoleFilter();
		filter.setProperty(IdmRole_.name.getName());
		filter.setValue(ROLE_NAME_TEN);

		IdmRole roleTen = roleService.find(filter, null).getContent().get(0);
		Assert.assertNotNull(roleTen);
		
		// Check state before provisioning
		TestRoleResource ten = entityManager.find(TestRoleResource.class, ROLE_NAME_TEN);
		Assert.assertNotNull(ten);
		Assert.assertEquals(2, ten.getPriority());
		
		roleTen.setPriority(10);
		
		// Save IDM changed node (must invoke provisioning)
		roleService.save(roleTen);
		
		// Check state after provisioning
		ten = entityManager.find(TestRoleResource.class, ROLE_NAME_TEN);
		Assert.assertNotNull(ten);
		Assert.assertEquals(10, ten.getPriority());
	}
	
	@Test
	public void provisioningD_UpdateAccount_Extended_Attribute() {
		
		RoleFilter filter = new RoleFilter();
		filter.setProperty(IdmRole_.name.getName());
		filter.setValue(ROLE_NAME_TEN);

		IdmRole roleTen = roleService.find(filter, null).getContent().get(0);
		Assert.assertNotNull(roleTen);
		Assert.assertTrue(formService.getValues(roleTen, "changed").isEmpty());
		
		// Check state before provisioning
		TestRoleResource ten = entityManager.find(TestRoleResource.class, ROLE_NAME_TEN);
		Assert.assertNotNull(ten);
		Assert.assertEquals(null, ten.getModified());
	
		// Create extended attribute
		LocalDateTime now = LocalDateTime.now();
		formService.saveValues(roleTen, "changed", ImmutableList.of(now.toString(DATE_TABLE_CONNECTOR_FORMAT)));
		
		// Save IDM changed node (must invoke provisioning)
		roleService.save(roleTen);
		
		// Check state after provisioning
		ten = entityManager.find(TestRoleResource.class, ROLE_NAME_TEN);
		Assert.assertNotNull(ten);
		Assert.assertEquals(now.toString(DATE_TABLE_CONNECTOR_FORMAT), ten.getModified().toString(DATE_TABLE_CONNECTOR_FORMAT));
	}
	
	@Test
	public void provisioningF_DeleteAccount() {
		
		RoleFilter filter = new RoleFilter();
		filter.setProperty(IdmRole_.name.getName());
		filter.setValue(ROLE_NAME_TEN);

		IdmRole roleTen = roleService.find(filter, null).getContent().get(0);
		Assert.assertNotNull(roleTen);
		
		TestRoleResource ten = entityManager.find(TestRoleResource.class, ROLE_NAME_TEN);
		Assert.assertNotNull(ten);
		
		// Delete IDM role (must invoke provisioning)
		roleService.delete(roleTen);
		
		ten = entityManager.find(TestRoleResource.class, ROLE_NAME_TEN);
		Assert.assertNull(ten);
	}
	
	

	@Transactional
	public void deleteAllResourceData() {
		// Delete all
		Query q = entityManager.createNativeQuery("DELETE FROM test_role_resource");
		q.executeUpdate();
	}

	private void createProvisionigMapping() {

		SynchronizationConfigFilter configFilter = new SynchronizationConfigFilter();
		configFilter.setName(SYNC_CONFIG_NAME);
		List<SysSyncConfig> syncConfigs = syncConfigService.find(configFilter, null).getContent();
		
		Assert.assertEquals(1, syncConfigs.size());
		SysSyncConfig syncConfigCustom = syncConfigs.get(0);
	
		SysSystemMapping systemMappingSync = syncConfigCustom.getSystemMapping();
		
		// Create provisioning mapping
		SysSystemMapping systemMapping = new SysSystemMapping();
		systemMapping.setName("default_" + System.currentTimeMillis());
		systemMapping.setEntityType(SystemEntityType.ROLE);
		systemMapping.setOperationType(SystemOperationType.PROVISIONING);
		systemMapping.setObjectClass(systemMappingSync.getObjectClass());
		final SysSystemMapping syncMapping = systemMappingService.save(systemMapping);

		createMapping(systemMappingSync.getSystem(), syncMapping);

	}
	
	private void initData() {

		// create test system
		system = defaultSysAccountManagementServiceTest.createTestSystem("test_role_resource");
		system.setName(SYSTEM_NAME);
		system = systemService.save(system);

		// generate schema for system
		List<SysSchemaObjectClass> objectClasses = systemService.generateSchema(system);
		
		// Create synchronization mapping
		SysSystemMapping syncSystemMapping = new SysSystemMapping();
		syncSystemMapping.setName("default_" + System.currentTimeMillis());
		syncSystemMapping.setEntityType(SystemEntityType.ROLE);
		syncSystemMapping.setOperationType(SystemOperationType.SYNCHRONIZATION);
		syncSystemMapping.setObjectClass(objectClasses.get(0));
		final SysSystemMapping syncMapping = systemMappingService.save(syncSystemMapping);

		createMapping(system, syncMapping);
		initRoleData();
		
		syncConfigService.find(null).getContent().forEach(config -> {
			syncConfigService.delete(config);
		});

	}
	
	private void initRoleData(){
		deleteAllResourceData();
		LocalDateTime now = LocalDateTime.now();
		entityManager.persist(this.createRole("1", RoleType.TECHNICAL.name(), now.plusHours(1), 0));
		entityManager.persist(this.createRole("2", RoleType.TECHNICAL.name(), now.plusHours(2), 0));
		entityManager.persist(this.createRole("3", RoleType.TECHNICAL.name(), now.plusHours(3), 0));
		entityManager.persist(this.createRole("4", RoleType.SYSTEM.name(), now.plusHours(4), 1));
		entityManager.persist(this.createRole("5", RoleType.BUSINESS.name(), now.plusHours(5), 1));

	}
	
	private TestRoleResource createRole(String code, String type, LocalDateTime changed, int priority){
		TestRoleResource role = new TestRoleResource();
		role.setType(type);
		role.setName(code);
		role.setPriority(priority);
		role.setModified(changed);
		role.setDescription(code);
		return role;
	}
	
	@Transactional
	public void changeOne(){
		TestRoleResource one = entityManager.find(TestRoleResource.class, "1");
		one.setDescription(CHANGED);
		entityManager.persist(one);
	}
	
	@Transactional
	public void removeOne(){
		TestRoleResource one = entityManager.find(TestRoleResource.class, "1");
		entityManager.remove(one);
	}
	

	private void createMapping(SysSystem system, final SysSystemMapping entityHandlingResult) {
		SchemaAttributeFilter schemaAttributeFilter = new SchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());

		Page<SysSchemaAttribute> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		schemaAttributesPage.forEach(schemaAttr -> {
			if (ATTRIBUTE_NAME.equals(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeHandlingName = new SysSystemAttributeMapping();
				attributeHandlingName.setUid(true);
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setIdmPropertyName("name");
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				// For provisioning .. we need create UID
				attributeHandlingName.setTransformToResourceScript("return entity.getName();");
				attributeHandlingName.setSystemMapping(entityHandlingResult);
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("TYPE".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeHandlingName = new SysSystemAttributeMapping();
				attributeHandlingName.setIdmPropertyName("roleType");
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSystemMapping(entityHandlingResult);
				schemaAttributeMappingService.save(attributeHandlingName);
			
			} else if ("PRIORITY".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeHandlingName = new SysSystemAttributeMapping();
				attributeHandlingName.setIdmPropertyName("priority");
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSystemMapping(entityHandlingResult);
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("APPROVE_REMOVE".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeHandlingName = new SysSystemAttributeMapping();
				attributeHandlingName.setIdmPropertyName("approveRemove");
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setSystemMapping(entityHandlingResult);
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("MODIFIED".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeHandlingName = new SysSystemAttributeMapping();
				attributeHandlingName.setIdmPropertyName("changed");
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setEntityAttribute(false);
				attributeHandlingName.setExtendedAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setSystemMapping(entityHandlingResult);
				schemaAttributeMappingService.save(attributeHandlingName);
	
			} else if ("DESCRIPTION".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeHandlingName = new SysSystemAttributeMapping();
				attributeHandlingName.setIdmPropertyName("description");
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setEntityAttribute(true);;
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setSystemMapping(entityHandlingResult);
				schemaAttributeMappingService.save(attributeHandlingName);
	
			}
		});
	}

	private DefaultRoleSynchronizationServiceTest getBean() {
		return applicationContext.getBean(this.getClass());
	}
}
