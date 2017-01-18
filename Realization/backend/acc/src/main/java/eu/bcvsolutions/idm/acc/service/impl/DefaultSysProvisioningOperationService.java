package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningRequestRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Persists provisioning operations
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysProvisioningOperationService
		extends AbstractReadWriteEntityService<SysProvisioningOperation, EmptyFilter> implements SysProvisioningOperationService {

	private final SysProvisioningRequestRepository provisioningRequestRepository;
	private final SysProvisioningArchiveService provisioningArchiveService;

	@Autowired
	public DefaultSysProvisioningOperationService(
			SysProvisioningOperationRepository repository,
			SysProvisioningRequestRepository provisioningRequestRepository,
			SysProvisioningArchiveService provisioningArchiveService) {
		super(repository);
		//
		Assert.notNull(provisioningRequestRepository);
		Assert.notNull(provisioningArchiveService);
		//
		this.provisioningRequestRepository = provisioningRequestRepository;
		this.provisioningArchiveService = provisioningArchiveService;
	}

	@Override
	@Transactional
	public void delete(SysProvisioningOperation provisioningOperation) {
		Assert.notNull(provisioningOperation);
		// create archived operation
		provisioningArchiveService.archive(provisioningOperation);	
		// delete request
		provisioningRequestRepository.deleteByOperation(provisioningOperation);
		provisioningOperation.setRequest(null);
		// TODO: remove empty batch
		super.delete(provisioningOperation);
	}
}
