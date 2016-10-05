package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccRoleSystem;
import eu.bcvsolutions.idm.acc.repository.AccRoleSystemRepository;
import eu.bcvsolutions.idm.acc.service.AccRoleSystemService;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;
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
	protected BaseRepository<AccRoleSystem, RoleSystemFilter> getRepository() {
		return roleSystemRepository;
	}
}
