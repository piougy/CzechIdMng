package eu.bcvsolutions.idm.acc.service.impl;

import java.beans.IntrospectionException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.annotations.Beta;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationContext;
import eu.bcvsolutions.idm.acc.domain.SynchronizationEventType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationSituationType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.EntityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SyncActionLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncLog;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationEntityExecutor;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.CorrelationFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractLongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcSyncDelta;
import eu.bcvsolutions.idm.ic.api.IcSyncResultsHandler;
import eu.bcvsolutions.idm.ic.api.IcSyncToken;
import eu.bcvsolutions.idm.ic.domain.IcFilterOperationType;
import eu.bcvsolutions.idm.ic.filter.api.IcFilter;
import eu.bcvsolutions.idm.ic.filter.api.IcResultsHandler;
import eu.bcvsolutions.idm.ic.filter.impl.IcAndFilter;
import eu.bcvsolutions.idm.ic.filter.impl.IcFilterBuilder;
import eu.bcvsolutions.idm.ic.filter.impl.IcOrFilter;
import eu.bcvsolutions.idm.ic.impl.IcAttributeImpl;
import eu.bcvsolutions.idm.ic.impl.IcObjectClassImpl;
import eu.bcvsolutions.idm.ic.impl.IcSyncDeltaTypeEnum;
import eu.bcvsolutions.idm.ic.impl.IcSyncTokenImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Abstract executor for do synchronization and reconciliation
 * 
 * @author svandav
 *
 */

