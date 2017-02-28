package eu.bcvsolutions.idm.acc.service.impl;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationEventType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationItemWrapper;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationSituationType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.filter.AccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SyncActionLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
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
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
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
 * Service for do synchronization and reconciliation
 * @author svandav
 *
 */
@Service
public class DefaultSynchronizationService extends AbstractLongRunningTaskExecutor<SysSyncConfig> implements SynchronizationService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSynchronizationService.class);
	private final IdmIdentityService identityService;
	private final IdmIdentityRoleService identityRoleService;
	private final WorkflowProcessInstanceService workflowProcessInstanceService;
	private final IcConnectorFacade connectorFacade;
	private final SysSystemService systemService;
	private final SysSystemAttributeMappingService attributeHandlingService;
	private final SysSyncConfigService synchronizationConfigService;
	private final SysSyncLogService synchronizationLogService;
	private final SysSyncItemLogService syncItemLogService;
	private final SysSyncActionLogService syncActionLogService;
	private final SysSystemEntityService systemEntityService;
	private final AccAccountService accountService;
	private final AccIdentityAccountService identityAccoutnService;
	private final GroovyScriptService groovyScriptService;
	private final ConfidentialStorage confidentialStorage;
	private final FormService formService;
	private final EntityEventManager entityEventManager;
	private final EntityManager entityManager;
	private final LongRunningTaskManager longRunningTaskManager;
	//
	private UUID synchronizationConfigId = null;

	@Autowired
	public DefaultSynchronizationService(IcConnectorFacade connectorFacade, SysSystemService systemService,
			SysSystemAttributeMappingService attributeHandlingService,
			SysSyncConfigService synchronizationConfigService,
			SysSyncLogService synchronizationLogService, SysSyncActionLogService syncActionLogService,
			AccAccountService accountService, SysSystemEntityService systemEntityService,
			ConfidentialStorage confidentialStorage, FormService formService, IdmIdentityService identityService,
			AccIdentityAccountService identityAccoutnService, SysSyncItemLogService syncItemLogService,
			IdmIdentityRoleService identityRoleService, EntityEventManager entityEventManager,
			GroovyScriptService groovyScriptService, WorkflowProcessInstanceService workflowProcessInstanceService,
			EntityManager entityManager,
			LongRunningTaskManager longRunningTaskManager) {
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
		Assert.notNull(identityService);
		Assert.notNull(identityAccoutnService);
		Assert.notNull(syncItemLogService);
		Assert.notNull(identityRoleService);
		Assert.notNull(entityEventManager);
		Assert.notNull(groovyScriptService);
		Assert.notNull(workflowProcessInstanceService);
		Assert.notNull(entityManager);
		Assert.notNull(longRunningTaskManager);
		//
		this.connectorFacade = connectorFacade;
		this.systemService = systemService;
		this.attributeHandlingService = attributeHandlingService;
		this.synchronizationConfigService = synchronizationConfigService;
		this.synchronizationLogService = synchronizationLogService;
		this.accountService = accountService;
		this.systemEntityService = systemEntityService;
		this.confidentialStorage = confidentialStorage;
		this.formService = formService;
		this.identityService = identityService;
		this.identityAccoutnService = identityAccoutnService;
		this.syncItemLogService = syncItemLogService;
		this.identityRoleService = identityRoleService;
		this.entityEventManager = entityEventManager;
		this.groovyScriptService = groovyScriptService;
		this.workflowProcessInstanceService = workflowProcessInstanceService;
		this.entityManager = entityManager;
		this.syncActionLogService = syncActionLogService;
		this.longRunningTaskManager = longRunningTaskManager;
	}
	
	@Override
	public SysSyncConfig startSynchronizationEvent(SysSyncConfig config) {
		CoreEvent<SysSyncConfig> event = new CoreEvent<SysSyncConfig>(SynchronizationEventType.START, config);
		return (SysSyncConfig) entityEventManager.process(event).getContent(); 
	}
	
	/**
	 * Prepare and execute long running task
	 */
	@Override
	@Transactional(propagation = Propagation.NEVER)
	public void startSynchronization(SysSyncConfig config) {
		DefaultSynchronizationService taskExecutor = new DefaultSynchronizationService(connectorFacade, systemService, attributeHandlingService, synchronizationConfigService, synchronizationLogService, syncActionLogService, accountService, systemEntityService, confidentialStorage, formService, identityService, identityAccoutnService, syncItemLogService, identityRoleService, entityEventManager, groovyScriptService, workflowProcessInstanceService, entityManager, longRunningTaskManager);
		taskExecutor.synchronizationConfigId = config.getId();
		longRunningTaskManager.execute(taskExecutor);
	}
	
	/**
	 * Add transactional only - public method called from long running task manager
	 */
	@Override
	@Transactional(propagation = Propagation.NEVER)
	public SysSyncConfig call() {
		return super.call();
	}
		
	@Override
	public String getDescription() {
		SysSyncConfig config = synchronizationConfigService.get(synchronizationConfigId);
		if (config == null) {
			return "Synchronization long running task";
		}
		return MessageFormat.format("Run synchronization [{0}] - [{1}]", config.getName(), config.getSystemMapping().getName());
	}

	/**
	 * Called from long running task
	 */
	@Override
	public SysSyncConfig process() {
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
		List<SysSystemAttributeMapping> mappedAttributes = attributeHandlingService.find(attributeHandlingFilter, null)
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
		IcSyncToken lastIcToken = lastToken != null ? new IcSyncTokenImpl(lastToken) : null;

		// Create basic synchronization log
		SysSyncLog log = new SysSyncLog();
		log.setSynchronizationConfig(config);
		log.setStarted(LocalDateTime.now());
		log.setRunning(true);
		log.setToken(lastToken != null ? lastToken.toString() : null);

		log.addToLog(MessageFormat.format("Synchronization was started in {0}.", log.getStarted()));

		List<String> systemAccountsList = new ArrayList<>();
		
		counter = 0L;

		try {
			synchronizationLogService.save(log);
			List<SysSyncActionLog> actionsLog = new ArrayList<>();

			if (config.isCustomFilter() || config.isReconciliation()) {

				log.addToLog("Synchronization will use custom filter (not synchronization implemented in connector).");
				AttributeMapping tokenAttribute = config.getTokenAttribute();
				if (tokenAttribute == null && !config.isReconciliation()) {
					throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_TOKEN_ATTRIBUTE_NOT_FOUND);
				}

				IcResultsHandler resultHandler = new IcResultsHandler() {

					@Override
					public boolean handle(IcConnectorObject connectorObject) {
						SysSyncItemLog itemLog = new SysSyncItemLog();
						Assert.notNull(connectorObject);
						Assert.notNull(connectorObject.getUidValue());
						String uid = connectorObject.getUidValue();

						// Find token by token attribute
						// For Reconciliation can be token attribute null
						Object tokenObj = null;
						if (tokenAttribute != null) {
							tokenObj = getValueByMappedAttribute(tokenAttribute, connectorObject.getAttributes());
						}
						String token = tokenObj != null ? tokenObj.toString() : null;

						// In custom filter mode, we don't have token. We find
						// token in object by tokenAttribute, but
						// order of returned (searched) objects is random. We
						// have to do !!STRING!! compare and save only
						// grater token to config and log.
						if (token != null && config.getToken() != null && token.compareTo(config.getToken()) == -1) {
							token = config.getToken();
						}
						// Save token
						log.setToken(token);
						config.setToken(token);

						// Synchronization by custom filter not supported DELETE
						// event
						IcSyncDeltaTypeEnum type = IcSyncDeltaTypeEnum.CREATE_OR_UPDATE;
						boolean result =  startItemSynchronization(uid, connectorObject, type, entityType, itemLog, config, system,
								mappedAttributes, log, systemAccountsList);
						
						// We reload log (maybe was synchronization canceled) 
						counter++;
						updateState(log.isRunning());
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
						return result;

					}
				};
				
				IcFilter filter = resolveSynchronizationFilter(config);
				log.addToLog(MessageFormat.format("Start search with filter {0}.", filter != null ? filter : "NONE"));
				synchronizationLogService.save(log);

				connectorFacade.search(system.getConnectorInstance(), connectorConfig, objectClass, filter, resultHandler);
			} else {
				log.addToLog("Synchronization will use inner connector synchronization implementation.");
				IcSyncResultsHandler icSyncResultsHandler = new IcSyncResultsHandler() {

					@Override
					public boolean handle(IcSyncDelta delta) {
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
						boolean result = startItemSynchronization(uid, icObject, type, entityType, itemLog, config, system,
								mappedAttributes, log, systemAccountsList);
						
						// We reload log (maybe was synchronization canceled) 
						log.setRunning(synchronizationLogService.get(log.getId()).isRunning());
						counter++;
						updateState(log.isRunning());
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
						return result;
					}
				};

				connectorFacade.synchronization(system.getConnectorInstance(), connectorConfig, objectClass, lastIcToken,
						icSyncResultsHandler);
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
			count = counter;
			updateState(false);
		}
		return config;
	}
	
	/**
	 * Synchronization has own cancel mechanism
	 * 
	 * @param running
	 */
	public void updateState(boolean running) {
		boolean result = super.updateState();
		if (running && !result) { // synchronization was canceled from long running task agenda - we need to stop synchronization through event 
			stopSynchronizationEvent(synchronizationConfigService.get(synchronizationConfigId));
		}
	}
	
	@Override
	public SysSyncConfig stopSynchronizationEvent(SysSyncConfig config) {
		CoreEvent<SysSyncConfig> event = new CoreEvent<SysSyncConfig>(SynchronizationEventType.CANCEL, config);
		return (SysSyncConfig) entityEventManager.process(event).getContent(); 
	}
	
	@Override
	public SysSyncConfig stopSynchronization(SysSyncConfig config){
		Assert.notNull(config);
		// Synchronization must be running
		SynchronizationLogFilter logFilter = new SynchronizationLogFilter();
		logFilter.setSynchronizationConfigId(config.getId());
		logFilter.setRunning(Boolean.TRUE);
		List<SysSyncLog> logs  = synchronizationLogService.find(logFilter, null).getContent();
		
		if (logs.isEmpty()) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_IS_NOT_RUNNING,
					ImmutableMap.of("name", config.getName()));
		}
		
		logs.forEach(log -> {
			log.setRunning(false);
		});
		synchronizationLogService.saveAll(logs);
		return config;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public boolean doItemSynchronization(SynchronizationItemWrapper wrapper) {
		Assert.notNull(wrapper);
		
		String uid = wrapper.getUid();
		IcConnectorObject icObject = wrapper.getIcObject();
		IcSyncDeltaTypeEnum type = wrapper.getType();
		SysSyncConfig config = wrapper.getConfig();
		SysSystem system = wrapper.getSystem();
		SystemEntityType entityType = wrapper.getEntityType();
		List<SysSystemAttributeMapping> mappedAttributes = wrapper.getMappedAttributes();
		AccAccount account = wrapper.getAccount();
		SysSyncLog log = wrapper.getLog();
		SysSyncItemLog logItem = wrapper.getLogItem();
		List<SysSyncActionLog> actionLogs = wrapper.getActionLogs();
		
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

			if (IcSyncDeltaTypeEnum.CREATE == type || IcSyncDeltaTypeEnum.UPDATE == type
					|| IcSyncDeltaTypeEnum.CREATE_OR_UPDATE == type) {
				// Update or create
				Assert.notNull(icObject);
				List<IcAttribute> icAttributes = icObject.getAttributes();

				if (account == null) {
					// Account not exist in IDM
					addToItemLog(logItem, "Account not exist in IDM");
					AbstractEntity entity = findEntityByCorrelationAttribute(config.getCorrelationAttribute(),
							entityType, icAttributes);
					if (entity != null) {
						// Account not exist but, entity by correlation was
						// found (UNLINKED)
						actionType = config.getUnlinkedAction().getAction();
						SynchronizationSituationType situation = SynchronizationSituationType.UNLINKED;
						if (StringUtils.hasLength(config.getUnlinkedActionWfKey())) {
							SynchronizationUnlinkedActionType unlinkedActionType = config.getUnlinkedAction();
							SynchronizationActionType action = unlinkedActionType.getAction();

							// We will start specific workflow
							startWorkflow(config.getUnlinkedActionWfKey(), uid, situation, action, icAttributes, entity,
									null, entityType, config, log, logItem, actionLogs);

						} else {
							resolveUnlinkedSituation(uid, entity, entityType, systemEntity, config.getUnlinkedAction(),
									system, log, logItem, actionLogs);
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
							startWorkflow(config.getMissingEntityActionWfKey(), uid, situation, action, icAttributes,
									null, null, entityType, config, log, logItem, actionLogs);

						} else {
							resolveMissingEntitySituation(uid, entityType, mappedAttributes, system,
									config.getMissingEntityAction(), log, logItem, actionLogs, icAttributes);
						}
					}

				} else {
					// Account exist in IdM (LINKED)
					actionType = config.getLinkedAction().getAction();
					SynchronizationSituationType situation = SynchronizationSituationType.LINKED;
					if (StringUtils.hasLength(config.getLinkedActionWfKey())) {
						SynchronizationLinkedActionType linkedAction = config.getLinkedAction();
						SynchronizationActionType action = linkedAction.getAction();

						// We will start specific workflow
						startWorkflow(config.getLinkedActionWfKey(), uid, situation, action, icAttributes, null,
								account, entityType, config, log, logItem, actionLogs);

					} else {
						resolveLinkedSituation(uid, entityType, icAttributes, mappedAttributes, account,
								config.getLinkedAction(), log, logItem, actionLogs);
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
					startWorkflow(config.getMissingAccountActionWfKey(), account.getRealUid(), situation, action, null,
							null, account, entityType, config, log, logItem, actionLogs);

				} else {
					// Resolve missing account situation for one item
					this.resolveMissingAccountSituation(account.getRealUid(), account, entityType,
							config.getMissingAccountAction(), system, log, logItem, actionLogs);
				}
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

	@Override
	public SysSyncItemLog resolveMissingEntitySituation(String uid, SystemEntityType entityType,
			List<IcAttribute> icAttributes, UUID configId, String actionType) {
		Assert.notNull(uid);
		Assert.notNull(entityType);
		Assert.notNull(icAttributes);
		Assert.notNull(configId);
		Assert.notNull(actionType);

		SysSyncConfig config = synchronizationConfigService.get(configId);
		SysSystemMapping mapping = config.getSystemMapping();
		SysSystem system = mapping.getSystem();

		SystemAttributeMappingFilter attributeHandlingFilter = new SystemAttributeMappingFilter();
		attributeHandlingFilter.setSystemMappingId(mapping.getId());
		List<SysSystemAttributeMapping> mappedAttributes = attributeHandlingService.find(attributeHandlingFilter, null)
				.getContent();
		SysSyncItemLog itemLog = new SysSyncItemLog();
		this.resolveMissingEntitySituation(uid, entityType, mappedAttributes, system,
				SynchronizationMissingEntityActionType.valueOf(actionType), null, itemLog, null, icAttributes);
		return itemLog;

	}

	@Override
	public SysSyncItemLog resolveLinkedSituation(String uid, SystemEntityType entityType,
			List<IcAttribute> icAttributes, UUID accountId, UUID configId, String actionType) {
		Assert.notNull(uid);
		Assert.notNull(entityType);
		Assert.notNull(icAttributes);
		Assert.notNull(configId);
		Assert.notNull(actionType);
		Assert.notNull(accountId);

		SysSyncItemLog itemLog = new SysSyncItemLog();

		SysSyncConfig config = synchronizationConfigService.get(configId);
		SysSystemMapping mapping = config.getSystemMapping();
		AccAccount account = accountService.get(accountId);

		SystemAttributeMappingFilter attributeHandlingFilter = new SystemAttributeMappingFilter();
		attributeHandlingFilter.setSystemMappingId(mapping.getId());
		List<SysSystemAttributeMapping> mappedAttributes = attributeHandlingService.find(attributeHandlingFilter, null)
				.getContent();

		this.resolveLinkedSituation(uid, entityType, icAttributes, mappedAttributes, account,
				SynchronizationLinkedActionType.valueOf(actionType), null, itemLog, null);
		return itemLog;
	}

	@Override
	public SysSyncItemLog resolveUnlinkedSituation(String uid, SystemEntityType entityType, UUID entityId,
			UUID configId, String actionType) {
		Assert.notNull(uid);
		Assert.notNull(entityType);
		Assert.notNull(configId);
		Assert.notNull(actionType);
		Assert.notNull(entityId);

		SysSyncConfig config = synchronizationConfigService.get(configId);
		SysSystemMapping mapping = config.getSystemMapping();
		AbstractEntity entity = null;
		if (SystemEntityType.IDENTITY == entityType) {
			entity = identityService.get(entityId);
		} else {
			throw new UnsupportedOperationException(
					MessageFormat.format("SystemEntityType {0} is not supported!", entityType));
		}
		SysSystem system = mapping.getSystem();
		SysSystemEntity systemEntity = findSystemEntity(uid, system, entityType);
		SysSyncItemLog itemLog = new SysSyncItemLog();

		this.resolveUnlinkedSituation(uid, entity, entityType, systemEntity,
				SynchronizationUnlinkedActionType.valueOf(actionType), system, null, itemLog, null);
		return itemLog;
	}

	@Override
	public SysSyncItemLog resolveMissingAccountSituation(String uid, SystemEntityType entityType, UUID accountId,
			UUID configId, String actionType) {
		Assert.notNull(uid);
		Assert.notNull(entityType);
		Assert.notNull(configId);
		Assert.notNull(actionType);
		Assert.notNull(accountId);

		SysSyncConfig config = synchronizationConfigService.get(configId);
		SysSystemMapping mapping = config.getSystemMapping();
		AccAccount account = accountService.get(accountId);
		SysSystem system = mapping.getSystem();
		SysSyncItemLog itemLog = new SysSyncItemLog();

		this.resolveMissingAccountSituation(uid, account, entityType,
				ReconciliationMissingAccountActionType.valueOf(actionType), system, null, itemLog, null);
		return itemLog;
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
	 * @param systemAccountsList
	 * @param actionsLog
	 * @return
	 */
	private boolean startItemSynchronization(String uid, IcConnectorObject icObject, IcSyncDeltaTypeEnum type,
			SystemEntityType entityType, SysSyncItemLog itemLog, SysSyncConfig config, SysSystem system,
			List<SysSystemAttributeMapping> mappedAttributes, SysSyncLog log,
			List<String> systemAccountsList) {

		List<SysSyncActionLog> actionsLog = new ArrayList<>();
		try {
			if (config.isReconciliation()) {
				systemAccountsList.add(uid);
			}

			SyncActionLogFilter actionFilter = new SyncActionLogFilter();
			actionFilter.setSynchronizationLogId(log.getId());
			actionsLog.addAll(syncActionLogService.find(actionFilter, null).getContent());

			// Default setting for log item
			itemLog.setIdentification(uid);
			itemLog.setDisplayName(uid);
			itemLog.setType(entityType.getEntityType().getSimpleName());

			// Do synchronization for one item (produces item)
			// Start in new Transaction
			SynchronizationItemWrapper itemWrapper = new SynchronizationItemWrapper(uid, icObject, type, config, system,
					entityType, mappedAttributes, null, log, itemLog, actionsLog);
			
			CoreEvent<SysSyncItemLog> event = new CoreEvent<SysSyncItemLog>(SynchronizationEventType.START_ITEM, itemLog);
			event.getProperties().put(SynchronizationService.WRAPPER_SYNC_ITEM, itemWrapper);
			EventResult<SysSyncItemLog> lastResult = entityEventManager.process(event).getLastResult(); 
			boolean result = false;
			if(lastResult != null && lastResult.getEvent().getProperties().containsKey(SynchronizationService.RESULT_SYNC_ITEM)){
				result =  (boolean) lastResult.getEvent().getProperties().get(SynchronizationService.RESULT_SYNC_ITEM);
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
			//synchronizationLogService.save(log);
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
	private void startReconciliation(SystemEntityType entityType, List<String> systemAccountsList,
			SysSyncConfig config, SysSystem system, SysSyncLog log,
			List<SysSyncActionLog> actionsLog) {
		AccountFilter accountFilter = new AccountFilter();
		accountFilter.setSystemId(system.getId());
		List<AccAccount> accounts = accountService.find(accountFilter, null).getContent();

		for (AccAccount account : accounts) {
			if (!log.isRunning()) {
				return;
			}
			String uid = account.getRealUid();
			if (!systemAccountsList.contains(uid)) {
				SysSyncItemLog itemLog = new SysSyncItemLog();
				try {

					// Default setting for log item
					itemLog.setIdentification(uid);
					itemLog.setDisplayName(uid);
					itemLog.setType(entityType.getEntityType().getSimpleName());

					// Do reconciliation for one item (produces event)
					// Start in new Transaction
					SynchronizationItemWrapper itemWrapper = new SynchronizationItemWrapper(uid, null,
							IcSyncDeltaTypeEnum.DELETE, config, system, entityType, null, account, log, itemLog,
							actionsLog);
					CoreEvent<SysSyncItemLog> event = new CoreEvent<SysSyncItemLog>(SynchronizationEventType.START_ITEM, itemLog);
					event.getProperties().put(SynchronizationService.WRAPPER_SYNC_ITEM, itemWrapper);
					EventResult<SysSyncItemLog> lastResult = entityEventManager.process(event).getLastResult(); 
					boolean result = false;
					if(lastResult != null && lastResult.getEvent().getProperties().containsKey(SynchronizationService.RESULT_SYNC_ITEM)){
						result =  (boolean) lastResult.getEvent().getProperties().get(SynchronizationService.RESULT_SYNC_ITEM);
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
	 * Compile filter for search from filter attribute and filter script
	 * @param config
	 * @return
	 */
	private IcFilter resolveSynchronizationFilter(SysSyncConfig config) {
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
			Object transformedValue = attributeHandlingService.transformValueToResource(configToken,
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
	private void resolveLinkedSituation(String uid, SystemEntityType entityType, List<IcAttribute> icAttributes,
			List<SysSystemAttributeMapping> mappedAttributes, AccAccount account,
			SynchronizationLinkedActionType action, SysSyncLog log, SysSyncItemLog logItem,
			List<SysSyncActionLog> actionLogs) {

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
			doUpdateEntity(account, entityType, uid, icAttributes, mappedAttributes, log, logItem, actionLogs);
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
	private void resolveMissingEntitySituation(String uid, SystemEntityType entityType,
			List<SysSystemAttributeMapping> mappedAttributes, SysSystem system,
			SynchronizationMissingEntityActionType actionType, SysSyncLog log, SysSyncItemLog logItem,
			List<SysSyncActionLog> actionLogs, List<IcAttribute> icAttributes) {
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
			doCreateEntity(entityType, mappedAttributes, logItem, uid, icAttributes, account);
			initSyncActionLog(SynchronizationActionType.CREATE_ENTITY, OperationResultType.SUCCESS, logItem, log,
					actionLogs);
			return;
		}
	}

	/**
	 * Method for resolve unlinked situation for one item.
	 */
	private void resolveUnlinkedSituation(String uid, AbstractEntity entity, SystemEntityType entityType,
			SysSystemEntity systemEntity, SynchronizationUnlinkedActionType action, SysSystem system,
			SysSyncLog log, SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {
		addToItemLog(logItem, "Account not exist but, entity by correlation was found (entity unlinked).");
		addToItemLog(logItem, MessageFormat.format("Unlinked action is {0}", action));
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

	/**
	 * Method for resolve missing account situation for one item.
	 */
	private void resolveMissingAccountSituation(String uid, AccAccount account, SystemEntityType entityType,
			ReconciliationMissingAccountActionType action, SysSystem system, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {
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
	private void doUpdateAccount(AccAccount account, SystemEntityType entityType, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {
		if (SystemEntityType.IDENTITY == entityType) {
			IdmIdentity identity = getIdentityByAccount(account);
			if (identity == null) {
				addToItemLog(logItem, "Identity account relation (with ownership = true) was not found!");
				initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
						actionLogs);
				return;
			}
			// Call provisioning for this entity
			doUpdateAccountByEntity(identity, entityType, logItem);
		} else if (SystemEntityType.GROUP == entityType) {
			// TODO: group
		}
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
	private void doDeleteEntity(AccAccount account, SystemEntityType entityType, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {
		if (SystemEntityType.IDENTITY == entityType) {
			IdmIdentity identity = getIdentityByAccount(account);
			if (identity == null) {
				addToItemLog(logItem, "Identity account relation (with ownership = true) was not found!");
				initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
						actionLogs);
				return;
			}
			// Delete identity
			identityService.delete(identity);
		} else if (SystemEntityType.GROUP == entityType) {
			// TODO: group
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
	private void doCreateLink(String uid, boolean callProvisioning, AbstractEntity entity, SysSystemEntity systemEntity,
			SystemEntityType entityType, SysSystem system, SysSyncItemLog logItem) {
		AccAccount account = doCreateIdmAccount(uid, system);
		if (systemEntity != null) {
			// If SystemEntity for this account already exist, then we linked
			// him to new account
			account.setSystemEntity(systemEntity);
		}

		accountService.save(account);
		addToItemLog(logItem,
				MessageFormat.format("Account with uid {0} and id {1} was created", uid, account.getId()));
		if (SystemEntityType.IDENTITY == entityType) {
			IdmIdentity identity = (IdmIdentity) entity;

			// Create new Identity account relation
			AccIdentityAccount identityAccount = new AccIdentityAccount();
			identityAccount.setAccount(account);
			identityAccount.setIdentity(identity);
			identityAccount.setOwnership(true);
			identityAccoutnService.save(identityAccount);

			// Identity account Created
			addToItemLog(logItem,
					MessageFormat.format(
							"Identity account relation  with id ({0}), between account ({1}) and identity ({2}) was created",
							uid, identity.getUsername(), identityAccount.getId()));
			logItem.setDisplayName(identity.getUsername());
			logItem.setType(AccIdentityAccount.class.getSimpleName());
			logItem.setIdentification(identityAccount.getId().toString());

			if (callProvisioning) {
				// Call provisioning for this identity
				doUpdateAccountByEntity(entity, entityType, logItem);
			}
		} else if (SystemEntityType.GROUP == entityType) {
			// TODO: group
		}
	}

	/**
	 * Call provisioning for given account
	 * 
	 * @param entity
	 * @param entityType
	 * @param logItem
	 */
	private void doUpdateAccountByEntity(AbstractEntity entity, SystemEntityType entityType, SysSyncItemLog logItem) {
		if (SystemEntityType.IDENTITY == entityType) {
			IdmIdentity identity = (IdmIdentity) entity;
			addToItemLog(logItem,
					MessageFormat.format(
							"Call provisioning (process IdentityEventType.SAVE) for identity ({0}) with username ({1}).",
							identity.getId(), identity.getUsername()));
			entityEventManager.process(new IdentityEvent(IdentityEventType.UPDATE, identity)).getContent();
		} else if (SystemEntityType.GROUP == entityType) {
			// TODO: group
		}
	}

	/**
	 * Create new instance of ACC account
	 * 
	 * @param uid
	 * @param system
	 * @return
	 */
	private AccAccount doCreateIdmAccount(String uid, SysSystem system) {
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
	private void doCreateEntity(SystemEntityType entityType, List<SysSystemAttributeMapping> mappedAttributes,
			SysSyncItemLog logItem, String uid, List<IcAttribute> icAttributes, AccAccount account) {
		if (SystemEntityType.IDENTITY == entityType) {
			// We will create new Identity
			addToItemLog(logItem, "Missing entity action is CREATE_ENTITY, we will do create new identity.");
			IdmIdentity identity = new IdmIdentity();
			// Fill Identity by mapped attribute
			identity = (IdmIdentity) fillEntity(mappedAttributes, uid, icAttributes, identity);
			// Create new Identity
			identityService.save(identity);
			// Update extended attribute (entity must be persisted first)
			updateExtendedAttributes(mappedAttributes, uid, icAttributes, identity);
			// Update confidential attribute (entity must be persisted first)
			updateConfidentialAttributes(mappedAttributes, uid, icAttributes, identity);

			// Create new Identity account relation
			AccIdentityAccount identityAccount = new AccIdentityAccount();
			identityAccount.setAccount(account);
			identityAccount.setIdentity(identity);
			identityAccount.setOwnership(true);
			identityAccoutnService.save(identityAccount);

			// Identity Created
			addToItemLog(logItem, MessageFormat.format("Identity with id {0} was created", identity.getId()));
			if (logItem != null) {
				logItem.setDisplayName(identity.getUsername());
			}
		} else if (SystemEntityType.GROUP == entityType) {
			// TODO create group
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
	private void doUpdateEntity(AccAccount account, SystemEntityType entityType, String uid,
			List<IcAttribute> icAttributes, List<SysSystemAttributeMapping> mappedAttributes, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {
		if (SystemEntityType.IDENTITY == entityType) {
			IdmIdentity identity = null;

			identity = getIdentityByAccount(account);
			if (identity != null) {
				// Update identity
				identity = (IdmIdentity) fillEntity(mappedAttributes, uid, icAttributes, identity);
				identityService.save(identity);
				// Update extended attribute (entity must be persisted first)
				updateExtendedAttributes(mappedAttributes, uid, icAttributes, identity);
				// Update confidential attribute (entity must be persisted first)
				updateConfidentialAttributes(mappedAttributes, uid, icAttributes, identity);

				// Identity Updated
				addToItemLog(logItem, MessageFormat.format("Identity with id {0} was updated", identity.getId()));
				if (logItem != null) {
					logItem.setDisplayName(identity.getUsername());
				}

				return;
			} else {
				addToItemLog(logItem, "Identity account relation (with ownership = true) was not found!");
				initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
						actionLogs);
				return;
			}

		} else if (SystemEntityType.GROUP == entityType) {
			// TODO: for groups
		}
	}

	/**
	 * Add message to logItem. Add timestamp.
	 * 
	 * @param logItem
	 * @param text
	 */
	private void addToItemLog(SysSyncItemLog logItem, String text) {
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
	 * Operation remove IdentityAccount relations and linked roles
	 * 
	 * @param account
	 * @param removeIdentityRole
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	private void doUnlink(AccAccount account, boolean removeIdentityRole, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {

		IdentityAccountFilter identityAccountFilter = new IdentityAccountFilter();
		identityAccountFilter.setAccountId(account.getId());
		List<AccIdentityAccount> identityAccounts = identityAccoutnService.find(identityAccountFilter, null)
				.getContent();
		if (identityAccounts.isEmpty()) {
			addToItemLog(logItem, "Identity account relation was not found!");
			initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return;
		}
		addToItemLog(logItem, MessageFormat.format("Identity-account relations to delete {0}", identityAccounts));

		identityAccounts.stream().forEach(identityAccount -> {
			// We will remove identity account, but without delete connected
			// account
			identityAccoutnService.delete(identityAccount, false);
			addToItemLog(logItem,
					MessageFormat.format(
							"Identity-account relation deleted (without call delete provisioning) (username: {0}, id: {1})",
							identityAccount.getIdentity().getUsername(), identityAccount.getId()));
			IdmIdentityRole identityRole = identityAccount.getIdentityRole();

			if (removeIdentityRole && identityRole != null) {
				// We will remove connected identity role
				identityRoleService.delete(identityRole);
				addToItemLog(logItem, MessageFormat.format("Identity-role relation deleted (username: {0}, id: {1})",
						identityRole.getIdentityContract().getIdentity().getUsername(), identityRole.getId()));
			}

		});
		return;
	}

	/**
	 * Find identity by account
	 * 
	 * @param account
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 * @return
	 */
	private IdmIdentity getIdentityByAccount(AccAccount account) {
		IdentityAccountFilter identityAccountFilter = new IdentityAccountFilter();
		identityAccountFilter.setAccountId(account.getId());
		identityAccountFilter.setOwnership(Boolean.TRUE);
		List<AccIdentityAccount> identityAccounts = identityAccoutnService.find(identityAccountFilter, null)
				.getContent();
		if (identityAccounts.isEmpty()) {
			return null;
		} else {
			// We assume that all identity accounts
			// (mark as
			// ownership) have same identity!
			return identityAccounts.get(0).getIdentity();
		}
	}

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
	private AbstractEntity findEntityByCorrelationAttribute(AttributeMapping attribute, SystemEntityType entityType,
			List<IcAttribute> icAttributes) {
		Assert.notNull(attribute);
		Assert.notNull(entityType);
		Assert.notNull(icAttributes);

		Object value = getValueByMappedAttribute(attribute, icAttributes);
		if (value == null) {
			return null;
		}
		if (attribute.isEntityAttribute()) {
			if (SystemEntityType.IDENTITY == entityType) {
				IdentityFilter identityFilter = new IdentityFilter();
				identityFilter.setProperty(attribute.getIdmPropertyName());
				identityFilter.setValue(value);
				List<IdmIdentity> identities = identityService.find(identityFilter, null).getContent();
				if (CollectionUtils.isEmpty(identities)) {
					return null;
				}
				if (identities.size() > 1) {
					throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_CORRELATION_TO_MANY_RESULTS,
							ImmutableMap.of("correlationAttribute", attribute.getName(), "value", value));
				}
				if (identities.size() == 1) {
					return identities.get(0);
				}
			}
		} else if (attribute.isExtendedAttribute()) {
			// TODO: not supported now

			return null;
		}
		return null;
	}

	/**
	 * Get value from given entity field
	 * 
	 * @param entity
	 * @param propertyName
	 * @param value
	 * @return
	 * @throws IntrospectionException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private Object setEntityValue(AbstractEntity entity, String propertyName, Object value)
			throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Optional<PropertyDescriptor> propertyDescriptionOptional = Arrays
				.asList(Introspector.getBeanInfo(entity.getClass()).getPropertyDescriptors()).stream()
				.filter(propertyDescriptor -> {
					return propertyName.equals(propertyDescriptor.getName());
				}).findFirst();
		if (!propertyDescriptionOptional.isPresent()) {
			throw new IllegalAccessException("Field " + propertyName + " not found!");
		}
		PropertyDescriptor propertyDescriptor = propertyDescriptionOptional.get();

		return propertyDescriptor.getWriteMethod().invoke(entity, value);
	}

	/**
	 * Fill entity with attributes from IC module (by mapped attributes).
	 * 
	 * @param mappedAttributes
	 * @param uid
	 * @param icAttributes
	 * @param entity
	 * @return
	 */
	private AbstractEntity fillEntity(List<SysSystemAttributeMapping> mappedAttributes, String uid,
			List<IcAttribute> icAttributes, AbstractEntity entity) {
		mappedAttributes.stream().filter(attribute -> {
			// Skip disabled attributes
			// Skip extended attributes (we need update/ create entity first)
			// Skip confidential attributes (we need update/ create entity
			// first)
			return !attribute.isDisabledAttribute() && attribute.isEntityAttribute()
					&& !attribute.isConfidentialAttribute();

		}).forEach(attribute -> {
			String attributeProperty = attribute.getIdmPropertyName();
			Object transformedValue = getValueByMappedAttribute(attribute, icAttributes);
			// Set transformed value from target system to identity
			try {
				setEntityValue(entity, attributeProperty, transformedValue);
			} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | ProvisioningException e) {
				throw new ProvisioningException(AccResultCode.PROVISIONING_IDM_FIELD_NOT_FOUND,
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
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private AbstractEntity updateExtendedAttributes(List<SysSystemAttributeMapping> mappedAttributes, String uid,
			List<IcAttribute> icAttributes, AbstractEntity entity) {
		mappedAttributes.stream().filter(attribute -> {
			// Skip disabled attributes
			// Only for extended attributes
			return !attribute.isDisabledAttribute() && attribute.isExtendedAttribute();

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
				formService.saveValues((FormableEntity) entity, defAttribute, (List<Serializable>) transformedValue);
			} else {
				formService.saveValues((FormableEntity) entity, defAttribute,
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
	 * @return
	 */
	private AbstractEntity updateConfidentialAttributes(List<SysSystemAttributeMapping> mappedAttributes, String uid,
			List<IcAttribute> icAttributes, AbstractEntity entity) {
		mappedAttributes.stream().filter(attribute -> {
			// Skip disabled attributes
			// Only for confidential attribute
			return !attribute.isDisabledAttribute() && attribute.isConfidentialAttribute();

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
	
	private Object getValueByMappedAttribute(AttributeMapping attribute, List<IcAttribute> icAttributes) {
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

		Object transformedValue = attributeHandlingService.transformValueFromResource(icValue, attribute, icAttributes);
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
	 * Start workflow process by wfDefinitionKey. Create input variables and put them to process.
	 * If is log variable present after process started, then will be log add to synchronization log.
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
	private void startWorkflow(String wfDefinitionKey, String uid, SynchronizationSituationType situation,
			SynchronizationActionType action, List<IcAttribute> icAttributes, AbstractEntity entity, AccAccount account,
			SystemEntityType entityType, SysSyncConfig config, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {

		addToItemLog(logItem,
				MessageFormat.format("Workflow for {0} situation was found. We will start him.", situation));

		Map<String, Object> variables = new HashMap<>();
		variables.put(SynchronizationService.WF_VARIABLE_KEY_UID, uid);
		variables.put(SynchronizationService.WF_VARIABLE_KEY_ENTITY_TYPE, entityType);
		variables.put(SynchronizationService.WF_VARIABLE_KEY_SYNC_SITUATION, situation.name());
		variables.put(SynchronizationService.WF_VARIABLE_KEY_IC_ATTRIBUTES, icAttributes);
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
	private void initSyncActionLog(SynchronizationActionType actionType, OperationResultType resultType,
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
	
	@Override
	public void setSynchronizationConfigId(UUID synchronizationConfigId) {
		this.synchronizationConfigId = synchronizationConfigId;
	}
}
