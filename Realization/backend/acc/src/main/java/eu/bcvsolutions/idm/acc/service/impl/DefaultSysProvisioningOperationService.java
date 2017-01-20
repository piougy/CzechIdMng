package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ResultState;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningBatch;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningRequest;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningResult;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningRequestRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.notification.service.api.NotificationManager;

/**
 * Persists provisioning operations
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysProvisioningOperationService
		extends AbstractReadWriteEntityService<SysProvisioningOperation, EmptyFilter> implements SysProvisioningOperationService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSysProvisioningOperationService.class);
	private final SysProvisioningRequestRepository provisioningRequestRepository;
	private final SysProvisioningArchiveService provisioningArchiveService;
	private final SysProvisioningBatchService batchService;
	private final NotificationManager notificationManager;

	@Autowired
	public DefaultSysProvisioningOperationService(
			SysProvisioningOperationRepository repository,
			SysProvisioningRequestRepository provisioningRequestRepository,
			SysProvisioningArchiveService provisioningArchiveService,
			SysProvisioningBatchService batchService,
			NotificationManager notificationManager) {
		super(repository);
		//
		Assert.notNull(provisioningRequestRepository);
		Assert.notNull(provisioningArchiveService);
		Assert.notNull(batchService);
		Assert.notNull(notificationManager);
		//
		this.provisioningRequestRepository = provisioningRequestRepository;
		this.provisioningArchiveService = provisioningArchiveService;
		this.batchService = batchService;
		this.notificationManager = notificationManager;
	}

	@Override
	@Transactional
	public void delete(SysProvisioningOperation provisioningOperation) {
		Assert.notNull(provisioningOperation);
		// create archived operation
		provisioningArchiveService.archive(provisioningOperation);	
		// delete request and empty batch
		SysProvisioningBatch batch = provisioningOperation.getRequest().getBatch();
		if (batch.getRequests().size() <= 1) {
			batchService.delete(batch);
		}
		provisioningRequestRepository.deleteByOperation(provisioningOperation);
		provisioningOperation.setRequest(null);
		//
		super.delete(provisioningOperation);
	}
	
	@Override
	@Transactional
	public void handleFailed(SysProvisioningOperation operation, Exception ex) {
		ResultModel resultModel = new DefaultResultModel(AccResultCode.PROVISIONING_FAILED, 
				ImmutableMap.of(
						"name", operation.getSystemEntityUid(), 
						"system", operation.getSystem().getName(),
						"operationType", operation.getOperationType(),
						"objectClass", operation.getProvisioningContext().getConnectorObject().getObjectClass().getType()));			
		LOG.error(resultModel.toString(), ex);
		//
		SysProvisioningRequest request = operation.getRequest();
		request.increaseAttempt();
		request.setMaxAttempts(6); // TODO: from configuration
		operation.getRequest().setResult(
				new SysProvisioningResult.Builder(ResultState.EXCEPTION).setModel(resultModel).setCause(ex).build());
		//
		save(operation);
		//
		// calculate next attempt
		SysProvisioningBatch batch = request.getBatch();
		if (batch.getFirstRequest().equals(request)) {
			batch.setNextAttempt(batchService.calculateNextAttempt(request));
			batchService.save(batch);
		}
		//
		notificationManager.send(
				AccModuleDescriptor.TOPIC_PROVISIONING, 
				new IdmMessage.Builder().setModel(resultModel).build());
	}
	
	@Override
	@Transactional
	public void handleSuccessful(SysProvisioningOperation operation) {
		ResultModel resultModel = new DefaultResultModel(
				AccResultCode.PROVISIONING_SUCCEED, 
				ImmutableMap.of(
						"name", operation.getSystemEntityUid(), 
						"system", operation.getSystem().getName(),
						"operationType", operation.getOperationType(),
						"objectClass", operation.getProvisioningContext().getConnectorObject().getObjectClass().getType()));
		operation.getRequest().setResult(new SysProvisioningResult.Builder(ResultState.EXECUTED).setModel(resultModel).build());
		save(operation);
		//
		LOG.debug(resultModel.toString());
		notificationManager.send(AccModuleDescriptor.TOPIC_PROVISIONING, new IdmMessage.Builder().setModel(resultModel).build());
	}
}
