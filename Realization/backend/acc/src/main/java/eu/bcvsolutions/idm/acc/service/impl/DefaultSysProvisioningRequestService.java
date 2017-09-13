package eu.bcvsolutions.idm.acc.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningRequestDto;
import eu.bcvsolutions.idm.acc.dto.filter.ProvisioningRequestFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningRequest;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningRequest_;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningRequestRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningRequestService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;

/**
 * Service for log provisioning
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public class DefaultSysProvisioningRequestService
		extends AbstractReadWriteDtoService<SysProvisioningRequestDto, SysProvisioningRequest, ProvisioningRequestFilter>
		implements SysProvisioningRequestService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(DefaultSysProvisioningRequestService.class);

	
	@Autowired
	public DefaultSysProvisioningRequestService(SysProvisioningRequestRepository repository) {
		super(repository);
	}

	@Override
	public SysProvisioningRequestDto findByOperationId(UUID operationId) {
		ProvisioningRequestFilter filter = new ProvisioningRequestFilter();
		filter.setOperationId(operationId);
		List<SysProvisioningRequestDto> requests = this.find(filter, null).getContent();
		if (requests.isEmpty()) {
			return null;
		}
		// requests must be only one
		if (requests.size() > 1) {
			LOG.error("Operation has more requests!");
			throw new IllegalStateException("Operation has more requests!");
		}
		return requests.get(0);
	}

	@Override
	public Page<SysProvisioningRequestDto> findByBatchId(UUID batchId,  Pageable pageable) {
		ProvisioningRequestFilter filter = new ProvisioningRequestFilter();
		filter.setBatchId(batchId);
		return this.find(filter, pageable);
	}

	@Override
	public List<SysProvisioningRequestDto> getByTimelineAndBatchId(UUID batchId) {
		List<SysProvisioningRequestDto> sortedList = this.findByBatchId(batchId, new PageRequest(0, Integer.MAX_VALUE,
				new Sort(Direction.DESC, SysProvisioningRequest_.created.getName()))).getContent();
		return Collections.unmodifiableList(sortedList);
	}

	@Override
	public SysProvisioningRequestDto getFirstRequestByBatchId(UUID batchId) {
		List<SysProvisioningRequestDto> requests = getByTimelineAndBatchId(batchId);
		return (requests.isEmpty()) ? null : requests.get(0);
	}

	@Override
	public SysProvisioningRequestDto getLastRequestByBatchId(UUID batchId) {
		List<SysProvisioningRequestDto> requests = getByTimelineAndBatchId(batchId);
		return (requests.isEmpty()) ? null : requests.get(requests.size() - 1);
	}

}
