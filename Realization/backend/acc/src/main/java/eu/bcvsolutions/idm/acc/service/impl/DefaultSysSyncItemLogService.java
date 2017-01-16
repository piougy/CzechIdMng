package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.SyncItemLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.acc.repository.SysSyncItemLogRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Default synchronization item log service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSyncItemLogService
		extends AbstractReadWriteEntityService<SysSyncItemLog, SyncItemLogFilter>
		implements SysSyncItemLogService {

	@Autowired
	public DefaultSysSyncItemLogService(SysSyncItemLogRepository repository) {
		super(repository);
	}

}
