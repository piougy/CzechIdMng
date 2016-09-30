package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.repository.SysSystemRepository;
import eu.bcvsolutions.idm.acc.service.SysSystemService;
import eu.bcvsolutions.idm.core.model.dto.QuickFilter;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.service.impl.AbstractReadWriteEntityService;

/**
 * Deafult target system configuration service 
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysSystemService extends AbstractReadWriteEntityService<SysSystem, QuickFilter> implements SysSystemService {

	@Autowired
	private SysSystemRepository systemRepository;
	
	@Override
	protected BaseRepository<SysSystem> getRepository() {
		return systemRepository;
	}
	
	@Override
	@Transactional(readOnly = true)
	public SysSystem getByName(String name) {
		return systemRepository.findOneByName(name);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<SysSystem> find(QuickFilter filter, Pageable pageable) {
		if (filter == null) {
			return find(pageable);
		}
		return systemRepository.findQuick(filter.getText(), pageable);
	}
}
