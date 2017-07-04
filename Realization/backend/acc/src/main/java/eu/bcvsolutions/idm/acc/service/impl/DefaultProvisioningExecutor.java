package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningBatch;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningRequest;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.service.api.NotificationManager;

/**
 * Entry point to all provisioning operations.
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultProvisioningExecutor implements ProvisioningExecutor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultProvisioningExecutor.class);
	private final EntityEventManager entityEventManager;
	private final SysProvisioningOperationService provisioningOperationService;
	private final SysProvisioningBatchService batchService;
	private final NotificationManager notificationManager;

	@Autowired
	public DefaultProvisioningExecutor(
			SysProvisioningOperationRepository repository,
			EntityEventManager entityEventManager,
			SysProvisioningOperationService provisioningOperationService,
			SysProvisioningBatchService batchService,
			NotificationManager notificationManager) {
		Assert.notNull(entityEventManager);
		Assert.notNull(provisioningOperationService);
		Assert.notNull(batchService);
		Assert.notNull(notificationManager);
		//
		this.entityEventManager = entityEventManager;
		this.provisioningOperationService = provisioningOperationService;
		this.batchService = batchService;
		this.notificationManager = notificationManager;
	}

	@Override
	@Transactional
	public SysProvisioningOperation execute(SysProvisioningOperation provisioningOperation) {
		//
		// execute - after original transaction is commited
		entityEventManager.publishEvent(provisioningOperation);
		return provisioningOperation;
	}
	
	/**
	 * We need to wait to transaction commit, when provisioning is executed - all accounts have to be prepared.
	 * 
	 * @param provisioningOperation
	 * @return
	 */
	// @Async
	@Override
	@TransactionalEventListener
	@Transactional(noRollbackFor = ProvisioningException.class, propagation = Propagation.REQUIRES_NEW)
	public SysProvisioningOperation executeInternal(SysProvisioningOperation provisioningOperation) {
		Assert.notNull(provisioningOperation);
		Assert.notNull(provisioningOperation.getSystem());
		Assert.notNull(provisioningOperation.getProvisioningContext());
		//
		if (provisioningOperationService.isNew(provisioningOperation)) {
			// save new operation to provisioning log / queue
			SysProvisioningBatch batch = batchService.findBatch(provisioningOperation);
			SysProvisioningRequest request = new SysProvisioningRequest(provisioningOperation);
			if (batch == null) {
				batch = batchService.save(new SysProvisioningBatch());
				request.setResult(new OperationResult.Builder(OperationState.CREATED).build());
			} else {
				// put to queue
				// TODO: maybe putting into queue has to run after disable and readonly system
				ResultModel resultModel = new DefaultResultModel(AccResultCode.PROVISIONING_IS_IN_QUEUE, 
						ImmutableMap.of(
								"name", provisioningOperation.getSystemEntityUid(), 
								"system", provisioningOperation.getSystem().getName(),
								"operationType", provisioningOperation.getOperationType(),
								"objectClass", provisioningOperation.getProvisioningContext().getConnectorObject().getObjectClass()));
				LOG.debug(resultModel.toString());				
				request.setResult(new OperationResult.Builder(OperationState.NOT_EXECUTED).setModel(resultModel).build());
			}
			request.setBatch(batch);
			provisioningOperation.setRequest(request);
			//
			provisioningOperation = provisioningOperationService.save(provisioningOperation);
			if (OperationState.NOT_EXECUTED == request.getResult().getState()) {
				notificationManager.send(
						AccModuleDescriptor.TOPIC_PROVISIONING,
						new IdmMessageDto.Builder(NotificationLevel.INFO)
						.setModel(request.getResult().getModel())
						.build());
				return provisioningOperation;
			}
		}
		CoreEvent<SysProvisioningOperation> event = new CoreEvent<SysProvisioningOperation>(provisioningOperation.getOperationType(), provisioningOperation);
		EventContext<SysProvisioningOperation> context = entityEventManager.process(event);		
		return context.getContent();
	}
	
	@Override
	@Transactional
	public SysProvisioningOperation cancel(SysProvisioningOperation provisioningOperation) {
		// Cancel single request
		CoreEvent<SysProvisioningOperation> event = new CoreEvent<SysProvisioningOperation>(ProvisioningEventType.CANCEL, provisioningOperation);
		EventContext<SysProvisioningOperation> context = entityEventManager.process(event);
		return context.getContent();
	}

	@Override
	public void execute(SysProvisioningBatch batch) {
		Assert.notNull(batch);
		batch = batchService.get(batch.getId());
		//
		for (SysProvisioningRequest request : batch.getRequestsByTimeline()) {
			SysProvisioningOperation operation = executeInternal(request.getOperation()); // not run in transaction
			if (operation.getRequest() != null && OperationState.EXECUTED != operation.getResultState()) {
				return;
			}
			batch.removeRequest(request);
		}
	}

	@Override
	@Transactional
	public void cancel(SysProvisioningBatch batch) {
		Assert.notNull(batch);
		//
		for (SysProvisioningRequest request : batch.getRequestsByTimeline()) {
			cancel(request.getOperation());
		}
	}
}
