package eu.bcvsolutions.idm.acc.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
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
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractLongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.impl.IcConnectorObjectImpl;

/**
 * Service for do synchronization and reconciliation
 * @author svandav
 *
 */
@Service
public class DefaultSynchronizationService extends AbstractLongRunningTaskExecutor<AbstractSysSyncConfigDto> implements SynchronizationService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSynchronizationService.class);
	//
	private final SysSystemAttributeMappingService attributeHandlingService;
	private final SysSyncConfigService synchronizationConfigService;
	private final SysSyncLogService synchronizationLogService;
	private final SysSystemEntityService systemEntityService;
	private final AccAccountService accountService;
	private final EntityEventManager entityEventManager;
	private final LongRunningTaskManager longRunningTaskManager;
	private final PluginRegistry<SynchronizationEntityExecutor, SystemEntityType> pluginExecutors; 
	private final List<SynchronizationEntityExecutor> executors; 
	private final SysSystemMappingService systemMappingService;
	private final SysSystemService systemService;
	private final SysSchemaObjectClassService schemaObjectClassService;
	//
	@Autowired private ConfigurationService configurationService;
	@Autowired private IdmLongRunningTaskService longRunningTaskService;
	//
	private UUID synchronizationConfigId = null;

	@Autowired
	public DefaultSynchronizationService(
			SysSystemAttributeMappingService attributeHandlingService,
			SysSyncConfigService synchronizationConfigService,
			SysSyncLogService synchronizationLogService,
			AccAccountService accountService, SysSystemEntityService systemEntityService,
			EntityEventManager entityEventManager,
			LongRunningTaskManager longRunningTaskManager, List<SynchronizationEntityExecutor>  executors,
			SysSystemMappingService systemMappingService,
			SysSystemService systemService,
			SysSchemaObjectClassService schemaObjectClassService) {
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
		this.longRunningTaskManager = longRunningTaskManager;
		this.executors = executors;
		this.systemMappingService = systemMappingService;
		this.systemService = systemService;
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
		lrtFilter.setTaskType(this.getName());
		List<IdmLongRunningTaskDto> allRunningSynchronizations = longRunningTaskService.find(lrtFilter, null).getContent();
		// stop logs on the same instance id
		SysSyncLogFilter logFilter = new SysSyncLogFilter();
		logFilter.setRunning(Boolean.TRUE);
		synchronizationLogService.find(logFilter, null).forEach(sync -> {
			boolean runningOnOtherInstance = allRunningSynchronizations
					.stream()
					.anyMatch(lrt -> {
						return !lrt.getInstanceId().equals(instanceId) && sync.getSynchronizationConfig().equals(lrt.getTaskProperties().get(PARAMETER_SYNCHRONIZATION_ID));
					});
			if (!runningOnOtherInstance) {
				String message = MessageFormat.format("Cancel unprocessed synchronization [{0}] - tasks was interrupt during instance [{1}] restart", sync.getId(), instanceId);
				LOG.info(message);
				sync.addToLog(message);
				sync.setRunning(false);
				synchronizationLogService.save(sync);
			}
		});
	}
	
	@Override
	public AbstractSysSyncConfigDto startSynchronizationEvent(AbstractSysSyncConfigDto config) {
		CoreEvent<AbstractSysSyncConfigDto> event = new CoreEvent<AbstractSysSyncConfigDto>(SynchronizationEventType.START, config,null, null, AbstractSysSyncConfigDto.class);
		return (AbstractSysSyncConfigDto) entityEventManager.process(event).getContent(); 
	}
	
	/**
	 * Prepare and execute long running task
	 */
	@Override
	@Transactional(propagation = Propagation.NEVER)
	public void startSynchronization(AbstractSysSyncConfigDto config) {
		DefaultSynchronizationService taskExecutor = new DefaultSynchronizationService(attributeHandlingService,
				synchronizationConfigService, synchronizationLogService, accountService, systemEntityService,
				entityEventManager, longRunningTaskManager, executors, systemMappingService, systemService, schemaObjectClassService);
		taskExecutor.synchronizationConfigId = config.getId();
		longRunningTaskManager.execute(taskExecutor);
	}
	
	/**
	 * Add @Transactional only - public method called from long running task manager
	 */
	@Override
	@Transactional(propagation = Propagation.NEVER)
	public AbstractSysSyncConfigDto call() {
		return super.call();
	}
		
	@Override
	public String getDescription() {
		AbstractSysSyncConfigDto config = synchronizationConfigService.get(synchronizationConfigId);
		if (config == null) {
			return "Synchronization long running task";
		}
		return MessageFormat.format("Run synchronization name: [{0}] - system mapping id: [{1}]", config.getName(), config.getSystemMapping());
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> params = super.getPropertyNames();
		params.add(SynchronizationService.PARAMETER_SYNCHRONIZATION_ID);
		return params;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> props = super.getProperties();
		props.put(PARAMETER_SYNCHRONIZATION_ID, synchronizationConfigId);
		//
		return props;
	}

	/**
	 * Called from long running task
	 */
	@Override
	public AbstractSysSyncConfigDto process() {
		AbstractSysSyncConfigDto config = synchronizationConfigService.get(synchronizationConfigId);
		//
		if (config == null) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_NOT_FOUND,
					ImmutableMap.of("id", synchronizationConfigId));
		}
		SysSystemMappingDto mapping = systemMappingService.get(config.getSystemMapping());
		Assert.notNull(mapping);
		SystemEntityType entityType = mapping.getEntityType();
	
		SynchronizationEntityExecutor executor =  getSyncExecutor(entityType);
		executor.setLongRunningTaskExecutor(this);
		return executor.process(synchronizationConfigId);
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
	public AbstractSysSyncConfigDto stopSynchronizationEvent(AbstractSysSyncConfigDto config) {
		CoreEvent<AbstractSysSyncConfigDto> event = new CoreEvent<AbstractSysSyncConfigDto>(SynchronizationEventType.CANCEL, config, null, null, AbstractSysSyncConfigDto.class);
		return (AbstractSysSyncConfigDto) entityEventManager.process(event).getContent(); 
	}
	
	@Override
	public AbstractSysSyncConfigDto stopSynchronization(AbstractSysSyncConfigDto config){
		Assert.notNull(config);
		// Synchronization must be running
		SysSyncLogFilter logFilter = new SysSyncLogFilter();
		logFilter.setSynchronizationConfigId(config.getId());
		logFilter.setRunning(Boolean.TRUE);
		List<SysSyncLogDto> logs  = synchronizationLogService.find(logFilter, null).getContent();
		
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
	public boolean doItemSynchronization(SynchronizationContext wrapper) {
		Assert.notNull(wrapper);
		return getSyncExecutor(wrapper.getEntityType()).doItemSynchronization(wrapper);
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
		List<SysSystemAttributeMappingDto> mappedAttributes = attributeHandlingService.find(attributeHandlingFilter, null)
				.getContent();
		SysSyncItemLogDto itemLog = new SysSyncItemLogDto();
		// Little workaround, we have only IcAttributes ... we create IcObject manually
		IcConnectorObjectImpl icObject = new IcConnectorObjectImpl();
		icObject.setAttributes(icAttributes);
		icObject.setUidValue(uid);
		
		SynchronizationContext context = new SynchronizationContext();
		context.addUid(uid)
		.addSystem(system)
		.addConfig(config)
		.addEntityType(entityType)
		.addLogItem(itemLog)
		.addMappedAttributes(mappedAttributes)
		.addIcObject(icObject);
		
		getSyncExecutor(entityType).resolveMissingEntitySituation(SynchronizationMissingEntityActionType.valueOf(actionType), context);
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
		List<SysSystemAttributeMappingDto> mappedAttributes = attributeHandlingService.find(attributeHandlingFilter, null)
				.getContent();
	
		// Little workaround, we have only IcAttributes ... we create IcObject manually
		IcConnectorObjectImpl icObject = new IcConnectorObjectImpl();
		icObject.setAttributes(icAttributes);
		icObject.setUidValue(uid);
				
		SynchronizationContext context = new SynchronizationContext();
		context.addUid(uid)
		.addAccount(account)
		.addConfig(config)
		.addEntityType(entityType)
		.addLogItem(itemLog)
		.addMappedAttributes(mappedAttributes)
		.addIcObject(icObject);

		getSyncExecutor(entityType).resolveLinkedSituation(SynchronizationLinkedActionType.valueOf(actionType), context);
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
		context.addUid(uid)
		.addSystem(system)
		.addConfig(config)
		.addEntityType(entityType)
		.addEntityId(entityId)
		.addSystemEntity(systemEntity);
		
		getSyncExecutor(entityType).resolveUnlinkedSituation(SynchronizationUnlinkedActionType.valueOf(actionType), context);
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
		context.addUid(uid)
		.addSystem(system)
		.addConfig(config)
		.addEntityType(entityType)
		.addAccount(account)
		.addLogItem(itemLog);

		getSyncExecutor(entityType).resolveMissingAccountSituation(ReconciliationMissingAccountActionType.valueOf(actionType), context);
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
	public void setSynchronizationConfigId(UUID synchronizationConfigId) {
		this.synchronizationConfigId = synchronizationConfigId;
	}
	
	/**
	 * Find executor for synchronization given entity type
	 * @param entityType
	 * @return
	 */
	private SynchronizationEntityExecutor getSyncExecutor(SystemEntityType entityType){
		
		SynchronizationEntityExecutor executor =  pluginExecutors.getPluginFor(entityType);
		if (executor == null) {
			throw new UnsupportedOperationException(
					MessageFormat.format("Synchronization executor for SystemEntityType {0} is not supported!", entityType));
		}
		return executor;
	}
}
