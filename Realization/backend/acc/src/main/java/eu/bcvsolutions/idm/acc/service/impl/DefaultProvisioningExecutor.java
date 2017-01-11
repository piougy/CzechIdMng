package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.ResultState;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * Entry point to all provisioning operations.
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultProvisioningExecutor extends AbstractReadWriteEntityService<SysProvisioningOperation, EmptyFilter>
		implements ProvisioningExecutor, ReadWriteEntityService<SysProvisioningOperation, EmptyFilter> {

	private final EntityEventManager entityEventManager;

	@Autowired
	public DefaultProvisioningExecutor(
			SysProvisioningOperationRepository repository,
			EntityEventManager entityEventManager) {
		super(repository);
		//
		Assert.notNull(entityEventManager);
		//
		this.entityEventManager = entityEventManager;
	}

	@Override
	public SysProvisioningOperation execute(SysProvisioningOperation provisioningOperation) {
		Assert.notNull(provisioningOperation);
		//
		if (provisioningOperation.getId() == null) {
			provisioningOperation = save(provisioningOperation);
		}
		CoreEvent<SysProvisioningOperation> event = new CoreEvent<SysProvisioningOperation>(provisioningOperation.getOperationType(), provisioningOperation);
		EventContext<SysProvisioningOperation> context = entityEventManager.process(event);
		return context.getContent();
	}
	
	@Override
	public SysProvisioningOperation cancel(SysProvisioningOperation provisioningOperation) {
		// Cancel single request
		provisioningOperation.setResultState(ResultState.CANCELED);
		delete(provisioningOperation);
		return provisioningOperation;
	}

}
