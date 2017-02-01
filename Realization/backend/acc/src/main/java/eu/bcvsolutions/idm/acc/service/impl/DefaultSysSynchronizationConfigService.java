package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSynchronizationConfig;
import eu.bcvsolutions.idm.acc.repository.SysSynchronizationConfigRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSynchronizationConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSynchronizationLogService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Default synchronization config service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSynchronizationConfigService
		extends AbstractReadWriteEntityService<SysSynchronizationConfig, SynchronizationConfigFilter>
		implements SysSynchronizationConfigService {

	private final SysSynchronizationLogService synchronizationLogService;

	@Autowired
	public DefaultSysSynchronizationConfigService(SysSynchronizationConfigRepository repository,
			SysSynchronizationLogService synchronizationLogService) {
		super(repository);
		//
		Assert.notNull(synchronizationLogService);
		//
		this.synchronizationLogService = synchronizationLogService;
	}

	@Override
	@Transactional
	public void delete(SysSynchronizationConfig synchronizationConfig) {
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
	public boolean isRunning(SysSynchronizationConfig config){
		if(config == null){
			return false;
		}
		int count = ((SysSynchronizationConfigRepository)this.getRepository()).runningCount(config);
		return count > 0;
	}

}
