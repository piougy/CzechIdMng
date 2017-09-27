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
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation_;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Entry point to all provisioning operations.
 * 
 * @author Radek Tomiška
 *
 */
@Service("provisioningExecutor")
public class DefaultProvisioningExecutor implements ProvisioningExecutor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultProvisioningExecutor.class);
	private final EntityEventManager entityEventManager;
	private final SysProvisioningOperationService provisioningOperationService;
	private final SysProvisioningBatchService batchService;
	private final NotificationManager notificationManager;
	private final SysSystemService systemService;
	private final SecurityService securityService;

	@Autowired
	public DefaultProvisioningExecutor(
			SysProvisioningOperationRepository repository,
			EntityEventManager entityEventManager,
			SysProvisioningOperationService provisioningOperationService,
			SysProvisioningBatchService batchService,
			NotificationManager notificationManager,
			SysSystemService systemService,
			SecurityService securityService) {
		Assert.notNull(entityEventManager);
		Assert.notNull(provisioningOperationService);
		Assert.notNull(batchService);
		Assert.notNull(notificationManager);
		Assert.notNull(systemService);
		Assert.notNull(securityService);
		//
		this.entityEventManager = entityEventManager;
		this.provisioningOperationService = provisioningOperationService;
		this.batchService = batchService;
		this.notificationManager = notificationManager;
		this.systemService = systemService;
		this.securityService = securityService;
	}
	
	private SysProvisioningOperationDto persistOperationIfNeeded(SysProvisioningOperationDto provisioningOperation) {
		Assert.notNull(provisioningOperation);
		Assert.notNull(provisioningOperation.getSystemEntityUid());
		Assert.notNull(provisioningOperation.getProvisioningContext());
		// get system from service, in provisioning operation may not exist
		SysSystemDto system = systemService.get(provisioningOperation.getSystem());
		Assert.notNull(system);
		provisioningOperation.getEmbedded().put(SysProvisioningOperation_.system.getName(), system); // make sure system will be in embedded
		//
		if (provisioningOperationService.isNew(provisioningOperation)) {
			// save new operation to provisioning log / queue
			SysProvisioningBatchDto batch = batchService.findBatch(system.getId(), provisioningOperation.getCreatorId(), provisioningOperation.getSystemEntityUid());
			if (batch == null) {
				batch = batchService.save(new SysProvisioningBatchDto());
				provisioningOperation.setResult(new OperationResult.Builder(OperationState.CREATED).build());
			} else {				
				// put to queue
				// TODO: maybe putting into queue has to run after disable and readonly system
				ResultModel resultModel = new DefaultResultModel(AccResultCode.PROVISIONING_IS_IN_QUEUE, 
						ImmutableMap.of(
								"name", provisioningOperation.getSystemEntityUid(), 
								"system", system.getName(),
								"operationType", provisioningOperation.getOperationType(),
								"objectClass", provisioningOperation.getProvisioningContext().getConnectorObject().getObjectClass()));
				LOG.debug(resultModel.toString());				
				provisioningOperation.setResult(new OperationResult.Builder(OperationState.NOT_EXECUTED).setModel(resultModel).build());
			}
			provisioningOperation.setBatch(batch.getId());
			provisioningOperation = provisioningOperationService.save(provisioningOperation);
			//
			//
			if (OperationState.NOT_EXECUTED == provisioningOperation.getResult().getState()) {
				if (securityService.getCurrentId() != null) { // TODO: check logged identity and account owner
					notificationManager.send(
							AccModuleDescriptor.TOPIC_PROVISIONING,
							new IdmMessageDto.Builder(NotificationLevel.WARNING)
								.setModel(provisioningOperation.getResult().getModel())
								.build());
				}
			}
		}
		return provisioningOperation;
	}

	@Override
	@Transactional(noRollbackFor = ProvisioningException.class)
	public SysProvisioningOperationDto execute(SysProvisioningOperationDto provisioningOperation) {
		boolean isNew = provisioningOperationService.isNew(provisioningOperation);
		provisioningOperation = persistOperationIfNeeded(provisioningOperation);
		if (isNew && OperationState.NOT_EXECUTED == provisioningOperation.getResult().getState()) {
			return provisioningOperation;
		}
		//
		// execute - after original transaction is commited
		// only if system supports synchronous processing
		SysSystemDto system = DtoUtils.getEmbedded(provisioningOperation, SysProvisioningOperation_.system, SysSystemDto.class);
		Assert.notNull(system);
		if (!system.isQueue()) {
			entityEventManager.publishEvent(provisioningOperation);
		}
		//
		return provisioningOperation;
	}
	
	@Override
	@Transactional(noRollbackFor = ProvisioningException.class)
	public SysProvisioningOperationDto executeSync(SysProvisioningOperationDto provisioningOperation) {
		boolean isNew = provisioningOperationService.isNew(provisioningOperation);
		provisioningOperation = persistOperationIfNeeded(provisioningOperation);
		if (isNew && OperationState.NOT_EXECUTED == provisioningOperation.getResult().getState()) {
			return provisioningOperation;
		}
		//
		return executeInternal(provisioningOperation);
	}
	
	/**
	 * We need to wait to transaction commit, when provisioning is executed - all accounts have to be prepared.
	 * 
	 * @param provisioningOperation
	 * @return
	 */
	@TransactionalEventListener
	@Transactional(noRollbackFor = ProvisioningException.class, propagation = Propagation.REQUIRES_NEW)
	public SysProvisioningOperationDto executeInternal(SysProvisioningOperationDto provisioningOperation) {
		Assert.notNull(provisioningOperation);
		Assert.notNull(provisioningOperation.getSystemEntityUid());
		Assert.notNull(provisioningOperation.getProvisioningContext());
		CoreEvent<SysProvisioningOperationDto> event = new CoreEvent<SysProvisioningOperationDto>(provisioningOperation.getOperationType(), provisioningOperation);
		try {
			EventContext<SysProvisioningOperationDto> context = entityEventManager.process(event);		
			return context.getContent();
		} catch (ProvisioningException ex) { // TODO: result code exceptions? or all exceptions?
			return provisioningOperationService.handleFailed(provisioningOperation, ex);
		}
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
	public OperationResult execute(SysProvisioningBatchDto batch) {
		Assert.notNull(batch);
		batch = batchService.get(batch.getId());
		//	
		OperationResult result = null;
		for (SysProvisioningOperationDto provisioningOperation : provisioningOperationService.getByTimelineAndBatchId(batch.getId())) {
			// It not possible to get operation from embedded, because missing request
			SysProvisioningOperationDto operation = executeInternal(provisioningOperation); // not run in transaction
			result = operation.getResult();
			if (OperationState.EXECUTED != result.getState()) {
				// stop processing next requests
				return result;
			}
		}
		// last processed request state (previous requests will be OperationState.EXECUTED)
		return result;
	}

	@Override
	@Transactional
	public void cancel(SysProvisioningBatchDto batch) {
		Assert.notNull(batch);
		//
		for (SysProvisioningOperationDto operation : provisioningOperationService.getByTimelineAndBatchId(batch.getId())) {
			// It not possible get operation from embedded, missing request
			cancel(operation);
		}
	}
}
