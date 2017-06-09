package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.filter.SyncActionLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SyncItemLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.acc.repository.SysSyncActionLogRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Default synchronization action log service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSyncActionLogService extends
		AbstractReadWriteEntityService<SysSyncActionLog, SyncActionLogFilter> implements SysSyncActionLogService {

	private final SysSyncItemLogService syncItemLogService;

	@Autowired
	public DefaultSysSyncActionLogService(SysSyncActionLogRepository repository,
			SysSyncItemLogService syncItemLogService) {
		super(repository);
		Assert.notNull(syncItemLogService);

		this.syncItemLogService = syncItemLogService;
	}

	@Override
	@Transactional
	public void delete(SysSyncActionLog syncLog) {
		Assert.notNull(syncLog);
		//
		// remove all synchronization item logs
		SyncItemLogFilter filter = new SyncItemLogFilter();
		filter.setSyncActionLogId(syncLog.getId());
		syncItemLogService.find(filter, null).forEach(log -> {
			syncItemLogService.delete(log);
		});
		//
		super.delete(syncLog);
	}

}
