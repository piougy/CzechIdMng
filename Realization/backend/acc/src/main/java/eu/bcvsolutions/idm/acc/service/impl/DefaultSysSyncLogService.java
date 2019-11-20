package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.joda.time.DateTime;
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
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass_;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig_;
import eu.bcvsolutions.idm.acc.entity.SysSyncLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncLog_;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping_;
import eu.bcvsolutions.idm.acc.repository.SysSyncLogRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
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
	
	private final SysSyncActionLogService syncActionLogService;
	
	@Autowired
	public DefaultSysSyncLogService(
			SysSyncLogRepository repository,
			SysSyncActionLogService syncActionLogService, 
			ModelMapper modelMapper) { // model mapper: just for backward compatibility (constructor can be used externally)
		super(repository);
		//
		Assert.notNull(syncActionLogService);
		Assert.notNull(modelMapper);
		//
		this.syncActionLogService = syncActionLogService;
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

	@Override
	protected List<Predicate> toPredicates(Root<SysSyncLog> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			SysSyncLogFilter filter) {
		
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		
		// Sync-configuration ID
		UUID syncConfigId = filter.getSynchronizationConfigId();
		if (syncConfigId != null) {
			predicates.add(builder.equal(root.get(SysSyncLog_.synchronizationConfig).get(AbstractEntity_.id), syncConfigId));
		}
		
		// Sync running
		Boolean running = filter.getRunning();
		if (running != null) {
			predicates.add(builder.equal(root.get(SysSyncLog_.running), running));
		}
		
		// System ID
		UUID systemId = filter.getSystemId();
		if (systemId != null) {
			predicates.add(builder.equal(root.get(SysSyncLog_.synchronizationConfig) //
					.get(SysSyncConfig_.systemMapping) //
					.get(SysSystemMapping_.objectClass) //
					.get(SysSchemaObjectClass_.system) //
					.get(AbstractEntity_.id), systemId));
		}
		
		// From
		DateTime from = filter.getFrom();
		if (from != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(SysSyncLog_.created), from));
		}
		
		// Till
		DateTime till = filter.getTill();
		if (till != null) {
			predicates.add(builder.lessThanOrEqualTo(root.get(SysSyncLog_.created), till));
		}
		
		return predicates;
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
