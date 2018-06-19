package eu.bcvsolutions.idm.acc.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationContext;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass_;
import eu.bcvsolutions.idm.acc.event.SynchronizationEventType;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.scheduler.task.impl.SynchronizationSchedulableTaskExecutor;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationEntityExecutor;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.impl.IcConnectorObjectImpl;

/**
 * Service for do synchronization and reconciliation
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSynchronizationService implements SynchronizationService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSynchronizationService.class);
	//
	private final SysSystemAttributeMappingService attributeHandlingService;
	private final SysSyncConfigService synchronizationConfigService;
	private final SysSyncLogService synchronizationLogService;
	private final SysSystemEntityService systemEntityService;
	private final AccAccountService accountService;
	private final EntityEventManager entityEventManager;
	private final PluginRegistry<SynchronizationEntityExecutor, SystemEntityType> pluginExecutors;
	private final SysSystemMappingService systemMappingService;
	private final SysSchemaObjectClassService schemaObjectClassService;
	//
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private IdmLongRunningTaskService longRunningTaskService;
	@Autowired
	private CacheManager cacheManager;

	@Autowired
	public DefaultSynchronizationService(SysSystemAttributeMappingService attributeHandlingService,
			SysSyncConfigService synchronizationConfigService, SysSyncLogService synchronizationLogService,
			AccAccountService accountService, SysSystemEntityService systemEntityService,
			EntityEventManager entityEventManager, LongRunningTaskManager longRunningTaskManager,
			List<SynchronizationEntityExecutor> executors, SysSystemMappingService systemMappingService,
			SysSystemService systemService, SysSchemaObjectClassService schemaObjectClassService) {
		Assert.notNull(attributeHandlingService);
		Assert.notNull(synchronizationConfigService);
		Assert.notNull(synchronizationLogService);
		Assert.notNull(accountService);
		Assert.notNull(systemEntityService);
		Assert.notNull(entityEventManager);
		Assert.notNull(longRunningTaskManager);
		Assert.notNull(executors);
		Assert.notNull(systemMappingService);
		Assert.notNull(systemService);
		Assert.notNull(schemaObjectClassService);
		//
		this.attributeHandlingService = attributeHandlingService;
		this.synchronizationConfigService = synchronizationConfigService;
		this.synchronizationLogService = synchronizationLogService;
		this.accountService = accountService;
		this.systemEntityService = systemEntityService;
		this.entityEventManager = entityEventManager;
		this.systemMappingService = systemMappingService;
		this.schemaObjectClassService = schemaObjectClassService;
		//
		this.pluginExecutors = OrderAwarePluginRegistry.create(executors);
	}

	/**
	 * Cancel all previously ran synchronizations
	 */
	@Override
	@Transactional
	public void init() {
		String instanceId = configurationService.getInstanceId();
		LOG.info("Cancel unprocessed synchronizations - tasks was interrupt during instance [{}] restart", instanceId);
		//
		// find all running sync on all instances
		IdmLongRunningTaskFilter lrtFilter = new IdmLongRunningTaskFilter();
		lrtFilter.setRunning(Boolean.TRUE);
		lrtFilter.setTaskType(SynchronizationSchedulableTaskExecutor.class.getCanonicalName());
		List<IdmLongRunningTaskDto> allRunningSynchronizations = longRunningTaskService.find(lrtFilter, null)
				.getContent();
		// stop logs on the same instance id
		SysSyncLogFilter logFilter = new SysSyncLogFilter();
		logFilter.setRunning(Boolean.TRUE);
		synchronizationLogService.find(logFilter, null).forEach(sync -> {
			boolean runningOnOtherInstance = allRunningSynchronizations.stream().anyMatch(lrt -> {
				return !lrt.getInstanceId().equals(instanceId) && sync.getSynchronizationConfig()
						.equals(lrt.getTaskProperties().get(PARAMETER_SYNCHRONIZATION_ID));
			});
			if (!runningOnOtherInstance) {
				String message = MessageFormat.format(
						"Cancel unprocessed synchronization [{0}] - tasks was interrupt during instance [{1}] restart",
						sync.getId(), instanceId);
				LOG.info(message);
				sync.addToLog(message);
				sync.setRunning(false);
				synchronizationLogService.save(sync);
			}
		});
	}

	@Override
	public AbstractSysSyncConfigDto startSynchronizationEvent(AbstractSysSyncConfigDto config) {
		CoreEvent<AbstractSysSyncConfigDto> event = new CoreEvent<AbstractSysSyncConfigDto>(
				SynchronizationEventType.START, config, null, null, AbstractSysSyncConfigDto.class);
		return (AbstractSysSyncConfigDto) entityEventManager.process(event).getContent();
	}

	/**
	 * Prepare and execute long running task
	 */
	@Override
	// @Transactional
	public void startSynchronization(AbstractSysSyncConfigDto config,
			AbstractSchedulableTaskExecutor<Boolean> longRunningTaskExecutor) {

		Assert.notNull(config, "Sync configuration is required!");
		Assert.notNull(config.getId(), "Id of sync configuration is required!");
		UUID syncConfigId = config.getId();
		SysSystemMappingDto mapping = systemMappingService.get(config.getSystemMapping());
		Assert.notNull(mapping);
		SystemEntityType entityType = mapping.getEntityType();

		SynchronizationEntityExecutor executor = getSyncExecutor(entityType, syncConfigId);
		executor.setLongRunningTaskExecutor(longRunningTaskExecutor);
		executor.process(config.getId());
	}

	@Override
	public AbstractSysSyncConfigDto stopSynchronizationEvent(AbstractSysSyncConfigDto config) {
		CoreEvent<AbstractSysSyncConfigDto> event = new CoreEvent<AbstractSysSyncConfigDto>(
				SynchronizationEventType.CANCEL, config, null, null, AbstractSysSyncConfigDto.class);
		return (AbstractSysSyncConfigDto) entityEventManager.process(event).getContent();
	}

	@Override
	public AbstractSysSyncConfigDto stopSynchronization(AbstractSysSyncConfigDto config) {
		Assert.notNull(config);
		// Synchronization must be running
		SysSyncLogFilter logFilter = new SysSyncLogFilter();
		logFilter.setSynchronizationConfigId(config.getId());
		logFilter.setRunning(Boolean.TRUE);
		List<SysSyncLogDto> logs = synchronizationLogService.find(logFilter, null).getContent();

		if (logs.isEmpty()) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_IS_NOT_RUNNING,
					ImmutableMap.of("name", config.getName()));
		}

		logs.forEach(log -> {
			log.setRunning(false);
			log.setEnded(LocalDateTime.now());
		});
		synchronizationLogService.saveAll(logs);
		return config;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public boolean doItemSynchronization(SynchronizationContext context) {
		Assert.notNull(context);
		return getSyncExecutor(context.getEntityType(), context.getConfig().getId()).doItemSynchronization(context);
	}

	@Override
	public SysSyncItemLogDto resolveMissingEntitySituation(String uid, SystemEntityType entityType,
			List<IcAttribute> icAttributes, UUID configId, String actionType) {
		Assert.notNull(uid);
		Assert.notNull(entityType);
		Assert.notNull(icAttributes);
		Assert.notNull(configId);
		Assert.notNull(actionType);

		AbstractSysSyncConfigDto config = synchronizationConfigService.get(configId);
		SysSystemMappingDto mapping = systemMappingService.get(config.getSystemMapping());

		SysSchemaObjectClassDto sysSchemaObjectClassDto = schemaObjectClassService.get(mapping.getObjectClass());
		SysSystemDto system = DtoUtils.getEmbedded(sysSchemaObjectClassDto, SysSchemaObjectClass_.system);

		SysSystemAttributeMappingFilter attributeHandlingFilter = new SysSystemAttributeMappingFilter();
		attributeHandlingFilter.setSystemMappingId(mapping.getId());
		List<SysSystemAttributeMappingDto> mappedAttributes = attributeHandlingService
				.find(attributeHandlingFilter, null).getContent();
		SysSyncItemLogDto itemLog = new SysSyncItemLogDto();
		// Little workaround, we have only IcAttributes ... we create IcObject manually
		IcConnectorObjectImpl icObject = new IcConnectorObjectImpl();
		icObject.setAttributes(icAttributes);
		icObject.setUidValue(uid);

		SynchronizationContext context = new SynchronizationContext();
		context.addUid(uid) //
				.addSystem(system) //
				.addConfig(config) //
				.addEntityType(entityType) //
				.addLogItem(itemLog) //
				.addMappedAttributes(mappedAttributes) //
				.addIcObject(icObject); //

		getSyncExecutor(entityType, configId)
				.resolveMissingEntitySituation(SynchronizationMissingEntityActionType.valueOf(actionType), context);
		return itemLog;

	}

	@Override
	public SysSyncItemLogDto resolveLinkedSituation(String uid, SystemEntityType entityType,
			List<IcAttribute> icAttributes, UUID accountId, UUID configId, String actionType) {
		Assert.notNull(uid);
		Assert.notNull(entityType);
		Assert.notNull(icAttributes);
		Assert.notNull(configId);
		Assert.notNull(actionType);
		Assert.notNull(accountId);

		SysSyncItemLogDto itemLog = new SysSyncItemLogDto();

		AbstractSysSyncConfigDto config = synchronizationConfigService.get(configId);
		SysSystemMappingDto mapping = systemMappingService.get(config.getSystemMapping());
		AccAccountDto account = accountService.get(accountId);

		SysSystemAttributeMappingFilter attributeHandlingFilter = new SysSystemAttributeMappingFilter();
		attributeHandlingFilter.setSystemMappingId(mapping.getId());
		List<SysSystemAttributeMappingDto> mappedAttributes = attributeHandlingService
				.find(attributeHandlingFilter, null).getContent();

		// Little workaround, we have only IcAttributes ... we create IcObject manually
		IcConnectorObjectImpl icObject = new IcConnectorObjectImpl();
		icObject.setAttributes(icAttributes);
		icObject.setUidValue(uid);

		SynchronizationContext context = new SynchronizationContext();
		context.addUid(uid) //
				.addAccount(account) //
				.addConfig(config) //
				.addEntityType(entityType) //
				.addLogItem(itemLog) //
				.addMappedAttributes(mappedAttributes) //
				.addIcObject(icObject); //

		getSyncExecutor(entityType, configId)
				.resolveLinkedSituation(SynchronizationLinkedActionType.valueOf(actionType), context);
		return itemLog;
	}

	@Override
	public SysSyncItemLogDto resolveUnlinkedSituation(String uid, SystemEntityType entityType, UUID entityId,
			UUID configId, String actionType) {
		Assert.notNull(uid);
		Assert.notNull(entityType);
		Assert.notNull(configId);
		Assert.notNull(actionType);
		Assert.notNull(entityId);

		AbstractSysSyncConfigDto config = synchronizationConfigService.get(configId);
		SysSystemMappingDto mapping = systemMappingService.get(config.getSystemMapping());

		SysSchemaObjectClassDto sysSchemaObjectClassDto = schemaObjectClassService.get(mapping.getObjectClass());
		SysSystemDto system = DtoUtils.getEmbedded(sysSchemaObjectClassDto, SysSchemaObjectClass_.system);
		SysSystemEntityDto systemEntity = findSystemEntity(uid, system, entityType);
		SysSyncItemLogDto itemLog = new SysSyncItemLogDto();

		SynchronizationContext context = new SynchronizationContext();
		context.addUid(uid) //
				.addSystem(system) //
				.addConfig(config) //
				.addEntityType(entityType) //
				.addEntityId(entityId) //
				.addSystemEntity(systemEntity); //

		getSyncExecutor(entityType, configId)
				.resolveUnlinkedSituation(SynchronizationUnlinkedActionType.valueOf(actionType), context);
		return itemLog;
	}

	@Override
	public SysSyncItemLogDto resolveMissingAccountSituation(String uid, SystemEntityType entityType, UUID accountId,
			UUID configId, String actionType) {
		Assert.notNull(uid);
		Assert.notNull(entityType);
		Assert.notNull(configId);
		Assert.notNull(actionType);
		Assert.notNull(accountId);

		AbstractSysSyncConfigDto config = synchronizationConfigService.get(configId);
		SysSystemMappingDto mapping = systemMappingService.get(config.getSystemMapping());
		AccAccountDto account = accountService.get(accountId);
		SysSchemaObjectClassDto sysSchemaObjectClassDto = schemaObjectClassService.get(mapping.getObjectClass());
		SysSystemDto system = DtoUtils.getEmbedded(sysSchemaObjectClassDto, SysSchemaObjectClass_.system);
		SysSyncItemLogDto itemLog = new SysSyncItemLogDto();

		SynchronizationContext context = new SynchronizationContext();
		context.addUid(uid) //
				.addSystem(system) //
				.addConfig(config) //
				.addEntityType(entityType) //
				.addAccount(account) //
				.addLogItem(itemLog); //

		getSyncExecutor(entityType, configId)
				.resolveMissingAccountSituation(ReconciliationMissingAccountActionType.valueOf(actionType), context);
		return itemLog;
	}

	private SysSystemEntityDto findSystemEntity(String uid, SysSystemDto system, SystemEntityType entityType) {
		SysSystemEntityFilter systemEntityFilter = new SysSystemEntityFilter();
		systemEntityFilter.setEntityType(entityType);
		systemEntityFilter.setSystemId(system.getId());
		systemEntityFilter.setUid(uid);
		List<SysSystemEntityDto> systemEntities = systemEntityService.find(systemEntityFilter, null).getContent();
		SysSystemEntityDto systemEntity = null;
		if (systemEntities.size() == 1) {
			systemEntity = systemEntities.get(0);
		} else if (systemEntities.size() > 1) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_TO_MANY_SYSTEM_ENTITY, uid);
		}
		return systemEntity;
	}

	@Override
	public SynchronizationEntityExecutor getSyncExecutor(SystemEntityType entityType, UUID syncConfigId) {
		ValueWrapper value = this.getCachedValue(syncConfigId);
		if (value != null) {
			return (SynchronizationEntityExecutor) value.get();
		}

		SynchronizationEntityExecutor executor = pluginExecutors.getPluginFor(entityType);
		if (executor == null) {
			throw new UnsupportedOperationException(MessageFormat
					.format("Synchronization executor for SystemEntityType {0} is not supported!", entityType));
		}
		SynchronizationEntityExecutor prototypeExecutor = AutowireHelper.createBean(executor.getClass());
		this.setCachedValue(syncConfigId, prototypeExecutor);

		return (SynchronizationEntityExecutor) this.getCachedValue(syncConfigId).get();
	}

	private ValueWrapper getCachedValue(UUID authorityId) {
		Cache cache = getCache();
		if (cache == null) {
			return null;
		}
		return cache.get(authorityId);
	}

	private void setCachedValue(UUID syncConfigId, SynchronizationEntityExecutor executor) {
		Cache cache = getCache();
		if (cache == null) {
			return;
		}
		cache.put(syncConfigId, executor);
	}

	private Cache getCache() {
		if (cacheManager == null) {
			return null;
		}
		return cacheManager.getCache(SYNC_EXECUTOR_CACHE_NAME);
	}
}
