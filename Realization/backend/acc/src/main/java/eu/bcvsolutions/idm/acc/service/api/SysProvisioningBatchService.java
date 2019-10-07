package eu.bcvsolutions.idm.acc.service.api;

import java.util.UUID;

import java.time.ZonedDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
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
	 * @param operation
	 * @return Date of the next attempt. Null if there should be no next attempt 
	 */
	ZonedDateTime calculateNextAttempt(SysProvisioningOperationDto operation);
	
	/**
	 * Gets batches to process (cteated requests)
	 * 
	 * @param virtualSystem true - virtual system only, false - normat system, null - all systems
	 * @param pageable
	 * @return
	 */
	Page<SysProvisioningBatchDto> findBatchesToProcess(Boolean virtualSystem, Pageable pageable);
	
	/**
	 * Gets batches to retry
	 * 
	 * @param date
	 * @param pageable
	 * @return
	 */
	Page<SysProvisioningBatchDto> findBatchesToRetry(ZonedDateTime date, Pageable pageable);
	
	/**
	 * Finds batch for given system entity
	 * 
	 * @param systemEntity
	 * @return
	 */
	SysProvisioningBatchDto findBatch(UUID systemEntity);
}
