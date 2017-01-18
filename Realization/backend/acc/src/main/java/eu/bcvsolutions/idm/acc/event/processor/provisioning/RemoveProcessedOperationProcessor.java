package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.ResultState;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Archives processed provisioning operations.
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class RemoveProcessedOperationProcessor extends AbstractEntityEventProcessor<SysProvisioningOperation> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ReadonlySystemProcessor.class);
	private final SysProvisioningOperationService provisioningOperationService;
	
	@Autowired
	public RemoveProcessedOperationProcessor(
			SysProvisioningOperationService provisioningOperationService,
			SysProvisioningArchiveService provisioningArchiveService) {
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
