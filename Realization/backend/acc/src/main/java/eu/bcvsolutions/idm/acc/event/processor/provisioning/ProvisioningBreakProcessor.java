package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.SysBlockedOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakItems;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningBreakConfig_;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakRecipientService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;

/**
 * Provisioning break processor, all block logic are there.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@Description("Breaks provisioning operation.")
public class ProvisioningBreakProcessor extends AbstractEntityEventProcessor<SysProvisioningOperationDto> {

	public static final String PROCESSOR_NAME = "provisioning-break-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProvisioningBreakProcessor.class);

	private final SysSystemService systemService;
	private final SysProvisioningBreakConfigService breakConfigService;
	private final SysProvisioningBreakRecipientService breakRecipientService;
	private final NotificationManager notificationManager;
	private final SysProvisioningOperationService provisioningOperationService;

	@Autowired
	public ProvisioningBreakProcessor(SysSystemService systemService,
			SysProvisioningBreakConfigService breakConfigService,
			SysProvisioningBreakRecipientService breakRecipientService,
			NotificationManager notificationManager,
			SysProvisioningOperationService provisioningOperationService) {
		super(ProvisioningEventType.CREATE, ProvisioningEventType.UPDATE, ProvisioningEventType.DELETE);
		//
		Assert.notNull(systemService);
		Assert.notNull(breakConfigService);
		Assert.notNull(breakRecipientService);
		Assert.notNull(notificationManager);
		Assert.notNull(provisioningOperationService);
		//
		this.systemService = systemService;
		this.breakConfigService = breakConfigService;
		this.breakRecipientService = breakRecipientService;
		this.notificationManager = notificationManager;
		this.provisioningOperationService = provisioningOperationService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<SysProvisioningOperationDto> process(EntityEvent<SysProvisioningOperationDto> event) {
		SysProvisioningOperationDto provisioningOperation = event.getContent();
		ProvisioningEventType operationType = provisioningOperation.getOperationType();
		SysSystemDto system = systemService.get(provisioningOperation.getSystem());
		// system may be blocked
		boolean blocked = isSystemBlockedOperation(operationType, system);
		//
		if (blocked) {
			// system is already blocked
			provisioningOperation = blockOperation(provisioningOperation, system);
			event.setContent(provisioningOperation);
			return new DefaultEventResult<>(event, this, blocked);
		}
		//
		// try found provisioning break configuration
		SysProvisioningBreakConfigDto breakConfig = breakConfigService.getConfig(operationType, system.getId());
		if (breakConfig == null) {
			LOG.debug(
					"Provisioning break configuration for system name: [{}] and operation: [{}] not found. Global configuration will be used.",
					system.getCode(), operationType.toString());
			breakConfig = breakConfigService.getGlobalBreakConfiguration(operationType);
		}
		//
		if (breakConfig == null) {
			LOG.debug("Global configuration for provisioning break isn't found.");
			return new DefaultEventResult<>(event, this, blocked);
		}
		//
		if (breakConfig.isDisabled()) {
			LOG.debug("Provisioning break configuration id: [{}] for system name: [{}] and operation: [{}] is disabled.",
					breakConfig.getId(), system.getCode(), operationType.toString());
			// break configuration is disable continue
			return new DefaultEventResult<>(event, this, blocked);
		}
		Long currentTimeMillis = System.currentTimeMillis();
		//
		// get cache for system
		SysProvisioningBreakItems cache = breakConfigService.getCacheProcessedItems(system.getId());
		// calculate timestamp without period
		Long timestampWithoutPeriod = currentTimeMillis - breakConfig.getPeriod(TimeUnit.MILLISECONDS);
		// remove older records
		cache.removeOlderRecordsThan(operationType, timestampWithoutPeriod);
		// get actual count - processed items from timestampWithoutPeriod
		Integer actualCount = cache.getSizeRecordsNewerThan(operationType, timestampWithoutPeriod);
		//
		// add this operation to actual count, we want sum including this operation
		actualCount++;
		//
		if (breakConfig.getWarningLimit() != null && actualCount == breakConfig.getWarningLimit()) {
			// if count is equals to warning limit send notification, only when notification is equals
			// disabled may be null
			if (breakConfig.getDisableLimit() == null) {
				LOG.warn("Block for the system id [{}] and operation [{}] is not set. Operation counter [{}].",
						provisioningOperation.getSystem(), provisioningOperation.getOperationType().toString(), actualCount);
			} else {
				LOG.warn("To block the system id [{}] for operation [{}] remains [{}] operations + send message.",
						provisioningOperation.getSystem(), provisioningOperation.getOperationType().toString(), breakConfig.getDisableLimit() - actualCount);
			}
			IdmNotificationTemplateDto template = null;
			if (breakConfig.getWarningTemplate() == null) {
				LOG.debug("Warning template for provisioning break id [{}] missing.", breakConfig.getId());
			} else {
				template = DtoUtils.getEmbedded(breakConfig,
						SysProvisioningBreakConfig_.warningTemplate, IdmNotificationTemplateDto.class);				
			}
			//
			sendMessage(AccModuleDescriptor.TOPIC_PROVISIONING_BREAK_WARNING, system, actualCount, template,
					breakConfig, operationType, cache.getDiffBetweenActualAndLast(operationType, currentTimeMillis));
		} else if (breakConfig.getDisableLimit() != null && actualCount >= breakConfig.getDisableLimit()) {
			// check count is higher than disable limit
			// block system for operation
			blockSystemForOperation(operationType, system);
			//
			IdmNotificationTemplateDto template = null;
			if (breakConfig.getWarningTemplate() == null) {
				LOG.debug("Warning template for provisioning break id [{}] missing.", breakConfig.getId());
			} else {
				template = DtoUtils.getEmbedded(breakConfig,
						SysProvisioningBreakConfig_.disableTemplate, IdmNotificationTemplateDto.class);
			}
			//
			sendMessage(AccModuleDescriptor.TOPIC_PROVISIONING_BREAK_DISABLE, system, actualCount, template,
					breakConfig, operationType, cache.getDiffBetweenActualAndLast(operationType, currentTimeMillis));
			//
			LOG.warn("System id: [{}] will be blocked for operation: [{}].",
					provisioningOperation.getSystem(), operationType.toString());
			provisioningOperation = blockOperation(provisioningOperation, system);
			blocked = true;
		} else if (breakConfig.getWarningLimit() != null && actualCount > breakConfig.getWarningLimit()) {
			// after overrun warning limit, isn't send any another notification - add at least log
			if (breakConfig.getDisableLimit() == null) {
				LOG.warn("Block for the system id [{}] and operation [{}] is not set. Operation counter [{}].",
						provisioningOperation.getSystem(), provisioningOperation.getOperationType().toString(), actualCount);
			} else {
				LOG.warn("To block the system id [{}] for operation [{}] remains [{}] operations.",
						provisioningOperation.getSystem(), provisioningOperation.getOperationType().toString(), breakConfig.getDisableLimit() - actualCount);
			}
		}
		// remove all unless items in cache
		cache.addItem(operationType, currentTimeMillis);
		breakConfigService.saveCacheProcessedItems(provisioningOperation.getSystem(), cache);
		//
		event.setContent(provisioningOperation);
		return new DefaultEventResult<>(event, this, blocked);
	}

	@Override
	public int getOrder() {
		// before execute provisioning
		return -10;
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}
	
	/**
	 * Check if system isn't blocked for provisioning operation create, update and delete
	 * 
	 * @param operationType
	 * @param system
	 * @return
	 */
	private boolean isSystemBlockedOperation(ProvisioningEventType operationType, SysSystemDto system) {
		SysBlockedOperationDto blockedOperation = system.getBlockedOperation();
		// check also null values
		if (blockedOperation == null) {
			return false;
		}
		if (operationType == ProvisioningEventType.CREATE) {
			return blockedOperation.getCreateOperation();
		} else if (operationType == ProvisioningEventType.DELETE) {
			return blockedOperation.getDeleteOperation();
		} else if (operationType == ProvisioningEventType.UPDATE) {
			return blockedOperation.getUpdateOperation();
		}
		return false;
	}
	
	/**
	 * Method block this operation and send message to topic.
	 * 
	 * @param provisioningOperation
	 * @return
	 */
	private SysProvisioningOperationDto blockOperation(SysProvisioningOperationDto provisioningOperation, SysSystemDto system) {
		ResultModel resultModel = new DefaultResultModel(AccResultCode.PROVISIONING_SYSTEM_BLOCKED, 
				ImmutableMap.of("name", provisioningOperation.getSystemEntityUid(), "system", system.getName()));
		provisioningOperation.setResult(new OperationResult.Builder(OperationState.BLOCKED).setModel(resultModel).build());
		//
		provisioningOperation = provisioningOperationService.save(provisioningOperation);
		//
		// send also to provisioning topic (websocket)
		notificationManager.send(AccModuleDescriptor.TOPIC_PROVISIONING, new IdmMessageDto.Builder()
				.setModel(resultModel)
				.build());
		return provisioningOperation;
	}
	
	/**
	 * Method block system for specific operation
	 * 
	 * @param event
	 * @param system
	 */
	private void blockSystemForOperation(ProvisioningEventType event, SysSystemDto system) {
		if (event == ProvisioningEventType.CREATE) {
			system.getBlockedOperation().blockCreate();
		} else if (event == ProvisioningEventType.DELETE) {
			system.getBlockedOperation().blockDelete();
		} else if (event == ProvisioningEventType.UPDATE) {
			system.getBlockedOperation().blockUpdate();
		}
		system = systemService.save(system);
	}

	/**
	 * Send message with information about disabled system or warning
	 *  
	 * @param topic
	 * @param system
	 * @param actualCount
	 * @param template
	 * @param breakConfigId
	 */
	private void sendMessage(String topic, SysSystemDto system, Integer actualCount, IdmNotificationTemplateDto template,
			SysProvisioningBreakConfigDto breakConfig, ProvisioningEventType operationType, Long duration) {
		// Transform mills to sec and min for apache velocity
		String minTime = String.format("%d", 
			    TimeUnit.MILLISECONDS.toMinutes(duration));
		//
		String secTime = String.format("%d",
			    TimeUnit.MILLISECONDS.toSeconds(duration) - 
			    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
		//
		List<IdmIdentityDto> recipients = null;
		if (breakConfig.getGlobalConfiguration() == null || breakConfig.getGlobalConfiguration() == Boolean.FALSE) {
			recipients = breakRecipientService.getAllRecipients(breakConfig.getId());
		} else {
			recipients = breakRecipientService.getAllRecipientsForGlobalConfiguration(operationType);
		}
		//
		notificationManager.send(
				topic,
				new IdmMessageDto.Builder()
					.setTemplate(template)
					.addParameter("systemName", system.getName())
					.addParameter("operationName", operationType.name())
					.addParameter("actualCount", actualCount)
					.addParameter("minTime", minTime)
					.addParameter("secTime", secTime)
					.build(),
				recipients);
	}

}