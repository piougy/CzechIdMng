package eu.bcvsolutions.idm.acc.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.repository.SysSyncConfigRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Default synchronization config service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSyncConfigService
		extends AbstractReadWriteDtoService<SysSyncConfigDto, SysSyncConfig, SysSyncConfigFilter>
		implements SysSyncConfigService {

	private final SysSyncConfigRepository repository;
	private final SysSyncLogService synchronizationLogService;

	@Autowired
	public DefaultSysSyncConfigService(SysSyncConfigRepository repository,
			SysSyncLogService synchronizationLogService) {
		super(repository);
		//
		Assert.notNull(synchronizationLogService);
		//
		this.repository = repository;
		this.synchronizationLogService = synchronizationLogService;
	}
	
	@Override
	protected Page<SysSyncConfig> findEntities(SysSyncConfigFilter filter, Pageable pageable, BasePermission... permission) {
		if (filter == null) {
			return repository.findAll(pageable);
		}
		return repository.find(filter, pageable);
	}

	@Override
	@Transactional
	public void delete(SysSyncConfigDto synchronizationConfig, BasePermission... permission) {
		Assert.notNull(synchronizationConfig);
		checkAccess(getEntity(synchronizationConfig.getId()), permission);
		//
		// remove all synchronization logs
		SysSyncLogFilter filter = new SysSyncLogFilter();
		filter.setSynchronizationConfigId(synchronizationConfig.getId());
		synchronizationLogService.find(filter, null).forEach(log -> {
			synchronizationLogService.delete(log);
		});
		//
		super.delete(synchronizationConfig);
	}
	
	@Override
	public boolean isRunning(SysSyncConfigDto config){
		if(config == null){
			return false;
		}
		int count = ((SysSyncConfigRepository) this.getRepository())
				.runningCount(((SysSyncConfigRepository) this.getRepository()).findOne(config.getId()));
		return count > 0;
	}
	
	@Override
	public SysSyncConfigDto clone(UUID id) {
		SysSyncConfigDto original = this.get(id);
		Assert.notNull(original, "Config of synchronization must be found!");
		
		// We do detach this entity (and set id to null)
		original.setId(null);
		EntityUtils.clearAuditFields(original);
		return original;
	}


}
