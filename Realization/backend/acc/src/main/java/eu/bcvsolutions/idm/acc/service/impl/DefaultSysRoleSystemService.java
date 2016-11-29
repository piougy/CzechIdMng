package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.repository.SysRoleSystemRepository;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Role could assign identity accont on target system.
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysRoleSystemService extends AbstractReadWriteEntityService<SysRoleSystem, RoleSystemFilter> implements SysRoleSystemService {

	@Autowired
	private SysRoleSystemRepository roleSystemRepository;
	
	@Override
	protected AbstractEntityRepository<SysRoleSystem, RoleSystemFilter> getRepository() {
		return roleSystemRepository;
	}
}
