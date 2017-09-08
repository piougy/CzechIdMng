package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningRequestDto;
import eu.bcvsolutions.idm.acc.dto.filter.ProvisioningRequestFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * Service for log provisioning
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface SysProvisioningRequestService
		extends ReadWriteDtoService<SysProvisioningRequestDto, ProvisioningRequestFilter> {

	/**
	 * Return request for operation id.
	 * 
	 * @param operationId
	 * @return
	 */
	SysProvisioningRequestDto findByOperationId(UUID operationId);

	/**
	 * Return requests for batch id.
	 * 
	 * @param batchId
	 * @return
	 */
	Page<SysProvisioningRequestDto> findByBatchId(UUID batchId, Pageable pageable);

	/**
	 * Returns requests for batch id sorted by oldest to newest.
	 * 
	 * @param batchId
	 * @return
	 */
	List<SysProvisioningRequestDto> getByTimelineAndBatchId(UUID batchId);

	/**
	 * Method returns oldest request for batch id.
	 * 
	 * @param batchId
	 * @return
	 */
	SysProvisioningRequestDto getFirstRequestByBatchId(UUID batchId);

	/**
	 * Method return newest request by batch id.
	 * 
	 * @param batchId
	 * @return
	 */
	SysProvisioningRequestDto getLastRequestByBatchId(UUID batchId);
}
