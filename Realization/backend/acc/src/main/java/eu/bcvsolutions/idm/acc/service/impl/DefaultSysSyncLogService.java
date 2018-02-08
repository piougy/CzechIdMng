package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysSyncActionLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncActionLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncLog;
import eu.bcvsolutions.idm.acc.repository.SysSyncLogRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Default synchronization log service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSyncLogService
		extends AbstractReadWriteDtoService<SysSyncLogDto, SysSyncLog, SysSyncLogFilter>
		implements SysSyncLogService {

	private final SysSyncLogRepository repository;
	private final SysSyncActionLogService syncActionLogService;
	private final ModelMapper modelMapper;
	
	@Autowired
	public DefaultSysSyncLogService(
			SysSyncLogRepository repository,
			SysSyncActionLogService syncActionLogService, ModelMapper modelMapper) {
		super(repository);
		//
		Assert.notNull(syncActionLogService);
		Assert.notNull(modelMapper);
		//
		this.repository = repository;
		this.syncActionLogService = syncActionLogService;
		this.modelMapper = modelMapper;
	}
	
	@Override
	protected Page<SysSyncLog> findEntities(SysSyncLogFilter filter, Pageable pageable, BasePermission... permission) {
		if (filter == null) {
			return repository.findAll(pageable);
		}
		return repository.find(filter, pageable);
	}
	
	@Override
	public SysSyncLogDto get(Serializable id, BasePermission... permission) {
		SysSyncLogDto dto = super.get(id, permission);
		// fill action list
		dto.setSyncActionLogs(getActionsForLog(dto.getId()));
		return dto;
	}

	@Override
	public Page<SysSyncLogDto> find(SysSyncLogFilter filter, Pageable pageable, BasePermission... permission) {
		Page<SysSyncLogDto> logs = super.find(filter, pageable, permission);
		
		for (SysSyncLogDto log : logs) {
			log.setSyncActionLogs(getActionsForLog(log.getId()));
		}
		
		return logs;
	}
	
	@Override
	protected SysSyncLogDto toDto(SysSyncLog entity, SysSyncLogDto dto) {
		if (entity == null) {
			return null;
		}
		if (dto == null) {
			return modelMapper.map(entity, getDtoClass());
		}
		modelMapper.map(entity, dto);
		return dto;
	}
	
	@Override
	@Transactional
	public void delete(SysSyncLogDto syncLog, BasePermission... permission) {
		Assert.notNull(syncLog);
		checkAccess(this.getEntity(syncLog.getId()), permission);
		//
		// remove all synchronization action logs
		SysSyncActionLogFilter filter = new SysSyncActionLogFilter();
		filter.setSynchronizationLogId(syncLog.getId());
		syncActionLogService.find(filter, null).forEach(log -> {
			syncActionLogService.delete(log);
		});
		//
		super.delete(syncLog);
	}

	/**
	 * Method return all {@link SysSyncActionLogDto} for given log id
	 * 
	 * @param logId
	 * @return
	 */
	private List<SysSyncActionLogDto> getActionsForLog(UUID logId) {
		Assert.notNull(logId);
		//
		SysSyncActionLogFilter filter = new SysSyncActionLogFilter();
		filter.setSynchronizationLogId(logId);
		return syncActionLogService.find(filter, null).getContent();
	}
}
