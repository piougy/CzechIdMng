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
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningRequestFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningRequest;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningRequest_;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningRequestRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningRequestService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Service for log provisioning
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service
public class DefaultSysProvisioningRequestService
		extends AbstractReadWriteDtoService<SysProvisioningRequestDto, SysProvisioningRequest, SysProvisioningRequestFilter>
		implements SysProvisioningRequestService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSysProvisioningRequestService.class);
	//
	private final SysProvisioningRequestRepository repository;
	
	@Autowired
	public DefaultSysProvisioningRequestService(SysProvisioningRequestRepository repository) {
		super(repository);
		//
		this.repository = repository;
	}
	
	@Override
	protected Page<SysProvisioningRequest> findEntities(SysProvisioningRequestFilter filter, Pageable pageable, BasePermission... permission) {
		if (filter == null) {
			return repository.findAll(pageable);
		}
		return repository.find(filter, pageable);
	}

	@Override
	public SysProvisioningRequestDto findByOperationId(UUID operationId) {
		SysProvisioningRequestFilter filter = new SysProvisioningRequestFilter();
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
		SysProvisioningRequestFilter filter = new SysProvisioningRequestFilter();
		filter.setBatchId(batchId);
		return this.find(filter, pageable);
	}

	@Override
	public List<SysProvisioningRequestDto> getByTimelineAndBatchId(UUID batchId) {
		// sort from higher created
		List<SysProvisioningRequestDto> sortedList = this.findByBatchId(batchId, new PageRequest(0, Integer.MAX_VALUE,
				new Sort(Direction.ASC, SysProvisioningRequest_.created.getName()))).getContent();
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
