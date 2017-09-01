package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.SystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.repository.AccAccountRepository;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.repository.SysSystemEntityRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Entities on target system
 * 
 * @author Radek Tomiška
 *
 */
@Service("sysSystemEntityService")
public class DefaultSysSystemEntityService extends AbstractReadWriteDtoService<SysSystemEntityDto, SysSystemEntity, SystemEntityFilter> implements SysSystemEntityService {

	private final SysSystemEntityRepository repository;
	private final AccAccountRepository accountRepository;
	private final SysProvisioningOperationRepository provisioningOperationRepository;
	private final SysSystemService systemService;
	
	@Autowired
	public DefaultSysSystemEntityService(
			SysSystemEntityRepository systemEntityRepository,
			AccAccountRepository accountRepository,
			SysProvisioningOperationRepository provisioningOperationRepository,
			SysSystemService systemService) {
		super(systemEntityRepository);
		//
		Assert.notNull(accountRepository);
		Assert.notNull(provisioningOperationRepository);
		Assert.notNull(systemService);
		//
		this.repository = systemEntityRepository;
		this.accountRepository = accountRepository;
		this.provisioningOperationRepository = provisioningOperationRepository;
		this.systemService = systemService;
	}
	
	@Override
	@Transactional
	public void delete(SysSystemEntityDto systemDto, BasePermission... permission) {
		Assert.notNull(systemDto);
		//
		if (provisioningOperationRepository.countBySystemEntity(this.getEntity(systemDto.getId())) > 0) {
			SysSystem system = systemService.get(systemDto.getSystem());
			throw new ResultCodeException(AccResultCode.SYSTEM_ENTITY_DELETE_FAILED_HAS_OPERATIONS,
					ImmutableMap.of("uid", systemDto.getUid(), "system", system.getName()));
		}
		//
		// clear accounts - only link, can be rebuild
		accountRepository.clearSystemEntity(systemDto.getId());
		//
		super.delete(systemDto, permission);
	}

	@Override
	public SysSystemEntityDto getBySystemAndEntityTypeAndUid(SysSystem system, SystemEntityType entityType, String uid) {
		return toDto(repository.findOneBySystem_IdAndEntityTypeAndUid(system.getId(), entityType, uid));
	}
}