public abstract class AbstractSynchronizationExecutor<ENTITY extends AbstractDto> implements SynchronizationEntityExecutor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(AbstractSynchronizationExecutor.class);
	private final WorkflowProcessInstanceService workflowProcessInstanceService;
	protected final IcConnectorFacade connectorFacade;
	protected final SysSystemService systemService;
	protected final SysSystemAttributeMappingService systemAttributeMappingService;
	protected final SysSyncConfigService synchronizationConfigService;
	protected final SysSyncLogService synchronizationLogService;
	protected final SysSyncItemLogService syncItemLogService;
	protected final SysSyncActionLogService syncActionLogService;
	protected final SysSystemEntityService systemEntityService;
	protected final AccAccountService accountService;
	protected final GroovyScriptService groovyScriptService;
	private final ConfidentialStorage confidentialStorage;
	private final FormService formService;
	protected final EntityEventManager entityEventManager;
	private final EntityManager entityManager;
	protected AbstractLongRunningTaskExecutor<SysSyncConfig> longRunningTaskExecutor;
	//

	@Autowired
	public AbstractSynchronizationExecutor(IcConnectorFacade connectorFacade, SysSystemService systemService,
			SysSystemAttributeMappingService attributeHandlingService,
			SysSyncConfigService synchronizationConfigService, SysSyncLogService synchronizationLogService,
			SysSyncActionLogService syncActionLogService, AccAccountService accountService,
			SysSystemEntityService systemEntityService, ConfidentialStorage confidentialStorage,
			FormService formService, SysSyncItemLogService syncItemLogService, EntityEventManager entityEventManager,
			GroovyScriptService groovyScriptService, WorkflowProcessInstanceService workflowProcessInstanceService,
			EntityManager entityManager) {
		Assert.notNull(connectorFacade);
		Assert.notNull(systemService);
		Assert.notNull(attributeHandlingService);
		Assert.notNull(synchronizationConfigService);
		Assert.notNull(synchronizationLogService);
		Assert.notNull(syncActionLogService);
		Assert.notNull(accountService);
		Assert.notNull(systemEntityService);
		Assert.notNull(confidentialStorage);
		Assert.notNull(formService);
		Assert.notNull(syncItemLogService);
		Assert.notNull(entityEventManager);
		Assert.notNull(groovyScriptService);
		Assert.notNull(workflowProcessInstanceService);
		Assert.notNull(entityManager);
		//
		this.connectorFacade = connectorFacade;
		this.systemService = systemService;
		this.systemAttributeMappingService = attributeHandlingService;
		this.synchronizationConfigService = synchronizationConfigService;
		this.synchronizationLogService = synchronizationLogService;
		this.accountService = accountService;
		this.systemEntityService = systemEntityService;
		this.confidentialStorage = confidentialStorage;
		this.formService = formService;
		this.syncItemLogService = syncItemLogService;
		this.entityEventManager = entityEventManager;
		this.groovyScriptService = groovyScriptService;
		this.workflowProcessInstanceService = workflowProcessInstanceService;
		this.entityManager = entityManager;
		this.syncActionLogService = syncActionLogService;
	}

	@Override
	public SysSyncConfig process(UUID synchronizationConfigId) {
		
		// Validate and create basic context
		SynchronizationContext context = this.validate(synchronizationConfigId);
		
		SysSyncConfig config = context.getConfig();
		SystemEntityType entityType = context.getEntityType();
		SysSystem system = context.getSystem();
		IcConnectorConfiguration connectorConfig = context.getConnectorConfig();
		IcObjectClass objectClass = new IcObjectClassImpl(context.getConfig().getSystemMapping().getObjectClass().getObjectClassName());
		// Load last token
		Object lastToken = config.isReconciliation() ? null : config.getToken();
		IcSyncToken lastIcToken = lastToken != null ? new IcSyncTokenImpl(lastToken) : null;

		// Create basic synchronization log
		SysSyncLog log = new SysSyncLog();
		log.setSynchronizationConfig(config);
		log.setStarted(LocalDateTime.now());
		log.setRunning(true);
		log.setToken(lastToken != null ? lastToken.toString() : null);
		log.addToLog(MessageFormat.format("Synchronization was started in {0}.", log.getStarted()));

		// List of all accounts keys (used in reconciliation)
		List<String> systemAccountsList = new ArrayList<>();

		// TODO: Export is not fully implemented (FE, configuration and Groovy part missing)
		boolean export = false;
		

		longRunningTaskExecutor.setCounter(0L);

		try {
			synchronizationLogService.save(log);
			List<SysSyncActionLog> actionsLog = new ArrayList<>();
			
			// add logs to context
			context
			.addLog(log)
			.addActionLogs(actionsLog);

			if(export){
				// Start exporting entities to resource
				log.addToLog("Exporting entities to resource started...");
				this.startExport(entityType, config, context.getMappedAttributes(), log, actionsLog);
				
			}else if (config.isCustomFilter() || config.isReconciliation()) {
				// Custom filter Sync
				log.addToLog("Synchronization will use custom filter (not synchronization implemented in connector).");
				AttributeMapping tokenAttribute = config.getTokenAttribute();
				if (tokenAttribute == null && !config.isReconciliation()) {
					throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_TOKEN_ATTRIBUTE_NOT_FOUND);
				}
				context.addTokenAttribute(tokenAttribute);
				// Resolve filter for custom search
				IcFilter filter = resolveSynchronizationFilter(config);
				log.addToLog(MessageFormat.format("Start search with filter {0}.", filter != null ? filter : "NONE"));
				synchronizationLogService.save(log);

				connectorFacade.search(system.getConnectorInstance(), connectorConfig, objectClass, filter,
						new DefaultResultHandler(context, systemAccountsList));
			} else {
				// Inner Sync
				log.addToLog("Synchronization will use inner connector synchronization implementation.");
				DefalutSyncResultHandler syncResultsHandler = new DefalutSyncResultHandler(context, systemAccountsList); 
				connectorFacade.synchronization(system.getConnectorInstance(), connectorConfig, objectClass,
						lastIcToken, syncResultsHandler);
			}

			// We do reconciliation (find missing account)
			if (config.isReconciliation()) {
				startReconciliation(entityType, systemAccountsList, config, system, log, actionsLog);
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
	
	
	@Override
	public boolean doItemSynchronization(SynchronizationContext context) {
		Assert.notNull(context);

		String uid = context.getUid();
		IcConnectorObject icObject = context.getIcObject();
		IcSyncDeltaTypeEnum type = context.getType();
		SysSyncConfig config = context.getConfig();
		SysSystem system = context.getSystem();
		SystemEntityType entityType = context.getEntityType();
		AccAccount account = context.getAccount();
		SysSyncLog log = context.getLog();
		SysSyncItemLog logItem = context.getLogItem();
		List<SysSyncActionLog> actionLogs = context.getActionLogs();

		SynchronizationActionType actionType = SynchronizationActionType.IGNORE;
		try {

			// Find system entity for uid
			SysSystemEntity systemEntity = findSystemEntity(uid, system, entityType);

			// Find acc account for uid or system entity
			if (account == null) {
				account = findAccount(uid, entityType, systemEntity, system, logItem);
				if (systemEntity == null) {
					addToItemLog(logItem, "SystemEntity for this uid not exist. We will create him.");
					systemEntity = createSystemEntity(uid, entityType, system);
				}

			}
			
			context
			.addSystemEntity(systemEntity)
			.addAccount(account);

			if (IcSyncDeltaTypeEnum.CREATE == type || IcSyncDeltaTypeEnum.UPDATE == type
					|| IcSyncDeltaTypeEnum.CREATE_OR_UPDATE == type) {
				// Update or create
				Assert.notNull(icObject);
				List<IcAttribute> icAttributes = icObject.getAttributes();

				if (account == null) {
					// Account not exist in IDM
					actionType = resolveAccountNotExistSituation(context, systemEntity, icAttributes);

				} else {
					// Account exist in IdM (LINKED)
					actionType = config.getLinkedAction().getAction();
					SynchronizationSituationType situation = SynchronizationSituationType.LINKED;
					if (StringUtils.hasLength(config.getLinkedActionWfKey())) {
						SynchronizationLinkedActionType linkedAction = config.getLinkedAction();
						SynchronizationActionType action = linkedAction.getAction();

						// We will start specific workflow
						startWorkflow(config.getLinkedActionWfKey(), situation, action, null, context);

					} else {
						resolveLinkedSituation(config.getLinkedAction(), context);
					}
					addToItemLog(logItem, "Account exist in IdM (LINKED) - ended");

				}

			} else if (IcSyncDeltaTypeEnum.DELETE == type) {
				// Missing account situation, can be call from connector
				// (support delete account event) and from reconciliation

				actionType = config.getMissingAccountAction().getAction();
				SynchronizationSituationType situation = SynchronizationSituationType.MISSING_ACCOUNT;
				if (StringUtils.hasLength(config.getMissingAccountActionWfKey())) {
					ReconciliationMissingAccountActionType missingAccountActionType = config.getMissingAccountAction();
					SynchronizationActionType action = missingAccountActionType.getAction();

					// We will start specific workflow
					startWorkflow(config.getMissingAccountActionWfKey(), situation, action, null, context);

				} else {
					// Resolve missing account situation for one item
					this.resolveMissingAccountSituation(config.getMissingAccountAction(), context);
				}
			} else if (context.isExportAction()) {
				// Export situation - create account to system
				this.resolveUnlinkedSituation(SynchronizationUnlinkedActionType.LINK_AND_UPDATE_ACCOUNT, context);
			}
			// Call hard hibernate session flush and clear
			if (getHibernateSession().isOpen()) {
				getHibernateSession().flush();
				getHibernateSession().clear();
			}
			return true;
		} catch (Exception e) {
			loggingException(actionType, log, logItem, actionLogs, uid, e);
			throw e;
		}
	}

	/**
	 * Resolve "Account not exist in IDM" situation. Result can be UNLINKED or UNMATCHED situations.
	 * @param context
	 * @param systemEntity
	 * @param icAttributes
	 * @return
	 */
	protected SynchronizationActionType resolveAccountNotExistSituation(SynchronizationContext context, SysSystemEntity systemEntity, List<IcAttribute> icAttributes) {
		Assert.notNull(context);
		
		SysSyncConfig config = context.getConfig();
		SysSyncItemLog logItem = context.getLogItem();
		
		addToItemLog(logItem, "Account not exist in IDM");
		SynchronizationActionType actionType;
		AbstractEntity entity = findEntityByCorrelationAttribute(config.getCorrelationAttribute(),
				icAttributes);
		if (entity != null) {
			// Account not exist but, entity by correlation was
			// found (UNLINKED)
			actionType = config.getUnlinkedAction().getAction();
			SynchronizationSituationType situation = SynchronizationSituationType.UNLINKED;
			if (StringUtils.hasLength(config.getUnlinkedActionWfKey())) {
				SynchronizationUnlinkedActionType unlinkedActionType = config.getUnlinkedAction();
				SynchronizationActionType action = unlinkedActionType.getAction();

				// We will start specific workflow
				startWorkflow(config.getUnlinkedActionWfKey(), situation, action, entity, context);

			} else {
				context
				.addEntityId(entity.getId())
				.addSystemEntity(systemEntity);
				resolveUnlinkedSituation(config.getUnlinkedAction(), context);
			}
		} else {
			// Account not exist and entity too (UNMATCHED)
			actionType = config.getMissingEntityAction().getAction();
			SynchronizationSituationType situation = SynchronizationSituationType.MISSING_ENTITY;
			if (StringUtils.hasLength(config.getMissingEntityActionWfKey())) {
				SynchronizationMissingEntityActionType missingEntityAction = config
						.getMissingEntityAction();
				SynchronizationActionType action = missingEntityAction.getAction();

				// We will start specific workflow
				startWorkflow(config.getMissingEntityActionWfKey(), situation, action, entity, context);

			} else {
				resolveMissingEntitySituation(config.getMissingEntityAction(), context);
			}
		}
		return actionType;
	}

	/**
	 * Handle IC connector object
	 * 
	 * @param tokenAttribute
	 * @param itemContext
	 * @return
	 */
	protected boolean handleIcObject(SynchronizationContext itemContext) {
		Assert.notNull(itemContext);

		String uid = itemContext.getUid();
		IcConnectorObject icObject = itemContext.getIcObject();
		SysSyncConfig config = itemContext.getConfig();
		SysSyncLog log = itemContext.getLog();
		List<SysSyncActionLog> actionLogs = itemContext.getActionLogs();
		AttributeMapping tokenAttribute = itemContext.getTokenAttribute();
		
		SysSyncItemLog itemLog = new SysSyncItemLog();
		// Find token by token attribute
		// For Reconciliation can be token attribute null
		Object tokenObj = null;
		if (tokenAttribute != null) {
			tokenObj = getValueByMappedAttribute(tokenAttribute, icObject.getAttributes());
		}

		// Token is saved in Sync as String, therefore we transform token (from IcObject) to String too.
		String token = tokenObj != null ? tokenObj.toString() : null;

		// In custom filter mode, we don't have token. We find
		// token in object by tokenAttribute, but
		// order of returned (searched) objects is random. We
		// have to do !!STRING!! compare and save only
		// grater token to config and log.

		if (token != null && config.getToken() != null && token.compareTo(config.getToken()) <= -1) {
			token = config.getToken();
		}
		// Save token
		log.setToken(token);
		config.setToken(token);

		// Synchronization by custom filter not supported DELETE
		// event
		IcSyncDeltaTypeEnum type = IcSyncDeltaTypeEnum.CREATE_OR_UPDATE;
		itemContext
		.addLogItem(itemLog)
		.addType(type);

		boolean result = startItemSynchronization(itemContext);

		// We reload log (maybe was synchronization canceled)
		longRunningTaskExecutor.increaseCounter();
		log.setRunning(synchronizationLogService.get(log.getId()).isRunning());
		if (!log.isRunning()) {
			result = false;
		}
		if (!result) {
			log.setRunning(false);
			log.addToLog(MessageFormat.format("Synchronization canceled during resolve UID [{0}]", uid));
			addToItemLog(itemLog, "Canceled!");
			initSyncActionLog(SynchronizationActionType.IGNORE, OperationResultType.WARNING, itemLog, log, actionLogs);
		}
		return result;
	}

	/**
	 * Main method for synchronization item. This method is call form "custom
	 * filter" and "connector sync" mode.
	 * 
	 * @param uid
	 * @param icObject
	 * @param type
	 * @param entityType
	 * @param itemLog
	 * @param config
	 * @param system
	 * @param mappedAttributes
	 * @param log
	 * @param actionsLog
	 * @return
	 */
	protected boolean startItemSynchronization(SynchronizationContext itemContext) {
		String uid = itemContext.getUid();
		SysSyncConfig config = itemContext.getConfig();
		SystemEntityType entityType = itemContext.getEntityType();
		SysSyncLog log = itemContext.getLog();
		SysSyncItemLog itemLog = itemContext.getLogItem();
		
		List<SysSyncActionLog> actionsLog = new ArrayList<>();
		try {

			SyncActionLogFilter actionFilter = new SyncActionLogFilter();
			actionFilter.setSynchronizationLogId(log.getId());
			actionsLog.addAll(syncActionLogService.find(actionFilter, null).getContent());
			itemContext.addActionLogs(actionsLog);

			// Default setting for log item
			itemLog.setIdentification(uid);
			itemLog.setDisplayName(uid);
			itemLog.setType(entityType.getEntityType().getSimpleName());

			// Do synchronization for one item (produces item)
			// Start in new Transaction
			
			CoreEvent<SysSyncItemLog> event = new CoreEvent<SysSyncItemLog>(SynchronizationEventType.START_ITEM,
					itemLog);
			event.getProperties().put(SynchronizationService.WRAPPER_SYNC_ITEM, itemContext);
			EventResult<SysSyncItemLog> lastResult = entityEventManager.process(event).getLastResult();
			boolean result = false;
			if (lastResult != null
					&& lastResult.getEvent().getProperties().containsKey(SynchronizationService.RESULT_SYNC_ITEM)) {
				result = (boolean) lastResult.getEvent().getProperties().get(SynchronizationService.RESULT_SYNC_ITEM);
			}

			return result;
		} catch (Exception ex) {
			if (itemLog.getSyncActionLog() != null) {
				// We have to decrement count and log as error
				itemLog.getSyncActionLog().setOperationCount(itemLog.getSyncActionLog().getOperationCount() - 1);
				loggingException(itemLog.getSyncActionLog().getSyncAction(), log, itemLog, actionsLog, uid, ex);
			} else {
				loggingException(SynchronizationActionType.IGNORE, log, itemLog, actionsLog, uid, ex);
			}
			return true;
		} finally {
			synchronizationConfigService.save(config);
			if (itemLog.getSyncActionLog() == null) {
				addToItemLog(itemLog, MessageFormat.format("Missing action log for UID {0}!", uid));
				initSyncActionLog(SynchronizationActionType.IGNORE, OperationResultType.ERROR, itemLog, log,
						actionsLog);
			}
			// synchronizationLogService.save(log);
			syncActionLogService.saveAll(actionsLog);
			if (itemLog.getSyncActionLog() != null) {
				syncItemLogService.save(itemLog);
			}
		}
	}

	/**
	 * Start reconciliation. Is call after synchronization. Main purpose is find
	 * and resolve missing accounts
	 * 
	 * @param entityType
	 * @param systemAccountsMap
	 * @param config
	 * @param system
	 * @param log
	 * @param actionsLog
	 */
	protected void startReconciliation(SystemEntityType entityType, List<String> allAccountsList, SysSyncConfig config,
			SysSystem system, SysSyncLog log, List<SysSyncActionLog> actionsLog) {
		AccountFilter accountFilter = new AccountFilter();
		accountFilter.setSystemId(system.getId());
		
		List<AccAccount> accounts = accountService.find(accountFilter, null).getContent();

		for (AccAccount account : accounts) {
			if (!log.isRunning()) {
				return;
			}
			String uid = account.getRealUid();
			if (!allAccountsList.contains(uid)) {
				SysSyncItemLog itemLog = new SysSyncItemLog();
				try {

					// Default setting for log item
					itemLog.setIdentification(uid);
					itemLog.setDisplayName(uid);
					itemLog.setType(entityType.getEntityType().getSimpleName());

					// Do reconciliation for one item (produces event)
					// Start in new Transaction
					SynchronizationContext builder = new SynchronizationContext();
					builder
					.addUid(uid)
					.addType(IcSyncDeltaTypeEnum.DELETE)
					.addConfig(config)
					.addSystem(system)
					.addEntityType(entityType)
					.addAccount(account)
					.addLog(log)
					.addLogItem(itemLog)
					.addActionLogs(actionsLog);
					
					CoreEvent<SysSyncItemLog> event = new CoreEvent<SysSyncItemLog>(SynchronizationEventType.START_ITEM,
							itemLog);
					event.getProperties().put(SynchronizationService.WRAPPER_SYNC_ITEM, builder);
					EventResult<SysSyncItemLog> lastResult = entityEventManager.process(event).getLastResult();
					boolean result = false;
					if (lastResult != null && lastResult.getEvent().getProperties()
							.containsKey(SynchronizationService.RESULT_SYNC_ITEM)) {
						result = (boolean) lastResult.getEvent().getProperties()
								.get(SynchronizationService.RESULT_SYNC_ITEM);
					}

					// We reload log (maybe was synchronization canceled)
					log.setRunning(synchronizationLogService.get(log.getId()).isRunning());
					if (!log.isRunning()) {
						result = false;
					}
					if (!result) {
						log.setRunning(false);
						log.addToLog(MessageFormat.format("Synchronization canceled during resolve UID [{0}]", uid));
						addToItemLog(itemLog, "Canceled!");
						initSyncActionLog(SynchronizationActionType.IGNORE, OperationResultType.WARNING, itemLog, log,
								actionsLog);
					}

				} catch (Exception ex) {
					String message = MessageFormat.format("Reconciliation - error for uid {0}", uid);
					log.addToLog(message);
					log.addToLog(Throwables.getStackTraceAsString(ex));
					LOG.error(message, ex);
				} finally {
					synchronizationConfigService.save(config);
					if (itemLog.getSyncActionLog() == null) {
						addToItemLog(itemLog, MessageFormat.format("Missing action log for UID {0}!", uid));
						initSyncActionLog(SynchronizationActionType.IGNORE, OperationResultType.ERROR, itemLog, log,
								actionsLog);
					}
					// synchronizationLogService.save(log);
					syncActionLogService.saveAll(actionsLog);
					if (itemLog.getSyncActionLog() != null) {
						syncItemLogService.save(itemLog);
					}
				}
			}
		}
	}

	/**
	 * Start export entities to target resource
	 * @param entityType
	 * @param config
	 * @param mappedAttributes
	 * @param log
	 * @param actionsLog
	 */
	@Beta
	protected void startExport(SystemEntityType entityType, SysSyncConfig config,
			List<SysSystemAttributeMapping> mappedAttributes, SysSyncLog log, List<SysSyncActionLog> actionsLog) {

		SysSystem system = config.getSystemMapping().getSystem();
		SysSystemAttributeMapping uidAttribute = systemAttributeMappingService.getUidAttribute(mappedAttributes,
				system);

		List<? extends AbstractEntity> entities = this.findAllEntity();
		entities.stream().forEach(entity -> {

			// TODO: evaluate to groovy script
			
			SynchronizationContext itemBuilder = new SynchronizationContext();
			itemBuilder.addConfig(config) //
					.addSystem(system) //
					.addEntityType(entityType) //
					.addEntityId(entity.getId()) //
					.addLog(log) //
					.addActionLogs(actionsLog);
			// Start export for this entity
			exportEntity(itemBuilder, uidAttribute, entity);
		});
	}

	/**
	 * Start export item (entity) to target resource
	 * @param itemBuilder
	 * @param uidAttribute
	 * @param entity
	 */
	protected void exportEntity(SynchronizationContext itemBuilder, SysSystemAttributeMapping uidAttribute,
			AbstractEntity entity) {
		SystemEntityType entityType = itemBuilder.getEntityType();
		SysSyncConfig config = itemBuilder.getConfig();
		SysSyncLog log = itemBuilder.getLog();
		List<SysSyncActionLog> actionsLog = itemBuilder.getActionLogs();
		SysSystem system = itemBuilder.getSystem();
		SysSyncItemLog itemLog = new SysSyncItemLog();
		try {
			// Default setting for log item
			itemLog.setIdentification(entity.getId().toString());
			itemLog.setDisplayName(this.getDisplayNameForEntity(entity));
			itemLog.setType(entityType.getEntityType().getSimpleName());
			itemLog.addToLog(MessageFormat.format("Start export for entity [{0}].",this.getDisplayNameForEntity(entity)));

			UUID accountId = this.getAccountByEntity(entity.getId(), system.getId());
			if (accountId != null) {
				initSyncActionLog(SynchronizationActionType.CREATE_ACCOUNT, OperationResultType.IGNORE, itemLog, log,
						actionsLog);
				itemLog.addToLog(MessageFormat.format(
						"For entity [{0}] was found AccAccount [{1}]. Export for this entity ends (only entity without AccAccount can be export)!",
						this.getDisplayNameForEntity(entity), accountId));
				return;
			}

			String uid = systemAttributeMappingService.generateUid(entity, uidAttribute);

			// Do export for one item (produces event)
			// Start in new Transaction

			itemBuilder.addUid(uid) //
					.addConfig(config) //
					.addSystem(system) //
					.addEntityType(entityType) //
					.addEntityId(entity.getId()).addLog(log) //
					.addLogItem(itemLog) //
					.addActionLogs(actionsLog) //
					.addExportAction(true);

			CoreEvent<SysSyncItemLog> event = new CoreEvent<SysSyncItemLog>(SynchronizationEventType.START_ITEM,
					itemLog);
			event.getProperties().put(SynchronizationService.WRAPPER_SYNC_ITEM, itemBuilder);
			EventResult<SysSyncItemLog> lastResult = entityEventManager.process(event).getLastResult();
			boolean result = false;
			if (lastResult != null
					&& lastResult.getEvent().getProperties().containsKey(SynchronizationService.RESULT_SYNC_ITEM)) {
				result = (boolean) lastResult.getEvent().getProperties().get(SynchronizationService.RESULT_SYNC_ITEM);
			}

			// We reload log (maybe was synchronization canceled)
			log.setRunning(synchronizationLogService.get(log.getId()).isRunning());
			if (!log.isRunning()) {
				result = false;
			}
			if (!result) {
				log.setRunning(false);
				log.addToLog(MessageFormat.format("Synchronization canceled during resolve UID [{0}]", uid));
				addToItemLog(itemLog, "Canceled!");
				initSyncActionLog(SynchronizationActionType.IGNORE, OperationResultType.WARNING, itemLog, log,
						actionsLog);
			}

		} catch (Exception ex) {
			String message = MessageFormat.format("Export - error for entity {0}", entity.getId());
			log.addToLog(message);
			log.addToLog(Throwables.getStackTraceAsString(ex));
			LOG.error(message, ex);
		} finally {
			synchronizationConfigService.save(config);
			if (itemLog.getSyncActionLog() == null) {
				addToItemLog(itemLog, MessageFormat.format("Missing action log for entity {0}!", entity.getId()));
				initSyncActionLog(SynchronizationActionType.IGNORE, OperationResultType.ERROR, itemLog, log,
						actionsLog);
			}
			syncActionLogService.saveAll(actionsLog);
			if (itemLog.getSyncActionLog() != null) {
				syncItemLogService.save(itemLog);
			}
		}
	}
	
	/**
	 * Validate synchronization on: Exist, enable, running, has mapping,
	 * has connector key, has connector configuration 
	 * @param synchronizationConfigId
	 * @return
	 */
	protected SynchronizationContext validate(UUID synchronizationConfigId){
		
		SynchronizationContext context = new SynchronizationContext();
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
		
		context
		.addConfig(config)
		.addSystem(system)
		.addEntityType(entityType)
		.addMappedAttributes(mappedAttributes)
		.addConnectorConfig(connectorConfig);
		
		return context;
	}


	/**
	 * Compile filter for search from filter attribute and filter script
	 * 
	 * @param config
	 * @return
	 */
	protected IcFilter resolveSynchronizationFilter(SysSyncConfig config) {
		// If is reconciliation, then is filter null
		if (config.isReconciliation()) {
			return null;
		}
		IcFilter filter = null;
		AttributeMapping filterAttributeMapping = config.getFilterAttribute();
		String configToken = config.getToken();
		String filterScript = config.getCustomFilterScript();

		if (filterAttributeMapping == null && configToken == null && StringUtils.isEmpty(filterScript)) {
			return null;
		}

		if (filterAttributeMapping != null) {
			Object transformedValue = systemAttributeMappingService.transformValueToResource(null, configToken,
					filterAttributeMapping, config);

			if (transformedValue != null) {
				IcAttributeImpl filterAttribute = new IcAttributeImpl(
						filterAttributeMapping.getSchemaAttribute().getName(), transformedValue);

				switch (config.getFilterOperation()) {
				case GREATER_THAN:
					filter = IcFilterBuilder.greaterThan(filterAttribute);
					break;

				case LESS_THAN:
					filter = IcFilterBuilder.lessThan(filterAttribute);
					break;

				case EQUAL_TO:
					filter = IcFilterBuilder.equalTo(filterAttribute);
					break;

				case CONTAINS:
					filter = IcFilterBuilder.contains(filterAttribute);
					break;

				case ENDS_WITH:
					filter = IcFilterBuilder.endsWith(filterAttribute);
					break;

				case STARTS_WITH:
					filter = IcFilterBuilder.startsWith(filterAttribute);
					break;
				}
			}
		}

		if (StringUtils.hasLength(filterScript)) {
			Map<String, Object> variables = new HashMap<>();
			variables.put("filter", filter);
			variables.put("token", configToken);

			List<Class<?>> allowTypes = new ArrayList<>();
			// Allow all IC filter operator
			for (IcFilterOperationType operation : IcFilterOperationType.values()) {
				allowTypes.add(operation.getImplementation());
			}
			allowTypes.add(IcAndFilter.class);
			allowTypes.add(IcOrFilter.class);
			allowTypes.add(IcFilterBuilder.class);
			allowTypes.add(IcAttributeImpl.class);
			allowTypes.add(IcAttribute.class);
			Object filterObj = groovyScriptService.evaluate(filterScript, variables, allowTypes);
			if (filterObj != null && !(filterObj instanceof IcFilter)) {
				throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_FILTER_VALUE_WRONG_TYPE,
						ImmutableMap.of("type", filterObj.getClass().getName()));
			}
			filter = (IcFilter) filterObj;
		}
		return filter;
	}

	/**
	 * Method for resolve linked situation for one item.
	 */
	@Override
	public void resolveLinkedSituation(SynchronizationLinkedActionType action, SynchronizationContext context) {
		
		String uid = context.getUid();
		SystemEntityType entityType = context.getEntityType();
		SysSyncLog log = context.getLog(); 
		SysSyncItemLog logItem = context.getLogItem();
		List<SysSyncActionLog> actionLogs = context.getActionLogs();
		List<SysSystemAttributeMapping> mappedAttributes = context.getMappedAttributes();
		AccAccount account = context.getAccount();

		addToItemLog(logItem, MessageFormat.format("IdM Account ({0}) exist in IDM (LINKED)", account.getUid()));
		addToItemLog(logItem, MessageFormat.format("Linked action is {0}", action));
		switch (action) {
		case IGNORE:
			// Linked action is IGNORE. We will do nothing
			initSyncActionLog(SynchronizationActionType.LINKED, OperationResultType.IGNORE, logItem, log, actionLogs);
			return;
		case UNLINK:
			// Linked action is UNLINK
			doUnlink(account, false, log, logItem, actionLogs);

			initSyncActionLog(SynchronizationActionType.UNLINK, OperationResultType.SUCCESS, logItem, log, actionLogs);

			return;
		case UNLINK_AND_REMOVE_ROLE:
			// Linked action is UNLINK_AND_REMOVE_ROLE
			doUnlink(account, true, log, logItem, actionLogs);

			initSyncActionLog(SynchronizationActionType.UNLINK, OperationResultType.SUCCESS, logItem, log, actionLogs);

			return;
		case UPDATE_ENTITY:
			// Linked action is UPDATE_ENTITY
			doUpdateEntity(account, entityType, uid, context.getIcObject().getAttributes(), mappedAttributes, log, logItem, actionLogs);
			initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.SUCCESS, logItem, log,
					actionLogs);
			return;
		case UPDATE_ACCOUNT:
			// Linked action is UPDATE_ACCOUNT
			doUpdateAccount(account, entityType, log, logItem, actionLogs);
			initSyncActionLog(SynchronizationActionType.UPDATE_ACCOUNT, OperationResultType.SUCCESS, logItem, log,
					actionLogs);
			return;
		default:
			break;
		}
	}

	/**
	 * Method for resolve missing entity situation for one item.
	 */
	@Override
	public void resolveMissingEntitySituation(SynchronizationMissingEntityActionType actionType, SynchronizationContext context) {
		
		String uid = context.getUid();
		SystemEntityType entityType = context.getEntityType();
		SysSystem system = context.getSystem();
		SysSyncLog log = context.getLog(); 
		SysSyncItemLog logItem = context.getLogItem();
		List<SysSyncActionLog> actionLogs = context.getActionLogs();
		List<SysSystemAttributeMapping> mappedAttributes = context.getMappedAttributes();
		
		addToItemLog(logItem, "Account not exist and entity too (missing entity).");

		switch (actionType) {
		case IGNORE:
			// Ignore we will do nothing
			addToItemLog(logItem, "Missing entity action is IGNORE, we will do nothing.");
			initSyncActionLog(SynchronizationActionType.MISSING_ENTITY, OperationResultType.IGNORE, logItem, log,
					actionLogs);
			return;
		case CREATE_ENTITY:
			// Create idm account
			AccAccount account = doCreateIdmAccount(uid, system);
			// Find and set SystemEntity (must exist)
			account.setSystemEntity(this.findSystemEntity(uid, system, entityType));
			accountService.save(account);

			// Create new entity
			doCreateEntity(entityType, mappedAttributes, logItem, uid, context.getIcObject().getAttributes(), account);
			initSyncActionLog(SynchronizationActionType.CREATE_ENTITY, OperationResultType.SUCCESS, logItem, log,
					actionLogs);
			return;
		}
	}

	/**
	 * Method for resolve unlinked situation for one item.
	 */
	@Override
	public void resolveUnlinkedSituation(SynchronizationUnlinkedActionType action, SynchronizationContext context) {
		
		String uid = context.getUid();
		UUID entityId = context.getEntityId();
		SystemEntityType entityType = context.getEntityType();
		SysSystem system = context.getSystem();
		SysSyncLog log = context.getLog(); 
		SysSyncItemLog logItem = context.getLogItem();
		List<SysSyncActionLog> actionLogs = context.getActionLogs();
		SysSystemEntity systemEntity = context.getSystemEntity();
		
		addToItemLog(logItem, "Account not exist but, entity by correlation was found (entity unlinked).");
		addToItemLog(logItem, MessageFormat.format("Unlinked action is {0}", action));
		AbstractEntity entity = findEntityById(entityId, entityType);

		switch (action) {
		case IGNORE:
			// Ignore we will do nothing
			initSyncActionLog(SynchronizationActionType.UNLINKED, OperationResultType.IGNORE, logItem, log, actionLogs);
			return;
		case LINK:
			// Create idm account
			doCreateLink(uid, false, entity, systemEntity, entityType, system, logItem);
			initSyncActionLog(SynchronizationActionType.LINK, OperationResultType.SUCCESS, logItem, log, actionLogs);
			return;
		case LINK_AND_UPDATE_ACCOUNT:
			// Create idm account
			doCreateLink(uid, true, entity, systemEntity, entityType, system, logItem);
			initSyncActionLog(SynchronizationActionType.LINK_AND_UPDATE_ACCOUNT, OperationResultType.SUCCESS, logItem,
					log, actionLogs);
			return;

		}
	}

	protected abstract AbstractEntity findEntityById(UUID entityId, SystemEntityType entityType);

	/**
	 * Method for resolve missing account situation for one item.
	 */
	@Override
	public void resolveMissingAccountSituation(ReconciliationMissingAccountActionType action, SynchronizationContext context) {
		
		SystemEntityType entityType = context.getEntityType();
		SysSyncLog log = context.getLog(); 
		SysSyncItemLog logItem = context.getLogItem();
		List<SysSyncActionLog> actionLogs = context.getActionLogs();
		AccAccount account = context.getAccount();
		
		addToItemLog(logItem,
				"Account on target system not exist but, account in IdM was found (missing account situation).");
		addToItemLog(logItem, MessageFormat.format("Missing account action is {0}", action));
		switch (action) {
		case IGNORE:
			// Ignore we will do nothing
			initSyncActionLog(SynchronizationActionType.MISSING_ACCOUNT, OperationResultType.IGNORE, logItem, log,
					actionLogs);
			return;
		case CREATE_ACCOUNT:
			doUpdateAccount(account, entityType, log, logItem, actionLogs);
			initSyncActionLog(SynchronizationActionType.CREATE_ACCOUNT, OperationResultType.SUCCESS, logItem, log,
					actionLogs);
			return;
		case DELETE_ENTITY:
			doDeleteEntity(account, entityType, log, logItem, actionLogs);
			initSyncActionLog(SynchronizationActionType.DELETE_ENTITY, OperationResultType.SUCCESS, logItem, log,
					actionLogs);
			return;
		case UNLINK:
			doUnlink(account, false, log, logItem, actionLogs);
			initSyncActionLog(SynchronizationActionType.UNLINK, OperationResultType.SUCCESS, logItem, log, actionLogs);
			return;
		case UNLINK_AND_REMOVE_ROLE:
			doUnlink(account, true, log, logItem, actionLogs);
			initSyncActionLog(SynchronizationActionType.UNLINK_AND_REMOVE_ROLE, OperationResultType.SUCCESS, logItem,
					log, actionLogs);
			return;

		}
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
	protected abstract void doUpdateAccount(AccAccount account, SystemEntityType entityType, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs);

	/**
	 * Call provisioning for given account
	 * 
	 * @param entity
	 * @param entityType
	 * @param logItem
	 */
	protected abstract void doUpdateAccountByEntity(AbstractEntity entity, SystemEntityType entityType,
			SysSyncItemLog logItem);

	/**
	 * Create new instance of ACC account
	 * 
	 * @param uid
	 * @param system
	 * @return
	 */
	protected AccAccount doCreateIdmAccount(String uid, SysSystem system) {
		AccAccount account = new AccAccount();
		account.setSystem(system);
		account.setAccountType(AccountType.PERSONAL);
		account.setUid(uid);
		return account;
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
	protected abstract void doCreateEntity(SystemEntityType entityType,
			List<SysSystemAttributeMapping> mappedAttributes, SysSyncItemLog logItem, String uid,
			List<IcAttribute> icAttributes, AccAccount account);

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
	protected abstract void doUpdateEntity(AccAccount account, SystemEntityType entityType, String uid,
			List<IcAttribute> icAttributes, List<SysSystemAttributeMapping> mappedAttributes, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs);

	/**
	 * Add message to logItem. Add timestamp.
	 * 
	 * @param logItem
	 * @param text
	 */
	protected void addToItemLog(Loggable logItem, String text) {
		StringBuilder sb = new StringBuilder();
		sb.append(DateTime.now());
		sb.append(": ");
		sb.append(text);
		text = sb.toString();
		if (logItem == null) {
			// Log item is null, we will log to console only.
			// We probably call this outside standard sync cycle (workflow
			// maybe)
			LOG.info(text);
		} else {
			logItem.addToLog(text);
			LOG.info(text);
		}
	}

	/**
	 * Operation remove entity account relations and linked roles
	 * 
	 * @param account
	 * @param removeIdentityRole
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	protected abstract void doUnlink(AccAccount account, boolean removeIdentityRole, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs);

	/**
	 * Log exception to SyncLog and SyncItemLog, do init syncActionLog
	 * 
	 * @param synchronizationActionType
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 * @param uid
	 * @param e
	 */
	private void loggingException(SynchronizationActionType synchronizationActionType, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs, String uid, Exception e) {
		String message = MessageFormat.format("Synchronization - exception during {0} for UID {1}",
				synchronizationActionType, uid);
		log.setContainsError(true);
		logItem.setMessage(message);
		addToItemLog(logItem, Throwables.getStackTraceAsString(e));
		initSyncActionLog(synchronizationActionType, OperationResultType.ERROR, logItem, log, actionLogs);
		LOG.error(message, e);
	}

	/**
	 * Find entity by correlation attribute
	 * 
	 * @param attribute
	 * @param entityType
	 * @param icAttributes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected AbstractEntity findEntityByCorrelationAttribute(AttributeMapping attribute, List<IcAttribute> icAttributes) {
		Assert.notNull(attribute);
		Assert.notNull(icAttributes);

		Object value = getValueByMappedAttribute(attribute, icAttributes);
		if (value == null) {
			return null;
		}
		if (attribute.isEntityAttribute()) {
			return findEntityByAttribute(attribute.getIdmPropertyName(), value.toString());
		} else if (attribute.isExtendedAttribute()) {
			try {
				Serializable serializableValue = Serializable.class.cast(value);
				List<? extends AbstractEntity> entities = (List<? extends AbstractEntity>) formService.findOwners(getEntityClass(), attribute.getName(), serializableValue, null).getContent();
				if (CollectionUtils.isEmpty(entities)) {
					return null;
				}
				if (entities.size() > 1) {
					throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_CORRELATION_TO_MANY_RESULTS,
							ImmutableMap.of("correlationAttribute", attribute.getName(), "value", value));
				}
				if (entities.size() == 1) {
					return entities.get(0);
				}
			} catch (ClassCastException e) {
				throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_CORRELATION_BAD_VALUE,
						ImmutableMap.of("value", value), e);
			}
		}
		return null;
	}
	
	/**
	 * Find entity by idm attribute
	 * 
	 * @param idmAttributeName
	 * @param value
	 * @return
	 */
	protected abstract AbstractEntity findEntityByAttribute(String idmAttributeName, String value);
	
	/**
	 * Return entity class for synchronization.
	 * 
	 * @return
	 */
	protected abstract Class<? extends FormableEntity> getEntityClass();
	
	/**
	 * Return repository
	 * 
	 * @return
	 */
	protected abstract BaseEntityRepository getRepository();
	
	/**
	 * Return specific correlation filter
	 * 
	 * @return
	 */
	protected abstract CorrelationFilter getEntityFilter();
	
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
			// Set transformed value from target system to entity
			try {
				EntityUtils.setEntityValue(entity, attributeProperty, transformedValue);
			} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | ProvisioningException e) {
				throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_IDM_FIELD_NOT_SET,
						ImmutableMap.of("property", attributeProperty, "uid", uid), e);
			}

		});
		return entity;
	}

	/**
	 * Update extended attribute for given entity. Entity must be persisted
	 * first.
	 * 
	 * @param mappedAttributes
	 * @param uid
	 * @param icAttributes
	 * @param entity
	 * @param create (is create or update entity situation)
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected AbstractEntity updateExtendedAttributes(List<SysSystemAttributeMapping> mappedAttributes, String uid,
			List<IcAttribute> icAttributes, AbstractEntity entity, boolean create) {
		mappedAttributes.stream().filter(attribute -> {
			// Skip disabled attributes
			// Only for extended attributes
			boolean fastResult =  !attribute.isDisabledAttribute() && attribute.isExtendedAttribute();
			if(!fastResult){
				return false;
			}
			// Can be value set by attribute strategy?
			return this.canSetValue(uid, attribute, entity, create);

		}).forEach(attribute -> {
			String attributeProperty = attribute.getIdmPropertyName();
			Object transformedValue = getValueByMappedAttribute(attribute, icAttributes);
			// Save to extended attribute

			if (!(entity instanceof FormableEntity)) {
				String message = MessageFormat.format("Entity [{0}] is not instance of fromable entity!",
						entity.getId());
				throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_ERROR_DURING_SYNC_ITEM,
						ImmutableMap.of("uid", uid, "message", message));
			}
			IdmFormAttribute defAttribute = formService.getDefinition(((FormableEntity) entity).getClass())
					.getMappedAttributeByName(attributeProperty);
			if (defAttribute == null) {
				// eav definition could be changed
				String message = MessageFormat.format("Form attribute defininion [{0}] was not found!",
						attributeProperty);
				throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_ERROR_DURING_SYNC_ITEM,
						ImmutableMap.of("uid", uid, "message", message));
			}
			if (transformedValue instanceof List<?>) {
				((List<?>) transformedValue).stream().forEach(value -> {
					if (value != null && !(value instanceof Serializable)) {
						String message = MessageFormat.format(
								"Value is not serializable [{0}] for attribute [{1}] and UID [{2}]!", value, attribute,
								uid);
						throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_ERROR_DURING_SYNC_ITEM,
								ImmutableMap.of("uid", uid, "message", message));
					}
				});
				formService.saveValues(entity.getId(), (Class<? extends FormableEntity>) entity.getClass(), defAttribute, (List<Serializable>) transformedValue);
			} else {
				formService.saveValues(entity.getId(), (Class<? extends FormableEntity>) entity.getClass(), defAttribute,
						Lists.newArrayList((Serializable) transformedValue));
			}
		});
		return entity;
	}

	/**
	 * Update confidential attribute for given entity. Entity must be persisted
	 * first.
	 * 
	 * @param mappedAttributes
	 * @param uid
	 * @param icAttributes
	 * @param entity
	 * @param create (is create or update entity situation)
	 * @return
	 */
	protected AbstractEntity updateConfidentialAttributes(List<SysSystemAttributeMapping> mappedAttributes, String uid,
			List<IcAttribute> icAttributes, AbstractEntity entity, boolean create) {
		mappedAttributes.stream().filter(attribute -> {
			// Skip disabled attributes
			// Only for confidential attribute
			boolean fastResult =  !attribute.isDisabledAttribute() && attribute.isConfidentialAttribute();
			if(!fastResult){
				return false;
			}
			// Can be value set by attribute strategy?
			return this.canSetValue(uid, attribute, entity, create);

		}).forEach(attribute -> {
			String attributeProperty = attribute.getIdmPropertyName();
			Object transformedValue = getValueByMappedAttribute(attribute, icAttributes);
			// If is attribute confidential, then we will set
			// value to
			// secured storage
			if (!(transformedValue == null || transformedValue instanceof GuardedString)) {
				throw new ProvisioningException(AccResultCode.CONFIDENTIAL_VALUE_IS_NOT_GUARDED_STRING,
						ImmutableMap.of("property", attributeProperty, "class", transformedValue.getClass().getName()));
			}

			confidentialStorage.saveGuardedString(entity, attribute.getIdmPropertyName(),
					(GuardedString) transformedValue);

		});
		return entity;
	}
	
	/**
	 * Return true if can be value set to this entity for this mapped attribute.
	 * @param uid
	 * @param attribute
	 * @param entity
	 * @param create (create or update entity situation)
	 * @return
	 */
	protected boolean canSetValue(String uid, SysSystemAttributeMapping attribute, AbstractEntity entity,
			boolean create) {
		Assert.notNull(attribute);
		AttributeMappingStrategyType strategyType = attribute.getStrategyType();
		switch (strategyType) {
			case CREATE: {
				return create ? true : false;
			}
			case SET: {
				return true;
			}
	
			case WRITE_IF_NULL: {
				Object value = systemAttributeMappingService.getAttributeValue(uid, entity, attribute);
				return value == null ? true : false;
			}
			default: {
				return false;
			}
		}
	}

	protected Object getValueByMappedAttribute(AttributeMapping attribute, List<IcAttribute> icAttributes) {
		Object icValue = null;
		Optional<IcAttribute> optionalIcAttribute = icAttributes.stream().filter(icAttribute -> {
			return attribute.getSchemaAttribute().getName().equals(icAttribute.getName());
		}).findFirst();
		if (optionalIcAttribute.isPresent()) {
			IcAttribute icAttribute = optionalIcAttribute.get();
			if (icAttribute.isMultiValue()) {
				icValue = icAttribute.getValues();
			} else {
				icValue = icAttribute.getValue();
			}
		}

		Object transformedValue = systemAttributeMappingService.transformValueFromResource(icValue, attribute, icAttributes);
		return transformedValue;
	}

	private AccAccount findAccount(String uid, SystemEntityType entityType, SysSystemEntity systemEntity,
			SysSystem system, SysSyncItemLog logItem) {
		AccAccount account = null;
		AccountFilter accountFilter = new AccountFilter();
		accountFilter.setSystemId(system.getId());
		List<AccAccount> accounts = null;
		if (systemEntity != null) {
			// System entity for this uid was found. We will find account
			// for this system entity.
			addToItemLog(logItem,
					MessageFormat.format(
							"System entity for this uid ({0}) was found. We will find account for this system entity ({1})",
							uid, systemEntity.getId()));
			accountFilter.setSystemEntityId(systemEntity.getId());
			accounts = accountService.find(accountFilter, null).getContent();
		}
		if (CollectionUtils.isEmpty(accounts)) {
			// System entity was not found. We will find account by uid
			// directly.
			addToItemLog(logItem, MessageFormat
					.format("System entity was not found. We will find account for uid ({0}) directly", uid));
			accountFilter.setUidId(uid);
			accountFilter.setSystemEntityId(null);
			accounts = accountService.find(accountFilter, null).getContent();
		}
		if (accounts.size() > 1) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_TO_MANY_ACC_ACCOUNT, uid);
		}
		if (!accounts.isEmpty()) {
			account = accounts.get(0);
		}
		return account;
	}

	private SysSystemEntity createSystemEntity(String uid, SystemEntityType entityType, SysSystem system) {
		SysSystemEntity systemEntityNew = new SysSystemEntity();
		systemEntityNew.setUid(uid);
		systemEntityNew.setEntityType(entityType);
		systemEntityNew.setSystem(system);
		return systemEntityService.save(systemEntityNew);
	}

	private SysSystemEntity findSystemEntity(String uid, SysSystem system, SystemEntityType entityType) {
		SystemEntityFilter systemEntityFilter = new SystemEntityFilter();
		systemEntityFilter.setEntityType(entityType);
		systemEntityFilter.setSystemId(system.getId());
		systemEntityFilter.setUidId(uid);
		List<SysSystemEntity> systemEntities = systemEntityService.find(systemEntityFilter, null).getContent();
		SysSystemEntity systemEntity = null;
		if (systemEntities.size() == 1) {
			systemEntity = systemEntities.get(0);
		} else if (systemEntities.size() > 1) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_TO_MANY_SYSTEM_ENTITY, uid);
		}
		return systemEntity;
	}

	/**
	 * Start workflow process by wfDefinitionKey. Create input variables and put
	 * them to process. If is log variable present after process started, then
	 * will be log add to synchronization log.
	 * 
	 * @param wfDefinitionKey
	 * @param uid
	 * @param situation
	 * @param action
	 * @param icAttributes
	 * @param entity
	 * @param account
	 * @param entityType
	 * @param config
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	private void startWorkflow(String wfDefinitionKey, SynchronizationSituationType situation,
			SynchronizationActionType action, AbstractEntity entity,SynchronizationContext context) {
		
		SystemEntityType entityType = context.getEntityType();
		SysSyncLog log = context.getLog(); 
		SysSyncItemLog logItem = context.getLogItem();
		List<SysSyncActionLog> actionLogs = context.getActionLogs();
		AccAccount account = context.getAccount();
		String uid = context.getUid();
		SysSyncConfig config = context.getConfig();
		
		addToItemLog(logItem,
				MessageFormat.format("Workflow for {0} situation was found. We will start him.", situation));

		Map<String, Object> variables = new HashMap<>();
		variables.put(SynchronizationService.WF_VARIABLE_KEY_UID, uid);
		variables.put(SynchronizationService.WF_VARIABLE_KEY_ENTITY_TYPE, entityType);
		variables.put(SynchronizationService.WF_VARIABLE_KEY_SYNC_SITUATION, situation.name());
		variables.put(SynchronizationService.WF_VARIABLE_KEY_IC_ATTRIBUTES, context.getIcObject().getAttributes());
		variables.put(SynchronizationService.WF_VARIABLE_KEY_ACTION_TYPE, action.name());
		variables.put(SynchronizationService.WF_VARIABLE_KEY_ENTITY_ID, entity != null ? entity.getId() : null);
		variables.put(SynchronizationService.WF_VARIABLE_KEY_ACC_ACCOUNT_ID, account != null ? account.getId() : null);
		variables.put(SynchronizationService.WF_VARIABLE_KEY_SYNC_CONFIG_ID, config.getId());

		ProcessInstance processInstance = workflowProcessInstanceService.startProcess(wfDefinitionKey,
				SysSyncConfig.class.getSimpleName(), uid, config.getId().toString(), variables);

		if (processInstance instanceof VariableScope) {
			Object logItemObj = ((VariableScope) processInstance)
					.getVariable(SynchronizationService.WF_VARIABLE_KEY_LOG_ITEM);
			if (logItemObj instanceof String) {
				addToItemLog(logItem, (String) logItemObj);
			}

		}
		if (processInstance.isEnded()) {
			addToItemLog(logItem, MessageFormat.format("Workflow (with id {0}) for missing entity situation ended.",
					processInstance.getId()));
			initSyncActionLog(situation.getAction(), OperationResultType.WF, logItem, log, actionLogs);

		} else {
			addToItemLog(logItem,
					MessageFormat.format(
							"Workflow (with id {0}) for missing entity situation not ended (will be ended asynchronously).",
							processInstance.getId()));
			initSyncActionLog(situation.getAction(), OperationResultType.WF, logItem, log, actionLogs);
		}
	}

	private Session getHibernateSession() {
		return (Session) this.entityManager.getDelegate();
	}

	/**
	 * Init sync action log
	 * 
	 * @param actionType
	 * @param resultType
	 * @param logItem
	 * @param log
	 * @param actionLogs
	 */
	protected void initSyncActionLog(SynchronizationActionType actionType, OperationResultType resultType,
			SysSyncItemLog logItem, SysSyncLog log, List<SysSyncActionLog> actionLogs) {

		if (logItem == null || actionLogs == null) {
			// If is logItem null, then we have nothing for init.
			// We probably call this outside standard sync cycle (workflow
			// maybe)
			return;
		}

		if (logItem.getSyncActionLog() != null && !(OperationResultType.ERROR == resultType)) {
			// Log is already initialized, but if is new result type ERROR, then
			// have priority
			return;
		}
		SysSyncActionLog actionLog = null;
		Optional<SysSyncActionLog> optionalActionLog = actionLogs.stream().filter(al -> {
			return actionType == al.getSyncAction() && resultType == al.getOperationResult();
		}).findFirst();
		if (optionalActionLog.isPresent()) {
			actionLog = optionalActionLog.get();
		} else {
			actionLog = new SysSyncActionLog();
			actionLog.setOperationResult(resultType);
			actionLog.setSyncAction(actionType);
			actionLog.setSyncLog(log);
			actionLogs.add(actionLog);
		}
		logItem.setSyncActionLog(actionLog);
		actionLog.setOperationCount(actionLog.getOperationCount() + 1);
		addToItemLog(logItem, MessageFormat.format("Operation count for [{0}] is [{1}]", actionLog.getSyncAction(),
				actionLog.getOperationCount()));
	}


	/**
	 * Find entity by account
	 * 
	 * @param account
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 * @return
	 */
	protected UUID getEntityByAccount(UUID accountId) {
		EntityAccountFilter entityAccountFilter = createEntityAccountFilter();
		entityAccountFilter.setAccountId(accountId);
		entityAccountFilter.setOwnership(Boolean.TRUE);
		@SuppressWarnings("unchecked")
		List<EntityAccountDto> entityAccounts = this.getEntityAccountService()
				.find((BaseFilter) entityAccountFilter, null).getContent();
		if (entityAccounts.isEmpty()) {
			return null;
		} else {
			// We assume that all identity accounts
			// (mark as
			// ownership) have same identity!
			return entityAccounts.get(0).getEntity();
		}
	}
	
	/**
	 * Find account ID by entity ID
	 * @param entityId
	 * @param systemId
	 * @return
	 */
	protected UUID getAccountByEntity(UUID entityId, UUID systemId) {
		EntityAccountFilter entityAccountFilter = createEntityAccountFilter();
		entityAccountFilter.setEntityId(entityId);
		entityAccountFilter.setSystemId(systemId);
		entityAccountFilter.setOwnership(Boolean.TRUE);
		@SuppressWarnings("unchecked")
		List<EntityAccountDto> entityAccounts = this.getEntityAccountService().find((BaseFilter) entityAccountFilter, null).getContent();
		if (entityAccounts.isEmpty()) {
			return null;
		} else {
			// We assume that all entity accounts
			// (mark as
			// ownership) have same account!
			return entityAccounts.get(0).getEntity();
		}
	}

	/**
	 * Create account and relation on him
	 * 
	 * @param uid
	 * @param callProvisioning
	 * @param entity
	 * @param systemEntity
	 * @param entityType
	 * @param system
	 * @param logItem
	 */
	@SuppressWarnings("unchecked")
	protected void doCreateLink(String uid, boolean callProvisioning, AbstractEntity entity,
			SysSystemEntity systemEntity, SystemEntityType entityType, SysSystem system, SysSyncItemLog logItem) {
		AccAccount account = doCreateIdmAccount(uid, system);
		if (systemEntity != null) {
			// If SystemEntity for this account already exist, then we linked
			// him to new account
			account.setSystemEntity(systemEntity);
		}

		accountService.save(account);
		addToItemLog(logItem,
				MessageFormat.format("Account with uid {0} and id {1} was created", uid, account.getId()));

		// Create new entity account relation
		EntityAccountDto entityAccount = this.createEntityAccountDto();
		entityAccount.setAccount(account.getId());
		entityAccount.setEntity(entity.getId());
		entityAccount.setOwnership(true);
		entityAccount = (EntityAccountDto) getEntityAccountService().save(entityAccount);

		String entityIdentification = entity.getId().toString();
		if (entity instanceof Codeable) {
			entityIdentification = ((Codeable) entity).getCode();
		}

		// Identity account Created
		addToItemLog(logItem,
				MessageFormat.format(
						"Entity account relation  with id ({0}), between account ({1}) and identity ({2}) was created",
						entityAccount.getId(), uid, entityIdentification));
		logItem.setDisplayName(entityIdentification);
		logItem.setType(entityAccount.getClass().getSimpleName());
		logItem.setIdentification(entityAccount.getId().toString());

		if (callProvisioning) {
			// Call provisioning for this identity
			doUpdateAccountByEntity(entity, entityType, logItem);
		}
	}
	
	protected String getDisplayNameForEntity(AbstractEntity entity){
		if(entity == null){
			return null;
		}
		if(entity instanceof Codeable){
			return ((Codeable)entity).getCode();
		}
		return entity.getId().toString();
	} 

	@SuppressWarnings("unchecked")
	protected void doDeleteEntity(AccAccount account, SystemEntityType entityType, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {
		UUID entity = this.getEntityByAccount(account.getId());
		if (entity == null) {
			addToItemLog(logItem, "Entity account relation (with ownership = true) was not found!");
			initSyncActionLog(SynchronizationActionType.DELETE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return;
		}
		BaseDto dto = getEntityService().get(entity);
		String entityIdentification = dto.getId().toString();
		if (dto instanceof Codeable) {
			entityIdentification = ((Codeable) dto).getCode();
		}
		logItem.setDisplayName(entityIdentification);
		// Delete entity
		getEntityService().delete(dto);
	}
	
	protected abstract List<? extends AbstractEntity> findAllEntity();

	@Override
	public void setLongRunningTaskExecutor(AbstractLongRunningTaskExecutor<SysSyncConfig> longRunningTaskExecutor) {
		this.longRunningTaskExecutor = longRunningTaskExecutor;
	}
	
	protected SysSystemMapping getSystemMapping(List<SysSystemAttributeMapping> attributes){
		if(attributes == null || attributes.isEmpty()){
			return null;
		}
		return attributes.get(0).getSystemMapping();
	}
	
	protected SysSystemAttributeMapping getAttributeByIdmProperty(String idmProperty, List<SysSystemAttributeMapping> mappedAttributes) {
		Optional<SysSystemAttributeMapping> optional = mappedAttributes.stream().filter(attribute -> {
			return !attribute.isDisabledAttribute() && attribute.isEntityAttribute() && idmProperty.equals(attribute.getIdmPropertyName());
		}).findFirst();
		
		if(!optional.isPresent()){
			return null;
		}
		return optional.get();
	}
	
	protected abstract EntityAccountFilter createEntityAccountFilter();

	protected abstract EntityAccountDto createEntityAccountDto();

	@SuppressWarnings("rawtypes")
	protected abstract ReadWriteDtoService getEntityAccountService();

	@SuppressWarnings("rawtypes")
	protected abstract ReadWriteDtoService getEntityService();
	
	/**
	 * Default implementation of {@link IcResultsHandler}
	 * @author svandav
	 *
	 */
	private class DefaultResultHandler implements IcResultsHandler{
		
		private SynchronizationContext context;
		private List<String> systemAccountsList;
		
		public DefaultResultHandler(final SynchronizationContext context, final List<String> systemAccountsList) {
			this.context = context;
			this.systemAccountsList = systemAccountsList;
		}
		
		@Override
		public boolean handle(IcConnectorObject connectorObject) {
			Assert.notNull(connectorObject);
			Assert.notNull(connectorObject.getUidValue());
			String uid = connectorObject.getUidValue();

			if (context.getConfig().isReconciliation()) {
				systemAccountsList.add(uid);
			}

			SynchronizationContext itemContext = SynchronizationContext.cloneContext(context);
			itemContext
			.addUid(uid)
			.addIcObject(connectorObject);
				
			return handleIcObject(itemContext);
		}
	}
	
	/**
	 * Default implementation of {@link IcSyncResultsHandler}
	 * @author svandav
	 *
	 */
	private class DefalutSyncResultHandler implements IcSyncResultsHandler{
		
		private SynchronizationContext context;
		private List<String> systemAccountsList;
		
		public DefalutSyncResultHandler(final SynchronizationContext context, final List<String> systemAccountsList) {
			this.context = context;
			this.systemAccountsList = systemAccountsList;
		}
		
		
		@Override
		public boolean handle(IcSyncDelta delta) {
			SysSyncLog log = context.getLog();
			SysSyncConfig config = context.getConfig();
			SysSyncItemLog itemLog = new SysSyncItemLog();
			
			Assert.notNull(delta);
			Assert.notNull(delta.getUid());
			String uid = delta.getUid().getUidValue();
			IcSyncDeltaTypeEnum type = delta.getDeltaType();
			IcConnectorObject icObject = delta.getObject();
			IcSyncToken token = delta.getToken();
			String tokenObject = token.getValue() != null ? token.getValue().toString() : null;
			// Save token
			log.setToken(tokenObject);
			config.setToken(tokenObject);
			//
			if (config.isReconciliation()) {
				systemAccountsList.add(uid);
			}

			SynchronizationContext itemContext = SynchronizationContext.cloneContext(context);
			itemContext
			.addUid(uid)
			.addLogItem(itemLog)
			.addType(type)
			.addIcObject(icObject);

			boolean result = startItemSynchronization(itemContext);

			// We reload log (maybe was synchronization canceled)
			log.setRunning(synchronizationLogService.get(log.getId()).isRunning());
			longRunningTaskExecutor.increaseCounter();
			if (!log.isRunning()) {
				result = false;
			}
			if (!result) {
				log.setRunning(false);
				log.addToLog(MessageFormat.format("Synchronization canceled during resolve UID [{0}]", uid));
				addToItemLog(itemLog, "Canceled!");
				initSyncActionLog(SynchronizationActionType.IGNORE, OperationResultType.WARNING, itemLog, log,
						itemContext.getActionLogs());
			}
			return result;

		}
	}
	

}
