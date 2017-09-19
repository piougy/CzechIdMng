package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity_;
import eu.bcvsolutions.idm.acc.repository.AccAccountRepository;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.repository.SysSystemEntityRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Entities on target system
 * 
 * @author Radek Tomiška
 *
 */
@Service("sysSystemEntityService")
public class DefaultSysSystemEntityService extends AbstractReadWriteDtoService<SysSystemEntityDto, SysSystemEntity, SysSystemEntityFilter> implements SysSystemEntityService {

	private final SysSystemEntityRepository repository;
	private final AccAccountRepository accountRepository;
	private final SysProvisioningOperationRepository provisioningOperationRepository;
	
	@Autowired
	public DefaultSysSystemEntityService(
			SysSystemEntityRepository systemEntityRepository,
			AccAccountRepository accountRepository,
			SysProvisioningOperationRepository provisioningOperationRepository) {
		super(systemEntityRepository);
		//
		Assert.notNull(accountRepository);
		Assert.notNull(provisioningOperationRepository);
		//
		this.repository = systemEntityRepository;
		this.accountRepository = accountRepository;
		this.provisioningOperationRepository = provisioningOperationRepository;
	}
	
	@Override
	protected Page<SysSystemEntity> findEntities(SysSystemEntityFilter filter, Pageable pageable, BasePermission... permission) {
		if (filter == null) {
			return repository.findAll(pageable);
		}
		return repository.find(filter, pageable);
	}
	
	@Override
	@Transactional
	public void delete(SysSystemEntityDto systemDto, BasePermission... permission) {
		Assert.notNull(systemDto);
		//
		if (provisioningOperationRepository.countBySystemEntity(this.getEntity(systemDto.getId())) > 0) {
			SysSystemDto system = DtoUtils.getEmbedded(systemDto, SysSystemEntity_.system, SysSystemDto.class);
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
	public SysSystemEntityDto getBySystemAndEntityTypeAndUid(SysSystemDto system, SystemEntityType entityType, String uid) {
		return toDto(repository.findOneBySystem_IdAndEntityTypeAndUid(system.getId(), entityType, uid));
	}
}
