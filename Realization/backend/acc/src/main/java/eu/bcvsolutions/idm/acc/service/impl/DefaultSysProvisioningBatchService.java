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
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningRequestDto;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningBatch;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningBatchRepository;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Persists provisioning operation batches
 * 
 * @author Radek Tomiška
 * @author Filip Mestanek
 *
 */
@Service
public class DefaultSysProvisioningBatchService
		extends AbstractReadWriteDtoService<SysProvisioningBatchDto, SysProvisioningBatch, EmptyFilter> implements SysProvisioningBatchService {

	private final SysProvisioningBatchRepository repository;
	private final SysProvisioningOperationRepository operationRepository;
	
	@Autowired
	public DefaultSysProvisioningBatchService(
			SysProvisioningBatchRepository repository,
			SysProvisioningOperationRepository operationRepository) {
		super(repository);
		//
		Assert.notNull(operationRepository);
		//
		this.repository = repository;
		this.operationRepository = operationRepository;
	}
	
	@Override
	@Transactional(readOnly = true)
	public SysProvisioningBatchDto get(Serializable id, BasePermission ...permissions) {
		SysProvisioningBatchDto batch = super.get(id, permissions);
		return batch;
	}
	
	@Override
	@Transactional(readOnly = true)
	public SysProvisioningBatchDto findBatch(SysProvisioningOperationDto operation) {
		// TODO:update repository method to UUID, not object
		SysProvisioningBatchDto batch = toDto(repository.findBatch(operationRepository.findOne(operation.getId())));
		return batch;
	}
	
	@Override
	@Transactional
	public SysProvisioningBatchDto save(SysProvisioningBatchDto entity, BasePermission ...permissions) {
		return super.save(entity, permissions);
	}
	
	/**
	 * Calculates when the request should be invoked
	 * 
	 * @return Date of the next attempt. Null if there should be no next attempt 
	 */
	@Override
	public DateTime calculateNextAttempt(SysProvisioningRequestDto request) {		
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
	public Page<SysProvisioningBatchDto> findBatchesToRetry(DateTime date, Pageable pageable) {
		return toDtoPage(repository.findByNextAttemptLessThanEqual(date, pageable));
	}
}
