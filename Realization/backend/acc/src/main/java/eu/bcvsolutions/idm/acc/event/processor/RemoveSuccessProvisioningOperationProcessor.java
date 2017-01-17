package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.ResultState;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Removes successfully processed provisioning event from queue.
 * 
 * TODO: Archive success operations - without request and batch
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class RemoveSuccessProvisioningOperationProcessor extends AbstractEntityEventProcessor<SysProvisioningOperation> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProvisioningReadonlySystemProcessor.class);
	private final SysProvisioningOperationService provisioningOperationService;
	
	@Autowired
	public RemoveSuccessProvisioningOperationProcessor(
			SysProvisioningOperationService provisioningOperationService) {
		super(ProvisioningOperationType.CREATE, ProvisioningOperationType.UPDATE, ProvisioningOperationType.DELETE, ProvisioningOperationType.CANCEL);
		//
		Assert.notNull(provisioningOperationService);
		//
		this.provisioningOperationService = provisioningOperationService;
	}
	
	@Override
	public EventResult<SysProvisioningOperation> process(EntityEvent<SysProvisioningOperation> event) {
		SysProvisioningOperation provisioningOperation = event.getContent();
		if (ResultState.EXECUTED.equals(provisioningOperation.getResultState()) 
				|| ResultState.CANCELED.equals(provisioningOperation.getResultState())) {
			 provisioningOperationService.delete(provisioningOperation);
			 LOG.debug("Executed provisioning operation [{}] was removed from queue.", provisioningOperation.getId());
		}
		return new DefaultEventResult<>(event, this, isClosable());
	}
	
	@Override
	public boolean isClosable() {
		return true;
	}
	
	@Override
	public int getOrder() {
		// on the end
		return Ordered.LOWEST_PRECEDENCE;
	}
}
