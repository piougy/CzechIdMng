package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.ResultState;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningRequest;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningResult;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.notification.domain.NotificationLevel;
import eu.bcvsolutions.idm.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.notification.service.api.NotificationManager;

/**
 * Readonly sestem provisioning - only saves provisioning operations
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class ProvisioningReadonlySystemProcessor extends AbstractEntityEventProcessor<SysProvisioningOperation> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProvisioningReadonlySystemProcessor.class);
	private final NotificationManager notificationManager;
	private final SysProvisioningOperationRepository provisioningOperationRepository;
	
	@Autowired
	public ProvisioningReadonlySystemProcessor(
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
			request.setResult(new SysProvisioningResult.Builder(ResultState.NOT_EXECUTED).setCode("READONLY").build()); // TODO: code
			
			provisioningOperationRepository.save(provisioningOperation);
			//
			LOG.debug("Provisioning operation for object with uid [{}] and system [{}] is canceled - system is readonly", 
					provisioningOperation.getSystemEntityUid(),
					system.getName());
			notificationManager.send(
					"idm:websocket", 
					new IdmMessage.Builder(NotificationLevel.WARNING)
						.setSubject("Provisioning účtu [" + provisioningOperation.getSystemEntityUid() + "] neproběhl")
						.setMessage("Provisioning účtu [" + provisioningOperation.getSystemEntityUid() + "] na systém [" + provisioningOperation.getSystem().getName() + "] nebyl proveden. Systém je v readonly režimu.")
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
		return -2000;
	}
}
