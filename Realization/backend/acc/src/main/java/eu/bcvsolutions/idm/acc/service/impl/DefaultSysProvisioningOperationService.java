package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningRequestRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Role could assign identity account on target system.
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysProvisioningOperationService
		extends AbstractReadWriteEntityService<SysProvisioningOperation, EmptyFilter> implements SysProvisioningOperationService {

	private final SysProvisioningRequestRepository provisioningRequestRepository;

	@Autowired
	public DefaultSysProvisioningOperationService(
			SysProvisioningOperationRepository repository,
			SysProvisioningRequestRepository provisioningRequestRepository) {
		super(repository);
		//
		Assert.notNull(provisioningRequestRepository);
		//
		this.provisioningRequestRepository = provisioningRequestRepository;
	}

	@Override
	@Transactional
	public void delete(SysProvisioningOperation provisioningOperation) {
		Assert.notNull(provisioningOperation);
		// delete request
		provisioningRequestRepository.deleteByOperation(provisioningOperation);
		provisioningOperation.setRequest(null);
		// TODO: remove batch
		// TODO: move to archive 
		super.delete(provisioningOperation);
	}
}
