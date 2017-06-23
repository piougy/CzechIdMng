package eu.bcvsolutions.idm.acc.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationContext;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccTreeAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.EntityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.TreeAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncLog;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccTreeAccountService;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationEntityExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.CorrelationFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.TreeNodeFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.event.TreeNodeEvent;
import eu.bcvsolutions.idm.core.model.event.TreeNodeEvent.TreeNodeEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.filter.api.IcFilter;
import eu.bcvsolutions.idm.ic.filter.api.IcResultsHandler;
import eu.bcvsolutions.idm.ic.impl.IcAttributeImpl;
import eu.bcvsolutions.idm.ic.impl.IcLoginAttributeImpl;
import eu.bcvsolutions.idm.ic.impl.IcObjectClassImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Default implementation of tree sync
 * @author svandav
 *
 */
@Component
public class TreeSynchronizationExecutor extends AbstractSynchronizationExecutor<IdmTreeNodeDto>
		implements SynchronizationEntityExecutor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TreeSynchronizationExecutor.class);
	public final static String PARENT_FIELD = "parent";
	public final static String CODE_FIELD = "code";

	private final IdmTreeNodeService treeNodeService;
	private final AccTreeAccountService treeAccoutnService;
	private final IdmTreeNodeRepository treeNodeRepository;

	@Autowired
	public TreeSynchronizationExecutor(IcConnectorFacade connectorFacade, SysSystemService systemService,
			SysSystemAttributeMappingService attributeHandlingService,
			SysSyncConfigService synchronizationConfigService, SysSyncLogService synchronizationLogService,
			SysSyncActionLogService syncActionLogService, AccAccountService accountService,
			SysSystemEntityService systemEntityService, ConfidentialStorage confidentialStorage,
			FormService formService, AccTreeAccountService treeAccoutnService, SysSyncItemLogService syncItemLogService,
			EntityEventManager entityEventManager, GroovyScriptService groovyScriptService,
			WorkflowProcessInstanceService workflowProcessInstanceService, EntityManager entityManager,
			IdmTreeNodeService treeNodeService, IdmTreeNodeRepository treeNodeRepository) {
		super(connectorFacade, systemService, attributeHandlingService, synchronizationConfigService,
				synchronizationLogService, syncActionLogService, accountService, systemEntityService,
				confidentialStorage, formService, syncItemLogService, entityEventManager, groovyScriptService,
				workflowProcessInstanceService, entityManager);

		Assert.notNull(treeNodeService, "Tree node service is mandatory!");
		Assert.notNull(treeAccoutnService, "Tree account service is mandatory!");
		Assert.notNull(treeNodeRepository);

		this.treeNodeService = treeNodeService;
		this.treeAccoutnService = treeAccoutnService;
		this.treeNodeRepository = treeNodeRepository;
	}

	@Override
	public SysSyncConfig process(UUID synchronizationConfigId) {
		// Validate and create basic context
		SynchronizationContext context = this.validate(synchronizationConfigId);
				
		SysSyncConfig config = context.getConfig();
		SystemEntityType entityType = context.getEntityType();
		SysSystem system = context.getSystem();
		IcConnectorConfiguration connectorConfig = context.getConnectorConfig();
		List<SysSystemAttributeMapping> mappedAttributes = context.getMappedAttributes();		
		IcObjectClass objectClass = new IcObjectClassImpl(context.getConfig().getSystemMapping().getObjectClass().getObjectClassName());
		
		// Load last token
		Object lastToken = config.isReconciliation() ? null : config.getToken();

		// Create basic synchronization log
		SysSyncLog log = new SysSyncLog();
		log.setSynchronizationConfig(config);
		log.setStarted(LocalDateTime.now());
		log.setRunning(true);
		log.setToken(lastToken != null ? lastToken.toString() : null);
		log.addToLog(MessageFormat.format("Synchronization was started in {0}.", log.getStarted()));

		// List of all accounts with full IC object (used in tree sync)
		Map<String, IcConnectorObject> accountsMap = new HashMap<>();

		longRunningTaskExecutor.setCounter(0L);

		try {
			synchronizationLogService.save(log);
			List<SysSyncActionLog> actionsLog = new ArrayList<>();
			// Add logs to context
			context
			.addLog(log)
			.addActionLogs(actionsLog);
			
			boolean export = false;

			if (export) {
				// Start exporting entities to resource
				log.addToLog("Exporting entities to resource started...");
				this.startExport(entityType, config, mappedAttributes, log, actionsLog);
			} else {

				AttributeMapping tokenAttribute = config.getTokenAttribute();
				if (tokenAttribute == null && !config.isReconciliation()) {
					throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_TOKEN_ATTRIBUTE_NOT_FOUND);
				}

				TreeResultsHandler resultHandler = new TreeResultsHandler(accountsMap);

				IcFilter filter = null; // We have to search all data for tree
				log.addToLog(MessageFormat.format("Start search with filter {0}.", filter != null ? filter : "NONE"));
				synchronizationLogService.save(log);

				connectorFacade.search(system.getConnectorInstance(), connectorConfig, objectClass, filter,
						resultHandler);
				// Execute sync for this tree and searched accounts
				processTreeSync(context, accountsMap);
			}
			//
			log.addToLog(MessageFormat.format("Synchronization was correctly ended in {0}.", LocalDateTime.now()));
			synchronizationConfigService.save(config);
		} catch (Exception e) {
			String message = "Error during synchronization";
			log.addToLog(message);
			log.setContainsError(true);
			log.addToLog(Throwables.getStackTraceAsString(e));
			LOG.error(message, e);
		} finally {
			log.setRunning(false);
			log.setEnded(LocalDateTime.now());
			synchronizationLogService.save(log);
			//
			longRunningTaskExecutor.setCount(longRunningTaskExecutor.getCounter());
			longRunningTaskExecutor.updateState();
		}
		return config;
	}


	/**
	 * Call provisioning for given account
	 * 
	 * @param account
	 * @param entityType
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	@Override
	protected void doUpdateAccount(AccAccount account, SystemEntityType entityType, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {
		UUID entityId = getEntityByAccount(account.getId());
		IdmTreeNode treeNode = null;
		if (entityId != null) {
			treeNode = treeNodeService.get(entityId);
		}
		if (treeNode == null) {
			addToItemLog(logItem, "Tree account relation (with ownership = true) was not found!");
			initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return;
		}
		// Call provisioning for this entity
		doUpdateAccountByEntity(treeNode, entityType, logItem);
	}

	/**
	 * Call provisioning for given account
	 * 
	 * @param entity
	 * @param entityType
	 * @param logItem
	 */
	@Override
	protected void doUpdateAccountByEntity(AbstractEntity entity, SystemEntityType entityType, SysSyncItemLog logItem) {
		IdmTreeNode treeNode = (IdmTreeNode) entity;
		addToItemLog(logItem,
				MessageFormat.format(
						"Call provisioning (process TreeNodeEventType.SAVE) for treeNode ({0}) with name ({1}).",
						treeNode.getId(), treeNode.getName()));
		entityEventManager.process(new TreeNodeEvent(TreeNodeEventType.UPDATE, treeNode)).getContent();
	}

	/**
	 * Create and persist new entity by data from IC attributes
	 * 
	 * @param entityType
	 * @param mappedAttributes
	 * @param logItem
	 * @param uid
	 * @param icAttributes
	 * @param account
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void doCreateEntity(SystemEntityType entityType, List<SysSystemAttributeMapping> mappedAttributes,
			SysSyncItemLog logItem, String uid, List<IcAttribute> icAttributes, AccAccount account) {
		// We will create new TreeNode
		addToItemLog(logItem, "Missing entity action is CREATE_ENTITY, we will do create new entity.");
		IdmTreeNode treeNode = new IdmTreeNode();
		// Fill entity by mapped attribute
		treeNode = (IdmTreeNode) fillEntity(mappedAttributes, uid, icAttributes, treeNode, true);
		treeNode.setTreeType(this.getSystemMapping(mappedAttributes).getTreeType());
		// Create new Entity
		treeNodeService.save(treeNode);
		// Update extended attribute (entity must be persisted first)
		updateExtendedAttributes(mappedAttributes, uid, icAttributes, treeNode, true);
		// Update confidential attribute (entity must be persisted first)
		updateConfidentialAttributes(mappedAttributes, uid, icAttributes, treeNode, true);

		// Create new Entity account relation
		EntityAccountDto entityAccount = this.createEntityAccountDto();
		entityAccount.setAccount(account.getId());
		entityAccount.setEntity(treeNode.getId());
		entityAccount.setOwnership(true);
		this.getEntityAccountService().save(entityAccount);

		// Entity Created
		addToItemLog(logItem, MessageFormat.format("Tree node with id {0} was created", treeNode.getId()));
		if (logItem != null) {
			logItem.setDisplayName(treeNode.getName());
		}
	}

	/**
	 * Fill data from IC attributes to entity (EAV and confidential storage too)
	 * 
	 * @param account
	 * @param entityType
	 * @param uid
	 * @param icAttributes
	 * @param mappedAttributes
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	@Override
	protected void doUpdateEntity(AccAccount account, SystemEntityType entityType, String uid,
			List<IcAttribute> icAttributes, List<SysSystemAttributeMapping> mappedAttributes, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {
		UUID entityId = getEntityByAccount(account.getId());
		IdmTreeNode treeNode = null;
		if (entityId != null) {
			treeNode = treeNodeService.get(entityId);
		}
		if (treeNode != null) {
			// Update entity
			treeNode = (IdmTreeNode) fillEntity(mappedAttributes, uid, icAttributes, treeNode, false);
			treeNodeService.save(treeNode);
			// Update extended attribute (entity must be persisted first)
			updateExtendedAttributes(mappedAttributes, uid, icAttributes, treeNode, false);
			// Update confidential attribute (entity must be persisted first)
			updateConfidentialAttributes(mappedAttributes, uid, icAttributes, treeNode, false);

			// TreeNode Updated
			addToItemLog(logItem, MessageFormat.format("TreeNode with id {0} was updated", treeNode.getId()));
			if (logItem != null) {
				logItem.setDisplayName(treeNode.getName());
			}

			return;
		} else {
			addToItemLog(logItem, "Tree - account relation (with ownership = true) was not found!");
			initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return;
		}
	}

	/**
	 * Operation remove EntityAccount relations and linked roles
	 * 
	 * @param account
	 * @param removeIdentityRole
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	@Override
	protected void doUnlink(AccAccount account, boolean removeIdentityRole, SysSyncLog log, SysSyncItemLog logItem,
			List<SysSyncActionLog> actionLogs) {

		TreeAccountFilter treeAccountFilter = new TreeAccountFilter();
		treeAccountFilter.setAccountId(account.getId());
		List<AccTreeAccountDto> treeAccounts = treeAccoutnService.find(treeAccountFilter, null).getContent();
		if (treeAccounts.isEmpty()) {
			addToItemLog(logItem, "Tree account relation was not found!");
			initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return;
		}
		addToItemLog(logItem, MessageFormat.format("Tree-account relations to delete {0}", treeAccounts));

		treeAccounts.stream().forEach(treeAccount -> {
			// We will remove tree account, but without delete connected
			// account
			treeAccoutnService.delete(treeAccount, false);
			addToItemLog(logItem,
					MessageFormat.format(
							"Tree-account relation deleted (without call delete provisioning) (treeNode: {0}, id: {1})",
							treeAccount.getTreeNode(), treeAccount.getId()));

		});
		return;
	}

	/**
	 * Delete entity linked with given account
	 * 
	 * @param account
	 * @param entityType
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	@Override
	protected void doDeleteEntity(AccAccount account, SystemEntityType entityType, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {
		UUID entityId = getEntityByAccount(account.getId());
		IdmTreeNode treeNode = null;
		if (entityId != null) {
			treeNode = treeNodeService.get(entityId);
		}
		if (treeNode == null) {
			addToItemLog(logItem, "Tree account relation (with ownership = true) was not found!");
			initSyncActionLog(SynchronizationActionType.DELETE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return;
		}
		
		logItem.setDisplayName(treeNode.getName());
		// Delete entity (recursively)
		deleteChildrenRecursively(treeNode, logItem);
	}
	
	/**
	 * Start export entities to target resource
	 * @param entityType
	 * @param config
	 * @param mappedAttributes
	 * @param log
	 * @param actionsLog
	 */
	@Override
	protected void startExport(SystemEntityType entityType, SysSyncConfig config,
			List<SysSystemAttributeMapping> mappedAttributes, SysSyncLog log, List<SysSyncActionLog> actionsLog) {

		SysSystem system = config.getSystemMapping().getSystem();
		SysSystemAttributeMapping uidAttribute = systemAttributeMappingService.getUidAttribute(mappedAttributes,
				system);

		List<IdmTreeNode> roots = treeNodeService.findRoots(config.getSystemMapping().getTreeType().getId(), null).getContent();
		roots.stream().forEach(root -> {
			SynchronizationContext itemBuilder = new SynchronizationContext();
			itemBuilder.addConfig(config) //
					.addSystem(system) //
					.addEntityType(entityType) //
					.addLog(log) //
					.addActionLogs(actionsLog);
			// Start export for this entity
			exportChildrenRecursively(root, itemBuilder, uidAttribute);
		});
	}

	@Override
	protected Object getValueByMappedAttribute(AttributeMapping attribute, List<IcAttribute> icAttributes) {
		
		Object transformedValue = super.getValueByMappedAttribute(attribute, icAttributes);
		
		if (PARENT_FIELD.equals(attribute.getIdmPropertyName()) && transformedValue != null) {
			// Find account by UID from parent field
			AccountFilter accountFilter = new AccountFilter();
			accountFilter.setUid(transformedValue.toString());
			accountFilter.setSystemId(((SysSystemAttributeMapping)attribute).getSystemMapping().getSystem().getId());
			transformedValue = null;
			List<AccAccount> parentAccounts = accountService.find(accountFilter, null).getContent();
			if (!parentAccounts.isEmpty()) {
				// Find relation between tree and account
				TreeAccountFilter treeAccountFilter = new TreeAccountFilter();
				treeAccountFilter.setAccountId(parentAccounts.get(0).getId());
				List<AccTreeAccountDto> treeAccounts = treeAccoutnService.find(treeAccountFilter, null).getContent();
				if(!treeAccounts.isEmpty()){
					// Find parent tree node by ID
					// TODO: resolve more treeAccounts situations
					transformedValue = treeNodeService.get(treeAccounts.get(0).getTreeNode());
				}
			}
		}
		return transformedValue;
	}

	@Override
	public boolean supports(SystemEntityType delimiter) {
		return SystemEntityType.TREE == delimiter;
	}

	@Override
	protected AbstractEntity findEntityById(UUID entityId, SystemEntityType entityType) {
		return treeNodeService.get(entityId);
	}

	@Override
	protected EntityAccountFilter createEntityAccountFilter() {
		return new TreeAccountFilter();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected ReadWriteDtoService getEntityAccountService() {
		return treeAccoutnService;
	}

	@Override
	protected EntityAccountDto createEntityAccountDto() {
		return new AccTreeAccountDto();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected ReadWriteDtoService getEntityService() {
		return null; // We don't have DTO service for IdmTreeNode now
	}

	public class TreeResultsHandler implements IcResultsHandler {

		// List of all accounts
		private Map<String, IcConnectorObject> accountsMap = new HashMap<>();

		public TreeResultsHandler(Map<String, IcConnectorObject> accountsMap) {
			this.accountsMap = accountsMap;
		}

		@Override
		public boolean handle(IcConnectorObject connectorObject) {
			Assert.notNull(connectorObject);
			Assert.notNull(connectorObject.getUidValue());
			String uid = connectorObject.getUidValue();
			accountsMap.put(uid, connectorObject);
			return true;

		}
	}

	@Override
	protected List<IdmTreeNode> findAllEntity() {
		return treeNodeService.find(null).getContent();
	};
	
	/**
	 * Execute sync for tree and given accounts.
	 * @param context
	 * @param accountsMap
	 */
	private void processTreeSync(SynchronizationContext context,
			Map<String, IcConnectorObject> accountsMap) {
		
		SysSyncConfig config = context.getConfig();
		SystemEntityType entityType = context.getEntityType();
		SysSystem system = context.getSystem();
		List<SysSystemAttributeMapping> mappedAttributes = context.getMappedAttributes();
		SysSyncLog log = context.getLog();
		List<SysSyncActionLog> actionsLog = context.getActionLogs();
		AttributeMapping tokenAttribute = context.getTokenAttribute();
		List<String> accountsUseInTreeList = new ArrayList<>();
		
		// Find UID/PARENT/CODE attribute
		SysSystemAttributeMapping uidAttribute = systemAttributeMappingService.getUidAttribute(mappedAttributes, system);
		SysSystemAttributeMapping parentAttribute = getAttributeByIdmProperty(PARENT_FIELD, mappedAttributes);
		SysSystemAttributeMapping codeAttribute = getAttributeByIdmProperty(CODE_FIELD, mappedAttributes);
		if (parentAttribute == null) {
//			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_ATTRIBUTE_NOT_FOUND,
//					ImmutableMap.of("name", PARENT_FIELD));
			LOG.warn("Parent attribute is not specified! Organization tree will not be recomputed.");
		}
		if (codeAttribute == null) {
//			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_ATTRIBUTE_NOT_FOUND,
//					ImmutableMap.of("name", CODE_FIELD));
			LOG.warn("Code attribute is not specified!");
		}
		// Find all roots
		Collection<String> roots = findRoots(parentAttribute, accountsMap, config);

		if (roots.isEmpty()) {
			log.addToLog("No roots to synchronization found!");
		} else {
			log.addToLog(MessageFormat.format("We found [{0}] roots: [{1}]", roots.size(), roots));
		}

		if (parentAttribute == null) {
			// just alias all accounts as roots and process
			roots.addAll(accountsMap.keySet());
		}
		for (String root : roots) {
			accountsUseInTreeList.add(root);
			IcConnectorObject account = accountsMap.get(root);

			SynchronizationContext itemContext = SynchronizationContext.cloneContext(context);
			itemContext
					.addUid(root)
					.addAccount(null)
					.addIcObject(account)
					.addTokenAttribute(tokenAttribute);

			boolean result = handleIcObject(itemContext);
			if (!result) {
				continue;
			}

			if (parentAttribute != null) {
				Object uidValueParent = this.getValueByMappedAttribute(uidAttribute, account.getAttributes());
				processChildren(parentAttribute, uidValueParent, uidAttribute, accountsMap, accountsUseInTreeList,
						itemContext, roots);
			}
		}

		if (config.isReconciliation()) {
			// We do reconciliation (find missing account)
			startReconciliation(entityType, accountsUseInTreeList, config, system, log, actionsLog);
		}
	}
	
	private void exportChildrenRecursively(IdmTreeNode treeNode, SynchronizationContext itemBuilder, SysSystemAttributeMapping uidAttribute){
		SysSyncItemLog logItem = itemBuilder.getLogItem();
		
		List<IdmTreeNode> children = treeNodeService.findChildrenByParent(treeNode.getId(), null).getContent();
		if (children.isEmpty()) {
			this.exportEntity(itemBuilder, uidAttribute, treeNode);
		} else {
			addToItemLog(logItem,
					MessageFormat.format("Tree node [{0}] has children [count={1}].",
							treeNode.getName(), children.size()));
			this.exportEntity(itemBuilder, uidAttribute, treeNode);
			children.forEach(child -> {
				exportChildrenRecursively(child, itemBuilder, uidAttribute);
			});
		}
	}

	private void deleteChildrenRecursively(IdmTreeNode treeNode, SysSyncItemLog logItem) {
		List<IdmTreeNode> children = treeNodeService.findChildrenByParent(treeNode.getId(), null).getContent();
		if (children.isEmpty()) {
			treeNodeService.delete(treeNode);
			addToItemLog(logItem, MessageFormat.format("Tree node [{0}] was deleted.", treeNode.getName()));
		} else {
			addToItemLog(logItem,
					MessageFormat.format("Tree node [{0}] has children [count={1}]. We have to delete them first.",
							treeNode.getName(), children.size()));
			children.forEach(child -> {
				deleteChildrenRecursively(child, logItem);
			});
			treeNodeService.delete(treeNode);
			addToItemLog(logItem, MessageFormat.format("Tree node [{0}] was deleted.", treeNode.getName()));
		}
	}
	

	/**
	 * Find all roots for this tree (uses groovy script for root definition)
	 * @param parentAttribute
	 * @param accountsMap
	 * @param config
	 * @return
	 */
	private Collection<String> findRoots(SysSystemAttributeMapping parentAttribute,
										 Map<String, IcConnectorObject> accountsMap, SysSyncConfig config) {
		Set<String> roots = Sets.newHashSet();
		if (parentAttribute == null) {
			return roots;
		}
		accountsMap.forEach((uid, account) -> {
			if (StringUtils.hasLength(config.getRootsFilterScript())) {
				Map<String, Object> variables = new HashMap<>();
				variables.put("account", account);

				List<Class<?>> allowTypes = new ArrayList<>();
				allowTypes.add(IcAttributeImpl.class);
				allowTypes.add(IcAttribute.class);
				allowTypes.add(IcLoginAttributeImpl.class);
				Object isRoot = groovyScriptService.evaluate(config.getRootsFilterScript(), variables,
						allowTypes);
				if (isRoot != null && !(isRoot instanceof Boolean)) {
					throw new ProvisioningException(
							AccResultCode.SYNCHRONIZATION_TREE_ROOT_FILTER_VALUE_WRONG_TYPE,
							ImmutableMap.of("type", isRoot.getClass().getName()));
				}
				if ((Boolean) isRoot) {
					roots.add(uid);
				}
			} else {
				// Default search root strategy (if is parent null, then is node root)
				Object parentValue = super.getValueByMappedAttribute(parentAttribute, account.getAttributes());
				if (parentValue == null) {
					roots.add(uid);
				}
			}
		});
		return roots;
	}

	/**
	 * Process recursively tree children
	 *
	 * @param parentAttribute
	 * @param uidValueParent
	 * @param uidAttribute
	 * @param accountsMap
	 * @param accountsUseInTreeList
	 * @param context
	 */
	private void processChildren(SysSystemAttributeMapping parentAttribute, Object uidValueParent,
			SysSystemAttributeMapping uidAttribute, Map<String, IcConnectorObject> accountsMap,
			List<String> accountsUseInTreeList, SynchronizationContext context, Collection<String> roots) {

		accountsMap.forEach((uid, account) -> {
			if (roots.contains(uid)) {
				return;
			}
			Object parentValue = super.getValueByMappedAttribute(parentAttribute, account.getAttributes());
			if (parentValue != null && parentValue.equals(uidValueParent)) {
				// Account is use in tree
				if(!accountsUseInTreeList.contains(uid)){
					accountsUseInTreeList.add(uid);
				}
				// Do provisioning for this account
				SynchronizationContext itemContext = SynchronizationContext.cloneContext(context);
				itemContext
				.addUid(uid)
				.addIcObject(account)
				.addAccount(null);

				boolean resultChild = handleIcObject(itemContext);
				if (!resultChild) {
					return;
				}
				Object uidValueParentChilde = super.getValueByMappedAttribute(uidAttribute, account.getAttributes());
				processChildren(parentAttribute, uidValueParentChilde, uidAttribute, accountsMap, accountsUseInTreeList,
						itemContext, roots);

			}
		});
	}
	
	/**
	 * Find tree type ID by attribute mapping
	 * @param attribute
	 * @return
	 */
	private UUID findTreeTypeId(AttributeMapping attribute){
		return ((SysSystemAttributeMapping)attribute).getSystemMapping().getTreeType().getId();
	}

	@Override
	protected Class<? extends FormableEntity> getEntityClass() {
		return IdmTreeNode.class;
	}

	@Override
	protected BaseEntityRepository getRepository() {
		return this.treeNodeRepository;
	}

	@Override
	protected CorrelationFilter getEntityFilter() {
		return new TreeNodeFilter();
	}

	@Override
	protected AbstractEntity findEntityByAttribute(String idmAttributeName, String value) {
		CorrelationFilter filter = getEntityFilter();
		filter.setProperty(idmAttributeName);
		filter.setValue(value);
		
		List<IdmTreeNode> entities = treeNodeService.find((TreeNodeFilter) filter, null).getContent();
		
		if (CollectionUtils.isEmpty(entities)) {
			return null;
		}
		if (entities.size() > 1) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_CORRELATION_TO_MANY_RESULTS,
					ImmutableMap.of("correlationAttribute", idmAttributeName, "value", value));
		}
		if (entities.size() == 1) {
			return entities.get(0);
		}
		return null;
	}
}
