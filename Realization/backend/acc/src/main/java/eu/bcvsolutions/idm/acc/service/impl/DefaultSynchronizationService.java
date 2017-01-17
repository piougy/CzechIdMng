package eu.bcvsolutions.idm.acc.service.impl;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccountFilter;
import eu.bcvsolutions.idm.acc.dto.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.SynchronizationLogFilter;
import eu.bcvsolutions.idm.acc.dto.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.SystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.acc.entity.SysSynchronizationConfig;
import eu.bcvsolutions.idm.acc.entity.SysSynchronizationLog;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSynchronizationConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSynchronizationLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.service.api.FormService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcSyncDelta;
import eu.bcvsolutions.idm.ic.api.IcSyncResultsHandler;
import eu.bcvsolutions.idm.ic.api.IcSyncToken;
import eu.bcvsolutions.idm.ic.impl.IcObjectClassImpl;
import eu.bcvsolutions.idm.ic.impl.IcSyncDeltaTypeEnum;
import eu.bcvsolutions.idm.ic.impl.IcSyncTokenImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

@Service
public class DefaultSynchronizationService implements SynchronizationService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSynchronizationService.class);
	private final IcConnectorFacade connectorFacade;
	private final SysSystemService systemService;
	private final SysSystemAttributeMappingService attributeHandlingService;
	private final SysSynchronizationConfigService synchronizationConfigService;
	private final SysSynchronizationLogService synchronizationLogService;
	private final AccAccountService accountService;
	private final SysSystemEntityService systemEntityService;
	private final ConfidentialStorage confidentialStorage;
	private final FormService formService;
	private final IdmIdentityService identityService;
	private final AccIdentityAccountService identityAccoutnService;
	private final IdmIdentityRoleService identityRoleService;
	private final SysSyncItemLogService syncItemLogService;
	private final EntityEventManager entityEventProcessorService;

	@Autowired(required = false)
	private ApplicationContext applicationContext;
	private SynchronizationService synchronizationService;

	@Autowired
	public DefaultSynchronizationService(IcConnectorFacade connectorFacade, SysSystemService systemService,
			SysSystemAttributeMappingService attributeHandlingService,
			SysSynchronizationConfigService synchronizationConfigService,
			SysSynchronizationLogService synchronizationLogService, AccAccountService accountService,
			SysSystemEntityService systemEntityService, ConfidentialStorage confidentialStorage,
			FormService formService, IdmIdentityService identityService,
			AccIdentityAccountService identityAccoutnService, SysSyncItemLogService syncItemLogService,
			IdmIdentityRoleService identityRoleService, EntityEventManager entityEventProcessorService) {
		Assert.notNull(connectorFacade);
		Assert.notNull(systemService);
		Assert.notNull(attributeHandlingService);
		Assert.notNull(synchronizationConfigService);
		Assert.notNull(synchronizationLogService);
		Assert.notNull(accountService);
		Assert.notNull(systemEntityService);
		Assert.notNull(confidentialStorage);
		Assert.notNull(formService);
		Assert.notNull(identityService);
		Assert.notNull(identityAccoutnService);
		Assert.notNull(syncItemLogService);
		Assert.notNull(identityRoleService);
		Assert.notNull(entityEventProcessorService);

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
		this.entityEventProcessorService = entityEventProcessorService;
	}

	@Override
	@Transactional
	public SysSynchronizationConfig synchronization(SysSynchronizationConfig config) {
		Assert.notNull(config);
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

		IcObjectClass objectClass = findObjectClass(mappedAttributes);

		Object lastToken = config.isReconciliation() ? null : config.getToken();
		IcSyncToken lastIcToken = lastToken != null ? new IcSyncTokenImpl(lastToken) : null;

		// Create basic synchronization log
		SysSynchronizationLog log = new SysSynchronizationLog();
		log.setSynchronizationConfig(config);
		log.setStarted(LocalDateTime.now());
		log.setRunning(true);
		log.setToken(lastToken != null ? lastToken.toString() : null);

		Map<String, IcConnectorObject> systemAccountsMap = new HashMap<>();

		try {
			synchronizationLogService.save(log);
			List<SysSyncActionLog> actionsLog = new ArrayList<>();
			IcSyncResultsHandler icSyncResultsHandler = new IcSyncResultsHandler() {

				@Override
				public boolean handle(IcSyncDelta delta) {
					SysSyncItemLog itemLog = new SysSyncItemLog();
					String uid = delta.getUid().getUidValue();
					try {
						Assert.notNull(delta);
						Assert.notNull(delta.getUid());
						IcSyncDeltaTypeEnum type = delta.getDeltaType();
						IcConnectorObject icObject = delta.getObject();
						if (config.isReconciliation()) {
							systemAccountsMap.put(uid, icObject);
						}

						// Default setting for log item
						itemLog.setIdentification(uid);
						itemLog.setDisplayName(uid);
						itemLog.setType(entityType.getEntityType().getSimpleName());

						// Do synchronization for one item
						boolean result = findSynchronizationService().doItemSynchronization(uid, icObject, type, config,
								system, entityType, mappedAttributes, log, itemLog, actionsLog);
						// Save token
						IcSyncToken token = delta.getToken();
						String tokenObject = token.getValue() != null ? token.getValue().toString() : null;
						log.setToken(tokenObject);
						config.setToken(tokenObject);

						if (!log.isRunning()) {
							return false;
						}
						return result;
					} catch (Exception ex) {
						if (itemLog.getSyncActionLog() != null) {
							// We have to decrement count and log as error
							itemLog.getSyncActionLog()
									.setOperationCount(itemLog.getSyncActionLog().getOperationCount() - 1);
							loggingException(itemLog.getSyncActionLog().getSyncAction(), log, itemLog, actionsLog, uid,
									ex);
						} else {
							loggingException(SynchronizationActionType.IGNORE, log, itemLog, actionsLog, uid, ex);
						}
						return true;
					} finally {
						synchronizationConfigService.save(config);
						synchronizationLogService.save(log);
						if (itemLog.getSyncActionLog() == null) {
							// Default action log (for unexpected situation)
							initMissingActionLog(uid, itemLog, log);
						}
						syncItemLogService.save(itemLog);
					}
				}
			};

			connectorFacade.synchronization(connectorKey, connectorConfig, objectClass, lastIcToken,
					icSyncResultsHandler);

			if (config.isReconciliation()) {
				AccountFilter accountFilter = new AccountFilter();
				accountFilter.setSystemId(system.getId());
				accountService.find(accountFilter, null).forEach(account -> {
					if (!log.isRunning()) {
						return;
					}
					String uid = account.getRealUid();
					if (!systemAccountsMap.containsKey(account.getRealUid())) {
						SysSyncItemLog itemLog = new SysSyncItemLog();
						try {

							// Default setting for log item
							itemLog.setIdentification(uid);
							itemLog.setDisplayName(uid);
							itemLog.setType(entityType.getEntityType().getSimpleName());

							// Resolve missing account situation for one item
							resolveMissingAccountSituation(account.getRealUid(), account, entityType, config, system,
									log, itemLog, actionsLog);

						} catch (Exception ex) {
							LOG.error(MessageFormat.format("Reconciliation - error for uid {0}", uid), ex);
						} finally {
							synchronizationConfigService.save(config);
							synchronizationLogService.save(log);
							if (itemLog.getSyncActionLog() == null) {
								// Default action log (for unexpected situation)
								initMissingActionLog(uid, itemLog, log);
							}
							syncItemLogService.save(itemLog);
						}
					}
				});
			}

			return synchronizationConfigService.save(config);

		} finally {
			log.setRunning(false);
			log.setEnded(LocalDateTime.now());
			synchronizationLogService.save(log);
		}
	}

	private void initMissingActionLog(String uid, SysSyncItemLog itemLog, SysSynchronizationLog log) {
		String message = MessageFormat.format("Missing syncActionLog for uid {0}", uid);
		LOG.warn(message);
		SysSyncActionLog actionLogDefault = new SysSyncActionLog();
		actionLogDefault.setSyncLog(log);
		actionLogDefault.setOperationCount(1);
		actionLogDefault.setOperationResult(OperationResultType.ERROR);
		actionLogDefault.setSyncAction(SynchronizationActionType.IGNORE);
		itemLog.addToLog(message);
		itemLog.setSyncActionLog(actionLogDefault);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public boolean doItemSynchronization(String uid, IcConnectorObject icObject, IcSyncDeltaTypeEnum type,
			SysSynchronizationConfig config, SysSystem system, SystemEntityType entityType,
			List<SysSystemAttributeMapping> mappedAttributes, SysSynchronizationLog log, SysSyncItemLog logItem,
			List<SysSyncActionLog> actionLogs) {
		if (IcSyncDeltaTypeEnum.CREATE == type || IcSyncDeltaTypeEnum.UPDATE == type
				|| IcSyncDeltaTypeEnum.CREATE_OR_UPDATE == type) {
			// Update or create
			Assert.notNull(icObject);
			List<IcAttribute> icAttributes = icObject.getAttributes();

			SystemEntityFilter systemEntityFilter = new SystemEntityFilter();
			systemEntityFilter.setEntityType(entityType);
			systemEntityFilter.setSystemId(system.getId());
			systemEntityFilter.setUidId(uid);
			List<SysSystemEntity> systemEntities = systemEntityService.find(systemEntityFilter, null).getContent();
			SysSystemEntity systemEntity = null;
			if (systemEntities.size() == 1) {
				systemEntity = systemEntities.get(0);
			} else if (systemEntities.size() > 1) {
				// TODO exception
			}

			AccountFilter accountFilter = new AccountFilter();
			accountFilter.setSystemId(system.getId());
			List<AccAccount> accounts = null;
			if (systemEntity != null) {
				// System entity for this uid was found. We will find account
				// for this system entity.
				logItem.addToLog(MessageFormat.format(
						"System entity for this uid ({0}) was found. We will find account for this system entity ({1})",
						uid, systemEntity.getId()));
				accountFilter.setSystemEntityId(systemEntity.getId());
				accounts = accountService.find(accountFilter, null).getContent();
			} 
			if(CollectionUtils.isEmpty(accounts)){
				// System entity was not found. We will find account by uid
				// directly.
				logItem.addToLog(MessageFormat
						.format("System entity was not found. We will find account for uid ({0}) directly", uid));
				accountFilter.setUidId(uid);
				accountFilter.setSystemEntityId(null);
				accounts = accountService.find(accountFilter, null).getContent();
			}
			if (accounts.size() > 1) {
				// TODO: exception
				return true;
			}
			if (accounts.isEmpty()) {
				// Account not exist in IDM
				logItem.addToLog("Account not exist in IDM");

				AbstractEntity entity = findEntityByCorrelationAttribute(config.getCorrelationAttribute(), entityType,
						icAttributes);
				if (entity != null) {
					try {
						// Account not exist but, entity by correlation was
						// found (ENTITY MATCHED)
						resolveUnlinkedSituation(uid, entity, entityType, systemEntity, config, system, log, logItem,
								actionLogs);
						return true;
					} catch (Exception e) {
						loggingException(SynchronizationActionType.valueOf(config.getUnlinkedAction().name()), log,
								logItem, actionLogs, uid, e);
						throw e;
					}
				} else {
					try {
						// Account not exist and entity too (UNMATCHED)
						resolveMissingEntitySituation(uid, entityType, mappedAttributes, system, config, log, logItem,
								actionLogs, icAttributes);
						return true;
					} catch (Exception e) {
						loggingException(SynchronizationActionType.valueOf(config.getMissingEntityAction().name()), log,
								logItem, actionLogs, uid, e);
						throw e;
					}
				}

			} else if (accounts.size() == 1) {
				try {
					// Account exist in IdM (LINKED)
					resolveLinkedSituation(uid, entityType, icAttributes, mappedAttributes, accounts, config, log,
							logItem, actionLogs);
					return true;
				} catch (Exception e) {
					loggingException(SynchronizationActionType.valueOf(config.getLinkedAction().name()), log, logItem,
							actionLogs, uid, e);
					throw e;
				}
			} else {
				// TODO: exception and log
			}

		} else if (IcSyncDeltaTypeEnum.DELETE == type) {
			// TODO: Delete
		}
		return true;
	}

	private void resolveLinkedSituation(String uid, SystemEntityType entityType, List<IcAttribute> icAttributes,
			List<SysSystemAttributeMapping> mappedAttributes, List<AccAccount> accounts,
			SysSynchronizationConfig config, SysSynchronizationLog log, SysSyncItemLog logItem,
			List<SysSyncActionLog> actionLogs) {

		AccAccount account = accounts.get(0);
		logItem.addToLog(MessageFormat.format("IdM Account ({0}) exist in IDM (LINKED)", account.getUid()));
		logItem.addToLog(MessageFormat.format("Linked action is {0}", config.getLinkedAction()));
		switch (config.getLinkedAction()) {
		case IGNORE:
			// Linked action is IGNORE. We will do nothing
			initSyncActionLog(SynchronizationActionType.IGNORE, OperationResultType.SUCCESS, logItem, log, actionLogs);
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

	private void doUpdateAccount(AccAccount account, SystemEntityType entityType, SysSynchronizationLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {
		if (SystemEntityType.IDENTITY == entityType) {
			IdmIdentity identity = getIdentityByAccount(account, log, logItem, actionLogs);
			if (identity == null) {
				return;
			}
			// Call provisioning for this entity
			doUpdateAccountByEntity(identity, entityType, logItem);
		} else if (SystemEntityType.GROUP == entityType) {
			// TODO: group
		}
	}

	private void resolveMissingEntitySituation(String uid, SystemEntityType entityType,
			List<SysSystemAttributeMapping> mappedAttributes, SysSystem system, SysSynchronizationConfig config,
			SysSynchronizationLog log, SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs,
			List<IcAttribute> icAttributes) {
		logItem.addToLog("Account not exist and entity too (missing entity).");
		switch (config.getMissingEntityAction()) {
		case IGNORE:
			// Ignore we will do nothing
			logItem.addToLog("Missing entity action is IGNORE, we will do nothing.");
			initSyncActionLog(SynchronizationActionType.IGNORE, OperationResultType.SUCCESS, logItem, log, actionLogs);
			return;
		case CREATE_ENTITY:
			// Create idm account
			AccAccount account = doCreateIdmAccount(uid, system);
			accountService.save(account);

			// Create new entity
			doCreateEntity(entityType, mappedAttributes, logItem, uid, icAttributes, account);
			initSyncActionLog(SynchronizationActionType.CREATE_ENTITY, OperationResultType.SUCCESS, logItem, log,
					actionLogs);
			return;
		}
	}

	private void resolveUnlinkedSituation(String uid, AbstractEntity entity, SystemEntityType entityType,
			SysSystemEntity systemEntity, SysSynchronizationConfig config, SysSystem system, SysSynchronizationLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {
		logItem.addToLog("Account not exist but, entity by correlation was found (entity unlinked).");
		logItem.addToLog(MessageFormat.format("Unlinked action is {0}", config.getUnlinkedAction()));
		switch (config.getUnlinkedAction()) {
		case IGNORE:
			// Ignore we will do nothing
			initSyncActionLog(SynchronizationActionType.IGNORE, OperationResultType.SUCCESS, logItem, log, actionLogs);
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

	private void resolveMissingAccountSituation(String uid, AccAccount account, SystemEntityType entityType,
			SysSynchronizationConfig config, SysSystem system, SysSynchronizationLog log, SysSyncItemLog logItem,
			List<SysSyncActionLog> actionLogs) {
		logItem.addToLog(
				"Account on target system not exist but, account in IdM was found (missing account situation).");
		logItem.addToLog(MessageFormat.format("Missing account action is {0}", config.getMissingAccountAction()));
		switch (config.getMissingAccountAction()) {
		case IGNORE:
			// Ignore we will do nothing
			initSyncActionLog(SynchronizationActionType.IGNORE, OperationResultType.SUCCESS, logItem, log, actionLogs);
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

	private void doDeleteEntity(AccAccount account, SystemEntityType entityType, SysSynchronizationLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {
		if (SystemEntityType.IDENTITY == entityType) {
			IdmIdentity identity = getIdentityByAccount(account, log, logItem, actionLogs);
			if (identity == null) {
				return;
			}
			// Delete identity
			identityService.delete(identity);
		} else if (SystemEntityType.GROUP == entityType) {
			// TODO: group
		}
	}

	private void doCreateLink(String uid, boolean callProvisioning, AbstractEntity entity, SysSystemEntity systemEntity,
			SystemEntityType entityType, SysSystem system, SysSyncItemLog logItem) {
		AccAccount account = doCreateIdmAccount(uid, system);
		if (systemEntity != null) {
			// If SystemEntity for this account already exist, then we linked
			// him to new account
			account.setSystemEntity(systemEntity);
		}

		accountService.save(account);
		logItem.addToLog(MessageFormat.format("Account with uid {0} and id {1} was created", uid, account.getId()));
		if (SystemEntityType.IDENTITY == entityType) {
			IdmIdentity identity = (IdmIdentity) entity;

			// Create new Identity account relation
			AccIdentityAccount identityAccount = new AccIdentityAccount();
			identityAccount.setAccount(account);
			identityAccount.setIdentity(identity);
			identityAccount.setOwnership(true);
			identityAccoutnService.save(identityAccount);

			// Identity account Created
			logItem.addToLog(MessageFormat.format(
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

	private void doUpdateAccountByEntity(AbstractEntity entity, SystemEntityType entityType, SysSyncItemLog logItem) {
		if (SystemEntityType.IDENTITY == entityType) {
			IdmIdentity identity = (IdmIdentity) entity;
			logItem.addToLog(MessageFormat.format(
					"Call provisioning (process IdentityEventType.SAVE) for identity ({0}) with username ({1}).",
					identity.getId(), identity.getUsername()));
			entityEventProcessorService.process(new IdentityEvent(IdentityEventType.SAVE, identity)).getContent();
		} else if (SystemEntityType.GROUP == entityType) {
			// TODO: group
		}
	}

	private AccAccount doCreateIdmAccount(String uid, SysSystem system) {
		AccAccount account = new AccAccount();
		account.setSystem(system);
		account.setAccountType(AccountType.PERSONAL);
		account.setUid(uid);
		return account;
	}

	private void doCreateEntity(SystemEntityType entityType, List<SysSystemAttributeMapping> mappedAttributes,
			SysSyncItemLog logItem, String uid, List<IcAttribute> icAttributes, AccAccount account) {
		if (SystemEntityType.IDENTITY == entityType) {
			// We will create new Identity
			logItem.addToLog("Missing entity action is CREATE_ENTITY, we will do create new identity.");
			IdmIdentity identity = new IdmIdentity();
			// Fill Identity by mapped attribute
			identity = (IdmIdentity) fillEntity(mappedAttributes, uid, icAttributes, identity);
			// Create new Identity
			identityService.save(identity);

			// Create new Identity account relation
			AccIdentityAccount identityAccount = new AccIdentityAccount();
			identityAccount.setAccount(account);
			identityAccount.setIdentity(identity);
			identityAccount.setOwnership(true);
			identityAccoutnService.save(identityAccount);

			// Identity Created
			logItem.addToLog(MessageFormat.format("Identity with id {0} was created", identity.getId()));
			logItem.setDisplayName(identity.getUsername());
		} else if (SystemEntityType.GROUP == entityType) {
			// TODO create group
		}
	}

	private void doUpdateEntity(AccAccount account, SystemEntityType entityType, String uid,
			List<IcAttribute> icAttributes, List<SysSystemAttributeMapping> mappedAttributes, SysSynchronizationLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {
		if (SystemEntityType.IDENTITY == entityType) {
			IdmIdentity identity = null;

			identity = getIdentityByAccount(account, log, logItem, actionLogs);
			if (identity != null) {
				// Update identity
				identity = (IdmIdentity) fillEntity(mappedAttributes, uid, icAttributes, identity);
				identityService.save(identity);

				// Identity Updated
				logItem.addToLog(MessageFormat.format("Identity with id {0} was updated", identity.getId()));
				logItem.setDisplayName(identity.getUsername());

				return;
			}

		} else if (SystemEntityType.GROUP == entityType) {
			// TODO: for groups
		}
	}

	private IdmIdentity getIdentityByAccount(AccAccount account, SysSynchronizationLog log, SysSyncItemLog logItem,
			List<SysSyncActionLog> actionLogs) {
		IdentityAccountFilter identityAccountFilter = new IdentityAccountFilter();
		identityAccountFilter.setAccountId(account.getId());
		identityAccountFilter.setOwnership(Boolean.TRUE);
		List<AccIdentityAccount> identityAccounts = identityAccoutnService.find(identityAccountFilter, null)
				.getContent();
		if (identityAccounts.isEmpty()) {
			logItem.addToLog("Identity account relation (with ownership = true) was not found!");
			initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return null;
		} else {
			// We assume that all identity accounts
			// (mark as
			// ownership) have same identity!
			return identityAccounts.get(0).getIdentity();
		}
	}

	private void loggingException(SynchronizationActionType synchronizationActionType, SysSynchronizationLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs, String uid, Exception e) {
		String message = MessageFormat.format("Synchronization - exception during {0} for UID {1}",
				synchronizationActionType, uid);
		logItem.setMessage(message);
		logItem.addToLog(Throwables.getStackTraceAsString(e));
		initSyncActionLog(synchronizationActionType, OperationResultType.ERROR, logItem, log, actionLogs);
		LOG.error(message, e);
	}

	private void doUnlink(AccAccount account, boolean removeIdentityRole, SysSynchronizationLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {

		IdentityAccountFilter identityAccountFilter = new IdentityAccountFilter();
		identityAccountFilter.setAccountId(account.getId());
		List<AccIdentityAccount> identityAccounts = identityAccoutnService.find(identityAccountFilter, null)
				.getContent();
		if (identityAccounts.isEmpty()) {
			logItem.addToLog("Identity account relation was not found!");
			initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return;
		}
		logItem.addToLog(MessageFormat.format("Identity-account relations to delete {0}", identityAccounts));

		identityAccounts.stream().forEach(identityAccount -> {
			// We will remove identity account, but without delete connected
			// account
			identityAccoutnService.delete(identityAccount, false);
			logItem.addToLog(MessageFormat.format(
					"Identity-account relation deleted (without call delete provisioning) (username: {0}, id: {1})",
					identityAccount.getIdentity().getUsername(), identityAccount.getId()));
			IdmIdentityRole identityRole = identityAccount.getIdentityRole();

			if (removeIdentityRole && identityRole != null) {
				// We will remove connected identity role
				identityRoleService.delete(identityRole);
				logItem.addToLog(MessageFormat.format("Identity-role relation deleted (username: {0}, id: {1})",
						identityRole.getIdentity().getUsername(), identityRole.getId()));
			}

		});
		return;
	}

	private void initSyncActionLog(SynchronizationActionType actionType, OperationResultType resultType,
			SysSyncItemLog logItem, SysSynchronizationLog log, List<SysSyncActionLog> actionLogs) {

		if (logItem.getSyncActionLog() != null && !(OperationResultType.ERROR == resultType)) {
			// Log is already initialized, but if is new result type ERROR, then have priority
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
	}

	/**
	 * TODO: !!!We assume that all mapped attributes have same object class!!!
	 * Find objectClass from mapped attribute
	 * 
	 * @param mappedAttributes
	 * @return
	 */
	private IcObjectClass findObjectClass(List<SysSystemAttributeMapping> mappedAttributes) {
		Assert.notNull(mappedAttributes);
		return new IcObjectClassImpl(
				mappedAttributes.get(0).getSchemaAttribute().getObjectClass().getObjectClassName());
	}

	private SynchronizationService findSynchronizationService() {
		if (this.synchronizationService == null) {
			this.synchronizationService = applicationContext.getBean(SynchronizationService.class);
		}
		return this.synchronizationService;
	}

	private AbstractEntity findEntityByCorrelationAttribute(AttributeMapping attribute, SystemEntityType entityType,
			List<IcAttribute> icAttributes) {
		Assert.notNull(attribute);
		Assert.notNull(entityType);
		Assert.notNull(icAttributes);

		if (attribute.isEntityAttribute()) {
			if (SystemEntityType.IDENTITY == entityType) {
				Object value = getValueByMappedAttribute(attribute, icAttributes);
				if (value == null) {
					return null;
				}
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

	private AbstractEntity fillEntity(List<SysSystemAttributeMapping> mappedAttributes, String uid,
			List<IcAttribute> icAttributes, AbstractEntity entity) {
		mappedAttributes.stream().filter(attribute -> {
			// Skip disabled attributes
			return !attribute.isDisabledAttribute();

		}).forEach(attribute -> {
			String attributeProperty = attribute.getIdmPropertyName();
			Object transformedValue = getValueByMappedAttribute(attribute, icAttributes);
			if (attribute.isEntityAttribute()) {
				if (attribute.isConfidentialAttribute()) {
					// If is attribute confidential, then we will set
					// value to
					// secured storage
					if (!(transformedValue == null || transformedValue instanceof GuardedString)) {
						throw new ProvisioningException(AccResultCode.CONFIDENTIAL_VALUE_IS_NOT_GUARDED_STRING,
								ImmutableMap.of("property", attributeProperty, "class",
										transformedValue.getClass().getName()));
					}

					confidentialStorage.saveGuardedString(entity, attribute.getIdmPropertyName(),
							(GuardedString) transformedValue);

				} else {
					// Set transformed value from target system to identity
					try {
						setEntityValue(entity, attributeProperty, transformedValue);
					} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException | ProvisioningException e) {
						throw new ProvisioningException(AccResultCode.PROVISIONING_IDM_FIELD_NOT_FOUND,
								ImmutableMap.of("property", attributeProperty, "uid", uid), e);
					}
				}
			} else if (attribute.isExtendedAttribute()) {
				// TODO: new method to form service to read concrete
				// attribute definition
				IdmFormAttribute defAttribute = formService.getDefinition(((FormableEntity) entity).getClass())
						.getMappedAttributeByName(attributeProperty);
				if (defAttribute == null) {
					// eav definition could be changed
					LOG.warn("Form attribute defininion [{}] was not found, skip", attributeProperty);
					// continue;
				}
				// List<AbstractFormValue<FormableEntity>> formValues =
				// formService.getValues((FormableEntity) entity,
				// defAttribute.getFormDefinition(),
				// defAttribute.getName());
				// if (formValues.isEmpty()) {
				// continue;
				// }
				//
				// if(defAttribute.isMultiple()){
				// // Multivalue extended attribute
				// List<Object> values = new ArrayList<>();
				// formValues.stream().forEachOrdered(formValue -> {
				// values.add(formValue.getValue());
				// });
				//
				// }else{
				// // Singlevalue extended attribute
				// AbstractFormValue<FormableEntity> formValue =
				// formValues.get(0);
				// if (formValue.isConfidential()) {
				// return
				// formService.getConfidentialPersistentValue(formValue);
				// }
				// return formValue.getValue();
				// }
			}
		});
		return entity;
	}

	private Object getValueByMappedAttribute(AttributeMapping attribute, List<IcAttribute> icAttributes) {
		Optional<IcAttribute> optionalIcAttribute = icAttributes.stream().filter(icAttribute -> {
			return attribute.getSchemaAttribute().getName().equals(icAttribute.getName());
		}).findFirst();
		if (!optionalIcAttribute.isPresent()) {
			return null;
		}
		IcAttribute icAttribute = optionalIcAttribute.get();
		Object icValue = null;
		if (icAttribute.isMultiValue()) {
			icValue = icAttribute.getValues();
		} else {
			icValue = icAttribute.getValue();
		}

		Object transformedValue = attributeHandlingService.transformValueFromResource(icValue, attribute, icAttributes);
		return transformedValue;
	}

}
