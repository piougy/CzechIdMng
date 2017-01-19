package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningBatch;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningBatchRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Persists provisioning operation batches
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysProvisioningBatchService
		extends AbstractReadWriteEntityService<SysProvisioningBatch, EmptyFilter> implements SysProvisioningBatchService {

	private final SysProvisioningBatchRepository repository;
	
	@Autowired
	public DefaultSysProvisioningBatchService(
			SysProvisioningBatchRepository repository) {
		super(repository);
		//
		this.repository = repository;
	}
	
	@Override
	@Transactional(readOnly = true)
	public SysProvisioningBatch findBatch(SysProvisioningOperation operation) {
		return repository.findBatch(operation);
	}
}
