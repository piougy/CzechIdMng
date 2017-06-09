package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningBatch;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningRequest;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningBatchRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Persists provisioning operation batches
 * 
 * @author Radek Tomiška
 * @author Filip Mestanek
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
	public SysProvisioningBatch get(Serializable id) {
		SysProvisioningBatch batch = super.get(id);
		// TODO: remove batch requests list
		if(batch != null) {
			batch.getRequests().size();
		}
		return batch;
	}
	
	@Override
	@Transactional(readOnly = true)
	public SysProvisioningBatch findBatch(SysProvisioningOperation operation) {
		SysProvisioningBatch batch = repository.findBatch(operation);
		// TODO: remove batch requests list
		if(batch != null) {
			batch.getRequests().size();
		}
		return batch;
	}
	
	/**
	 * Calculates when the request should be invoked
	 * 
	 * @return Date of the next attempt. Null if there should be no next attempt 
	 */
	@Override
	public DateTime calculateNextAttempt(SysProvisioningRequest request) {		
		if (request.getCurrentAttempt() >= request.getMaxAttempts()) return null;
		if (request.getCurrentAttempt() == 0) return new DateTime();
		
		List<Integer> sequence = Arrays.asList(120, 300, 1200, 7200, 43200); // TODO: from configuration
		int indexToSequence = Math.min(request.getCurrentAttempt() - 1, sequence.size() - 1);
		
		Integer secInterval = sequence.get(indexToSequence);
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, secInterval);
		
		return new DateTime(calendar.getTime());
	}	
	
	@Override
	@Transactional(readOnly = true)
	public Page<SysProvisioningBatch> findBatchesToRetry(DateTime date, Pageable pageable) {
		return repository.findByNextAttemptLessThanEqual(date, pageable);
	}
}
