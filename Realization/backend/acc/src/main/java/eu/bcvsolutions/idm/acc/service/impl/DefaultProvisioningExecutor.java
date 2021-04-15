package eu.bcvsolutions.idm.acc.service.impl;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.config.domain.ProvisioningConfiguration;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation_;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.config.DelegatingTransactionContextRunnable;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.Assert;

/**
 * Entry point to all provisioning operations.
 *
 * @author Radek Tomiška
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
	private final ProvisioningConfiguration provisioningConfiguration;
	private final SysSystemEntityService systemEntityService;
	//
	@Autowired	private IdmRoleRequestService roleRequestService;
	@Autowired	private LookupService lookupService;

	@Autowired
	public DefaultProvisioningExecutor(
			SysProvisioningOperationRepository repository,
			EntityEventManager entityEventManager,
			SysProvisioningOperationService provisioningOperationService,
			SysProvisioningBatchService batchService,
			NotificationManager notificationManager,
			SysSystemService systemService,
			SecurityService securityService,
			ProvisioningConfiguration provisioningConfiguration,
			SysSystemEntityService systemEntityService) {
		Assert.notNull(entityEventManager, "Manager is required.");
		Assert.notNull(provisioningOperationService, "Service is required.");
		Assert.notNull(batchService, "Service is required.");
		Assert.notNull(notificationManager, "Manager is required.");
		Assert.notNull(systemService, "Service is required.");
		Assert.notNull(securityService, "Service is required.");
		Assert.notNull(provisioningConfiguration, "Configuration is required.");
		Assert.notNull(systemEntityService, "Service is required.");
		//
		this.entityEventManager = entityEventManager;
		this.provisioningOperationService = provisioningOperationService;
		this.batchService = batchService;
		this.notificationManager = notificationManager;
		this.systemService = systemService;
		this.securityService = securityService;
		this.provisioningConfiguration = provisioningConfiguration;
		this.systemEntityService = systemEntityService;
	}

	@Override
	@Transactional
	public synchronized void execute(SysProvisioningOperationDto provisioningOperation) {
		// execute - after original transaction is commited
		// only if system supports synchronous processing
		SysSystemDto system = DtoUtils.getEmbedded(provisioningOperation, SysProvisioningOperation_.system.getName(), SysSystemDto.class, null);
		if (system == null) {
			system = systemService.get(provisioningOperation.getSystem());
		}
		Assert.notNull(system, "System is required.");
		if (!system.isQueue()) {
			if (provisioningOperationService.isNew(provisioningOperation)) {
				// In sync mode, we need to save operation now (for request and system state)
				provisioningOperation = persistOperation(provisioningOperation);
				// We need to mark DTO, because in the executeInternal we need to make check on NOT_EXECUTED state.
				provisioningOperation.setSynchronousProvisioning(true);
			}
			entityEventManager.publishEvent(provisioningOperation);
			
			IdmEntityEventDto entityEvent = new IdmEntityEventDto();
			entityEvent.setEventType(ProvisioningEventType.START.name());
			entityEvent.setOwnerId(provisioningOperation.getId());
			entityEvent.setOwnerType(lookupService.getOwnerType(provisioningOperation));
			// Create manual event. We need to ensure that a sync will be ends after
			// all provisioning operations will be executed.
			entityEvent = entityEventManager.createManualEvent(entityEvent);
			
			provisioningOperation.setManualEventId(entityEvent.getId());
			return;
		}
		// put to queue
		if (provisioningOperationService.isNew(provisioningOperation)) {
			provisioningOperation = persistOperation(provisioningOperation);
		}

	}

	/**
	 * Next processing is executed outside a transaction
	 * => operation states has to be saved in new transactions
	 * => rollback on the target system is not possible anyway
	 */
	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public SysProvisioningOperationDto executeSync(SysProvisioningOperationDto provisioningOperation) {
		return executeInternal(provisioningOperation);
	}

	/**
	 * We need to wait to transaction commit, when provisioning is executed - all accounts have to be prepared.
	 * Next processing is executed outside a transaction
	 * => operation states has to be saved in new transactions
	 * => rollback on the target system is not possible anyway
	 *
	 * @param provisioningOperation
	 * @return
	 */
	@TransactionalEventListener
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public SysProvisioningOperationDto executeInternal(SysProvisioningOperationDto provisioningOperation) {
		Assert.notNull(provisioningOperation, "Provisioning operation is required.");
		Assert.notNull(provisioningOperation.getSystemEntity(), "System entity is required.");
		Assert.notNull(provisioningOperation.getProvisioningContext(), "Provisioning context is required.");
		//
		try {
			boolean checkNotExecuted = provisioningOperation.isSynchronousProvisioning();
			if (provisioningOperationService.isNew(provisioningOperation)) {
				provisioningOperation = persistOperation(provisioningOperation);
				checkNotExecuted = true;
			}
			if (checkNotExecuted
					&& provisioningOperation.getResult() != null
					&& OperationState.NOT_EXECUTED == provisioningOperation.getResult().getState()) {
				return provisioningOperation;
			}
			//
			CoreEvent<SysProvisioningOperationDto> event = new CoreEvent<SysProvisioningOperationDto>(provisioningOperation.getOperationType(), provisioningOperation);
			try {
				// set a global provisioning timeout even for synchronous call
				FutureTask<EventContext<SysProvisioningOperationDto>> futureTask = new FutureTask<EventContext<SysProvisioningOperationDto>>(new Callable<EventContext<SysProvisioningOperationDto>>() {

					@Override
					public EventContext<SysProvisioningOperationDto> call() {
						return entityEventManager.process(event);
					}

				});
				// thread pool is not used here
				Thread thread = new Thread(new DelegatingSecurityContextRunnable(new DelegatingTransactionContextRunnable(futureTask)));
				thread.start();
				//
				// global timeout by configuration
				long timeout = provisioningConfiguration.getTimeout();
				try {
					// TODO: non blocking wait if possible (refactoring is needed + java 9 helps)
					EventContext<SysProvisioningOperationDto> context = futureTask.get(
							timeout,
							TimeUnit.MILLISECONDS
					);
					//
					return context.getContent();
				} catch (InterruptedException ex) {
					futureTask.cancel(true);
					// propagate exception to upper catch
					throw ex;
				} catch (TimeoutException ex) {
					futureTask.cancel(true);
					// put thread into queue and wait => timeout too => retry mecchanism will work
					throw new ResultCodeException(
							AccResultCode.PROVISIONING_TIMEOUT,
							ImmutableMap.of(
									"name", provisioningOperation.getSystemEntityUid(),
									"system", provisioningOperation.getSystem(),
									"operationType", provisioningOperation.getOperationType(),
									"objectClass", provisioningOperation.getProvisioningContext().getConnectorObject().getObjectClass(),
									"timeout", String.valueOf(timeout)
							),
							ex
					);
				}
			} catch (Exception ex) {
				return provisioningOperationService.handleFailed(provisioningOperation, ex);
			} finally {
				try {
					UUID roleRequestId = provisioningOperation.getRoleRequestId();
					if (roleRequestId != null) {
						// Check of the state for whole request
						// Create mock request -> we don't wont load request from DB -> optimization
						IdmRoleRequestDto mockRequest = new IdmRoleRequestDto();
						mockRequest.setId(roleRequestId);
						mockRequest.setState(RoleRequestState.EXECUTED);

						IdmRoleRequestDto returnedReqeust = roleRequestService.refreshSystemState(mockRequest);
						OperationResultDto systemState = returnedReqeust.getSystemState();
						if (systemState == null) {
							// State on system of request was not changed (may be not all provisioning operations are
							// resolved)
						} else {
							// We have final state on systems
							IdmRoleRequestDto requestDto = roleRequestService.get(roleRequestId);
							if (requestDto != null) {
								requestDto.setSystemState(systemState);
								roleRequestService.save(requestDto);
							} else {
								LOG.info(MessageFormat.format(
										"Refresh role-request system state: Role-request with ID [{0}] was not found (maybe was deleted).",
										roleRequestId));
							}
						}
					}

				} catch (Exception ex) {
					return provisioningOperationService.handleFailed(provisioningOperation, ex);
				}
			}
		} finally {
			UUID eventId = provisioningOperation.getManualEventId();
			if (eventId != null) {
				IdmEntityEventDto startEvent = entityEventManager.getEvent(eventId);
				if (startEvent != null) {
					// Complete a manual event (for ensure end of the sync).
					entityEventManager.completeManualEvent(startEvent);
				}
			}
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

	/**
	 * Next processing is executed outside a transaction
	 * => operation states has to be saved in new transactions
	 * => rollback on the target system is not possible anyway.
	 */
	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public synchronized OperationResult execute(SysProvisioningBatchDto batch) {
		Assert.notNull(batch, "Provisioning batch is required.");
		batch = batchService.get(batch.getId());
		//	
		OperationResult result = null;
		List<SysProvisioningOperationDto> operations = provisioningOperationService.getByTimelineAndBatchId(batch.getId());
		if (operations.isEmpty()) {
			// reset next attempt time if is filled
			if (batch.getNextAttempt() != null) {
				batch.setNextAttempt(null);
				batchService.save(batch);
			}
			return new OperationResult.Builder(OperationState.EXECUTED).build();
		}
		for (SysProvisioningOperationDto provisioningOperation : operations) {
			// operation is already running - not complete or executed manually between
			if (provisioningOperation.getResultState() == OperationState.RUNNING) {
				LOG.debug("Previous operation [{}] still running, next operations will be executed after previous operation ends (with next retry run)",
						provisioningOperation.getId());
				//
				ResultModel resultModel = new DefaultResultModel(AccResultCode.PROVISIONING_IS_IN_QUEUE,
						ImmutableMap.of(
								"name", provisioningOperation.getSystemEntityUid(),
								"system", provisioningOperation.getSystem(),
								"operationType", provisioningOperation.getOperationType(),
								"objectClass", provisioningOperation.getProvisioningContext().getConnectorObject().getObjectClass()));
				result = new OperationResult.Builder(OperationState.NOT_EXECUTED).setModel(resultModel).build();
				break;
			}
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
		Assert.notNull(batch, "Provisioning batch is required.");
		//
		for (SysProvisioningOperationDto operation : provisioningOperationService.getByTimelineAndBatchId(batch.getId())) {
			// It not possible get operation from embedded, missing request
			cancel(operation);
		}
	}

	@Override
	public ProvisioningConfiguration getConfiguration() {
		return provisioningConfiguration;
	}

	/**
	 * Persist new operation - assign appropriate batch. Operation is put into queue, if it's already in the queue.
	 */
	private SysProvisioningOperationDto persistOperation(SysProvisioningOperationDto provisioningOperation) {
		Assert.notNull(provisioningOperation, "Provisioning operation is required.");
		Assert.notNull(provisioningOperation.getSystemEntity(), "System entity is required.");
		Assert.notNull(provisioningOperation.getProvisioningContext(), "Provisioning context is required.");
		// get system from service, in provisioning operation may not exist
		SysSystemDto system = DtoUtils.getEmbedded(provisioningOperation, SysProvisioningOperation_.system, (SysSystemDto) null);
		if (system == null) {
			system = systemService.get(provisioningOperation.getSystem());
		}
		Assert.notNull(system, "System is required.");
		provisioningOperation.getEmbedded().put(SysProvisioningOperation_.system.getName(), system); // make sure system will be in embedded - optimize
		//
		// save new operation to provisioning log / queue
		String uid = systemEntityService.getByProvisioningOperation(provisioningOperation).getUid();
		// look out - system entity uid can be changed - we need to use system entity id
		SysProvisioningBatchDto batch = batchService.findBatch(provisioningOperation.getSystemEntity());
		if (batch == null) {
			// new batch
			batch = batchService.save(new SysProvisioningBatchDto(provisioningOperation));
			provisioningOperation.setResult(new OperationResult.Builder(OperationState.CREATED).build());
		} else {
			SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
			filter.setNotInState(OperationState.CREATED);
			filter.setBatchId(batch.getId());
			List<SysProvisioningOperationDto> activeOperations = provisioningOperationService
					.find(filter, PageRequest.of(0, 1, new Sort(Direction.DESC, SysProvisioningOperation_.created.getName())))
					.getContent();
			if (activeOperations.isEmpty()) {
				// batch is completed (no operations in queue)
				provisioningOperation.setResult(new OperationResult.Builder(OperationState.CREATED).build());
			} else {
				// put to queue, if previous
				ResultModel resultModel = new DefaultResultModel(AccResultCode.PROVISIONING_IS_IN_QUEUE,
						ImmutableMap.of(
								"name", uid,
								"system", system.getName(),
								"operationType", provisioningOperation.getOperationType(),
								"objectClass", provisioningOperation.getProvisioningContext().getConnectorObject().getObjectClass()));
				LOG.debug(resultModel.toString());
				provisioningOperation.setResult(new OperationResult.Builder(OperationState.NOT_EXECUTED).setModel(resultModel).build());
				if (activeOperations.get(0).getResultState() == OperationState.RUNNING) { // the last operation = the first operation and it's running
					// Retry date will be set for the second operation in the queue (the first is running).
					// Other operations will be executed automatically by batch.
					provisioningOperation.setMaxAttempts(provisioningConfiguration.getRetryMaxAttempts());
					batch.setNextAttempt(batchService.calculateNextAttempt(provisioningOperation));
					batch = batchService.save(batch);
				}
				if (securityService.getCurrentId() != null) { // TODO: check logged identity and account owner
					notificationManager.send(
							AccModuleDescriptor.TOPIC_PROVISIONING,
							new IdmMessageDto.Builder(NotificationLevel.WARNING)
									.setModel(provisioningOperation.getResult().getModel())
									.build());
				}
			}
		}
		provisioningOperation.setBatch(batch.getId());
		provisioningOperation = provisioningOperationService.save(provisioningOperation);
		//
		return provisioningOperation;
	}
}
