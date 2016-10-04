package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.dto.SystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.repository.SysSystemEntityRepository;
import eu.bcvsolutions.idm.acc.service.SysSystemEntityService;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.service.impl.AbstractReadWriteEntityService;

/**
 * Entities on target system
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysSystemEntityService extends AbstractReadWriteEntityService<SysSystemEntity, SystemEntityFilter> implements SysSystemEntityService {

	@Autowired
	private SysSystemEntityRepository systemEntityRepository;
	
	@Override
	protected BaseRepository<SysSystemEntity> getRepository() {
		return systemEntityRepository;
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<SysSystemEntity> find(SystemEntityFilter filter, Pageable pageable) {
		if (filter == null) {
			return find(pageable);
		}
		return systemEntityRepository.findQuick(filter.getSystemId(), filter.getUid(), filter.getEntityType(), pageable);
	}
}
