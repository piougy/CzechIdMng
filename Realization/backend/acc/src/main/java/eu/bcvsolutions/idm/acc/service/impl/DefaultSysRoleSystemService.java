package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.repository.SysRoleSystemAttributeRepository;
import eu.bcvsolutions.idm.acc.repository.SysRoleSystemRepository;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Role could assign identity account on target system.
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysRoleSystemService extends AbstractReadWriteDtoService<SysRoleSystemDto, SysRoleSystem, RoleSystemFilter> implements SysRoleSystemService {

	private final SysRoleSystemAttributeRepository roleSystemAttributeRepository;
	private final AccIdentityAccountRepository identityAccountRepository;
	private final IdmRoleService roleService;
	private final SysSystemService systemService;
	
	@Autowired
	public DefaultSysRoleSystemService(
			SysRoleSystemRepository repository,
			SysRoleSystemAttributeRepository roleSystemAttributeRepository,
			AccIdentityAccountRepository identityAccountRepository,
			IdmRoleService roleService,
			SysSystemService systemService) {
		super(repository);
		//
		Assert.notNull(roleSystemAttributeRepository);
		Assert.notNull(identityAccountRepository);
		Assert.notNull(roleService);
		Assert.notNull(systemService);
		//
		this.roleSystemAttributeRepository = roleSystemAttributeRepository;
		this.identityAccountRepository = identityAccountRepository;
		this.roleService = roleService;
		this.systemService = systemService;
	}
	
	
	@Override
	@Transactional
	public void delete(SysRoleSystemDto roleSystem, BasePermission... permission) {
		Assert.notNull(roleSystem);
		// delete attributes
		SysRoleSystem roleSystemEntity = this.getEntity(roleSystem.getId());
		roleSystemAttributeRepository.deleteByRoleSystem(this.getEntity(roleSystem.getId()));
		//
		// clear identityAccounts - only link on roleSystem
		identityAccountRepository.clearRoleSystem(roleSystemEntity);
		//
		super.delete(roleSystem, permission);
	}
	
	@Override
	public SysRoleSystemDto save(SysRoleSystemDto dto, BasePermission... permission) {
		Assert.notNull(dto, "RoleSystem cannot be null!");
		Assert.notNull(dto.getRole(), "Role cannot be null!");
		Assert.notNull(dto.getSystem(), "System cannot be null!");
		
		RoleSystemFilter filter = new RoleSystemFilter();
	    filter.setRoleId(dto.getRole());
	    filter.setSystemId(dto.getSystem());
	    
	    List<SysRoleSystemDto> roleSystems = this.find(filter, null).getContent();
		boolean isDuplicated = roleSystems.stream().filter(roleSystem -> {
			return !roleSystem.getId().equals(dto.getId());
		}).findFirst().isPresent();
		
		if(isDuplicated){
			IdmRoleDto roleDto = roleService.get(dto.getRole());
			SysSystem systemEntity = systemService.get(dto.getSystem());
			throw new ResultCodeException(AccResultCode.ROLE_SYSTEM_ALREADY_EXISTS,
					ImmutableMap.of("role", roleDto.getName(), "system", systemEntity.getName()));
		}
	    
		return super.save(dto, permission);
	}
	
}
