package eu.bcvsolutions.idm.acc.service.api;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * Persists provisioning operation batches
 * 
 * @author Radek Tomiška
 * @author Filip Mestanek
 *
 */
public interface SysProvisioningBatchService extends ReadWriteDtoService<SysProvisioningBatchDto, EmptyFilter> {
	
	/**
	 * Calculates when the request should be invoked
	 * 
	 * @return Date of the next attempt. Null if there should be no next attempt 
	 */
	DateTime calculateNextAttempt(SysProvisioningRequestDto request);
	
	/**
	 * Gets batches to process (cteated requests)
	 * 
	 * @param pageable
	 * @return
	 */
	Page<SysProvisioningBatchDto> findBatchesToProcess(Pageable pageable);
	
	/**
	 * Gets batches to retry
	 * 
	 * @param date
	 * @param pageable
	 * @return
	 */
	Page<SysProvisioningBatchDto> findBatchesToRetry(DateTime date, Pageable pageable);
}
