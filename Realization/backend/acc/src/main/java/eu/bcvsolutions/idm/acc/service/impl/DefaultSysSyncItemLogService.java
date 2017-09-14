package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.filter.SyncItemLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.acc.repository.SysSyncItemLogRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Default synchronization item log service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSyncItemLogService
		extends AbstractReadWriteDtoService<SysSyncItemLogDto, SysSyncItemLog, SyncItemLogFilter>
		implements SysSyncItemLogService {

	private final SysSyncItemLogRepository repository;
	
	@Autowired
	public DefaultSysSyncItemLogService(SysSyncItemLogRepository repository) {
		super(repository);
		//
		this.repository = repository;
	}
	
	@Override
	protected Page<SysSyncItemLog> findEntities(SyncItemLogFilter filter, Pageable pageable, BasePermission... permission) {
		if (filter == null) {
			return repository.findAll(pageable);
		}
		return repository.find(filter, pageable);
	}

}
