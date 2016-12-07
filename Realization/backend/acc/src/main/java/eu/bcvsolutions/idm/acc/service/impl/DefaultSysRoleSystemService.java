package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.repository.SysRoleSystemAttributeRepository;
import eu.bcvsolutions.idm.acc.repository.SysRoleSystemRepository;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Role could assign identity account on target system.
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysRoleSystemService extends AbstractReadWriteEntityService<SysRoleSystem, RoleSystemFilter> implements SysRoleSystemService {

	private final SysRoleSystemAttributeRepository roleSystemAttributeRepository;
	
	@Autowired
	public DefaultSysRoleSystemService(
			SysRoleSystemRepository repository,
			SysRoleSystemAttributeRepository roleSystemAttributeRepository) {
		super(repository);
		//
		Assert.notNull(roleSystemAttributeRepository);
		//
		this.roleSystemAttributeRepository = roleSystemAttributeRepository;
	}
	
	
	@Override
	@Transactional
	public void delete(SysRoleSystem roleSystem) {
		Assert.notNull(roleSystem);
		// delete attributes
		roleSystemAttributeRepository.deleteByRoleSystem(roleSystem);
		//
		super.delete(roleSystem);
	}
}
