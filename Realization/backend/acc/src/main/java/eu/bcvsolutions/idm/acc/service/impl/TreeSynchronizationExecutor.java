package eu.bcvsolutions.idm.acc.service.impl;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationItemBuilder;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccTreeAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.EntityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.TreeAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccTreeAccount;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncLog;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
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
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.domain.EntityUtilities;
import eu.bcvsolutions.idm.core.model.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.model.dto.filter.TreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.event.TreeNodeEvent;
import eu.bcvsolutions.idm.core.model.event.TreeNodeEvent.TreeNodeEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcSyncToken;
import eu.bcvsolutions.idm.ic.filter.api.IcFilter;
import eu.bcvsolutions.idm.ic.filter.api.IcResultsHandler;
import eu.bcvsolutions.idm.ic.impl.IcAttributeImpl;
import eu.bcvsolutions.idm.ic.impl.IcLoginAttributeImpl;
import eu.bcvsolutions.idm.ic.impl.IcObjectClassImpl;
import eu.bcvsolutions.idm.ic.impl.IcSyncTokenImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

@Component
public class TreeSynchronizationExecutor extends AbstractSynchronizationExecutor<IdmTreeNodeDto>
		implements SynchronizationEntityExecutor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TreeSynchronizationExecutor.class);
	public final static String PARENT_FIELD = "parent";
	public final static String CODE_FIELD = "code";

	private final IdmTreeNodeService treeNodeService;
	private final AccTreeAccountService treeAccoutnService;

	@Autowired
	public TreeSynchronizationExecutor(IcConnectorFacade connectorFacade, SysSystemService systemService,
			SysSystemAttributeMappingService attributeHandlingService,
			SysSyncConfigService synchronizationConfigService, SysSyncLogService synchronizationLogService,
			SysSyncActionLogService syncActionLogService, AccAccountService accountService,
			SysSystemEntityService systemEntityService, ConfidentialStorage confidentialStorage,
			FormService formService, AccTreeAccountService treeAccoutnService, SysSyncItemLogService syncItemLogService,
			EntityEventManager entityEventManager, GroovyScriptService groovyScriptService,
			WorkflowProcessInstanceService workflowProcessInstanceService, EntityManager entityManager,
			IdmTreeNodeService treeNodeService) {
		super(connectorFacade, systemService, attributeHandlingService, synchronizationConfigService,
				synchronizationLogService, syncActionLogService, accountService, systemEntityService,
				confidentialStorage, formService, syncItemLogService, entityEventManager, groovyScriptService,
				workflowProcessInstanceService, entityManager);

		Assert.notNull(treeNodeService, "Tree node service is mandatory!");
		Assert.notNull(treeAccoutnService, "Tree account service is mandatory!");

		this.treeNodeService = treeNodeService;
		this.treeAccoutnService = treeAccoutnService;

	}

	@Override
	public SysSyncConfig process(UUID synchronizationConfigId) {
		SysSyncConfig config = synchronizationConfigService.get(synchronizationConfigId);
		//
		if (config == null) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_NOT_FOUND,
					ImmutableMap.of("id", synchronizationConfigId));
		}
		//
		// Synchronization must be enabled
		if (!config.isEnabled()) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_IS_NOT_ENABLED,
					ImmutableMap.of("name", config.getName()));
		}

		// Synchronization can not be running twice
		SynchronizationLogFilter logFilter = new SynchronizationLogFilter();
		logFilter.setSynchronizationConfigId(config.getId());
		logFilter.setRunning(Boolean.TRUE);
		if (!synchronizationLogService.find(logFilter, null).getContent().isEmpty()) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_IS_RUNNING,
					ImmutableMap.of("name", config.getName()));
		}

		SysSystemMapping mapping = config.getSystemMapping();
		Assert.notNull(mapping);
		SysSystem system = mapping.getSystem();
		Assert.notNull(system);

		// System must be enabled
		if (system.isDisabled()) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_SYSTEM_IS_NOT_ENABLED,
					ImmutableMap.of("name", config.getName(), "system", system.getName()));
		}

		SystemEntityType entityType = mapping.getEntityType();
		SystemAttributeMappingFilter attributeHandlingFilter = new SystemAttributeMappingFilter();
		attributeHandlingFilter.setSystemMappingId(mapping.getId());
		List<SysSystemAttributeMapping> mappedAttributes = systemAttributeMappingService.find(attributeHandlingFilter, null)
				.getContent();

		// Find connector identification persisted in system
		IcConnectorKey connectorKey = system.getConnectorKey();
		if (connectorKey == null) {
			throw new ProvisioningException(AccResultCode.CONNECTOR_KEY_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Find connector configuration persisted in system
		IcConnectorConfiguration connectorConfig = systemService.getConnectorConfiguration(system);
		if (connectorConfig == null) {
			throw new ProvisioningException(AccResultCode.CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		IcObjectClass objectClass = new IcObjectClassImpl(mapping.getObjectClass().getObjectClassName());

		Object lastToken = config.isReconciliation() ? null : config.getToken();
		//IcSyncToken lastIcToken = lastToken != null ? new IcSyncTokenImpl(lastToken) : null;

		// Create basic synchronization log
		SysSyncLog log = new SysSyncLog();
		log.setSynchronizationConfig(config);
		log.setStarted(LocalDateTime.now());
		log.setRunning(true);
		log.setToken(lastToken != null ? lastToken.toString() : null);

		log.addToLog(MessageFormat.format("Synchronization was started in {0}.", log.getStarted()));

		// List of all accounts with full IC object (used in tree sync)
		Map<String, IcConnectorObject> accountsMap = new HashMap<>();
		List<String> accountsUseInTreeList = new ArrayList<>();

		longRunningTaskExecutor.setCounter(0L);

		try {
			synchronizationLogService.save(log);
			List<SysSyncActionLog> actionsLog = new ArrayList<>();
			
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

				SysSystemAttributeMapping uidAttribute = systemAttributeMappingService.getUidAttribute(mappedAttributes,
						system);
				SysSystemAttributeMapping parentAttribute = getAttributeByIdmProperty(PARENT_FIELD, mappedAttributes);
				if (parentAttribute == null) {
					throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_ATTRIBUTE_NOT_FOUND,
							ImmutableMap.of("name", PARENT_FIELD));
				}
				SysSystemAttributeMapping codeAttribute = getAttributeByIdmProperty(CODE_FIELD, mappedAttributes);
				if (codeAttribute == null) {
					throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_ATTRIBUTE_NOT_FOUND,
							ImmutableMap.of("name", CODE_FIELD));
				}

				List<String> roots = new ArrayList<>();
				accountsMap.forEach((uid, account) -> {
					Object parentValue = this.getValueByMappedAttribute(parentAttribute, account.getAttributes());
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
						if (parentValue == null) {
							roots.add(uid);
						}
					}
				});

				if (roots.isEmpty()) {
					log.addToLog("No roots to synchronization found!");
				} else {
					log.addToLog(MessageFormat.format("We found [{0}] roots: [{1}]", roots.size(), roots));
				}

				roots.forEach(root -> {
					accountsUseInTreeList.add(root);
					IcConnectorObject parentIcObject = accountsMap.get(root);
					boolean result = handleIcObject(root, parentIcObject, tokenAttribute, config, system, entityType,
							log, mappedAttributes, actionsLog);
					if (!result) {
						return;
					}
					Object uidValueParent = this.getValueByMappedAttribute(uidAttribute,
							parentIcObject.getAttributes());
					SynchronizationItemBuilder builder = new SynchronizationItemBuilder();

					builder.addConfig(config).addSystem(system).addEntityType(entityType)
							.addMappedAttributes(mappedAttributes).addLog(log).addActionLogs(actionsLog);

					processChildren(parentAttribute, uidValueParent, uidAttribute, tokenAttribute, accountsMap,
							accountsUseInTreeList, builder);
				});

				if (config.isReconciliation()) {
					// We do reconciliation (find missing account)
					startReconciliation(entityType, accountsUseInTreeList, config, system, log, actionsLog);
				}
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
	 * Process recursively tree children
	 * 
	 * @param parentAttribute
	 * @param uidValueParent
	 * @param uidAttribute
	 * @param tokenAttribute
	 * @param accountsMap
	 * @param wrapper
	 */
	private void processChildren(SysSystemAttributeMapping parentAttribute, Object uidValueParent,
			SysSystemAttributeMapping uidAttribute, AttributeMapping tokenAttribute,
			Map<String, IcConnectorObject> accountsMap, List<String> accountsUseInTreeList,
			SynchronizationItemBuilder wrapper) {

		accountsMap.forEach((uid, account) -> {
			Object parentValue = this.getValueByMappedAttribute(parentAttribute, account.getAttributes());
			if (parentValue != null && parentValue.equals(uidValueParent)) {
				// Account is use in tree
				accountsUseInTreeList.add(uid);
				// Do provisioning for this account
				boolean resultChild = handleIcObject(uid, account, tokenAttribute, wrapper.getConfig(),
						wrapper.getSystem(), wrapper.getEntityType(), wrapper.getLog(), wrapper.getMappedAttributes(),
						wrapper.getActionLogs());
				if (!resultChild) {
					return;
				}
				Object uidValueParentChilde = this.getValueByMappedAttribute(uidAttribute, account.getAttributes());
				processChildren(parentAttribute, uidValueParentChilde, uidAttribute, tokenAttribute, accountsMap,
						accountsUseInTreeList, wrapper);

			}
		});
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
			SynchronizationItemBuilder itemBuilder = new SynchronizationItemBuilder();
			itemBuilder.addConfig(config) //
					.addSystem(system) //
					.addEntityType(entityType) //
					.addLog(log) //
					.addActionLogs(actionsLog);
			// Start export for this entity
			exportChildrenRecursively(root, itemBuilder, uidAttribute);
		});
	}
	
	private void exportChildrenRecursively(IdmTreeNode treeNode, SynchronizationItemBuilder itemBuilder, SysSystemAttributeMapping uidAttribute){
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
	 * Find entity by correlation attribute
	 * 
	 * @param attribute
	 * @param icAttributes
	 * @return
	 */
	protected AbstractEntity findEntityByCorrelationAttribute(AttributeMapping attribute,
			List<IcAttribute> icAttributes) {
		Assert.notNull(attribute);
		Assert.notNull(icAttributes);

		Object value = getValueByMappedAttribute(attribute, icAttributes);
		if (value == null) {
			return null;
		}
		if (attribute.isEntityAttribute()) {
			TreeNodeFilter correlationFilter = new TreeNodeFilter();
			correlationFilter.setProperty(attribute.getIdmPropertyName());
			correlationFilter.setValue(value.toString());

			List<IdmTreeNode> treeNodes = treeNodeService.find(correlationFilter, null).getContent();
			if (CollectionUtils.isEmpty(treeNodes)) {
				return null;
			}
			if (treeNodes.size() > 1) {
				throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_CORRELATION_TO_MANY_RESULTS,
						ImmutableMap.of("correlationAttribute", attribute.getName(), "value", value));
			}
			if (treeNodes.size() == 1) {
				return treeNodes.get(0);
			}

		} else if (attribute.isExtendedAttribute()) {
			// TODO: not supported now

			return null;
		}
		return null;
	}

	/**
	 * Fill entity with attributes from IC module (by mapped attributes).
	 * 
	 * @param mappedAttributes
	 * @param uid
	 * @param icAttributes
	 * @param entity
	 * @param create (is create or update entity situation)
	 * @return
	 */
	@Override
	protected AbstractEntity fillEntity(List<SysSystemAttributeMapping> mappedAttributes, String uid,
			List<IcAttribute> icAttributes, AbstractEntity entity, boolean create) {
		mappedAttributes.stream().filter(attribute -> {
			// Skip disabled attributes
						// Skip extended attributes (we need update/ create entity first)
						// Skip confidential attributes (we need update/ create entity
						// first)
						boolean fastResult =  !attribute.isDisabledAttribute() && attribute.isEntityAttribute()
								&& !attribute.isConfidentialAttribute();
						if(!fastResult){
							return false;
						}
						// Can be value set by attribute strategy?
						return this.canSetValue(uid, attribute, entity, create);

		}).forEach(attribute -> {
			String attributeProperty = attribute.getIdmPropertyName();
			Object transformedValue = getValueByMappedAttribute(attribute, icAttributes);
			if (attributeProperty.equals(PARENT_FIELD) && transformedValue != null) {
				// Find account by UID from parent field
				AccountFilter accountFilter = new AccountFilter();
				accountFilter.setUidId(transformedValue.toString());
				accountFilter.setSystemId(attribute.getSystemMapping().getSystem().getId());
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

			// Set transformed value from target system to entity
			try {
				EntityUtilities.setEntityValue(entity, attributeProperty, transformedValue);
			} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | ProvisioningException e) {
				throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_IDM_FIELD_NOT_SET,
						ImmutableMap.of("property", attributeProperty, "uid", uid), e);
			}

		});
		return entity;
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
}
