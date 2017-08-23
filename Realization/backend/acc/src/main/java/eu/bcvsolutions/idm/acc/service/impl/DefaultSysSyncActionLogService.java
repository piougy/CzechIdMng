package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import eu.bcvsolutions.idm.acc.dto.SysSyncActionLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.filter.SyncActionLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SyncItemLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.acc.repository.SysSyncActionLogRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Default synchronization action log service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSyncActionLogService extends
		AbstractReadWriteDtoService<SysSyncActionLogDto, SysSyncActionLog, SyncActionLogFilter> implements SysSyncActionLogService {

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
	public void delete(SysSyncActionLogDto syncLog, BasePermission... permission) {
		Assert.notNull(syncLog);
		checkAccess(this.getEntity(syncLog.getId()), permission);
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
	
	@Override
	public SysSyncActionLogDto save(SysSyncActionLogDto dto, BasePermission... permission) {
		Assert.notNull(dto);
		//
		if (!ObjectUtils.isEmpty(permission)) {
			SysSyncActionLog persistEntity = null;
			if (dto.getId() != null) {
				persistEntity = this.getEntity(dto.getId());
				if (persistEntity != null) {
					// check access on previous entity - update is needed
					checkAccess(persistEntity, IdmBasePermission.UPDATE);
				}
			}
			checkAccess(toEntity(dto, persistEntity), permission); // TODO: remove one checkAccess?
		}
		//
		// save
		SysSyncActionLogDto newDto = saveInternal(dto);
		//
		// iterate over all log items
		for (SysSyncItemLogDto item : dto.getLogItems()) {
			item.setSyncActionLog(newDto.getId());
			item = syncItemLogService.save(item);
			newDto.addLogItems(item);
		}
		return newDto;
	}
}
