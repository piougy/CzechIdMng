package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncLog;
import eu.bcvsolutions.idm.acc.repository.SysSynchronizationLogRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSynchronizationLogService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Default synchronization log service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSynchronizationLogService
		extends AbstractReadWriteEntityService<SysSyncLog, SynchronizationLogFilter>
		implements SysSynchronizationLogService {

	@Autowired
	public DefaultSysSynchronizationLogService(SysSynchronizationLogRepository repository) {
		super(repository);
	}

}
