package eu.bcvsolutions.idm.acc.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;

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
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
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
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractLongRunningTaskExecutor;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.impl.IcConnectorObjectImpl;

/**
 * Service for do synchronization and reconciliation
 * @author svandav
 *
 */
@Service
public class DefaultSynchronizationService extends AbstractLongRunningTaskExecutor<SysSyncConfigDto> implements SynchronizationService {

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
	
	@Override
	public SysSyncConfigDto startSynchronizationEvent(SysSyncConfigDto config) {
		CoreEvent<SysSyncConfigDto> event = new CoreEvent<SysSyncConfigDto>(SynchronizationEventType.START, config);
		return (SysSyncConfigDto) entityEventManager.process(event).getContent(); 
	}
	
	/**
	 * Prepare and execute long running task
	 */
	@Override
	@Transactional(propagation = Propagation.NEVER)
	public void startSynchronization(SysSyncConfigDto config) {
		DefaultSynchronizationService taskExecutor = new DefaultSynchronizationService(attributeHandlingService,
				synchronizationConfigService, synchronizationLogService, accountService, systemEntityService,
				entityEventManager, longRunningTaskManager, executors, systemMappingService, systemService, schemaObjectClassService);
		taskExecutor.synchronizationConfigId = config.getId();
		longRunningTaskManager.execute(taskExecutor);
	}
	
	/**
	 * Add transactional only - public method called from long running task manager
	 */
	@Override
	@Transactional(propagation = Propagation.NEVER)
	public SysSyncConfigDto call() {
		return super.call();
	}
		
	@Override
	public String getDescription() {
		SysSyncConfigDto config = synchronizationConfigService.get(synchronizationConfigId);
		if (config == null) {
			return "Synchronization long running task";
		}
		return MessageFormat.format("Run synchronization name: [{0}] - system mapping id: [{1}]", config.getName(), config.getSystemMapping());
	}

	/**
	 * Called from long running task
	 */
	@Override
	public SysSyncConfigDto process() {
		SysSyncConfigDto config = synchronizationConfigService.get(synchronizationConfigId);
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
	public SysSyncConfigDto stopSynchronizationEvent(SysSyncConfigDto config) {
		CoreEvent<SysSyncConfigDto> event = new CoreEvent<SysSyncConfigDto>(SynchronizationEventType.CANCEL, config);
		return (SysSyncConfigDto) entityEventManager.process(event).getContent(); 
	}
	
	@Override
	public SysSyncConfigDto stopSynchronization(SysSyncConfigDto config){
		Assert.notNull(config);
		// Synchronization must be running
		SynchronizationLogFilter logFilter = new SynchronizationLogFilter();
		logFilter.setSynchronizationConfigId(config.getId());
		logFilter.setRunning(Boolean.TRUE);
		List<SysSyncLogDto> logs  = synchronizationLogService.find(logFilter, null).getContent();
		
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

		SysSyncConfigDto config = synchronizationConfigService.get(configId);
		SysSystemMappingDto mapping = systemMappingService.get(config.getSystemMapping());
		
		SysSchemaObjectClassDto sysSchemaObjectClassDto = schemaObjectClassService.get(mapping.getObjectClass());
		SysSystem system = systemService.get(sysSchemaObjectClassDto.getSystem());

		SystemAttributeMappingFilter attributeHandlingFilter = new SystemAttributeMappingFilter();
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

		SysSyncConfigDto config = synchronizationConfigService.get(configId);
		SysSystemMappingDto mapping = systemMappingService.get(config.getSystemMapping());
		AccAccountDto account = accountService.get(accountId);

		SystemAttributeMappingFilter attributeHandlingFilter = new SystemAttributeMappingFilter();
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

		SysSyncConfigDto config = synchronizationConfigService.get(configId);
		SysSystemMappingDto mapping = systemMappingService.get(config.getSystemMapping());
	
		SysSchemaObjectClassDto sysSchemaObjectClassDto = schemaObjectClassService.get(mapping.getObjectClass());
		SysSystem system = systemService.get(sysSchemaObjectClassDto.getSystem());
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

		SysSyncConfigDto config = synchronizationConfigService.get(configId);
		SysSystemMappingDto mapping = systemMappingService.get(config.getSystemMapping());
		AccAccountDto account = accountService.get(accountId);
		SysSchemaObjectClassDto sysSchemaObjectClassDto = schemaObjectClassService.get(mapping.getObjectClass());
		SysSystem system = systemService.get(sysSchemaObjectClassDto.getSystem());
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
	
	private SysSystemEntityDto findSystemEntity(String uid, SysSystem system, SystemEntityType entityType) {
		SystemEntityFilter systemEntityFilter = new SystemEntityFilter();
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
