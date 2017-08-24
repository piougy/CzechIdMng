package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.repository.SysSyncConfigRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
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
		extends AbstractReadWriteDtoService<SysSyncConfigDto, SysSyncConfig, SynchronizationConfigFilter>
		implements SysSyncConfigService {

	private final SysSyncLogService synchronizationLogService;
	private final SysSystemMappingService systemMappingService;
	private final ModelMapper modelMapper;

	@Autowired
	public DefaultSysSyncConfigService(SysSyncConfigRepository repository,
			SysSyncLogService synchronizationLogService,
			ModelMapper modelMapper, SysSystemMappingService systemMappingService) {
		super(repository);
		//
		Assert.notNull(synchronizationLogService);
		Assert.notNull(modelMapper);
		Assert.notNull(systemMappingService);
		//
		this.synchronizationLogService = synchronizationLogService;
		this.modelMapper = modelMapper;
		this.systemMappingService = systemMappingService;
	}

	@Override
	@Transactional
	public void delete(SysSyncConfigDto synchronizationConfig, BasePermission... permission) {
		Assert.notNull(synchronizationConfig);
		checkAccess(getEntity(synchronizationConfig.getId()), permission);
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
	public boolean isRunning(SysSyncConfigDto config){
		if(config == null){
			return false;
		}
		int count = ((SysSyncConfigRepository) this.getRepository())
				.runningCount(((SysSyncConfigRepository) this.getRepository()).findOne(config.getId()));
		return count > 0;
	}
	
	@Override
	public SysSyncConfigDto get(Serializable id, BasePermission... permission) {
		// TODO remove this convert after refactor to DTOs
		SysSyncConfigDto dto = toDto(getEntity(id, permission));
		SysSystemMapping mappingEntity = systemMappingService.get(dto.getSystemMapping());
		//
		SysSystemMappingDto mappingDto = modelMapper.map(mappingEntity, SysSystemMappingDto.class);
		Map<String, BaseDto> embedded = dto.getEmbedded();
		embedded.put("systemMapping", mappingDto); // TODO key as string, get from field
		dto.setEmbedded(embedded);
		//
		return dto;
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
