package eu.bcvsolutions.idm.acc.service.impl;

import java.util.UUID;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.repository.SysSyncConfigRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;

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
	private final EntityManager entityManager;

	@Autowired
	public DefaultSysSyncConfigService(SysSyncConfigRepository repository,
			SysSyncLogService synchronizationLogService,
			EntityManager entityManager) {
		super(repository);
		//
		Assert.notNull(synchronizationLogService);
		Assert.notNull(entityManager);
		//
		this.synchronizationLogService = synchronizationLogService;
		this.entityManager = entityManager;
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
		int count = ((SysSyncConfigRepository)this.getRepository()).runningCount(config);
		return count > 0;
	}
	
	@Override
	public SysSyncConfig clone(UUID id) {
		SysSyncConfig original = this.get(id);
		Assert.notNull(original, "Config of synchronization must be found!");
		
		// We do detach this entity (and set id to null)
		entityManager.detach(original);
		original.setId(null);
		EntityUtils.clearAuditFields(original);
		return original;
	}


}
