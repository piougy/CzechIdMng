package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.ResultState;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningRequest;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningResult;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.notification.service.api.NotificationManager;

/**
 * Readonly system provisioning - only saves provisioning operations
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class ReadonlySystemProcessor extends AbstractEntityEventProcessor<SysProvisioningOperation> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ReadonlySystemProcessor.class);
	private final NotificationManager notificationManager;
	private final SysProvisioningOperationRepository provisioningOperationRepository;
	
	@Autowired
	public ReadonlySystemProcessor(
			NotificationManager notificationManager,
			SysProvisioningOperationRepository provisioningOperationRepository) {
		super(ProvisioningOperationType.CREATE, ProvisioningOperationType.UPDATE, ProvisioningOperationType.DELETE);
		//
		Assert.notNull(notificationManager);
		Assert.notNull(provisioningOperationRepository);
		//
		this.notificationManager = notificationManager;
		this.provisioningOperationRepository = provisioningOperationRepository;
	}
	
	@Override
	public EventResult<SysProvisioningOperation> process(EntityEvent<SysProvisioningOperation> event) {
		SysProvisioningOperation provisioningOperation = event.getContent();
		SysSystem system = provisioningOperation.getSystem();
		boolean closed = false;
		if (system.isReadonly()) {
			SysProvisioningRequest request = provisioningOperation.getRequest();
			if (provisioningOperation.getRequest() == null) {
				request = new SysProvisioningRequest(provisioningOperation);
				provisioningOperation.setRequest(request);
			}
			ResultModel resultModel = new DefaultResultModel(AccResultCode.PROVISIONING_SYSTEM_READONLY, 
					ImmutableMap.of("name", provisioningOperation.getSystemEntityUid(), "system", system.getName()));
			request.setResult(new SysProvisioningResult.Builder(ResultState.NOT_EXECUTED).setModel(resultModel).build());
			//
			provisioningOperationRepository.save(provisioningOperation);
			//
			LOG.info(resultModel.toString());
			notificationManager.send(
					AccModuleDescriptor.TOPIC_PROVISIONING,
					new IdmMessage.Builder().setModel(resultModel).build());
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
