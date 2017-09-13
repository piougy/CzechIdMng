package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.OperationResultDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningRequestDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningRequest_;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity_;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningRequestService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
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
	private final SysProvisioningRequestService requestService;
	private final SysSystemEntityService systemEntityService;

	@Autowired
	public DefaultProvisioningExecutor(
			SysProvisioningOperationRepository repository,
			EntityEventManager entityEventManager,
			SysProvisioningOperationService provisioningOperationService,
			SysProvisioningBatchService batchService,
			NotificationManager notificationManager,
			SysProvisioningRequestService requestService,
			SysSystemEntityService systemEntityService) {
		Assert.notNull(entityEventManager);
		Assert.notNull(provisioningOperationService);
		Assert.notNull(batchService);
		Assert.notNull(notificationManager);
		Assert.notNull(requestService);
		Assert.notNull(systemEntityService);
		//
		this.entityEventManager = entityEventManager;
		this.provisioningOperationService = provisioningOperationService;
		this.batchService = batchService;
		this.notificationManager = notificationManager;
		this.requestService = requestService;
		this.systemEntityService = systemEntityService;
	}

	@Override
	@Transactional
	public SysProvisioningOperationDto execute(SysProvisioningOperationDto provisioningOperation) {
		Assert.notNull(provisioningOperation);
		Assert.notNull(provisioningOperation.getSystemEntity());
		Assert.notNull(provisioningOperation.getProvisioningContext());
		SysSystemEntityDto systemEntityDto = systemEntityService.get(provisioningOperation.getSystemEntity());
		SysSystemDto system = DtoUtils.getEmbedded(systemEntityDto, SysSystemEntity_.system, SysSystemDto.class);
		Assert.notNull(system);
		//
		if (provisioningOperationService.isNew(provisioningOperation)) {
			// save new operation to provisioning log / queue
			SysProvisioningBatchDto batch = provisioningOperationService.findBatch(provisioningOperation);
			SysProvisioningRequestDto request = new SysProvisioningRequestDto(provisioningOperation.getId());
			if (batch == null) {
				batch = batchService.save(new SysProvisioningBatchDto());
				request.setResult(new OperationResultDto.Builder(OperationState.CREATED).build());
			} else {
				// get system entity from service, in provisioning operation may not exist
				SysSystemEntityDto systemEntity = systemEntityService.get(provisioningOperation.getSystemEntity());
				// put to queue
				// TODO: maybe putting into queue has to run after disable and readonly system
				ResultModel resultModel = new DefaultResultModel(AccResultCode.PROVISIONING_IS_IN_QUEUE, 
						ImmutableMap.of(
								"name", systemEntity.getUid(), 
								"system", system.getName(),
								"operationType", provisioningOperation.getOperationType(),
								"objectClass", provisioningOperation.getProvisioningContext().getConnectorObject().getObjectClass()));
				LOG.debug(resultModel.toString());				
				request.setResult(new OperationResultDto.Builder(OperationState.NOT_EXECUTED).setModel(resultModel).build());
			}
			request.setBatch(batch.getId());
			provisioningOperation.setRequest(request);
			provisioningOperation = provisioningOperationService.save(provisioningOperation);
			request = provisioningOperation.getRequest();
			//
			//
			if (OperationState.NOT_EXECUTED == request.getResult().getState()) {
				notificationManager.send(
						AccModuleDescriptor.TOPIC_PROVISIONING,
						new IdmMessageDto.Builder(NotificationLevel.INFO)
						.setModel(request.getResult().getModel())
						.build());
				return provisioningOperation;
			}
		}
		//
		// execute - after original transaction is commited
		entityEventManager.publishEvent(provisioningOperation);
		//
		return provisioningOperation;
	}
	
	/**
	 * We need to wait to transaction commit, when provisioning is executed - all accounts have to be prepared.
	 * 
	 * @param provisioningOperation
	 * @return
	 */
	// @Async TODO: turns on asynchronous provisioning - tests are broken
	@Override
	@TransactionalEventListener
	@Transactional(noRollbackFor = ProvisioningException.class, propagation = Propagation.REQUIRES_NEW)
	public SysProvisioningOperationDto executeInternal(SysProvisioningOperationDto provisioningOperation) {
		Assert.notNull(provisioningOperation);
		Assert.notNull(provisioningOperation.getSystemEntity());
		Assert.notNull(provisioningOperation.getProvisioningContext());
		CoreEvent<SysProvisioningOperationDto> event = new CoreEvent<SysProvisioningOperationDto>(provisioningOperation.getOperationType(), provisioningOperation);
		EventContext<SysProvisioningOperationDto> context = entityEventManager.process(event);		
		return context.getContent();
	}
	
	@Override
	@Transactional
	public SysProvisioningOperationDto cancel(SysProvisioningOperationDto provisioningOperation) {
		// Cancel single request
		CoreEvent<SysProvisioningOperationDto> event = new CoreEvent<SysProvisioningOperationDto>(ProvisioningEventType.CANCEL, provisioningOperation);
		EventContext<SysProvisioningOperationDto> context = entityEventManager.process(event);
		return context.getContent();
	}

	@Override
	public void execute(SysProvisioningBatchDto batch) {
		Assert.notNull(batch);
		batch = batchService.get(batch.getId());
		//		
		for (SysProvisioningRequestDto request : requestService.getByTimelineAndBatchId(batch.getId())) {
			SysProvisioningOperationDto provisioningOperation = DtoUtils.getEmbedded(request, SysProvisioningRequest_.operation, SysProvisioningOperationDto.class);
			SysProvisioningOperationDto operation = executeInternal(provisioningOperation); // not run in transaction
			if (operation.getRequest() != null && OperationState.EXECUTED != operation.getResultState()) {
				return;
			}
			// TODO: is this necessary 
			// batch.removeRequest(request);
		}
	}

	@Override
	@Transactional
	public void cancel(SysProvisioningBatchDto batch) {
		Assert.notNull(batch);
		//
		for (SysProvisioningRequestDto request : requestService.getByTimelineAndBatchId(batch.getId())) {
			cancel(DtoUtils.getEmbedded(request, SysProvisioningRequest_.operation, SysProvisioningOperationDto.class));
		}
	}
}
