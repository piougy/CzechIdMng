package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.dto.filter.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.repository.SysRoleSystemAttributeRepository;
import eu.bcvsolutions.idm.acc.repository.SysRoleSystemRepository;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
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
	private final AccIdentityAccountRepository identityAccountRepository;
	
	@Autowired
	public DefaultSysRoleSystemService(
			SysRoleSystemRepository repository,
			SysRoleSystemAttributeRepository roleSystemAttributeRepository,
			AccIdentityAccountRepository identityAccountRepository) {
		super(repository);
		//
		Assert.notNull(roleSystemAttributeRepository);
		Assert.notNull(identityAccountRepository);
		//
		this.roleSystemAttributeRepository = roleSystemAttributeRepository;
		this.identityAccountRepository = identityAccountRepository;
	}
	
	
	@Override
	@Transactional
	public void delete(SysRoleSystem roleSystem) {
		Assert.notNull(roleSystem);
		// delete attributes
		roleSystemAttributeRepository.deleteByRoleSystem(roleSystem);
		//
		// clear identityAccounts - only link on roleSystem
		identityAccountRepository.clearRoleSystem(roleSystem);
		//
		super.delete(roleSystem);
	}
	
	@Override
	public SysRoleSystem save(SysRoleSystem entity) {
		Assert.notNull(entity, "RoleSystem cannot be null!");
		Assert.notNull(entity.getRole(), "Role cannot be null!");
		Assert.notNull(entity.getSystem(), "System cannot be null!");
		
		RoleSystemFilter filter = new RoleSystemFilter();
	    filter.setRoleId(entity.getRole().getId());
	    filter.setSystemId(entity.getSystem().getId());
	    
	    List<SysRoleSystem> roleSystems = this.find(filter, null).getContent();
		boolean isDuplicated = roleSystems.stream().filter(roleSystem -> {
			return !roleSystem.getId().equals(entity.getId());
		}).findFirst().isPresent();
		
		if(isDuplicated){
			throw new ResultCodeException(AccResultCode.ROLE_SYSTEM_ALREADY_EXISTS,
					ImmutableMap.of("role", entity.getRole().getName(), "system", entity.getSystem().getName()));
		}
	    
		return super.save(entity);
	}
	
}
