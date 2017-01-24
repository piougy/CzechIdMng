package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.filter.SyncActionLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.acc.repository.SysSyncActionLogRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Default synchronization action log service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSyncActionLogService
		extends AbstractReadWriteEntityService<SysSyncActionLog, SyncActionLogFilter>
		implements SysSyncActionLogService {

	@Autowired
	public DefaultSysSyncActionLogService(SysSyncActionLogRepository repository) {
		super(repository);
	}

}
