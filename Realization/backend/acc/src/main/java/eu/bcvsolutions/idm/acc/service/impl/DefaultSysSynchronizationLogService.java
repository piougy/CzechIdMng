package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.filter.SyncActionLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncLog;
import eu.bcvsolutions.idm.acc.repository.SysSynchronizationLogRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
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

	private final SysSyncActionLogService syncActionLogService;
	
	@Autowired
	public DefaultSysSynchronizationLogService(SysSynchronizationLogRepository repository,
			SysSyncActionLogService syncActionLogService) {
		super(repository);
		Assert.notNull(syncActionLogService);
		
		this.syncActionLogService = syncActionLogService;
	}
	

	@Override
	@Transactional
	public void delete(SysSyncLog syncLog) {
		Assert.notNull(syncLog);
		//
		// remove all synchronization action logs
		SyncActionLogFilter filter = new SyncActionLogFilter();
		filter.setSynchronizationLogId(syncLog.getId());
		syncActionLogService.find(filter, null).forEach(log -> {
			syncActionLogService.delete(log);
		});
		//
		super.delete(syncLog);
	}

}
