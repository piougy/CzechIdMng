package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.service.api.NotificationManager;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Readonly system provisioning - only saves provisioning operations
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Checks readonly system before provisioning is called.")
public class ReadonlySystemProcessor extends AbstractEntityEventProcessor<SysProvisioningOperation> {
	
	public static final String PROCESSOR_NAME = "readonly-system-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ReadonlySystemProcessor.class);
	private final NotificationManager notificationManager;
	private final SysProvisioningOperationService provisioningOperationService;
	
	@Autowired
	public ReadonlySystemProcessor(
			NotificationManager notificationManager,
			SysProvisioningOperationService provisioningOperationService) {
		super(ProvisioningEventType.CREATE, ProvisioningEventType.UPDATE, ProvisioningEventType.DELETE);
		//
		Assert.notNull(notificationManager);
		Assert.notNull(provisioningOperationService);
		//
		this.notificationManager = notificationManager;
		this.provisioningOperationService = provisioningOperationService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<SysProvisioningOperation> process(EntityEvent<SysProvisioningOperation> event) {
		SysProvisioningOperation provisioningOperation = event.getContent();
		SysSystem system = provisioningOperation.getSystem();
		boolean closed = false;
		if (system.isReadonly()) {
			ResultModel resultModel = new DefaultResultModel(AccResultCode.PROVISIONING_SYSTEM_READONLY, 
					ImmutableMap.of("name", provisioningOperation.getSystemEntityUid(), "system", system.getName()));
			provisioningOperation.getRequest().setResult(
					new OperationResult.Builder(OperationState.NOT_EXECUTED).setModel(resultModel).build());
			//
			provisioningOperationService.save(provisioningOperation);
			//
			LOG.info(resultModel.toString());
			notificationManager.send(
					AccModuleDescriptor.TOPIC_PROVISIONING,
					new IdmMessageDto.Builder()
					.setModel(resultModel)
					.build());
			//
			closed = true;
		} 
		return new DefaultEventResult<>(event, this, closed);
	}
	
	@Override
	public boolean isClosable() {
		return true;
	}

	@Override
	public int getOrder() {
		// after account attributes preparation and before realization
		return -500;
	}
}
