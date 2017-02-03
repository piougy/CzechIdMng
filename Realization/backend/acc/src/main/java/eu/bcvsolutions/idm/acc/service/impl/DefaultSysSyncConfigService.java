package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.repository.SysSynchronizationConfigRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Default synchronization config service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSyncConfigService
		extends AbstractReadWriteEntityService<SysSyncConfig, SynchronizationConfigFilter>
		implements SysSyncConfigService {

	private final SysSyncLogService synchronizationLogService;

	@Autowired
	public DefaultSysSyncConfigService(SysSynchronizationConfigRepository repository,
			SysSyncLogService synchronizationLogService) {
		super(repository);
		//
		Assert.notNull(synchronizationLogService);
		//
		this.synchronizationLogService = synchronizationLogService;
	}

	@Override
	@Transactional
	public void delete(SysSyncConfig synchronizationConfig) {
		Assert.notNull(synchronizationConfig);
		//
		// remove all synchronization logs
		SynchronizationLogFilter filter = new SynchronizationLogFilter();
		filter.setSynchronizationConfigId(synchronizationConfig.getId());
		synchronizationLogService.find(filter, null).forEach(log -> {
			synchronizationLogService.delete(log);
		});
		//
		super.delete(synchronizationConfig);
	}
	
	@Override
	public boolean isRunning(SysSyncConfig config){
		if(config == null){
			return false;
		}
		int count = ((SysSynchronizationConfigRepository)this.getRepository()).runningCount(config);
		return count > 0;
	}

}
