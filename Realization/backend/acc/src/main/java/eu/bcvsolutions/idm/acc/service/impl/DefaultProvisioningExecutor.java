package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
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
	public void executeOperation(SysProvisioningOperation provisioningOperation) {
		Assert.notNull(provisioningOperation);
		// TODO: clone
		save(provisioningOperation);
		// TODO: readonly system
		// TODO: result / exception handling
		entityEventManager.process(new CoreEvent<SysProvisioningOperation>(provisioningOperation.getOperationType(),
				provisioningOperation));
	}

}
