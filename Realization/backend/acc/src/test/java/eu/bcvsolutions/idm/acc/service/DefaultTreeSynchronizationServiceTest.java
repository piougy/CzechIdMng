package eu.bcvsolutions.idm.acc.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.sql.DataSource;

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
import eu.bcvsolutions.idm.acc.entity.TestTreeResource;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
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
import eu.bcvsolutions.idm.core.api.dto.filter.TreeNodeFilter;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.exception.TreeNodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;


/**
 * Tree synchronization tests
 * 
 * @author Svanda
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Rollback(false)
@Service
public class DefaultTreeSynchronizationServiceTest extends AbstractIntegrationTest {

	private static final String SYNC_CONFIG_NAME = "syncConfigName";
	private static final String SYSTEM_NAME = "systemTreeName";
	private static final String TREE_TYPE_TEST = "TREE_TEST";
	private static final String NODE_NAME = "name";
	private static final String ATTRIBUTE_NAME = "__NAME__";
	private static final String CHANGED = "changed";
	

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
	private IdmTreeTypeService treeTypeService;
	@Autowired
	private IdmTreeNodeService treeNodeService;
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
		mappingFilter.setEntityType(SystemEntityType.TREE);
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
		}).findFirst().get();


		// Create default synchronization config
		SysSyncConfig syncConfigCustom = new SysSyncConfig();
		syncConfigCustom.setReconciliation(true);
		syncConfigCustom.setCustomFilter(true);
		syncConfigCustom.setSystemMapping(mapping);
		syncConfigCustom.setCorrelationAttribute(uidAttribute);
		syncConfigCustom.setReconciliation(true);
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
		Assert.assertEquals(6, items.size());
		
		IdmTreeType treeType = treeTypeService.find(null).getContent().stream().filter(tree -> {
			return tree.getName().equals(TREE_TYPE_TEST);
		}).findFirst().get();
		
		Assert.assertEquals(1, treeNodeService.findRoots(treeType.getId(), null).getContent().size());

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
		TreeNodeFilter nodeFilter = new TreeNodeFilter();
		nodeFilter.setProperty(NODE_NAME);
		nodeFilter.setValue("111");
		IdmTreeNode treeNode = treeNodeService.find(nodeFilter, null).getContent().get(0);
		Assert.assertEquals("111", treeNode.getCode());

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
		Assert.assertEquals(6, items.size());

		// Check state after sync
		treeNode = treeNodeService.get(treeNode.getId());
		Assert.assertEquals(CHANGED, treeNode.getCode());

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
		syncConfigService.save(syncConfigCustom);

		// Check state before sync
		TreeNodeFilter nodeFilter = new TreeNodeFilter();
		nodeFilter.setProperty(NODE_NAME);
		nodeFilter.setValue("111");
		IdmTreeNode treeNode = treeNodeService.find(nodeFilter, null).getContent().get(0);
		Assert.assertNotNull(treeNode.getCode());
		
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
		Assert.assertEquals(3, actions.size());

		SysSyncActionLog actionLog = actions.stream().filter(action -> {
			return SynchronizationActionType.DELETE_ENTITY == action.getSyncAction();
		}).findFirst().get();

		SyncItemLogFilter itemLogFilter = new SyncItemLogFilter();
		itemLogFilter.setSyncActionLogId(actionLog.getId());
		List<SysSyncItemLog> items = syncItemLogService.find(itemLogFilter, null).getContent();
		Assert.assertEquals(1, items.size());

		// Check state after sync
		treeNode = treeNodeService.get(treeNode.getId());
		Assert.assertNull(treeNode);

		// Delete log
		syncLogService.delete(log);

	}
	
	@Test
	public void doStartSyncC_MissingEntity() {
		SynchronizationConfigFilter configFilter = new SynchronizationConfigFilter();
		configFilter.setName(SYNC_CONFIG_NAME);
		List<SysSyncConfig> syncConfigs = syncConfigService.find(configFilter, null).getContent();

		Assert.assertEquals(1, syncConfigs.size());
		SysSyncConfig syncConfigCustom = syncConfigs.get(0);
		Assert.assertFalse(syncConfigService.isRunning(syncConfigCustom));
		syncConfigCustom.setRootsFilterScript("if(account){ def parentValue = account.getAttributeByName(\"PARENT\").getValue();"
				+ " def uidValue = account.getAttributeByName(\"__NAME__\").getValue();"
				+ " if(parentValue != null && parentValue.equals(uidValue)){"
				+ "	 account.getAttributeByName(\"PARENT\").setValues(null); return Boolean.TRUE;}}"
				+ " \nreturn Boolean.FALSE;");
		// Set sync config
		syncConfigCustom.setLinkedAction(SynchronizationLinkedActionType.IGNORE);
		syncConfigCustom.setUnlinkedAction(SynchronizationUnlinkedActionType.IGNORE);
		syncConfigCustom.setMissingEntityAction(SynchronizationMissingEntityActionType.CREATE_ENTITY);
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
		Assert.assertEquals(2, actions.size());

		SysSyncActionLog createEntityActionLog = actions.stream().filter(action -> {
			return SynchronizationActionType.CREATE_ENTITY == action.getSyncAction();
		}).findFirst().get();

		SyncItemLogFilter itemLogFilter = new SyncItemLogFilter();
		itemLogFilter.setSyncActionLogId(createEntityActionLog.getId());
		List<SysSyncItemLog> items = syncItemLogService.find(itemLogFilter, null).getContent();
		Assert.assertEquals(6, items.size());
		
		IdmTreeType treeType = treeTypeService.find(null).getContent().stream().filter(tree -> {
			return tree.getName().equals(TREE_TYPE_TEST);
		}).findFirst().get();
		
		Assert.assertEquals(2, treeNodeService.findRoots(treeType.getId(), null).getContent().size());

		// Delete log
		syncLogService.delete(log);
	}
	
	@Test
	@Transactional
	public void provisioningA_CreateAccount_withOutMapping() {
		
		// Delete all resource data
		this.deleteAllResourceData();
		
		IdmTreeType treeType = treeTypeService.find(null).getContent().stream().filter(tree -> {
			return tree.getName().equals(TREE_TYPE_TEST);
		}).findFirst().get();
		
		// Create root node in IDM tree
		IdmTreeNode nodeRoot = new IdmTreeNode();
		nodeRoot.setCode("P1");
		nodeRoot.setName(nodeRoot.getCode());
		nodeRoot.setParent(null);
		nodeRoot.setTreeType(treeType);
		nodeRoot = treeNodeService.save(nodeRoot);

		// Create node in IDM tree
		IdmTreeNode nodeOne = new IdmTreeNode();
		nodeOne.setCode("P12");
		nodeOne.setName(nodeOne.getCode());
		nodeOne.setParent(nodeRoot);
		nodeOne.setTreeType(treeType);
		nodeOne = treeNodeService.save(nodeOne);
		
		// Check state before provisioning
		TestTreeResource one = entityManager.find(TestTreeResource.class, "P12");
		Assert.assertNull(one);
	}
	
	@Test(expected = ProvisioningException.class) // Provisioning tree in incorrect order
	public void provisioningB_CreateAccounts_withException() {

		TreeNodeFilter filter = new TreeNodeFilter();
		filter.setProperty(NODE_NAME);
		filter.setValue("P1");

		IdmTreeNode nodeRoot = treeNodeService.find(filter, null).getContent().get(0);
		Assert.assertNotNull(nodeRoot);

		filter.setValue("P12");
		IdmTreeNode nodeOne = treeNodeService.find(filter, null).getContent().get(0);
		Assert.assertNotNull(nodeOne);

		// Check state before provisioning
		TestTreeResource one = entityManager.find(TestTreeResource.class, "P12");
		Assert.assertNull(one);

		// Create mapping for provisioning
		this.createProvisionigMapping();

		// Save IDM node (must invoke provisioning)
		// We didn't provisioning for root first ... expect throw exception
		treeNodeService.save(nodeOne);
	}
	
	@Test
	@Transactional
	public void provisioningC_CreateAccounts_correct() {
		
		TreeNodeFilter filter = new TreeNodeFilter();
		filter.setProperty(NODE_NAME);
		filter.setValue("P1");
		
		IdmTreeNode nodeRoot = treeNodeService.find(filter, null).getContent().get(0);
		Assert.assertNotNull(nodeRoot);

		filter.setValue("P12");
		IdmTreeNode nodeOne = treeNodeService.find(filter, null).getContent().get(0);
		Assert.assertNotNull(nodeOne);
		
		// Check state before provisioning
		TestTreeResource one = entityManager.find(TestTreeResource.class, "P12");
		Assert.assertNull(one);
		TestTreeResource root = entityManager.find(TestTreeResource.class, "P1");
		Assert.assertNull(root);
		
		// Save IDM node again (must invoke provisioning)
		// Root first
		treeNodeService.save(nodeRoot);
		// Node next
		treeNodeService.save(nodeOne);
		
		// Check state before provisioning
		root = entityManager.find(TestTreeResource.class, "P1");
		Assert.assertNotNull(root);
		one = entityManager.find(TestTreeResource.class, "P12");
		Assert.assertNotNull(one);
	}
	
	
	@Test
	public void provisioningD_UpdateAccount() {
		
		TreeNodeFilter filter = new TreeNodeFilter();
		filter.setProperty(NODE_NAME);
		filter.setValue("P1");
		
		IdmTreeNode nodeRoot = treeNodeService.find(filter, null).getContent().get(0);
		Assert.assertNotNull(nodeRoot);

		filter.setValue("P12");
		IdmTreeNode nodeOne = treeNodeService.find(filter, null).getContent().get(0);
		Assert.assertNotNull(nodeOne);
		
		// Check state before provisioning
		TestTreeResource one = entityManager.find(TestTreeResource.class, "P12");
		Assert.assertNotNull(one);
		Assert.assertEquals("P12", one.getCode());
		
		nodeOne.setCode(CHANGED);
		
		// Save IDM changed node (must invoke provisioning)
		treeNodeService.save(nodeOne);
		
		// Check state before provisioning
		one = entityManager.find(TestTreeResource.class, "P12");
		Assert.assertNotNull(one);
		Assert.assertEquals(CHANGED, one.getCode());
	}
	
	@Test(expected=TreeNodeException.class)
	public void provisioningE_DeleteAccount_IntegrityException() {
		
		TreeNodeFilter filter = new TreeNodeFilter();
		filter.setProperty(NODE_NAME);
		filter.setValue("P1");
		
		IdmTreeNode nodeRoot = treeNodeService.find(filter, null).getContent().get(0);
		Assert.assertNotNull(nodeRoot);
		
		// Delete IDM node (must invoke provisioning) .. We delete node with some children ... must throw integrity exception
		// Generally we counts with provisioning on every node ... include children (Recursively delete is not good idea!) 
		treeNodeService.delete(nodeRoot);
	}
	
	@Test
	public void provisioningF_DeleteAccount() {
		
		TreeNodeFilter filter = new TreeNodeFilter();
		filter.setProperty(NODE_NAME);
		filter.setValue("P12");
		IdmTreeNode nodeOne = treeNodeService.find(filter, null).getContent().get(0);
		Assert.assertNotNull(nodeOne);
		
		// Delete IDM node (must invoke provisioning) .. We delete child
		treeNodeService.delete(nodeOne);
		
		Assert.assertTrue(treeNodeService.find(filter, null).getContent().isEmpty());
	}
	
	

	@Transactional
	public void deleteAllResourceData() {
		// Delete all
		Query q = entityManager.createNativeQuery("DELETE FROM test_tree_resource");
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
		systemMapping.setEntityType(SystemEntityType.TREE);
		systemMapping.setTreeType(systemMappingSync.getTreeType());
		systemMapping.setOperationType(SystemOperationType.PROVISIONING);
		systemMapping.setObjectClass(systemMappingSync.getObjectClass());
		final SysSystemMapping syncMapping = systemMappingService.save(systemMapping);

		createMapping(systemMappingSync.getSystem(), syncMapping);

	}
	
	private void initData() {

		// create test system
		system = defaultSysAccountManagementServiceTest.createTestSystem("test_tree_resource");
		system.setName(SYSTEM_NAME);
		system = systemService.save(system);
		// key to EAV
		IdmFormDefinition savedFormDefinition = systemService.getConnectorFormDefinition(system.getConnectorInstance());
		List<AbstractFormValue<SysSystem>> values = formService.getValues(system, savedFormDefinition);
		AbstractFormValue<SysSystem> changeLogColumn = values.stream().filter(value -> {return "keyColumn".equals(value.getFormAttribute().getCode());}).findFirst().get();
		formService.saveValues(system, changeLogColumn.getFormAttribute(), ImmutableList.of("ID"));
		// generate schema for system
		List<SysSchemaObjectClass> objectClasses = systemService.generateSchema(system);

		IdmTreeType treeType = new IdmTreeType();
		treeType.setCode(TREE_TYPE_TEST);
		treeType.setDefaultTreeType(false);
		treeType.setName(TREE_TYPE_TEST);
		treeType = treeTypeService.save(treeType);
		
		// Create synchronization mapping
		SysSystemMapping syncSystemMapping = new SysSystemMapping();
		syncSystemMapping.setName("default_" + System.currentTimeMillis());
		syncSystemMapping.setEntityType(SystemEntityType.TREE);
		syncSystemMapping.setTreeType(treeType);
		syncSystemMapping.setOperationType(SystemOperationType.SYNCHRONIZATION);
		syncSystemMapping.setObjectClass(objectClasses.get(0));
		final SysSystemMapping syncMapping = systemMappingService.save(syncSystemMapping);

		createMapping(system, syncMapping);
		initTreeData();
		
		syncConfigService.find(null).getContent().forEach(config -> {
			syncConfigService.delete(config);
		});

	}
	
	private void initTreeData(){
		deleteAllResourceData();
		
		entityManager.persist(this.createNode("1", null));
		entityManager.persist(this.createNode("2", "2"));
		
		entityManager.persist(this.createNode("11", "1"));
		entityManager.persist(this.createNode("12", "1"));
		entityManager.persist(this.createNode("111", "11"));
		entityManager.persist(this.createNode("112", "11"));
		entityManager.persist(this.createNode("1111", "111"));
		
		entityManager.persist(this.createNode("21", "2"));
		entityManager.persist(this.createNode("22", "2"));
		entityManager.persist(this.createNode("211", "21"));
		entityManager.persist(this.createNode("212", "21"));
		entityManager.persist(this.createNode("2111", "211"));
	}
	
	private TestTreeResource createNode(String code, String parent){
		TestTreeResource node = new TestTreeResource();
		node.setCode(code);
		node.setName(code);
		node.setParent(parent);
		node.setId(code);
		return node;
	}
	
	@Transactional
	public void changeOne(){
		TestTreeResource one = entityManager.find(TestTreeResource.class, "111");
		one.setCode(CHANGED);
		entityManager.persist(one);
	}
	
	@Transactional
	public void removeOne(){
		TestTreeResource one = entityManager.find(TestTreeResource.class, "111");
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
				attributeHandlingName.setEntityAttribute(false);
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				// For provisioning .. we need create UID
				attributeHandlingName.setTransformToResourceScript("if(uid){return uid;}\nreturn entity.getCode();");
				attributeHandlingName.setSystemMapping(entityHandlingResult);
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("CODE".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeHandlingName = new SysSystemAttributeMapping();
				attributeHandlingName.setIdmPropertyName("code");
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSystemMapping(entityHandlingResult);
				schemaAttributeMappingService.save(attributeHandlingName);
			
			} else if ("PARENT".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeHandlingName = new SysSystemAttributeMapping();
				attributeHandlingName.setIdmPropertyName("parent");
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSystemMapping(entityHandlingResult);
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("NAME".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMapping attributeHandlingName = new SysSystemAttributeMapping();
				attributeHandlingName.setIdmPropertyName("name");
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setSystemMapping(entityHandlingResult);
				schemaAttributeMappingService.save(attributeHandlingName);

			}
		});
	}

	private DefaultTreeSynchronizationServiceTest getBean() {
		return applicationContext.getBean(this.getClass());
	}
}
