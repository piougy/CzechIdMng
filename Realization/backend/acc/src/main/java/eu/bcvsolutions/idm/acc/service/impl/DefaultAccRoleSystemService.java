package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.dto.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccRoleSystem;
import eu.bcvsolutions.idm.acc.repository.AccRoleSystemRepository;
import eu.bcvsolutions.idm.acc.service.AccRoleSystemService;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.service.impl.AbstractReadWriteEntityService;

/**
 * Role could assign identity accont on target system.
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultAccRoleSystemService extends AbstractReadWriteEntityService<AccRoleSystem, RoleSystemFilter> implements AccRoleSystemService {

	@Autowired
	private AccRoleSystemRepository roleSystemRepository;
	
	@Override
	protected BaseRepository<AccRoleSystem> getRepository() {
		return roleSystemRepository;
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<AccRoleSystem> find(RoleSystemFilter filter, Pageable pageable) {
		if (filter == null) {
			return find(pageable);
		}
		return roleSystemRepository.findQuick(filter.getRoleId(), filter.getSystemId(), pageable);
	}
}
