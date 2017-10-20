package eu.bcvsolutions.idm.acc.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncContractConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.entity.SysSyncContractConfig;
import eu.bcvsolutions.idm.acc.repository.SysSyncConfigRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
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
		extends AbstractReadWriteDtoService<AbstractSysSyncConfigDto, SysSyncConfig, SysSyncConfigFilter>
		implements SysSyncConfigService {

	private final SysSyncConfigRepository repository;
	private final SysSyncLogService synchronizationLogService;

	@Autowired
	public DefaultSysSyncConfigService(SysSyncConfigRepository repository, SysSyncLogService synchronizationLogService) {
		super(repository);
		//
		Assert.notNull(synchronizationLogService);
		//
		this.repository = repository;
		this.synchronizationLogService = synchronizationLogService;
	}

	@Override
	public AbstractSysSyncConfigDto save(AbstractSysSyncConfigDto dto, BasePermission... permission) {
		if (dto != null && !this.isNew(dto)) {
			AbstractSysSyncConfigDto persistedConfig = this.get(dto.getId(), permission);
			if (!dto.getClass().equals(persistedConfig.getClass())) {
				throw new ResultCodeException(AccResultCode.SYNCHRONIZATION_CONFIG_TYPE_CANNOT_BE_CANGED,
						ImmutableMap.of("old", persistedConfig.getClass().getSimpleName(), "new",
								persistedConfig.getClass().getSimpleName()));
			}
		}
		return super.save(dto, permission);
	}

	@Override
	protected Page<SysSyncConfig> findEntities(SysSyncConfigFilter filter, Pageable pageable,
			BasePermission... permission) {
		if (filter == null) {
			return repository.findAll(pageable);
		}
		return repository.find(filter, pageable);
	}

	@Override
	protected Class<? extends SysSyncConfig> getEntityClass(AbstractSysSyncConfigDto dto) {
		if (dto instanceof SysSyncContractConfigDto) {
			return SysSyncContractConfig.class;
		}
		return SysSyncConfig.class;
	}

	@Override
	protected Class<? extends AbstractSysSyncConfigDto> getDtoClass(SysSyncConfig entity) {
		if (entity instanceof SysSyncContractConfig) {
			return SysSyncContractConfigDto.class;
		}
		return SysSyncConfigDto.class;
	}

	@Override
	@Transactional
	public void delete(AbstractSysSyncConfigDto synchronizationConfig, BasePermission... permission) {
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
	public boolean isRunning(AbstractSysSyncConfigDto config) {
		if (config == null) {
			return false;
		}
		int count = ((SysSyncConfigRepository) this.getRepository())
				.runningCount(((SysSyncConfigRepository) this.getRepository()).findOne(config.getId()));
		return count > 0;
	}

	@Override
	public AbstractSysSyncConfigDto clone(UUID id) {
		AbstractSysSyncConfigDto original = this.get(id);
		Assert.notNull(original, "Config of synchronization must be found!");

		// We do detach this entity (and set id to null)
		original.setId(null);
		EntityUtils.clearAuditFields(original);
		return original;
	}

	@Override
	public Long countBySystemMapping(SysSystemMappingDto mappingDto) {
		Assert.notNull(mappingDto);
		Assert.notNull(mappingDto.getId());
		return repository.countByCorrelationAttribute_Id(mappingDto.getId());
	}

}
